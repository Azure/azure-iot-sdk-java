// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import samples.com.microsoft.azure.sdk.iot.DeviceClientManager;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ErrorInjectionHelper;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestDeviceIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.ConnectionStatusChangeTracker;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.FaultInjectionManager;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.LonghaulTests;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.TestParameters;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.DeviceClientLonghaulTestAddOn;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl.DeviceClientCloudToDeviceTelemetryAddOn;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl.DeviceClientLonghaulTestAddOnCollection;
import tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers.callbacks.impl.DeviceClientDeviceToCloudTelemetryAddOn;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

@Slf4j
@RunWith(Parameterized.class)
public class DeviceClientLonghaulTests extends LonghaulTests
{
    @Parameterized.Parameter()
    public TestParameters testParameters;

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data()
    {
        return asList(new Object[][]{
            {new TestParameters(HTTPS, AuthenticationType.SAS)},
            {new TestParameters(MQTT, AuthenticationType.SAS)},
            {new TestParameters(MQTT_WS, AuthenticationType.SAS)},
            {new TestParameters(AMQPS, AuthenticationType.SAS)},
            {new TestParameters(AMQPS_WS, AuthenticationType.SAS)},

            {new TestParameters(HTTPS, AuthenticationType.SELF_SIGNED)},
            {new TestParameters(MQTT, AuthenticationType.SELF_SIGNED)},
            {new TestParameters(MQTT_WS, AuthenticationType.SELF_SIGNED)},
            {new TestParameters(AMQPS, AuthenticationType.SELF_SIGNED)},
            //{new TestParameters(AMQPS_WS, AuthenticationType.SELF_SIGNED)}, // not supported by the SDK yet
        });
    }

    @Test
    public void testDeviceToCloudTelemetry() throws Exception
    {
        DeviceClientLonghaulTestAddOn testDeviceToCloudTelemetry = new DeviceClientDeviceToCloudTelemetryAddOn();

        deviceClientLonghaulTestBase(testDeviceToCloudTelemetry, 1, false, testParameters);
    }

    @Test
    public void testCloudToDeviceTelemetry() throws Exception
    {
        DeviceClientLonghaulTestAddOn testCloudToDeviceTelemetry = new DeviceClientCloudToDeviceTelemetryAddOn();
        deviceClientLonghaulTestBase(testCloudToDeviceTelemetry, 1, false, testParameters);
    }

    @Test
    public void testCloudToDeviceAndDeviceToCloudTelemetry() throws Exception
    {
        DeviceClientLonghaulTestAddOn testCloudToDeviceTelemetryAndCloudToDeviceTelemetry =
            new DeviceClientLonghaulTestAddOnCollection(
                new DeviceClientDeviceToCloudTelemetryAddOn(),
                new DeviceClientCloudToDeviceTelemetryAddOn());

        deviceClientLonghaulTestBase(testCloudToDeviceTelemetryAndCloudToDeviceTelemetry, 1, false, testParameters);
    }

    @Test
    public void testDeviceToCloudTelemetryWithRandomFaultInjection() throws Exception
    {
        if (testParameters.getProtocol() == HTTPS)
        {
            log.info("Skipping fault injection test since HTTPS clients cannot be faulted");
            return;
        }

        DeviceClientLonghaulTestAddOn testDeviceToCloudTelemetry = new DeviceClientDeviceToCloudTelemetryAddOn();

        deviceClientLonghaulTestBase(testDeviceToCloudTelemetry, 1, true, testParameters);
    }

    public static void deviceClientLonghaulTestBase(
        DeviceClientLonghaulTestAddOn longhaulTestAddOn,
        int testOperationsPerInterval,
        boolean withRandomFaultInjection,
        TestParameters testParameters) throws Exception
    {
        // Build all the required device and service clients
        String iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);
        ServiceClient serviceClient = new ServiceClient(iotHubConnectionString, IotHubServiceClientProtocol.AMQPS);

        TestDeviceIdentity testDevice = Tools.getTestDevice(iotHubConnectionString, testParameters.getProtocol(), testParameters.getAuthenticationType(), false);

        DeviceClientManager deviceClientManager = new DeviceClientManager((DeviceClient) testDevice.getClient());

        // Setup connection status change trackers on the client under test
        ConnectionStatusChangeTracker connectionStatusChangeTracker = new ConnectionStatusChangeTracker(testDevice.getDeviceId());
        deviceClientManager.registerConnectionStatusChangeCallback(connectionStatusChangeTracker, null);

        longhaulTestAddOn.setupClientBeforeOpen(deviceClientManager, serviceClient, registryManager, testParameters);

        deviceClientManager.open();

        longhaulTestAddOn.setupClientAfterOpen(deviceClientManager, serviceClient, registryManager, testParameters);

        // Start up random fault injection if requested
        FaultInjectionManager faultInjectionManager = null;
        if (withRandomFaultInjection)
        {
            faultInjectionManager = new FaultInjectionManager(deviceClientManager, ErrorInjectionHelper.FaultType_Tcp, ErrorInjectionHelper.FaultCloseReason_Boom);
            faultInjectionManager.startRandomFaultInjection();
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
                    longhaulTestAddOn.performPeriodicTestableAction(deviceClientManager, serviceClient, registryManager, testParameters);
                }
                catch (Exception e)
                {
                    log.error("Failed a testable action", e);
                    failedTestableActionsCount++;
                }
            }

            TimeUnit.SECONDS.sleep(SLEEP_PERIOD_IN_SECONDS);

            testRunTimeSpan = Duration.between(startTime, Instant.now());

            connectionStatusChangeTracker.refreshConnectionStatistics();

            log.info("Longhaul test has run for {} hours", testRunTimeSpan.toHours());
            log.info("Longhaul test will run for {} more hours", LONGHAUL_TEST_LENGTH_HOURS - testRunTimeSpan.toHours());
            log.info("The current time is {}", new Date());
            log.info("Device id is {}", testDevice.getDeviceId());
            connectionStatusChangeTracker.logCurrentStatus();
            longhaulTestAddOn.performPeriodicStatusReport(testParameters);
        }

        if (faultInjectionManager != null)
        {
            faultInjectionManager.stopRandomFaultInjection();
        }

        testDevice.getClient().closeNow();

        boolean testPassed = true;
        if (failedTestableActionsCount > 0)
        {
            testPassed = false;
            log.error("One or more testable actions failed. See logs for more details.");
        }

        testPassed &= longhaulTestAddOn.validateExpectations(testParameters);

        Tools.disposeTestIdentity(testDevice, iotHubConnectionString);

        if (!testPassed)
        {
            fail("One or more tested functionalities failed during the test. See logs for more details.");
        }
    }
}
