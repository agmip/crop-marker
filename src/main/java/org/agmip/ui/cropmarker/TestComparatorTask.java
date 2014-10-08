package org.agmip.ui.cropmarker;

import java.util.ArrayList;
import org.agmip.common.Functions;
import org.agmip.utility.testframe.comparator.TestComparator;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.slf4j.LoggerFactory;

/**
 * Test comparator runner, which will invode each test comparator and get the
 * comparison result.
 *
 * @author Meng Zhang
 */
public class TestComparatorTask extends Task<Boolean> {

    private final ArrayList<TestComparator> comparators;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestComparatorTask.class);

    public TestComparatorTask(ArrayList<TestComparator> comparators) {
        this.comparators = comparators;
    }

    @Override
    public Boolean execute() throws TaskExecutionException {
        try {
            boolean ret = true;
            for (TestComparator comparator : comparators) {
                if (!comparator.getLastCompareResult()) {
                    ret = false;
                    break;
                }
            }
            return ret;
        } catch (Exception ex) {
            LOG.error(Functions.getStackTrace(ex));
            return false;
        }
    }

}
