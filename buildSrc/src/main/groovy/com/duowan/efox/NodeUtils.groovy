package com.duowan.efox

import groovy.util.Node
import groovy.util.NodeList
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation

// https://blog.csdn.net/danpie3295/article/details/106779461
// https://blog.csdn.net/LjingDong/article/details/86289243
final class NodeUtils {

    // 筛选string标签
    static void filterNodeChilrenString(Node node) {
        assert node instanceof  Node
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
        assert node instanceof  Node
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
                return DefaultTypeTransformation.compareTo(o1.name(), o2.name())
            }
        })
    }

    // 读取本地的
    static Node readNodeFromLocal(File xmlFile) {
        return new XmlParser().parse(xmlFile)
    }

    // 写入到本地
    static void writeNode2Local(Node node, File xmlFile) {
        xmlFile.parentFile.mkdirs()

        // 将XML写入本地
        FileWriter fileWriter = new FileWriter(xmlFile)
        XmlNodePrinter nodePrinter = new XmlNodePrinter(new PrintWriter(fileWriter))
        nodePrinter.setPreserveWhitespace(true)
        nodePrinter.print(node)
    }
}