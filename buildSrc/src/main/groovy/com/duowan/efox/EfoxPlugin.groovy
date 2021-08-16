package com.duowan.efox


import org.gradle.api.Plugin
import org.gradle.api.Project

class EfoxPlugin implements Plugin<Project> {

    static void log(String msg) {
        println("[EfoxPlugin] $msg")
    }

    String opPath = "/Users/flannery/Desktop/yy/GradleDownloadPlugin/library2/src/main/res/values/strings_same.xml"
    // 实现apply方法，注入插件的逻辑
    void apply(Project project) {
        log("apply(Porject project)")

        project.extensions.create('efox', EfoxExtension)
        EfoxExtension extension = project.efox

        project.afterEvaluate {
            extension.efoxPaths.forEach({ pathName ->
                println(pathName)
                project.task('efox_' + pathName) {
                    setGroup('efox')
                    doLast {
                        EFox2 efox2 = new EFox2(extension, project, pathName)
                        efox2.downloadEFox()
                    }
                }
            })
        }

        project.task('efox_中文命名') {
            setGroup("efox")
            doLast {
                println("==============")
                println("==============")
                println("====中文=========")
                println("====中文=========")
                println("====中文=========")
                println("====中文=========")
                println("====中文=========")
                println("==============")
                println("==============")
            }
        }
//        // 文件排序，不做操作 <string>
//        project.task('efoxSort') {
//            setGroup("efox")
//            doLast {
////              String src = "/Users/flannery/Desktop/yy/Educator-android/common/commonres/src/main/res"
//                extension.opSrcs.forEach({ fileName ->
//                    List<NodeData> list = Utils.readXmlTNodeData(new File(fileName))
//                    Utils.sortNodeData(list)
//                    Utils.writeNodeDataToFile(new File(Utils.newFileName(fileName, "sort")), list)
//                })
//            }
//        }

        // 将相同的key进行处理
        project.task('efox_排序') {
            setGroup("efox")
            log('efoxResultSame')
            doLast {
                // 先读取这个xml文件
//                String src = "/Users/flannery/Desktop/yy/Educator-android/common/commonres/src/main/res"
                extension.opSrcs.forEach({ filename ->
                    //HashMap<String, String> map = Utils.readXmlToHashMap_hasSameKey(new File(filename))
                    //Utils.writeHashMapToFileWithSort(new File(Utils.newFileName(filename, "new")), map)
                    // 1. 先读取本地文件
                    Node node = NodeUtils.readNodeFromLocal(new File(filename))
                    NodeUtils.filterNodeChilrenString(node)
                    // 2. 进行排序
                    NodeUtils.sortNodeChilren(node)
                    // 3. 写到本地文件
                    NodeUtils.writeNode2Local(node, new File(Utils.newFileName(filename, "sort")))
                })
            }
        }

        //project.task('efox_' +)

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
//        project.task('efoxclean') {
//            setGroup("efox")
//            log('efoxclean')
//            doLast {
//                log('efoxclean')
//                File resFile = new File(project.getProjectDir(), extension.resPath)
//                if (resFile.exists()) {
//                    Utils.deleteCommonFiles(resFile, extension.resName)
//                }
//                log('efoxclean done!')
//            }
//        }

        project.task('efox_不同文件找出K相同V不同') {
            setGroup("efox")
            setDescription("KEY-VALUE都相同")
            doLast {
                String path_1 = "/Users/flannery/Desktop/yy/Educator-android/common/commonres/src/main/res/values-ko/strings-ko_sort.xml"
                String path_2 = "/Users/flannery/Desktop/yy/GradleDownloadPlugin/原始数据/Educator/merged.dir/values-ko/values-ko_sort.xml"
                Node node_1 = NodeUtils.readNodeFromLocal(new File(path_1))
                Node node_2 = NodeUtils.readNodeFromLocal(new File(path_2)) // 总的
                // 比较两个node
                List<NodeData> listResult = NodeUtils.findSameKV(node_1, node_2)

                // 打印出来
                listResult.forEach({ NodeData data ->
                    if (data.oldValue != "") {
                        println("[samekv] " + data.key)
                        println("[samekv] >>>>>>>> " + data.value)
                        println("[samekv] >>>>>>>> " + data.oldValue)
                        println()
                        println()
                    }
                })
            }
        }

        project.task('efox_同一文件找出K相同V不同') {
            setGroup("efox")
            setDescription("KEY-VALUE都相同")
            doLast {
                extension.opSrcs.forEach({ path ->

                    println("[efox]=========${path}")

                    Map<String, List<NodeData>> map = NodeUtils.findSameKv2(new File(path))
                    map.forEach({ key, list ->
                        list.forEach({ nd ->
                            println("[sameKV] ${nd.key} \t ${nd.value}")
                        })
                    })
                })
            }
        }


        project.task('efox_同一文件找出K相同V相同(key忽略大小写)') {
            setGroup("efox")
            setDescription("KEY-VALUE都相同")
            doLast {
                extension.opSrcs.forEach({ path ->

                    println("[efox]=========${path}")

                    Map<String, List<NodeData>> map = NodeUtils.findSameKv2(new File(path))
                    map.forEach({ key, list ->
                        list.forEach({ nd ->
                            println("[sameKV] ${nd.key} \t ${nd.value}")
                        })
                    })
                })
            }
        }

        project.task('efox_同一文件找出K-V都相同') {
            setGroup("efox")
            setDescription("KEY-VALUE都相同")
            doLast {
                String path = opPath
                List<NodeData> listNode = Utils.readXmlTNodeData(new File(path))
                List<NodeData> listResult = Utils.findSameKV(listNode, true)
                // 打印出来
                listResult.forEach({ NodeData data ->
                    println("[samekv] " + data.key + " = " + data.value)
                })
            }
        }

        // 创建一个任务：比较两个xml中的不一样的key打印出来
        project.task('efox_比较两个XML内容') {
            setGroup("efox")
            doLast {
                String src = extension.opSrcs[0]
                // "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res/values/strings.xml";
                String des = extension.opSrcs[1]
                // "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res/values-ko/strings-ko.xml";
                println(src)
                println(des)
                Utils.compare2Xml(src, des)
            }
        }

        // 将相同的key， 和不同的key分开
        project.task('efox_相同和不相同的key分开') {
            setGroup("efox")
            doLast {
//                String from = "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res"
//                String src_en = "/values/strings.xml"
//                String src_ko = "/values-ko/strings-ko.xml"

                String src_en = extension.opSrcs[0]
                String src_ko = extension.opSrcs[1]
                // 读取原始的xml文件
                Map<String, String> from_en = Utils.readXmlToHashMap(new File(src_en))
                Map<String, String> from_ko = Utils.readXmlToHashMap(new File(src_ko));

//                String to = "/Users/flannery/Desktop/yy/GradleDownloadPlugin/library2/src/main/res";
//                String des_en = "/values/strings_same.xml"
//                String des_en_diff = "/values/strings_diff.xml"
//                String des_ko = "/values-ko/strings_same.xml"
//                String des_ko_diff = "/values-ko/strings_diff.xml"
                HashMap<String, String> to_map = Utils.differenceSRC(from_en, from_ko)
                HashMap<String, String> en_same = to_map.get(Utils.KEY_SAME_SRC)
                HashMap<String, String> en_diff = to_map.get(Utils.KEY_DIFF_SRC)
                // 写入文件
                // 排序
                Utils.writeHashMapToFileWithSort(new File(src_en, "same"), en_same)
                Utils.writeHashMapToFileWithSort(new File(src_ko, "diff"), en_diff)

                HashMap<String, String> to_map2 = Utils.differenceSRC(from_ko, from_en)
                HashMap<String, String> ko_same = to_map2.get(Utils.KEY_SAME_SRC)
                HashMap<String, String> ko_diff = to_map2.get(Utils.KEY_DIFF_SRC)
                //写入文件
                Utils.writeHashMapToFileWithSort(new File(Utils.newFileName(src_ko, "same")), ko_same)
                Utils.writeHashMapToFileWithSort(new File(Utils.newFileName(src_ko, "diff")), ko_diff)
            }
        }

        project.task('efox_补齐两个文件的key') {
            setGroup("efox")
            doLast {
                String s1 = extension.opSrcs[0]
                String s2 = extension.opSrcs[1]
                NodeUtils.fillIn(new File(s1), new File(s2))
            }
        }

        project.task('efox_比较两个文件差几个key') {
            setGroup("efox")
            doLast {
                println("efox_比较两个文件差几个key！")
                String s1 = extension.opSrcs[0]
                String s2 = extension.opSrcs[1]
                NodeUtils.difference(new File(s1), new File(s2))
            }
        }

        project.task('efox_测试') {
            setGroup("efox")
            doLast {
                println("测试大小写！")
                HashMap<String, String> map = new HashMap<>();
                map.put("Cancel", "Cancel")
                map.put("cancel", "cancel")
                println(map)
            }
        }
    }
}

// --------》排序
//     println("===================")
//     String src = "/Users/flannery/Desktop/yy/GradleDownloadPlugin/原始数据/Teachea/merged.dir/values/values.xml"
//     // 1. 先读取
//     Node node = NodeUtils.readNodeFromLocal(new File(src)) //先读取
//     // 2. 再筛选
//     NodeUtils.filterNodeChilrenString(node)
//     // 3. 在排序
//     NodeUtils.sortNodeChilren(node)
//     // 4. 写入本地
//     NodeUtils.writeNode2Local(node, new File(Utils.newFileName(src, "nodes")))