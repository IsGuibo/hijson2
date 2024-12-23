package com.hgb.tools.element;

import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.SearchComboBox;
import org.fife.rsta.ui.search.SearchListener;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class JsonReplaceDialog extends ReplaceDialog {
    public JsonReplaceDialog(Dialog owner, SearchListener listener) {
        super(owner, listener);
    }

    public JsonReplaceDialog(Frame owner, SearchListener listener) {
        super(owner, listener);
        JTextField findTextField = (JTextField) findTextCombo.getEditor().getEditorComponent();
        // 获取私有字段
        Field field = null;
        try {
            field = ReplaceDialog.class.getDeclaredField("replaceWithCombo");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        // 设置可访问
        field.setAccessible(true);

        // 获取字段的值
        try {
            SearchComboBox replaceWithCombo = (SearchComboBox) field.get(this);
            JTextField replaceTextField = (JTextField) replaceWithCombo.getEditor().getEditorComponent();
            replaceTextField.addKeyListener(new TextFieldKeyAdapter(replaceTextField));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // 添加 DocumentListener 来监听输入的变化
        findTextField.addKeyListener(new TextFieldKeyAdapter(findTextField));

    }
}
