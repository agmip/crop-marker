package org.agmip.ui.cropmarker;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.agmip.common.Functions;
import org.agmip.util.JSONAdapter;
import org.agmip.util.MapUtil;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.media.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The page used for listing the available data sets and lead to configuration
 * dialog for executable path of each involved programs.
 *
 * @author Meng Zhang
 */
public class MainPage extends Page {

    private static final Logger LOG = LoggerFactory.getLogger(MainPage.class);
//    private ListView dataListview = null;
    private TableView datasetListTableView = null;
    private PushButton nextButton = null;
    private PushButton testButton = null;

    @Override
    protected void init(Map<String, Object> ns, URL url, Resources rsrcs) {
//        dataListview = (ListView) ns.get("dataList");
        datasetListTableView = (TableView) ns.get("datasetListTableView");
        nextButton = (PushButton) ns.get("nextBtn");

//        // For dialog debug
//        testButton = (PushButton) ns.get("testBtn");
//        final DiffRetDialog dialog = (DiffRetDialog) ns.get("diffRet");
//        testButton.getButtonPressListeners().add(new ButtonPressListener() {
//            @Override
//            public void buttonPressed(Button button) {
//                ArrayList<File> files = new ArrayList();
//                files.add(new File("D:\\SSD_USER\\Documents\\NetBeansProjects\\Develop\\crop-marker\\work\\GHNY9701_PNX\\DSSAT\\ACMO_GHNY9701_PNX_DSSAT45_Comparator-Diff_Report.html"));
//                files.add(new File("abc1.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc21111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                files.add(new File("abc2.csv"));
//                dialog.open(button.getWindow());
//                dialog.setReportFiles(files);
//            }
//        });
        nextButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                ArrayList<String> errors = validateInputs();
                if (errors.isEmpty()) {
                    LOG.debug("To build_test page...");
                    BXMLSerializer bxml = new BXMLSerializer();
                    BuildTestPage window;
                    try {
                        window = (BuildTestPage) bxml.readObject(getClass().getResource("/page_build.bxml"));
                        window.setPostParams(preparePostParams());
                        window.open(button.getDisplay());
                    } catch (IOException ex) {
                        LOG.error(Functions.getStackTrace(ex));
                    } catch (SerializationException ex) {
                        LOG.error(Functions.getStackTrace(ex));
                    }
                } else {
                    final BoxPane pane = new BoxPane(Orientation.VERTICAL);
                    for (String error : errors) {
                        pane.add(new Label(error));
                    }
                    Alert.alert(MessageType.ERROR, "Cannot Start test", pane, MainPage.this);
                }
            }
        });

        datasetListTableView.setTableData(scanDataDir());
        datasetListTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public boolean mouseClick(Component comp, Mouse.Button button, int x, int y, int count) {
                if (button == Mouse.Button.LEFT) {
                    org.apache.pivot.collections.List<SelectableTableRow> datasetListTableData
                            = (org.apache.pivot.collections.List<SelectableTableRow>) datasetListTableView.getTableData();

                    int curRowIndex = datasetListTableView.getRowAt(y);
                    if (curRowIndex < 0) {
                        return false;
                    }
                    SelectableTableRow curRow = datasetListTableData.get(curRowIndex);
                    boolean flag = !curRow.getCheckFlg();

                    if (datasetListTableView.getSelectedRows().getLength() > 1 && datasetListTableView.getSelectedRanges().getLength() == 1) {
                        Span s = datasetListTableView.getSelectedRanges().get(0);
                        for (int i = s.start; i <= s.end; i++) {
                            SelectableTableRow row = datasetListTableData.get(i);
                            row.setCheckFlg(flag);
                            datasetListTableData.update(i, row);
                        }
                    } else {
                        curRow.setCheckFlg(flag);
                        datasetListTableData.update(curRowIndex, curRow);
//                    }
                    }
                }

                return false;
            }
        });
    }

    @Override
    protected void readPostParams() {
        if (postParams != null && !postParams.isEmpty()) {
            ArrayList testDataList = MapUtil.getObjectOr(postParams, "testDataList", new ArrayList());
            setSelectedTestDataList(testDataList);
        }
    }

    // TODO
    private org.apache.pivot.collections.ArrayList scanDataDir() {
        org.apache.pivot.collections.ArrayList<SelectableTableRow> ret = new org.apache.pivot.collections.ArrayList();
        for (File sub : DEF_DATA_DIR.listFiles()) {
            if (sub.isDirectory()) {
                CustomTableRow row = new CustomTableRow();
                row.setName(sub.getName());
                File meta = new File(sub.getPath() + "//meta.json");
                if (meta.exists()) {
                    try {
                        HashMap metaInfo = JSONAdapter.fromJSONFile(meta.getPath());
                        row.setCrop(MapUtil.getValueOr(metaInfo, "crop", "N/A"));
                        row.setFormat(MapUtil.getValueOr(metaInfo, "format", "N/A"));
                        row.setDescription(MapUtil.getValueOr(metaInfo, "description", ""));
                        String iconName = MapUtil.getValueOr(metaInfo, "icon", "").trim();
                        if (!iconName.equals("")) {
                            File icon = new File(sub + File.separator + iconName);
                            if (icon.exists()) {
                                String p = icon.getPath();
                                row.setIcon(p);
                            } else {
                                LOG.warn("Missing icon file {} for {} data set.", iconName, sub.getName());
                            }
                        }
                    } catch (IOException ex) {
                        LOG.error(Functions.getStackTrace(ex));
                    }
                } else {
                    boolean isInputValid = false;
                    boolean isExpectedValid = false;
                    for (File dir : sub.listFiles()) {
                        if (dir.getName().equalsIgnoreCase("input")) {
                            isInputValid = true;
                        } else if (dir.getName().equalsIgnoreCase("expeceted")) {
                            isExpectedValid = true;
                        }
                    }
                    if (!isInputValid || !isExpectedValid) {
                        row.setDescription("*** Not ready yet ***");
                        row.setIcon("");
                    }
                }

                ret.add(row);
            }
        }
        return ret;
    }

    private HashMap preparePostParams() {
        HashMap params = new HashMap();
        params.put("testDataList", getSelectedTestDataList());
        return params;
    }

    private ArrayList getSelectedTestDataList() {
        ArrayList arr = new ArrayList();
        org.apache.pivot.collections.List<CustomTableRow> datasetListTableData
                = (org.apache.pivot.collections.List<CustomTableRow>) datasetListTableView.getTableData();
        for (CustomTableRow row : datasetListTableData) {
            if (row.getCheckFlg()) {
                arr.add(row.getName());
            }
        }
        return arr;
    }

    private void setSelectedTestDataList(ArrayList arr) {
        if (arr.isEmpty()) {
            return;
        }
        org.apache.pivot.collections.List<CustomTableRow> datasetListTableData
                = (org.apache.pivot.collections.List<CustomTableRow>) datasetListTableView.getTableData();
        HashSet selected = new HashSet(arr);
        for (int i = 0; i < datasetListTableData.getLength(); i++) {
            CustomTableRow row = datasetListTableData.get(i);
            if (selected.contains(row.getName())) {
                row.setCheckFlg(true);
                datasetListTableData.update(i, row);
            }
        }
    }

    private ArrayList<String> validateInputs() {
        ArrayList<String> errs = new ArrayList<String>();
        if (getSelectedTestDataList().isEmpty()) {
            errs.add("You need to select at least one data set");
        }
        return errs;
    }

    public static final class CustomTableRow extends SelectableTableRow {

        private String crop = "";
        private String name = "";
        private String format = "";
        private String description = "";
        private Image icon = null;

        public CustomTableRow() {
            setIcon("/help.png");
        }

        public String getCrop() {
            return this.crop;
        }

        public void setCrop(String crop) {
            this.crop = crop;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getFormat() {
            return this.format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getDescription() {
            return this.description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Image getIcon() {
            return this.icon;
        }

        public void setIcon(Image icon) {
            this.icon = icon;
        }

        public void setIcon(URL url) {
            Image imageFromURL = (Image) ApplicationContext.getResourceCache().get(url);

            if (imageFromURL == null) {
                try {
                    imageFromURL = Image.load(url);
                } catch (TaskExecutionException exception) {
                    throw new RuntimeException(exception);
                }

                ApplicationContext.getResourceCache().put(url, imageFromURL);
            }

            CustomTableRow.this.setIcon(imageFromURL);
        }

        public void setIcon(String path) {
            if (path == null || path.equals("")) {
                this.icon = null;
                return;
            }
            try {
                URL url = this.getClass().getResource(path);
                if (url == null) {
                    url = new File(path).toURI().toURL();
                }
                if (url != null) {
                    CustomTableRow.this.setIcon(url);
                }
            } catch (MalformedURLException ex) {
                LOG.error(Functions.getStackTrace(ex));
            }

        }
    }
}
