package com.hgb.tools.element;

import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.SearchListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class JsonFindDialog extends FindDialog {
    private static final Logger logger = LoggerFactory.getLogger(JsonFormatterTab.class);

    public JsonFindDialog(Dialog owner, SearchListener listener) {
        super(owner, listener);
    }

    public JsonFindDialog(Frame owner, SearchListener listener) {
        super(owner, listener);
        JTextField textField = (JTextField) findTextCombo.getEditor().getEditorComponent();
        // 添加 DocumentListener 来监听输入的变化
        textField.addKeyListener(new TextFieldKeyAdapter(textField));
    }


}
