package com.jason.demo.gradle.asm

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.jason.demo.gradle.MyInject
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Created by HuaChao on 2016/7/4.
 * https://juejin.im/entry/577b03438ac2470061afb130
 */
class AsmPluginTransform extends Transform {

    Project project

    // 构造函数，我们将Project保存下来备用
    AsmPluginTransform(Project project) {
        this.project = project
    }

    // 设置我们自定义的Transform对应的Task名称
    @Override
    String getName() {
        return "asmTrans"
    }

    // 指定输入的类型，通过这里的设定，可以指定我们要处理的文件类型
    //这样确保其他类型的文件不会传入
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    @Override
    Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {
        boolean inject = project['injectParam'].inject
        AsmPlugin.logger.lifecycle("======================transform from asm ======= " + inject)

        //仿照shrinker的实现，在processor中使用asm编辑文件，可以根据要实现的不同编辑目标，传入不同的function，里面已经处理好并且保存到dst，其实可以保存回原处，多次处理之后一次性保存到dst
        //https://www.diycode.cc/topics/581
        //http://www.wangyuwei.me/2017/01/20/ASM-%E6%93%8D%E4%BD%9C%E5%AD%97%E8%8A%82%E7%A0%81%E5%88%9D%E6%8E%A2/
        if (inject) {
            new ClassSimpleProcessor(inputs, outputProvider, new ClassVisitorFunction()).proceed()
        } else {
            // Transform的inputs有两种类型，一种是目录，一种是jar包，要分开遍历
            // 以下是把class从 来处 写到 去处 ，写之前可以用javassist或者asm对字节码做编辑，如果没有改动原样写入，当前plugin相当于什么都没做
            inputs.each { TransformInput input ->

                //对类型为“文件夹”的input进行遍历
                input.directoryInputs.each { DirectoryInput directoryInput ->

                    //使用javassist编辑原文件
                    MyInject.injectDir(directoryInput.file.absolutePath,"com/jason/workdemo")

                    // 获取output目录
                    def dest = outputProvider.getContentLocation(directoryInput.name,
                            directoryInput.contentTypes, directoryInput.scopes,
                            Format.DIRECTORY)

                    // 将input的目录复制到output指定目录
                    FileUtils.copyDirectory(directoryInput.file, dest)
                }

                //对类型为jar文件的input进行遍历
                input.jarInputs.each { JarInput jarInput ->

                    //生成输出路径
                    def dest = outputProvider.getContentLocation(jarInput.name,
                            jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    //将输入内容复制到输出
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }
        }
    }
}