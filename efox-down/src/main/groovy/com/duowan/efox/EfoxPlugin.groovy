package com.duowan.efox


import org.gradle.api.Plugin
import org.gradle.api.Project

class EfoxPlugin implements Plugin<Project> {

    static void log(String msg) {
        println("[EfoxPlugin] $msg")
    }

    // 实现apply方法，注入插件的逻辑
    void apply(Project project) {
        log("apply(Porject project)")

        project.extensions.create('efox', EfoxExtension)
        EfoxExtension extension = project.efox

        project.afterEvaluate {

            project.task('sync') {
                setGroup('language')
                doLast {
                    new EFox5(extension, project).downloadEFOX()
                }
            }
        }
    }

}