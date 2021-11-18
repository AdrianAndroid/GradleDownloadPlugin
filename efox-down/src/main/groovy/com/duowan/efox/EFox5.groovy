package com.duowan.efox

import com.google.gson.Gson
import org.gradle.api.Project
import org.json.JSONObject

import java.text.SimpleDateFormat

/**
 * 使用单个Path
 */
class EFox5 {
    private final String TAG = "[EFOX]"

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
    EFox5(EfoxExtension extension, Project project) {
        this.extension = extension
        this.project = project
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

    // https://config-api.teachee-backend.com/message/project/langPackages?projectCodes=%s
    // teachee_app_localize
    private List<PackagesData> getPackagesList() {
        String url = String.format(extension.urlFomat, extension.efoxPaths.first())
        def json = readStringFromUrl(url)
        Gson gson = new Gson()
        LanguageModel languageModel = gson.fromJson(json, LanguageModel.class)
        return languageModel.data[0].langPackages
    }

    void downloadEFOX() {
        if (extension.useLog) logSb = new StringBuilder()
        long startTime = System.nanoTime()

        // 思路：
        // 1. 有几个local， 创建几个[value-xx]
        // 2. 把这几个local的Map保存下来

        List<PackagesData> pkgList = getPackagesList()
        // 创建所有的需要保存的文件

        // 创建所有的values, values-zh, values-ko
        pkgList.each { item ->
            String v_dir = extension.valuesDir.get(item.locale) //en -> values, zh -> values-zh
            if (v_dir == null || v_dir.isEmpty()) { // 没有的报错
                log("请配置valuesDir 缺少[${item.locale}]")
                throw RuntimeException("请配置valuesDir 缺少[${item.locale}]")
            }
            File resFile = createFile(v_dir) // 创建 */src/main/res/value-ko/
            logWrite(">>>${resFile.absolutePath}")
        }

        // 获取所有符合要求的HashMap
        // 此时的格式
        // [values:[key's:values's] , values-zh:[key's:values], values-ko:[key's:values]]
        HashMap<String, HashMap<String, String>> valueMaps = new HashMap<>()
        pkgList.each { item ->
            String v_dir = extension.valuesDir.get(item.locale) //en -> values, zh -> values-zh
            HashMap<String, String> v_maps = getKeyValueFromUrl(item.url) // 获取此[values]下所有的key-value
            // 保存起来
            valueMaps.put(v_dir, v_maps) // 将所有的语言保留下来
        }

        // 找出最齐全的key
        HashSet<String> set = new HashSet<>()
        valueMaps.each {
            it.value.each {
                set.add(it.key)
            }
        }

        // 补齐所有的key
        valueMaps.each {
            set.iterator().each { key ->
                if (!it.value.containsKey(key)) {
                    it.value.put(key, "") // 空字符串补全
                }
            }
        }

        // 根据本地比较，并写回到本地
        valueMaps.each {//[values-zh : [key's:values's]'s]
            File resFile = createFile(it.key)  //// 创建 */src/main/res/value-ko/
            File valFile = new File(resFile, extension.resName) // strings.xml

            HashMap<String, String> newMap = it.value

            // 2. 读取本地key-value
            Node oldNode = NodeUtils.readNodeFromLocal(valFile)

            // 3。 比较
            if (oldNode == null) {
                logWrite("本地没有数据， 直接写入")
                // 直接写入本地
                Node wNode = NodeUtils.hashMap2Node(newMap, { v -> replaceValue(v) })
                NodeUtils.writeNode2Local(wNode, valFile)
            } else {
                // 把本地放入到hashmap中
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
            Node newNode = NodeUtils.hashMap2Node(newMap, { v -> v })
            NodeUtils.writeNode2Local(newNode, valFile)
        }

        long endTime = System.nanoTime()
        log("[EFOX] 更新完毕！！用时 = ${endTime - startTime}")
        if (extension.useLog) writeLogToFile(new File(project.getProjectDir(), "log.txt"), logSb.toString())
    }

    // 获取所有符合要求的key-value
    HashMap<String, String> getKeyValueFromUrl(String url) {
        def json = readStringFromUrl(url)
        HashMap<String, String> newMap = new HashMap<>()
        // 下载并读取字符串
        JSONObject jo_data = new JSONObject(json)
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
        return newMap
    }

//    void downloadEFOX_2() {
//        if (extension.useLog) logSb = new StringBuilder()
//        long startTime = System.nanoTime()
//
//        HashMap<String, String> newMap = new HashMap<>()
//        getPackagesList().each { item ->
//            String valueDir = "values-${item.locale}"
//            if (item.locale == "en") {
//                valueDir = "values"
//            }
//            File resFile = createFile(valueDir) // 创建 */src/main/res/value-ko/
//            File valFile = new File(resFile, extension.resName) //string.xml
//            logWrite(">>>${valFile.absolutePath}")
//
//            // 1. 下载所有路径下的key-value
//            newMap.clear()
//            if (true) {
//                def json = readStringFromUrl(item.url)
//                // 下载并读取字符串
//                JSONObject jo_data = new JSONObject(json)
//                // 直接得到合格的key-value
//                jo_data.keys().each { key ->
//                    // 判断是否符合
//                    if (!check_validate(key)) { // 不合格
//                        logWrite("KEY不合法 请在efox修改 [ $key ] ")
//                    } else { // 合格
//                        String v = jo_data.get(key)
//                        newMap.put(key, replaceValue(v))
//                    }
//                }
//            }
//
//            // 2. 读取本地key-value
//            Node oldNode = NodeUtils.readNodeFromLocal(valFile)
//
//            // 3。 比较
//            if (oldNode == null) {
//                logWrite("本地没有数据， 直接写入")
//                // 直接写入本地
//                Node wNode = NodeUtils.hashMap2Node(newMap, { v -> replaceValue(v) })
//                NodeUtils.writeNode2Local(wNode, valFile)
//            } else {
////                logWrite("本地有数据， 增量写入")
//
//                HashMap<String, Node> oldMap = NodeUtils.nodeChild2HashMap(oldNode)
//
//                // 遍历newMap //本次遍历为了日志
//                newMap.keySet().each { newKey ->
//                    String newValue = newMap.get(newKey)
//                    if (oldMap.containsKey(newKey)) {
//                        String oldValue = NodeUtils.getNodeValue(oldMap.get(newKey))
//                        if (newValue != oldValue) {
//                            logWrite("[值不同] $newKey 值不同，已经自动替换！！！ 旧值：$oldValue  新值：$newValue")
//                        }
//                        // 清除oldMap
//                        oldMap.remove(newKey)
//                    } else {
//                        //不包含，直接写入本地
//                        logWrite("[增量] $newKey <==>  值：$newValue")
//                    }
//                }
//                // efox上面已经删除的
//                oldMap.forEach({ key, node ->
//                    logWrite("[删除] $key <==>  值：${NodeUtils.getNodeValue(node)}")
//                })
//            }
//
//            // 4。 写入本地
//            Node newNode = NodeUtils.hashMap2Node(newMap, { v -> v })
//            NodeUtils.writeNode2Local(newNode, valFile)
//
//        }
//
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
        extension.valueReplace.forEach({ k, v ->
            value = value.replace(k, v)//value.replaceAll(k, v)
        })
        value = NodeUtils.escape(value)
        return value
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

class LanguageModel {
    public int code
    public String msg
    public List<LanguageData> data
}

class LanguageData {
    public List<PackagesData> langPackages
}

class PackagesData {
    public String locale
    public String url
}