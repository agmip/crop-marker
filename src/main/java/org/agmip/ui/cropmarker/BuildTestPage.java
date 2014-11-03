package org.agmip.ui.cropmarker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.agmip.common.Functions;
import org.agmip.util.MapUtil;
import org.agmip.utility.testframe.comparator.TestComparator;
import org.agmip.utility.testframe.model.TestDefBuilder;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.DialogCloseListener;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The page used for setup the test configuration and invoke the whole test
 * flow.
 *
 * @author Meng Zhang
 */
public class BuildTestPage extends Page {

    private static final Logger LOG = LoggerFactory.getLogger(BuildTestPage.class);
    private ActivityIndicator testIndicator = null;
    private ListButton testDataListBtn = null;
    private PushButton startButton = null;
    private PushButton backButton = null;
    private PushButton browseOutputDir = null;
    private Checkbox outputCB = null;
    private Checkbox modelApsim = null;
    private Checkbox modelDssat = null;
//    private Checkbox modelStics = null;
    private Checkbox modelWofost = null;
//    private Checkbox modelCgnau = null; 
    private Checkbox modelJson = null;
    private Checkbox comparatorAll = null;
    private Checkbox comparatorAcmo = null;
    private Checkbox comparatorYield = null;
    private Checkbox comparatorNone = null;
    DiffRetDialog diffRetDialog = null;
    private Label txtStatus = null;
//    private Label txtAutoDomeApplyMsg = null;
    private TextInput outputText = null;
    private final ArrayList<Checkbox> modelCBGroup = new ArrayList<Checkbox>();
    private final ArrayList<Checkbox> comparatorCBGroup = new ArrayList<Checkbox>();
// TODO    private Preferences pref = Preferences.userNodeForPackage(getClass());
    private final HashMap<String, DataSpecConfig> dataSpecConfig = new HashMap();
    private DataSpecConfig curConfig = new DataSpecConfig();
    protected final static String DEF_SELECTED_DATA_NAME = "ALL";
    protected final static String DEF_WORK_PATH = new File("work").getAbsolutePath();

    private ArrayList<String> validateInputs() {
        ArrayList<String> errs = new ArrayList<String>();
        for (String dataName : dataSpecConfig.keySet()) {
            DataSpecConfig config = dataSpecConfig.get(dataName);
            if (config.getModels().isEmpty()) {
                errs.add("Need to select a model for " + dataName);
            }
            if (config.getComparators().isEmpty()) {
                errs.add("Need to select a comparator for " + dataName);
            }
        }
        File outputDir = new File(outputText.getText());
        if (!outputDir.exists() || !outputDir.isDirectory()) {
            errs.add("You need to select an output directory");
        }
        if (!new File(ConfigHelper.getAcmouiPath()).exists()) {
            errs.add("Missing the executable of ACMOUI");
        }
        if (!new File(ConfigHelper.getQuaduiPath()).exists()) {
            errs.add("Missing the executable of QuadUI");
        }
        if (!new File(ConfigHelper.getApsim75ExePath()).exists()) {
            errs.add("Missing the executable of APSIM 7.5");
        }
        if (!new File(ConfigHelper.getDssat45ExePath()).exists()) {
            errs.add("Missing the executable of DSSAT 4.5");
        }
        return errs;
    }

    @Override
    public void init(Map<String, Object> ns, URL location, Resources res) {
        testIndicator = (ActivityIndicator) ns.get("testIndicator");
        testDataListBtn = (ListButton) ns.get("testDataList");
        startButton = (PushButton) ns.get("startButton");
        backButton = (PushButton) ns.get("backToMain");
        browseOutputDir = (PushButton) ns.get("browseOutputButton");
        txtStatus = (Label) ns.get("txtStatus");
//        txtAutoDomeApplyMsg = (Label) ns.get("txtAutoDomeApplyMsg");
        txtVersion = (Label) ns.get("txtVersion");
        outputText = (TextInput) ns.get("outputText");
        outputCB = (Checkbox) ns.get("outputCB");
        modelApsim = (Checkbox) ns.get("model-apsim");
        modelDssat = (Checkbox) ns.get("model-dssat");
//        modelStics          = (Checkbox) ns.get("model-stics");
        modelWofost = (Checkbox) ns.get("model-wofost");
//        modelCgnau          = (Checkbox) ns.get("model-cgnau");
        modelJson = (Checkbox) ns.get("model-json");
        comparatorAll = (Checkbox) ns.get("comparator-all");
        comparatorAcmo = (Checkbox) ns.get("comparator-acmo");
        comparatorYield = (Checkbox) ns.get("comparator-yield");
        comparatorNone = (Checkbox) ns.get("comparator-none");
        diffRetDialog = (DiffRetDialog) ns.get("diffRet");

        modelCBGroup.add(modelApsim);
        modelCBGroup.add(modelDssat);
//        checkboxGroup.add(modelStics);
        modelCBGroup.add(modelWofost);
//        checkboxGroup.add(modelCgnau);
        modelCBGroup.add(modelJson);
        comparatorCBGroup.add(comparatorAll);
        comparatorCBGroup.add(comparatorAcmo);
        comparatorCBGroup.add(comparatorYield);
        comparatorCBGroup.add(comparatorNone);

        File f;
        if (!(f = new File(DEF_WORK_PATH)).exists()) {
            f.mkdirs();
        }
        outputText.setText(DEF_WORK_PATH);
//        mode = "none";

        testDataListBtn.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {

//            @Override
//            public void selectedIndexChanged(ListButton lb, int i) {
//            }
            @Override
            public void selectedItemChanged(ListButton lb, Object o) {
                LOG.debug("Last config for {} : {}", o, curConfig);
                switchDataConfig((String) o);
                LOG.debug("Currrent config for {} : {}", testDataListBtn.getButtonData(), curConfig);
            }
        });

        outputCB.getButtonStateListeners().add(new ButtonStateListener() {

            @Override
            public void stateChanged(Button button, State state) {
                boolean isOutputUseDef = button.isSelected();
                if (isOutputUseDef) {
                    outputText.setText(DEF_WORK_PATH);
                    curConfig.setOutputDir(outputText.getText());
                } else {
                    outputText.setText(curConfig.getOutputDir());
                }
                browseOutputDir.setEnabled(!isOutputUseDef);
                curConfig.setIsOutputUseDef(isOutputUseDef);
            }
        });

        backButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                BXMLSerializer bxml = new BXMLSerializer();
                MainPage window;
                try {
                    window = (MainPage) bxml.readObject(getClass().getResource("/page_main.bxml"));
                    window.setPostParams(postParams);
                    window.open(button.getDisplay());
                } catch (IOException ex) {
                    LOG.error(ex.getMessage());
                } catch (SerializationException ex) {
                    LOG.error(ex.getMessage());
                }
            }
        });

        startButton.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {
                ArrayList<String> validationErrors = validateInputs();
                if (!validationErrors.isEmpty()) {
                    final BoxPane pane = new BoxPane(Orientation.VERTICAL);
                    for (String error : validationErrors) {
                        pane.add(new Label(error));
                    }
                    Alert.alert(MessageType.ERROR, "Cannot Start", pane, BuildTestPage.this);
                    return;
                }
//                modelSpecFiles = null;
                LOG.info("Starting test job");
                try {
                    prepareTestFlow();
                } catch (Exception ex) {
                    LOG.error(Functions.getStackTrace(ex));
                    Alert.alert(MessageType.ERROR, ex.toString(), BuildTestPage.this);
                    enableConvertIndicator(false);
                }
            }
        });

        browseOutputDir.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                final FileBrowserSheet browse;
//                if (outputText.getText().equals("")) {
////                    browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
//                    String lastPath = pref.get("last_output", "");
//                    if (lastPath.equals("") || !new File(lastPath).exists()) {
//                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO);
//                    } else {
//                        File f = new File(lastPath);
//                        browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, f.getParent());
//                    }
//                } else {
                File f = new File(outputText.getText());
                browse = new FileBrowserSheet(FileBrowserSheet.Mode.SAVE_TO, f.getParent());
//                }
                browse.open(BuildTestPage.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            File outputDir = browse.getSelectedFile();
                            outputText.setText(outputDir.getPath());
                            curConfig.setOutputDir(outputDir.getPath());
//                            pref.put("last_output", outputDir.getPath());
                        }
                    }
                });
            }
        });

        initModelCheckBox(modelApsim, "last_model_select_apsim");
        initModelCheckBox(modelDssat, "last_model_select_dssat");
//        initCheckBox(modelCgnau, "last_model_select_cgnau");
//        initCheckBox(modelStics, "last_model_select_stics");
        initModelCheckBox(modelWofost, "last_model_select_wofost");
        initModelCheckBox(modelJson, "last_model_select_json");
        initComparatorCheckBox(comparatorAll, "last_comparator_select_all");
        initComparatorCheckBox(comparatorAcmo, "last_comparator_select_acmo");
        initComparatorCheckBox(comparatorYield, "last_comparator_select_yield");
        initComparatorCheckBox(comparatorNone, "last_comparator_select_none");
    }

    private void switchDataConfig(String dataName) {
        String curDataName = testDataListBtn.getSelectedItem().toString();
        if (dataName == null) {
            dataSpecConfig.put(curDataName, curConfig);
            return;
        }
        dataSpecConfig.put(dataName, curConfig);
        if (dataSpecConfig.containsKey(curDataName)) {
            curConfig = MapUtil.getObjectOr(dataSpecConfig, curDataName, new DataSpecConfig());
        } else {
            curConfig = MapUtil.getObjectOr(dataSpecConfig, DEF_SELECTED_DATA_NAME, new DataSpecConfig()).copy();
            dataSpecConfig.put(curDataName, curConfig);
        }
        switchCheckboxesSelection();
//        switchOutputBox(config.getIsOutputUseDef());
        outputCB.setSelected(curConfig.getIsOutputUseDef());
    }

    private void switchCheckboxesSelection() {
        for (Checkbox cbox : modelCBGroup) {
            if (curConfig.isModelSelected((String) cbox.getButtonData())) {
                cbox.setSelected(true);
            } else {
                cbox.setSelected(false);
            }
        }
        for (Checkbox cbox : comparatorCBGroup) {
            if (curConfig.isComparatorSelected((String) cbox.getButtonData())) {
                cbox.setSelected(true);
            } else {
                cbox.setSelected(false);
            }
        }
    }

    @Override
    protected void readPostParams() {
        // read info from postParams
        if (postParams != null) {
            for (Object key : postParams.keySet()) {
                LOG.debug("{} : {}", key, postParams.get(key));
            }
            ArrayList<String> arr;
            ArrayList postArr = MapUtil.getObjectOr(postParams, "testDataList", new ArrayList());
            if (postArr.size() < 2) {
                arr = postArr;
            } else {
                arr = new ArrayList();
                arr.add(DEF_SELECTED_DATA_NAME);
                arr.addAll(postArr);
            }
            LOG.debug("postParams: {}", arr.toString());
            testDataListBtn.setListData(toListDataStr(arr));
            testDataListBtn.setSelectedIndex(0);
            dataSpecConfig.put(arr.get(0), curConfig);
        } else {
            LOG.debug("postParams = null");
        }
    }

    private String toListDataStr(ArrayList arr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object val : arr) {
            sb.append("'").append(val.toString()).append("',");
        }
        if (!arr.isEmpty()) {
            sb.delete(sb.length() - 1, sb.length());
        }
        sb.append("]");
        return sb.toString();
    }

    private void prepareTestFlow() throws Exception {
        enableConvertIndicator(true);
        LOG.info("Preparing test flow...");
        txtStatus.setText("Preparing test flow...");

        // Preparing App runners
        ArrayList<String> testDataList = MapUtil.getObjectOr(postParams, "testDataList", new ArrayList());
        TestBuilderTask task = new TestBuilderTask(testDataList, dataSpecConfig);
        TaskListener<TestDefBuilder> listener = new TaskListener<TestDefBuilder>() {

            @Override
            public void taskExecuted(Task<TestDefBuilder> t) {
                TestDefBuilder builder = t.getResult();
                if (builder != null) {
                    startAppRuner(builder);
                } else {
                    Alert.alert(MessageType.ERROR, "Failed to create test flow", BuildTestPage.this);
                    enableConvertIndicator(false);
                }
            }

            @Override
            public void executeFailed(Task<TestDefBuilder> arg0) {
                Alert.alert(MessageType.ERROR, arg0.getFault().toString(), BuildTestPage.this);
                LOG.error(Functions.getStackTrace(arg0.getFault()));
                enableConvertIndicator(false);
            }
        };
        task.execute(new TaskAdapter<TestDefBuilder>(listener));
    }

    private void startAppRuner(TestDefBuilder builder) {

        LOG.info("Start test flow...");
        txtStatus.setText("Start test flow...");
        AppRunnerTask task = new AppRunnerTask(builder);
        TaskListener<ArrayList<TestComparator>> listener = new TaskListener<ArrayList<TestComparator>>() {

            @Override
            public void taskExecuted(Task<ArrayList<TestComparator>> t) {
                ArrayList<TestComparator> comparators = t.getResult();
                if (comparators != null) {
                    compareResult(comparators);
                } else {
                    Alert.alert(MessageType.ERROR, "Failed to run test flow", BuildTestPage.this);
                    enableConvertIndicator(false);
                }
            }

            @Override
            public void executeFailed(Task<ArrayList<TestComparator>> arg0) {
                Alert.alert(MessageType.ERROR, arg0.getFault().toString(), BuildTestPage.this);
                LOG.error(Functions.getStackTrace(arg0.getFault()));
                enableConvertIndicator(false);
            }
        };
        task.execute(new TaskAdapter<ArrayList<TestComparator>>(listener));
    }

    private void compareResult(ArrayList<TestComparator> comparators) {

        LOG.info("Handling comparation...");
        txtStatus.setText("Handling comparation...");
        ArrayList<File> reportFiles = new ArrayList();
        HashMap<File, File> applyFiles = new HashMap();
        for (TestComparator comparator : comparators) {
            if (!comparator.getLastCompareResult()) {
                LOG.info("{}: mismatched result has been detected.", comparator.getTitle());
                reportFiles.add(comparator.getReport());
                applyFiles.putAll(comparator.getDiffFiles());
            } else {
                LOG.info("{}: All results matched with expected data", comparator.getTitle());
            }
        }
        if (reportFiles.isEmpty()) {
            txtStatus.setText("Match with expeceted");
            Alert.alert(MessageType.INFO, "All tests are passed", BuildTestPage.this);
            LOG.info("=== Completed test job ===");
        } else {
            txtStatus.setText("Mismatch with expeceted");
            diffRetDialog.setApplyBtn(applyFiles);
            diffRetDialog.setReportFiles(reportFiles);
            diffRetDialog.open(BuildTestPage.this, new DialogCloseListener() {

                @Override
                public void dialogClosed(Dialog dialog, boolean bln) {
                    txtStatus.setText("Need re-test");
                }
            });
        }
        enableConvertIndicator(false);
    }

    private void enableConvertIndicator(boolean enabled) {
        testIndicator.setActive(enabled);
        startButton.setEnabled(!enabled);
        backButton.setEnabled(!enabled);
        testDataListBtn.setEnabled(!enabled);
        for (Checkbox c : modelCBGroup) {
            c.setEnabled(!enabled);
        }
        for (Checkbox c : comparatorCBGroup) {
            c.setEnabled(!enabled);
        }
        outputCB.setEnabled(!enabled);
        if (!outputCB.isSelected()) {
            browseOutputDir.setEnabled(!enabled);
        }
    }

//    private void SetAutoDomeApplyMsg() {
////        File convertFile = new File(convertText.getText());
////        String fileName = convertFile.getName().toLowerCase();
//        String msg = "";
////        autoApply = false;
////        if (!linkText.getText().equals("")) {
////            msg = "";
////            autoApply = false;
////        }
////        else if (fileName.endsWith(".zip")) {
////            try {
////                ZipFile zf = new ZipFile(convertFile);
////                Enumeration<? extends ZipEntry> e = zf.entries();
////                while (e.hasMoreElements()) {
////                    ZipEntry ze = (ZipEntry) e.nextElement();
////                    String zeName = ze.getName().toLowerCase();
////                    if (!zeName.endsWith(".csv")) {
////                        msg = "Selected DOME will be Auto applied";
////                        autoApply = true;
////                    } else {
////                        msg = "";
////                        autoApply = false;
////                        break;
////                    }
////                }
////                zf.close();
////            } catch (IOException ex) {
////            }
////
////        } else if (!fileName.endsWith(".csv")) {
////            msg = "Selected DOME will be Auto applied";
////            autoApply = true;
////        }
//        txtAutoDomeApplyMsg.setText(msg);
//    }
//    private FileBrowserSheet openFileBrowserSheet(String lastPathId) {
//        if (convertText.getText().equals("")) {
//        String lastPath = pref.get(lastPathId, "");
//        File tmp = new File(lastPath);
//        if (lastPath.equals("") || !tmp.exists()) {
//            return new FileBrowserSheet(FileBrowserSheet.Mode.OPEN);
//        } else {
//            if (!tmp.isDirectory()) {
//                lastPath = tmp.getParentFile().getPath();
//            }
//            return new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, lastPath);
//        }
//        } else {
//            try {
//                String path = new File(convertText.getText()).getCanonicalFile().getParent();
//                return new FileBrowserSheet(FileBrowserSheet.Mode.OPEN, path);
//            } catch (IOException ex) {
//                return new FileBrowserSheet(FileBrowserSheet.Mode.OPEN);
//            }
//        }
//    }
    private void initModelCheckBox(Checkbox cb, final String lastSelectId) {
//        cb.setSelected(pref.getBoolean(lastSelectId, false));
        cb.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {

//                pref.putBoolean(lastSelectId, button.isSelected());
                String model = (String) button.getButtonData();
                if (button.isSelected()) {
                    curConfig.selecteModel(model);
                } else {
                    curConfig.unselecteModel(model);
                }
            }
        });
    }

    private void initComparatorCheckBox(Checkbox cb, final String lastSelectId) {
//        cb.setSelected(pref.getBoolean(lastSelectId, false));
        cb.getButtonPressListeners().add(new ButtonPressListener() {

            @Override
            public void buttonPressed(Button button) {

//                pref.putBoolean(lastSelectId, button.isSelected());
                String model = (String) button.getButtonData();
                if (button.isSelected()) {
                    curConfig.selecteComparator(model);
                } else {
                    curConfig.unselecteComparator(model);
                }
            }
        });
    }

    protected class DataSpecConfig {

        private boolean isOutputUseDef = true;
        private String outputDir = DEF_WORK_PATH;
        private HashSet<String> models = new HashSet();
        private HashSet<String> comparators = new HashSet();

        public void setIsOutputUseDef(boolean isOutputUseDef) {
            this.isOutputUseDef = isOutputUseDef;
        }

        public boolean getIsOutputUseDef() {
            return this.isOutputUseDef;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public String getOutputDir() {
            return this.outputDir;
        }

        public ArrayList<String> getModels() {
            return new ArrayList<String>(models);
        }

        public void setModels(ArrayList<String> models) {
            this.models = new HashSet(models);
        }

        public boolean isModelSelected(String model) {
            return models.contains(model);
        }

        public void selecteModel(String model) {
            models.add(model);
        }

        public void unselecteModel(String model) {
            models.remove(model);
        }

        public ArrayList<String> getComparators() {
            return new ArrayList<String>(comparators);
        }

        public void setComparators(ArrayList<String> comparators) {
            this.comparators = new HashSet(comparators);
        }

        public boolean isComparatorSelected(String comparator) {
            return comparators.contains(comparator);
        }

        public void selecteComparator(String comparator) {
            comparators.add(comparator);
        }

        public void unselecteComparator(String comparator) {
            comparators.remove(comparator);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("OutputUseDef:[");
            sb.append(isOutputUseDef);
            sb.append("], OutputDir:[");
            sb.append(outputDir);
            sb.append("], Models:[");
            sb.append(models);
            sb.append("], Comparators:[");
            sb.append(comparators);
            sb.append("]");
            return sb.toString();
        }

        public DataSpecConfig copy() {
            DataSpecConfig ret = new DataSpecConfig();
            ret.setIsOutputUseDef(this.isOutputUseDef);
            ret.setOutputDir(this.outputDir);
            ret.setModels(this.getModels());
            ret.setComparators(this.getComparators());
            return ret;
        }
    }
}
