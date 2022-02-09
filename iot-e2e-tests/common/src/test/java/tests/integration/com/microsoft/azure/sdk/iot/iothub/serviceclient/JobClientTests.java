/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;


import com.azure.core.credential.AzureSasCredential;
import com.microsoft.azure.sdk.iot.service.jobs.DirectMethodsJobOptions;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobType;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobsClient;
import com.microsoft.azure.sdk.iot.service.query.JobQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.query.SqlQueryBuilder;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.methods.MethodResult;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubUnathorizedException;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobsClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceEmulator;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceTestManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SasTokenTools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to method and twin jobs.
 */
@Ignore
@Slf4j
@IotHubTest
public class JobClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    public static boolean isBasicTierHub;
    private static ScheduledJobsClient jobClient;
    private static RegistryClient registryClient;

    private static final String STANDARD_PROPERTY_HOMETEMP = "HomeTemp(F)";

    private static final int MAX_DEVICES = 1;

    private static final String DEVICE_ID_NAME = "E2EJavaJob";
    private static final String JOB_ID_NAME = "JobTest";

    private static final long MAX_TIME_WAIT_FOR_PREVIOUSLY_SCHEDULED_JOBS_TO_FINISH_IN_MILLIS = 6 * 60 * 1000; // 6 minutes
    private static final int RESPONSE_TIMEOUT = 120;
    private static final int CONNECTION_TIMEOUT = 5;
    private static final long TEST_TIMEOUT_MILLISECONDS = 7 * 60 * 1000L; // 7 minutes
    private static final long MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS = 200; // 0.2 sec
    private static final String PAYLOAD_STRING = "This is a valid payload";
    private static int newTemperature = 70;

    private static final List<DeviceTestManager> devices = new LinkedList<>();
    private static Device testDevice;

    private static final int MAX_NUMBER_JOBS = 3;
    private static final int MAX_EXECUTION_TIME_IN_SECONDS = 15;

    @BeforeClass
    public static void setUp() throws IOException, IotHubException, InterruptedException, URISyntaxException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        jobClient = new ScheduledJobsClient(iotHubConnectionString);
        registryClient = new RegistryClient(
            iotHubConnectionString,
            RegistryClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());

        String uuid = UUID.randomUUID().toString();
        for (int i = 0; i < MAX_DEVICES; i++)
        {
            testDevice = Tools.addDeviceWithRetry(registryClient, new Device(DEVICE_ID_NAME.concat("-" + i + "-" + uuid)));
            DeviceTestManager testManager = new DeviceTestManager(new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, testDevice), IotHubClientProtocol.AMQPS));
            testManager.client.open(false);
            testManager.subscribe(true, true);
            devices.add(testManager);
        }
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        for (DeviceTestManager device : devices)
        {
            device.tearDown();
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @Ignore
    public void scheduleUpdateTwinSucceed() throws InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, ScheduledJob> jobResults = new ConcurrentHashMap<>();
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
                    Twin twin = new Twin(deviceId);
                    Set<Pair> testDesProp = new HashSet<>();
                    testDesProp.add(new Pair(STANDARD_PROPERTY_HOMETEMP, jobTemperature));
                    twin.setDesiredProperties(testDesProp);
                    twinExpectedTemperature.put(jobId, jobTemperature);

                    jobClient.scheduleUpdateTwin(
                        jobId, queryCondition,
                        twin,
                        new Date(), MAX_EXECUTION_TIME_IN_SECONDS);

                    ScheduledJob job = jobClient.get(jobId);
                    while (job.getJobStatus() != ScheduledJobStatus.completed)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        job = jobClient.get(jobId);
                    }
                    job = queryDeviceJobResult(jobId, ScheduledJobType.scheduleUpdateTwin, ScheduledJobStatus.completed);
                    jobResults.put(jobId, job);
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
        for (Map.Entry<String, ScheduledJob> job : jobResults.entrySet())
        {
            String jobId = job.getKey();
            ScheduledJob jobResult = job.getValue();
            assertNotNull(jobResult);
            assertEquals("ScheduledJob reported incorrect jobId", jobId, jobResult.getJobId());
            String expectedTemperature = twinExpectedTemperature.get(jobId) + ".0";
            assertTrue("Device do not change " + STANDARD_PROPERTY_HOMETEMP + " to " + expectedTemperature, receivedTemperatures.contains(expectedTemperature));
        }
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @Ignore
    public void scheduleDeviceMethodSucceed() throws InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, ScheduledJob> jobResults = new ConcurrentHashMap<>();

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
                    DirectMethodsJobOptions options =
                        DirectMethodsJobOptions.builder()
                            .payload(PAYLOAD_STRING)
                            .methodConnectTimeout(CONNECTION_TIMEOUT)
                            .methodResponseTimeout(RESPONSE_TIMEOUT)
                            .maxExecutionTimeInSeconds(MAX_EXECUTION_TIME_IN_SECONDS)
                            .build();

                    jobClient.scheduleDirectMethod(jobId, queryCondition, DeviceEmulator.METHOD_LOOPBACK, new Date(), options);

                    ScheduledJob job = jobClient.get(jobId);
                    while (job.getJobStatus() != ScheduledJobStatus.completed)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        job = jobClient.get(jobId);
                    }
                    log.info("job finished with status {}", job.getJobStatus());

                    if (job.getJobStatus().equals(ScheduledJobStatus.completed))
                    {
                        job = queryDeviceJobResult(jobId, ScheduledJobType.scheduleDeviceMethod, ScheduledJobStatus.completed);
                        jobResults.put(jobId, job);
                    }
                    else
                    {
                        jobExceptions.put(jobId, new Exception("Scheduled job did not finish with status 'completed' but with " + job.getJobStatus()));
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
        for (Map.Entry<String, ScheduledJob> jobResult : jobResults.entrySet())
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
        scheduleDeviceMethod(buildJobClientWithAzureSasCredential());
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    public void jobClientTokenRenewalWithAzureSasCredential() throws InterruptedException, IOException, IotHubException
    {
        // Arrange
        IotHubConnectionString iotHubConnectionStringObj =
            IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);

        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);

        AzureSasCredential sasCredential = new AzureSasCredential(serviceSasToken.toString());
        ScheduledJobsClient jobClientWithSasCredential = new ScheduledJobsClient(iotHubConnectionStringObj.getHostName(), sasCredential);

        // ScheduledJobsClient usage should succeed since the shared access signature hasn't expired yet
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

        // ScheduledJobsClient usage should succeed since the shared access signature has been renewed
        scheduleDeviceMethod(jobClientWithSasCredential);
    }

    private static void scheduleDeviceMethod(ScheduledJobsClient jobClient) throws IOException, IotHubException, InterruptedException
    {
        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        // Act
        String jobId = JOB_ID_NAME + UUID.randomUUID();
        DirectMethodsJobOptions options =
            DirectMethodsJobOptions.builder()
                .payload(PAYLOAD_STRING)
                .methodConnectTimeout(CONNECTION_TIMEOUT)
                .methodResponseTimeout(RESPONSE_TIMEOUT)
                .maxExecutionTimeInSeconds(MAX_EXECUTION_TIME_IN_SECONDS)
                .build();

        jobClient.scheduleDirectMethod(jobId, queryCondition, DeviceEmulator.METHOD_LOOPBACK, new Date(), options);

        ScheduledJob job = jobClient.get(jobId);
        while (job.getJobStatus() != ScheduledJobStatus.completed)
        {
            Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
            job = jobClient.get(jobId);
        }

        log.info("job finished with status {}", job.getJobStatus());

        if (job.getJobStatus().equals(ScheduledJobStatus.completed))
        {
            job = queryDeviceJobResult(jobId, ScheduledJobType.scheduleDeviceMethod, ScheduledJobStatus.completed);
        }
        else
        {
            fail("Failed to schedule a method invocation, job status " + job.getJobStatus() + ":" + job.getStatusMessage());
        }

        MethodResult methodResult = job.getOutcomeResult();
        assertNotNull("Device method didn't return any outcome", methodResult);
        assertEquals(200L, (long) methodResult.getStatus());
        assertEquals(DeviceEmulator.METHOD_LOOPBACK + ":" + PAYLOAD_STRING, methodResult.getPayload());

        // asserts for the client side.
        assertEquals(0, deviceTestManger.getStatusError());
    }

    @Test(timeout = TEST_TIMEOUT_MILLISECONDS)
    @ContinuousIntegrationTest
    @Ignore
    public void mixScheduleInFutureSucceed() throws InterruptedException
    {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(MAX_NUMBER_JOBS);

        DeviceTestManager deviceTestManger = devices.get(0);
        final String deviceId = testDevice.getDeviceId();
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        final Date future = new Date(new Date().getTime() + 10000L); // 10 seconds in the future.

        final ConcurrentMap<String, Exception> jobExceptions = new ConcurrentHashMap<>();
        final ConcurrentMap<String, ScheduledJob> jobResults = new ConcurrentHashMap<>();
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
                        DirectMethodsJobOptions options =
                            DirectMethodsJobOptions.builder()
                                .payload(PAYLOAD_STRING)
                                .methodConnectTimeout(CONNECTION_TIMEOUT)
                                .methodResponseTimeout(RESPONSE_TIMEOUT)
                                .maxExecutionTimeInSeconds(MAX_EXECUTION_TIME_IN_SECONDS)
                                .build();

                        jobClient.scheduleDirectMethod(jobId, queryCondition, DeviceEmulator.METHOD_LOOPBACK, future, options);
                    }
                    else
                    {
                        Twin twin = new Twin(deviceId);
                        Set<Pair> testDesProp = new HashSet<>();
                        testDesProp.add(new Pair(STANDARD_PROPERTY_HOMETEMP, jobTemperature));
                        twin.setDesiredProperties(testDesProp);
                        twinExpectedTemperature.put(jobId, jobTemperature);

                        jobClient.scheduleUpdateTwin(
                            jobId, queryCondition,
                            twin,
                            new Date(), MAX_EXECUTION_TIME_IN_SECONDS);
                    }
                    ScheduledJob job = jobClient.get(jobId);
                    while (job.getJobStatus() != ScheduledJobStatus.completed)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        job = jobClient.get(jobId);
                    }
                    job = queryDeviceJobResult(jobId,
                        ((index % 2 == 0) ? ScheduledJobType.scheduleDeviceMethod : ScheduledJobType.scheduleUpdateTwin),
                        ScheduledJobStatus.completed);
                    jobResults.put(jobId, job);
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
        for (Map.Entry<String, ScheduledJob> job : jobResults.entrySet())
        {
            ScheduledJob jobResult = job.getValue();
            String jobId = jobResult.getJobId();
            assertNotNull(jobResult);
            if (jobResult.getJobType() == ScheduledJobType.scheduleDeviceMethod)
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
    public void cancelScheduleDeviceMethodSucceed() throws InterruptedException
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
                    DirectMethodsJobOptions options =
                        DirectMethodsJobOptions.builder()
                            .payload(PAYLOAD_STRING)
                            .methodConnectTimeout(CONNECTION_TIMEOUT)
                            .methodResponseTimeout(RESPONSE_TIMEOUT)
                            .maxExecutionTimeInSeconds(MAX_EXECUTION_TIME_IN_SECONDS)
                            .build();

                    jobClient.scheduleDirectMethod(jobId, queryCondition, DeviceEmulator.METHOD_LOOPBACK, (index % 2 == 0) ? future : new Date(), options);

                    ScheduledJobStatus expectedJobStatus = ScheduledJobStatus.completed;
                    if (index % 2 == 0)
                    {
                        expectedJobStatus = ScheduledJobStatus.cancelled;
                        Thread.sleep(1000); // wait 1 seconds and cancel.
                        jobClient.cancel(jobId);
                    }

                    ScheduledJob job = jobClient.get(jobId);
                    while (job.getJobStatus() != expectedJobStatus)
                    {
                        Thread.sleep(MAXIMUM_TIME_TO_WAIT_FOR_IOTHUB_MILLISECONDS);
                        job = jobClient.get(jobId);
                    }
                    log.info("Iothub confirmed {} {} for type {}", jobId, expectedJobStatus, ScheduledJobType.scheduleDeviceMethod);
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

    private static ScheduledJobsClient buildJobClientWithAzureSasCredential()
    {
        IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
        IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
        AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
        ScheduledJobsClientOptions options = ScheduledJobsClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
        return new ScheduledJobsClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
    }

    private static ScheduledJob queryDeviceJobResult(String jobId, ScheduledJobType jobType, ScheduledJobStatus jobStatus) throws IOException, IotHubException
    {
        QueryClient queryClient = new QueryClient(iotHubConnectionString);
        String queryContent = SqlQueryBuilder.createSqlQuery("*", SqlQueryBuilder.FromType.JOBS,
            "devices.jobs.jobId = '" + jobId + "' and devices.jobs.jobType = '" + jobType.toString() + "'",
            null);

        JobQueryResponse jobQueryResponse = queryClient.queryJobs(queryContent);

        while (jobQueryResponse.hasNext())
        {
            ScheduledJob job = jobQueryResponse.next();
            if (job.getJobId().equals(jobId))
            {
                if (job.getJobType() == jobType)
                {
                    if (job.getJobStatus() == jobStatus)
                    {
                        //query confirmed that the specified job has the correct type, and status
                        return job;
                    }
                    else
                    {
                        throw new AssertionError("queryDeviceJob received job unexpected status. Expected " + jobStatus + " but job ended with status " + job.getJobStatus());
                    }
                }
                else
                {
                    throw new AssertionError("queryDeviceJob received job with the wrong job type. Expected " + jobType + " but found " + job.getJobType());
                }
            }
        }

        throw new AssertionError("queryDeviceJob did not find the job");
    }
}
