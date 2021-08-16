package com.duowan.efox

class NodeData {
    String key
    String value
    String oldValue
    List<String> ovs //所有的values

    NodeData(String key, String value) {
        this.key = key
        this.value = value
    }

    NodeData(String key, String value, String oldValue) {
        this.key = key
        this.value = value
        this.oldValue = oldValue
    }

    // 添加olevalue
    NodeData appendOldValues(String oldValue) {
        if (ovs == null) {
            ovs = new ArrayList<>();
        }
        ovs.add(oldValue)
    }
}