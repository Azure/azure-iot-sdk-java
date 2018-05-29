/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.*;
import mockit.*;
import org.junit.Test;

import java.net.URISyntaxException;

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
}
