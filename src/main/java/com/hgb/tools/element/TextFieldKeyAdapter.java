package com.hgb.tools.element;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class TextFieldKeyAdapter extends KeyAdapter {
    private final JTextField textField;

    public TextFieldKeyAdapter(JTextField textField) {
        this.textField = textField;
    }

    /**
     * Invoked when a key has been released.
     *
     * @param e
     */
    @Override
    public void keyReleased(KeyEvent e) {
        super.keyReleased(e);
        int keyCode = e.getKeyCode();
        if (keyCode == KeyEvent.VK_A) {
            // Ctrl + A (全选)
            textField.selectAll();
        } else if (keyCode == KeyEvent.VK_C) {
            // Ctrl + C (复制)
            textField.copy();
        } else if (keyCode == KeyEvent.VK_V) {
            // Ctrl + V (粘贴)
            textField.paste();
        } else if (keyCode == KeyEvent.VK_X) {
            // Ctrl + X (剪切)
            textField.cut();
        }
    }

}
