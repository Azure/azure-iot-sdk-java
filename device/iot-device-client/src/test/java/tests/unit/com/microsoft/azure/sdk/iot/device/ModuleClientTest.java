/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.AuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignatureProvider;
import com.microsoft.azure.sdk.iot.device.auth.ModuleAuthenticationWithHsm;
import com.microsoft.azure.sdk.iot.device.auth.SignatureProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;

/**
 * Unit tests for ModuleClient.java
 * Methods:
 * Lines:
 */
public class ModuleClientTest
{
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
    AuthenticationProvider mockedAuthenticationProvider;

    @Mocked
    ModuleAuthenticationWithHsm mockedModuleAuthenticationWithHsm;

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

    //Tests_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncWithOutputThrowsForEmptyOutputName() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);

        //act
        client.sendEventAsync("", mockedMessage, mockedIotHubEventCallback, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_001: [If the provided outputName is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void sendEventAsyncWithOutputThrowsForNullOutputName() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);

        //act
        client.sendEventAsync(null, mockedMessage, mockedIotHubEventCallback, new Object());
    }

    //Tests_SRS_MODULECLIENT_34_002: [This function shall set the provided message with the provided outputName, device id, and module id properties.]
    //Tests_SRS_MODULECLIENT_34_003: [This function shall invoke super.sendEventAsync(message, callback, callbackContext).]
    @Test
    public void sendEventAsyncSuccess() throws URISyntaxException
    {
        //arrange
        baseExpectations();
        ModuleClient client = new ModuleClient("some connection string", IotHubClientProtocol.MQTT);
        final String expectedOutputName = "some output name";
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
        client.sendEventAsync(expectedOutputName, mockedMessage, mockedIotHubEventCallback, new Object());

        //assert
        new Verifications()
        {
            {
                mockedMessage.setOutputName(expectedOutputName);
                times = 1;

                mockedMessage.setConnectionDeviceId(expectedDeviceId);
                times = 1;

                mockedMessage.setConnectionModuleId(expectedModuleId);
                times = 1;

                mockedDeviceIO.sendEventAsync(mockedMessage, mockedIotHubEventCallback, any, anyString);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_008: [If the provided protocol is not MQTT, AMQPS, MQTT_WS, or AMQPS_WS, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void x509ConstructorThrowsForHTTP() throws URISyntaxException
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
    public void x509ConstructorThrowsForConnectionStringWithoutModuleId() throws URISyntaxException
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
                mockedDeviceClientConfig.setMessageCallback(expectedInputName, mockedMessageCallback, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
    // hostname, authScheme, gatewayHostname, and apiVersion. If any of these other than apiVersion or gatewayHostname is missing,
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
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);

        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
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
    // hostname, authScheme, gatewayHostname, and apiVersion. If any of these other than apiVersion or gatewayHostname is missing,
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
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);

        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
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
    // hostname, authScheme, gatewayHostname, and apiVersion. If any of these other than apiVersion or gatewayHostname is missing,
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
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);

        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
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
    // hostname, authScheme, gatewayHostname, and apiVersion. If any of these other than apiVersion or gatewayHostname is missing,
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
        String expectedAuthScheme = null;

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);

        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
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

    //Tests_SRS_MODULECLIENT_34_014: [If the auth scheme environment variable is not "SasToken", this function shall throw a moduleClientException.]
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
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

    //Tests_SRS_MODULECLIENT_34_015: [If the environment variables do not include an API version, this function shall
    // construct a signature provider with no api version specified.]
    @Test
    public void signatureProviderWithoutApiVersionSpecified(final @Mocked System mockedSystem) throws ModuleClientException, NoSuchAlgorithmException
    {
        //arrange
        IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedIotEdgedUri = "someUri";
        String expectedApiVersion = null;
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
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);

        //assert
        new Verifications()
        {
            {
                new HttpHsmSignatureProvider(expectedIotEdgedUri);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_016: [If the environment variables does include an API version, this function shall
    // construct a signature provider with that api version.]
    //Tests_SRS_MODULECLIENT_34_017: [This function shall create an authentication provider using the created
    // signature provider, and the environment variables for deviceid, moduleid, hostname, gatewayhostname,
    // and the default time for tokens to live and the default sas token buffer time.]
    //Tests_SRS_MODULECLIENT_34_018: [This function return a new ModuleClient instance built from the created authentication provider and the provided protocol.]
    @Test
    public void signatureProviderWithApiVersionSpecified(final @Mocked System mockedSystem, @Mocked InternalClient internalClient) throws ModuleClientException, NoSuchAlgorithmException, IOException, TransportException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedIotEdgedUri = "someUri";
        final String expectedApiVersion = "1.1.1";
        final String expectedHostname = "someHostname";
        final String expectedGatewayHostname = "someGatewayHostname";
        final String expectedDeviceId = "1234";
        final String expectedModuleId = "5678";
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);

        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
        new NonStrictExpectations()
        {
            {
                mockedSystem.getenv();
                result = mockedSystemVariables;

                new HttpHsmSignatureProvider(expectedIotEdgedUri, expectedApiVersion);
                result = mockedHttpHsmSignatureProvider;

                ModuleAuthenticationWithHsm.create(mockedHttpHsmSignatureProvider, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, anyInt, anyInt);
                result = mockedModuleAuthenticationWithHsm;
            }
        };

        //act
        ModuleClient.createFromEnvironment(protocol);

        //assert
        new Verifications()
        {
            {
                new HttpHsmSignatureProvider(expectedIotEdgedUri, expectedApiVersion);
                times = 1;

                ModuleAuthenticationWithHsm.create((SignatureProvider) any, expectedDeviceId, expectedModuleId, expectedHostname, expectedGatewayHostname, anyInt, anyInt);
                times = 1;

                Deencapsulation.newInstance(ModuleClient.class,
                        new Class[] {AuthenticationProvider.class, IotHubClientProtocol.class, long.class, long.class},
                        mockedModuleAuthenticationWithHsm, protocol, anyLong, anyLong);
                times = 1;
            }
        };
    }

    //Tests_SRS_MODULECLIENT_34_014: [This function shall check for environment variables for edgedUri, deviceId, moduleId,
    // hostname, authScheme, gatewayHostname, and apiVersion. If any of these other than apiVersion or gatewayHostname is missing,
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
        String expectedAuthScheme = Deencapsulation.getField(ModuleClient.class, "SasTokenAuthScheme");

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotHubHostnameVariableName").toString(), expectedHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "DeviceIdVariableName").toString(), expectedDeviceId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "ModuleIdVariableName").toString(), expectedModuleId);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "AuthSchemeVariableName").toString(), expectedAuthScheme);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedUriVariableName").toString(), expectedIotEdgedUri);

        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "GatewayHostnameVariableName").toString(), expectedGatewayHostname);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IotEdgedApiVersionVariableName").toString(), expectedApiVersion);
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
    @Test
    public void createFromEnvironmentChecksForEnvVarOfEdgeHub(final @Mocked System mockedSystem) throws ModuleClientException, URISyntaxException
    {
        //arrange
        final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
        final String expectedEdgeHubConnectionString = "edgehubConnString";
        final String expectedIotHubConnectionString = "iothubConnString";

        final Map<String, String> mockedSystemVariables = new HashMap<>();
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "EdgehubConnectionstringVariableName").toString(), expectedEdgeHubConnectionString);
        mockedSystemVariables.put(Deencapsulation.getField(ModuleClient.class, "IothubConnectionstringVariableName").toString(), expectedIotHubConnectionString);

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

}
