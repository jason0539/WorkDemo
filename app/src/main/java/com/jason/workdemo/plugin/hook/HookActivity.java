package com.jason.workdemo.plugin.hook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.jason.common.utils.MLog;
import com.jason.common.utils.ScreenUtils;

/**
 * Created by liuzhenhui on 2016/12/29.
 * http://weishu.me/2016/02/16/understand-plugin-framework-binder-hook/
 */
public class HookActivity extends Activity {
    public static final String TAG = HookActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MLog.d(MLog.TAG_HOOK, "HookActivity->" + "onCreate ");
        super.onCreate(savedInstanceState);

        //Hook Activity的instrumentation
        try {
            //hook掉Activity的instrumentation
            HookHelper.hookActivityInstrumentation(this);
        } catch (Exception e) {
            MLog.d(MLog.TAG_HOOK, "HookActivity->" + "onCreate hook activity error " + e.toString());
            e.printStackTrace();
        }

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dpToPxInt(this, 50));

        Button tv = new Button(this);
        tv.setLayoutParams(buttonParams);
        // 测试对activity instrument的hook和对AMS的hook
        tv.setText("打开百度网址");
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse("http://www.baidu.com"));
                // 注意这里使用的ApplicationContext 启动的Activity
                // 因为Activity对象的startActivity使用的并不是ContextImpl的mInstrumentation
                // 而是自己的mInstrumentation, 如果你需要这样, 可以自己Hook
                // 比较简单, 直接替换这个Activity的此字段即可.
                getApplicationContext().startActivity(intent);
//                startActivity(intent);
            }
        });
        linearLayout.addView(tv);

        Button btnHookClipboard = new Button(this);
        btnHookClipboard.setLayoutParams(buttonParams);
        btnHookClipboard.setText("HookBinder");
        btnHookClipboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    HookHelper.hookBinderClipboardService();
                } catch (Exception e) {
                    MLog.d(MLog.TAG_HOOK, "HookActivity->" + "hook clipboard failed");
                    e.printStackTrace();
                }
            }
        });
        linearLayout.addView(btnHookClipboard);

        EditText etInput = new EditText(this);
        etInput.setLayoutParams(buttonParams);
        linearLayout.addView(etInput);

        Button btnHookPMS = new Button(this);
        btnHookPMS.setText("测试PMS效果");
        btnHookPMS.setLayoutParams(buttonParams);
        linearLayout.addView(btnHookPMS);
        btnHookPMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 测试PMS HOOK (调用其相关方法)
                getPackageManager().getInstalledApplications(0);
            }
        });

        Button btnStartPluginActivity = new Button(this);
        btnStartPluginActivity.setText("启动插件Activity");
        btnStartPluginActivity.setLayoutParams(buttonParams);
        linearLayout.addView(btnStartPluginActivity);
        btnStartPluginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HookActivity.this, PluginTargetActivity.class);
                HookActivity.this.startActivity(intent);
            }
        });

        setContentView(linearLayout);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        MLog.d(MLog.TAG_HOOK, "HookActivity->" + "attachBaseContext ");
        super.attachBaseContext(newBase);
        try {
            //hook掉ActivityThread的instumentation
//            HookHelper.hookActivityThreadInstrumentation();
        } catch (Exception e) {
            MLog.d(MLog.TAG_HOOK, "HookActivity->" + "attachBaseContext exception = " + e.toString());
        }
        try {
            HookHelper.hookAMS();
        } catch (Exception e) {
            MLog.d(MLog.TAG_HOOK, "HookActivity->" + "attachBaseContext hookAMS failed e = " + e.toString());
        }
        try {
            HookHelper.hookPMS(newBase);
        } catch (Exception e) {
            MLog.d(MLog.TAG_HOOK,"HookActivity->"+"attachBaseContext hookPMS failed = " + e.toString());
        }

        AmsHookHelper.hookActivityThreadHandlerCallback();
    }

}