package org.agmip.ui.cropmarker;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import org.agmip.common.Functions;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.LoggerFactory;

/**
 * The dialog activated by Main page to change the path for the executable file
 * of each model/tools. When add new variables in this dialog, ConfigHelper
 * should be modified in the same time.
 *
 * @author Meng Zhang
 */
public class PathConfigDialog extends Dialog implements Bindable {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PathConfigDialog.class);

    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {

        try {
            setConfigComponents(ns, "quaduiPath");
            setConfigComponents(ns, "acmouiPath");
            setConfigComponents(ns, "dssat45ExePath");
            setConfigComponents(ns, "apsim75ExePath");
        } catch (Exception ex) {
            LOG.error(Functions.getStackTrace(ex));
            Alert.alert(MessageType.ERROR, "Failed to load config", PathConfigDialog.this);
        }
    }

    private void setConfigComponents(Map<String, Object> ns, String id) throws Exception {
        String mid = id.substring(0, 1).toUpperCase() + id.substring(1);
        Method getMtd = ConfigHelper.class.getDeclaredMethod("get" + mid);
        final Method setMtd = ConfigHelper.class.getDeclaredMethod("set" + mid, String.class);
        final TextInput text = (TextInput) ns.get(id + "Text");
        text.setText((String) getMtd.invoke(ConfigHelper.class));
        PushButton button = (PushButton) ns.get(id + "Btn");
        button.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;
                browse = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, new File(text.getText()).getParent());
                browse.setDisabledFileFilter(new Filter<File>() {

                    @Override
                    public boolean include(File file) {
                        return (file.isFile()
                                && (!file.getName().toLowerCase().endsWith(".exe"))
                                && (!file.getName().toLowerCase().endsWith(".jar")));
                    }
                });
                browse.open(PathConfigDialog.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File outputDir = browse.getSelectedFile();
                            text.setText(outputDir.getPath());
                            try {
                                setMtd.invoke(ConfigHelper.class, outputDir.getPath());
                            } catch (Exception ex) {
                                LOG.error(Functions.getStackTrace(ex));
                                Alert.alert(MessageType.ERROR, "Failed to load config", PathConfigDialog.this);
                            }
                        }
                    }
                });
            }
        });
    }
}
