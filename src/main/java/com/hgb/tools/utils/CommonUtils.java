package com.hgb.tools.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hgb.tools.pojo.NodeData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommonUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    public static final int REMARK_ALL_UNLIMITED_TEXT_LENGTH_MAXIMUM = 500000;
    public static final int REPLACE_ALL_UNLIMITED_TEXT_LENGTH_MAXIMUM = 100000;
    

    public static String TreePath2JsonPath(TreePath treePath) {
        StringBuilder jsonPath = new StringBuilder();
        // 获取 TreePath 中的每个元素
        Object[] pathElements = treePath.getPath();

        // 遍历路径中的节点
        for (int i = 1; i < pathElements.length; i++) {
            // 跳过根节点，因为根节点一般不需要出现在路径中
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathElements[i];
            if (node == null || !(node.getUserObject() instanceof NodeData)) {
                return "$";
            }
            NodeData nodeData = (NodeData) node.getUserObject();
            String key = nodeData.getKey();
            if (!key.startsWith("[") || !key.endsWith("]")) {
                jsonPath.append(".");
            }
            jsonPath.append(key);
        }

        // 返回最终的 JSON 路径
        return "$" + jsonPath;
    }


    // 静态方法，根据 JSON 路径跳转到 JTree 中的目标节点
    public static void navigateToJsonPath(JTree jsonTree, String jsonPath) {
        if (jsonPath.length() < 3) {
            return;
        }
        // 解析 JSON 路径
        List<String> pathParts = parseJsonPath(jsonPath);
        TreeNode root = (TreeNode) jsonTree.getModel().getRoot();

        // 根据路径逐步进入树结构
        TreeNode targetNode = findNodeByPath(root, pathParts, 0);

        if (targetNode != null) {
            // 构造 TreePath
            TreePath path = new TreePath(((DefaultTreeModel) jsonTree.getModel()).getPathToRoot(targetNode));
            // 滚动到目标节点
            jsonTree.scrollPathToVisible(path);
            // 选中目标节点
            jsonTree.setSelectionPath(path);
        }
    }

    private static List<String> parseJsonPath(String jsonPath) {
        List<String> pathParts = new ArrayList<>();
        String[] parts = jsonPath.substring(2).split("\\.");
        for (String part : parts) {
            if (part.contains("[")) {
                String[] arrayParts = part.split("\\[");
                pathParts.add(arrayParts[0]);
                pathParts.add(arrayParts[1].replace("]", ""));
            } else {
                pathParts.add(part);
            }
        }
        return pathParts;
    }

    // 根据路径在树中找到目标节点
    private static TreeNode findNodeByPath(TreeNode root, List<String> pathParts, int index) {
        if (index >= pathParts.size()) {
            return root;
        }

        String currentPart = pathParts.get(index);
        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode child = root.getChildAt(i);
            // 如果当前部分是数字，表示它是数组的索引
            if (currentPart.matches("\\d+")) {
                if (i == Integer.parseInt(currentPart)) {
                    return findNodeByPath(child, pathParts, index + 1);
                }
            } else {
                if (child.toString().equals(currentPart)) {
                    return findNodeByPath(child, pathParts, index + 1);
                }
            }
        }
        return null;
    }


    public static String findJsonPath(String jsonStr, int selectionStart, int selectionEnd) {
        // 修改selectionStart到selectionEnd 的内容成"f21083d6-c6d3-44fc-893e-f1fafd88f490"
        String originalKey = jsonStr.substring(selectionStart, selectionEnd);
        String uuid = "f21083d6-c6d3-44fc-893e-f1fafd88f490";
        String replacedStr = jsonStr.substring(0, selectionStart) + uuid + jsonStr.substring(selectionEnd);
        // 对replacedStr进行解析
        Object jsonObj = JSON.parse(replacedStr);
        ArrayList<String> paths = new ArrayList<>();
        findPaths(jsonObj, "", uuid, paths);
        if (!paths.isEmpty()) {
            return "$" + paths.get(0).replace(uuid, originalKey);
        } else {
            return "$";
        }

    }

    // 递归查找属性为 "aaa" 的路径
    public static void findPaths(Object obj, String currentPath, String targetKey, List<String> paths) {
        if (obj instanceof JSONObject) {
            // 如果是 JSONObject，则遍历每个字段
            JSONObject jsonObject = (JSONObject) obj;
            for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String newPath = currentPath + "." + key;

                if (key.equals(targetKey)) {
                    paths.add(newPath);  // 找到目标属性，添加路径
                }

                // 递归处理子对象
                findPaths(value, newPath, targetKey, paths);
            }
        } else if (obj instanceof JSONArray) {
            // 如果是 JSONArray，则遍历每个元素
            JSONArray jsonArray = (JSONArray) obj;
            for (int i = 0; i < jsonArray.size(); i++) {
                findPaths(jsonArray.get(i), currentPath + "[" + i + "]", targetKey, paths);
            }
        }
    }


    public static String minifyJsonStr(String jsonStr) {
        // 直接用 replace 替换换行符和制表符
        return jsonStr.replace("\n", "").replace("\t", "");
    }


    // 获取剪贴板中的文本内容
    public static String getClipboardText() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(null);

        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                return (String) transferable.getTransferData(DataFlavor.stringFlavor);
            } catch (UnsupportedFlavorException | IOException e) {
                logger.error("获取剪贴板文本失败", e);
            }
        }
        return null;
    }


    public static String unescape(String text) {
        text = text.replaceAll("\\\\\"", "\"");
        if (text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length() - 1);
        }
        return text;

    }

    public static String escape(String text) {

        text = text.replaceAll("\"", "\\\\\"");
        return "\"" + text + "\"";

    }





    
}
