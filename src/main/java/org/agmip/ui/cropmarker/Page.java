package org.agmip.ui.cropmarker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.Stack;
import java.util.prefs.Preferences;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The template page for common functionalities and features.
 *
 * @author Meng Zhang
 */
public abstract class Page extends Window implements Bindable {

    protected Preferences pref = Preferences.userNodeForPackage(Page.class);
    protected final static File DEF_DATA_DIR = new File("data");
    protected final static String DEF_DATA_PATH = DEF_DATA_DIR.getAbsolutePath();
    private static final Logger LOG = LoggerFactory.getLogger(Page.class);
    protected Label txtVersion = null;
    protected String atVersion = "";
    protected static Stack<HashMap<String, Object>> pastParams = new Stack();
    protected HashMap postParams = null;

    public Page() {
        try {
            Properties versionProperties = new Properties();
            InputStream versionFile = getClass().getClassLoader().getResourceAsStream("product.properties");
            versionProperties.load(versionFile);
            versionFile.close();
            StringBuilder qv = new StringBuilder();
            String buildType = versionProperties.getProperty("product.buildtype");
            qv.append("Version ");
            qv.append(versionProperties.getProperty("product.version"));
            qv.append("-").append(versionProperties.getProperty("product.buildversion"));
            qv.append("(").append(buildType).append(")");
            if (buildType.equals("dev")) {
                qv.append(" [").append(versionProperties.getProperty("product.buildts")).append("]");
            }
            atVersion = qv.toString();
        } catch (IOException ex) {
            LOG.error("Unable to load version information, version will be blank.");
        }

        Action.getNamedActions().put("fileQuit", new Action() {
            @Override
            public void perform(Component src) {
                DesktopApplicationContext.exit();
            }
        });
    }

    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {
        txtVersion = (Label) ns.get("txtVersion");
        txtVersion.setText(atVersion);
        init(ns, url, rsrcs);

//        if (rsrcs != null) {
//            Iterator<String> it = rsrcs.iterator();
//            while (it.hasNext()) {
//                String key = it.next();
//                System.out.print(key);
//                System.out.print(" : ");
//                System.out.println(rsrcs.get(key));
//                
//            }
//        } else {
//            System.out.println("rsrcs = null");
//        }
    }

    protected abstract void init(Map<String, Object> ns, URL url, Resources rsrcs);

    public void setPostParams(HashMap params) {
        this.postParams = params;
        pastParams.push(postParams);
        readPostParams();
    }

    protected HashMap<String, Object> getLastParams() {
        // TODO
        if (pastParams.empty()) {
            return new HashMap();
        } else {
            return pastParams.pop();
        }
    }

    protected abstract void readPostParams();

    public void ptintSysInfo() {
        LOG.info("Crop Marker {} lauched with JAVA {} under OS {}", atVersion, System.getProperty("java.runtime.version"), System.getProperty("os.name"));
    }
}
