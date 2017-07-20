// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsStatisticsParser;
import org.junit.Test;

import java.text.ParseException;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for jobs statistics deserializer
 * 100% methods, 100% lines covered
 */
public class JobsStatisticsParserTest
{
    /* Tests_SRS_JOBSSTATISTICSPARSER_21_001: [The getDeviceCount shall return the value of the deviceCount counter.] */
    /* Tests_SRS_JOBSSTATISTICSPARSER_21_002: [The getFailedCount shall return the value of the failedCount counter.] */
    /* Tests_SRS_JOBSSTATISTICSPARSER_21_003: [The getSucceededCount shall return the value of the succeededCount counter.] */
    /* Tests_SRS_JOBSSTATISTICSPARSER_21_004: [The getRunningCount shall return the value of the runningCount counter.] */
    /* Tests_SRS_JOBSSTATISTICSPARSER_21_005: [The getPendingCount shall return the value of the pendingCount counter.] */
    @Test
    public void gettersSucceed() throws ParseException
    {
        // arrange
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        String json =
                "{\n" +
                "        \"deviceCount\": 1,\n" +
                "        \"failedCount\": 2,\n" +
                "        \"succeededCount\": 3,\n" +
                "        \"runningCount\": 4,\n" +
                "        \"pendingCount\": 5\n" +
                "}";
        JobsStatisticsParser jobsStatisticsParser= gson.fromJson(json, JobsStatisticsParser.class);

        // act
        // assert
        assertEquals(1, jobsStatisticsParser.getDeviceCount());
        assertEquals(2, jobsStatisticsParser.getFailedCount());
        assertEquals(3, jobsStatisticsParser.getSucceededCount());
        assertEquals(4, jobsStatisticsParser.getRunningCount());
        assertEquals(5, jobsStatisticsParser.getPendingCount());
    }
}
