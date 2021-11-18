package com.duowan.efox

class EfoxExtension {
    boolean debugTask = false // 是否加载测试的task
    String urlFomat = "http://.......%s" // 地址模版
    String resName = "strings.xml" //通用的名称
    String resPath = "src/main/res"
    List<String> efoxPaths = []//["Teachee___2_3_0"]
    Map<String, String> valuesDir = ["en": "values", "ko": "values-ko"] //需要外部传入
    Map<String, String> valueReplace = ["%@": "%s"]//["&": "&amp;", "%@": "%s", "\'": "\\\'"]
    Map<String, String> afterValueReplace = ["//n": "/n"]//["&": "&amp;", "%@": "%s", "\'": "\\\'"]
    boolean useLog = true
    String patternKey = '^[0-9a-zA-Z_]+\$' // key的表达式
}