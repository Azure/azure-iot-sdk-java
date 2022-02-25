/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.twin.Pair;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.runners.Parameterized;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.ClientType;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestModuleIdentity;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Utility functions, setup and teardown for all device twin integration tests. This class should not contain any tests,
 * but any child class should.
 */
@Slf4j
public class TwinCommon extends IntegrationTest
{
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputs()
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        IntegrationTest.isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        IntegrationTest.isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        List inputs = new ArrayList();
        for (ClientType clientType : ClientType.values())
        {
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token, device client
                                        {AMQPS, SAS, ClientType.DEVICE_CLIENT},
                                        {AMQPS_WS, SAS, ClientType.DEVICE_CLIENT},
                                        {MQTT, SAS, ClientType.DEVICE_CLIENT},
                                        {MQTT_WS, SAS, ClientType.DEVICE_CLIENT},

                                        //x509, device client
                                        {AMQPS, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                        {MQTT, SELF_SIGNED, ClientType.DEVICE_CLIENT},
                                }
                ));
            }
            else
            {
                inputs.addAll(Arrays.asList(
                        new Object[][]
                                {
                                        //sas token, module client
                                        {AMQPS, SAS, ClientType.MODULE_CLIENT},
                                        {AMQPS_WS, SAS, ClientType.MODULE_CLIENT},
                                        {MQTT, SAS, ClientType.MODULE_CLIENT},
                                        {MQTT_WS, SAS, ClientType.MODULE_CLIENT}
                                }
                ));
            }
        }

        return inputs;
    }

    // Max time to wait to see it on Hub
    public static final long START_TWIN_TIMEOUT_MILLISECONDS = 30 * 1000; // 30 seconds

    public static final long DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS = 5 * 1000; //5 seconds

    protected static String iotHubConnectionString = "";

    protected DeviceTwinTestInstance testInstance;

    public TwinCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, ModuleClientException, GeneralSecurityException, URISyntaxException
    {
        this.testInstance = new DeviceTwinTestInstance(protocol, authenticationType, clientType);
    }

    public static class DeviceTwinTestInstance
    {
        public IotHubClientProtocol protocol;
        public AuthenticationType authenticationType;
        public String publicKeyCert;
        public String privateKey;
        public String x509Thumbprint;
        public ClientType clientType;
        public TwinClient twinServiceClient;
        public RegistryClient registryClient;
        public QueryClient queryClient;
        public TestIdentity testIdentity;
        public Twin serviceTwin;

        public DeviceTwinTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
        {
            this.protocol = protocol;
            this.authenticationType = authenticationType;
            this.publicKeyCert = x509CertificateGenerator.getPublicCertificatePEM();
            this.privateKey = x509CertificateGenerator.getPrivateKeyPEM();
            this.x509Thumbprint = x509CertificateGenerator.getX509Thumbprint();
            this.clientType = clientType;
            
            this.twinServiceClient = new TwinClient(iotHubConnectionString, TwinClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
            this.registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
            this.queryClient = new QueryClient(iotHubConnectionString);

            if (clientType == ClientType.DEVICE_CLIENT)
            {
                testIdentity = Tools.getTestDevice(iotHubConnectionString, protocol, authenticationType, true);
                this.serviceTwin = new Twin(testIdentity.getDeviceId());
            }
            else
            {
                testIdentity = Tools.getTestModule(iotHubConnectionString, protocol, authenticationType, true);
                this.serviceTwin = new Twin(testIdentity.getDeviceId(), ((TestModuleIdentity) testIdentity).getModuleId());
            }
        }
    }

    @After
    public void cleanup()
    {
        if (testInstance != null)
        {
            if (testInstance.testIdentity != null && testInstance.testIdentity.getClient() != null)
            {
                testInstance.testIdentity.getClient().close();
            }

            Tools.disposeTestIdentity(testInstance.testIdentity, iotHubConnectionString);
        }
    }

    // a function that tests both reported and desired property functionality for a test instance. Can be called multiple
    // times and does not require a clean twin
    public void testBasicTwinFlow() throws InterruptedException, IOException, IotHubException
    {
        final String desiredPropertyKey = UUID.randomUUID().toString();
        final String desiredPropertyValue = UUID.randomUUID().toString();

        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair(desiredPropertyKey, desiredPropertyValue));
        testInstance.serviceTwin.setDesiredProperties(desiredProperties);

        testInstance.testIdentity.getClient().open(true);

        // subscribe to desired properties
        final CountDownLatch twinSubscribedLatch = new CountDownLatch(1);
        final CountDownLatch desiredPropertyUpdatedLatch = new CountDownLatch(1);
        AtomicReference<com.microsoft.azure.sdk.iot.device.twin.Twin> desiredPropertyUpdateAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().subscribeToDesiredPropertiesAsync(
            desiredPropertiesUpdate ->
            {
                desiredPropertyUpdateAtomicReference.set(desiredPropertiesUpdate.getTwin());
                desiredPropertyUpdatedLatch.countDown();
            },
            (statusCode, context) -> twinSubscribedLatch.countDown());

        twinSubscribedLatch.await();

        // after subscribing to desired properties, call getTwin to get the initial state
        final CountDownLatch getTwinLatch = new CountDownLatch(1);
        AtomicReference<com.microsoft.azure.sdk.iot.device.twin.Twin> twinAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().getTwinAsync(
            getTwinResponse ->
            {
                twinAtomicReference.set(getTwinResponse.getTwin());
                getTwinLatch.countDown();
            });

        getTwinLatch.await();

        // a twin should have no the desired property yet
        com.microsoft.azure.sdk.iot.device.twin.Twin twin = twinAtomicReference.get();
        assertNotNull(twin);
        assertNotNull(twin.getReportedProperties());
        assertNotNull(twin.getDesiredProperties());
        assertFalse(twin.getDesiredProperties().containsKey(desiredPropertyKey));

        // send a desired property update and wait for it to be received by the device/module
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        desiredPropertyUpdatedLatch.await();
        com.microsoft.azure.sdk.iot.device.twin.Twin desiredPropertyUpdate = desiredPropertyUpdateAtomicReference.get();

        // the desired property update received by the device must match the key/value pair sent by the service client
        assertTrue(desiredPropertyUpdate.getDesiredProperties().containsKey(desiredPropertyKey));
        String value = (String) desiredPropertyUpdate.getDesiredProperties().get(desiredPropertyKey);
        assertEquals(desiredPropertyValue, value);

        // create some reported properties
        final String reportedPropertyKey = UUID.randomUUID().toString();
        final String reportedPropertyValue = UUID.randomUUID().toString();
        TwinCollection reportedProperties = new TwinCollection();
        reportedProperties.put(reportedPropertyKey, reportedPropertyValue);

        // send the reported properties and wait for the service to have acknowledged them
        final CountDownLatch reportedPropertiesSentLatch = new CountDownLatch(1);
        AtomicReference<IotHubStatusCode> statusCodeAtomicReference = new AtomicReference<>();
        testInstance.testIdentity.getClient().updateReportedPropertiesAsync(
            reportedProperties,
            sendReportedPropertiesResponse ->
            {
                statusCodeAtomicReference.set(sendReportedPropertiesResponse.getStatus());
                reportedPropertiesSentLatch.countDown();
            });

        reportedPropertiesSentLatch.await();

        // the reported properties request should have been ack'd with OK from the service
        IotHubStatusCode statusCode = statusCodeAtomicReference.get();
        assertEquals(IotHubStatusCode.OK, statusCode);

        // get the twin from the service client to check if the reported property is now present
        if (testInstance.clientType == ClientType.DEVICE_CLIENT)
        {
            testInstance.serviceTwin = testInstance.twinServiceClient.get(testInstance.testIdentity.getDeviceId());
        }
        else
        {
            testInstance.serviceTwin = testInstance.twinServiceClient.get(testInstance.testIdentity.getDeviceId(), ((TestModuleIdentity) testInstance.testIdentity).getModuleId());
        }

        Set<Pair> reportedPropertiesSet = testInstance.serviceTwin.getReportedProperties();
        assertTrue(reportedPropertiesSet.size() > 0);
        boolean expectedKeyValuePairFound = false;
        for (Pair pair : reportedPropertiesSet)
        {
            String actualReportedPropertyKey = pair.getKey();
            String actualReportedPropertyValue = (String) pair.getValue();

            expectedKeyValuePairFound = reportedPropertyKey.equals(actualReportedPropertyKey) && reportedPropertyValue.equals(actualReportedPropertyValue);

            if (expectedKeyValuePairFound)
            {
                break;
            }
        }

        assertTrue("Did not find expected reported property key and/or value after the device reported it", expectedKeyValuePairFound);
    }
}
