package com.hgb.tools.element;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hgb.tools.pojo.NodeData;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.util.Objects;

public class JsonTreeCellRenderer extends DefaultTreeCellRenderer {

    final ImageIcon STRING_ICON = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon/v.gif")));
    final ImageIcon OBJECT_ICON = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon/o.gif")));
    final ImageIcon ARRAY_ICON = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon/a.gif")));
    final ImageIcon NUMBER_ICON = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon/n.gif")));
    final ImageIcon BOOLEAN_ICON = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon/v.gif")));
    final ImageIcon NULL_ICON = new ImageIcon(Objects.requireNonNull(getClass().getResource("/icon/k.gif")));

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        // 使用默认的渲染器
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        // 如果节点是 DefaultMutableTreeNode 类型
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            // 获取节点的数据对象
            Object userObject = node.getUserObject();

            // 如果节点的数据是 NodeData 类型
            if (!(userObject instanceof NodeData)) {
                // 根节点
                setIcon(STRING_ICON);
                return this; // 返回渲染后的组件
            }
            NodeData nodeData = (NodeData) userObject;
            // 获取 key 和 value
            String key = nodeData.getKey();
            Object val = nodeData.getValue();

            // 设置节点显示为 key : value
            if (val == null) {
                setIcon(NULL_ICON);
                setText(key + " : null");
            } else {
                // 如果是对象或数组，则显示为 `key : Object/Array`，否则显示为 `key : value`
                if (val instanceof JSONObject) {
                    setText(key);
                    setIcon(OBJECT_ICON);
                } else if (val instanceof JSONArray) {
                    setText(key);
                    setIcon(ARRAY_ICON);
                } else if (val instanceof String) {
                    setText(key + " : \"" + val + "\"");
                    setIcon(STRING_ICON);
                } else if (val instanceof Number) {
                    setText(key + " : " + val);
                    setIcon(NUMBER_ICON);
                } else if (val instanceof Boolean) {
                    setText(key + " : " + val);
                    setIcon(BOOLEAN_ICON);
                } else {
                    setIcon(STRING_ICON);
                    setText(key + " : " + val);
                }
            }
        }

        return this; // 返回渲染后的组件
    }
}
