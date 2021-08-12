package com.duowan.efox

import jdk.jshell.execution.Util
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
            project.task('efoxtest' + i) {
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

        // 文件排序，不做操作 <string>
        project.task('efoxSort') {
            setGroup("efox")
            doLast {
                String src = "/Users/flannery/Desktop/yy/Educator-android/common/commonres/src/main/res"
                String src_en = "/values/strings.xml"
                String src_ko = "/values-ko/strings-ko.xml"

                String src_en_sort = "/values/strings_sort.xml"
                String src_ko_sort = "/values-ko/strings-ko_sort.xml"

                List<String> _old = [src_en, src_ko]
                List<String> _new = [src_en_sort, src_ko_sort]

                for (i in 0..<_old.size()) {
                    List<NodeData> list = Utils.readXmlTNodeData(new File(src, _old[i]))
                    list = Utils.sortNodeData(list)
                    Utils.writeNodeDataToFile(new File(src, _new[i]), list)
                }
            }
        }

        // 将相同的key进行处理
        project.task('efoxResultSame') {
            setGroup("efox")
            log('efoxResultSame')
            doLast {
                // 先读取这个xml文件
                String src = "/Users/flannery/Desktop/yy/Educator-android/common/commonres/src/main/res"
                String src_en = "/values/strings.xml"
                String src_en_new = "/values/strings_new.xml"

                String src_ko = "/values-ko/strings-ko.xml"
                String src_ko_new = "/values-ko/strings-ko_new.xml"

                List<String> _old = [src_en, src_ko]
                List<String> _new = [src_en_new, src_ko_new]

                for (i in 0..<_old.size()) {
                    HashMap<String, String> map = Utils.readXmlToHashMap_hasSameKey(new File(src, _old[i]))
                    Utils.writeHashMapToFileWithSort(new File(src, _new[i]), map)
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
                    Utils.deleteCommonFiles(resFile, extension.resName)
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
                Utils.compare2Xml(src, des)
            }
        }

        // 将相同的key， 和不同的key分开
        project.task('disguishXmlFile') {
            setGroup("efox")
            doLast {
                String from = "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res"
                String src_en = "/values/strings.xml";
                String src_ko = "/values-ko/strings-ko.xml";
                Map<String, String> from_en = Utils.readXmlToHashMap(new File(from, src_en))
                Map<String, String> from_ko = Utils.readXmlToHashMap(new File(from, src_ko));

                String to = "/Users/flannery/Desktop/yy/GradleDownloadPlugin/library2/src/main/res";
                String des_en = "/values/strings_same.xml"
                String des_en_diff = "/values/strings_diff.xml"
                String des_ko = "/values-ko/strings_same.xml"
                String des_ko_diff = "/values-ko/strings_diff.xml"
                HashMap<String, String> to_map = Utils.differenceSRC(from_en, from_ko)
                HashMap<String, String> en_same = to_map.get(Utils.KEY_SAME_SRC)
                HashMap<String, String> en_diff = to_map.get(Utils.KEY_DIFF_SRC)
                // 写入文件
                // 排序
                Utils.writeHashMapToFileWithSort(new File(to, des_en), en_same)
                Utils.writeHashMapToFileWithSort(new File(to, des_en_diff), en_diff)

                HashMap<String, String> to_map2 = Utils.differenceSRC(from_ko, from_en)
                HashMap<String, String> ko_same = to_map2.get(Utils.KEY_SAME_SRC)
                HashMap<String, String> ko_diff = to_map2.get(Utils.KEY_DIFF_SRC)
                //写入文件
                Utils.writeHashMapToFileWithSort(new File(to, des_ko), ko_same)
                Utils.writeHashMapToFileWithSort(new File(to, des_ko_diff), ko_diff)
            }
        }

    }
}
