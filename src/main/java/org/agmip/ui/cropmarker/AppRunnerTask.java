package org.agmip.ui.cropmarker;

import java.util.ArrayList;
import org.agmip.common.Functions;
import org.agmip.utility.testframe.comparator.TestComparator;
import org.agmip.utility.testframe.model.TestController;
import org.agmip.utility.testframe.model.TestDefBuilder;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.slf4j.LoggerFactory;

/**
 * Test flow runner, which will invoke each AppRunner instance to execute the
 * simulation tasks.
 *
 * @author Meng Zhang
 */
public class AppRunnerTask extends Task<ArrayList<TestComparator>> {

    private final TestDefBuilder builder;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(AppRunnerTask.class);

    public AppRunnerTask(TestDefBuilder builder) {
        this.builder = builder;
    }

    @Override
    public ArrayList<TestComparator> execute() throws TaskExecutionException {
        try {
            TestController c = new TestController();
            c.readDefMap(builder.getDef());
            c.run();
            return c.getComparators();
        } catch (Exception ex) {
            LOG.error(Functions.getStackTrace(ex));
            return null;
        }
    }

}
