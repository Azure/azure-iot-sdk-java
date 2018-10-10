/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.serviceclient;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import org.junit.*;
import com.microsoft.azure.sdk.iot.common.helpers.DeviceEmulator;
import com.microsoft.azure.sdk.iot.common.helpers.DeviceTestManager;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Integration E2E test for Job Client on the service client.
 */
@Ignore //ignoring these tests until icm bug 77555728 is resolved
public class JobClientCommon
{
    protected static String iotHubConnectionString = "";
    private static JobClient jobClient;
    private static RegistryManager registryManager;

    private static final String STANDARD_PROPERTY_HOMETEMP = "HomeTemp(F)";

    private static final int MAX_DEVICES = 1;

    private static String DEVICE_ID_NAME = "E2EJavaJob";
    private static String JOB_ID_NAME = "JobTest";

    private static final long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(120);
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    private static final long TEST_TIMEOUT_MS = 60000L; // 1 minute
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 200; // 0.2 sec
    private static final String PAYLOAD_STRING = "This is a valid payload";
    private static int newTemperature = 70;

    private static List<DeviceTestManager> devices = new LinkedList<>();
    private static Device testDevice;

    private static final int MAX_NUMBER_JOBS = 3;
    private static final long MAX_EXECUTION_TIME_IN_MS = 100;

    private JobResult queryDeviceJobResult(String jobId, JobType jobType, JobStatus jobStatus) throws IOException, IotHubException
    {
        String queryContent = SqlQuery.createSqlQuery("*", SqlQuery.FromType.JOBS,
                "devices.jobs.jobId = '" + jobId + "' and devices.jobs.jobType = '" + jobType.toString() + "'",
                null).getQuery();
        Query query = jobClient.queryDeviceJob(queryContent);
        JobResult jobResult;
        while(jobClient.hasNextJob(query))
        {
            jobResult = jobClient.getNextJob(query);
            if(jobResult.getJobId().equals(jobId) &&
                    (jobResult.getJobType() == jobType) &&
                    (jobResult.getJobStatus() == jobStatus))
            {
                System.out.println("Iothub confirmed " + jobId + " " + jobStatus + " for " + jobType);
                return jobResult;
            }
        }
        throw new IotHubException("queryDeviceJob do not find the job");
    }

    private JobResult queryJobResponseResult(String jobId, JobType jobType, JobStatus jobStatus) throws IOException, IotHubException
    {
        Query query = jobClient.queryJobResponse(jobType, jobStatus);
        JobResult jobResult;
        while(jobClient.hasNextJob(query))
        {
            jobResult = jobClient.getNextJob(query);
            if(jobResult.getJobId().equals(jobId) &&
                    (jobResult.getJobType() == jobType) &&
                    (jobResult.getJobStatus() == jobStatus))
            {
                System.out.println("Iothub confirmed " + jobId + " " + jobStatus + " for " + jobType);
                return jobResult;
            }
        }
        throw new IotHubException("queryDeviceJob do not find the job");
    }

    //@BeforeClass
    public static void setUp() throws IOException, IotHubException, InterruptedException, URISyntaxException
    {
        jobClient = JobClient.createFromConnectionString(iotHubConnectionString);
        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testDevice = registryManager.addDevice(Device.createFromId(DEVICE_ID_NAME.concat("-" + i + "-" + uuid), DeviceStatus.Enabled, null));
            devices.add(new DeviceTestManager(null));
        }
    }

    //@Before
    public void cleanToStart() throws IOException, IotHubException
    {
        for (DeviceTestManager device:devices)
        {
            device.clearDevice();
        }
    }

    //@AfterClass
    public static void tearDown() throws Exception
    {
        for (DeviceTestManager device:devices)
        {
            device.stop();
        }

        if (registryManager != null){
            registryManager.close();
            registryManager = null;
        }
    }

    @Ignore
    @Test
    public void scheduleUpdateTwinSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();
        final ConcurrentMap<String, Integer> twinExpectedTemperature = new ConcurrentHashMap<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            final int jobTemperature = (newTemperature++);
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    String jobId = JOB_ID_NAME + UUID.randomUUID();
                    try
                    {
                        DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(deviceId);
                        Set<Pair> testDesProp = new HashSet<>();
                        testDesProp.add(new Pair(STANDARD_PROPERTY_HOMETEMP, jobTemperature));
                        deviceTwinDevice.setDesiredProperties(testDesProp);
                        twinExpectedTemperature.put(jobId, jobTemperature);

                        jobClient.scheduleUpdateTwin(
                                jobId, queryCondition,
                                deviceTwinDevice,
                                new Date(), MAX_EXECUTION_TIME_IN_MS);

                        JobResult jobResult = jobClient.getJob(jobId);
                        while(jobResult.getJobStatus() != JobStatus.completed)
                        {
                            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
                            jobResult = jobClient.getJob(jobId);
                        }
                        jobResult = queryJobResponseResult(jobId, JobType.scheduleUpdateTwin, JobStatus.completed);
                        jobResults.put(jobId, jobResult);
                    }
                    catch (IotHubException | IOException | InterruptedException e)
                    {
                        jobExceptions.put(jobId, e);
                    }
                }
            });
        }

        // Cleanup
        executor.shutdown();
        if (!executor.awaitTermination(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
            assertTrue("Test finish with timeout", false);
        }

        // Assert
        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
        ConcurrentMap<String, ConcurrentLinkedQueue<Object>> changes = deviceTestManger.getTwinChanges();
        ConcurrentLinkedQueue<Object> receivedTemperatures = changes.get(STANDARD_PROPERTY_HOMETEMP);
        assertEquals(MAX_NUMBER_JOBS, receivedTemperatures.size());

        // asserts for the service side.
        if(jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException: jobExceptions.entrySet())
            {
                System.out.println(jobException.getKey() + " throws " + jobException.getValue().getMessage());
            }
            assertTrue("Service throw an exception enqueuing jobs", false);
        }
        assertEquals("Missing job result", MAX_NUMBER_JOBS, jobResults.size());
        for (Map.Entry<String, JobResult> job: jobResults.entrySet())
        {
            String jobId = job.getKey();
            JobResult jobResult = job.getValue();
            assertNotNull(jobResult);
            assertEquals("JobResult reported incorrect jobId", jobId, jobResult.getJobId());
            String expectedTemperature = Integer.toString(twinExpectedTemperature.get(jobId)) + ".0";
            assertTrue("Device do not change " + STANDARD_PROPERTY_HOMETEMP + " to " + expectedTemperature, receivedTemperatures.contains(expectedTemperature));
        }
    }

    @Ignore
    @Test
    public void scheduleDeviceMethodSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    String jobId = JOB_ID_NAME + UUID.randomUUID();
                    try
                    {
                        jobClient.scheduleDeviceMethod(
                                jobId, queryCondition,
                                DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING,
                                new Date(), MAX_EXECUTION_TIME_IN_MS);

                        JobResult jobResult = jobClient.getJob(jobId);
                        while(jobResult.getJobStatus() != JobStatus.completed)
                        {
                            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
                            jobResult = jobClient.getJob(jobId);
                        }
                        jobResult = queryDeviceJobResult(jobId, JobType.scheduleDeviceMethod, JobStatus.completed);
                        jobResults.put(jobId, jobResult);
                    }
                    catch (IotHubException | IOException |InterruptedException e)
                    {
                        jobExceptions.put(jobId, e);
                    }
                }
            });
        }

        // Cleanup
        executor.shutdown();
        if (!executor.awaitTermination(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
            assertTrue("Test finish with timeout", false);
        }

        // Assert
        // asserts for the service side.
        if(jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException: jobExceptions.entrySet())
            {
                System.out.println(jobException.getKey() + " throws " + jobException.getValue().getMessage());
            }
            assertTrue("Service throw an exception enqueuing jobs", false);
        }
        assertEquals("Missing job result", MAX_NUMBER_JOBS, jobResults.size());
        for (Map.Entry<String, JobResult> jobResult: jobResults.entrySet())
        {
            assertNotNull(jobResult.getValue());
            MethodResult methodResult = jobResult.getValue().getOutcomeResult();
            assertNotNull("Device method didn't return any outcome", methodResult);
            assertEquals(200L, (long)methodResult.getStatus());
            assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, methodResult.getPayload());
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Ignore
    @Test
    public void mixScheduleInFutureSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final Date future = new Date(new Date().getTime() + 10000L); // 10 seconds in the future.

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();
        final ConcurrentMap<String, Integer> twinExpectedTemperature = new ConcurrentHashMap<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            final int index = i;
            final int jobTemperature = (newTemperature++);
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    String jobId = JOB_ID_NAME + UUID.randomUUID();
                    try
                    {
                        if(index % 2 == 0)
                        {
                            jobClient.scheduleDeviceMethod(
                                    jobId, queryCondition,
                                    DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING,
                                    future, MAX_EXECUTION_TIME_IN_MS);
                        }
                        else
                        {
                            DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(deviceId);
                            Set<Pair> testDesProp = new HashSet<>();
                            testDesProp.add(new Pair(STANDARD_PROPERTY_HOMETEMP, jobTemperature));
                            deviceTwinDevice.setDesiredProperties(testDesProp);
                            twinExpectedTemperature.put(jobId, jobTemperature);

                            jobClient.scheduleUpdateTwin(
                                    jobId, queryCondition,
                                    deviceTwinDevice,
                                    new Date(), MAX_EXECUTION_TIME_IN_MS);
                        }
                        JobResult jobResult = jobClient.getJob(jobId);
                        while(jobResult.getJobStatus() != JobStatus.completed)
                        {
                            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
                            jobResult = jobClient.getJob(jobId);
                        }
                        jobResult = queryDeviceJobResult(jobId,
                                ((index % 2 == 0)?JobType.scheduleDeviceMethod:JobType.scheduleUpdateTwin),
                                JobStatus.completed);
                        jobResults.put(jobId, jobResult);
                    }
                    catch (IotHubException | IOException |InterruptedException e)
                    {
                        jobExceptions.put(jobId, e);
                    }
                }
            });
        }

        // Cleanup
        executor.shutdown();
        if (!executor.awaitTermination(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
            assertTrue("Test finish with timeout", false);
        }

        // wait until device receive the Twin change
        ConcurrentMap<String, ConcurrentLinkedQueue<Object>> changes = deviceTestManger.getTwinChanges();
        int timeout = 0;
        while(changes.size() == 0)
        {
            if((timeout += MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB) >= TEST_TIMEOUT_MS)
            {
                assertTrue("Device didn't receive the twin change", false);
            }
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB); // wait 10 seconds
            changes = deviceTestManger.getTwinChanges();
        }

        // Assert
        // asserts for the service side.
        if(jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException: jobExceptions.entrySet())
            {
                System.out.println(jobException.getKey() + " throws " + jobException.getValue().getMessage());
            }
            assertTrue("Service throw an exception enqueuing jobs", false);
        }
        assertEquals("Missing job result", MAX_NUMBER_JOBS, jobResults.size());
        ConcurrentLinkedQueue<Object> temperatures = changes.get(STANDARD_PROPERTY_HOMETEMP);
        assertNotNull("There is no " + STANDARD_PROPERTY_HOMETEMP + " in the device changes", temperatures);
        for (Map.Entry<String, JobResult> job: jobResults.entrySet())
        {
            JobResult jobResult = job.getValue();
            String jobId = jobResult.getJobId();
            assertNotNull(jobResult);
            if(jobResult.getJobType() == JobType.scheduleDeviceMethod)
            {
                MethodResult methodResult = jobResult.getOutcomeResult();
                assertNotNull("Device method didn't return any outcome", methodResult);
                assertEquals(200L, (long)methodResult.getStatus());
                assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, methodResult.getPayload());
            }
            else
            {
                String temperature = Integer.toString(twinExpectedTemperature.get(jobId)) + ".0";
                assertTrue("Device do not change " + STANDARD_PROPERTY_HOMETEMP + " to " + temperature, temperatures.contains(temperature));
            }
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Ignore
    @Test
    public void cancelScheduleDeviceMethodSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final Date future = new Date(new Date().getTime() + 180000L); // 3 minutes in the future.

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            final int index = i;
            executor.submit(new Runnable()
            {
                @Override
                public void run()
                {
                    String jobId = JOB_ID_NAME + UUID.randomUUID();
                    try
                    {
                        jobClient.scheduleDeviceMethod(
                                jobId, queryCondition,
                                DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING,
                                (index % 2 == 0)?future:new Date(), MAX_EXECUTION_TIME_IN_MS);

                        JobStatus expectedJobStatus = JobStatus.completed;
                        if(index % 2 == 0)
                        {
                            expectedJobStatus = JobStatus.cancelled;
                            Thread.sleep(1000); // wait 1 seconds and cancel.
                            jobClient.cancelJob(jobId);
                        }

                        JobResult jobResult = jobClient.getJob(jobId);
                        while (jobResult.getJobStatus() != expectedJobStatus)
                        {
                            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB);
                            jobResult = jobClient.getJob(jobId);
                        }
                        System.out.println("Iothub confirmed " + jobId + " " + expectedJobStatus + " for " + JobType.scheduleDeviceMethod);
                    }
                    catch (IotHubException | IOException |InterruptedException e)
                    {
                        jobExceptions.put(jobId, e);
                    }
                }
            });
        }

        // Cleanup
        executor.shutdown();
        if (!executor.awaitTermination(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
            assertTrue("Test finish with timeout", false);
        }

        // Assert
        // asserts for the service side.
        if(jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException: jobExceptions.entrySet())
            {
                System.out.println(jobException.getKey() + " throws " + jobException.getValue().getMessage());
            }
            assertTrue("Service throw an exception enqueuing jobs", false);
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }
}
