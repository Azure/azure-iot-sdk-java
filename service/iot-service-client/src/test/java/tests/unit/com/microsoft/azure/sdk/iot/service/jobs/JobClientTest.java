/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.jobs;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.deps.auth.TokenCredentialType;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    DeviceTwinDevice mockedDeviceTwinDevice;

    @Mocked
    DeviceOperations mockedDeviceOperations;

    @Mocked
    JobResult mockedJobResult;

    @Mocked
    HttpResponse mockedHttpResponse;

    URL mockedURL;

    @Mocked
    TokenCredentialType mockedTokenCredentialType;

    @Mocked
    TokenCredential mockedTokenCredential;

    public JobClientTest() throws MalformedURLException
    {
        mockedURL = new URL("https://www.microsoft.com");
    }

    @Before
    public void setUp() throws IOException
    {
        VALID_SQL_QUERY = SqlQuery.createSqlQuery("*",
                                                  SqlQuery.FromType.JOBS, null, null).getQuery();
    }

    /* Tests_SRS_JOBCLIENT_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
    /* Tests_SRS_JOBCLIENT_21_003: [The constructor shall create a new DeviceMethod instance and return it.] */
    @Test
    public void constructorSucceed() throws IOException
    {
        //arrange
        final String connectionString = "testString";

        //act
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //assert
        assertNotNull(testJobClient);
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
        JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = null;

                mockedDeviceTwinDevice.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
                result = null;

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = null;

                mockedDeviceTwinDevice.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
                result = "1234";

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = testDesired;

                mockedDeviceTwinDevice.getReportedProperties();
                result = testResponse;

                new TwinState((TwinCollection)any, (TwinCollection)any, (TwinCollection)any);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
                result = "1234";

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        JobClient testJobClient = null;
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        JobClient testJobClient = null;
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = null;

                mockedDeviceTwinDevice.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = null;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 10;
        JobClient testJobClient = null;
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
        final Date startTimeUtc = null;
        final long maxExecutionTimeInSeconds = 10;
        JobClient testJobClient = null;
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = -10;
        JobClient testJobClient = null;
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = null;

                mockedDeviceTwinDevice.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
                result = null;

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = null;

                mockedDeviceTwinDevice.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
                result = null;

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);
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
        final DeviceTwinDevice updateTwin = mockedDeviceTwinDevice;
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

                mockedDeviceTwinDevice.getTags();
                result = testTags;

                mockedDeviceTwinDevice.getDesiredProperties();
                result = null;

                mockedDeviceTwinDevice.getReportedProperties();
                result = null;

                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;

                mockedDeviceTwinDevice.getDeviceId();
                result = deviceId;

                mockedDeviceTwinDevice.getETag();
                result = null;

                new JobsParser(jobId, mockedTwinState, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc, maxExecutionTimeInSeconds);

        //assert
        assertNotNull(jobResult);
    }

    /* Tests_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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

                new MethodParser(methodName, null, null, payload);
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
                new MethodParser(methodName, null, null, payload);
                result = new IllegalArgumentException();
            }
        };

        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_016: [If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_017: [If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
        }
        catch (IllegalArgumentException e)
        {
            assertTrue("Test did not run because createFromConnectionString failed to create new instance of the JobClient", true);
        }

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_018: [The scheduleDeviceMethod shall create a json String that represent the invoke method job using the JobsParser class.] */
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

                new MethodParser(methodName, null, null, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_019: [The scheduleDeviceMethod shall create a URL for Jobs using the iotHubConnectionString.] */
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

                new MethodParser(methodName, null, null, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlJobs(anyString, jobId);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOBCLIENT_21_020: [The scheduleDeviceMethod shall send a PUT request to the iothub using the created url and json.] */
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

                new MethodParser(methodName, null, null, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOBCLIENT_21_023: [The scheduleDeviceMethod shall parse the iothub response and return it as JobResult.] */
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

                new MethodParser(methodName, null, null, payload);
                result = mockedMethodParser;

                new JobsParser(jobId, mockedMethodParser, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
                result = mockedJobsParser;

                mockedJobsParser.toJson();
                result = json;

                IotHubConnectionString.getUrlJobs(anyString, jobId);
                result = mockedURL;

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        JobResult jobResult = testJobClient.scheduleDeviceMethod(jobId, queryCondition, methodName, null, null, payload, startTimeUtc, maxExecutionTimeInSeconds);

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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };
        try
        {
            testJobClient = JobClient.createFromConnectionString(connectionString);
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

    //Tests_SRS_JOBCLIENT_25_039: [The queryDeviceJob shall create a query object for the type DEVICE_JOB.]
    //Tests_SRS_JOBCLIENT_25_040: [The queryDeviceJob shall send a query request on the query object using Query URL, HTTP POST method and wait for the response by calling sendQueryRequest.]
    @Test
    public void queryDeviceJobSucceeds(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
            }
        };

        //act
        testJobClient.queryDeviceJob(VALID_SQL_QUERY);
    }

    //Tests_SRS_JOBCLIENT_25_036: [If the sqlQuery is null, empty, or invalid, the queryDeviceJob shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void queryDeviceJobThrowsOnNullQuery(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.queryDeviceJob(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void queryDeviceJobThrowsOnEmptyQuery() throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.queryDeviceJob("");
    }

    //Tests_SRS_JOBCLIENT_25_037: [If the pageSize is null, zero or negative, the queryDeviceJob shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void queryDeviceJobThrowsOnNegativePageSize() throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.queryDeviceJob(VALID_SQL_QUERY, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void queryDeviceJobThrowsOnZeroPageSize() throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.queryDeviceJob(VALID_SQL_QUERY, 0);
    }

    /*
    Tests_SRS_JOBCLIENT_25_043: [If the pageSize is not specified, default pageSize of 100 shall be used.] SRS_JOBCLIENT_25_044: [The queryDeviceJob shall create a query object for the type JOB_RESPONSE.]
    Tests_SRS_JOBCLIENT_25_045: [The queryDeviceJob shall send a query request on the query object using Query URL, HTTP GET method and wait for the response by calling sendQueryRequest.]
     */
    @Test
    public void queryJobResponseSucceeds(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {Integer.class, QueryType.class}, anyInt, QueryType.JOB_RESPONSE);
                result = mockedQuery;
            }
        };

        //act
        testJobClient.queryJobResponse(JOB_TYPE_DEFAULT, JOB_STATUS_DEFAULT);
    }

    @Test
    public void queryJobResponseWithoutJobTypeAndJobJobStausSucceeds(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new Expectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {Integer.class, QueryType.class}, anyInt, QueryType.JOB_RESPONSE);
                result = mockedQuery;
            }
        };

        //act
        testJobClient.queryJobResponse(null, null);
    }

    //Tests_SRS_JOBCLIENT_25_042: [If the pageSize is null, zero or negative, the queryJobResponse shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void queryJobResponseThrowsOnNegativePageSize() throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.queryJobResponse(JOB_TYPE_DEFAULT, JOB_STATUS_DEFAULT, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void queryJobResponseThrowsOnZeroPageSize() throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        //act
        testJobClient.queryJobResponse(JOB_TYPE_DEFAULT, JOB_STATUS_DEFAULT, 0);
    }

    //Tests_SRS_JOBCLIENT_25_047: [hasNextJob shall return true if the next job exist, false other wise.]
    @Test
    public void hasNextSucceeds(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        boolean result = testJobClient.hasNextJob(testQuery);
        assertTrue(result);
    }

    //Tests_SRS_JOBCLIENT_25_046: [If the input query is null, empty, or invalid, the hasNextJob shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextThrowsOnNullQuery(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        boolean result = testJobClient.hasNextJob(null);
    }

    @Test (expected = IotHubException.class)
    public void hasNextThrowsIfQueryHasNextThrows(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = new IotHubException();
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        boolean result = testJobClient.hasNextJob(testQuery);
    }

    //Tests_SRS_JOBCLIENT_25_049: [getNextJob shall return next Job Result if the exist, and throw NoSuchElementException other wise.]
    //Tests_SRS_JOBCLIENT_25_051: [getNextJob method shall parse the next job element from the query response provide the response as JobResult object.]
    @Test
    public void nextRetrievesCorrectly(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        final String expectedString = "testJsonAsNext";

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
                Deencapsulation.invoke(mockedQuery, "next");
                result = expectedString;
                Deencapsulation.newInstance(JobResult.class, new Class[] {byte[].class}, (byte[])any);
                result = mockedJobResult;
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        JobResult result = testJobClient.getNextJob(testQuery);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(JobResult.class, new Class [] {byte[].class}, expectedString.getBytes());
                times = 1;
            }
        };
    }

    //Tests_SRS_JOBCLIENT_25_048: [If the input query is null, empty, or invalid, the getNextJob shall throw IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void nextThrowsOnNullQuery(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        testJobClient.getNextJob(null);
    }

    @Test (expected = IotHubException.class)
    public void nextThrowsOnQueryNextThrows(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "next");
                result = new IotHubException();
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        testJobClient.getNextJob(testQuery);
    }

    @Test (expected = NoSuchElementException.class)
    public void nextThrowsIfNoNewElements(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = false;
                Deencapsulation.invoke(mockedQuery, "next");
                result = new NoSuchElementException();
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        testJobClient.getNextJob(testQuery);
    }

    //Tests_SRS_JOBCLIENT_25_050: [getNextJob shall throw IOException if next Job Result exist and is not a string.]
    @Test (expected = IOException.class)
    public void nextThrowsIfNonStringRetrieved(@Mocked Query mockedQuery) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        JobClient testJobClient = JobClient.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.DEVICE_JOB);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
                Deencapsulation.invoke(mockedQuery, "next");
                result = 5;
            }
        };

        Query testQuery = testJobClient.queryDeviceJob(VALID_SQL_QUERY);

        //act
        testJobClient.getNextJob(testQuery);
    }
}
