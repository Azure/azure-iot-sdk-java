/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.edge.DirectMethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.DirectMethodResponse;
import com.microsoft.azure.sdk.iot.device.edge.HttpsHsmTrustBundleProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.hsm.IotHubSasTokenHsmAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.hsm.UnixDomainSocketChannel;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
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
    ClientConfiguration mockedClientConfiguration;

    @Mocked
    IotHubConnectionString mockedIotHubConnectionString;

    @Mocked
    Message mockedMessage;

    @Mocked
    MessageSentCallback mockedMessageSentCallback;

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
    DirectMethodResponse mockedDirectMethodResponse;

    @Mocked
    DirectMethodRequest mockedDirectMethodRequest;

    @Mocked
    HttpsTransportManager mockedHttpsTransportManager;

    @Mocked
    IotHubSSLContext mockIotHubSSLContext;

    @Mocked
    UnixDomainSocketChannel mockedUnixDomainSocketChannel;

    private void baseExpectations() throws URISyntaxException
    {
        new NonStrictExpectations()
        {
            {
                new IotHubConnectionString(anyString);
                result = mockedIotHubConnectionString;

                mockedIotHubConnectionString.getModuleId();
                result = "someModuleId";

                mockedClientConfiguration.getModuleId();
                result = "someModuleId";
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_004: [If the provided connection string does not contain a module id, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorRequiresModuleId() throws URISyntaxException
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
    public void constructorThrowsForHTTP() throws URISyntaxException
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

    @Test
    public void constructorWithModelIdSuccess(final @Mocked System mockedSystem) throws URISyntaxException, IOException, IotHubClientException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedEdgeHubConnectionString = null;
        final String expectedIotHubConnectionString = "testConnectionString";
        final ClientOptions clientOptions = ClientOptions.builder().modelId("testModelId").build();

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "EdgehubConnectionstringVariableName").toString(), expectedEdgeHubConnectionString);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IothubConnectionstringVariableName").toString(), expectedIotHubConnectionString);

        //assert
        new Expectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;

                mockedClientConfiguration.getModuleId();
                result = "someModuleId";
            }
        };

        // act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol, clientOptions);
    }

    //Tests_SRS_MODULECLIENT_34_006: [This function shall invoke the super constructor.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorCallsSuper() throws URISyntaxException
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

    @Test
    public void constructorWithModelIdSuccess() throws URISyntaxException {
        // arrange
        final String connString =
                "TestConnectionString";
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS_WS;

        new Expectations()
        {
            {
                new IotHubConnectionString(connString);
                result = mockedIotHubConnectionString;

                mockedClientConfiguration.getModuleId();
                result = "some module id";
            }
        };

        // act
        new ModuleClient(connString, protocol, null);
    }

    //Tests_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncWithOutputThrowsForEmptyOutputName() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);

        //act
        client.sendEventAsync(mockedMessage, mockedMessageSentCallback, new Object(), "");
    }

    //Tests_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncWithOutputThrowsForNullOutputName() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);

        //act
        client.sendEventAsync(mockedMessage, mockedMessageSentCallback, new Object(), null);
    }

    //Tests_SRS_MODULECLIENT_34_002: [This function shall set the provided message with the provided outputName, device id, and module id properties.]
    //Tests_SRS_MODULECLIENT_34_003: [This function shall invoke super.sendEventAsync(message, callback, callbackContext).]
    @Test
    public void sendEventAsyncToOutputSuccess() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);
        final String expectedOutputName = "some output name";
        Deencapsulation.setField(client, "config", mockedClientConfiguration);

        //act
        client.sendEventAsync(mockedMessage, mockedMessageSentCallback, new Object(), expectedOutputName);

        //assert
        new Verifications()
        {
            {
                mockedMessage.setOutputName(expectedOutputName);
                times = 1;

                mockedDeviceIO.sendEventAsync(mockedMessage, mockedMessageSentCallback, any, anyString);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_040: [This function shall set the message's connection moduleId to the config's saved module id.]
    //Tests_SRS_MODULECLIENT_34_041: [This function shall invoke super.sendEventAsync(message, callback, callbackContext).]
    @Test
    public void sendEventAsyncSuccess() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);
        final String expectedDeviceId = "1234";
        final String expectedModuleId = "5678";
        Deencapsulation.setField(client, "config", mockedClientConfiguration);

        new NonStrictExpectations()
        {
            {
                mockedClientConfiguration.getDeviceId();
                result = expectedDeviceId;

                mockedClientConfiguration.getModuleId();
                result = expectedModuleId;
            }
        };

        //act
        client.sendEventAsync(mockedMessage, mockedMessageSentCallback, new Object());

        //assert
        new Verifications()
        {
            {
                mockedMessage.setConnectionDeviceId(expectedDeviceId);
                mockedMessage.setConnectionModuleId(expectedModuleId);

                mockedDeviceIO.sendEventAsync(mockedMessage, mockedMessageSentCallback, any, expectedDeviceId);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_010: [If the provided callback is null and the provided context is not null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackWithInputThrowsForNullCallbackWithoutNullContext() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback("validInputName", null, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_011: [If the provided inputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackWithInputThrowsForNullInputName() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback(null, mockedMessageCallback, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_011: [If the provided inputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setMessageCallbackWithInputThrowsForEmptyInputName() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.AMQPS_WS);

        //act
        client.setMessageCallback("", mockedMessageCallback, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_012: [This function shall save the provided callback with context in config tied to the provided inputName.]
    @Test
    public void setMessageCallbackWithInputSavesInConfig() throws URISyntaxException
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
                mockedClientConfiguration.setMessageCallback(expectedInputName, mockedMessageCallback, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForHostname(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = null;
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForDeviceId(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForModuleId(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForAuthScheme(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_030: [If the auth scheme environment variable is not "SasToken", this function shall throw a moduleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForAuthSchemeToBeSasToken(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_017: [This function shall create an authentication provider using the created
    // signature provider, and the environment variables for deviceid, moduleid, hostname, gatewayhostname,
    // and the default time for tokens to live and the default sas token buffer time.]
    //Tests_SRS_MODULECLIENT_34_018: [This function shall return a new ModuleClient instance built from the created authentication provider and the provided protocol.]
    //Tests_SRS_MODULECLIENT_34_032: [This function shall retrieve the trust bundle from the hsm and set them in the module client.]
    @Test
    public void signatureProvider(final @Mocked System mockedSystem, @Mocked final InternalClient internalClient, @Mocked final HttpsHsmTrustBundleProvider mockedHttpsHsmTrustBundleProvider) throws IotHubClientException, IOException, NoSuchAlgorithmException, IOException, TransportException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedIotEdgedUri = "someUri";
        final String expectedApiVersion = Deencapsulation.getField(ModuleClient.class, "DEFAULT_API_VERSION");
        final String expectedGenerationId = "gen1";
        final String expectedHostname = "someHostname";
        final String expectedGatewayHostname = "someGatewayHostname";
        final String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);

        new Expectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;

                new HttpHsmSignatureProvider(expectedIotEdgedUri, expectedApiVersion, mockedUnixDomainSocketChannel);
                result = mockedHttpHsmSignatureProvider;

                IotHubSasTokenHsmAuthenticationProvider.create(mockedHttpHsmSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, expectedMqttGatewayHostname, expectedGenerationId, anyInt, anyInt, (SSLContext) any);
                result = mockedModuleAuthenticationWithHsm;

                new HttpsHsmTrustBundleProvider();
                result = mockedHttpsHsmTrustBundleProvider;

                mockedHttpsHsmTrustBundleProvider.getTrustBundleCerts(expectedIotEdgedUri, expectedApiVersion, mockedUnixDomainSocketChannel);
                result = expectedTrustedCerts;

                Deencapsulation.newInstance(ModuleClient.class,
                        new Class[] {IotHubAuthenticationProvider.class, IotHubClientProtocol.class},
                        mockedModuleAuthenticationWithHsm, (IotHubClientProtocol) any);
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
            // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
            // this function shall throw a ModuleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForEdgedUri(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = null;
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
    // hostname, authScheme, gatewayHostname, and generationId. If any of these other than gatewayHostname is missing,
    // this function shall throw a ModuleClientException.]
    @Test (expected = IllegalStateException.class)
    public void createFromEnvironmentChecksForGenerationId(final @Mocked System mockedSystem) throws IotHubClientException, IOException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        String expectedIotEdgedUri = null;
        String expectedApiVersion = "1.1.1";
        String expectedHostname = "someHostname";
        String expectedGatewayHostname = "someGatewayHostname";
        String expectedMqttGatewayHostname = "someMqttGatewayHostname";
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "MqttGatewayHostnameVariableName").toString(), expectedMqttGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleGenerationIdVariableName").toString(), null);
        new NonStrictExpectations()
        {
            {
                System.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }


    //Tests_SRS_MODULECLIENT_34_013: [This function shall check for a saved edgehub connection string.]
    //Tests_SRS_MODULECLIENT_34_020: [If an edgehub or iothub connection string is present, this function shall create a module client instance using that connection string and the provided protocol.]
    //Tests_SRS_MODULECLIENT_34_031: [If an alternative default trusted cert is saved in the environment
    // variables, this function shall set that trusted cert in the created module client.]
    @Test
    public void createFromEnvironmentChecksForEnvVarOfEdgeHub(final @Mocked System mockedSystem) throws IotHubClientException, IOException, URISyntaxException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
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
                System.getenv();
                result = mockedSystemVariables;

                new IotHubConnectionString(expectedEdgeHubConnectionString);
                result = mockedIotHubConnectionString;

                mockedClientConfiguration.getModuleId();
                result = "someModuleId";
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);

        //assert
        new Verifications()
        {
            {
                IotHubSSLContext.getSSLContextFromFile(expectedTrustedCert);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_019: [If no edgehub connection string is present, this function shall check for a saved iothub connection string.]
    //Tests_SRS_MODULECLIENT_34_020: [If an edgehub or iothub connection string is present, this function shall create a module client instance using that connection string and the provided protocol.]
    @Test
    public void createFromEnvironmentChecksForEnvVarOfIotHub(final @Mocked System mockedSystem) throws IotHubClientException, IOException, URISyntaxException
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
                System.getenv();
                result = mockedSystemVariables;

                new IotHubConnectionString(expectedIotHubConnectionString);
                result = mockedIotHubConnectionString;
                mockedClientConfiguration.getModuleId();
                result = "someModuleId";
            }
        };

        //act
        ModuleClient.createFromEnvironment(mockedUnixDomainSocketChannel, protocol);
    }

    //Tests_SRS_MODULECLIENT_34_033: [This function shall create an HttpsTransportManager and use it to invoke the method on the device.]
    @Test
    public void invokeMethodOnDeviceSuccess() throws URISyntaxException, IOException, TransportException, IotHubClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((ClientConfiguration) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedDirectMethodRequest, expectedDeviceId, "");
                result = mockedDirectMethodResponse;
            }
        };

        //act
        DirectMethodResponse actualResult = client.invokeMethod(expectedDeviceId, mockedDirectMethodRequest);

        //assert
        assertEquals(mockedDirectMethodResponse, actualResult);
        new Verifications()
        {
            {
                mockedHttpsTransportManager.invokeMethod(mockedDirectMethodRequest, expectedDeviceId, "");
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_034: [If this function encounters an exception, it shall throw a moduleClientException with that exception nested.]
    @Test (expected = IotHubClientException.class)
    public void invokeMethodOnDeviceWrapsExceptions() throws URISyntaxException, IOException, TransportException, IotHubClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((ClientConfiguration) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedDirectMethodRequest, expectedDeviceId, "");
                result = new IOException();
            }
        };

        //act
        client.invokeMethod(expectedDeviceId, mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_035: [This function shall create an HttpsTransportManager and use it to invoke the method on the module.]
    @Test
    public void invokeMethodOnModuleSuccess() throws URISyntaxException, IOException, TransportException, IotHubClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";
        final String expectedModuleId = "someModule";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((ClientConfiguration) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedDirectMethodRequest, expectedDeviceId, expectedModuleId);
                result = mockedDirectMethodResponse;
            }
        };

        //act
        DirectMethodResponse actualResult = client.invokeMethod(expectedDeviceId, expectedModuleId, mockedDirectMethodRequest);

        //assert
        assertEquals(mockedDirectMethodResponse, actualResult);
        new Verifications()
        {
            {
                mockedHttpsTransportManager.invokeMethod(mockedDirectMethodRequest, expectedDeviceId, expectedModuleId);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_036: [If this function encounters an exception, it shall throw a moduleClientException with that exception nested.]
    @Test (expected = IotHubClientException.class)
    public void invokeMethodOnModuleWrapsExceptions() throws URISyntaxException, IOException, TransportException, IotHubClientException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);
        final String expectedDeviceId = "someDevice";
        final String expectedModuleId = "someModule";

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager((ClientConfiguration) any);
                result = mockedHttpsTransportManager;

                mockedHttpsTransportManager.invokeMethod(mockedDirectMethodRequest, expectedDeviceId, expectedModuleId);
                result = new IOException();
            }
        };

        //act
        client.invokeMethod(expectedDeviceId, expectedModuleId, mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_037: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnDeviceThrowsForNullDeviceId() throws URISyntaxException, IotHubClientException, IOException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod(null, mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_037: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnDeviceThrowsForEmptyDeviceId() throws URISyntaxException, IotHubClientException, IOException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("", mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_038: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForNullDeviceId() throws URISyntaxException, IotHubClientException, IOException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod(null, "someValidModule", mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_038: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForEmptyDeviceId() throws URISyntaxException, IotHubClientException, IOException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("", "someValidModule", mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_039: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForNullModuleId() throws URISyntaxException, IotHubClientException, IOException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("someValidDevice", null, mockedDirectMethodRequest);
    }

    //Tests_SRS_MODULECLIENT_34_039: [If the provided deviceId is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodOnModuleThrowsForEmptyModuleId() throws URISyntaxException, IotHubClientException, IOException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("connection string", IotHubClientProtocol.AMQPS);

        //act
        client.invokeMethod("someValidDevice", "", mockedDirectMethodRequest);
    }
}
