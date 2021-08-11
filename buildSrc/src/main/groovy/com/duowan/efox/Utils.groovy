import java.util.regex.Matcher
import java.util.regex.Pattern

class Utils {

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
}