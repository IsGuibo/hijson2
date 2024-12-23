package com.hgb.tools;

import com.hgb.tools.element.JCloseableTabbedPane;
import com.hgb.tools.element.JsonFindDialog;
import com.hgb.tools.element.JsonFormatterTab;
import com.hgb.tools.element.JsonReplaceDialog;
import com.hgb.tools.element.StatusBar;
import com.hgb.tools.utils.CommonUtils;
import org.fife.rsta.ui.CollapsibleSectionPanel;
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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.prefs.Preferences;

public class HiJson extends JFrame implements SearchListener {
    private static final Logger logger = LoggerFactory.getLogger(HiJson.class);

    private final Preferences prefs = Preferences.userRoot().node("com/hgb/tools/hijson");
    private static final String PREF_WINDOW_WIDTH = "windowWidth";
    private static final String PREF_WINDOW_HEIGHT = "windowHeight";


    private final JCloseableTabbedPane tabbedPane; // Tab 容器
    private JsonFindDialog findDialog;
    private JsonReplaceDialog replaceDialog;
    private FindToolBar findToolBar;
    private ReplaceToolBar replaceToolBar;
    private final StatusBar statusBar;
    private final CollapsibleSectionPanel csp;
    private JFileChooser fileDialog;
    private String lastSavePath;
    private JToolBar toolBar;


    public HiJson() {
        loadSettings();
        setTitle("HiJson");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        // 创建工具栏
        initToolBar();

        // 将工具栏添加到窗口的顶部
        csp = new CollapsibleSectionPanel();
        getContentPane().add(csp);

        statusBar = new StatusBar();
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // 创建 Tab 容器
        tabbedPane = new JCloseableTabbedPane();
        //添加关闭按钮事件  
        tabbedPane.addCloseListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (!e.getActionCommand().equals(JCloseableTabbedPane.ON_TAB_CLOSE)) {
                    return;
                }
                // 弹出确认关闭
                int result = JOptionPane.showConfirmDialog(
                        null, // 父组件（null表示没有父窗口）
                        "Confirm to close this Tab page？", // 对话框的提示信息
                        "Confirm", // 对话框的标题
                        JOptionPane.YES_NO_OPTION, // 可选按钮
                        JOptionPane.WARNING_MESSAGE); // 消息类型：问题类型
                if (result == JOptionPane.YES_OPTION) {
                    tabbedPane.removeTabAt(tabbedPane.getSelectedIndex());
                }
            }
        });
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // 创建一个新的 Tab 页面，初始化它
        addNewTab();
        initSearchDialogs();
        setJMenuBar(createMenuBar());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent winEvt) {
                saveSettings();
            }
        });
        setVisible(true);
    }

    private void initToolBar() {
        toolBar = new JToolBar();
        toolBar.setFloatable(false); // 禁止工具栏漂浮
        toolBar.setBorder(BorderFactory.createEmptyBorder());

        // 创建按钮
        JButton formatButton = new JButton("Format");
        formatButton.addActionListener(e -> formatJsonInActiveTab());

        JButton newTabButton = new JButton("New Tab");
        newTabButton.addActionListener(e -> addNewTab());
        JButton minifyJsonButton = new JButton("Minify Json ");
        minifyJsonButton.addActionListener(e -> minifyJsonInActiveTab());


        // 将按钮添加到工具栏
        toolBar.add(formatButton);
        toolBar.add(newTabButton);
        toolBar.add(minifyJsonButton);
        add(toolBar, BorderLayout.NORTH);
    }


    private JMenuBar createMenuBar() {

        JMenuBar mb = new JMenuBar();

        JMenu menu = new JMenu("File");
        menu.add(new JMenuItem(new ShowFileDialogAction()));
        menu.addSeparator();
        menu.add(new JMenuItem(new ShowFindDialogAction()));
        menu.add(new JMenuItem(new ShowReplaceDialogAction()));
        menu.add(new JMenuItem(new GoToLineAction()));


        mb.add(menu);

        menu = new JMenu("LookAndFeel");
        ButtonGroup bg = new ButtonGroup();
        UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo info : infos) {
            addItem(new LookAndFeelAction(info), bg, menu);
        }
        mb.add(menu);

        return mb;

    }


    /**
     * Creates our Find and Replace dialogs.
     */
    private void initSearchDialogs() {

        findDialog = new JsonFindDialog(this, this);

        replaceDialog = new JsonReplaceDialog(this, this);

        fileDialog = new JFileChooser(lastSavePath == null ? System.getProperty("user.home") + "/Downloads" : lastSavePath);
        String fileName = "hijson.json";
        File saveFile = new File(fileName);
        fileDialog.setSelectedFile(saveFile);


        // This ties the properties of the two dialogs together (match case,
        // regex, etc.).
        SearchContext context = findDialog.getSearchContext();
        context.setMarkAll(false);
        replaceDialog.setSearchContext(context);

        // Create toolbars and tie their search contexts together also.
        findToolBar = new FindToolBar(this);
        findToolBar.setSearchContext(context);
        replaceToolBar = new ReplaceToolBar(this);
        replaceToolBar.setSearchContext(context);

    }


    private void addNewTab() {
        // 创建一个新的 JsonFormatterTab，表示每个标签页
        JsonFormatterTab tab = new JsonFormatterTab();

        // 创建标签页的标题
        String tabTitle = "  Tab  " + (tabbedPane.getTabCount() + 1) + "  ";

        // 将新标签页添加到 Tab 容器中
        tabbedPane.addTab(tabTitle, tab);  // 先添加 Tab 页


        // 使新 Tab 页面成为当前选中的页面
        tabbedPane.setSelectedComponent(tab);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HiJson::new);
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
            statusBar.setLabel("The text is too long, please format it first.", Color.RED);
            return;
        }

        if (type == SearchEvent.Type.REPLACE_ALL
                && currentTextArea.getText().length() > CommonUtils.REPLACE_ALL_UNLIMITED_TEXT_LENGTH_MAXIMUM) {
            // 全部标记，text大于最大限制长度，没有格式化
            statusBar.setLabel("The text is too long to replace all.", Color.RED);
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
                JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
                break;
        }

        String text;
        if (result.wasFound()) {
            text = "Text found";
            if (result.getMarkedCount() > 0) {
                text += "; occurrences marked: " + result.getMarkedCount();
            }
            statusBar.setLabel(text, Color.BLACK);
        } else if (type == SearchEvent.Type.MARK_ALL) {
            if (result.getMarkedCount() > 0) {
                text = "Occurrences marked: " + result.getMarkedCount();
            } else {
                text = "";
            }
            statusBar.setLabel(text, Color.BLACK);
        } else {
            text = "Text not found";
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
            super("Go To Line...");
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
                    ble.printStackTrace();
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
            } catch (RuntimeException re) {
                throw re; // FindBugs
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


    /**
     * Shows the Find dialog.
     */
    private class ShowFindDialogAction extends AbstractAction {

        ShowFindDialogAction() {
            super("Find...");
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
            super("Save...");
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
            super("Replace...");
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


    private void loadSettings() {
        // 加载窗口大小
        int windowWidth = prefs.getInt(PREF_WINDOW_WIDTH, 1000); // 默认值为 1000
        int windowHeight = prefs.getInt(PREF_WINDOW_HEIGHT, 600); // 默认值为 600
        setSize(windowWidth, windowHeight);
        // 加载设置
        // 读取文件对话框上次的保存路径
        lastSavePath = prefs.get("lastFilePath", System.getProperty("user.home") + "/Downloads");


        // 读取 Look and Feel
        String lookAndFeel = prefs.get("lookAndFeel", "javax.swing.plaf.metal.MetalLookAndFeel");
        try {
            UIManager.setLookAndFeel(lookAndFeel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saveSettings() {
        // 保存设置
        // 保存窗口大小
        prefs.putInt(PREF_WINDOW_WIDTH, getWidth());
        prefs.putInt(PREF_WINDOW_HEIGHT, getHeight());

        // 保存文件对话框上次的保存路径
        prefs.put("lastFilePath", fileDialog.getCurrentDirectory().getAbsolutePath());

        // 保存 Look and Feel
        prefs.put("lookAndFeel", UIManager.getLookAndFeel().getClass().getName());

    }


}