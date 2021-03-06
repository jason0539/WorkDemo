package com.jason.demo.gradle.test

import org.gradle.api.Plugin
import org.gradle.api.Project

public class TimePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.gradle.addListener(new TimeListener())
        project.task('testTask') << {
            println "Hello gradle plugin"
        }
    }
}