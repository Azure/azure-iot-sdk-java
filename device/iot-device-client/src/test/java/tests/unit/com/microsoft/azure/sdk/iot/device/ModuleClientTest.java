/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.edge.HttpsHsmTrustBundleProvider;
import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.MethodResult;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HsmException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.hsm.IotHubSasTokenHsmAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * Unit tests for ModuleClient.java
 * Methods:
 * Lines:
 */
public class ModuleClientTest
{
    @Mocked
    CustomLogger mockedCustomLogger;

    @Mocked
    DeviceClientConfig mockedDeviceClientConfig;

    @Mocked
    IotHubConnectionString mockedIotHubConnectionString;

    @Mocked
    Message mockedMessage;

    @Mocked
    IotHubEventCallback mockedIotHubEventCallback;

    @Mocked
    DeviceIO mockedDeviceIO;

    @Mocked
    MessageCallback mockedMessageCallback;

    @Mocked
    HttpHsmSignatureProvider mockedHttpHsmSignatureProvider;

    @Mocked
    IotHubAuthenticationProvider mockedIotHubAuthenticationProvider;

    @Mocked
    IotHubSasTokenHsmAuthenticationProvider mockedModuleAuthenticationWithHsm;

    @Mocked
    URL mockedURL;

    @Mocked
    MethodResult mockedMethodResult;

    @Mocked
    MethodRequest mockedMethodRequest;

    @Mocked
    HttpsTransportManager mockedHttpsTransportManager;

    private void baseExpectations() throws URISyntaxException
    {
        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(anyString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = "someModuleId";

                mockedDeviceClientConfig.getModuleId();
                result = "someModuleId";
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_004: [If the provided connection string does not contain a module id, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorRequiresModuleId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        final String connectionString = "some connection string";
        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = null;
            }
        };

        //act
        new ModuleClient(connectionString, IotHubClientProtocol.AMQPS);
    }

    //Tests_SRS_MODULECLIENT_34_007: [If the provided protocol is not MQTT, AMQPS, MQTT_WS, or AMQPS_WS, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void constructorThrowsForHTTP() throws URISyntaxException, ModuleClientException
    {
        //arrange
        final String connectionString = "some connection string";
        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = "module";
            }
        };

        //act
        new ModuleClient(connectionString, IotHubClientProtocol.HTTPS);
    }

    //Tests_SRS_MODULECLIENT_34_006: [This function shall invoke the super constructor.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorCallsSuper() throws URISyntaxException, ModuleClientException
    {
        //arrange
        final String connectionString = "some connection string";
        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = "some module";

                mockedIotHubConnectionString.isUsingX509();
                result = false;
            }
        };

        //act
        ModuleClient client = new ModuleClient(connectionString, IotHubClientProtocol.AMQPS);

        //assert
        assertNotNull(client.getConfig());
        assertNotNull(Deencapsulation.getField(client, "deviceIO"));
    }

    //Tests_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncWithOutputThrowsForEmptyOutputName() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);

        //act
        client.sendEventAsync(mockedMessage, mockedIotHubEventCallback, new Object(), "");
    }

    //Tests_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncWithOutputThrowsForNullOutputName() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);

        //act
        client.sendEventAsync(mockedMessage, mockedIotHubEventCallback, new Object(), null);
    }

    //Tests_SRS_MODULECLIENT_34_002: [This function shall set the provided message with the provided outputName, device id, and module id properties.]
    //Tests_SRS_MODULECLIENT_34_003: [This function shall invoke super.sendEventAsync(message, callback, callbackContext).]
    @Test
    public void sendEventAsyncToOutputSuccess() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);
        final String expectedOutputName = "some output name";
        Deencapsulation.setField(client, "config", mockedDeviceClientConfig);

        //act
        client.sendEventAsync(mockedMessage, mockedIotHubEventCallback, new Object(), expectedOutputName);

        //assert
        new Verifications()
        {
            {
                mockedMessage.setOutputName(expectedOutputName);
                times = 1;

                mockedDeviceIO.sendEventAsync(mockedMessage, mockedIotHubEventCallback, any, anyString);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_040: [This function shall set the message's connection moduleId to the config's saved module id.]
    //Tests_SRS_MODULECLIENT_34_041: [This function shall invoke super.sendEventAsync(message, callback, callbackContext).]
    @Test
    public void sendEventAsyncSuccess() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);
        final String expectedDeviceId = "1234";
        final String expectedModuleId = "5678";
        Deencapsulation.setField(client, "config", mockedDeviceClientConfig);

        new NonStrictExpectations()
        {
            {
                mockedDeviceClientConfig.getDeviceId();
                result = expectedDeviceId;

                mockedDeviceClientConfig.getModuleId();
                result = expectedModuleId;
            }
        };

        //act
        client.sendEventAsync(mockedMessage, mockedIotHubEventCallback, new Object());

        //assert
        new Verifications()
        {
            {
                mockedMessage.setConnectionDeviceId(expectedDeviceId);
                mockedMessage.setConnectionModuleId(expectedModuleId);

                mockedDeviceIO.sendEventAsync(mockedMessage, mockedIotHubEventCallback, any, expectedDeviceId);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_008: [If the provided protocol is not MQTT, AMQPS, MQTT_WS, or AMQPS_WS, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void x509ConstructorThrowsForHTTP() throws URISyntaxException, ModuleClientException
    {
        //arrange
        final String connectionString = "connectionString";

        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = "someModuleId";

                mockedIotHubConnectionString.isUsingX509();
                result = true;
            }
        };

        //act
        new ModuleClient(connectionString, IotHubClientProtocol.HTTPS, "public cert", false, "private key", false);
    }

    //Tests_SRS_MODULECLIENT_34_009: [If the provided connection string does not contain a module id, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void x509ConstructorThrowsForConnectionStringWithoutModuleId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        final String connectionString = "connectionString";

        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(connectionString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = null;

                mockedIotHubConnectionString.isUsingX509();
                result = true;
            }
        };

        //act
        new ModuleClient(connectionString, IotHubClientProtocol.AMQPS, "public cert", false, "private key", false);
    }

    //Tests_SRS_MODULECLIENT_34_010: [If the provided callback is null and the provided context is not null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackWithInputThrowsForNullCallbackWithoutNullContext() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback("validInputName", null, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_011: [If the provided inputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackWithInputThrowsForNullInputName() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback(null, mockedMessageCallback, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_011: [If the provided inputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackWithInputThrowsForEmptyInputName() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback("", mockedMessageCallback, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_012: [This function shall save the provided callback with context in config tied to the provided inputName.]
    @Test
    public void setMessageCallbackWithInputSavesInConfig() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        final String expectedInputName = "someInputNameString";
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback(expectedInputName, mockedMessageCallback, new Object());

        //assert
        new Verifications()
        {
            {
                mockedDeviceClientConfig.setMessageCallback(expectedInputName, mockedMessageCallback, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForHostname(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = null;
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = "1234";
        String expectedModuleId = "5678";
        String expectedGenerationId = "gen1";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), expectedGenerationId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForDeviceId(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = null;
        String expectedModuleId = "5678";
        String expectedGenerationId = "gen1";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), expectedGenerationId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForModuleId(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = "1234";
        String expectedModuleId = null;
        String expectedGenerationId = "gen1";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), expectedGenerationId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForAuthScheme(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = "1234";
        String expectedModuleId = "5678";
        String expectedGenerationId = "gen1";
        String expectedAuthScheme = null;

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), expectedGenerationId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_030: [If the auth scheme environment variable is not "SasToken", this function shall throw a moduleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForAuthSchemeToBeSasToken(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = "1234";
        String expectedModuleId = "5678";
        String expectedAuthScheme = "not sas token auth";

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_017: [This function shall create an authentication provider using the created
    // signature provider, and the environment variables for deviceid, moduleid, hostname, gatewayhostname,
    // and the default time for tokens to live and the default sas token buffer time.]
    //Tests_SRS_MODULECLIENT_34_018: [This function shall return a new ModuleClient instance built from the created authentication provider and the provided protocol.]
    //Tests_SRS_MODULECLIENT_34_032: [This function shall retrieve the trust bundle from the hsm and set them in the module client.]
    @Test
    public void signatureProvider(final @Mocked System mockedSystem, @Mocked final InternalClient internalClient, @Mocked final HttpsHsmTrustBundleProvider mockedHttpsHsmTrustBundleProvider) throws ModuleClientException, NoSuchAlgorithmException, IOException, TransportException, URISyntaxException, HsmException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedIotEdgedUri = "someUri";
        final String expectedApiVersion = Deencapsulation.getField(ModuleClient.class, "DEFAULT_API_VERSION");
        final String expectedGenerationId = "gen1";
        final String expectedHostname = "someHostname";
        final String expectedGatewayHostname = "someGatewayHostname";
        final String expectedDeviceId = "1234";
        final String expectedModuleId = "5678";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");
        final String expectedTrustedCerts = "some string of trusted certs";

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), expectedGenerationId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);

        new Expectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;

                new HttpHsmSignatureProvider(expectedIotEdgedUri, expectedApiVersion);
                result = mockedHttpHsmSignatureProvider;

                IotHubSasTokenHsmAuthenticationProvider.create(mockedHttpHsmSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, expectedGenerationId, anyInt, anyInt);
                result = mockedModuleAuthenticationWithHsm;

                new HttpsHsmTrustBundleProvider();
                result = mockedHttpsHsmTrustBundleProvider;

                mockedHttpsHsmTrustBundleProvider.getTrustBundleCerts(expectedIotEdgedUri, expectedApiVersion);
                result = expectedTrustedCerts;

                Deencapsulation.newInstance(ModuleClient.class,
                        new Class[] {IotHubAuthenticationProvider.class, IotHubClientProtocol.class, long.class, long.class},
                        mockedModuleAuthenticationWithHsm, (IotHubClientProtocol) any, anyLong, anyLong);
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForEdgedUri(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = null;
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = "1234";
        String expectedModuleId = "5678";
        String expectedGenerationId = "gen1";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), expectedGenerationId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
    // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
    // this function shall throw a ModuleClientException.]
    @Test (expected = ModuleClientException.class)
    public void createFromEnvironmentChecksForGenerationId(final @Mocked System mockedSystem) throws ModuleClientException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = null;
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedDeviceId = "1234";
        String expectedModuleId = "5678";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), null);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }


    //Tests_SRS_MODULECLIENT_34_013: [This function shall check for a saved edgehub connection string.]
    //Tests_SRS_MODULECLIENT_34_020: [If an edgehub or iothub connection string is present, this function shall create a module client instance using that connection string and the provided protocol.]
    //Tests_SRS_MODULECLIENT_34_031: [If an alternative default trusted cert is saved in the environment
    // variables, this function shall set that trusted cert in the created module client.]
    @Test
    public void createFromEnvironmentChecksForEnvVarOfEdgeHub(final @Mocked System mockedSystem) throws ModuleClientException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedEdgeHubConnectionString = "edgehubConnString";
        final String expectedIotHubConnectionString = "iothubConnString";
        final String expectedTrustedCert = "some trusted cert";

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "EdgehubConnectionstringVariableName").toString(), expectedEdgeHubConnectionString);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IothubConnectionstringVariableName").toString(), expectedIotHubConnectionString);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "EdgeCaCertificateFileVariableName").toString(), expectedTrustedCert);

        //assert
        new Expectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;

                new IotHubConnectionString(expectedEdgeHubConnectionString);
                result = mockedIotHubConnectionString;

                new DeviceClientConfig(mockedIotHubConnectionString);
                result = mockedDeviceClientConfig;

                mockedDeviceClientConfig.getModuleId();
                result = "someModuleId";
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);

        //assert
        new Verifications()
        {
            {
                mockedDeviceClientConfig.getAuthenticationProvider().setPathToIotHubTrustedCert(expectedTrustedCert);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_019: [If no edgehub connection string is present, this function shall check for a saved iothub connection string.]
    //Tests_SRS_MODULECLIENT_34_020: [If an edgehub or iothub connection string is present, this function shall create a module client instance using that connection string and the provided protocol.]
    @Test
    public void createFromEnvironmentChecksForEnvVarOfIotHub(final @Mocked System mockedSystem) throws ModuleClientException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedEdgeHubConnectionString = null;
        final String expectedIotHubConnectionString = "HostName=hub.azure-devices.net;DeviceId=device;ModuleId=module;SharedAccessKey=ecI+YlN6YFCACtVXaPM73/z/Pradyfh4IbtHusg4zEbE=";

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "EdgehubConnectionstringVariableName").toString(), expectedEdgeHubConnectionString);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IothubConnectionstringVariableName").toString(), expectedIotHubConnectionString);

        //assert
        new Expectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;

                new IotHubConnectionString(expectedIotHubConnectionString);
                result = mockedIotHubConnectionString;

                new DeviceClientConfig(mockedIotHubConnectionString);
                result = mockedDeviceClientConfig;

                mockedDeviceClientConfig.getModuleId();
                result = "someModuleId";
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);
    }

    //Tests_SRS_MODULECLIENT_34_033: [This function shall create an HttpsTransportManager and use it to invoke the method on the device.]
    @Test
    public void invokeMethodOnDeviceSuccess() throws URISyntaxException, ModuleClientException, IOException, TransportException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((DeviceClientConfig) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, "");
                result = mockedMethodResult;
            }
        };

        //act
        MethodResult actualResult = client.invokeMethod(expectedDeviceId, mockedMethodRequest);

        //assert
        assertEquals(mockedMethodResult, actualResult);
        new Verifications()
        {
            {
                mockedHttpsTransportManager.open();
                times = 1;

                mockedHttpsTransportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, "");
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_034: [If this function encounters an exception, it shall throw a moduleClientException with that exception nested.]
    @Test (expected = ModuleClientException.class)
    public void invokeMethodOnDeviceWrapsExceptions() throws URISyntaxException, ModuleClientException, IOException, TransportException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((DeviceClientConfig) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, "");
                result = new IOException();
            }
        };

        //act
        client.invokeMethod(expectedDeviceId, mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_035: [This function shall create an HttpsTransportManager and use it to invoke the method on the module.]
    @Test
    public void invokeMethodOnModuleSuccess() throws URISyntaxException, ModuleClientException, IOException, TransportException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";
        final String expectedModuleId = "someModule";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((DeviceClientConfig) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, expectedModuleId);
                result = mockedMethodResult;
            }
        };

        //act
        MethodResult actualResult = client.invokeMethod(expectedDeviceId, expectedModuleId, mockedMethodRequest);

        //assert
        assertEquals(mockedMethodResult, actualResult);
        new Verifications()
        {
            {
                mockedHttpsTransportManager.open();
                times = 1;

                mockedHttpsTransportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, expectedModuleId);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_036: [If this function encounters an exception, it shall throw a moduleClientException with that exception nested.]
    @Test (expected = ModuleClientException.class)
    public void invokeMethodOnModuleWrapsExceptions() throws URISyntaxException, ModuleClientException, IOException, TransportException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";
        final String expectedModuleId = "someModule";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((DeviceClientConfig) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, expectedModuleId);
                result = new IOException();
            }
        };

        //act
        client.invokeMethod(expectedDeviceId, expectedModuleId, mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_037: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnDeviceThrowsForNullDeviceId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod(null, mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_037: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnDeviceThrowsForEmptyDeviceId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("", mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_038: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForNullDeviceId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod(null, "someValidModule", mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_038: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForEmptyDeviceId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("", "someValidModule", mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_039: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForNullModuleId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("someValidDevice", null, mockedMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_039: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForEmptyModuleId() throws URISyntaxException, ModuleClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("someValidDevice", "", mockedMethodRequest);
    }
}
