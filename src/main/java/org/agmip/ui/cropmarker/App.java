package org.agmip.ui.cropmarker;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;

public class App extends Application.Adapter {
    private MainPage window = null;

    @Override
    public void startup(Display display, Map<String, String> props) throws Exception {
        BXMLSerializer bxml = new BXMLSerializer();
        window = (MainPage) bxml.readObject(getClass().getResource("/page_main.bxml"));
        window.ptintSysInfo();
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean opt) {
        if (window != null) {
            window.close();
        }
        return false;
    }

    public static void main(String[] args) {
        boolean cmdFlg = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("-cli")) {
                cmdFlg = true;
                break;
            }
        }
        if (cmdFlg) {
//            ATCmdLine cmd = new ATCmdLine();
//            cmd.run(args);
            
        } else {
            DesktopApplicationContext.main(App.class, args);
        }
    }
}
