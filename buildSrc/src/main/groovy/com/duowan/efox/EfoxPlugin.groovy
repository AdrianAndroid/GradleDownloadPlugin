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

        EfoxExtension extension = project.extensions.create('efox', EfoxExtension)

        // 创建一个新的task
        project.task('efoxdownload') {
            setGroup("efox")
            log('efoxdownload')
            doLast {
                log(" ${extension.message}")
                log(" ${project.getProjectDir().absolutePath} ")
                EFox efox = new EFox(extension, project)
                efox.downloadEFox()
                //downloadEfox(project)
            }
        }

        // 清空values文件夹， 应该只是清空commonstring.xml 文件
        project.task('efoxclean') {
            setGroup("efox")
            log('efoxclean')
            doLast {
                log('efoxclean')
                File resFile = new File(project.getProjectDir(), extension.resPath)
                if (resFile.exists()) {
                    deleteCommonFiles(resFile, extension.resName)
                }
                log('efoxclean done!')
            }
        }
    }

    static void deleteCommonFiles(File resDir, String comName) {
        File[] files = resDir.listFiles()
        files.each {f ->
            if(f.isFile() && f.getName() == comName) {
                f.delete()
            } else if(f.isDirectory()) {
                deleteCommonFiles(f, comName)
            }
        }
    }

}
