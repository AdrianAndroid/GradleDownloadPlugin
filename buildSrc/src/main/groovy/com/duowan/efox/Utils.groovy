package com.duowan.efox

import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation

import java.util.regex.Matcher
import java.util.regex.Pattern

final class Utils {


//    final static Map<String, String> valueReplace = ["&": "&amp;", "%@": "%s", "\'":"\\\'"]
    final static Map<String, String> valueReplace = ["&": "&amp;", "%@": "%s"]


    static void printlnLog(String... text) {
        if (text.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(">>")
            for (i in 0..<text.length) {
                sb.append(text[i]).append("\t-\t")
            }
            println(sb.toString())
        }
    }

    // 替换空格
    static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\\t|\\r|\\n")
            Matcher m = p.matcher(str)
            dest = m.replaceAll(str)
        }
        return dest;
    }

    static List<NodeData> sortNodeData(List<NodeData> list) {
        list.sort(new Comparator<NodeData>() {
            @Override
            int compare(NodeData o1, NodeData o2) {
                //return o1.key > o2.key
                return DefaultTypeTransformation.compareTo(o1.key, o2.key)
            }
        })
        return list
    }

    static List<NodeData> readXmlTNodeData(File xmlFile) {
        assert xmlFile.exists() && xmlFile.size() > 0 //这里不能为空，按理说已经判断了
        println ">>>>>>>>>>>>>>>>>>>>"
        println(xmlFile.absolutePath)
        def xml = new XmlParser().parse(xmlFile) // 读出来
        println xml.children().size()
        println ">>>>>>>>读取xml开始>>>>>>>>>>>>>>>>>>>>"
        List<NodeData> list = new ArrayList<>();
        xml.children().forEach({
            if (it instanceof Node && "string" == it.name()) {
                def key = it.attributes()['name']
                def valueList = ((List) it.value())
                if (valueList.size() > 0) {
                    def value = valueList.get(0)
                    list.add(new NodeData(key, value))
                } else {
                    list.add(new NodeData(key, ''))
                    println("$it 不符合要求!! 保留key，放入了空值")
                }
            }
        })
        println ">>>>>>>读取xml结束>>>>>>>>>>>>>>>>>>>>>"
        //println xml
        return list
    }


    // 把本地文件全部读出来
    // key和value的全部删除
    // key相同，value不同的打印出来
    static Map<String, String> readXmlToHashMap_hasSameKey(File xmlFile) {
        assert xmlFile.exists() && xmlFile.size() > 0 //这里不能为空，按理说已经判断了
        println ">>>>>>>>>>>>>>>>>>>>"
        println(xmlFile.absolutePath)
        def xml = new XmlParser().parse(xmlFile) // 读出来
        println xml.children().size()
        println ">>>>>>>>读取xml开始>>>>>>>>>>>>>>>>>>>>"
        def hashMap = new HashMap() // 现在可以为空
        xml.children().forEach({
            if (it instanceof Node && "string" == it.name()) {
                def key = it.attributes()['name']
                def valueList = ((List) it.value())
                if (valueList.size() > 0) {
                    def value = valueList.get(0)
                    if (null != hashMap.put(key, value)) { // 包含别的值
                        // 插入不成功
                        // 相同key - value
                        String preValue = hashMap.get(key)
                        if (preValue == value) {
                            println "[相同K-V] " + key + " - " + value
                            println "[打印K-V] " + key + " - " + preValue
                        } else {// 相同key - 不同value
                            println "[相同K] " + key + " - " + value
                            println "[旧的K] " + key + " - " + preValue
                        }
                    } else {
                        // 加入成功
                    }
                } else {
                    assert null == hashMap.put(key, "")
                    println("$it 不符合要求!! 保留key，放入了空值")
                }
            }
        })
        println ">>>>>>>读取xml结束>>>>>>>>>>>>>>>>>>>>>"
        //println xml
        return hashMap
    }

    // 把本地文件全部读出来
    static LinkedHashMap<String, String> readXmlToHashMap(File xmlFile) {
        assert xmlFile.exists() && xmlFile.size() > 0 //这里不能为空，按理说已经判断了
        println ">>>>>>>>>>>>>>>>>>>>"
        println(xmlFile.absolutePath)
        def xml = new XmlParser().parse(xmlFile) // 读出来
        println xml.children().size()
        println ">>>>>>>>读取xml开始>>>>>>>>>>>>>>>>>>>>"
        def hashMap = new LinkedHashMap() // 现在可以为空
        xml.children().forEach({
            if (it instanceof Node && "string".equals(it.name())) {
                def key = it.attributes()['name']
                def valueList = ((List) it.value())
                if (valueList.size() > 0) {
                    def value = valueList.get(0)
                    assert null == hashMap.put(key, value), "不能有重复的值，直接报错，请删除原先文件中的${key}"
                } else {
                    assert null == hashMap.put(key, "")
                    println("$it 不符合要求!! 保留key，放入了空值")
                }
            }
        })
        println ">>>>>>>读取xml结束>>>>>>>>>>>>>>>>>>>>>"
        //println xml
        return hashMap
    }

    final static String KEY_SAME_SRC = "KEY_SAME_SRC"
    final static String KEY_DIFF_SRC = "KEY_DIFF_SRC"

    // 以src为准， 找出相同的key
    static HashMap<String, String> differenceSRC(Map<String, String> srcMap, Map<String, String> desMap) {
        HashMap<String, String> sameMapSRC = new HashMap<>()
        HashMap<String, String> diffMapSRC = new HashMap<>()

        srcMap.forEach({ k, v ->
            if (desMap.containsKey(k)) {
                sameMapSRC.put(k, v)
            } else {
                diffMapSRC.put(k, v)
            }
        })

        HashMap<String, Map<String, String>> result = new HashMap<>()
        result.put(KEY_SAME_SRC, sameMapSRC)
        result.put(KEY_DIFF_SRC, diffMapSRC)
        return result
    }

    static void printMap(Map<String, String> map) {
        println("-------------")
        map.forEach({ k, v ->
            printlnLog(k, v)
        })
        println("-------------")
    }


    static void writeHashMapToFileWithSort(File file, HashMap<String, String> map) {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)))
        bufferedWriter.writeLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        bufferedWriter.writeLine("<resources>")
        List<String> listKey = map.keySet().toList().sort()
        listKey.forEach({ String key ->
            String line = generatXmlStringitem(key, map.get(key, ""))
            bufferedWriter.writeLine(line)
        })
        bufferedWriter.writeLine("</resources>")
        bufferedWriter.flush()
        bufferedWriter.close()
    }

    // 要做增量更新
    static void writeHashMapToFile(File file, HashMap<String, String> map) {
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)))
        bufferedWriter.writeLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        bufferedWriter.writeLine("<resources>")
        map.entrySet().forEach({ entry ->
            if (check_validate(entry.getKey())) {
                bufferedWriter.writeLine(generatXmlStringitem(entry.getKey(), entry.getValue()))
            } else { //不符合要求
                print("\n 不符合要求 ：$entry ")
            }
        })
        bufferedWriter.writeLine("</resources>")
        bufferedWriter.flush()
        bufferedWriter.close()
    }


    // 要做增量更新
    static void writeNodeDataToFile(File file, List<NodeData> list) {
        println(file.absolutePath)
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false)))
        bufferedWriter.writeLine("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        bufferedWriter.writeLine("<resources>")
        list.forEach({ NodeData data ->
            bufferedWriter.writeLine(generatXmlStringitem(data.key, data.value))
        })
        bufferedWriter.writeLine("</resources>")
        bufferedWriter.flush()
        bufferedWriter.close()
    }

    static String generatXmlStringitem(String key, String value) {
        if (key == null || key.isEmpty()) {
            throw IllegalArgumentException("key 不能为空！！！")
        }
        if (value == null /*|| value.isEmpty()*/) {
            //throw new IllegalArgumentException("value 不能为空！！！")
            value = ""
        }
        String v = replaceValue(value)
        def str = "    <string name=\"${key}\">${v}</string>"
        return str
    }

    static String replaceValue(String value) {
        if (value == null || value.size() == 0) return ""
        valueReplace.forEach({ k, v ->
            value = value.replace(k, v)//value.replaceAll(k, v)
        })
        return value
    }


    // 比较两个
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
        files.each { f ->
            if (f.isFile() && f.getName() == comName) {
                f.delete()
            } else if (f.isDirectory()) {
                deleteCommonFiles(f, comName)
            }
        }
    }

    // strings.xml -> string_[suffix].xml
    static String newFileName(String filename, String suffix) {
        int index = filename.lastIndexOf(".")
        if(index > 0) {
            return filename.substring(0, index) + "_" + suffix + filename.substring(index)
        } else {
            return filename + "_" + suffix;
        }
    }

}