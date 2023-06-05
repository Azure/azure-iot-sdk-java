/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.setup;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.twin.ReportedPropertiesUpdateResponse;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SAS;
import static com.microsoft.azure.sdk.iot.service.auth.AuthenticationType.SELF_SIGNED;
import static org.junit.Assert.*;

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

        return Arrays.asList(
            new Object[][]
                {
                    {MQTT, SAS, ClientType.DEVICE_CLIENT},
                    {MQTT, SAS, ClientType.MODULE_CLIENT},
                });
    }

    // Max time to wait to see it on Hub
    public static final long TWIN_TIMEOUT_MILLISECONDS = 30 * 1000; // 30 seconds
    public static final long DESIRED_PROPERTIES_PROPAGATION_TIME_MILLISECONDS = 5 * 1000; //5 seconds

    protected static String iotHubConnectionString = "";

    protected TwinTestInstance testInstance;

    public TwinCommon(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, InterruptedException, IotHubException, GeneralSecurityException, URISyntaxException
    {
        this.testInstance = new TwinTestInstance(protocol, authenticationType, clientType);
    }

    public static class TwinTestInstance
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
        public com.microsoft.azure.sdk.iot.device.twin.Twin lastDesiredPropertyUpdate;

        public TwinTestInstance(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType) throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException
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
        }

        public void setup() throws IOException, GeneralSecurityException, IotHubException, URISyntaxException, InterruptedException, IotHubClientException
        {
            if (clientType == ClientType.DEVICE_CLIENT)
            {
                this.testIdentity = Tools.getTestDevice(iotHubConnectionString, protocol, authenticationType, false);
                this.serviceTwin = new Twin(testIdentity.getDeviceId());
            }
            else
            {
                this.testIdentity = Tools.getTestModule(iotHubConnectionString, protocol, authenticationType, false);
                this.serviceTwin = new Twin(testIdentity.getDeviceId(), ((TestModuleIdentity) testIdentity).getModuleId());
            }

            this.lastDesiredPropertyUpdate = new com.microsoft.azure.sdk.iot.device.twin.Twin(new TwinCollection(), new TwinCollection());

            testIdentity.getClient().open(true);
        }

        public Twin getServiceClientTwin() throws IOException, IotHubException
        {
            if (this.clientType == ClientType.DEVICE_CLIENT)
            {
                return this.twinServiceClient.get(this.testIdentity.getDeviceId());
            }
            else
            {
                return this.twinServiceClient.get(this.testIdentity.getDeviceId(), ((TestModuleIdentity) this.testIdentity).getModuleId());
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
    public void testBasicTwinFlow(boolean subscribe) throws InterruptedException, IOException, IotHubException, TimeoutException, IotHubClientException
    {
        final String desiredPropertyKey = UUID.randomUUID().toString();
        final String desiredPropertyValue = UUID.randomUUID().toString();

        // subscribe to desired properties
        if (subscribe)
        {
            testInstance.testIdentity.getClient().subscribeToDesiredProperties(
                (twin, context) -> testInstance.lastDesiredPropertyUpdate = twin,
                null);
        }

        // after subscribing to desired properties, onMethodInvoked getTwin to get the initial state
        com.microsoft.azure.sdk.iot.device.twin.Twin twin = testInstance.testIdentity.getClient().getTwin();

        // a twin should have no the desired property yet
        assertNotNull(twin);
        assertNotNull(twin.getReportedProperties());
        assertNotNull(twin.getDesiredProperties());
        assertFalse(twin.getDesiredProperties().containsKey(desiredPropertyKey));

        // send a desired property update and wait for it to be received by the device/module
        testInstance.serviceTwin.getDesiredProperties().put(desiredPropertyKey, desiredPropertyValue);
        testInstance.twinServiceClient.patch(testInstance.serviceTwin);

        // the desired property update received by the device must match the key/value pair sent by the service client
        long startTime = System.currentTimeMillis();
        while (!isPropertyInTwinCollection(testInstance.lastDesiredPropertyUpdate.getDesiredProperties(), desiredPropertyKey, desiredPropertyValue))
        {
            if (System.currentTimeMillis() - startTime > TWIN_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for expected desired property update on device client");
            }
        }

        // create some reported properties
        final String reportedPropertyKey = UUID.randomUUID().toString();
        final String reportedPropertyValue = UUID.randomUUID().toString();
        twin.getReportedProperties().put(reportedPropertyKey, reportedPropertyValue);

        // send the reported properties and wait for the service to have acknowledged them
        ReportedPropertiesUpdateResponse response = testInstance.testIdentity.getClient().updateReportedProperties(twin.getReportedProperties());

        // the reported properties request should have been ack'd with OK from the service
        assertTrue(response.getVersion() > 0);

        // get the twin from the service client to check if the reported property is now present
        testInstance.serviceTwin = testInstance.getServiceClientTwin();

        assertTrue(testInstance.serviceTwin.getReportedProperties().size() > 0);
        assertTrue("Did not find expected reported property key and/or value after the device reported it", isPropertyInTwinCollection(testInstance.serviceTwin.getReportedProperties(), reportedPropertyKey, reportedPropertyValue));
    }

    public static boolean isPropertyInTwinCollection(TwinCollection properties, String expectedKey, String expectedValue)
    {
        for (String key : properties.keySet())
        {
            if (key.equals(expectedKey) && properties.get(key).equals(expectedValue))
            {
                return true;
            }
        }

        return false;
    }

    public static boolean isPropertyInTwinCollection(com.microsoft.azure.sdk.iot.service.twin.TwinCollection properties, String expectedKey, String expectedValue)
    {
        for (String key : properties.keySet())
        {
            if (key.equals(expectedKey) && properties.get(key).equals(expectedValue))
            {
                return true;
            }
        }

        return false;
    }
}
