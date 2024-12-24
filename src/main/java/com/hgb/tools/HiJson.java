package com.hgb.tools;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.hgb.tools.config.ApplicationConfiguration;
import com.hgb.tools.element.JsonFindDialog;
import com.hgb.tools.element.JsonFormatterTab;
import com.hgb.tools.element.JsonReplaceDialog;
import com.hgb.tools.element.StatusBar;
import com.hgb.tools.utils.CommonUtils;
import com.jidesoft.swing.JideTabbedPane;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static com.hgb.tools.config.ApplicationConfiguration.prefs;

public class HiJson extends JFrame implements SearchListener {
    private static final Logger logger = LoggerFactory.getLogger(HiJson.class);

    private JideTabbedPane tabbedPane; // Tab 容器
    private JsonFindDialog findDialog;
    private JsonReplaceDialog replaceDialog;
    private FindToolBar findToolBar;
    private ReplaceToolBar replaceToolBar;
    private StatusBar statusBar;
    private JFileChooser fileDialog;
    private JToolBar toolBar;


    public HiJson() {
        ApplicationConfiguration.loadSettings();

        setTitle("HiJson");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(ApplicationConfiguration.WINDOW_WIDTH, ApplicationConfiguration.WINDOW_HEIGHT);
        initLookAndFeel();
        initToolBar();
        initStatusBar();
        initTabPane();
        initSearchDialogs();
        initMenuBar();
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                saveSettings();
            }
        });
        setVisible(true);
    }

    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(prefs.get(ApplicationConfiguration.PREF_LOOK_AND_FEEL, UIManager.getSystemLookAndFeelClassName()));
        } catch (Exception e) {
            logger.error("SetLookAndFeel Error occurred", e);
        }
    }

    private void initStatusBar() {
        statusBar = new StatusBar();
        getContentPane().add(statusBar, BorderLayout.SOUTH);
    }

    private void initTabPane() {
        // 创建 Tab 容器
        tabbedPane = new JideTabbedPane();
        tabbedPane.setShowCloseButtonOnTab(true);
        tabbedPane.setShowCloseButtonOnMouseOver(true);
       

        getContentPane().add(tabbedPane, BorderLayout.CENTER);
        // 创建一个新的 Tab 页面，初始化它
        addNewTab();
    }

    private void initToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false); // 禁止工具栏漂浮
        toolBar.setBorder(BorderFactory.createEmptyBorder());

        // 创建按钮
        JButton formatButton = new JButton("格式化(F)");
        formatButton.addActionListener(e -> formatJsonInActiveTab());

        JButton newTabButton = new JButton("新标签(N)");
        newTabButton.addActionListener(e -> addNewTab());
        JButton minifyJsonButton = new JButton("压缩(M)");
        minifyJsonButton.addActionListener(e -> minifyJsonInActiveTab());
        JButton escapeButton = new JButton("转义(E)");
        escapeButton.addActionListener(e -> escapeJsonInActiveTab());
        JButton reEscapeButton = new JButton("反转义(R)");
        reEscapeButton.addActionListener(e -> escapeJsonInActiveTab());
        


        // 将按钮添加到工具栏
        toolBar.add(newTabButton);
        toolBar.add(escapeButton);
        toolBar.add(reEscapeButton);
        toolBar.add(formatButton);
        toolBar.add(minifyJsonButton);
        add(toolBar, BorderLayout.NORTH);
    }


    private void initMenuBar() {

        JMenuBar mb = new JMenuBar();
        JMenu menu = new JMenu("文件");
        menu.add(new JMenuItem(new ShowFileDialogAction()));
        mb.add(menu);
        menu = new JMenu("编辑");
        menu.add(new JMenuItem(new ShowFindDialogAction()));
        menu.add(new JMenuItem(new ShowReplaceDialogAction()));
        menu.add(new JMenuItem(new GoToLineAction()));
        mb.add(menu);
        menu = new JMenu("主题");
        ButtonGroup bg = new ButtonGroup();
        UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();

        for (UIManager.LookAndFeelInfo info : infos) {
            addItem(new LookAndFeelAction(info), bg, menu);
        }
        addItem(new LookAndFeelAction(new UIManager.LookAndFeelInfo("FlatIntelliJLaf", FlatIntelliJLaf.class.getName())), bg, menu);
        addItem(new LookAndFeelAction(new UIManager.LookAndFeelInfo("FlatMacLightLaf", FlatMacLightLaf.class.getName())), bg, menu);
        mb.add(menu);
        setJMenuBar(mb);
    }


    /**
     * Creates our Find and Replace dialogs.
     */
    private void initSearchDialogs() {

        findDialog = new JsonFindDialog(this, this);
        replaceDialog = new JsonReplaceDialog(this, this);

        fileDialog = new JFileChooser(ApplicationConfiguration.LAST_SAVE_PATH);
        String fileName = "hijson.json";
        File saveFile = new File(fileName);
        fileDialog.setSelectedFile(saveFile);
        SearchContext context = findDialog.getSearchContext();
        context.setMarkAll(false);
        replaceDialog.setSearchContext(context);
        findToolBar = new FindToolBar(this);
        findToolBar.setSearchContext(context);
        replaceToolBar = new ReplaceToolBar(this);
        replaceToolBar.setSearchContext(context);

    }


    private void addNewTab() {
        // 创建一个新的 JsonFormatterTab，表示每个标签页
        JsonFormatterTab tab = new JsonFormatterTab();
        // 创建标签页的标题
        String tabTitle = "Tab " + (tabbedPane.getTabCount() + 1);
        // 将新标签页添加到 Tab 容器中
        tabbedPane.addTab(tabTitle, tab);  // 先添加 Tab 页
        // 使新 Tab 页面成为当前选中的页面
        tabbedPane.setSelectedComponent(tab);
    }

    private void escapeJsonInActiveTab() {
        long start = System.currentTimeMillis();
        if (tabbedPane.getTabCount() == 0) {
            return;
        }
        RSyntaxTextArea jsonInputTextArea = getActiveTab().getJsonInputTextArea();
        String jsonStr = jsonInputTextArea.getText();
        if (jsonInputTextArea.isBracketMatchingEnabled()) {
            jsonInputTextArea.setBracketMatchingEnabled(false);
        }
        if (jsonStr.contains("\\")) {
            jsonInputTextArea.setText(CommonUtils.unescape(jsonStr));
        } else {
            jsonInputTextArea.setText(CommonUtils.escape(jsonStr));
        }
        logger.debug("Escape Json cost time: {} ms", System.currentTimeMillis() - start);
    }

    private void minifyJsonInActiveTab() {
        long start = System.currentTimeMillis();
        if (tabbedPane.getTabCount() == 0) {
            return;
        }
        RSyntaxTextArea jsonInputTextArea = getActiveTab().getJsonInputTextArea();
        String jsonStr = jsonInputTextArea.getText();
        String miniJsonStr = CommonUtils.minifyJsonStr(jsonStr);
        if (jsonInputTextArea.isBracketMatchingEnabled()) {
            jsonInputTextArea.setBracketMatchingEnabled(false);
        }
        jsonInputTextArea.setText(miniJsonStr);
        logger.debug("Minify Json cost time: {} ms", System.currentTimeMillis() - start);
    }


    // 格式化当前选中 Tab 页中的 JSON
    private void formatJsonInActiveTab() {
        if (getActiveTab() == null) {
            return;
        }
        getActiveTab().formatJson();  // 调用当前 Tab 页的 JSON 格式化方法
    }


    @Override
    public String getSelectedText() {
        if (tabbedPane.getTabCount() == 0) {
            return null;
        }
        RSyntaxTextArea currentTextArea = getActiveTab().getJsonInputTextArea();
        return currentTextArea.getSelectedText();
    }

    /**
     * Listens for events from our search dialogs and actually does the dirty
     * work.
     */
    @Override
    public void searchEvent(SearchEvent e) {
        if (tabbedPane.getTabCount() == 0) {
            return;
        }

        SearchEvent.Type type = e.getType();
        SearchContext context = e.getSearchContext();
        SearchResult result;
        RSyntaxTextArea currentTextArea = getActiveTab().getJsonInputTextArea();

        if (type == SearchEvent.Type.FIND && context.getMarkAll()
                && currentTextArea.getText().length() > CommonUtils.REMARK_ALL_UNLIMITED_TEXT_LENGTH_MAXIMUM
                && !currentTextArea.isBracketMatchingEnabled()) {
            // 查找全部，text大于最大限制长度，没有格式化
            statusBar.setLabel("内容太长，请先格式化！", Color.RED);
            return;
        }

        if (type == SearchEvent.Type.REPLACE_ALL
                && currentTextArea.getText().length() > CommonUtils.REPLACE_ALL_UNLIMITED_TEXT_LENGTH_MAXIMUM) {
            // 全部标记，text大于最大限制长度，没有格式化
            statusBar.setLabel("内容太长，不支持全部替换！", Color.RED);
            return;
        }

        switch (type) {
            default: // Prevent FindBugs warning later
            case MARK_ALL:
                result = SearchEngine.markAll(currentTextArea, context);
                break;
            case FIND:
                result = SearchEngine.find(currentTextArea, context);
                if (!result.wasFound() || result.isWrapped()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(currentTextArea);
                }
                break;
            case REPLACE:
                result = SearchEngine.replace(currentTextArea, context);
                if (!result.wasFound() || result.isWrapped()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(currentTextArea);
                }
                break;
            case REPLACE_ALL:
                result = SearchEngine.replaceAll(currentTextArea, context);
                JOptionPane.showMessageDialog(null, result.getCount() + " 处已替换");
                break;
        }

        String text;
        if (result.wasFound()) {
            text = "已找到";
            if (result.getMarkedCount() > 0) {
                text += "：" + result.getMarkedCount();
            }
            statusBar.setLabel(text, Color.BLACK);
        } else if (type == SearchEvent.Type.MARK_ALL) {
            if (result.getMarkedCount() > 0) {
                text = "已找到: " + result.getMarkedCount();
            } else {
                text = "";
            }
            statusBar.setLabel(text, Color.BLACK);
        } else {
            text = "未找到";
            statusBar.setLabel(text, Color.RED);
        }

    }

    private JsonFormatterTab getActiveTab() {
        Component selectedTab = tabbedPane.getSelectedComponent();
        return (JsonFormatterTab) selectedTab;
    }


    /**
     * Opens the "Go to Line" dialog.
     */
    private class GoToLineAction extends AbstractAction {

        GoToLineAction() {
            super("跳转到...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getTabCount() == 0) {
                return;
            }
            if (findDialog.isVisible()) {
                findDialog.setVisible(false);
            }
            if (replaceDialog.isVisible()) {
                replaceDialog.setVisible(false);
            }
            GoToDialog dialog = new GoToDialog(HiJson.this);
            RSyntaxTextArea currentTextArea = getActiveTab().getJsonInputTextArea();
            dialog.setMaxLineNumberAllowed(currentTextArea.getLineCount());
            dialog.setVisible(true);
            int line = dialog.getLineNumber();
            if (line > 0) {
                try {
                    currentTextArea.setCaretPosition(currentTextArea.getLineStartOffset(line - 1));
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().provideErrorFeedback(currentTextArea);
                    logger.debug("Go to line error", ble);
                }
            }
        }

    }


    /**
     * Changes the Look and Feel.
     */
    private class LookAndFeelAction extends AbstractAction {

        private final UIManager.LookAndFeelInfo info;

        LookAndFeelAction(UIManager.LookAndFeelInfo info) {
            putValue(NAME, info.getName());
            this.info = info;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                UIManager.setLookAndFeel(info.getClassName());
                SwingUtilities.updateComponentTreeUI(HiJson.this);
                if (findDialog != null) {
                    findDialog.updateUI();
                    replaceDialog.updateUI();
                }
                pack();
            } catch (Exception ex) {
                logger.error("SetLookAndFeel Error occurred", ex);
            }
        }
    }


    /**
     * Shows the Find dialog.
     */
    private class ShowFindDialogAction extends AbstractAction {

        ShowFindDialogAction() {
            super("查找...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (replaceDialog.isVisible()) {
                replaceDialog.setVisible(false);
            }
            findDialog.setVisible(true);

        }

    }

    private class ShowFileDialogAction extends AbstractAction {

        ShowFileDialogAction() {
            super("保存...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getTabCount() == 0) {
                return;
            }

            // 打开文件选择框（线程将被阻塞, 直到选择框被关闭）
            int result = fileDialog.showSaveDialog(getActiveTab());

            if (result == JFileChooser.APPROVE_OPTION) {
                // 如果点击了"保存", 则获取选择的保存路径
                File file = fileDialog.getSelectedFile();
                try {
                    Files.write(file.toPath(), getActiveTab().getJsonInputTextArea().getText().getBytes());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

        }

    }


    /**
     * Shows the Replace dialog.
     */
    private class ShowReplaceDialogAction extends AbstractAction {

        ShowReplaceDialogAction() {
            super("替换...");
            int c = getToolkit().getMenuShortcutKeyMask();
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, c));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (findDialog.isVisible()) {
                findDialog.setVisible(false);
            }
            replaceDialog.setVisible(true);
        }

    }

    private void addItem(Action a, ButtonGroup bg, JMenu menu) {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(a);
        bg.add(item);
        menu.add(item);
    }

    private void saveSettings() {

        prefs.putInt(ApplicationConfiguration.PREF_WINDOW_WIDTH, getWidth());
        prefs.putInt(ApplicationConfiguration.PREF_WINDOW_HEIGHT, getHeight());
        prefs.put(ApplicationConfiguration.PREF_LOOK_AND_FEEL, UIManager.getLookAndFeel().getClass().getName());
        prefs.put(ApplicationConfiguration.PREF_LAST_SAVE_PATH, fileDialog.getCurrentDirectory().getAbsolutePath());
    }


    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(HiJson::new);
    }


}