package com.duowan.efox

class NodeData {
    String key
    String value
    String oldValue

    NodeData(String key, String value) {
        this.key = key
        this.value = value
    }

    NodeData(String key, String value, String oldValue) {
        this.key = key
        this.value = value
        this.oldValue = oldValue
    }
}