// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul;

import com.microsoft.azure.sdk.iot.device.MultiplexingClient;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import samples.com.microsoft.azure.sdk.iot.MultiplexingClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ErrorInjectionHelper;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.ConnectionStatusChangeTracker;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.FaultInjectionManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.MultiplexingClientLonghaulTestAddOn;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl.MultiplexingClientDeviceToCloudTelemetryAddOn;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

@Slf4j
@RunWith(Parameterized.class)
public class MultiplexingClientLonghaulTests extends LonghaulTests
{
    private static final int MULTIPLEXED_DEVICE_COUNT = 5;

    @Parameterized.Parameter()
    public TestParameters testParameters;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return asList(new Object[][]{
            {new TestParameters(AMQPS, AuthenticationType.SAS)},
            {new TestParameters(AMQPS_WS, AuthenticationType.SAS)},
        });
    }

    @Test
    public void testMultiplexedDeviceToCloudTelemetry() throws Exception
    {
        MultiplexingClientLonghaulTestAddOn testDeviceToCloudTelemetry =
            new MultiplexingClientDeviceToCloudTelemetryAddOn(MULTIPLEXED_DEVICE_COUNT);

        multiplexingClientLonghaulTestBase(testDeviceToCloudTelemetry, 1, false, testParameters);
    }

    @Test
    public void testMultiplexedDeviceToCloudTelemetryWithFaultInjection() throws Exception
    {
        MultiplexingClientLonghaulTestAddOn testDeviceToCloudTelemetry =
            new MultiplexingClientDeviceToCloudTelemetryAddOn(MULTIPLEXED_DEVICE_COUNT);

        multiplexingClientLonghaulTestBase(testDeviceToCloudTelemetry, 1, true, testParameters);
    }


    public void multiplexingClientLonghaulTestBase(
        MultiplexingClientLonghaulTestAddOn longhaulTestAddOn,
        int testOperationsPerInterval,
        boolean withRandomFaultInjection,
        TestParameters testParameters) throws Exception
    {
        String iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);
        ServiceClient serviceClient = new ServiceClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);

        String hostName = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString).getHostName();
        MultiplexingClient multiplexingClient = new MultiplexingClient(hostName, testParameters.getProtocol());

        MultiplexingClientManager multiplexingClientManager = new MultiplexingClientManager(multiplexingClient, UUID.randomUUID().toString());

        ConnectionStatusChangeTracker multiplexingClientConnectionStatusTracker = new ConnectionStatusChangeTracker();

        ConnectionStatusChangeTracker[] multiplexedClientsConnectionStatusTrackers = new ConnectionStatusChangeTracker[MULTIPLEXED_DEVICE_COUNT];
        TestDeviceIdentity[] testDeviceIdentities = new TestDeviceIdentity[MULTIPLEXED_DEVICE_COUNT];
        DeviceClientManager[] deviceClientManagers = new DeviceClientManager[MULTIPLEXED_DEVICE_COUNT];
        for (int i = 0; i < MULTIPLEXED_DEVICE_COUNT; i++)
        {
            testDeviceIdentities[i] = Tools.getTestDevice(iotHubConnectionString, testParameters.getProtocol(), AuthenticationType.SAS, false);
            deviceClientManagers[i] = new DeviceClientManager(testDeviceIdentities[i].getDeviceClient());
            multiplexedClientsConnectionStatusTrackers[i] = new ConnectionStatusChangeTracker(testDeviceIdentities[i].getDeviceId());
            deviceClientManagers[i].registerConnectionStatusChangeCallback(multiplexedClientsConnectionStatusTrackers[i], null);
            multiplexingClientManager.registerDeviceClient(testDeviceIdentities[i].getDeviceClient());
        }

        multiplexingClientManager.registerConnectionStatusChangeCallback(multiplexingClientConnectionStatusTracker, null);

        longhaulTestAddOn.setupClientBeforeOpen(multiplexingClientManager, deviceClientManagers, serviceClient, registryManager, testParameters);

        multiplexingClientManager.open();

        longhaulTestAddOn.setupClientAfterOpen(multiplexingClientManager, deviceClientManagers, serviceClient, registryManager, testParameters);

        FaultInjectionManager[] faultInjectionManagers = null;
        if (withRandomFaultInjection)
        {
            faultInjectionManagers = new FaultInjectionManager[MULTIPLEXED_DEVICE_COUNT];
            for (int i = 0; i < MULTIPLEXED_DEVICE_COUNT; i++)
            {
                faultInjectionManagers[i] = new FaultInjectionManager(deviceClientManagers[i], ErrorInjectionHelper.FaultType_AmqpSess, ErrorInjectionHelper.FaultCloseReason_Boom);
                faultInjectionManagers[i].startRandomFaultInjection();
            }
        }

        int failedTestableActionsCount = 0;
        Instant startTime = Instant.now();
        Duration testRunTimeSpan = Duration.between(startTime, Instant.now());
        while (testRunTimeSpan.toHours() < LONGHAUL_TEST_LENGTH_HOURS)
        {
            for (int i = 0; i < testOperationsPerInterval; i++)
            {
                try
                {
                    longhaulTestAddOn.performPeriodicTestableAction(multiplexingClientManager, deviceClientManagers, serviceClient, registryManager, testParameters);
                }
                catch (Exception e)
                {
                    log.error("Failed a testable action", e);
                    failedTestableActionsCount++;
                }
            }

            TimeUnit.SECONDS.sleep(SLEEP_PERIOD_IN_SECONDS);

            testRunTimeSpan = Duration.between(startTime, Instant.now());

            for (int i = 0; i < MULTIPLEXED_DEVICE_COUNT; i++)
            {
                multiplexedClientsConnectionStatusTrackers[i].refreshConnectionStatistics();
            }

            multiplexingClientConnectionStatusTracker.refreshConnectionStatistics();

            log.info("Longhaul test has run for {} hours", testRunTimeSpan.toHours());
            log.info("Longhaul test will run for {} more hours", LONGHAUL_TEST_LENGTH_HOURS - testRunTimeSpan.toHours());
            log.info("The current time is {}", new Date());
            for (int i = 0; i < MULTIPLEXED_DEVICE_COUNT; i++)
            {
                log.info("Device id {} is {}", i, testDeviceIdentities[i].getDeviceId());
                multiplexedClientsConnectionStatusTrackers[i].logCurrentStatus();
            }
            multiplexingClientConnectionStatusTracker.logCurrentStatus();

            longhaulTestAddOn.performPeriodicStatusReport(testParameters);
        }

        if (withRandomFaultInjection)
        {
            for (int i = 0; i < MULTIPLEXED_DEVICE_COUNT; i++)
            {
                faultInjectionManagers[i].stopRandomFaultInjection();
            }
        }

        multiplexingClientManager.closeNow();

        boolean testPassed = true;
        if (failedTestableActionsCount > 0)
        {
            testPassed = false;
            log.error("One or more testable actions failed. See logs for more details.");
        }

        testPassed &= longhaulTestAddOn.validateExpectations(testParameters);

        Tools.disposeTestIdentities(Arrays.asList(testDeviceIdentities), iotHubConnectionString);

        if (!testPassed)
        {
            fail("One or more tested functionalities failed during the test. See logs for more details.");
        }
    }
}
