// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.deps.serializer.JobsStatisticsParser;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatistics;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for job statistics
 * 100% methods, 100% lines covered
 */
public class JobStatisticsTest
{
    @Mocked
    JobsStatisticsParser mockedJobsStatisticsParser;

    /* Tests_SRS_JOBSTATISTICS_21_001: [The constructor shall throw IllegalArgumentException if the input jobsStatisticsParser is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullJobId()
    {
        //arrange
        final JobsStatisticsParser jobsStatisticsParser = null;

        //act
        JobStatistics jobStatistics = Deencapsulation.newInstance(JobStatistics.class, new Class[] {JobsStatisticsParser.class}, jobsStatisticsParser);
    }

    /* Tests_SRS_JOBSTATISTICS_21_002: [The constructor shall locally store all statistics information in jobsStatisticsParser.] */
    @Test
    public void constructorStoreStatistics()
    {
        //arrange
        final JobsStatisticsParser jobsStatisticsParser = mockedJobsStatisticsParser;

        new NonStrictExpectations()
        {
            {
                mockedJobsStatisticsParser.getDeviceCount();
                result = 1;
                times = 1;
                mockedJobsStatisticsParser.getFailedCount();
                result = 2;
                times = 1;
                mockedJobsStatisticsParser.getSucceededCount();
                result = 3;
                times = 1;
                mockedJobsStatisticsParser.getRunningCount();
                result = 4;
                times = 1;
                mockedJobsStatisticsParser.getPendingCount();
                result = 5;
                times = 1;
            }
        };

        //act
        JobStatistics jobStatistics = Deencapsulation.newInstance(JobStatistics.class, new Class[] {JobsStatisticsParser.class}, jobsStatisticsParser);

        //assert
        assertEquals(1, (int)Deencapsulation.getField(jobStatistics, "deviceCount"));
        assertEquals(2, (int)Deencapsulation.getField(jobStatistics, "failedCount"));
        assertEquals(3, (int)Deencapsulation.getField(jobStatistics, "succeededCount"));
        assertEquals(4, (int)Deencapsulation.getField(jobStatistics, "runningCount"));
        assertEquals(5, (int)Deencapsulation.getField(jobStatistics, "pendingCount"));
    }

    /* Tests_SRS_JOBSTATISTICS_21_003: [The getDeviceCount shall return the stored device count.] */
    /* Tests_SRS_JOBSTATISTICS_21_004: [The getFailedCount shall return the stored failed count.] */
    /* Tests_SRS_JOBSTATISTICS_21_005: [The getSucceededCount shall return the stored succeeded count.] */
    /* Tests_SRS_JOBSTATISTICS_21_006: [The getRunningCount shall return the stored running count.] */
    /* Tests_SRS_JOBSTATISTICS_21_007: [The getPendingCount shall return the stored pending count.] */
    @Test
    public void gettersStatistics()
    {
        //arrange
        final JobsStatisticsParser jobsStatisticsParser = mockedJobsStatisticsParser;

        new NonStrictExpectations()
        {
            {
                mockedJobsStatisticsParser.getDeviceCount();
                result = 1;
                mockedJobsStatisticsParser.getFailedCount();
                result = 2;
                mockedJobsStatisticsParser.getSucceededCount();
                result = 3;
                mockedJobsStatisticsParser.getRunningCount();
                result = 4;
                mockedJobsStatisticsParser.getPendingCount();
                result = 5;
            }
        };

        //act
        JobStatistics jobStatistics = Deencapsulation.newInstance(JobStatistics.class, new Class[] {JobsStatisticsParser.class}, jobsStatisticsParser);

        //assert
        assertEquals(1, jobStatistics.getDeviceCount());
        assertEquals(2, jobStatistics.getFailedCount());
        assertEquals(3, jobStatistics.getSucceededCount());
        assertEquals(4, jobStatistics.getRunningCount());
        assertEquals(5, jobStatistics.getPendingCount());
    }

}
