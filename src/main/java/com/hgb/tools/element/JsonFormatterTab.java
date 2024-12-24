package com.hgb.tools.element;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.hgb.tools.pojo.NodeData;
import com.hgb.tools.pojo.NodeType;
import com.hgb.tools.utils.CommonUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

// 单独的 Tab 页面类
public class JsonFormatterTab extends JPanel {
    private static final Logger logger = LoggerFactory.getLogger(JsonFormatterTab.class);


    private RSyntaxTextArea jsonInputTextArea; // 用于 JSON 高亮的输入框
    private JTree jsonTree;               // JSON 树
    private JTable keyValueTable;         // 键值对表格
    private DefaultTableModel tableModel; // 表格模型
    private JPopupMenu popupMenu;         // 右键菜单
    private Object JsonObj;
    private boolean autoNavigate = false;
    private final RTextScrollPane inputScrollPane;


    public JsonFormatterTab() {
        setLayout(new BorderLayout());

        initInputTextArea();
        inputScrollPane = new RTextScrollPane(jsonInputTextArea);
        initJTree();
        JScrollPane treeScrollPane = new JScrollPane(jsonTree);
        initPopupMenu();
        initKeyValueTable();

        // 布局
        JScrollPane tableScrollPane = new JScrollPane(keyValueTable);
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScrollPane, treeScrollPane);
        mainSplitPane.setDividerLocation(400);
        JSplitPane rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mainSplitPane, tableScrollPane);
        rightSplitPane.setDividerLocation(900);
        add(rightSplitPane, BorderLayout.CENTER);
    }

    private void initKeyValueTable() {
        // 键值对表格
        String[] columnNames = {"Key", "Value"};
        tableModel = new DefaultTableModel(columnNames, 0);
        keyValueTable = new JTable(tableModel);

    }

    private void initPopupMenu() {
        // 添加右键菜单
        popupMenu = new JPopupMenu();
        JMenuItem copyKeyItem = new JMenuItem("复制键名");
        JMenuItem copyValueItem = new JMenuItem("复制键值");
        // 添加事件监听器，避免重复添加
        copyKeyItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jsonTree.getLastSelectedPathComponent();
                if (selectedNode != null && selectedNode.getUserObject() instanceof NodeData) {
                    NodeData nodeData = (NodeData) selectedNode.getUserObject();
                    String key = nodeData.getKey();
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(key), null);
                }
            }
        });

        copyValueItem.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JsonObj == null) {
                    return;
                }
                TreePath selectionPath = jsonTree.getSelectionPath();
                if (selectionPath == null) {
                    return;
                }
                String jsonPath = CommonUtils.TreePath2JsonPath(selectionPath);
                Object value = JSONPath.eval(JsonObj, jsonPath);
                if (value != null) {
                    String valueStr = JSONObject.toJSONString(value, true);
                    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(valueStr), null);
                }
            }
        });
        popupMenu.add(copyKeyItem);
        popupMenu.add(copyValueItem);

    }

    private void initJTree() {

        // JSON 树
        jsonTree = new JTree();
        jsonTree.setCellRenderer(new JsonTreeCellRenderer()); // 设置自定义的渲染器

        jsonTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jsonTree.addTreeSelectionListener(e -> displayKeyValue());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("JSON");
        jsonTree.setModel(new DefaultTreeModel(root));

        jsonTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                long start = System.currentTimeMillis();
                if (autoNavigate) {
                    autoNavigate = false;
                    return;
                }
                TreePath newSelectionPath = e.getNewLeadSelectionPath();
                String jsonPath = CommonUtils.TreePath2JsonPath(newSelectionPath);
                Object realObj = JSONPath.eval(JsonObj, jsonPath);
                JSONPath.set(JsonObj, jsonPath, "238f22d8-e340-43cc-acae-6e119142d868");
                String jsonStrTemp = JSON.toJSONString(JsonObj, SerializerFeature.PrettyFormat);
                int index = jsonStrTemp.indexOf("238f22d8-e340-43cc-acae-6e119142d868");
                int indexEnd = jsonInputTextArea.getText().indexOf("\n", index);
                jsonInputTextArea.setSelectionStart(index - 1);
                jsonInputTextArea.setSelectionEnd(indexEnd);
                logger.debug("从树导航到JSON路径耗时：{} ms", System.currentTimeMillis() - start);
                JSONPath.set(JsonObj, jsonPath, realObj);
            }
        });

        jsonTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = jsonTree.getPathForLocation(e.getX(), e.getY());

                    if (path != null) {
                        jsonTree.setSelectionPath(path);
                        showPopupMenu(e);
                    }
                }
            }

            private void showPopupMenu(MouseEvent e) {
                // 获取右键点击的位置，并弹出菜单
                if (jsonTree.getSelectionPath() != null) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void initInputTextArea() {
        // JSON 输入区域
        jsonInputTextArea = new RSyntaxTextArea();
        jsonInputTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
        jsonInputTextArea.setCodeFoldingEnabled(true);
        jsonInputTextArea.setShowMatchedBracketPopup(false);
        jsonInputTextArea.setBracketMatchingEnabled(false);

        jsonInputTextArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                long start = System.currentTimeMillis();
                super.mouseReleased(e);
                String selectedText = jsonInputTextArea.getSelectedText();
                if (selectedText == null || selectedText.length() >= 25 || selectedText.length() <= 2) {
                    return;
                }
                int selectionStart = jsonInputTextArea.getSelectionStart();
                int selectionEnd = jsonInputTextArea.getSelectionEnd();
                String jsonText = jsonInputTextArea.getText();
                if (selectionEnd > jsonText.length()) {
                    return;
                }

                char beforeChar = jsonText.charAt(selectionStart - 1);
                char afterChar = jsonText.charAt(selectionEnd);
                char afterChar2 = jsonText.charAt(selectionEnd + 1);
                if (beforeChar != '"' || afterChar != '"' || afterChar2 != ':') {
                    return;
                }
                if (((DefaultMutableTreeNode) jsonTree.getModel().getRoot()).isLeaf()) {
                    return;
                }
                String jsonPath = CommonUtils.findJsonPath(jsonText, selectionStart, selectionEnd);
                autoNavigate = true;
                CommonUtils.navigateToJsonPath(jsonTree, jsonPath);
                logger.debug("从JSON导航到树路径耗时：{} ms", System.currentTimeMillis() - start);
            }
        });


        // 获取 InputMap 和 ActionMap
        jsonInputTextArea.getInputMap().put(KeyStroke.getKeyStroke("ctrl V"), "paste");
        jsonInputTextArea.getActionMap().put("paste", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long start = System.currentTimeMillis();
                if (jsonInputTextArea.isBracketMatchingEnabled()) {
                    jsonInputTextArea.setBracketMatchingEnabled(false);
                }
                jsonInputTextArea.replaceSelection(CommonUtils.getClipboardText());
                logger.debug("粘贴文本耗时：{} ms", System.currentTimeMillis() - start);
            }
        });
    }


    // 格式化 JSON
    public void formatJson() {
        String input = jsonInputTextArea.getText();
        try {
            // 格式化 JSON
            JsonObj = JSON.parse(input);
            String formattedJson = JSON.toJSONString(JsonObj, true);
            jsonInputTextArea.setText(formattedJson);
            jsonInputTextArea.setBracketMatchingEnabled(true);

            // 构建树形结构
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("JSON");
            buildTree(root, JsonObj);
            jsonTree.setModel(new DefaultTreeModel(root));

            // 清空表格
            tableModel.setRowCount(0);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid JSON format!" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void buildTree(DefaultMutableTreeNode parent, Object json) {
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;

            // 使用 TreeMap 按键字母顺序排序
            TreeMap<String, Object> sortedMap = new TreeMap<>(jsonObject);
            for (Map.Entry<String, Object> entry : sortedMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                NodeType type = NodeType.VALUE;
                if (value instanceof JSONObject) {
                    type = NodeType.OBJECT;
                } else if (value instanceof JSONArray) {
                    type = NodeType.ARRAY;
                }
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new NodeData(key, value, type));
                parent.add(child);
                buildTree(child, value);
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i = 0; i < jsonArray.size(); i++) {
                Object value = jsonArray.get(i);
                String key = "[" + i + "]";
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new NodeData(key, value, NodeType.ARRAY));
                parent.add(child);
                buildTree(child, value);
            }
        } else {
            parent.setUserObject(new NodeData(parent.getUserObject().toString(), json, NodeType.VALUE));
        }
    }


    private void displayKeyValue() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) jsonTree.getLastSelectedPathComponent();
        if (selectedNode == null) {
            return;
        }

        // 取消当前表格的编辑状态（如果正在编辑）
        if (keyValueTable.isEditing()) {
            keyValueTable.getCellEditor().stopCellEditing();
        }
        tableModel.setRowCount(0); // 清空表格

        // 获取节点的用户对象（NodeData）
        if (selectedNode.getUserObject() instanceof NodeData) {
            NodeData nodeData = (NodeData) selectedNode.getUserObject();

            // 如果当前节点有子节点，遍历子节点并展示
            if (selectedNode.getChildCount() > 0) {
                for (int i = 0; i < selectedNode.getChildCount(); i++) {
                    DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) selectedNode.getChildAt(i);
                    if (childNode.getUserObject() instanceof NodeData) {
                        NodeData childData = (NodeData) childNode.getUserObject();
                        displayNodeInTable(childData);
                    }
                }
            } else {
                // 当前节点的展示
                displayNodeInTable(nodeData);
            }
        }
    }

    // 新增一个方法来处理节点的展示
    private void displayNodeInTable(NodeData nodeData) {
        String key = nodeData.getKey();
        Object value = nodeData.getValue();
        // 根据节点类型展示对应的值
        if (nodeData.getType() == NodeType.OBJECT) {
            value = "Object"; // 显示 Object
        } else if (nodeData.getType() == NodeType.ARRAY) {
            value = "Array"; // 对于数组节点，显示 "Array"
        }
        // 将节点添加到表格中
        tableModel.addRow(new Object[]{key, value});
    }

    public RSyntaxTextArea getJsonInputTextArea() {
        return jsonInputTextArea;
    }


}