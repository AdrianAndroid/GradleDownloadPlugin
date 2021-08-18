package com.duowan.efox


import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation
import org.json.JSONObject

// https://blog.csdn.net/danpie3295/article/details/106779461
// https://blog.csdn.net/LjingDong/article/details/86289243
final class NodeUtils {

    // 筛选string标签
    static void filterNodeChilrenString(Node node) {
        assert node instanceof Node
        assert node.children() instanceof NodeList

        List<Node> nodes = new ArrayList<>()
        node.children().forEach({ // 找出所有想要加进来的
            if (it instanceof Node && "string" == it.name()) {
                // 是正确的
            } else {
                nodes.add(it) // 将没用的加进来
            }
        })
        node.children().removeAll(nodes) // 删掉所有没用的
//        node.children().forEach({
//            println(it)
//        })
    }

    // Node中的chiren进行排序
    static void sortNodeChilren(Node node) {
        assert node instanceof Node
        assert node.children() instanceof NodeList
        node.children().sort(new Comparator() {
            @Override
            int compare(Object o1, Object o2) {
//                println("o1 instanceof Node => " + (o1 instanceof Node))
//                println("o2 instanceof Node => " + (o2 instanceof Node))
//                println("o1 ==> " + o1)
//                println("o2 ==> " + o2)
//                println("o1.name ==> " + o1.name)
//                println("o2.name ==> " + o2.name)
                String k1 = o1.attributes()['name']
                String k2 = o2.attributes()['name']
                return DefaultTypeTransformation.compareTo(k1, k2)
            }
        })
    }

    // 读取本地的
    static Node readNodeFromLocal(File xmlFile) {
        if (!xmlFile.exists() || xmlFile.size() < 1) return null
        return new XmlParser().parse(xmlFile)
    }

    // 写入到本地
    static void writeNode2Local(Node node, File xmlFile) {
        xmlFile.parentFile.mkdirs()

        sortNodeChilren(node) // 都排序
        // 将XML写入本地
        FileWriter fileWriter = new FileWriter(xmlFile)
        XmlNodePrinter nodePrinter = new XmlNodePrinter(new PrintWriter(fileWriter))
        nodePrinter.setPreserveWhitespace(true)
        nodePrinter.print(node)
    }

    static String getNodeKey(Node node) {
        def key = node.attributes()['name']
        return key
    }

    static String getNodeValue(Node node) {
        assert node.value() instanceof NodeList
        def valueList = ((List) node.value())
        def value = ""
        if (valueList.size() > 0) {
            value = valueList.get(0)
        }
        return value
    }

    // 给node设置值
    static Node createNode(String key, String value) {
        return new Node(null, "string", ["name": "$key"], "$value")
    }

    static HashMap<String, String> nodeList2HashMap(Node node) {
        assert node.children() instanceof NodeList
        HashMap<String, String> hashMap = new HashMap<>()
        node.children().forEach({ n ->
            def key = getNodeKey(n)
            def val = getNodeValue(n)
            if (null != hashMap.put(key, val)) {
                println(">> key = " + key)
                println(">> val = " + val)
                println()
                println()
            }
        })
        return hashMap
    }

    // node child 转换成 HashMap
    static HashMap<String, Node> nodeChild2HashMap(Node node) {
        assert node.children() instanceof NodeList
        HashMap<String, Node> hashMap = new HashMap<>()
        println()
        node.children().forEach({ n ->
            def key = getNodeKey(n)
            print("key ")
            assert null == hashMap.put(key, n), "有重复的key ${key}" //重复的key
        })
        println()
        return hashMap
    }

    static Node hashMap2Node(HashMap<String, Node> map) {
        // 先要创建一个
        return Node()
    }

    // 找出n1中的不同
    static List<NodeData> findSameKV(Node n1, Node n2) {
        assert n1 != null && n1.children() instanceof NodeList
        assert n2 != null && n2.children() instanceof NodeList

        List<NodeData> result = new ArrayList<>()

        HashMap<String, String> hm2 = nodeList2HashMap(n2)

        n1.children().forEach({ n ->
            // 获取node的key
            String key = getNodeKey(n)
            // 获取node的value
            String val1 = getNodeValue(n)
            String val2 = hm2.get(key, "")
            if (val1 != val2 || val2 == "") {
                // 说明不相同
                result.add(new NodeData(key, val1, val2))
            }
        })

        return result
    }

    // 将x1中的key 补充到 x2中的key
    static void fillIn(File x1, File x2) {
        assert x1 != null && x2 != null && (x1.size() > 0 || x2.size() > 0)
        // 将node1放入HashMap
        Node n1 = readNodeFromLocal(x1)
        HashMap<String, Node> h1 = nodeChild2HashMap(n1)
        // 将node2放入HashMap
        Node n2 = readNodeFromLocal(x2)
        HashMap<String, Node> h2 = nodeChild2HashMap(n2)


        NodeList result = new NodeList()

        // h1
        h1.forEach({ key, node ->
            if (!h2.containsKey(key)) { // h1中没有
                result.add(node)
            }
        })
        n2.children().addAll(result)

        result.clear()
        // h2
        h2.forEach({ key, node ->
            if (!h1.containsKey(key)) {
                result.add(node)
            }
        })
        n1.children().addAll(result)

        println(x1.absolutePath)
        println(n1.children().size())
        println(x2.absolutePath)
        println(n2.children().size())

        // 排序
        sortNodeChilren(n1)
        sortNodeChilren(n2)

        // x1 写入本地
        writeNode2Local(n1, new File(newFileName(x1.absolutePath, "fill")))

        // x2 写入本地
        writeNode2Local(n2, new File(newFileName(x2.absolutePath, "fill")))
    }

    // strings.xml -> string_[suffix].xml
    static String newFileName(String filename, String suffix) {
        int index = filename.lastIndexOf(".")
        if (index > 0) {
            return filename.substring(0, index) + "_" + suffix + filename.substring(index)
        } else {
            return filename + "_" + suffix
        }
    }

    static boolean isSameValue(List<NodeData> list) {
        Boolean isSame = true
        List<String> all = new ArrayList<>()
        list.forEach({
            list.add(it.value)
        })
        return isSame
    }

    static HashMap<String, List<NodeData>> findSameKv2(File x1) {
        Node node = readNodeFromLocal(x1)
        HashMap<String, List<NodeData>> map = new HashMap<>()
        node.children().forEach({ n ->
            def key = getNodeKey(n)
            def val = getNodeValue(n)
            def key_lower = key.toLowerCase()
            List<NodeData> list = map.get(key_lower)
            if (list == null) {
                list = new ArrayList<>()
            }
            list.add(new NodeData(key, val))
            map.put(key_lower, list)
        })

        HashMap<String, List<NodeData>> result = new HashMap<>()
        map.forEach({ key, nodedata ->
            if (nodedata.size() > 1) result.put(key, nodedata) // 将多余一个的保存起来
        })
        return result
    }

    // 比较两个文件不同
    static void difference(File x1, File x2) {
        assert x1 != null && x2 != null && (x1.size() > 0 || x2.size() > 0)
        // 将node1放入HashMap
        Node n1 = readNodeFromLocal(x1)
        HashMap<String, Node> h1 = nodeChild2HashMap(n1)
        // 将node2放入HashMap
        Node n2 = readNodeFromLocal(x2)
        HashMap<String, Node> h2 = nodeChild2HashMap(n2)

        println()
        println("以第一个为准")
        n1.children().forEach({ n ->
            def key = getNodeKey(n)
            if (!h2.containsKey(key)) {
                println("[diff] $key")
            }
        })


        println()
        println("以第二个为准")
        n2.children().forEach({ n ->
            def key = getNodeKey(n)
            if (!h1.containsKey(key)) {
                println("[diff] $key")
            }
        })
    }

    static Node jsonObject2Node(JSONObject jo) {

        Node res = new Node(null, "resources")
        // 创建child
        //<string name="Kakao">Kakao</string>
        Iterator<String> iterator = jo.keys()
        while (iterator.hasNext()) {
            String key = iterator.next()
            String val = jo.opt(key)
            new Node(res, "string", ["name": "$key"], val == null ? "" : val)
        }
        return res
    }

}