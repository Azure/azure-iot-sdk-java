// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot;

import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.methods.MethodResult;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.digitaltwin.customized.DigitalTwinGetHeaders;
import com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.BasicDigitalTwin;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.query.TwinQueryResponse;
import com.microsoft.rest.ServiceResponseWithHeaders;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
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

    private static final String testPrefix = "provisioningservicecliente2etests-";

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void cloudToDeviceTelemetryWithTokenCredential() throws Exception
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        // We remove and recreate the device for a clean start
        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);

        Device device = new Device("some-device-" + UUID.randomUUID(), AuthenticationType.SAS);
        registryClient.addDevice(device);

        Device deviceGetBefore = registryClient.getDevice(device.getDeviceId());

        // Create service client
        MessagingClient messagingClient = buildServiceClientWithTokenCredential(IotHubServiceClientProtocol.AMQPS);
        messagingClient.open();

        Message message = new Message("some message".getBytes(StandardCharsets.UTF_8));

        messagingClient.send(device.getDeviceId(), message);

        messagingClient.close();

        Device deviceGetAfter = registryClient.getDevice(device.getDeviceId());

        registryClient.removeDevice(device.getDeviceId());

        // Assert
        assertEquals(0, deviceGetBefore.getCloudToDeviceMessageCount());
        assertEquals(1, deviceGetAfter.getCloudToDeviceMessageCount());
    }

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void getDigitalTwinWithTokenCredential() throws IOException, IotHubException, URISyntaxException
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        DeviceClient deviceClient = createDeviceClient(MQTT, registryClient);
        deviceClient.open(false);

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

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void deviceLifecycleWithTokenCredential() throws Exception
    {
        //-Create-//
        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        String deviceId = "some-device-" + UUID.randomUUID();
        Device deviceAdded = new Device(deviceId);
        registryClient.addDevice(deviceAdded);

        //-Read-//
        Device deviceRetrieved = registryClient.getDevice(deviceId);

        //-Update-//
        Device deviceUpdated = registryClient.getDevice(deviceId);
        deviceUpdated.setStatus(DeviceStatus.Disabled);
        deviceUpdated = registryClient.updateDevice(deviceUpdated);

        //-Delete-//
        registryClient.removeDevice(deviceId);

        // Assert
        assertEquals(deviceId, deviceAdded.getDeviceId());
        assertEquals(deviceId, deviceRetrieved.getDeviceId());
        assertEquals(DeviceStatus.Disabled, deviceUpdated.getStatus());
    }

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void invokeMethodSucceedWithTokenCredential() throws Exception
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        DirectMethodsClient methodServiceClient = buildDeviceMethodClientWithTokenCredential();

        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        Device device = new Device("some-device-" + UUID.randomUUID(), AuthenticationType.SAS);
        registryClient.addDevice(device);

        DeviceClient deviceClient = new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, device), MQTT);
        deviceClient.open(false);
        final int successStatusCode = 200;
        final AtomicBoolean methodsSubscriptionComplete = new AtomicBoolean(false);
        final AtomicBoolean methodsSubscribedSuccessfully = new AtomicBoolean(false);
        deviceClient.subscribeToMethodsAsync(
                (methodName, methodData, context) -> new DirectMethodResponse(successStatusCode, "success"),
                null,
                (responseStatus, callbackContext) ->
                {
                    if (responseStatus == IotHubStatusCode.OK)
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

        MethodResult result = methodServiceClient.invoke(device.getDeviceId(), "someMethod");

        assertEquals((long) successStatusCode, (long) result.getStatus());
    }

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void testGetDeviceTwinWithTokenCredential() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        TwinClient twinServiceClient = buildTwinClientWithTokenCredential();

        RegistryClient registryClient = new RegistryClient(iotHubConnectionString);
        Device device = new Device("some-device-" + UUID.randomUUID(), AuthenticationType.SAS);
        registryClient.addDevice(device);

        Twin twin = twinServiceClient.get(device.getDeviceId());

        assertNotNull(twin.getETag());
    }

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void testQueryTwinWithTokenCredential() throws IOException, InterruptedException, IotHubException, GeneralSecurityException, ModuleClientException, URISyntaxException
    {
        Assume.assumeFalse(isBasicTierHub); // only run tests for standard tier hubs

        QueryClient queryClient = buildQueryClientWithTokenCredential();

        TwinQueryResponse twinQueryResponse = queryClient.queryTwins("SELECT * FROM devices");

        // only testing that authentication works, so no need to delve deeper into what the query response contents are
        assertNotNull(twinQueryResponse);
    }

    @Ignore // TODO the service throws a 500 error in Canary environments
    @Test
    public void createIndividualEnrollmentWithTokenCredentialSucceeds() throws ProvisioningServiceClientException
    {
        ProvisioningConnectionString connectionString = ProvisioningConnectionStringBuilder.createConnectionString(provisioningConnectionString);
        TokenCredential credential = buildTokenCredentialFromEnvironment();
        ProvisioningServiceClient provisioningServiceClient1 = new ProvisioningServiceClient(connectionString.getHostName(), credential);

        String registrationId = testPrefix + UUID.randomUUID();
        Attestation attestation = new SymmetricKeyAttestation("", "");
        IndividualEnrollment enrollment = new IndividualEnrollment(registrationId, attestation);
        enrollment.setAllocationPolicy(AllocationPolicy.GEOLATENCY);
        ReprovisionPolicy reprovisionPolicy = new ReprovisionPolicy();
        reprovisionPolicy.setUpdateHubAssignment(true);
        reprovisionPolicy.setMigrateDeviceData(true);
        enrollment.setReprovisionPolicy(reprovisionPolicy);
        DeviceCapabilities capabilities = new DeviceCapabilities();
        capabilities.setIotEdge(true);
        enrollment.setCapabilities(capabilities);
        IndividualEnrollment returnedEnrollment = provisioningServiceClient1.createOrUpdateIndividualEnrollment(enrollment);

        assertEquals(enrollment.getRegistrationId(), returnedEnrollment.getRegistrationId());
        assertEquals(enrollment.getReprovisionPolicy().getMigrateDeviceData(), returnedEnrollment.getReprovisionPolicy().getMigrateDeviceData());
        assertEquals(enrollment.getReprovisionPolicy().getUpdateHubAssignment(), returnedEnrollment.getReprovisionPolicy().getUpdateHubAssignment());
        assertEquals(enrollment.getCapabilities().isIotEdge(), returnedEnrollment.getCapabilities().isIotEdge());
        assertEquals(enrollment.getAttestation().getClass(), returnedEnrollment.getAttestation().getClass());
        assertEquals(enrollment.getAllocationPolicy(), returnedEnrollment.getAllocationPolicy());
    }

    private DeviceClient createDeviceClient(IotHubClientProtocol protocol, RegistryClient registryClient) throws IOException, IotHubException, URISyntaxException
    {
        ClientOptions options = ClientOptions.builder().modelId(THERMOSTAT_MODEL_ID).build();
        String deviceId = "some-device-" + UUID.randomUUID();
        Device device = new Device(deviceId, AuthenticationType.SAS);
        Device registeredDevice = registryClient.addDevice(device);
        String deviceConnectionString = Tools.getDeviceConnectionString(iotHubConnectionString, registeredDevice);
        return new DeviceClient(deviceConnectionString, protocol, options);
    }
}