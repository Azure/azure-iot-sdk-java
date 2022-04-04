// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.device.ClientOptions;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.ModuleClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryIdentity;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.Module;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.DeviceConnectionString;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.SSLContextBuilder;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.StandardTierHubOnlyTest;

import javax.net.ssl.SSLContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Test class containing all tests to be run for PnP.
 */
@IotHubTest
@StandardTierHubOnlyTest
@RunWith(Parameterized.class)
public class TwinClientPnPTests extends IntegrationTest
{
    private static final int MODEL_ID_PROPAGATION_TIMEOUT_MILLIS = 60 * 1000; // 1 minute
    protected static String iotHubConnectionString = "";
    private static RegistryClient registryClient;
    private String ModelId;

    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));

        registryClient = new RegistryClient(iotHubConnectionString);

        List inputs = new ArrayList(Arrays.asList(
                new Object[][]
                        {
                                //sas token device client, no proxy
                                {MQTT, SAS, ClientType.DEVICE_CLIENT},
                                {MQTT_WS, SAS, ClientType.DEVICE_CLIENT},
                                {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                                {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT},

                                //x509 device client, no proxy
                                {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT},

                                //sas token device client, with proxy
                                {MQTT_WS, SAS, ClientType.DEVICE_CLIENT},
                                {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT},
                        }
        ));

        if (!isBasicTierHub)
        {
            inputs.addAll(Arrays.asList(
                new Object[][]
                    {
                            //sas token module client, no proxy
                            {MQTT, SAS, ClientType.MODULE_CLIENT},
                            {MQTT_WS, SAS, ClientType.MODULE_CLIENT},
                            {AMQPS, SAS, ClientType.MODULE_CLIENT},
                            {AMQPS_WS, SAS, ClientType.MODULE_CLIENT},

                            //x509 module client, no proxy
                            {MQTT, SELF_SIGNED, ClientType.MODULE_CLIENT},
                            {AMQPS, SELF_SIGNED, ClientType.MODULE_CLIENT},

                            //sas token module client, with proxy
                            {MQTT_WS, SAS, ClientType.MODULE_CLIENT},
                            {AMQPS_WS, SAS, ClientType.MODULE_CLIENT},
                    }
            ));
        }

        return inputs;
    }

    public TwinClientPnPTests.TwinPnPTestInstance testInstance;

    public TwinClientPnPTests(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType)
    {
        this.testInstance = new TwinPnPTestInstance(protocol, authenticationType, clientType);
    }

    public class TwinPnPTestInstance
    {
        public InternalClient client;
        public IotHubClientProtocol protocol;
        public RegistryIdentity identity;
        public AuthenticationType authenticationType;
        public ClientType clientType;
        public String x509Thumbprint;

        private final TwinClient twinServiceClient;
        private Twin twin;

        public TwinPnPTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType)
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.clientType = clientType;
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();

            this.twinServiceClient = new TwinClient(iotHubConnectionString);
        }

        public void setup() throws Exception {
            String TEST_UUID = UUID.randomUUID().toString();

            // Hardcoding the version so that the test hub doesn't accumulate PnP models over time. Each hub can only
            // have so many models in memory and the models currently don't get deleted ever.
            int TEST_MODEL_VERSION = 1;

            /* Create unique device names */
            String deviceId = "java-twinPnp-e2e-test-device".concat("-" + TEST_UUID);
            String moduleId = "java-twinPnp-e2e-test-module".concat("-" + TEST_UUID);
            String deviceX509Id = "java-twinPnp-e2e-test-device-x509".concat("-" + TEST_UUID);
            String moduleX509Id = "java-twinPnp-e2e-test-module-x509".concat("-" + TEST_UUID);
            ModelId = "dtmi:com:test:e2e;" + TEST_MODEL_VERSION;

            /* Create device on the service */
            Device device = new Device(deviceId);
            Module module = new Module(deviceId, moduleId);

            Device deviceX509 = new Device(deviceX509Id, AuthenticationType.SELF_SIGNED);
            deviceX509.setThumbprint(x509Thumbprint, x509Thumbprint);
            Module moduleX509 = new Module(deviceX509Id, moduleX509Id, AuthenticationType.SELF_SIGNED);
            moduleX509.setThumbprint(x509Thumbprint, x509Thumbprint);
            device = Tools.addDeviceWithRetry(registryClient, device);
            deviceX509 = Tools.addDeviceWithRetry(registryClient, deviceX509);
            ClientOptions.ClientOptionsBuilder clientOptionsBuilder = ClientOptions.builder().modelId(ModelId);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas device client
                    this.client = new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, device), protocol, clientOptionsBuilder.build());
                    this.identity = device;
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 device client
                    SSLContext sslContext = SSLContextBuilder.buildSSLContext(x509CertificateGenerator.getX509Certificate(), x509CertificateGenerator.getPrivateKey());
                    clientOptionsBuilder.sslContext(sslContext);
                    this.client = new DeviceClient(Tools.getDeviceConnectionString(iotHubConnectionString, deviceX509), protocol, clientOptionsBuilder.build());
                    this.identity = deviceX509;
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }

                this.twin = new Twin(testInstance.identity.getDeviceId());
            }
            else if (clientType == ClientType.MODULE_CLIENT)
            {
                if (authenticationType == SAS)
                {
                    //sas module client
                    module = Tools.addModuleWithRetry(registryClient, module);
                    this.client = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, device, module), protocol, clientOptionsBuilder.build());
                    this.identity = module;
                    this.twin = new Twin(deviceId, moduleId);
                }
                else if (authenticationType == SELF_SIGNED)
                {
                    //x509 module client
                    moduleX509 = Tools.addModuleWithRetry(registryClient, moduleX509);
                    SSLContext sslContext = SSLContextBuilder.buildSSLContext(x509CertificateGenerator.getX509Certificate(), x509CertificateGenerator.getPrivateKey());
                    clientOptionsBuilder.sslContext(sslContext);
                    this.client = new ModuleClient(DeviceConnectionString.get(iotHubConnectionString, deviceX509, moduleX509), protocol, clientOptionsBuilder.build());
                    this.identity = moduleX509;
                    this.twin = new Twin(deviceX509Id, moduleX509Id);
                }
                else
                {
                    throw new Exception("Test code has not been written for this path yet");
                }
            }

            this.client.open(true);
        }

        public void dispose()
        {
            try
            {
                this.client.close();
                registryClient.removeDevice(this.identity.getDeviceId()); //removes all modules associated with this device, too
            }
            catch (Exception e)
            {
                // not a big deal if dispose fails. This test suite is not testing the functions in this cleanup.
                // If identities are left registered, they will be deleted a nightly cleanup job anyways
            }
        }
    }

    @After
    public void tearDownTest()
    {
        this.testInstance.dispose();
    }

    @Test
    public void testGetTwinWithModelId() throws Exception {
        // arrange
        this.testInstance.setup();

        // Immediately checking if the object returned by the get twin operation has the expected modelId may fail
        // if the service is slow to propagate the model id to the twin service. To account for this potential delay,
        // check until it matches or the test times out to allow for longer model id propagation times
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime <= MODEL_ID_PROPAGATION_TIMEOUT_MILLIS)
        {
            // act
            if (testInstance.clientType == ClientType.DEVICE_CLIENT)
            {
                testInstance.twin = testInstance.twinServiceClient.get(testInstance.identity.getDeviceId());
            }
            else
            {
                testInstance.twin = testInstance.twinServiceClient.get(testInstance.identity.getDeviceId(), ((Module)testInstance.identity).getId());
            }

            // assert
            if (ModelId.equals(testInstance.twin.getModelId()))
            {
                return; // conditions met, exit test successfully
            }
        }

        fail("Timed out waiting for the model id to be present in the twin service");
    }
}
