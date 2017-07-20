// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.devicetwin.Job;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for Job
 * 100% methods, 100% lines covered
 */
public class JobTest 
{
    @Mocked
    JobClient mockedJobClient;

    @Mocked
    JobResult mockedJobResult;

    @Mocked
    DeviceTwinDevice mockedDeviceTwinDevice;
    
    /* Tests_SRS_JOB_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor1ThrowOnNullConnectionString()
    {
        // arrange
        final String connectionString = null;

        // act
        Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
    }

    /* Tests_SRS_JOB_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor1ThrowOnEmptyConnectionString()
    {
        // arrange
        final String connectionString = "";

        // act
        Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
    }

    /* Tests_SRS_JOB_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor2ThrowOnNullConnectionString()
    {
        // arrange
        final String jobId = "validJobId";
        final String connectionString = null;

        // act
        Deencapsulation.newInstance(Job.class, new Class[]{String.class, String.class}, jobId, connectionString);
    }

    /* Tests_SRS_JOB_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor2ThrowOnEmptyConnectionString()
    {
        // arrange
        final String jobId = "validJobId";
        final String connectionString = "";

        // act
        Deencapsulation.newInstance(Job.class, new Class[]{String.class, String.class}, jobId, connectionString);
    }

    /* Tests_SRS_JOB_21_002: [If a jobId is provided, the constructor shall use this jobId to identify the Job in the Iothub.] */
    @Test
    public void constructorReceiveUniqueJobId()
    {
        // arrange
        final String jobId = "validJobId";
        final String connectionString = "validConnectionString";

        // act
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class, String.class}, jobId, connectionString);

        // assert
        assertEquals(jobId, Deencapsulation.getField(job, "jobId"));
    }

    /* Tests_SRS_JOB_21_003: [If no jobId is provided, the constructor shall generate a unique jobId to identify the Job in the Iothub.] */
    @Test
    public void constructorCreateUniqueJobId()
    {
        // arrange
        final String connectionString = "validConnectionString";

        // act
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // assert
        assertNotNull(Deencapsulation.getField(job, "jobId"));
        assertFalse(Deencapsulation.getField(job, "jobId").toString().isEmpty());
    }

    /* Tests_SRS_JOB_21_004: [The constructor shall create a new instance of JobClient to manage the Job.] */
    @Test
    public void constructor1CreateJobClient() throws IOException
    {
        // arrange
        final String connectionString = "validConnectionString";

        // act
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // assert
        assertNotNull(Deencapsulation.getField(job, "jobClient"));
        new Verifications()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOB_21_004: [The constructor shall create a new instance of JobClient to manage the Job.] */
    @Test
    public void constructor2CreateJobClient() throws IOException
    {
        // arrange
        final String jobId = "validJobId";
        final String connectionString = "validConnectionString";

        // act
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class, String.class}, jobId, connectionString);

        // assert
        assertNotNull(Deencapsulation.getField(job, "jobClient"));
        new Verifications()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                times = 1;
            }
        };
    }

    /* Tests_SRS_JOB_21_005: [The constructor shall throw IOException if it failed to create a new instance of the JobClient. Threw by the JobClient constructor.] */
    @Test (expected = IOException.class)
    public void constructor1ThrowOnJobClientCreation() throws IOException
    {
        // arrange
        final String connectionString = "validConnectionString";

        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = new IOException();
                times = 1;
            }
        };

        // act
        Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
    }

    /* Tests_SRS_JOB_21_005: [The constructor shall throw IOException if it failed to create a new instance of the JobClient. Threw by the JobClient constructor.] */
    @Test (expected = IOException.class)
    public void constructor2ThrowOnJobClientCreation() throws IOException
    {
        // arrange
        final String jobId = "validJobId";
        final String connectionString = "validConnectionString";

        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = new IOException();
                times = 1;
            }
        };

        // act
        Deencapsulation.newInstance(Job.class, new Class[]{String.class, String.class}, jobId, connectionString);
    }

    /* Tests_SRS_JOB_21_006: [If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinThrowOnNullDeviceTwinDevice() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final DeviceTwinDevice deviceTwin = null;
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleUpdateTwin",
                new Class[]{String.class, DeviceTwinDevice.class, Date.class, Long.class},
                queryCondition, deviceTwin, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_007: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinThrowOnNullStartTimeUTC() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final DeviceTwinDevice deviceTwin = mockedDeviceTwinDevice;
        final Date now = null;
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleUpdateTwin",
                new Class[]{String.class, DeviceTwinDevice.class, Date.class, Long.class},
                queryCondition, deviceTwin, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_008: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinThrowOnInvalidMaxExecutionTimeInSeconds() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final DeviceTwinDevice deviceTwin = mockedDeviceTwinDevice;
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = -100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleUpdateTwin",
                new Class[]{String.class, DeviceTwinDevice.class, Date.class, Long.class},
                queryCondition, deviceTwin, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_009: [The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the JobClient class with the received parameters.] */
    @Test
    public void scheduleUpdateTwinCallJobClient() throws IOException, IotHubException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final DeviceTwinDevice deviceTwin = mockedDeviceTwinDevice;
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.scheduleUpdateTwin((String)any, queryCondition,deviceTwin, now, maxExecutionTimeInSeconds);
                result = mockedJobResult;
                times = 1;
                mockedJobResult.getJobStatus();
                result = JobStatus.enqueued;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleUpdateTwin",
                new Class[]{String.class, DeviceTwinDevice.class, Date.class, Long.class},
                queryCondition, deviceTwin, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_010: [If scheduleUpdateTwin failed, the scheduleUpdateTwin shall throws IotHubException. Threw by the scheduleUpdateTwin.] */
    @Test (expected = IotHubException.class)
    public void scheduleUpdateTwinThrowOnScheduleUpdateTwin() throws IOException, IotHubException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final DeviceTwinDevice deviceTwin = mockedDeviceTwinDevice;
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.scheduleUpdateTwin((String)any, queryCondition,deviceTwin, now, maxExecutionTimeInSeconds);
                result = new IotHubException();
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleUpdateTwin",
                new Class[]{String.class, DeviceTwinDevice.class, Date.class, Long.class},
                queryCondition, deviceTwin, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_011: [If the Iothub reported fail as result of the scheduleUpdateTwin, the scheduleUpdateTwin shall throws IotHubException.] */
    @Test (expected = IotHubException.class)
    public void scheduleUpdateTwinThrowOnJobClientFail() throws IOException, IotHubException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final DeviceTwinDevice deviceTwin = mockedDeviceTwinDevice;
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.scheduleUpdateTwin((String)any, queryCondition,deviceTwin, now, maxExecutionTimeInSeconds);
                result = mockedJobResult;
                mockedJobResult.getJobStatus();
                result = JobStatus.failed;
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleUpdateTwin",
                new Class[]{String.class, DeviceTwinDevice.class, Date.class, Long.class},
                queryCondition, deviceTwin, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_012: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnNullMethodName() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = null;
        final String payload = "validPayload";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_012: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnEmptyMethodName() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "";
        final String payload = "validPayload";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_013: [If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnNullStartTimeUtc() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final String payload = "validPayload";
        final Date now = null;
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_014: [If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnInvalidMaxExecutionTimeInSeconds() throws IOException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final String payload = "validPayload";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = -100;
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_015: [The scheduleDeviceMethod shall invoke the scheduleDeviceMethod in the JobClient class with the received parameters.] */
    @Test
    public void scheduleDeviceMethodInvokeJobClient() throws IOException, IotHubException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final String payload = "validPayload";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.scheduleDeviceMethod((String)any, queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
                result = mockedJobResult;
                times = 1;
                mockedJobResult.getJobStatus();
                result = JobStatus.enqueued;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_016: [If scheduleDeviceMethod failed, the scheduleDeviceMethod shall throws IotHubException. Threw by the scheduleUpdateTwin.] */
    @Test (expected = IOException.class)
    public void scheduleDeviceMethodThrowOnInvokeJobClient() throws IOException, IotHubException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final String payload = "validPayload";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.scheduleDeviceMethod((String)any, queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
                result = new IOException();
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_017: [If the Iothub reported fail as result of the scheduleDeviceMethod, the scheduleDeviceMethod shall throws IotHubException.] */
    @Test (expected = IotHubException.class)
    public void scheduleDeviceMethodThrowOnInvokeJobClientFail() throws IOException, IotHubException
    {
        // arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "validMethodName";
        final String payload = "validPayload";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.scheduleDeviceMethod((String)any, queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
                result = mockedJobResult;
                mockedJobResult.getJobStatus();
                result = JobStatus.failed;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        Deencapsulation.invoke(job, "scheduleDeviceMethod",
                new Class[]{String.class, String.class, Long.class, Long.class, Object.class, Date.class, Long.class},
                queryCondition, methodName, null, null, payload, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_JOB_21_018: [The get shall invoke getJob on JobClient with the current jobId and return its result.] */
    @Test
    public void getInvokeJobClient() throws IOException, IotHubException
    {
        // arrange
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.getJob((String)any);
                result = mockedJobResult;
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        JobResult jobResult = job.get();

        // assert
        assertNotNull(jobResult);
    }

    /* Tests_SRS_JOB_21_019: [If getJob failed, the get shall throws IOException. Threw by the getJob.] */
    @Test (expected = IOException.class)
    public void getThrowOnInvokeJobClient() throws IOException, IotHubException
    {
        // arrange
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.getJob((String)any);
                result = new IOException();
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        job.get();
    }

    /* Tests_SRS_JOB_21_020: [The cancel shall invoke cancelJob on JobClient with the current jobId and return its result.] */
    @Test
    public void cancelInvokeJobClient() throws IOException, IotHubException
    {
        // arrange
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.cancelJob((String)any);
                result = mockedJobResult;
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        JobResult jobResult = job.cancel();

        // assert
        assertNotNull(jobResult);
    }

    /* Tests_SRS_JOB_21_021: [If cancelJob failed, the cancel shall throws IOException. Threw by the cancelJob.] */
    @Test (expected = IOException.class)
    public void cancelThrowOnInvokeJobClient() throws IOException, IotHubException
    {
        // arrange
        final String connectionString = "validConnectionString";

        // assert
        new NonStrictExpectations()
        {
            {
                JobClient.createFromConnectionString(connectionString);
                result = mockedJobClient;
                mockedJobClient.cancelJob((String)any);
                result = new IOException();
                times = 1;
            }
        };

        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);

        // act
        job.cancel();
    }

    /* Tests_SRS_JOB_21_022: [The getJobId shall return the store value of jobId.] */
    @Test
    public void getJobIdSucceed() throws IOException, IotHubException
    {
        // arrange
        final String jobId = "validJobId";
        final String connectionString = "validConnectionString";
        Job job = Deencapsulation.newInstance(Job.class, new Class[]{String.class, String.class}, jobId, connectionString);

        // act
        String result = job.getJobId();

        // assert
        assertEquals(jobId, result);
    }

}
