package com.hgb.tools.pojo;

// 自定义节点数据类
public class NodeData {
    private final String key;
    private final Object value;
    private final NodeType type;

    public NodeData(String key, Object value, NodeType type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }


    public String getKey() {
        return key;
    }

    public NodeType getType() {
        return type;
    }

    @Override
    public String toString() {
        return key;
    }

    public Object getValue() {
        return value;
    }

}