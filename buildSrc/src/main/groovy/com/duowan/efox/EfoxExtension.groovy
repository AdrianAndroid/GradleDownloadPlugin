package com.duowan.efox

class EfoxExtension {
    String message = "Hello this is my custom plugin ..."

    String debugTask = false // 是否加载测试的task

    boolean clearBefore = false // 现在value之前是否清空原有数据， 最好不要变
    String default_values = "values"
    boolean check_K_V = false // 检查键值对
    String urlFomat = "http://multi-lang.duowan.com/multiLangBig/Teachee/%s/%s" // 地址模版
    String resName = "commonstring.xml" //通用的名称
    String resPath = "src/main/res"
    String efoxPath = "Teachee___2_3_0" //需要修改
    List<String> efoxPaths = []//["Teachee___2_3_0"]
//     String efoxPaths = [] // 所有的内容 // 通过这个创建不同的工程
    Map<String, String> valuesDir = ["values": "en", "values-ko": "ko"] //需要外部传入
    Map<String, String> valueReplace = ["%@": "%s"]//["&": "&amp;", "%@": "%s", "\'": "\\\'"]
    Map<String, String> afterValueReplace = ["//n": "/n"]//["&": "&amp;", "%@": "%s", "\'": "\\\'"]
    boolean useLog = true
    String patternKey = '^[0-9a-zA-Z_]+\$' // key的表达式


    // 额外的
    List<String> opSrcs = [
            "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res/values/strings.xml"
            , "/Users/flannery/Desktop/yy/TeacheeMaster-android/common/commonres/src/main/res/values-ko/strings-ko.xml"
    ]
}