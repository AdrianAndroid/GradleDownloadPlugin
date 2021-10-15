package com.duowan.efox

import groovy.json.StringEscapeUtils
import org.gradle.api.Project
import org.json.JSONObject

import java.text.SimpleDateFormat

/**
 * 使用单个Path
 */
class EFox2 {
    private final String TAG = "[EFOX]"
    static final String url = "http://multi-lang.duowan.com/multiLangBig/Teachee/%s/%s" //默认的

    static void log(String msg) {
        println("[EFox] $msg")
    }


    private StringBuilder logSb // 记录本地日志

    void logWrite(String msg) {
        def log = "[EFox] $msg"
        println(log)
        if (useLog && logSb != null) logSb.append(log).append("\n")
    }

    private final EfoxExtension extension //从外面传过来的参数
    private final Project project //这个项目


    // 项目地址
    private String efoxPath
    private List<String> efoxPaths
    private String efoxDefaultValues
    private Map valuesDir
    private String resName
    private String resPath
    private String urlFomat
    private Map<String, String> valueReplace
    private Map<String, String> afterValueReplace
    private boolean clearBefore
    private boolean useLog
    private String patternKey // 过滤key的正则表达式  '^[0-9a-zA-Z_]+\$'

    EFox2(EfoxExtension extension, Project project, String efoxPath) {
        this.extension = extension
        this.project = project

        this.resName = extension.resName //这个一般不改变，默认的就是 "commonstring.xml"
        this.efoxPath = efoxPath //"Teachee___2_3_0" //需要修改
        this.efoxDefaultValues = extension.default_values //"values"
        // 所有要下载下来的目录， 应该外部传入
        this.valuesDir = extension.valuesDir //["values": "en", "values-ko": "ko"]
        this.urlFomat = extension.urlFomat
        //"http://multi-lang.duowan.com/multiLangBig/Teachee/%s/%s" // 地址模版
        this.resPath = extension.resPath //src/main/res
        this.valueReplace = extension.valueReplace //["%@": "%s"]
        afterValueReplace = extension.afterValueReplace
        this.clearBefore = extension.clearBefore
        this.useLog = extension.useLog
        this.patternKey = extension.patternKey
    }

    File createFile(String valuedir) {
        File resFile = new File(project.getProjectDir(), resPath)
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


    void downloadEFOX_2() {
        if (useLog) logSb = new StringBuilder()
        long startTime = System.nanoTime()
        // 循环要下载的
        // ["values": "en", "values-ko": "ko"]
        valuesDir.forEach({ value, path ->
            // def url_path = "http://multi-lang.duowan.com/multiLangBig/Teachee/${projectPath}/${pathJson}?time=${System.currentTimeSeconds()}"
            def json = readStringFromUrl(getEfoxUrl(efoxPath, "${path}.json"))
            // 下载并读取字符串
            JSONObject jo = new JSONObject(json)
            JSONObject jo_data = jo.optJSONObject("data") // efox下载的数据

            File resFile = createFile(value) // 创建 */src/main/res/value-ko/commonstring.xml
            File valFile = new File(resFile, resName)

            logWrite(valFile.absolutePath)

            // 读取本地
            Node node = NodeUtils.readNodeFromLocal(valFile)

            if (node == null) {
                logWrite("本地没有数据， 直接写入")
                // 直接写入本地
                Node wNode = NodeUtils.jsonObject2Node(jo_data, { v -> replaceValue(v) })
                NodeUtils.writeNode2Local(wNode, valFile)
            } else {
                logWrite("本地有数据， 增量写入")
                // 增量写入本地
                NodeList result = new NodeList()

                HashMap<String, Node> map = NodeUtils.nodeChild2HashMap(node) // 转换成HashMap

                Iterator<String> iterator = jo_data.keys()
                while (iterator.hasNext()) {
                    String key = iterator.next() // 不校验KEY了， 直接本地改
                    if (!check_validate(key)) {
                        logWrite("KEY不合法 请在efox修改 [ $key ] ")
                        continue
                    }
                    String val = replaceValue(jo_data.opt(key)) // 从根源上就改
                    Node oldNode = map.get(key, null)

                    if (oldNode == null) { // 不包含，新增
                        result.add(NodeUtils.createNode(key, val))
                        logWrite("[增量] $key <==>  值：$val")
                    } else {
                        def oldValue = NodeUtils.getNodeValue(oldNode)
                        // 替换
                        oldNode.replaceNode(NodeUtils.createNode(key, val))
//                        assert val == oldValue , "值不同，请修改key= ${key} \n\t旧值：$oldValue  \n\t新值：$val"
                        if (val != oldValue) {
                            logWrite("[值不同] $key 值不同，已经自动替换！！！ 旧值：$oldValue  新值：$val")
                        }
                    }
                }
                if (result.size() == 0) {
                    logWrite("[增量]  没有增量数据！")
                }
                node.children().addAll(result)
                NodeUtils.writeNode2Local(node, valFile)
            }
        })
        long endTime = System.nanoTime()
        log("[EFOX] 更新完毕！！用时 = ${endTime - startTime}")
        if (useLog) writeLogToFile(new File(project.getProjectDir(), "log.txt"), logSb.toString())
    }



    void downloadEFOX_3() {
        if (useLog) logSb = new StringBuilder()
        long startTime = System.nanoTime()
        // 循环要下载的
        // ["values": "en", "values-ko": "ko"]
        valuesDir.forEach({ value, path ->
            // def url_path = "http://multi-lang.duowan.com/multiLangBig/Teachee/${projectPath}/${pathJson}?time=${System.currentTimeSeconds()}"
            def json = readStringFromUrl(getEfoxUrl(efoxPath, "${path}.json"))
            // 下载并读取字符串
            JSONObject jo = new JSONObject(json)
            JSONObject jo_data = jo.optJSONObject("data") // efox下载的数据

            File resFile = createFile(value) // 创建 */src/main/res/value-ko/commonstring.xml
            File valFile = new File(resFile, resName)

            logWrite(valFile.absolutePath)

            // 读取本地
            Node node = NodeUtils.readNodeFromLocal(valFile)

            if (node == null) {
                logWrite("本地没有数据， 直接写入")
                // 直接写入本地
                Node wNode = NodeUtils.jsonObject2Node(jo_data, { v -> replaceValue(v) })
                NodeUtils.writeNode2Local(wNode, valFile)
            } else {
                logWrite("本地有数据， 增量写入")
                // 增量写入本地
                Node resNode = NodeUtils.createResourceNode();

                HashMap<String, Node> map = NodeUtils.nodeChild2HashMap(node) // 转换成HashMap

                Iterator<String> iterator = jo_data.keys()
                while (iterator.hasNext()) {
                    String key = iterator.next() // 不校验KEY了， 直接本地改
                    if (!check_validate(key)) {
                        logWrite("KEY不合法 请在efox修改 [ $key ] ")
                        continue
                    }
                    String val = replaceValue(jo_data.opt(key)) // 从根源上就改
                    Node oldNode = map.get(key, null) // 获取本地相同的key
                    if (oldNode == null) { // 不包含，新增
                        Node n = NodeUtils.createNode(key, val)
                        resNode.append(n)
                        logWrite("[增量] $key <==>  值：$val") // 日志
                    } else { // 包含
                        def oldValue = NodeUtils.getNodeValue(oldNode) // 获取本地
                        if (val != oldValue) {
                            logWrite("[值不同] $key 值不同，已经自动替换！！！ 旧值：$oldValue  新值：$val")
                        }
                        resNode.append(NodeUtils.createNode(key, val))
                        // 剔除本地key
                        map.remove(oldValue)
                    }
                }
//                if (!map.isEmpty()) {
//                    map.each {
//                        def key = NodeUtils.getNodeKey(it.value)
//                        def val = NodeUtils.getNodeValue(it.value)
//                        logWrite("[删除] $key  >>>  $val")
//                    }
//                }

                // 新生成的
                NodeUtils.writeNode2Local(resNode, valFile)
            }
        })
        long endTime = System.nanoTime()
        log("[EFOX] 更新完毕！！用时 = ${endTime - startTime}")
        if (useLog) writeLogToFile(new File(project.getProjectDir(), "log.txt"), logSb.toString())
    }



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
        assert this.patternKey != null, "校验key的正则不能是这样子啦"
        return str.matches(this.patternKey)
    }

}