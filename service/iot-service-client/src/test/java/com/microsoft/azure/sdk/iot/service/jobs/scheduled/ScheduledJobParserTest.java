// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

import com.microsoft.azure.sdk.iot.service.jobs.serializers.ScheduledJobParser;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.service.Helpers;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for jobs serializer
 * 100% methods, 100% lines covered
 */
public class ScheduledJobParserTest
{

    private MethodParser makeMethodSample()
    {
        String methodName = "testMethodName";
        int responseTimeout = 100;
        int connectTimeout = 5;
        Object payload = "{\"testPayload\":\"Success\"}";

        return new MethodParser(methodName, responseTimeout, connectTimeout, payload);
    }


    private TwinState makeTwinSample()
    {
        return new TwinState(
                "{\"tags\":{" +
                    "\"tag1\":{\"KeyChar\":\"c\",\"KeyBool\":true,\"keyString\":\"value1\",\"keyEnum\":\"val1\",\"keyDouble\":1234.456}}," +
                "\"properties\":{" +
                    "\"desired\":{\"key1\":\"value1\",\"key2\":1234,\"key3\":\"value3\"}," +
                    "\"reported\":{\"key1\":\"value1\",\"key3\":\"value3\"}}}");
    }

    /* Tests_SRS_JOBSPARSER_21_001: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_014: [The commonFields shall store the jobId, queryCondition, and maxExecutionTimeSeconds.] */
    @Test
    public void constructorMethodCommonParsSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals(jobId, Deencapsulation.getField(jobsParser, "jobId"));
        assertEquals(queryCondition, Deencapsulation.getField(jobsParser, "queryCondition"));
    }

    /* Tests_SRS_JOBSPARSER_21_001: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_018: [The commonFields shall format startTime as a String and store it.] */
    @Test
    public void constructorMethodFormatStartTimeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertDateWithError(startTime, (String)Deencapsulation.getField(jobsParser, "startTime"));
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodNullJobIdThrows()
    {
        // Arrange
        String jobId = null;
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodEmptyJobIdThrows()
    {
        // Arrange
        String jobId = "";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodInvalidJobIdThrows()
    {
        // Arrange
        String jobId = "test\u1234JobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_017: [If the maxExecutionTimeSeconds is negative, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodInvalidMaxExecutionTimeInSecondsThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = -10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_019: [If the startTime is null, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorMethodNullStartTimeThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = null;
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_003: [The constructor shall store the JsonElement for the cloudToDeviceMethod.] */
    @Test
    public void constructorMethodStoreCloudToDeviceMethodSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertJson(cloudToDeviceMethod.toJsonElement().toString(), Deencapsulation.getField(jobsParser, "cloudToDeviceMethod").toString());
    }

    /* Tests_SRS_JOBSPARSER_21_004: [If the cloudToDeviceMethod is null, the constructor shall throws IllegalArgumentException.] */
    @Test(expected = IllegalArgumentException.class)
    public void constructorMethodNullCloudToDeviceMethodThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = null;

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_005: [The constructor shall set the jobType as scheduleDirectMethod.] */
    @Test
    public void constructorMethodSetJobTypeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals("scheduleDeviceMethod", Deencapsulation.getField(jobsParser, "jobType"));
    }

    /* Tests_SRS_JOBSPARSER_21_006: [The constructor shall set the updateTwin as null.] */
    @Test
    public void constructorMethodSetUpdateTwinSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertNull(Deencapsulation.getField(jobsParser, "updateTwin"));
    }

    /* Tests_SRS_JOBSPARSER_21_013: [The toJson shall return a String with a json that represents the content of this class.] */
    @Test
    public void toJsonMethodSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        MethodParser cloudToDeviceMethod = makeMethodSample();
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, cloudToDeviceMethod, queryCondition, startTime, maxExecutionTimeInSeconds);

        String expectedJson =
                "{" +
                        "\"jobId\":\"" + jobId + "\"," +
                        "\"type\":\"scheduleDeviceMethod\"," +
                        "\"cloudToDeviceMethod\":" + cloudToDeviceMethod.toJson() + "," +
                        "\"updateTwin\":null," +
                        "\"queryCondition\":\"" + queryCondition + "\"," +
                        "\"startTime\":\"" + Helpers.formatUTC(startTime) + "\"," +
                        "\"maxExecutionTimeSeconds\":" + maxExecutionTimeInSeconds +
                "}";

        // Act
        String json = jobsParser.toJson();

        // Assert
        Helpers.assertJson(json,expectedJson);
    }




    /* Tests_SRS_JOBSPARSER_21_007: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_014: [The commonFields shall store the jobId, queryCondition, and maxExecutionTimeSeconds.] */
    @Test
    public void ConstructorTwinCommonParsSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals(jobId, Deencapsulation.getField(jobsParser, "jobId"));
        assertEquals(queryCondition, Deencapsulation.getField(jobsParser, "queryCondition"));
    }

    /* Tests_SRS_JOBSPARSER_21_007: [The constructor shall evaluate and store the commons parameters using the internal function commonFields.] */
    /* Tests_SRS_JOBSPARSER_21_018: [The commonFields shall format startTime as a String and store it.] */
    @Test
    public void ConstructorTwinFormatStartTimeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertDateWithError(startTime, (String)Deencapsulation.getField(jobsParser, "startTime"));
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorTwinNullJobIdThrows()
    {
        // Arrange
        String jobId = null;
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorEmptyJobIdThrows()
    {
        // Arrange
        String jobId = "";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorInvalidJobIdThrows()
    {
        // Arrange
        String jobId = "test\u1234JobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_017: [If the maxExecutionTimeSeconds is negative, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorInvalidMaxExecutionTimeInSecondsThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = -10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
    /* Tests_SRS_JOBSPARSER_21_019: [If the startTime is null, the commonFields shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorNullStartTimeThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = null;
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_009: [The constructor shall store the JsonElement for the updateTwin.] */
    @Test
    public void ConstructorTwinStoreUpdateTwinSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        Helpers.assertJson(twinState.toJsonElement().toString(), Deencapsulation.getField(jobsParser, "updateTwin").toString());
    }

    /* Tests_SRS_JOBSPARSER_21_010: [If the updateTwin is null, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void ConstructorNullUpdateTwinThrows()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = null;

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBSPARSER_21_011: [The constructor shall set the jobType as scheduleUpdateTwin.] */
    @Test
    public void constructorTwinSetJobTypeSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertEquals("scheduleUpdateTwin", Deencapsulation.getField(jobsParser, "jobType"));
    }

    /* Tests_SRS_JOBSPARSER_21_012: [The constructor shall set the cloudToDeviceMethod as null.] */
    @Test
    public void ConstructorTwinSetCloudToDeviceMethodAsNullSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();

        // Act
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);

        // Assert
        assertNull(Deencapsulation.getField(jobsParser, "cloudToDeviceMethod"));
    }

    /* Tests_SRS_JOBSPARSER_21_013: [The toJson shall return a String with a json that represents the content of this class.] */
    @Test
    public void toJsonTwinSucceed()
    {
        // Arrange
        String jobId = "testJobId";
        String queryCondition = "testDeviceId";
        Date startTime = new Date();
        long maxExecutionTimeInSeconds = 10L;
        TwinState twinState = makeTwinSample();
        ScheduledJobParser jobsParser= new ScheduledJobParser(jobId, twinState, queryCondition, startTime, maxExecutionTimeInSeconds);

        String expectedJson =
                "{" +
                        "\"jobId\":\"" + jobId + "\"," +
                        "\"type\":\"scheduleUpdateTwin\"," +
                        "\"cloudToDeviceMethod\":null," +
                        "\"updateTwin\":" + twinState.toJsonElement().toString() + "," +
                        "\"queryCondition\":\"" + queryCondition + "\"," +
                        "\"startTime\":\"" + Helpers.formatUTC(startTime) + "\"," +
                        "\"maxExecutionTimeSeconds\":" + maxExecutionTimeInSeconds +
                "}";

        // Act
        String json = jobsParser.toJson();

        // Assert
        Helpers.assertJson(json, expectedJson);
    }

}
