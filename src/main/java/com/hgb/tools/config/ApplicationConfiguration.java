package com.hgb.tools.config;

import java.util.prefs.Preferences;

public class ApplicationConfiguration {

    public static int WINDOW_WIDTH = 1000;
    public static int WINDOW_HEIGHT = 600;
    public static String LAST_SAVE_PATH = System.getProperty("user.home") + "/Downloads";
    public static String LOOK_AND_FEEL = "javax.swing.plaf.metal.MetalLookAndFeel";

    public static final Preferences prefs = Preferences.userRoot().node("com/hgb/tools/hijson");
    public static final String PREF_WINDOW_WIDTH = "windowWidth";
    public static final String PREF_WINDOW_HEIGHT = "windowHeight";
    public static final String PREF_LOOK_AND_FEEL = "lookAndFeel";
    public static final String PREF_LAST_SAVE_PATH = "lastSavePath";


    public static void loadSettings() {
        ApplicationConfiguration.WINDOW_WIDTH = prefs.getInt(PREF_WINDOW_WIDTH, 1000); // 默认值为 1000
        ApplicationConfiguration.WINDOW_HEIGHT = prefs.getInt(PREF_WINDOW_HEIGHT, 600); // 默认值为 600
        ApplicationConfiguration.LAST_SAVE_PATH = prefs.get(PREF_LAST_SAVE_PATH, System.getProperty("user.home") + "/Downloads");
        ApplicationConfiguration.LOOK_AND_FEEL = prefs.get(PREF_LOOK_AND_FEEL, "javax.swing.plaf.metal.MetalLookAndFeel");
    }

}
