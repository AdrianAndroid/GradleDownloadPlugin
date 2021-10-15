package com.duowan.efox


import org.gradle.api.Project
import org.json.JSONObject

import java.text.SimpleDateFormat

/**
 * 使用单个Path
 */
class EFox4 {
    private final String TAG = "[EFOX]"
    static final String url = "http://multi-lang.duowan.com/multiLangBig/Teachee/%s/%s" //默认的

    static void log(String msg) {
        println("[EFox] $msg")
    }


    private StringBuilder logSb // 记录本地日志

    void logWrite(String msg) {
        def log = "[EFox] $msg"
        println(log)
        if (extension.useLog && logSb != null) logSb.append(log).append("\n")
    }

    private final EfoxExtension extension //从外面传过来的参数
    private final Project project //这个项目


    // 项目地址
    private String efoxDefaultValues
    private String urlFomat
    private Map<String, String> valueReplace

    EFox4(EfoxExtension extension, Project project) {
        this.extension = extension
        this.project = project

        this.efoxDefaultValues = extension.default_values //"values"
        // 所有要下载下来的目录， 应该外部传入
        this.urlFomat = extension.urlFomat
        //"http://multi-lang.duowan.com/multiLangBig/Teachee/%s/%s" // 地址模版
        this.valueReplace = extension.valueReplace //["%@": "%s"]
    }

    File createFile(String valuedir) {
        File resFile = new File(project.getProjectDir(), extension.resPath)
        if (resFile.mkdirs()) {// 就创建
            log("创建了文件夹 resPath = ${resFile.absolutePath}")
        } else {
            log("不用创建文件夹 resPath = ${resFile.absolutePath}")
        }
        File resValue = new File(resFile, valuedir)
        if (resValue.mkdirs()) {// 就创建
            log("创建了文件夹 resPath = ${resValue.absolutePath}")
        } else {
            log("不用创建文件夹 resPath = ${resValue.absolutePath}")
        }
        return resValue
    }

    void downloadEFOX() {
        if (extension.useLog) logSb = new StringBuilder()
        long startTime = System.nanoTime()

        HashMap<String, String> newMap = new HashMap<>()
        extension.valuesDir.forEach({ value/*values-ko*/, path /*ko*/ ->
            File resFile = createFile(value) // 创建 */src/main/res/value-ko/
            File valFile = new File(resFile, extension.resName) //string.xml
            logWrite(">>>${valFile.absolutePath}")

            // 1. 下载所有路径下的key-value
            newMap.clear()
            extension.efoxPaths.each { url ->
                def json = readStringFromUrl(getEfoxUrl(url, "${path}.json"))
                // 下载并读取字符串
                JSONObject jo_data = new JSONObject(json).optJSONObject("data")
                // 直接得到合格的key-value
                jo_data.keys().each { key ->
                    // 判断是否符合
                    if (!check_validate(key)) { // 不合格
                        logWrite("KEY不合法 请在efox修改 [ $key ] ")
                    } else { // 合格
                        String v = jo_data.get(key)
                        newMap.put(key, replaceValue(v))
                    }
                }
            }

            // 2. 读取本地key-value
            Node oldNode = NodeUtils.readNodeFromLocal(valFile)

            // 3。 比较
            if (oldNode == null) {
                logWrite("本地没有数据， 直接写入")
                // 直接写入本地
                Node wNode = NodeUtils.hashMap2Node(newMap, { v -> replaceValue(v) })
                NodeUtils.writeNode2Local(wNode, valFile)
            } else {
//                logWrite("本地有数据， 增量写入")

                HashMap<String, Node> oldMap = NodeUtils.nodeChild2HashMap(oldNode)

                // 遍历newMap //本次遍历为了日志
                newMap.keySet().each { newKey ->
                    String newValue = newMap.get(newKey)
                    if (oldMap.containsKey(newKey)) {
                        String oldValue = NodeUtils.getNodeValue(oldMap.get(newKey))
                        if (newValue != oldValue) {
                            logWrite("[值不同] $newKey 值不同，已经自动替换！！！ 旧值：$oldValue  新值：$newValue")
                        }
                        // 清除oldMap
                        oldMap.remove(newKey)
                    } else {
                        //不包含，直接写入本地
                        logWrite("[增量] $newKey <==>  值：$newValue")
                    }
                }
                // efox上面已经删除的
                oldMap.forEach({ key, node ->
                    logWrite("[删除] $key <==>  值：${NodeUtils.getNodeValue(node)}")
                })
            }

            // 4。 写入本地
            Node newNode = NodeUtils.hashMap2Node(newMap, { v -> v})
            NodeUtils.writeNode2Local(newNode, valFile)

        })

        long endTime = System.nanoTime()
        log("[EFOX] 更新完毕！！用时 = ${endTime - startTime}")
        if (extension.useLog) writeLogToFile(new File(project.getProjectDir(), "log.txt"), logSb.toString())
    }

//    void downloadEFOX_bak() {
//        if (extension.useLog) logSb = new StringBuilder()
//        long startTime = System.nanoTime()
//        // 循环要下载的
//        // ["values": "en", "values-ko": "ko"]
//        valuesDir.forEach({ value, path ->
//            // def url_path = "http://multi-lang.duowan.com/multiLangBig/Teachee/${projectPath}/${pathJson}?time=${System.currentTimeSeconds()}"
//            def json = readStringFromUrl(getEfoxUrl(efoxPath, "${path}.json"))
//            // 下载并读取字符串
//            JSONObject jo = new JSONObject(json)
//            JSONObject jo_data = jo.optJSONObject("data")
//
//            File resFile = createFile(value) // 创建 */src/main/res/value-ko/commonstring.xml
//            File valFile = new File(resFile, resName)
//
//            logWrite(valFile.absolutePath)
//
//            // 读取本地
//            Node node = NodeUtils.readNodeFromLocal(valFile)
//
//            if (node == null) {
//                logWrite("本地没有数据， 直接写入")
//                // 直接写入本地
//                Node wNode = NodeUtils.jsonObject2Node(jo_data, { v -> replaceValue(v) })
//                NodeUtils.writeNode2Local(wNode, valFile)
//            } else {
//                logWrite("本地有数据， 增量写入")
//                // 增量写入本地
//                NodeList result = new NodeList()
//
//                HashMap<String, Node> map = NodeUtils.nodeChild2HashMap(node) // 转换成HashMap
//
//                Iterator<String> iterator = jo_data.keys()
//                while (iterator.hasNext()) {
//                    String key = iterator.next() // 不校验KEY了， 直接本地改
//                    if (!check_validate(key)) {
//                        logWrite("KEY不合法 请在efox修改 [ $key ] ")
//                        continue
//                    }
//                    String val = replaceValue(jo_data.opt(key)) // 从根源上就改
//                    Node oldNode = map.get(key, null)
//
//                    if (oldNode == null) { // 不包含，新增
//                        result.add(NodeUtils.createNode(key, val))
//                        logWrite("[增量] $key <==>  值：$val")
//                    } else {
//                        def oldValue = NodeUtils.getNodeValue(oldNode)
//                        // 替换
//                        oldNode.replaceNode(NodeUtils.createNode(key, val))
////                        assert val == oldValue , "值不同，请修改key= ${key} \n\t旧值：$oldValue  \n\t新值：$val"
//                        if (val != oldValue) {
//                            logWrite("[值不同] $key 值不同，已经自动替换！！！ 旧值：$oldValue  新值：$val")
//                        }
//                    }
//                }
//                if (result.size() == 0) {
//                    logWrite("[增量]  没有增量数据！")
//                }
//                node.children().addAll(result)
//                NodeUtils.writeNode2Local(node, valFile)
//            }
//        })
//        long endTime = System.nanoTime()
//        log("[EFOX] 更新完毕！！用时 = ${endTime - startTime}")
//        if (extension.useLog) writeLogToFile(new File(project.getProjectDir(), "log.txt"), logSb.toString())
//    }


    // 将xml写入本地文件
    private void writeLogToFile(File file, String log) {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)))
        //
        // 重新写文件
        bufferedWriter.writeLine("<-------------------------------->")
        bufferedWriter.writeLine("<-----此文件最好不要加入版本控制----->")
        bufferedWriter.writeLine("<------------此文件可以随便删掉----->")
        def time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
        bufferedWriter.writeLine("<-----$time----->")
        bufferedWriter.writeLine(log)
        bufferedWriter.writeLine("<------------->\n\n\n\n")
        bufferedWriter.flush()
        bufferedWriter.close()
    }

    private String replaceValue(String value) {
        if (value == null || value.size() == 0) return ""
        valueReplace.forEach({ k, v ->
            value = value.replace(k, v)//value.replaceAll(k, v)
        })
        value = NodeUtils.escape(value)
//        valueReplace.forEach({ k, v ->
//            value = value.replace(k, v)//value.replaceAll(k, v)
//        })
        return value
    }

    private String getEfoxUrl(String efoxProjectPath, String pathJson) {
        //http://multi-lang.duowan.com/multiLangBig/Teachee/iOS_1_9_0___1_9_0/ko.json
//        return "http://multi-lang.duowan.com/multiLangBig/Teachee/${efoxProjectPath}/${pathJson}?time=${time}"
        def baseUrl = String.format(urlFomat, efoxProjectPath, pathJson)
        return "$baseUrl?time=${System.currentTimeSeconds()}" // 加time，为了防止缓存， ios这么加的
    }

    // 从网络读取内容
    private String readStringFromUrl(String url_path) {
        //https://blog.csdn.net/zhangmiao301/article/details/80839676
        URL url = new URL(url_path)
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection()
        httpURLConnection.setConnectTimeout(5000)
        httpURLConnection.setRequestMethod("GET")
        httpURLConnection.connect()
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()))
        StringBuffer stringBuffer = new StringBuffer()
        String json = ""
        while ((json = bufferedReader.readLine()) != null) {
            stringBuffer.append(json)
        }
        String rsp = new String(stringBuffer.toString().getBytes(), "UTF-8")
        bufferedReader.close()
        httpURLConnection.disconnect()
        return rsp
    }

    Boolean check_validate(String str) {
        assert extension.patternKey != null, "校验key的正则不能是这样子啦"
        return str.matches(extension.patternKey)
    }

}