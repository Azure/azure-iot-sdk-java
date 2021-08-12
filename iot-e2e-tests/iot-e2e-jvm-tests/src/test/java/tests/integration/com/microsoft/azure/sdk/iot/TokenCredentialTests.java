// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.BasicDigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.rest.ServiceResponseWithHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.MQTT;
import static junit.framework.TestCase.fail;
import static org.junit.Assert.*;
import static tests.integration.com.microsoft.azure.sdk.iot.Tools.*;

/**
 * Tests within this class test out the TokenCredential constructors in service clients. These tests are only run
 * on Windows and Linux at the gate because the Azure Identity library that these tests use to generate AAD authentication
 * tokens does not work on Android. In the future, these tests may be moved back to the common folder if a library
 * can be found to replace Azure Identity that works for android as well.
 */
@Slf4j
public class TokenCredentialTests
{
    private static final String THERMOSTAT_MODEL_ID = "dtmi:com:example:Thermostat;1";

    private static final int METHOD_SUBSCRIPTION_TIMEOUT_MILLISECONDS = 60 * 1000;

    @Test
    public void cloudToDeviceTelemetryWithTokenCredential() throws Exception
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        // We remove and recreate the device for a clean start
        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);

        Device device = Device.createDevice("some-device-" + UUID.randomUUID(), AuthenticationType.SAS);
        registryManager.addDevice(device);

        Device deviceGetBefore = registryManager.getDevice(device.getDeviceId());

        // Create service client
        ServiceClient serviceClient = buildServiceClientWithTokenCredential(IotHubServiceClientProtocol.AMQPS);
        serviceClient.open();

        Message message = new Message("some message".getBytes());

        serviceClient.send(device.getDeviceId(), message);

        Device deviceGetAfter = registryManager.getDevice(device.getDeviceId());
        serviceClient.close();

        registryManager.removeDevice(device.getDeviceId());

        // Assert
        assertEquals(0, deviceGetBefore.getCloudToDeviceMessageCount());
        assertEquals(1, deviceGetAfter.getCloudToDeviceMessageCount());

        registryManager.close();
    }

    @Test
    public void getDigitalTwinWithTokenCredential() throws IOException, IotHubException, URISyntaxException
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);
        DeviceClient deviceClient = createDeviceClient(MQTT, registryManager);
        deviceClient.open();

        // arrange
        DigitalTwinClient digitalTwinClient = buildDigitalTwinClientWithTokenCredential();

        // act
        BasicDigitalTwin response = digitalTwinClient.getDigitalTwin(deviceClient.getConfig().getDeviceId(), BasicDigitalTwin.class);
        ServiceResponseWithHeaders<BasicDigitalTwin, DigitalTwinGetHeaders> responseWithHeaders =
            digitalTwinClient.getDigitalTwinWithResponse(deviceClient.getConfig().getDeviceId(), BasicDigitalTwin.class);

        // assert
        assertEquals(response.getMetadata().getModelId(), THERMOSTAT_MODEL_ID);
        assertEquals(responseWithHeaders.body().getMetadata().getModelId(), THERMOSTAT_MODEL_ID);
    }

    @Test
    public void deviceLifecycleWithTokenCredential() throws Exception
    {
        //-Create-//
        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);
        String deviceId = "some-device-" + UUID.randomUUID();
        Device deviceAdded = Device.createFromId(deviceId, DeviceStatus.Enabled, null);
        registryManager.addDevice(deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryManager.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryManager.getDevice(deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryManager.updateDevice(deviceUpdated);

        //-Delete-//
        registryManager.removeDevice(deviceId);

        // Assert
        assertEquals(deviceId, deviceAdded.getDeviceId());
        assertEquals(deviceId, deviceRetrieved.getDeviceId());
        assertEquals(DeviceStatus.Disabled, deviceUpdated.getStatus());
    }

    @Test
    public void invokeMethodSucceedWithTokenCredential() throws Exception
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        DeviceMethod methodServiceClient = buildDeviceMethodClientWithTokenCredential();

        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);
        Device device = Device.createDevice("some-device-" + UUID.randomUUID(), AuthenticationType.SAS);
        registryManager.addDevice(device);

        DeviceClient deviceClient = new DeviceClient(registryManager.getDeviceConnectionString(device), MQTT);
        deviceClient.open();
        final int successStatusCode = 200;
        final AtomicBoolean methodsSubscriptionComplete = new AtomicBoolean(false);
        final AtomicBoolean methodsSubscribedSuccessfully = new AtomicBoolean(false);
        deviceClient.subscribeToMethodsAsync(
            (methodName, methodData, context) -> new DeviceMethodData(successStatusCode, "success"),
            null,
            (responseStatus, callbackContext) ->
            {
                if (responseStatus == IotHubStatusCode.OK_EMPTY || responseStatus == IotHubStatusCode.OK)
                {
                    methodsSubscribedSuccessfully.set(true);
                }

                methodsSubscriptionComplete.set(true);
            },
            null
        );

        long startTime = System.currentTimeMillis();
        while (!methodsSubscriptionComplete.get())
        {
            Thread.sleep(200);

            if (System.currentTimeMillis() - startTime > METHOD_SUBSCRIPTION_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for device registration to complete.");
            }
        }

        assertTrue("Method subscription callback fired with non 200 status code", methodsSubscribedSuccessfully.get());

        MethodResult result = methodServiceClient.invoke(
            device.getDeviceId(),
            "someMethod",
            5l,
            5l,
            null);

        assertEquals((long) successStatusCode, (long) result.getStatus());
    }

    @Test
    public void testGetDeviceTwinWithTokenCredential() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        DeviceTwin twinServiceClient = buildDeviceTwinClientWithTokenCredential();

        RegistryManager registryManager = new RegistryManager(iotHubConnectionString);
        Device device = Device.createDevice("some-device-" + UUID.randomUUID(), AuthenticationType.SAS);
        registryManager.addDevice(device);

        DeviceTwinDevice twin = new DeviceTwinDevice(device.getDeviceId());
        twinServiceClient.getTwin(twin);

        assertNotNull(twin.getETag());
    }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol, RegistryManager registryManager) throws IOException, IotHubException, URISyntaxException
    {
        ClientOptions options = new ClientOptions();
        options.setModelId(THERMOSTAT_MODEL_ID);

        String deviceId = "some-device-" + UUID.randomUUID();
        Device device = Device.createDevice(deviceId, AuthenticationType.SAS);
        Device registeredDevice = registryManager.addDevice(device);
        String deviceConnectionString = registryManager.getDeviceConnectionString(registeredDevice);
        return new DeviceClient(deviceConnectionString, protocol, options);
    }
}
