// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

import com.google.gson.JsonParseException;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatistics;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobType;
import com.microsoft.azure.sdk.iot.service.jobs.serializers.JobsResponseParser;
import com.microsoft.azure.sdk.iot.service.jobs.serializers.JobsStatisticsParser;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodResponse;
import mockit.*;
import org.junit.Test;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit test for job statistics
 * 100% methods, 100% lines covered
 */
public class JobTest
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


    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private void JobsResponseParserExpectations(String json, TwinState twinState, MethodParser methodParser, Date date, MethodParser methodParserResponse, String jobTypeStr)
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
                mockedJobsResponseParser.getCreatedTimeDate();
                result = date;
                mockedJobsResponseParser.getStartTimeDate();
                result = date;
                mockedJobsResponseParser.getLastUpdatedTimeDate();
                result = date;
                mockedJobsResponseParser.getEndTimeDate();
                result = date;
                mockedJobsResponseParser.getMaxExecutionTimeInSeconds();
                result = MAX_EXECUTION_TIME_IN_SECONDS;
                mockedJobsResponseParser.getType();
                result = jobTypeStr;
                mockedJobsResponseParser.getJobsStatus();
                result = "enqueued";
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = methodParser;
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = methodParserResponse;
                mockedJobsResponseParser.getUpdateTwin();
                result = twinState;
                mockedJobsResponseParser.getFailureReason();
                result = FAILURE_REASON;
                mockedJobsResponseParser.getStatusMessage();
                result = STATUS_MESSAGE;
                mockedJobsResponseParser.getDeviceJobStatistics();
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

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private void jobsResponseParserWithNullDeviceIdExpectations(String json, TwinState twinState, MethodParser methodParser, Date date, MethodParser methodParserResponse, String jobTypeStr)
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
                mockedJobsResponseParser.getCreatedTimeDate();
                result = date;
                mockedJobsResponseParser.getStartTimeDate();
                result = date;
                mockedJobsResponseParser.getLastUpdatedTimeDate();
                result = date;
                mockedJobsResponseParser.getEndTimeDate();
                result = date;
                mockedJobsResponseParser.getMaxExecutionTimeInSeconds();
                result = MAX_EXECUTION_TIME_IN_SECONDS;
                mockedJobsResponseParser.getType();
                result = jobTypeStr;
                mockedJobsResponseParser.getJobsStatus();
                result = "enqueued";
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = methodParser;
                mockedJobsResponseParser.getOutcome();
                result = methodParserResponse;
                mockedJobsResponseParser.getUpdateTwin();
                result = twinState;
                mockedJobsResponseParser.getFailureReason();
                result = FAILURE_REASON;
                mockedJobsResponseParser.getStatusMessage();
                result = STATUS_MESSAGE;
                mockedJobsResponseParser.getDeviceJobStatistics();
                result = mockedJobsStatisticsParser;
                mockedJobsResponseParser.getDeviceId();
                result = null;
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
        Deencapsulation.newInstance(ScheduledJob.class, new Class[] {byte[].class}, resultBytes);
    }

    /* Tests_SRS_JOBRESULT_21_002: [The constructor shall parse the body using the JobsResponseParser.] */
    @Test
    public void constructorParseJson() throws IOException
    {
        //arrange
        final String json = "validJson";

        TwinCollection tags = new TwinCollection();
        tags.put("tag1", "val1");

        TwinState twinState = new TwinState(tags, null, null);
        twinState.setDeviceId(DEVICE_ID);
        twinState.setETag(ETAG);

        JobsResponseParserExpectations(json, twinState, null, new Date(), null, "scheduleUpdateTwin");

        //act
        ScheduledJob job = new ScheduledJob(json);

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
        ScheduledJob job = new ScheduledJob(json);
    }

    /* Tests_SRS_JOBRESULT_21_004: [The constructor shall locally store all results information in the provided body.] */
    @Test
    public void constructorStoreJsonContent() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        TwinCollection tags = new TwinCollection();
        tags.put("tag1", "val1");

        TwinState twinState = new TwinState(tags, null, null);
        twinState.setDeviceId(DEVICE_ID);
        twinState.setETag(ETAG);

        JobsResponseParserExpectations(json, twinState, null, now, null, "scheduleUpdateTwin");

        //act
        ScheduledJob job = new ScheduledJob(json);

        //assert
        assertEquals(JOB_ID, Deencapsulation.getField(job, "jobId"));
        assertEquals(QUERY_CONDITION, Deencapsulation.getField(job, "queryCondition"));
        assertEquals(now, Deencapsulation.getField(job, "createdTime"));
        assertEquals(now, Deencapsulation.getField(job, "startTime"));
        assertEquals(now, Deencapsulation.getField(job, "endTime"));
        assertEquals(ScheduledJobType.scheduleUpdateTwin, Deencapsulation.getField(job, "jobType"));
        assertEquals(ScheduledJobStatus.enqueued, Deencapsulation.getField(job, "jobStatus"));
        assertNull(Deencapsulation.getField(job, "cloudToDeviceMethod"));
        assertNotNull(Deencapsulation.getField(job, "updateTwin"));
        assertEquals(FAILURE_REASON, Deencapsulation.getField(job, "failureReason"));
        assertEquals(STATUS_MESSAGE, Deencapsulation.getField(job, "statusMessage"));
        assertNotNull(Deencapsulation.getField(job, "jobStatistics"));
        assertEquals(DEVICE_ID, Deencapsulation.getField(job, "deviceId"));
        assertEquals(PARENT_JOB_ID, Deencapsulation.getField(job, "parentJobId"));
    }

    /* Tests_SRS_JOBRESULT_21_004: [The constructor shall locally store all results information in the provided body.] */
    @Test
    public void constructorStoreJsonContentNoTags() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        TwinCollection desired = new TwinCollection();
        desired.put("prop1", "val1");

        TwinState twinState = new TwinState(null, desired, null);
        twinState.setDeviceId(DEVICE_ID);
        twinState.setETag(ETAG);

        JobsResponseParserExpectations(json, twinState, null, now, null, "scheduleUpdateTwin");

        //act
        ScheduledJob job = new ScheduledJob(json);

        //assert
        assertNotNull(Deencapsulation.getField(job, "updateTwin"));
    }

    /* Tests_SRS_JOBRESULT_21_005: [The getJobId shall return the stored jobId.] */
    /* Tests_SRS_JOBRESULT_21_006: [The getQueryCondition shall return the stored queryCondition.] */
    /* Tests_SRS_JOBRESULT_21_007: [The getCreatedTime shall return the stored createdTime.] */
    /* Tests_SRS_JOBRESULT_21_008: [The getStartTime shall return the stored startTime.] */
    /* Tests_SRS_JOBRESULT_21_009: [The getEndTime shall return the stored endTime.] */
    /* Tests_SRS_JOBRESULT_21_010: [The getMaxExecutionTimeSeconds shall return the stored maxExecutionTimeSeconds.] */
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

        TwinCollection tags = new TwinCollection();
        tags.put("tag1", "val1");

        TwinState twinState = new TwinState(tags, null, null);
        twinState.setDeviceId(DEVICE_ID);
        twinState.setETag(ETAG);

        JobsResponseParserExpectations(json, twinState, null, now, null, "scheduleUpdateTwin");

        //act
        ScheduledJob job = new ScheduledJob(json);

        //assert
        assertEquals(JOB_ID, job.getJobId());
        assertEquals(QUERY_CONDITION, job.getQueryCondition());
        assertEquals(now, job.getCreatedTime());
        assertEquals(now, job.getStartTime());
        assertEquals(now, job.getEndTime());
        assertEquals(MAX_EXECUTION_TIME_IN_SECONDS, (long) job.getMaxExecutionTimeInSeconds());
        assertEquals(ScheduledJobType.scheduleUpdateTwin, job.getJobType());
        assertEquals(ScheduledJobStatus.enqueued, job.getJobStatus());
        assertNull(job.getCloudToDeviceMethod());
        assertNotNull(job.getUpdateTwin());
        assertEquals(FAILURE_REASON, job.getFailureReason());
        assertEquals(STATUS_MESSAGE, job.getStatusMessage());
        assertNotNull(job.getJobStatistics());
        assertEquals(DEVICE_ID, job.getDeviceId());
        assertEquals(PARENT_JOB_ID, job.getParentJobId());
    }

    /* Tests_SRS_JOBRESULT_21_021: [The getUpdateTwin shall return the nullable deviceId.] */
    @Test
    public void gettersTwinContentWithNullDeviceId() throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        TwinCollection tags = new TwinCollection();
        tags.put("tag1", "val1");

        TwinState twinState = new TwinState(tags, null, null);
        twinState.setETag(ETAG);

        jobsResponseParserWithNullDeviceIdExpectations(json, twinState, null, now, null, "scheduleUpdateTwin");

        //act
        ScheduledJob job = new ScheduledJob(json);

        //assert
        assertEquals(JOB_ID, job.getJobId());
        assertEquals(QUERY_CONDITION, job.getQueryCondition());
        assertEquals(now, job.getCreatedTime());
        assertEquals(now, job.getStartTime());
        assertEquals(now, job.getEndTime());
        assertEquals(MAX_EXECUTION_TIME_IN_SECONDS, (long) job.getMaxExecutionTimeInSeconds());
        assertEquals(ScheduledJobType.scheduleUpdateTwin, job.getJobType());
        assertEquals(ScheduledJobStatus.enqueued, job.getJobStatus());
        assertNull(job.getCloudToDeviceMethod());
        assertNotNull(job.getUpdateTwin());
        assertEquals(FAILURE_REASON, job.getFailureReason());
        assertEquals(STATUS_MESSAGE, job.getStatusMessage());
        assertNotNull(job.getJobStatistics());
        assertNull(job.getDeviceId());
        assertNull(job.getUpdateTwin().getDeviceId());
        assertEquals(PARENT_JOB_ID, job.getParentJobId());
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

        MethodParser methodParser = new MethodParser("methodName", 0, 0, new TwinCollection());

        JobsResponseParserExpectations(json, null, methodParser, now, mockedMethodParser, "scheduleUpdateTwin");
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
                mockedMethodParser.toJson();
                result = json;
                mockedMethodParser.getStatus();
                result = methodReturnStatus;
                mockedMethodParser.getPayload();
                result = methodReturnPayload;

                Deencapsulation.newInstance(JobStatistics.class, mockedJobsStatisticsParser);
                result = mockedJobStatistics;
            }
        };

        //act
        ScheduledJob job = new ScheduledJob(json);

        //assert
        assertEquals(JOB_ID, job.getJobId());
        assertEquals(ScheduledJobType.scheduleDeviceMethod, job.getJobType());
        assertEquals(ScheduledJobStatus.completed, job.getJobStatus());
        assertNotNull(job.getCloudToDeviceMethod());
        assertNotNull(job.getOutcomeResult());
        DirectMethodResponse directMethodResponse = job.getOutcomeResult();
        assertEquals(methodReturnStatus, (long) directMethodResponse.getStatus());
        assertEquals(methodReturnPayload, directMethodResponse.getPayloadAsString());
        assertNotNull(job.getLastUpdatedDateTime());
        assertNull(job.getError());
    }

    /* Tests_SRS_JOBRESULT_25_021: [The getOutcomeResult shall return the stored outcome.] */
    @Test
    public void gettersEmptyOutcome(@Mocked MethodParser mockedMethodParser) throws IOException
    {
        //arrange
        final String json = "validJson";
        final Date now = new Date();

        MethodParser methodParser = new MethodParser("methodName", 0, 0, new TwinCollection());

        JobsResponseParserExpectations(json, null, methodParser, now, mockedMethodParser, "scheduleUpdateTwin");
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
                mockedJobsResponseParser.getCloudToDeviceMethod();
                result = mockedMethodParser;
                mockedMethodParser.toJson();
                result = json;
                mockedMethodParser.getStatus();
                result = new IllegalArgumentException();

                Deencapsulation.newInstance(JobStatistics.class, mockedJobsStatisticsParser);
                result = mockedJobStatistics;
            }
        };

        //act
        ScheduledJob job = new ScheduledJob(json);

        //assert
        assertNull(job.getOutcomeResult());
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
                "    \"tags\": {\n" +
                "      \"tag1\": \"val1\"\n" +
                "    },\n" +
                "    \"desiredProperties\": {},\n" +
                "    \"parentScopes\": []\n" +
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

        TwinCollection tags = new TwinCollection();
        tags.put("tag1", "val1");

        TwinState twinState = new TwinState(tags, null, null);
        twinState.setDeviceId(DEVICE_ID);
        twinState.setETag(ETAG);

        JobsResponseParserExpectations(json, twinState, null, now, null, "scheduleUpdateTwin");
        ScheduledJob job = new ScheduledJob(json);

        //act
        String prettyPrint = job.toString();

        //assert
        assertThat(prettyPrint, is(expectedPrettyPrint));
    }

    /* Tests_SRS_JOBRESULT_21_020: [The toString shall return a String with a pretty print json that represents this class.] */
    @Test
    public void  toStringReturnJobTypeUnknown() throws IOException
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
                        "  \"jobType\": \"unknown\",\n" +
                        "  \"jobStatus\": \"enqueued\",\n" +
                        "  \"updateTwin\": {\n" +
                        "    \"deviceId\": \"validDeviceId\",\n" +
                        "    \"eTag\": \"validETag\",\n" +
                        "    \"tags\": {\n" +
                        "      \"tag1\": \"val1\"\n" +
                        "    },\n" +
                        "    \"desiredProperties\": {\n" +
                        "      \"key1\": \"val1\"\n" +
                        "    },\n" +
                        "    \"parentScopes\": []\n" +
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

        TwinCollection tags = new TwinCollection();
        tags.put("tag1", "val1");
        TwinCollection desired = new TwinCollection();
        desired.put("key1", "val1");

        TwinState twinState = new TwinState(tags, desired, null);
        twinState.setDeviceId(DEVICE_ID);
        twinState.setETag(ETAG);

        JobsResponseParserExpectations(json, twinState, null, now, null, "unknown");
        ScheduledJob job = new ScheduledJob(json);

        //act
        String prettyPrint = job.toString();

        //assert
        assertThat(prettyPrint, is(expectedPrettyPrint));
    }
}
