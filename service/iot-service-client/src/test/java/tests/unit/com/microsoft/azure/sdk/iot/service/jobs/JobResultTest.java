// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.jobs;

import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsStatisticsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatistics;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit test for job statistics
 * 100% methods, 100% lines covered
 */
public class JobResultTest
{
    @Mocked
    JobsResponseParser mockedJobsResponseParser;

    @Mocked
    JobsStatisticsParser mockedJobsStatisticsParser;

    @Mocked
    JobStatistics mockedJobStatistics;

    final static String JOB_ID = "validJobId";
    final static String QUERY_CONDITION = "DeviceId IN ['validDevice']";
    final static long MAX_EXECUTION_TIME_IN_SECONDS = 100L;
    final static String FAILURE_REASON = "This is a valid failure reason";
    final static String STATUS_MESSAGE = "This is a valid status message";
    final static String DEVICE_ID = "validDeviceId";
    final static String PARENT_JOB_ID = "validParentJobId";
    final static String ETAG = "validETag";
    final static String DATEFORMAT_JSON = "MMM d, yyyy h:mm:ss a";


    private void JobsResponseParserExpectations(String json, TwinParser twinParser, MethodParser methodParser, Date date, MethodParser methodParserResponse)
    {
        new NonStrictExpectations()
        {
            {
                JobsResponseParser.createFromJson(json);
                result = mockedJobsResponseParser;

                mockedJobsResponseParser.getJobId();
                result = JOB_ID;
                mockedJobsResponseParser.getQueryCondition();
                result = QUERY_CONDITION;
                mockedJobsResponseParser.getCreatedTime();
                result = date;
                mockedJobsResponseParser.getStartTime();
                result = date;
                mockedJobsResponseParser.getLastUpdatedTimeDate();
                result = date;
                mockedJobsResponseParser.getEndTime();
                result = date;
                mockedJobsResponseParser.getMaxExecutionTimeInSeconds();
                result = MAX_EXECUTION_TIME_IN_SECONDS;
                mockedJobsResponseParser.getType();
                result = "scheduleUpdateTwin";
                mockedJobsResponseParser.getJobsStatus();
                result = "enqueued";
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = methodParser;
                mockedJobsResponseParser.getOutcome();
                result = methodParserResponse;
                mockedJobsResponseParser.getUpdateTwin();
                result = twinParser;
                mockedJobsResponseParser.getFailureReason();
                result = FAILURE_REASON;
                mockedJobsResponseParser.getStatusMessage();
                result = STATUS_MESSAGE;
                mockedJobsResponseParser.getJobStatistics();
                result = mockedJobsStatisticsParser;
                mockedJobsResponseParser.getDeviceId();
                result = DEVICE_ID;
                mockedJobsResponseParser.getParentJobId();
                result = PARENT_JOB_ID;

                Deencapsulation.newInstance(JobStatistics.class, mockedJobsStatisticsParser);
                result = mockedJobStatistics;
            }
        };
    }
    
    /* Tests_SRS_JOBRESULT_21_001: [The constructor shall throw IllegalArgumentException if the input body is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullJson()
    {
        //arrange
        final byte[] resultBytes = null;

        //act
        Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, resultBytes);
    }

    /* Tests_SRS_JOBRESULT_21_002: [The constructor shall parse the body using the JobsResponseParser.] */
    @Test
    public void constructorParseJson() throws IOException
    {
        //arrange
        final String json = "validJson";

        Map<String, Object> tags = new HashMap<>();
        tags.put("tag1", "val1");

        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        twinParser.setDeviceId(DEVICE_ID);
        twinParser.setETag(ETAG);
        twinParser.resetTags(tags);

        JobsResponseParserExpectations(json, twinParser, null, new Date(), null);

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //assert
        new Verifications()
        {
            {
                JobsResponseParser.createFromJson(json);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBRESULT_21_003: [The constructor shall throw JsonParseException if the input body contains a invalid json.] */
    @Test (expected = JsonParseException.class)
    public void constructorThrowsOnInvalidJson()
    {
        //arrange
        final String json = "{invalidJson:";

        new NonStrictExpectations()
        {
            {
                JobsResponseParser.createFromJson(json);
                result = new JsonParseException("");
            }
        };

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());
    }

    /* Tests_SRS_JOBRESULT_21_004: [The constructor shall locally store all results information in the provided body.] */
    @Test
    public void constructorStoreJsonContent() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        Map<String, Object> tags = new HashMap<>();
        tags.put("tag1", "val1");

        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        twinParser.setDeviceId(DEVICE_ID);
        twinParser.setETag(ETAG);
        twinParser.resetTags(tags);

        JobsResponseParserExpectations(json, twinParser, null, now, null);

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //assert
        assertEquals(JOB_ID, Deencapsulation.getField(jobResult, "jobId"));
        assertEquals(QUERY_CONDITION, Deencapsulation.getField(jobResult, "queryCondition"));
        assertEquals(now, Deencapsulation.getField(jobResult, "createdTime"));
        assertEquals(now, Deencapsulation.getField(jobResult, "startTime"));
        assertEquals(now, Deencapsulation.getField(jobResult, "endTime"));
        assertEquals(MAX_EXECUTION_TIME_IN_SECONDS, (long)Deencapsulation.getField(jobResult, "maxExecutionTimeInSeconds"));
        assertEquals(JobType.scheduleUpdateTwin, Deencapsulation.getField(jobResult, "jobType"));
        assertEquals(JobStatus.enqueued, Deencapsulation.getField(jobResult, "jobStatus"));
        assertNull(Deencapsulation.getField(jobResult, "cloudToDeviceMethod"));
        assertNotNull(Deencapsulation.getField(jobResult, "updateTwin"));
        assertEquals(FAILURE_REASON, Deencapsulation.getField(jobResult, "failureReason"));
        assertEquals(STATUS_MESSAGE, Deencapsulation.getField(jobResult, "statusMessage"));
        assertNotNull(Deencapsulation.getField(jobResult, "jobStatistics"));
        assertEquals(DEVICE_ID, Deencapsulation.getField(jobResult, "deviceId"));
        assertEquals(PARENT_JOB_ID, Deencapsulation.getField(jobResult, "parentJobId"));
    }

    /* Tests_SRS_JOBRESULT_21_004: [The constructor shall locally store all results information in the provided body.] */
    @Test
    public void constructorStoreJsonContentNoTags() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        Map<String, Object> desired = new HashMap<>();
        desired.put("prop1", "val1");

        TwinParser twinParser = new TwinParser();
        twinParser.setDeviceId(DEVICE_ID);
        twinParser.setETag(ETAG);
        twinParser.resetDesiredProperty(desired);

        JobsResponseParserExpectations(json, twinParser, null, now, null);

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //assert
        assertNotNull(Deencapsulation.getField(jobResult, "updateTwin"));
    }

    /* Tests_SRS_JOBRESULT_21_005: [The getJobId shall return the stored jobId.] */
    /* Tests_SRS_JOBRESULT_21_006: [The getQueryCondition shall return the stored queryCondition.] */
    /* Tests_SRS_JOBRESULT_21_007: [The getCreatedTime shall return the stored createdTime.] */
    /* Tests_SRS_JOBRESULT_21_008: [The getStartTime shall return the stored startTime.] */
    /* Tests_SRS_JOBRESULT_21_009: [The getEndTime shall return the stored endTime.] */
    /* Tests_SRS_JOBRESULT_21_010: [The getMaxExecutionTimeInSeconds shall return the stored maxExecutionTimeInSeconds.] */
    /* Tests_SRS_JOBRESULT_21_011: [The getType shall return the stored jobType.] */
    /* Tests_SRS_JOBRESULT_21_012: [The getJobStatus shall return the stored jobStatus.] */
    /* Tests_SRS_JOBRESULT_21_014: [The getUpdateTwin shall return the stored updateTwin.] */
    /* Tests_SRS_JOBRESULT_21_015: [The getFailureReason shall return the stored failureReason.] */
    /* Tests_SRS_JOBRESULT_21_016: [The getStatusMessage shall return the stored statusMessage.] */
    /* Tests_SRS_JOBRESULT_21_017: [The getJobStatistics shall return the stored jobStatistics.] */
    /* Tests_SRS_JOBRESULT_21_018: [The getDeviceId shall return the stored deviceId.] */
    /* Tests_SRS_JOBRESULT_21_019: [The getParentJobId shall return the stored parentJobId.] */
    @Test
    public void gettersTwinContent() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        Map<String, Object> tags = new HashMap<>();
        tags.put("tag1", "val1");

        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        twinParser.setDeviceId(DEVICE_ID);
        twinParser.setETag(ETAG);
        twinParser.resetTags(tags);

        JobsResponseParserExpectations(json, twinParser, null, now, null);

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //assert
        assertEquals(JOB_ID, jobResult.getJobId());
        assertEquals(QUERY_CONDITION, jobResult.getQueryCondition());
        assertEquals(now, jobResult.getCreatedTime());
        assertEquals(now, jobResult.getStartTime());
        assertEquals(now, jobResult.getEndTime());
        assertEquals(MAX_EXECUTION_TIME_IN_SECONDS, (long)jobResult.getMaxExecutionTimeInSeconds());
        assertEquals(JobType.scheduleUpdateTwin, jobResult.getJobType());
        assertEquals(JobStatus.enqueued, jobResult.getJobStatus());
        assertNull(jobResult.getCloudToDeviceMethod());
        assertNotNull(jobResult.getUpdateTwin());
        assertEquals(FAILURE_REASON, jobResult.getFailureReason());
        assertEquals(STATUS_MESSAGE, jobResult.getStatusMessage());
        assertNotNull(jobResult.getJobStatistics());
        assertEquals(DEVICE_ID, jobResult.getDeviceId());
        assertEquals(PARENT_JOB_ID, jobResult.getParentJobId());
    }

    /* Tests_SRS_JOBRESULT_21_013: [The getCloudToDeviceMethod shall return the stored cloudToDeviceMethod.] */
    /* Tests_SRS_JOBRESULT_25_020: [The getLastUpdatedDateTime shall return the stored LastUpdatedDateTime.] */
    /* Tests_SRS_JOBRESULT_25_021: [The getOutcomeResult shall return the stored outcome.] */
    /* Tests_SRS_JOBRESULT_25_022: [The getError shall return the stored error message.] */
    @Test
    public void gettersMethodContent(@Mocked MethodParser mockedMethodParser) throws IOException
    {
        //arrange
        final String json = "validJson";
        final String methodReturnPayload = "validResult";
        final int methodReturnStatus = 200;
        final Date now = new Date();

        MethodParser methodParser = new MethodParser("methodName", null, null, new HashMap<String, Object>());
        MethodParser methodParserResponse = mockedMethodParser;

        JobsResponseParserExpectations(json, null, methodParser, now, methodParserResponse);
        new NonStrictExpectations()
        {
            {
                JobsResponseParser.createFromJson(json);
                result = mockedJobsResponseParser;

                mockedJobsResponseParser.getJobId();
                result = JOB_ID;
                mockedJobsResponseParser.getType();
                result = "scheduleDeviceMethod";
                mockedJobsResponseParser.getJobsStatus();
                result = "completed";
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = methodParser;
                mockedJobsResponseParser.getOutcome();
                result = methodParserResponse;
                methodParserResponse.toJson();
                result = json;
                methodParserResponse.getStatus();
                result = methodReturnStatus;
                methodParserResponse.getPayload();
                result = methodReturnPayload;

                Deencapsulation.newInstance(JobStatistics.class, mockedJobsStatisticsParser);
                result = mockedJobStatistics;
            }
        };

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //assert
        assertEquals(JOB_ID, jobResult.getJobId());
        assertEquals(JobType.scheduleDeviceMethod, jobResult.getJobType());
        assertEquals(JobStatus.completed, jobResult.getJobStatus());
        assertNotNull(jobResult.getCloudToDeviceMethod());
        assertNotNull(jobResult.getOutcome());
        MethodResult methodResult = jobResult.getOutcomeResult();
        assertEquals(methodReturnStatus, (long)methodResult.getStatus());
        assertEquals(methodReturnPayload, methodResult.getPayload());
        assertNotNull(jobResult.getLastUpdatedDateTime());
        assertNull(jobResult.getError());
    }

    /* Tests_SRS_JOBRESULT_25_021: [The getOutcomeResult shall return the stored outcome.] */
    @Test
    public void gettersEmptyOutcome(@Mocked MethodParser mockedMethodParser) throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        MethodParser methodParser = new MethodParser("methodName", null, null, new HashMap<String, Object>());
        MethodParser methodParserResponse = mockedMethodParser;

        JobsResponseParserExpectations(json, null, methodParser, now, methodParserResponse);
        new NonStrictExpectations()
        {
            {
                JobsResponseParser.createFromJson(json);
                result = mockedJobsResponseParser;

                mockedJobsResponseParser.getJobId();
                result = JOB_ID;
                mockedJobsResponseParser.getType();
                result = "scheduleDeviceMethod";
                mockedJobsResponseParser.getJobsStatus();
                result = "completed";
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = methodParser;
                mockedJobsResponseParser.getOutcome();
                result = methodParserResponse;
                methodParserResponse.toJson();
                result = json;
                methodParserResponse.getStatus();
                result = new IllegalArgumentException();

                Deencapsulation.newInstance(JobStatistics.class, mockedJobsStatisticsParser);
                result = mockedJobStatistics;
            }
        };

        //act
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //assert
        assertNull(jobResult.getOutcomeResult());
    }

    /* Tests_SRS_JOBRESULT_21_020: [The toString shall return a String with a pretty print json that represents this class.] */
    @Test
    public void  toStringReturnClassContent() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT_JSON);
        String nowString = dateFormat.format(now);
        final String expectedPrettyPrint =
                "{\n" +
                "  \"jobId\": \"validJobId\",\n" +
                "  \"queryCondition\": \"DeviceId IN ['validDevice']\",\n" +
                "  \"createdTime\": \"" + nowString + "\",\n" +
                "  \"startTime\": \"" + nowString + "\",\n" +
                "  \"lastUpdatedDateTime\": \"" + nowString + "\",\n" +
                "  \"endTime\": \"" + nowString + "\",\n" +
                "  \"maxExecutionTimeInSeconds\": 100,\n" +
                "  \"jobType\": \"scheduleUpdateTwin\",\n" +
                "  \"jobStatus\": \"enqueued\",\n" +
                "  \"updateTwin\": {\n" +
                "    \"deviceId\": \"validDeviceId\",\n" +
                "    \"eTag\": \"validETag\",\n" +
                "    \"tag\": {\n" +
                "      \"tag1\": \"val1\"\n" +
                "    },\n" +
                "    \"desiredProperties\": {},\n" +
                "    \"twinParser\": {\n" +
                "      \"tags\": {\n" +
                "        \"tags\": {}\n" +
                "      },\n" +
                "      \"properties\": {\n" +
                "        \"desired\": {\n" +
                "          \"lock\": {},\n" +
                "          \"property\": {},\n" +
                "          \"reportMetadata\": false\n" +
                "        },\n" +
                "        \"reported\": {\n" +
                "          \"lock\": {},\n" +
                "          \"property\": {},\n" +
                "          \"reportMetadata\": false\n" +
                "        }\n" +
                "      },\n" +
                "      \"manager\": {}\n" +
                "    }\n" +
                "  },\n" +
                "  \"failureReason\": \"This is a valid failure reason\",\n" +
                "  \"statusMessage\": \"This is a valid status message\",\n" +
                "  \"jobStatistics\": {\n" +
                "    \"deviceCount\": 0,\n" +
                "    \"failedCount\": 0,\n" +
                "    \"succeededCount\": 0,\n" +
                "    \"runningCount\": 0,\n" +
                "    \"pendingCount\": 0\n" +
                "  },\n" +
                "  \"deviceId\": \"validDeviceId\",\n" +
                "  \"parentJobId\": \"validParentJobId\"\n" +
                "}";

        Map<String, Object> tags = new HashMap<>();
        tags.put("tag1", "val1");

        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();
        twinParser.setDeviceId(DEVICE_ID);
        twinParser.setETag(ETAG);
        twinParser.resetTags(tags);

        JobsResponseParserExpectations(json, twinParser, null, now, null);
        JobResult jobResult = Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, json.getBytes());

        //act
        String prettyPrint = jobResult.toString();

        //assert
        assertThat(prettyPrint, is(expectedPrettyPrint));
    }

}
