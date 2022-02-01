// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.query.JobsQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.Twin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobClientOptions;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;
import com.microsoft.azure.sdk.iot.service.query.QueryPageOptions;
import com.microsoft.azure.sdk.iot.service.query.TwinsQueryResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.BeforeClass;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static junit.framework.TestCase.*;

@Slf4j
public class QueryClientTests extends IntegrationTest
{
    protected static String iotHubConnectionString = "";
    protected static String hostName;
    protected static RegistryManager registryManager;
    protected static JobClient jobClient;

    @BeforeClass
    public static void setup()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = new RegistryManager(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        jobClient = new JobClient(iotHubConnectionString, JobClientOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        hostName = IotHubConnectionStringBuilder.createIotHubConnectionString(iotHubConnectionString).getHostName();
    }

    @Test
    public void testQueryTwins() throws IOException, IotHubException, InterruptedException
    {
        String deviceId1 = UUID.randomUUID().toString();
        String deviceId2 = UUID.randomUUID().toString();

        registryManager.addDevice(Device.createDevice(deviceId1, AuthenticationType.SAS));
        registryManager.addDevice(Device.createDevice(deviceId2, AuthenticationType.SAS));

        Thread.sleep(2000);

        QueryClient queryClient = new QueryClient(iotHubConnectionString);
        String twinQueryString = "SELECT * FROM devices WHERE deviceId IN ['" + deviceId1 + "', '" + deviceId2 + "']";

        // force 1 result per page in order to test pagination
        QueryPageOptions queryPageOptions = QueryPageOptions.builder().pageSize(1).build();
        TwinsQueryResponse twinsQueryResponse = queryClient.queryTwins(twinQueryString, queryPageOptions);

        List<Twin> twinList = new ArrayList<>();
        assertTrue(twinsQueryResponse.hasNext());

        // query should have 2 results, and since the first page only had 1 result, there should be a continuation token present
        assertNotNull(twinsQueryResponse.getContinuationToken());
        twinList.add(twinsQueryResponse.next());

        assertTrue(twinsQueryResponse.hasNext());
        twinList.add(twinsQueryResponse.next());

        // query should only have 2 results, and since the both results have been queried, there should be no more continuation tokens
        assertNull(twinsQueryResponse.getContinuationToken());
        assertFalse(twinsQueryResponse.hasNext());

        assertEquals(2, twinList.size());
        assertTrue(twinList.get(0).getDeviceId().equals(deviceId1) || twinList.get(0).getDeviceId().equals(deviceId2));
        assertTrue(twinList.get(1).getDeviceId().equals(deviceId1) || twinList.get(1).getDeviceId().equals(deviceId2));
    }

    @Test
    public void testQueryJobs() throws IOException, IotHubException, InterruptedException
    {
        String jobId = UUID.randomUUID().toString();

        String deviceId = UUID.randomUUID().toString();

        registryManager.addDevice(Device.createDevice(deviceId, AuthenticationType.SAS));

        Thread.sleep(2000);

        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        Twin twinUpdate = new Twin();
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("key", "value"));
        twinUpdate.setDesiredProperties(desiredProperties);
        Date dateThreeMinutesInFuture = new Date(System.currentTimeMillis() + (1000 * 60 * 3));
        jobClient.scheduleUpdateTwin(jobId, queryCondition, twinUpdate, dateThreeMinutesInFuture, 100);

        QueryClient queryClient = new QueryClient(iotHubConnectionString);
        String query = "SELECT * FROM devices.jobs WHERE devices.jobs.deviceId = '" + deviceId + "'";
        JobsQueryResponse response = queryClient.queryJobs(query);
        int a = 1;
    }

    @Test
    public void testQueryJobsByType() throws IOException, IotHubException, InterruptedException
    {
        String jobId = UUID.randomUUID().toString();

        String deviceId = UUID.randomUUID().toString();

        registryManager.addDevice(Device.createDevice(deviceId, AuthenticationType.SAS));

        Thread.sleep(2000);

        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        Twin twinUpdate = new Twin();
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("key", "value"));
        twinUpdate.setDesiredProperties(desiredProperties);
        Date dateThreeMinutesInFuture = new Date(System.currentTimeMillis() + (1000 * 60 * 3));
        jobClient.scheduleUpdateTwin(jobId, queryCondition, twinUpdate, dateThreeMinutesInFuture, 100);

        QueryClient queryClient = new QueryClient(iotHubConnectionString);
        JobsQueryResponse response = queryClient.queryJobs(JobType.scheduleUpdateTwin, JobStatus.enqueued);
        int a = 1;
    }
}
