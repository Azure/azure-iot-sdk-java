// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.azure.core.credential.AzureSasCredential;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobsClient;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.registry.RegistryManager;
import com.microsoft.azure.sdk.iot.service.registry.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubTooManyRequestsException;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobsClientOptions;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.Job;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.JobType;
import com.microsoft.azure.sdk.iot.service.query.JobQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.query.QueryClientOptions;
import com.microsoft.azure.sdk.iot.service.query.QueryPageOptions;
import com.microsoft.azure.sdk.iot.service.query.RawQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.SqlQuery;
import com.microsoft.azure.sdk.iot.service.query.TwinQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.*;
import static tests.integration.com.microsoft.azure.sdk.iot.iothub.setup.TwinCommon.DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS;

@Slf4j
@IotHubTest
public class QueryClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    protected static String hostName;
    protected static RegistryManager registryManager;
    protected static ScheduledJobsClient jobClient;

    private static final int QUERY_TIMEOUT_MILLISECONDS = 1000 * 60; // 1 minute
    protected static final String PROPERTY_KEY_QUERY = "KeyQuery";
    protected static final String PROPERTY_VALUE_QUERY = "ValueQuery";

    @BeforeClass
    public static void setup()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));
        registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        jobClient = new ScheduledJobsClient(iotHubConnectionString, ScheduledJobsClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
    }

    @Test
    public void testQueryTwins() throws IOException, IotHubException, InterruptedException
    {
        String deviceId1 = UUID.randomUUID().toString();
        String deviceId2 = UUID.randomUUID().toString();
        Device device1 = new Device(deviceId1, AuthenticationType.SAS);
        Device device2 = new Device(deviceId2, AuthenticationType.SAS);

        registryManager.addDevice(device1);
        registryManager.addDevice(device2);

        try
        {
            Thread.sleep(2000);

            IotHubConnectionString iotHubConnectionStringObj = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString);
            IotHubServiceSasToken serviceSasToken = new IotHubServiceSasToken(iotHubConnectionStringObj);
            AzureSasCredential azureSasCredential = new AzureSasCredential(serviceSasToken.toString());
            QueryClientOptions options = QueryClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build();
            QueryClient queryClient = new QueryClient(iotHubConnectionStringObj.getHostName(), azureSasCredential, options);
            String twinQueryString = "SELECT * FROM devices WHERE deviceId IN ['" + deviceId1 + "', '" + deviceId2 + "']";

            // force 1 result per page in order to test pagination
            QueryPageOptions queryPageOptions = QueryPageOptions.builder().pageSize(1).build();
            TwinQueryResponse twinQueryResponse = queryClient.queryTwins(twinQueryString, queryPageOptions);

            List<Twin> twinList = new ArrayList<>();
            assertTrue(twinQueryResponse.hasNext());

            // query should have 2 results, and since the first page only had 1 result, there should be a continuation token present
            assertNotNull(twinQueryResponse.getContinuationToken());
            twinList.add(twinQueryResponse.next());

            assertTrue(twinQueryResponse.hasNext());
            twinList.add(twinQueryResponse.next());

            // query should only have 2 results, and since the both results have been queried, there should be no more continuation tokens
            assertNull(twinQueryResponse.getContinuationToken());
            assertFalse(twinQueryResponse.hasNext());

            assertEquals(2, twinList.size());
            assertTrue(twinList.get(0).getDeviceId().equals(deviceId1) || twinList.get(0).getDeviceId().equals(deviceId2));
            assertTrue(twinList.get(1).getDeviceId().equals(deviceId1) || twinList.get(1).getDeviceId().equals(deviceId2));
        }
        finally
        {
            try
            {
                registryManager.removeDevice(deviceId1);
                registryManager.removeDevice(deviceId2);
            }
            catch (IOException | IotHubException e)
            {
                log.debug("Failed to clean up devices after test");
            }
        }
    }

    @Test
    public void testQueryJobs() throws IOException, IotHubException, InterruptedException
    {
        String jobId = UUID.randomUUID().toString();

        String deviceId = UUID.randomUUID().toString();

        registryManager.addDevice(new Device(deviceId, AuthenticationType.SAS));

        try
        {
            Thread.sleep(2000);

            final String queryCondition = "DeviceId IN ['" + deviceId + "']";
            Twin twinUpdate = new Twin();
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair("key", "value"));
            twinUpdate.setDesiredProperties(desiredProperties);
            Date dateThreeMinutesInFuture = new Date(System.currentTimeMillis() + (1000 * 60 * 3));

            try
            {
                jobClient.scheduleUpdateTwin(jobId, queryCondition, twinUpdate, dateThreeMinutesInFuture, 100);
            }
            catch (IotHubTooManyRequestsException e)
            {
                log.info("Throttled when creating job. Will use existing job(s) to test query");
            }

            QueryClient queryClient = new QueryClient(iotHubConnectionString);

            String query = SqlQuery.createSqlQuery("*", SqlQuery.FromType.JOBS, null,null).getQuery();

            JobQueryResponse response = queryClient.queryJobs(query);
            long startTime = System.currentTimeMillis();
            while (!response.hasNext())
            {
                if (System.currentTimeMillis() - startTime > QUERY_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for the expected query response");
                }

                Thread.sleep(2000);

                response = queryClient.queryJobs(query);
            }

            Job job = response.next();

            assertNotNull(job.getJobId());
            assertNotNull(job.getJobType());
            assertNotNull(job.getStartTime());
            assertNotNull(job.getEndTime());
            assertNotNull(job.getCreatedTime());
            assertNotNull(job.getLastUpdatedDateTime());
            assertNotNull(job.getJobStatus());
            assertNotNull(job.getDeviceId());
        }
        finally
        {
            try
            {
                registryManager.removeDevice(deviceId);
            }
            catch (IOException | IotHubException e)
            {
                log.debug("Failed to clean up devices after test");
            }
        }
    }

    @Test
    public void testQueryJobsByType() throws IOException, IotHubException, InterruptedException
    {
        String jobId = UUID.randomUUID().toString();

        String deviceId = UUID.randomUUID().toString();

        registryManager.addDevice(new Device(deviceId, AuthenticationType.SAS));

        try
        {
            Thread.sleep(2000);

            final String queryCondition = "DeviceId IN ['" + deviceId + "']";
            Twin twinUpdate = new Twin();
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair("key", "value"));
            twinUpdate.setDesiredProperties(desiredProperties);
            Date now = new Date(System.currentTimeMillis());
            try
            {
                jobClient.scheduleUpdateTwin(jobId, queryCondition, twinUpdate, now, 10);
            }
            catch (IotHubTooManyRequestsException e)
            {
                log.info("Throttled when creating job. Will use existing job(s) to test query");
            }

            QueryClient queryClient = new QueryClient(iotHubConnectionString);
            JobQueryResponse response = queryClient.queryJobs(JobType.scheduleUpdateTwin, JobStatus.completed);

            long startTime = System.currentTimeMillis();
            while (!response.hasNext())
            {
                if (System.currentTimeMillis() - startTime > QUERY_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for the expected query response");
                }

                Thread.sleep(2000);

                response = queryClient.queryJobs(JobType.scheduleUpdateTwin, JobStatus.enqueued);
            }

            Job job = response.next();

            assertNotNull(job.getJobId());
            assertNotNull(job.getJobType());
            assertNotNull(job.getStartTime());
            assertNotNull(job.getEndTime());
            assertNotNull(job.getCreatedTime());
            assertNotNull(job.getMaxExecutionTimeInSeconds());
            assertNotNull(job.getJobStatus());
            assertNotNull(job.getJobStatistics());
            assertTrue(job.getJobStatistics().getDeviceCount() > 0);
        }
        finally
        {
            try
            {
                registryManager.removeDevice(deviceId);
            }
            catch (IOException | IotHubException e)
            {
                log.debug("Failed to clean up devices after test");
            }
        }
    }

    @Test
    public void testRawQuery() throws InterruptedException, IOException, IotHubException
    {
        int deviceCount = 4;
        Device[] devices = new Device[deviceCount];
        for (int i = 0; i < deviceCount; i++)
        {
            devices[i] = registryManager.addDevice(new Device(UUID.randomUUID().toString(), AuthenticationType.SAS));
        }

        try
        {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();

            // Add same desired on multiple devices
            final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
            final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();

            TwinClient twinClient = new TwinClient(iotHubConnectionString);
            for (int i = 0; i < deviceCount; i++)
            {
                Twin twin = twinClient.getTwin(devices[i].getDeviceId());
                Set<Pair> desiredProperties = twin.getDesiredProperties();
                desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
                twin.setDesiredProperties(desiredProperties);
                twinClient.updateTwin(twin);
            }

            Thread.sleep(DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS);

            // Raw Query for multiple devices having same property
            final String select = "properties.desired." + queryProperty + " AS " + queryProperty + "," + " COUNT() AS numberOfDevices";
            final String groupBy = "properties.desired." + queryProperty;
            final String sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy).getQuery();

            boolean querySucceeded = false;
            long startTime = System.currentTimeMillis();
            while (!querySucceeded)
            {
                QueryClient queryClient = new QueryClient(iotHubConnectionString);
                RawQueryResponse rawQueryResponse = queryClient.queryRaw(sqlQuery);
                while (rawQueryResponse.hasNext())
                {
                    String result = rawQueryResponse.next();
                    assertNotNull(result);
                    Map map = gson.fromJson(result, Map.class);
                    if (map.containsKey("numberOfDevices") && map.containsKey(queryProperty))
                    {
                        // Casting as a double first to get the value from the map, but then casting to an int because the
                        // number of devices should always be an integer
                        int actualNumberOfDevices = (int) (double) map.get("numberOfDevices");
                        if (actualNumberOfDevices == deviceCount)
                        {
                            // Due to propagation delays, there will be times when the query is executed and only a
                            // subset of the expected devices are queryable. This test will loop until all of them are queryable
                            // to avoid this issue.
                            querySucceeded = true;
                        }
                        else
                        {
                            log.info("Expected device count not correct, re-running query");
                            Thread.sleep(200);
                            rawQueryResponse = queryClient.queryRaw(sqlQuery);
                        }
                    }
                }

                if (System.currentTimeMillis() - startTime > QUERY_TIMEOUT_MILLISECONDS)
                {
                    fail("Timed out waiting for query results to match expectations");
                }
            }
        }
        finally
        {
            try
            {
                for (int i = 0; i < deviceCount; i++)
                {
                    registryManager.removeDevice(devices[i]);
                }
            }
            catch (IOException | IotHubException e)
            {
                log.debug("Failed to clean up devices after test");
            }
        }
    }
}
