package org.agmip.ui.cropmarker;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.agmip.common.Functions;
import org.agmip.ui.cropmarker.BuildTestPage.DataSpecConfig;
import static org.agmip.ui.cropmarker.Page.DEF_DATA_PATH;
import org.agmip.utility.testframe.comparator.AcmoCsvFileComparator;
import org.agmip.utility.testframe.comparator.TestComparator;
import static org.agmip.utility.testframe.comparator.TestComparator.Type.*;
import org.agmip.utility.testframe.model.TestDefBuilder;
import static org.agmip.utility.testframe.runner.AppRunner.Type.*;
import org.agmip.utility.testframe.runner.ApsimRunner;
import org.agmip.utility.testframe.runner.DssatRunner;
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
    private final String surveyDataPath = DEF_DATA_PATH + "\\%s\\input\\%s.zip";
    private final String fieldDomePath = DEF_DATA_PATH + "\\%s\\input\\Field_Overlay.zip";
    private final String seasonalDomePath = DEF_DATA_PATH + "\\%s\\input\\Seasonal_strategy.zip";
    private final String linkagePath = DEF_DATA_PATH + "\\%s\\input\\linkage.csv";
    private final String dssat45ExePath = ConfigHelper.getDssat45ExePath();
    private final String modelWorkDir = "*%s.output_dir\\%s";
    private final String dssat45BatchFileName = "dssbatch.v45";
    private final String apsim75ExePath = ConfigHelper.getApsim75ExePath();
    private final String acmoInputDir = "*%s_%s.output_dir";
    private final String expectedDataPath = DEF_DATA_PATH + "\\%s\\expected\\%s\\";
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
            String quaduiTitle = dataName + "_QuadUI";
            quadui.setTitle(quaduiTitle);
            String source = String.format(surveyDataPath, dataName, dataName);
            String linkage = String.format(linkagePath, dataName);
            String fDome = String.format(fieldDomePath, dataName);
            String sDome = String.format(seasonalDomePath, dataName);
            quadui.setRawdataPath(source);
            if (new File(fDome).exists()) {
                quadui.setOverlayPath(fDome);
                if (new File(linkage).exists()) {
                    quadui.setLinkPath(linkage);
                }
                if (new File(sDome).exists()) {
                    quadui.setSeasonalPath(sDome);
                    quadui.setArguments("-cli -clean -s " + outputModelStr);
                } else {
                    quadui.setArguments("-cli -clean -f " + outputModelStr);
                }
            } else {
                quadui.setArguments("-cli -clean -n " + outputModelStr);
            }
            // Models
            for (String model : testConfig.getModels()) {
                // DSSAT45
                if (model.equals("DSSAT45")) {
                    String workPath = String.format(modelWorkDir, quaduiTitle, "DSSAT");
                    String outputPath = String.format(modelWorkDir, quaduiTitle, model);
                    DssatRunner dssat = (DssatRunner) builder.addAppRunner(DSSAT, dssat45ExePath, workPath, outputPath); // TODO copy dssat input to somewhere
                    dssat.setTitle(dataName + "_" + model);
                    dssat.setArguments("b", dssat45BatchFileName);
                    // ACMO and comparator
                    setupAcmoAndComp(builder, dataName, model, testConfig);
                } // APSIM
                else if (model.equals("APSIM75")) {
                    String workPath = String.format(modelWorkDir, quaduiTitle, "APSIM");
                    String outputPath = String.format(modelWorkDir, quaduiTitle, model);
                    ApsimRunner apsim = (ApsimRunner) builder.addAppRunner(APSIM, apsim75ExePath, workPath, outputPath); // TODO copy dssat input to somewhere
                    apsim.setTitle(dataName + "_" + model);
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
        String acmoTitle = dataName + "_" + model + "_ACMOUI";
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
        acmo.setArguments("-cli", modelArg, String.format(acmoInputDir, dataName, model));

        // Comparators
        if (testConfig.getComparators().contains("None")) {
            LOG.info("None of comparator for {} data set will be load based on user configuration.", dataName);
            return;
        }
        for (String comparatorName : testConfig.getComparators()) {
            if (comparatorName.equalsIgnoreCase("ALL")) {
                try {
                    String expectedPath = String.format(expectedDataPath, dataName, model);
                    String actualPath = String.format(acmoInputDir, dataName, model);
                    TestComparator comparator = builder.addTestComparator(FOLDER, expectedPath, actualPath);
                    comparator.setTitle(comparatorName + "_" + dataName + "_" + model + "_Comparator");
                } catch (Exception ex) {
                    LOG.error(Functions.getStackTrace(ex));
                }
            } else if (comparatorName.equalsIgnoreCase("ACMO")) {
                // Preparing ACMO CSV file comparator
                try {
                    String expectedPath = String.format(expectedDataPath, dataName, model);
                    String actualPath = String.format(acmoInputDir, dataName, model);
                    File expDir = new File(expectedPath);
                    File acmoFile = new File(expectedPath + "ACMO-" + modelName + ".csv");
                    for (File f : expDir.listFiles()) {
                        String fname = f.getName().toUpperCase();
                        if (fname.startsWith("ACMO") && fname.endsWith(".CSV")) {
                            acmoFile = f;
                            break;
                        }
                    }
                    AcmoCsvFileComparator comparator = (AcmoCsvFileComparator) builder.addTestComparator(FILE, acmoFile.getAbsolutePath(), actualPath + File.separator + acmoFile.getName());
                    comparator.setCompareAllOutputCols();
                    comparator.setTitle(comparatorName + "_" + dataName + "_" + model + "_Comparator");
                } catch (Exception ex) {
                    LOG.error(Functions.getStackTrace(ex));
                }
            } else if (comparatorName.equalsIgnoreCase("Yield")) {
                // Preparing Yield variable comparator
                try {
                    String expectedPath = String.format(expectedDataPath, dataName, model);
                    String actualPath = String.format(acmoInputDir, dataName, model);
                    File expDir = new File(expectedPath);
                    File acmoFile = new File(expectedPath + "ACMO-" + modelName + ".csv");
                    for (File f : expDir.listFiles()) {
                        String fname = f.getName().toUpperCase();
                        if (fname.startsWith("ACMO") && fname.endsWith(".CSV")) {
                            acmoFile = f;
                            break;
                        }
                    }
                    TestComparator comparator = builder.addTestComparator(FILE, acmoFile.getAbsolutePath(), actualPath + File.separator + acmoFile.getName());
                    ((AcmoCsvFileComparator) comparator).setCompareHeaderNames(new ArrayList(Arrays.asList("HWAH_S")));
                    comparator.setTitle(comparatorName + "_" + dataName + "_" + model + "_Comparator");
                } catch (Exception ex) {
                    LOG.error(Functions.getStackTrace(ex));
                }
            }
        }
    }
}
