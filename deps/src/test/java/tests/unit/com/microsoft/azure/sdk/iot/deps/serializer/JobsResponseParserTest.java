// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.deps.serializer.JobQueryResponseError;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import mockit.Deencapsulation;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.deps.Helpers;

import java.text.ParseException;
import java.util.Date;

import static org.junit.Assert.*;

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
        assertEquals("scheduleUpdateTwin", Deencapsulation.getField(jobsResponseParser, "type").toString());
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
        assertEquals("scheduleUpdateTwin", Deencapsulation.getField(jobsResponseParser, "type").toString());
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

    @Test
    public void constructorQueryResponseTwinSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\"deviceId\":\"validDeviceID\"," +
                        "\"jobId\":\"DHCMD302dbaae-cec5-4755-b529-1ca48d17dabc\"," +
                        "\"jobType\":\"scheduleUpdateTwin\"," +
                        "\"status\":\"completed\"," +
                        "\"startTimeUtc\":\"2017-07-11T23:55:12.0052Z\"," +
                        "\"endTimeUtc\":\"2017-07-11T23:56:52.0052Z\"," +
                        "\"createdDateTimeUtc\":\"2017-07-11T23:55:13.4955445Z\"," +
                        "\"lastUpdatedDateTimeUtc\":\"2017-07-11T23:55:13.5267866Z\"," +
                        "\"outcome\":{}}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNull(Deencapsulation.getField(jobsResponseParser, "cloudToDeviceMethod"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "startTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "endTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "createdTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "lastUpdatedTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "outcome"));
        MethodParser methodParser = Deencapsulation.getField(jobsResponseParser, "methodResponse");
        assertNotNull(methodParser);

        try
        {
            methodParser.getStatus();
            methodParser.getPayload();
            assertTrue("Expected an exception as outcome does not apply to twin", true);
        }
        catch (IllegalArgumentException e)
        {
            // Do nothing as this is expected exception
        }
    }

    //Tests_SRS_JOBSRESPONSEPARSER_25_033: [The getOutcome shall return the outcome value.]
    //Tests_SRS_JOBSRESPONSEPARSER_25_031: [The getLastUpdatedTimeDate shall return the LastUpdatedTimeUTCDate value.]
    @Test
    public void constructorQueryResponseMethodSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\"deviceId\":\"validDeviceID\"," +
                        "\"jobId\":\"DHCMD7605b0fb-dff4-4368-aa00-9be5c563cfcd\"," +
                        "\"jobType\":\"scheduleDeviceMethod\"," +
                        "\"status\":\"completed\"," +
                        "\"startTimeUtc\":\"2017-07-11T23:55:12.0052Z\"," +
                        "\"endTimeUtc\":\"2017-07-11T23:56:52.0052Z\"," +
                        "\"createdDateTimeUtc\":\"2017-07-11T23:55:14.0580263Z\"," +
                        "\"lastUpdatedDateTimeUtc\":\"2017-07-11T23:55:14.198687Z\"," +
                        "\"outcome\":{\"deviceMethodResponse\":{\"status\":404,\"payload\":\"executed reboot\"}}}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert

        assertNotNull(Deencapsulation.getField(jobsResponseParser, "startTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "endTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "createdTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "lastUpdatedTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "outcome"));
        MethodParser methodParser = Deencapsulation.getField(jobsResponseParser, "methodResponse");
        assertNotNull(methodParser.getStatus());
        assertNotNull(methodParser.getPayload());
    }

    //Tests_SRS_JOBSRESPONSEPARSER_25_032: [The getError shall return the error value.]
    @Test
    public void constructorQueryResponseErrorSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\"deviceId\":\"validDeviceID\"," +
                        "\"jobId\":\"DHCMD798400e8-6d6c-44a5-b0ec-a790ad9705de\"," +
                        "\"jobType\":\"scheduleDeviceMethod\"," +
                        "\"status\":\"failed\"," +
                        "\"startTimeUtc\":\"2017-07-08T00:02:25.0556Z\"," +
                        "\"endTimeUtc\":\"2017-07-08T00:04:05.0556Z\"," +
                        "\"createdDateTimeUtc\":\"2017-07-08T00:02:31.6162976Z\"," +
                        "\"lastUpdatedDateTimeUtc\":\"2017-07-08T00:04:05.0736166Z\"," +
                        "\"outcome\":{}," +
                        "\"error\":{" +
                            "\"code\":\"JobRunPreconditionFailed\"," +
                        "   \"description\":\"The job did not start within specified period: either device did not come online or invalid endTime specified.\"" +
                            "}" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNull(Deencapsulation.getField(jobsResponseParser, "cloudToDeviceMethod"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "startTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "endTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "createdTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "lastUpdatedTimeDate"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "outcome"));
        assertNotNull(Deencapsulation.getField(jobsResponseParser, "error"));
        JobQueryResponseError jobQueryResponseError = Deencapsulation.getField(jobsResponseParser, "error");
        assertNotNull(jobQueryResponseError.toJson());
        assertNotNull(jobQueryResponseError.getCode());
        assertNotNull(jobQueryResponseError.getDescription());
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
    public void constructorEmptyCreateTimeUTCSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdDateTimeUtc\":\"\",\n" +
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

    @Test
    public void constructorEmptyLastUpdatedTimeUTCSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdDateTimeUtc\":\"2017-06-21T16:47:33.798692Z\",\n" +
                        "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                        "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                        "    \"lastUpdatedDateTimeUtc\":\"\",\n" +
                        "    \"maxExecutionTimeInSeconds\":120\n" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        assertNull(Deencapsulation.getField(jobsResponseParser, "lastUpdatedTimeDate"));
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "createdTimeDate"),"2017-06-21T16:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "startTimeDate"),"2017-06-21T16:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "endTimeDate"),"2017-06-21T20:47:33.798692Z");
    }

    //Tests_SRS_JOBSRESPONSEPARSER_25_034: [If the json contains both of the dates createdTime and createdDateTimeUtc or startTime and startTimeUtc or endTime and endTimeUtc, the createFromJson shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorBothCreateTimeAndUTCInJsonThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"createdDateTimeUtc\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTimeUtc\":\"invalidDate\",\n" +
                        "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                        "    \"maxExecutionTimeInSeconds\":120\n" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorBothStartTimeAndUTCInJsonThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTimeUtc\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                        "    \"maxExecutionTimeInSeconds\":120\n" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorBothEndTimeAndUTCInJsonThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                        "    \"endTimeUtc\":\"2017-06-21T20:47:33.798692Z\",\n" +
                        "    \"maxExecutionTimeInSeconds\":120\n" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorBothTypeAndJobTypeInJsonThrows() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"jobType\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"endTime\":\"2017-06-21T20:47:33.798692Z\",\n" +
                        "    \"maxExecutionTimeInSeconds\":120\n" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);
    }

    /* Tests_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
    @Test
    public void constructorEmptyStartTimeUTCSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTimeUtc\":\"invalidDate\",\n" +
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
    public void constructorEmptyEndTimeUTCSucceed() throws ParseException
    {
        // arrange
        String json =
                "{\n" +
                        "    \"jobId\":\"jobName\",\n" +
                        "    \"status\":\"enqueued\",\n" +
                        "    \"type\":\"scheduleUpdateTwin\",\n" +
                        "    \"createdTime\":\"2017-06-21T10:47:33.798692Z\",\n" +
                        "    \"startTime\":\"2017-06-21T16:47:33.798692Z\",\n" +
                        "    \"endTimeUtc\":\"invalidDate\",\n" +
                        "    \"maxExecutionTimeInSeconds\":120\n" +
                        "}";

        // act
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "createdTimeDate"),"2017-06-21T10:47:33.798692Z");
        Helpers.assertDateWithError((Date)Deencapsulation.getField(jobsResponseParser, "startTimeDate"),"2017-06-21T16:47:33.798692Z");
        assertNull(Deencapsulation.getField(jobsResponseParser, "endTimeDate"));
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
    /* Tests_SRS_JOBSRESPONSEPARSER_21_019: [The getType shall return a String with the job type value.] */
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
        assertEquals("scheduleUpdateTwin", jobsResponseParser.getType());
        assertEquals("enqueued", jobsResponseParser.getJobsStatus());
        assertNull(jobsResponseParser.getCloudToDeviceMethod());
        assertNotNull(jobsResponseParser.getUpdateTwin());
        assertNotNull(jobsResponseParser.getJobStatistics());
        assertEquals("Valid failure reason", jobsResponseParser.getFailureReason());
        assertEquals("Valid status message", jobsResponseParser.getStatusMessage());
        assertEquals("ValidDeviceId", jobsResponseParser.getDeviceId());
        assertEquals("ValidParentJobId", jobsResponseParser.getParentJobId());
    }

    @Test
    public void gettersSucceedQueryResponse() throws ParseException
    {
        // arrange
        String json =
                "{\"deviceId\":\"validDeviceID\"," +
                        "\"jobId\":\"jobName\"," +
                        "\"jobType\":\"scheduleDeviceMethod\"," +
                        "\"status\":\"completed\"," +
                        "\"startTimeUtc\":\"2017-07-11T23:55:12.0052Z\"," +
                        "\"endTimeUtc\":\"2017-07-11T23:56:52.0052Z\"," +
                        "\"createdDateTimeUtc\":\"2017-07-11T23:55:14.0580263Z\"," +
                        "\"lastUpdatedDateTimeUtc\":\"2017-07-11T23:55:14.198687Z\"," +
                        "\"outcome\":{\"deviceMethodResponse\":{\"status\":404,\"payload\":\"executed reboot\"}}}";
        ;
        JobsResponseParser jobsResponseParser = JobsResponseParser.createFromJson(json);

        // act
        // assert
        assertEquals("validDeviceID", jobsResponseParser.getDeviceId());
        assertEquals("jobName", jobsResponseParser.getJobId());
        Helpers.assertDateWithError(jobsResponseParser.getCreatedTime(),"2017-07-11T23:55:14.0580263Z");
        Helpers.assertDateWithError(jobsResponseParser.getStartTime(),"2017-07-11T23:55:12.0052Z");
        Helpers.assertDateWithError(jobsResponseParser.getEndTime(),"2017-07-11T23:56:52.0052Z");
        Helpers.assertDateWithError(jobsResponseParser.getLastUpdatedTimeDate(), "2017-07-11T23:55:14.198687Z");
        assertEquals("scheduleDeviceMethod", jobsResponseParser.getType());
        assertEquals("completed", jobsResponseParser.getJobsStatus());
        assertNotNull(jobsResponseParser.getOutcome());
        assertNull(jobsResponseParser.getCloudToDeviceMethod());
        assertNull(jobsResponseParser.getUpdateTwin());
        assertNull(jobsResponseParser.getJobStatistics());
        assertNull(jobsResponseParser.getError());
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
