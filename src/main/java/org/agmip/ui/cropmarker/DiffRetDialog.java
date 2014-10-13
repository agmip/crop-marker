package org.agmip.ui.cropmarker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.agmip.common.Functions;
import org.agmip.utility.testframe.comparator.TestComparator;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.slf4j.LoggerFactory;

/**
 * The dialog activated by build test page when test flow finished to show the
 * comparison result. If user confirm all the differences are acceptable, then
 * baseline of expected file will be updated to the latest simulation result.
 *
 * @author Meng Zhang
 */
public class DiffRetDialog extends Dialog implements Bindable {

    private BoxPane reportListBP;
    private ArrayList<LinkButton> linkBoxes = new ArrayList();
    private PushButton applyBtn;
    private ButtonPressListener applyBtnListener = null;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DiffRetDialog.class);

    @Override
    public void initialize(Map<String, Object> ns, URL url, Resources rsrcs) {
        reportListBP = (BoxPane) ns.get("reportList");
        applyBtn = (PushButton) ns.get("applyBtn");
    }

    public void setReportFiles(ArrayList<File> files) {

        // Setup file list
        reportListBP.removeAll();
        for (File file : files) {
            final String path = file.getAbsolutePath();
            LinkButton linkBox = new LinkButton(file.getName());
            linkBox.getButtonPressListeners().add(new ButtonPressListener() {
                @Override
                public void buttonPressed(Button button) {
                    try {
                        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", "\"\"", path);
                        pb.start();
                    } catch (IOException winEx) {
                        try {
                            ProcessBuilder pb = new ProcessBuilder("open", path);
                            pb.start();
                        } catch (IOException macEx) {
                            Alert.alert(MessageType.ERROR, "Your OS can not open the file by using this link", reportListBP.getWindow());
                            LOG.error(Functions.getStackTrace(winEx));
                            LOG.error(Functions.getStackTrace(macEx));
                        }
                    }
                }
            });
            reportListBP.add(linkBox);
        };
    }

    public void setApplyBtn(final HashMap<File, File> applyFiles) {

        if (applyBtnListener != null) {
            applyBtn.getButtonPressListeners().remove(applyBtnListener);
        }
        applyBtnListener = new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                LOG.info("{} will be applied to the base", applyFiles);
                ArrayList<String> copyErrors = new ArrayList();

                for (java.util.Map.Entry<File, File> entry : applyFiles.entrySet()) {
                    File exp = entry.getValue();
                    File act = entry.getKey();
                    try {
                        BufferedWriter bw = new BufferedWriter(new FileWriter(exp, false));
                        BufferedReader br = new BufferedReader(new FileReader(act));
                        String line;
                        while ((line = br.readLine()) != null) {
                            bw.write(line);
                            bw.newLine();
                        }
                        bw.flush();
                        bw.close();
                        br.close();
                        LOG.info("{} has been updated", exp.getName());
                    } catch (IOException ex) {
                        LOG.warn(Functions.getStackTrace(ex));
                        copyErrors.add(exp.getName());
                    }
                }
                if (!copyErrors.isEmpty()) {
                    final BoxPane pane = new BoxPane(Orientation.VERTICAL);
                    for (String error : copyErrors) {
                        pane.add(new Label(error));
                    }
                    Alert.alert(MessageType.ERROR, "Cannot apply the following files", pane, DiffRetDialog.this);
                } else {
                    Alert.alert(MessageType.INFO, "Update finished", DiffRetDialog.this, new DialogCloseListener() {
                        @Override
                        public void dialogClosed(Dialog dialog, boolean bln) {
                            DiffRetDialog.this.close();
                        }
                    });
                }
                LOG.info("=== Completed test job ===");
            }
        };
        applyBtn.getButtonPressListeners().add(applyBtnListener);
    }
}
