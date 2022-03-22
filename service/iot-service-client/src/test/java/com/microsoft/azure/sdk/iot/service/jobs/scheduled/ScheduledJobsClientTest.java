/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.DirectMethodsJobOptions;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobType;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobsClient;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobsClientOptions;
import com.microsoft.azure.sdk.iot.service.jobs.serializers.ScheduledJobParser;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.query.SqlQueryBuilder;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Unit test for job client
 * 100% methods, 100% lines covered
 */
public class ScheduledJobsClientTest
{
    private static String VALID_SQL_QUERY = null;
    private static final ScheduledJobType JOB_TYPE_DEFAULT = ScheduledJobType.scheduleDeviceMethod;
    private static final ScheduledJobStatus JOB_STATUS_DEFAULT = ScheduledJobStatus.completed;

    @Mocked
    IotHubConnectionStringBuilder mockedConnectionStringBuilder;

    @Mocked
    IotHubConnectionString mockedIotHubConnectionString;

    @Mocked
    ScheduledJobParser mockedJobsParser;

    @Mocked
    TwinState mockedTwinState;

    @Mocked
    MethodParser mockedMethodParser;

    @Mocked
    Twin mockedTwin;

    @Mocked
    ScheduledJob mockedJob;

    @Mocked
    HttpResponse mockedHttpResponse;

    @Mocked
    URL mockedURL;

    @Mocked
    IotHubServiceSasToken mockIotHubServiceSasToken;
    
    @Mocked
    HttpRequest mockHttpRequest;

    @Before
    public void setUp() throws IOException
    {
        VALID_SQL_QUERY = SqlQueryBuilder.createSqlQuery("*",
                                                  SqlQueryBuilder.FromType.JOBS, null, null);
    }

    /* Tests_SRS_JOBCLIENT_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
    /* Tests_SRS_JOBCLIENT_21_003: [The constructor shall create a new DirectMethodsClient instance and return it.] */
    @Test
    public void constructorSucceed() throws IOException
    {
        //arrange
        final String connectionString = "testString";

        //act
        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //assert
        assertNotNull(testJobClient);
    }

    @Test
    public void testOptionsDefaults()
    {
        ScheduledJobsClientOptions options = ScheduledJobsClientOptions.builder().build();
        assertEquals((int) Deencapsulation.getField(ScheduledJobsClientOptions.class, "DEFAULT_HTTP_READ_TIMEOUT_SECONDS"), options.getHttpReadTimeoutSeconds());
        assertEquals((int) Deencapsulation.getField(ScheduledJobsClientOptions.class, "DEFAULT_HTTP_CONNECT_TIMEOUT_SECONDS"), options.getHttpConnectTimeoutSeconds());
    }

    /* Tests_SRS_JOBCLIENT_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullCS() throws IOException
    {
        //arrange
        final String connectionString = null;

        //act
        new ScheduledJobsClient(connectionString);
    }

    /* Tests_SRS_JOBCLIENT_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyCS() throws IOException
    {
        //arrange
        final String connectionString = "";

        //act
        new ScheduledJobsClient(connectionString);
    }

    /* Tests_SRS_JOBCLIENT_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidCS() throws IOException
    {
        //arrange
        final String connectionString = "ImproperCSFormat";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = new IllegalArgumentException();
            }
        };

        //act
        new ScheduledJobsClient(connectionString);
    }

    /* Tests_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the ScheduledJobParser class.] */
    @Test
    public void scheduleUpdateTwinCreateJson() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        //assert
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = null;

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        ScheduledJob job = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the ScheduledJobParser class.] */
    @Test
    public void scheduleUpdateTwinCreateJsonWithEtag() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = "1234";

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        ScheduledJob job = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the ScheduledJobParser class.] */
    @Test
    public void scheduleUpdateTwinCreateJsonWithProperties() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        TwinCollection testDesired = new TwinCollection();
        testTags.put("testDesired", "val1");

        TwinCollection testResponse = new TwinCollection();
        testTags.put("testResponse", "val2");

        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = testDesired;

                mockedTwin.getReportedProperties();
                result = testResponse;

                new TwinState((TwinCollection)any, (TwinCollection)any, (TwinCollection)any);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = "1234";

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        ScheduledJob job = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_005: [If the JobId is null, empty, or invalid, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateThrowsOnNullJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = null;
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_005: [If the JobId is null, empty, or invalid, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateThrowsOnEmptyJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_005: [If the JobId is null, empty, or invalid, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateThrowsOnInvalidJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "invalidJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        final String json = "validJson";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = null;

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = new MalformedURLException();
            }
        };

        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_006: [If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateThrowsOnNullUpdateTwin() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = null;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_007: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateThrowsOnNullStartTimeUtc() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = null;
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_008: [If the maxExecutionTimeSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateThrowsOnNullMaxExecutionTimeInSeconds() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = -10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_009: [The scheduleUpdateTwin shall create a URL for Jobs using the iotHubConnectionString.] */
    @Test
    public void scheduleUpdateTwinCreateURL() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = null;

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        ScheduledJob job = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobs(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_010: [The scheduleUpdateTwin shall send a PUT request to the iothub using the created uri and json.] */
    @Test
    public void scheduleUpdateTwinSendPUT() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = null;

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        ScheduledJob job = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_011: [If the scheduleUpdateTwin failed to send a PUT request, it shall throw IOException.] */
    /* Tests_SRS_JOBCLIENT_21_012: [If the scheduleUpdateTwin failed to verify the iothub response, it shall throw IotHubException.] */
    @Test (expected = IOException.class)
    public void scheduleUpdateTwinThrowsOnSendPUT() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = null;

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = new IOException();
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_013: [The scheduleUpdateTwin shall parse the iothub response and return it as ScheduledJob.] */
    @Test
    public void scheduleUpdateTwinReturnResponse() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String deviceId = "validDeviceId";
        final String queryCondition = "validQueryCondition";
        final Twin updateTwin = mockedTwin;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        TwinCollection testTags = new TwinCollection();
        testTags.put("testTag", "tagObject");

        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedTwin.getTags();
                result = testTags;

                mockedTwin.getDesiredProperties();
                result = null;

                mockedTwin.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedTwin.getDeviceId();
                result = deviceId;

                mockedTwin.getETag();
                result = null;

                new ScheduledJobParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        ScheduledJob job = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        //assert
        assertNotNull(job);
    }

    /* Tests_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnNullJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = null;
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnEmptyJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnInvalidJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "invalidJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = 10;
        final String json = "validJson";
        ScheduledJobsClient testJobClient = null;

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new ScheduledJobParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = new MalformedURLException();
            }
        };

        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);
    }

    /* Tests_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnNullMethodName() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "ValidJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = null;
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;

        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnEmptyMethodName() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "ValidJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_016: [If the startTimeUtc is null, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnNullStartTimeUtc() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "ValidJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = null;
        final long maxExecutionTimeInSeconds = 10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_017: [If the maxExecutionTimeSeconds is negative, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnNegativeMaxExecutionTimeInSeconds() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "ValidJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = -10;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);
    }

    /* Tests_SRS_JOBCLIENT_21_018: [The scheduleDirectMethod shall create a json String that represent the invoke method job using the ScheduledJobParser class.] */
    @Test
    public void scheduleDeviceMethodCreateJson() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        //assert
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new ScheduledJobParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        ScheduledJob job = testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);
    }

    /* Tests_SRS_JOBCLIENT_21_019: [The scheduleDirectMethod shall create a URL for Jobs using the iotHubConnectionString.] */
    @Test
    public void scheduleDeviceMethodCreateUrl() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new ScheduledJobParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        ScheduledJob job = testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobs(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_020: [The scheduleDirectMethod shall send a PUT request to the iothub using the created url and json.] */
    @Test
    public void scheduleDeviceMethodSendPUT() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new ScheduledJobParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);
    }

    /* Tests_SRS_JOBCLIENT_21_021: [If the scheduleDirectMethod failed to send a PUT request, it shall throw IOException.] */
    /* Tests_SRS_JOBCLIENT_21_022: [If the scheduleDirectMethod failed to verify the iothub response, it shall throw IotHubException.] */
    @Test (expected = IOException.class)
    public void scheduleDeviceMethodThrowsOnSendPUT() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new ScheduledJobParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = new IOException();
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);
    }

    /* Tests_SRS_JOBCLIENT_21_023: [The scheduleDirectMethod shall parse the iothub response and return it as ScheduledJob.] */
    @Test
    public void scheduleDeviceMethodParseResponse() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final int maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new ScheduledJobParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };

        ScheduledJobsClient testJobClient = new ScheduledJobsClient(connectionString);

        //act
        DirectMethodsJobOptions options = DirectMethodsJobOptions.builder().payload(payload).maxExecutionTimeSeconds(maxExecutionTimeInSeconds).build();
        ScheduledJob job = testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc, options);

        //assert
        assertNotNull(job);
    }


    /* Tests_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the get shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getJobThrowsOnNullJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = null;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.get(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the get shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getJobThrowsOnEmptyJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "";
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.get(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the get shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getJobThrowsOnInvalidJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "invalidJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = new MalformedURLException();
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.get(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_025: [The get shall create a URL for Jobs using the iotHubConnectionString.] */
    @Test
    public void getJobCreateURL() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.GET, new byte[]{}, anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.get(jobId);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobs(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_026: [The get shall send a GET request to the iothub using the created url.] */
    @Test
    public void getJobSendGET() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.GET, new byte[]{}, anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.get(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_029: [The get shall parse the iothub response and return it as ScheduledJob.] */
    @Test
    public void getJobParseResponse() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.GET, new byte[]{}, anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        ScheduledJob job = testJobClient.get(jobId);

        //assert
        assertNotNull(job);
    }

    /* Tests_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancel shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void cancelJobThrowsOnNullJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = null;
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.cancel(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancel shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void cancelJobThrowsOnEmptyJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "";
        ScheduledJobsClient testJobClient = null;
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.cancel(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancel shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void cancelJobThrowsOnInvalidJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "invalidJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                result = new MalformedURLException();
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.cancel(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_031: [The cancel shall create a cancel URL for Jobs using the iotHubConnectionString.] */
    @Test
    public void cancelJobCreateURL() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.cancel(jobId);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_032: [The cancel shall send a POST request to the iothub using the created url.] */
    @Test
    public void cancelJobSendPOST() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.cancel(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_033: [If the cancel failed to send a POST request, it shall throw IOException.] */
    /* Tests_SRS_JOBCLIENT_21_034: [If the cancel failed to verify the iothub response, it shall throw IotHubException.] */
    @Test (expected = IOException.class)
    public void cancelJobThrowsOnSendPOST() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.POST, (byte[]) any, anyString, (Proxy) any);
                result = new IOException();
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        testJobClient.cancel(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_035: [The cancel shall parse the iothub response and return it as ScheduledJob.] */
    @Test
    public void cancelJobParseResponse() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        ScheduledJobsClient testJobClient = null;
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, (HttpMethod) any, (byte[]) any, anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                new ScheduledJob(anyString);
                result = mockedJob;
            }
        };
        try
        {
            testJobClient = new ScheduledJobsClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the ScheduledJobsClient", true);
        }

        //act
        ScheduledJob job = testJobClient.cancel(jobId);

        //assert
        assertNotNull(job);
    }
}
