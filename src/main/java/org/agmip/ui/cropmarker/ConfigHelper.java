package org.agmip.ui.cropmarker;

import java.io.File;
import java.util.prefs.Preferences;

/**
 * Help to load/save the configuration information of test frame.
 *
 * @author Meng Zhang
 */
public class ConfigHelper {

    private final static String quaduiPath = "bin\\QuadUI\\quadui-1.2.1-SNAPSHOT-Beta28.jar";
    private static final String acmouiPath = "bin\\ACMOUI\\acmoui-1.2-SNAPSHOT-beta7.jar";
    private static final String dssat45ExePath = "C:\\dssat45\\dscsm045.exe";
    private static final String apsim75ExePath = "C:\\Program Files (x86)\\Apsim75-r3008\\Model\\Apsim.exe";
    private static final Preferences pref = Preferences.userNodeForPackage(Page.class);

    public static String getQuaduiPath() {
        return pref.get("quaduiPath", new File(quaduiPath).getAbsolutePath());
    }

    public static void setQuaduiPath(String path) {
        pref.put("quaduiPath", path);
    }

    public static String getAcmouiPath() {
        return pref.get("acmouiPath", new File(acmouiPath).getAbsolutePath());
    }

    public static void setAcmouiPath(String path) {
        pref.put("acmouiPath", path);
    }

    public static String getDssat45ExePath() {
        return pref.get("dssat45ExePath", dssat45ExePath);
    }

    public static void setDssat45ExePath(String path) {
        pref.put("dssat45ExePath", path);
    }

    public static String getApsim75ExePath() {
        return pref.get("apsim75ExePath", apsim75ExePath);
    }

    public static void setApsim75ExePath(String path) {
        pref.put("apsim75ExePath", path);
    }
}
