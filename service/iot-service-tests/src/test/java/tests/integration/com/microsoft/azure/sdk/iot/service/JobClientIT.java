/*
* Copyright (c) Microsoft. All rights reserved.
* Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.rmi.server.InactiveGroupException;
import tests.integration.com.microsoft.azure.sdk.iot.service.helpers.DeviceEmulator;
import tests.integration.com.microsoft.azure.sdk.iot.service.helpers.DeviceTestManager;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * Integration E2E test for Job Client on the service client.
 */
public class JobClientIT
{
    private static String iotHubConnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static JobClient jobClient;

    private static final String STANDARD_PROPERTY_HOMETEMP = "HomeTemp(F)";

    private static final int MAX_DEVICES = 1;

    private static String DEVICE_ID_NAME = "E2EJavaJob";
    private static String JOB_ID_NAME = "JobTest";

    private static final long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(120);
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    private static final long TEST_TIMEOUT_MS = 30000L; // 30 seconds
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB = 200; // 0.2 sec
    private static final String PAYLOAD_STRING = "This is a valid payload";

    private static List<DeviceTestManager> devices = new LinkedList<>();

    private static final int MAX_NUMBER_JOBS = 3;
    private static final long MAX_EXECUTION_TIME_IN_MS = 100;
    private static final int NEW_TEMPERATURE_TEST = 50;

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
                return jobResult;
            }
        }
        throw new IotHubException("queryDeviceJob do not find the job");
    }

    private JobResult queryJobResponseResult(String jobId, JobType jobType, JobStatus jobStatus) throws IOException, IotHubException
    {
        Query query = jobClient.queryJobResponse(jobType, JobStatus.completed);
        JobResult jobResult;
        while(jobClient.hasNextJob(query))
        {
            jobResult = jobClient.getNextJob(query);
            if(jobResult.getJobId().equals(jobId) &&
                    (jobResult.getJobType() == jobType) &&
                    (jobResult.getJobStatus() == jobStatus))
            {
                return jobResult;
            }
        }
        throw new IotHubException("queryDeviceJob do not find the job");
    }

    @BeforeClass
    public static void setUp() throws NoSuchAlgorithmException, IotHubException, IOException, URISyntaxException, InterruptedException
    {
        Map<String, String> env = System.getenv();
        for (String envName : env.keySet())
        {
            if (envName.equals(iotHubConnectionStringEnvVarName.toString()))
            {
                iotHubConnectionString = env.get(envName);
                break;
            }
        }

        if ((iotHubConnectionString == null) || iotHubConnectionString.isEmpty())
        {
            throw new IllegalArgumentException("Environment variable is not set: " + iotHubConnectionStringEnvVarName);
        }

        jobClient = JobClient.createFromConnectionString(iotHubConnectionString);

        RegistryManager registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            devices.add(new DeviceTestManager(registryManager, DEVICE_ID_NAME.concat("-" + i), IotHubClientProtocol.MQTT));
        }
    }

    @Before
    public void cleanToStart() throws IOException, IotHubException
    {
        for (DeviceTestManager device:devices)
        {
            device.clearDevice();
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        for (DeviceTestManager device:devices)
        {
            device.stop();
        }
    }

    @Test
    public void scheduleUpdateTwinSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = deviceTestManger.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();

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
                        DeviceTwinDevice deviceTwinDevice = new DeviceTwinDevice(deviceId);
                        Set<Pair> testDesProp = new HashSet<>();
                        testDesProp.add(new Pair(STANDARD_PROPERTY_HOMETEMP, (NEW_TEMPERATURE_TEST + index)));
                        deviceTwinDevice.setDesiredProperties(testDesProp);

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
            assertEquals("JobResult reported incorrect jobId", jobResult.getKey(), jobResult.getValue().getJobId());
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
        ConcurrentMap<String, ConcurrentLinkedQueue<Object>> changes = deviceTestManger.getTwinChanges();
        ConcurrentLinkedQueue<Object> temperatures = changes.get(STANDARD_PROPERTY_HOMETEMP);
        assertEquals(MAX_NUMBER_JOBS, temperatures.size());
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            String expectedTemperature = Integer.toString(NEW_TEMPERATURE_TEST + i) + ".0";
            assertTrue("Device do not change " + STANDARD_PROPERTY_HOMETEMP + " to " + expectedTemperature, temperatures.contains(expectedTemperature));
        }
    }

    @Test
    public void scheduleDeviceMethodSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = deviceTestManger.getDeviceId();
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();

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

    @Test
    public void mixScheduleInFutureSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = deviceTestManger.getDeviceId();
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final Date future = new Date(new Date().getTime() + 10000L); // 10 seconds in the future.

        ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();
        ConcurrentMap<String, Integer> twinExpectedTemperature = new ConcurrentHashMap<>();

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
                            int temperature = NEW_TEMPERATURE_TEST + index;
                            testDesProp.add(new Pair(STANDARD_PROPERTY_HOMETEMP, temperature));
                            deviceTwinDevice.setDesiredProperties(testDesProp);
                            twinExpectedTemperature.put(jobId, temperature);

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
        ConcurrentMap<String, ConcurrentLinkedQueue<Object>> changes = deviceTestManger.getTwinChanges();
        ConcurrentLinkedQueue<Object> temperatures = changes.get(STANDARD_PROPERTY_HOMETEMP);
        if(temperatures == null)
        {
            System.out.println(changes.toString());
        }
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

    @Test
    public void cancelScheduleDeviceMethodSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = deviceTestManger.getDeviceId();
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final Date future = new Date(new Date().getTime() + 180000L); // 3 minutes in the future.

        ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();

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
