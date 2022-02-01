/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.service.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.query.SqlQuery;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit test for job client
 * 100% methods, 100% lines covered
 */
public class JobClientTest
{
    private static String VALID_SQL_QUERY = null;
    private static final JobType JOB_TYPE_DEFAULT = JobType.scheduleDeviceMethod;
    private static final JobStatus JOB_STATUS_DEFAULT = JobStatus.completed;

    @Mocked
    IotHubConnectionStringBuilder mockedConnectionStringBuilder;

    @Mocked
    IotHubConnectionString mockedIotHubConnectionString;

    @Mocked
    JobsParser mockedJobsParser;

    @Mocked
    TwinState mockedTwinState;

    @Mocked
    MethodParser mockedMethodParser;

    @Mocked
    Twin mockedTwin;

    @Mocked
    JobResult mockedJobResult;

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
        VALID_SQL_QUERY = SqlQuery.createSqlQuery("*",
                                                  SqlQuery.FromType.JOBS, null, null).getQuery();
    }

    /* Tests_SRS_JOBCLIENT_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
    /* Tests_SRS_JOBCLIENT_21_003: [The constructor shall create a new DirectMethodsClient instance and return it.] */
    @Test
    public void constructorSucceed() throws IOException
    {
        //arrange
        final String connectionString = "testString";

        //act
        JobClient testJobClient = new JobClient(connectionString);

        //assert
        assertNotNull(testJobClient);
    }

    @Test
    public void testOptionsDefaults()
    {
        JobClientOptions options = JobClientOptions.builder().build();
        assertEquals((int) Deencapsulation.getField(JobClientOptions.class, "DEFAULT_HTTP_READ_TIMEOUT_MS"), options.getHttpReadTimeout());
        assertEquals((int) Deencapsulation.getField(JobClientOptions.class, "DEFAULT_HTTP_CONNECT_TIMEOUT_MS"), options.getHttpConnectTimeout());
    }

    /* Tests_SRS_JOBCLIENT_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullCS() throws IOException
    {
        //arrange
        final String connectionString = null;

        //act
        new JobClient(connectionString);
    }

    /* Tests_SRS_JOBCLIENT_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyCS() throws IOException
    {
        //arrange
        final String connectionString = "";

        //act
        new JobClient(connectionString);
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
        new JobClient(connectionString);
    }

    /* Tests_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the JobsParser class.] */
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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the JobsParser class.] */
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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the JobsParser class.] */
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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));
        Set<Pair> testDesired = new HashSet<>();
        testTags.add(new Pair("testDesired", "val1"));
        Set<Pair> testResponse = new HashSet<>();
        testTags.add(new Pair("testResponse", "val2"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));
        final String json = "validJson";
        JobClient testJobClient = null;
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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = new MalformedURLException();
            }
        };

        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_008: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = new IOException();
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_013: [The scheduleUpdateTwin shall parse the iothub response and return it as JobResult.] */
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

        Set<Pair> testTags = new HashSet<>();
        testTags.add(new Pair("testTag", "tagObject"));

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

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        //assert
        assertNotNull(jobResult);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";
        JobClient testJobClient = null;

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = new MalformedURLException();
            }
        };

        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
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
        JobClient testJobClient = null;

        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDirectMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowsOnInvalidMethodName() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "ValidJobId";
        final String queryCondition = "validQueryCondition";
        final String methodName = "invalidMethodName";
        final Set<String> payload = new HashSet<>();
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        JobClient testJobClient = null;
        new NonStrictExpectations()
        {
            {
                new MethodParser(methodName, anyInt, anyInt, payload);
                result = new IllegalArgumentException();
            }
        };

        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
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
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_017: [If the maxExecutionTimeInSeconds is negative, the scheduleDirectMethod shall throws IllegalArgumentException.] */
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
        final long maxExecutionTimeInSeconds = -10;
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_018: [The scheduleDirectMethod shall create a json String that represent the invoke method job using the JobsParser class.] */
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
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        //assert
        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
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
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);

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
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
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
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = new IOException();
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);
    }

    /* Tests_SRS_JOBCLIENT_21_023: [The scheduleDirectMethod shall parse the iothub response and return it as JobResult.] */
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
        final long maxExecutionTimeInSeconds = 10;
        final String json = "validJson";

        new Expectations()
        {
            {
                IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                new MethodParser(methodName, anyInt, anyInt, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                new HttpRequest(mockedURL, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), anyString, (Proxy) any);
                result = mockHttpRequest;

                mockHttpRequest.send();
                result = mockedHttpResponse;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = new JobClient(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleDirectMethod(jobId, queryCondition, methodName, startTimeUtc);

        //assert
        assertNotNull(jobResult);
    }


    /* Tests_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the getJob shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getJobThrowsOnNullJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = null;
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.getJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the getJob shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getJobThrowsOnEmptyJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "";
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.getJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the getJob shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getJobThrowsOnInvalidJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "invalidJobId";
        JobClient testJobClient = null;
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
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.getJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_025: [The getJob shall create a URL for Jobs using the iotHubConnectionString.] */
    @Test
    public void getJobCreateURL() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.getJob(jobId);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobs(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_026: [The getJob shall send a GET request to the iothub using the created url.] */
    @Test
    public void getJobSendGET() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.getJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_029: [The getJob shall parse the iothub response and return it as JobResult.] */
    @Test
    public void getJobParseResponse() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        JobResult jobResult = testJobClient.getJob(jobId);

        //assert
        assertNotNull(jobResult);
    }

    /* Tests_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancelJob shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void cancelJobThrowsOnNullJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = null;
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.cancelJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancelJob shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void cancelJobThrowsOnEmptyJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "";
        JobClient testJobClient = null;
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.cancelJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancelJob shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void cancelJobThrowsOnInvalidJobId() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "invalidJobId";
        JobClient testJobClient = null;
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
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.cancelJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_031: [The cancelJob shall create a cancel URL for Jobs using the iotHubConnectionString.] */
    @Test
    public void cancelJobCreateURL() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.cancelJob(jobId);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobsCancel(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_032: [The cancelJob shall send a POST request to the iothub using the created url.] */
    @Test
    public void cancelJobSendPOST() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.cancelJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_033: [If the cancelJob failed to send a POST request, it shall throw IOException.] */
    /* Tests_SRS_JOBCLIENT_21_034: [If the cancelJob failed to verify the iothub response, it shall throw IotHubException.] */
    @Test (expected = IOException.class)
    public void cancelJobThrowsOnSendPOST() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.cancelJob(jobId);
    }

    /* Tests_SRS_JOBCLIENT_21_035: [The cancelJob shall parse the iothub response and return it as JobResult.] */
    @Test
    public void cancelJobParseResponse() throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String jobId = "validJobId";
        JobClient testJobClient = null;
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = new JobClient(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        JobResult jobResult = testJobClient.cancelJob(jobId);

        //assert
        assertNotNull(jobResult);
    }
}
