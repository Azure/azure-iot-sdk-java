/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;


import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.deps.serializer.JobsResponseParser;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import lombok.extern.slf4j.Slf4j;
import net.jcip.annotations.NotThreadSafe;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to method and twin jobs.
 */
@Slf4j
@IotHubTest
@NotThreadSafe // these tests will be run in serial because of this annotation. IoT Hub has a limit on number of concurrent jobs
public class JobClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    public static boolean isBasicTierHub;
    private static JobClient jobClient;
    private static RegistryManager registryManager;

    private static final String STANDARD_PROPERTY_HOMETEMP = "HomeTemp(F)";

    private static final int MAX_DEVICES = 1;

    private static final String DEVICE_ID_NAME = "E2EJavaJob";
    private static final String JOB_ID_NAME = "JobTest";

    private static final long MAX_TIME_WAIT_FOR_PREVIOUSLY_SCHEDULED_JOBS_TO_FINISH_IN_MILLIS = 6 * 60 * 1000; // 6 minutes
    private static final long RESPONSE_TIMEOUT = TimeUnit.SECONDS.toSeconds(120);
    private static final long CONNECTION_TIMEOUT = TimeUnit.SECONDS.toSeconds(5);
    private static final long TEST_TIMEOUT_MILLISECONDS = 7 * 60 * 1000L; // 7 minutes
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 200; // 0.2 sec
    private static final String PAYLOAD_STRING = "This is a valid payload";
    private static int newTemperature = 70;

    private static final List<DeviceTestManager> devices = new LinkedList<>();
    private static Device testDevice;

    private static final int MAX_NUMBER_JOBS = 3;
    private static final long MAX_EXECUTION_TIME_IN_SECONDS = 15;

    @BeforeClass
    public static void setUp() throws IOException, IotHubException, InterruptedException, URISyntaxException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        jobClient = new JobClient(iotHubConnectionString);
        registryManager = new RegistryManager(
            iotHubConnectionString,
            RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testDevice = Tools.addDeviceWithRetry(registryManager, Device.createFromId(DEVICE_ID_NAME.concat("-" + i + "-" + uuid), DeviceStatus.Enabled, null));
            DeviceTestManager testManager = new DeviceTestManager(new DeviceClient(registryManager.getDeviceConnectionString(testDevice), IotHubClientProtocol.AMQPS));
            testManager.client.open();
            testManager.subscribe(true, true);
            devices.add(testManager);
        }
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static JobResult queryDeviceJobResult(String jobId, JobType jobType, JobStatus jobStatus) throws IOException, IotHubException
    {
        String queryContent = SqlQuery.createSqlQuery("*", SqlQuery.FromType.JOBS,
            "devices.jobs.jobId = '" + jobId + "' and devices.jobs.jobType = '" + jobType.toString() + "'",
            null).getQuery();
        Query query = jobClient.queryDeviceJob(queryContent);
        JobResult jobResult;
        while (jobClient.hasNextJob(query))
        {
            jobResult = jobClient.getNextJob(query);
            if (jobResult.getJobId().equals(jobId))
            {
                if (jobResult.getJobType() == jobType)
                {
                    if (jobResult.getJobStatus() == jobStatus)
                    {
                        //query confirmed that the specified job has the correct type, and status
                        return jobResult;
                    }
                    else
                    {
                        throw new AssertionError("queryDeviceJob received job unexpected status. Expected " + jobStatus + " but job ended with status " + jobResult.getJobStatus());
                    }
                }
                else
                {
                    throw new AssertionError("queryDeviceJob received job with the wrong job type. Expected " + jobType + " but found " + jobResult.getJobType());
                }
            }
        }

        throw new AssertionError("queryDeviceJob did not find the job");
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private JobResult queryJobResponseResult(String jobId, JobType jobType, JobStatus jobStatus) throws IOException, IotHubException
    {
        Query query = jobClient.queryJobResponse(jobType, jobStatus);
        JobResult jobResult;
        while (jobClient.hasNextJob(query))
        {
            jobResult = jobClient.getNextJob(query);
            if (jobResult.getJobId().equals(jobId) &&
                (jobResult.getJobType() == jobType) &&
                (jobResult.getJobStatus() == jobStatus))
            {
                log.info("Iothub confirmed {} {} for type {}", jobId, jobStatus, jobType);
                return jobResult;
            }
        }
        throw new AssertionError("queryDeviceJob did not find the job");
    }

    @Before
    public void cleanToStart() throws IOException, IotHubException
    {
        for (DeviceTestManager device : devices)
        {
            device.clearStatistics();
        }

        log.info("Waiting for all previously scheduled jobs to finish...");
        long startTime = System.currentTimeMillis();
        Query activeJobsQuery = jobClient.queryDeviceJob("SELECT * FROM devices.jobs");
        while (activeJobsQuery.hasNext())
        {
            JobsResponseParser job = JobsResponseParser.createFromJson(activeJobsQuery.next().toString());

            JobStatus jobStatus = jobClient.getJob(job.getJobId()).getJobStatus();
            while (jobStatus.equals(JobStatus.enqueued) || jobStatus.equals(JobStatus.queued) || jobStatus.equals(JobStatus.running) || jobStatus.equals(JobStatus.scheduled))
            {
                try
                {
                    Thread.sleep(500);
                    jobStatus = jobClient.getJob(job.getJobId()).getJobStatus();
                } catch (InterruptedException e)
                {
                    fail("Unexpected interrupted exception occurred");
                }

                if (System.currentTimeMillis() - startTime > MAX_TIME_WAIT_FOR_PREVIOUSLY_SCHEDULED_JOBS_TO_FINISH_IN_MILLIS)
                {
                    fail("Waited too long for previously scheduled jobs to finish");
                }
            }
        }

        log.info("Done waiting for jobs to finish!");
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        for (DeviceTestManager device : devices)
        {
            device.tearDown();
        }

        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @Ignore
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
        final ArrayList<String> jobIdsPending = new ArrayList<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            final int jobTemperature = (newTemperature++);
            executor.submit(() ->
            {
                String jobId = JOB_ID_NAME + UUID.randomUUID();
                jobIdsPending.add(jobId);
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
                        new Date(), MAX_EXECUTION_TIME_IN_SECONDS);

                    JobResult jobResult = jobClient.getJob(jobId);
                    while (jobResult.getJobStatus() != JobStatus.completed)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        jobResult = jobClient.getJob(jobId);
                    }
                    jobResult = queryJobResponseResult(jobId, JobType.scheduleUpdateTwin, JobStatus.completed);
                    jobResults.put(jobId, jobResult);
                } catch (IotHubException | IOException | InterruptedException e)
                {
                    jobExceptions.put(jobId, e);
                }
                jobIdsPending.remove(jobId);
            });
        }

        cleanupJobs(executor, jobIdsPending);

        // Assert
        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
        ConcurrentMap<String, ConcurrentLinkedQueue<Object>> changes = deviceTestManger.getTwinChanges();
        ConcurrentLinkedQueue<Object> receivedTemperatures = changes.get(STANDARD_PROPERTY_HOMETEMP);
        assertNotNull(receivedTemperatures);
        assertEquals(MAX_NUMBER_JOBS, receivedTemperatures.size());

        // asserts for the service side.
        if (jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException : jobExceptions.entrySet())
            {
                log.error("{} threw", jobException.getKey(), jobException.getValue());
            }
            fail("Service throw an exception enqueuing jobs");
        }
        assertEquals("Missing job result", MAX_NUMBER_JOBS, jobResults.size());
        for (Map.Entry<String, JobResult> job : jobResults.entrySet())
        {
            String jobId = job.getKey();
            JobResult jobResult = job.getValue();
            assertNotNull(jobResult);
            assertEquals("JobResult reported incorrect jobId", jobId, jobResult.getJobId());
            String expectedTemperature = twinExpectedTemperature.get(jobId) + ".0";
            assertTrue("Device do not change " + STANDARD_PROPERTY_HOMETEMP + " to " + expectedTemperature, receivedTemperatures.contains(expectedTemperature));
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @Ignore
    public void scheduleDeviceMethodSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, JobResult> jobResults = new ConcurrentHashMap<>();

        final ArrayList<String> jobIdsPending = new ArrayList<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            executor.submit(() ->
            {
                String jobId = JOB_ID_NAME + UUID.randomUUID();
                jobIdsPending.add(jobId);
                try
                {
                    jobClient.scheduleDeviceMethod(
                        jobId, queryCondition,
                        DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING,
                        new Date(), MAX_EXECUTION_TIME_IN_SECONDS);

                    JobResult jobResult = jobClient.getJob(jobId);
                    while (jobResult.getJobStatus() != JobStatus.completed)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        jobResult = jobClient.getJob(jobId);
                    }
                    log.info("job finished with status {}", jobResult.getJobStatus());

                    if (jobResult.getJobStatus().equals(JobStatus.completed))
                    {
                        jobResult = queryDeviceJobResult(jobId, JobType.scheduleDeviceMethod, JobStatus.completed);
                        jobResults.put(jobId, jobResult);
                    }
                    else
                    {
                        jobExceptions.put(jobId, new Exception("Scheduled job did not finish with status 'completed' but with " + jobResult.getJobStatus()));
                    }
                } catch (IotHubException | IOException | InterruptedException e)
                {
                    jobExceptions.put(jobId, e);
                    log.warn("Adding {} to job exceptions...", jobId, e);
                }
                jobIdsPending.remove(jobId);
            });
        }

        cleanupJobs(executor, jobIdsPending);

        // Assert
        // asserts for the service side.
        if (jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException : jobExceptions.entrySet())
            {
                log.error("{} threw", jobException.getKey(), jobException.getValue());
            }
            fail("Service throw an exception enqueuing jobs");
        }
        assertEquals("Missing job result", MAX_NUMBER_JOBS, jobResults.size());
        for (Map.Entry<String, JobResult> jobResult : jobResults.entrySet())
        {
            assertNotNull(jobResult.getValue());
            MethodResult methodResult = jobResult.getValue().getOutcomeResult();
            assertNotNull("Device method didn't return any outcome", methodResult);
            assertEquals(200L, (long) methodResult.getStatus());
            assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, methodResult.getPayload());
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    public void scheduleDeviceMethodWithAzureSasCredentialSucceed() throws InterruptedException, IOException, IotHubException
    {
        // Arrange
        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        JobClient jobClientWithSasCredential = new JobClient(iotHubConnectionStringObj.getHostName(), sasCredential);

        scheduleDeviceMethod(jobClientWithSasCredential);
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    public void jobClientTokenRenewalWithAzureSasCredential() throws InterruptedException, IOException, IotHubException
    {
        // Arrange
        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);

        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        JobClient jobClientWithSasCredential = new JobClient(iotHubConnectionStringObj.getHostName(), sasCredential);

        // JobClient usage should succeed since the shared access signature hasn't expired yet
        scheduleDeviceMethod(jobClientWithSasCredential);

        // deliberately expire the SAS token to provoke a 401 to ensure that the job client is using the shared
        // access signature that is set here.
        sasCredential.update(SasTokenTools.makeSasTokenExpired(serviceSasToken.toString()));

        try
        {
            scheduleDeviceMethod(jobClientWithSasCredential);
            fail("Expected scheduling a job to throw unauthorized exception since an expired SAS token was used, but no exception was thrown");
        }
        catch (IotHubUnathorizedException e)
        {
            log.debug("IotHubUnauthorizedException was thrown as expected, continuing test");
        }

        // Renew the expired shared access signature
        serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        sasCredential.update(serviceSasToken.toString());

        // JobClient usage should succeed since the shared access signature has been renewed
        scheduleDeviceMethod(jobClientWithSasCredential);
    }

    private static void scheduleDeviceMethod(JobClient jobClient) throws IOException, IotHubException, InterruptedException
    {
        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        // Act
        String jobId = JOB_ID_NAME + UUID.randomUUID();
        jobClient.scheduleDeviceMethod(
            jobId,
            queryCondition,
            DeviceEmulator.METHOD_LOOPBACK,
            RESPONSE_TIMEOUT,
            CONNECTION_TIMEOUT,
            PAYLOAD_STRING,
            new Date(),
            MAX_EXECUTION_TIME_IN_SECONDS);

        JobResult jobResult = jobClient.getJob(jobId);
        while (jobResult.getJobStatus() != JobStatus.completed)
        {
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
            jobResult = jobClient.getJob(jobId);
        }

        log.info("job finished with status {}", jobResult.getJobStatus());

        if (jobResult.getJobStatus().equals(JobStatus.completed))
        {
            jobResult = queryDeviceJobResult(jobId, JobType.scheduleDeviceMethod, JobStatus.completed);
        }
        else
        {
            fail("Failed to schedule a method invocation, job status " + jobResult.getJobStatus() + ":" + jobResult.getStatusMessage());
        }

        MethodResult methodResult = jobResult.getOutcomeResult();
        assertNotNull("Device method didn't return any outcome", methodResult);
        assertEquals(200L, (long) methodResult.getStatus());
        assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, methodResult.getPayload());

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @ContinuousIntegrationTest
    @Ignore
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
        final ArrayList<String> jobIdsPending = new ArrayList<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            final int index = i;
            final int jobTemperature = (newTemperature++);
            executor.submit(() ->
            {
                String jobId = JOB_ID_NAME + UUID.randomUUID();
                jobIdsPending.add(jobId);
                try
                {
                    if (index % 2 == 0)
                    {
                        jobClient.scheduleDeviceMethod(
                            jobId, queryCondition,
                            DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING,
                            future, MAX_EXECUTION_TIME_IN_SECONDS);
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
                            new Date(), MAX_EXECUTION_TIME_IN_SECONDS);
                    }
                    JobResult jobResult = jobClient.getJob(jobId);
                    while (jobResult.getJobStatus() != JobStatus.completed)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        jobResult = jobClient.getJob(jobId);
                    }
                    jobResult = queryDeviceJobResult(jobId,
                        ((index % 2 == 0) ? JobType.scheduleDeviceMethod : JobType.scheduleUpdateTwin),
                        JobStatus.completed);
                    jobResults.put(jobId, jobResult);
                } catch (IotHubException | IOException | InterruptedException e)
                {
                    jobExceptions.put(jobId, e);
                }
                jobIdsPending.remove(jobId);
            });
        }

        cleanupJobs(executor, jobIdsPending);

        // wait until identity receive the twin change
        ConcurrentMap<String, ConcurrentLinkedQueue<Object>> changes = deviceTestManger.getTwinChanges();
        int timeout = 0;
        while (changes.size() == 0)
        {
            if ((timeout += MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS) >= TEST_TIMEOUT_MILLISECONDS)
            {
                fail("Device didn't receive the twin change");
            }
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
            changes = deviceTestManger.getTwinChanges();
        }

        // Assert
        // asserts for the service side.
        if (jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException : jobExceptions.entrySet())
            {
                log.error("{} threw", jobException.getKey(), jobException.getValue());
            }
            fail("Service throw an exception enqueuing jobs");
        }
        assertEquals("Missing job result", MAX_NUMBER_JOBS, jobResults.size());
        ConcurrentLinkedQueue<Object> temperatures = changes.get(STANDARD_PROPERTY_HOMETEMP);
        assertNotNull("There is no " + STANDARD_PROPERTY_HOMETEMP + " in the device changes", temperatures);
        for (Map.Entry<String, JobResult> job : jobResults.entrySet())
        {
            JobResult jobResult = job.getValue();
            String jobId = jobResult.getJobId();
            assertNotNull(jobResult);
            if (jobResult.getJobType() == JobType.scheduleDeviceMethod)
            {
                MethodResult methodResult = jobResult.getOutcomeResult();
                assertNotNull("Device method didn't return any outcome", methodResult);
                assertEquals(200L, (long) methodResult.getStatus());
                assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, methodResult.getPayload());
            }
            else
            {
                String temperature = twinExpectedTemperature.get(jobId) + ".0";
                assertTrue("Device do not change " + STANDARD_PROPERTY_HOMETEMP + " to " + temperature, temperatures.contains(temperature));
            }
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @ContinuousIntegrationTest
    @Ignore
    public void cancelScheduleDeviceMethodSucceed() throws IOException, IotHubException, InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final Date future = new Date(new Date().getTime() + 180000L); // 3 minutes in the future.

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ArrayList<String> jobIdsPending = new ArrayList<>();

        // Act
        for (int i = 0; i < MAX_NUMBER_JOBS; i++)
        {
            final int index = i;
            executor.submit(() ->
            {
                String jobId = JOB_ID_NAME + UUID.randomUUID();
                jobIdsPending.add(jobId);
                try
                {
                    jobClient.scheduleDeviceMethod(
                        jobId, queryCondition,
                        DeviceEmulator.METHOD_LOOPBACK, RESPONSE_TIMEOUT, CONNECTION_TIMEOUT, PAYLOAD_STRING,
                        (index % 2 == 0) ? future : new Date(), MAX_EXECUTION_TIME_IN_SECONDS);

                    JobStatus expectedJobStatus = JobStatus.completed;
                    if (index % 2 == 0)
                    {
                        expectedJobStatus = JobStatus.cancelled;
                        Thread.sleep(1000); // wait 1 seconds and cancel.
                        jobClient.cancelJob(jobId);
                    }

                    JobResult jobResult = jobClient.getJob(jobId);
                    while (jobResult.getJobStatus() != expectedJobStatus)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        jobResult = jobClient.getJob(jobId);
                    }
                    log.info("Iothub confirmed {} {} for type {}", jobId, expectedJobStatus, JobType.scheduleDeviceMethod);
                } catch (IotHubException | IOException | InterruptedException e)
                {
                    jobExceptions.put(jobId, e);
                }
                jobIdsPending.remove(jobId);
            });
        }

        cleanupJobs(executor, jobIdsPending);

        // Assert
        // asserts for the service side.
        if (jobExceptions.size() != 0)
        {
            for (Map.Entry<String, Exception> jobException : jobExceptions.entrySet())
            {
                log.error("{} threw", jobException.getKey(), jobException.getValue());
            }
            fail("Service throw an exception enqueuing jobs");
        }

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    private void cleanupJobs(ExecutorService executor, List<String> jobIdsPending) throws InterruptedException
    {
        executor.shutdown();
        if (!executor.awaitTermination(TEST_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
            String pendingJobIds = getPendingJobIds(jobIdsPending);
            fail("Test finish with timeout. Pending jobid's were: " + pendingJobIds);
        }
    }

    private String getPendingJobIds(List<String> jobIdsPending)
    {
        StringBuilder pendingJobIds = new StringBuilder();
        for (String jobId : jobIdsPending)
        {
            pendingJobIds.append(jobId).append(" ");
        }
        return pendingJobIds.toString();
    }
}
