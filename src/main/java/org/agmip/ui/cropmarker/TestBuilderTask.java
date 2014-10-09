package org.agmip.ui.cropmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.agmip.common.Functions;
import org.agmip.ui.cropmarker.BuildTestPage.DataSpecConfig;
import static org.agmip.ui.cropmarker.Page.DEF_DATA_PATH;
import org.agmip.utility.testframe.comparator.TestComparator;
import static org.agmip.utility.testframe.comparator.TestComparator.Type.FILE;
import org.agmip.utility.testframe.model.TestDefBuilder;
import static org.agmip.utility.testframe.runner.AppRunner.Type.*;
import org.agmip.utility.testframe.runner.ApsimRunner;
import org.agmip.utility.testframe.runner.ExeRunner;
import org.agmip.utility.testframe.runner.JarRunner;
import org.agmip.utility.testframe.runner.QuadUIJarRunner;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.slf4j.LoggerFactory;

/**
 * Test flow builder, which call the AppRunnerBuilder to build the whole test
 * flow.
 *
 * @author Meng Zhang
 */
public class TestBuilderTask extends Task<TestDefBuilder> {

    private final ArrayList<String> testDataList;
    private final HashMap<String, DataSpecConfig> dataSpecConfig;
    private final String quaduiPath = ConfigHelper.getQuaduiPath();
    private final String acmouiPath = ConfigHelper.getAcmouiPath();
    private final String surveyDataPath = DEF_DATA_PATH + "\\%s\\input\\Survey_data.zip";
    private final String fieldDomePath = DEF_DATA_PATH + "\\%s\\input\\Field_Overlay.zip";
    private final String seasonalDomePath = DEF_DATA_PATH + "\\%s\\input\\Seasonal_strategy.zip";
    private final String dssat45ExePath = ConfigHelper.getDssat45ExePath();
    private final String modelWorkDir = "*%s.output_dir\\%s";
    private final String dssat45BatchFileName = "dssbatch.v45";
    private final String apsim75ExePath = ConfigHelper.getApsim75ExePath();
    private final String acmoInputDir = "*%s.output_dir";
    private final String actualAcmoCsvPath = DEF_DATA_PATH + "\\%s\\expeceted\\%s_%s\\%s";
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestBuilderTask.class);

    public TestBuilderTask(ArrayList<String> testDataList, HashMap<String, DataSpecConfig> dataSpecConfig) {
        this.testDataList = testDataList;
        this.dataSpecConfig = dataSpecConfig;
    }

    @Override
    public TestDefBuilder execute() throws TaskExecutionException {

        TestDefBuilder builder = new TestDefBuilder();

        // Preparing App runners
//        ArrayList<String> testDataList = MapUtil.getObjectOr(postParams, "testDataList", new ArrayList());
        for (String dataName : testDataList) {
            DataSpecConfig testConfig;
            if (dataSpecConfig.containsKey(dataName)) {
                testConfig = dataSpecConfig.get(dataName);
            } else {
                testConfig = dataSpecConfig.get(BuildTestPage.DEF_SELECTED_DATA_NAME);
            }
            // QuadUI
            String outputModelStr = "-";
            if (testConfig.isModelSelected("DSSAT45")) {
                outputModelStr += "D";
            }
            if (testConfig.isModelSelected("APSIM75")) {
                outputModelStr += "A";
            }
            if (testConfig.isModelSelected("WOFOST")) {
                outputModelStr += "W";
            }
            if (testConfig.isModelSelected("JSON")) {
                outputModelStr += "J";
            }
            String quaduiWorkDir = Functions.revisePath(testConfig.getOutputDir() + File.separator + dataName);
            QuadUIJarRunner quadui = (QuadUIJarRunner) builder.addAppRunner(QUADUI, quaduiPath, quaduiWorkDir, quaduiWorkDir);
            String quaduiTitle = "QuadUI_" + dataName;
            quadui.setTitle(quaduiTitle);
            quadui.setArguments("-cli -clean -s " + outputModelStr);
            quadui.setRawdataPath(String.format(surveyDataPath, dataName));
            quadui.setLinkPath(" ");
            quadui.setOverlayPath(String.format(fieldDomePath, dataName));
            quadui.setSeasonalPath(String.format(seasonalDomePath, dataName));
            // Models
            for (String model : testConfig.getModels()) {
                // DSSAT45
                if (model.equals("DSSAT45")) {
                    String workPath = String.format(modelWorkDir, quaduiTitle, "DSSAT");
                    ExeRunner dssat = (ExeRunner) builder.addAppRunner(EXE, dssat45ExePath, workPath, workPath); // TODO copy dssat input to somewhere
                    dssat.setTitle(model);
                    dssat.setArguments("b", dssat45BatchFileName);
                    // ACMO and comparator
                    setupAcmoAndComp(builder, dataName, model, testConfig);
                } // APSIM
                else if (model.equals("APSIM75")) {
                    String workPath = String.format(modelWorkDir, quaduiTitle, "APSIM");
                    ApsimRunner apsim = (ApsimRunner) builder.addAppRunner(APSIM, apsim75ExePath, workPath, workPath); // TODO copy dssat input to somewhere
                    apsim.setTitle(model);
                    apsim.setArguments("AgMip.apsim");
                    // ACMO and comparator
                    setupAcmoAndComp(builder, dataName, model, testConfig);
                } // WOFOST
                else if (model.equals("WOFOST")) {
                    // TODO
                }
            }

        }
        return builder;
    }

    private void setupAcmoAndComp(TestDefBuilder builder, String dataName, String model, DataSpecConfig testConfig) {
        // ACMO for Model
        JarRunner acmo = (JarRunner) builder.addAppRunner(JAR, acmouiPath);
        String acmoTitle = "ACMO_" + model;
        acmo.setTitle(acmoTitle);
        String modelArg = "";
        String modelName = "";
        if (model.startsWith("DSSAT")) {
            modelArg = "-dssat";
            modelName = "DSSAT";
        } else if (model.startsWith("APSIM")) {
            modelArg = "-apsim";
            modelName = "APSIM";
        }
        acmo.setArguments("-cli", modelArg, String.format(acmoInputDir, model));
        if (testConfig.isComparatorSelected("ACMO")) {
            // Preparing result comparators
            try {
                String expectedPath = String.format(actualAcmoCsvPath, dataName, model, "ACMO", "ACMO-MACHAKOS-1-0XFX-0-0-" + modelName + ".csv");
                TestComparator comparator = builder.addTestComparator(FILE, expectedPath, String.format(acmoInputDir, model) + "\\ACMO-MACHAKOS-1-0XFX-0-0-" + modelName + ".csv");
                comparator.setTitle("ACMO_" + model + "_Comparator");
                comparator.setOutputDir(""); // TODO
            } catch (Exception ex) {
                LOG.error(Functions.getStackTrace(ex));
            }
        } else if (testConfig.isComparatorSelected("Yield")) {
            // TODO
        }
    }
}
