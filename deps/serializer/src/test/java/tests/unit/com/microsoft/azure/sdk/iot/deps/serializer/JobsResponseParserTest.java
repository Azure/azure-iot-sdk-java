// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsResponseParser;
import mockit.Deencapsulation;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/*
 * Unit tests for jobs response deserializer
 * 100% methods, 100% lines covered
 */
public class JobsResponseParserTest
{
    /* Tests_SRS_JOBSRESPONSEPARSER_21_001: [The createFromJson shall create a new instance of JobsResponseParser class.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_002: [The createFromJson shall parse the provided string for JobsResponseParser class.] */
    @Test
    public void constructorSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"queryCondition\":\"DeviceId IN ['new_device']\",\n" +
                "    \"failureReason\":\"Valid failure reason\",\n" +
                "    \"statusMessage\":\"Valid status message\",\n" +
                "    \"deviceId\":\"ValidDeviceId\",\n" +
                "    \"parentJobId\":\"ValidParentJobId\",\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertEquals("jobName", Deencapsulation.getField(jobsResponseParser, "jobId"));
        assertEquals("DeviceId IN ['new_device']", Deencapsulation.getField(jobsResponseParser, "queryCondition"));
        assertEquals(120L, Deencapsulation.getField(jobsResponseParser, "maxExecutionTimeInSeconds"));
        assertEquals("scheduleUpdateTwin", Deencapsulation.getField(jobsResponseParser, "jobType").toString());
        assertEquals("enqueued", Deencapsulation.getField(jobsResponseParser, "jobsStatus").toString());
        assertEquals("Valid failure reason", Deencapsulation.getField(jobsResponseParser, "failureReason"));
        assertEquals("Valid status message", Deencapsulation.getField(jobsResponseParser, "statusMessage"));
        assertEquals("ValidDeviceId", Deencapsulation.getField(jobsResponseParser, "deviceId"));
        assertEquals("ValidParentJobId", Deencapsulation.getField(jobsResponseParser, "parentJobId"));
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_001: [The createFromJson shall create a new instance of JobsResponseParser class.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_002: [The createFromJson shall parse the provided string for JobsResponseParser class.] */
    @Test
    public void constructorMinJsonSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertEquals("jobName", Deencapsulation.getField(jobsResponseParser, "jobId"));
        assertEquals("scheduleUpdateTwin", Deencapsulation.getField(jobsResponseParser, "jobType").toString());
        assertEquals("enqueued", Deencapsulation.getField(jobsResponseParser, "jobsStatus").toString());
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_003: [If the json contains `updateTwin`, the createFromJson shall parse the content of it for TwinParser class.] */
    @Test
    public void constructorTwinSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"updateTwin\":\n" +
                "    {\n" +
                "        \"deviceId\":\"new_device\",\n" +
                "        \"etag\":null,\n" +
                "        \"tags\":{\"Tag1\":100},\n" +
                "        \"properties\":{\"desired\":{},\"reported\":{}}\n" +
                "    },\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "updateTwin"));
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_004: [If the json contains `cloudToDeviceMethod`, the createFromJson shall parse the content of it for MethodParser class.] */
    @Test
    public void constructorMethodSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleDeviceMethod\",\n" +
                "    \"cloudToDeviceMethod\":\n" +
                "    {\n" +
                "        \"methodName\":\"reboot\",\n" +
                "        \"responseTimeoutInSeconds\":200,\n" +
                "        \"connectTimeoutInSeconds\":5,\n" +
                "        \"payload\":{\"Tag1\":100}\n" +
                "    },\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "cloudToDeviceMethod"));
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_005: [If the json contains `deviceJobStatistics`, the createFromJson shall parse the content of it for JobsStatisticsParser class.] */
    @Test
    public void constructorStatisticsSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"deviceJobStatistics\": {\n" +
                "        \"deviceCount\": 1,\n" +
                "        \"failedCount\": 2,\n" +
                "        \"succeededCount\": 3,\n" +
                "        \"runningCount\": 4,\n" +
                "        \"pendingCount\": 5\n" +
                "    },\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "deviceJobStatistics"));
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_006: [If the json is null or empty, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorJsonNullThrows() throws ParseException
    {
        // arrange
        String json = null;

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_006: [If the json is null or empty, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorJsonEmptyThrows() throws ParseException
    {
        // arrange
        String json = "";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_007: [If the json is not valid, the createFromJson shall throws JsonParseException.] */
    @Test (expected = JsonParseException.class)
    public void constructorInvalidJsonThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_008: [If the json do not contains `jobId`, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNoJobIdThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_008: [If the json do not contains `jobId`, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullJobIdThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":null,\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_008: [If the json do not contains `jobId`, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorEmptyJobIdThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_009: [If the json do not contains `type`, or the `type` is invalid, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNoTypeThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_009: [If the json do not contains `type`, or the `type` is invalid, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullTypeThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":null\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_009: [If the json do not contains `type`, or the `type` is invalid, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorEmptyTypeThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_010: [If the json do not contains `status`, or the `status` is invalid, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNoStatusThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_010: [If the json do not contains `status`, or the `status` is invalid, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullStatusThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":null,\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_010: [If the json do not contains `status`, or the `status` is invalid, the createFromJson shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorEmptyStatusThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"\",\n" +
                "    \"type\":\"scheduleUpdateTwin\"\n" +
                "}";

        // act
        JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_011: [If the json contains any of the dates `createdTime`, `startTime`, or `endTime`, the createFromJson shall parser it as ISO_8601.] */
    @Test
    public void constructorDateSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "createdTimeDate"),"2017-06-21T10:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "startTimeDate"),"2017-06-21T16:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "endTimeDate"),"2017-06-21T20:47:33.798692Z");
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
    @Test
    public void constructorInvalidCreateTimeSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"createdTime\":\"InvalidDateString\",\n" +
                "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNull(Deencapsulation.getField(jobsResponseParser, "createdTimeDate"));
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "startTimeDate"),"2017-06-21T16:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "endTimeDate"),"2017-06-21T20:47:33.798692Z");
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
    @Test
    public void constructorEmptyCreateTimeSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"createdTime\":\"\",\n" +
                "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNull(Deencapsulation.getField(jobsResponseParser, "createdTimeDate"));
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "startTimeDate"),"2017-06-21T16:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "endTimeDate"),"2017-06-21T20:47:33.798692Z");
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
    @Test
    public void constructorEmptyStartTimeSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                "    \"startTime\":\"invalidDate\",\n" +
                "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "createdTimeDate"),"2017-06-21T10:47:33.798692Z");
        assertNull(Deencapsulation.getField(jobsResponseParser, "startTimeDate"));
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "endTimeDate"),"2017-06-21T20:47:33.798692Z");
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
    @Test
    public void constructorEmptyEndTimeSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                "    \"endTime\":\"invalidDate\",\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "createdTimeDate"),"2017-06-21T10:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "startTimeDate"),"2017-06-21T16:47:33.798692Z");
        assertNull(Deencapsulation.getField(jobsResponseParser, "endTimeDate"));
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_013: [The getJobId shall return the jobId value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_014: [The getQueryCondition shall return the queryCondition value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_015: [The getCreateTime shall return the createTime value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_016: [The getStartTime shall return the startTime value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_017: [The getEndTime shall return the endTime value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_018: [The getMaxExecutionTimeInSeconds shall return the maxExecutionTimeInSeconds value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_019: [The getJobType shall return a String with the job type value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_020: [The getJobsStatus shall return a String with the job status value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_022: [The getUpdateTwin shall return the updateTwin value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_023: [The getFailureReason shall return the failureReason value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_024: [The getStatusMessage shall return the statusMessage value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_025: [The getJobStatistics shall return the jobStatistics value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_026: [The getDeviceId shall return the deviceId value.] */
    /* Tests_SRS_JOBSRESPONSEPARSER_21_027: [The getParentJobId shall return the parentJobId value.] */
    @Test
    public void gettersSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleUpdateTwin\",\n" +
                "    \"updateTwin\":\n" +
                "    {\n" +
                "        \"deviceId\":\"new_device\",\n" +
                "        \"etag\":null,\n" +
                "        \"tags\":{\"Tag1\":100},\n" +
                "        \"properties\":{\"desired\":{},\"reported\":{}}\n" +
                "    },\n" +
                "    \"queryCondition\":\"DeviceId IN ['new_device']\",\n" +
                "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                "    \"failureReason\":\"Valid failure reason\",\n" +
                "    \"statusMessage\":\"Valid status message\",\n" +
                "    \"deviceId\":\"ValidDeviceId\",\n" +
                "    \"parentJobId\":\"ValidParentJobId\",\n" +
                "    \"deviceJobStatistics\": {\n" +
                "        \"deviceCount\": 1,\n" +
                "        \"failedCount\": 2,\n" +
                "        \"succeededCount\": 3,\n" +
                "        \"runningCount\": 4,\n" +
                "        \"pendingCount\": 5\n" +
                "    },\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // act
        // assert
        assertEquals("jobName", jobsResponseParser.getJobId());
        assertEquals("DeviceId IN ['new_device']", jobsResponseParser.getQueryCondition());
        Helpers.assertDateWithError(jobsResponseParser.getCreatedTime(),"2017-06-21T10:47:33.798692Z");
        Helpers.assertDateWithError(jobsResponseParser.getStartTime(),"2017-06-21T16:47:33.798692Z");
        Helpers.assertDateWithError(jobsResponseParser.getEndTime(),"2017-06-21T20:47:33.798692Z");
        assertEquals(120L, (long)jobsResponseParser.getMaxExecutionTimeInSeconds());
        assertEquals("scheduleUpdateTwin", jobsResponseParser.getJobType());
        assertEquals("enqueued", jobsResponseParser.getJobsStatus());
        assertNull(jobsResponseParser.getCloudToDeviceMethod());
        assertNotNull(jobsResponseParser.getUpdateTwin());
        assertNotNull(jobsResponseParser.getJobStatistics());
        assertEquals("Valid failure reason", jobsResponseParser.getFailureReason());
        assertEquals("Valid status message", jobsResponseParser.getStatusMessage());
        assertEquals("ValidDeviceId", jobsResponseParser.getDeviceId());
        assertEquals("ValidParentJobId", jobsResponseParser.getParentJobId());
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_021: [The getCloudToDeviceMethod shall return the cloudToDeviceMethod value.] */
    @Test
    public void gettersMethodSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                "    \"jobId\":\"jobName\",\n" +
                "    \"status\":\"enqueued\",\n" +
                "    \"type\":\"scheduleDeviceMethod\",\n" +
                "    \"cloudToDeviceMethod\":\n" +
                "    {\n" +
                "        \"methodName\":\"reboot\",\n" +
                "        \"responseTimeoutInSeconds\":200,\n" +
                "        \"connectTimeoutInSeconds\":5,\n" +
                "        \"payload\":{\"Tag1\":100}\n" +
                "    },\n" +
                "    \"maxExecutionTimeInSeconds\":120\n" +
                "}";
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // act
        // assert
        assertNotNull(jobsResponseParser.getCloudToDeviceMethod());
    }
}
