package com.duowan.efox

import com.sun.xml.bind.Util
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

        project.task('efoxtest100') {
            setGroup("efox")
            doLast {
                println("==============")
                println("==============")
                println("====$i=========")
                println("====$i=========")
                println("====$i=========")
                println("====$i=========")
                println("====$i=========")
                println("==============")
                println("==============")
            }
        }

        for (i in 0..<10) {
            project.task('efoxtest'+i) {
                setGroup("efox")
                doLast {
                    println("==============")
                    println("==============")
                    println("====$i=========")
                    println("====$i=========")
                    println("====$i=========")
                    println("====$i=========")
                    println("====$i=========")
                    println("==============")
                    println("==============")
                }
            }
        }

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

        // 创建一个任务：比较两个xml中的不一样的key打印出来
        project.task('compare2xml') {
            setGroup("efox")
            doLast {
                String src = "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res/values/strings.xml";
                String des = "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res/values-ko/strings-ko.xml";
                println(src)
                println(des)
                compare2Xml(src, des)
            }
        }
        project.task('disguishXmlFile'){
            setGroup("efox")
            doLast {

            }
        }

    }
    static void compare2Xml(String src, String des) {
        Map<String, String> srcMap = Utils.readXmlToHashMap(new File(src));
        Map<String, String> desMap = Utils.readXmlToHashMap(new File(des));
        // 比较两个map， 得到不相同的key
        HashMap<String, String> mapSrc = Utils.differenceSRC(srcMap, desMap)
        Map<String, String> KEY_DIFF_SRC = mapSrc[Utils.KEY_DIFF_SRC]
        println("KEY_DIFF_SRC")
        Utils.printMap(KEY_DIFF_SRC)
        println("换个方向")

        HashMap<String, String> mapDes = Utils.differenceSRC(srcMap, desMap)
        Map<String, String> KEY_DIFF_SRC2 = mapDes[Utils.KEY_DIFF_SRC]
        println("KEY_DIFF_SRC2")
        Utils.printMap(KEY_DIFF_SRC2)
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
