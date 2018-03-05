// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.*;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLEncoder;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/* Unit tests for MqttIotHubConnection
 * Code coverage: 100% methods, 95% lines
 */
public class MqttIotHubConnectionTest
{
    private static final String SSL_PREFIX = "ssl://";
    private static final String SSL_PORT_SUFFIX = ":8883";
    final String iotHubHostName = "test.host.name";
    final String hubName = "test.iothub";
    final String deviceId = "test-deviceId";
    final String deviceKey = "test-devicekey?&test";
    final String API_VERSION = "api-version=2016-11-14";
    final String resourceUri = "test-resource-uri";
    final int qos = 1;
    final String publishTopic = "devices/test-deviceId/messages/events/";
    final String subscribeTopic = "devices/test-deviceId/messages/devicebound/#";
    final String expectedToken = "someToken";
    final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };

    @Mocked
    private DeviceClientConfig mockConfig;

    @Mocked
    private MqttDeviceTwin mockDeviceTwin;

    @Mocked
    private MqttMessaging mockDeviceMessaging;

    @Mocked
    private MqttDeviceMethod mockDeviceMethod;

    @Mocked
    private IotHubUri mockIotHubUri;

    @Mocked
    private SSLContext mockSslContext;

    @Mocked
    private MqttConnection mockedMqttConnection;

    @Mocked
    private IotHubConnectionStateCallback mockConnectionStateCallback;

    @Mocked
    private MqttConnectionStateListener mockedMqttConnectionStateListener;

    @Mocked
    private Message mockedMessage;

    @Mocked
    private IotHubSasTokenAuthenticationProvider mockedSasTokenAuthenticationProvider;

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_001: [The constructor shall save the configuration.]
    @Test
    public void constructorSavesCorrectConfigAndListener() throws IOException
    {

        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);

        DeviceClientConfig actualClientConfig = Deencapsulation.getField(connection, "config");
        DeviceClientConfig expectedClientConfig = mockConfig;
        assertEquals(expectedClientConfig, actualClientConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsEmpty()
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = "";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsNull()
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = null;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIDIsEmpty()
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = "";
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIDIsNull()
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = null;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfSasTokenIsEmpty()
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = "";
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfSasTokenIsNull()
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = null;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_004: [The function shall establish an MQTT connection with an IoT Hub
    // using the provided host name, user name, device ID, and sas token.]
    // Tests_SRS_MQTTIOTHUBCONNECTION_25_019: [The function shall establish an MQTT connection with a server uri as ssl://<hostName>:8883 if websocket was not enabled.]
    @Test
    public void openEstablishesConnectionUsingCorrectConfig() throws IOException
    {
        final String expectedSasToken = "someToken";
        final String serverUri = SSL_PREFIX + iotHubHostName + SSL_PORT_SUFFIX;
        baseExpectations();

        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = expectedSasToken;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        final String actualIotHubUserName = Deencapsulation.getField(connection, "iotHubUserName");

        String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION, "UTF-8");
        assertEquals(iotHubHostName + "/" + deviceId + "/" + API_VERSION + "&" + clientIdentifier, actualIotHubUserName);

        final String actualUserPassword = Deencapsulation.getField(connection, "iotHubUserPassword");

        assertEquals(expectedSasToken, actualUserPassword);

        State expectedState = State.OPEN;
        State actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, serverUri, deviceId, any, any, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_25_018: [The function shall establish an MQTT WS connection with a server uri as wss://<hostName>/$iothub/websocket?iothub-no-client-cert=true if websocket was enabled.]
    @Test
    public void openEstablishesWSConnectionUsingCorrectConfig() throws IOException
    {
        final String WS_RAW_PATH = "/$iothub/websocket";
        final String WS_QUERY = "?iothub-no-client-cert=true";
        final String WS_SSLPrefix = "wss://";
        final String serverUri = WS_SSLPrefix + iotHubHostName + WS_RAW_PATH + WS_QUERY;

        baseExpectations();
        openExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = expectedToken;
                mockConfig.isUseWebsocket();
                result = true;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        final String actualIotHubUserName = Deencapsulation.getField(connection, "iotHubUserName");

        String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION, "UTF-8");
        assertEquals(iotHubHostName + "/" + deviceId + "/" + API_VERSION + "&" + clientIdentifier, actualIotHubUserName);

        String actualUserPassword = Deencapsulation.getField(connection, "iotHubUserPassword");

        assertEquals(expectedToken, actualUserPassword);

        State expectedState = State.OPEN;
        State actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
               Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, serverUri, deviceId, any, any, any);
               times = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_005: [If an MQTT connection is unable to be established for any reason,
    // the function shall throw an IOException.]
    @Test(expected = IOException.class)
    public void openThrowsIOExceptionIfConnectionFails() throws IOException
    {
        baseExpectations();
        final String serverUri = SSL_PREFIX + iotHubHostName + SSL_PORT_SUFFIX;

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = expectedToken;
                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = "someToken";
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, serverUri, deviceId, any, any, mockSslContext);
                result = new IOException();
            }
        };

        try
        {
            MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
            connection.open();
        }
        catch (Exception e)
        {
            new Verifications()
            {
                {
                    new MqttMessaging(mockedMqttConnection, anyString, mockedMqttConnectionStateListener);
                    times = 0;
                    Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                    times = 0;
                    Deencapsulation.invoke(mockDeviceMessaging, "setDeviceClientConfig", mockConfig);
                    times = 0;
                    new MqttDeviceTwin(mockedMqttConnection);
                    times = 0;
                    new MqttDeviceMethod(mockedMqttConnection);
                    times = 0;
                    mockDeviceMessaging.start();
                    times = 0;
                }
            };

            throw e;
        }
    }

    @Test(expected = IOException.class)
    public void openThrowsIOExceptionIfConnectionFailsInMethod() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = "someToken";
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, any, any, any, any, mockSslContext);
                result = mockedMqttConnection;
            }
        };

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = expectedToken;
                new MqttMessaging(mockedMqttConnection, anyString, (MqttConnectionStateListener) any);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                result = null;
                new MqttDeviceMethod(mockedMqttConnection);
                result = new IOException(anyString);
            }
        };

        try
        {
            MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
            connection.open();
        }
        catch (IOException e)
        {
            new Verifications()
            {
                {
                    mockDeviceMessaging.stop();
                    times = 1;
                }
            };

            throw e;
        }
    }

    @Test(expected = IOException.class)
    public void openThrowsIOExceptionIfConnectionFailsInTwin() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = "someToken";
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, any, any, any, any, mockSslContext);
                result = mockedMqttConnection;
            }
        };

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = expectedToken;
                new MqttMessaging(mockedMqttConnection, anyString, (MqttConnectionStateListener) any);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                Deencapsulation.invoke(mockDeviceMessaging, "setDeviceClientConfig", mockConfig);
                new MqttDeviceMethod(mockedMqttConnection);
                result = mockDeviceMethod;
                new MqttDeviceTwin(mockedMqttConnection);
                result = new IOException(anyString);
            }
        };

        try
        {
            MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
            connection.open();
        }
        catch (Exception e)
        {
            new Verifications()
            {
                {
                    mockDeviceMessaging.stop();
                    times = 1;
                    mockDeviceMethod.stop();
                    times = 1;
                }
            };

            throw e;
        }
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT connection is already open, the function shall do nothing.]
    @Test
    public void openDoesNothingIfAlreadyOpened() throws IOException
    {
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        connection.open();

        new Verifications()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, any, any, any, any, any);
                maxTimes = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_006: [The function shall close the MQTT connection.]
    @Test
    public void closeClosesMqttConnection() throws IOException
    {
        baseExpectations();
        openExpectations();


        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        connection.close();

        State expectedState = State.CLOSED;
        State actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(expectedState, actualState);

        MqttDeviceMethod actualDeviceMethods = Deencapsulation.getField(connection, "deviceMethod");
        assertNull(actualDeviceMethods);

        MqttDeviceTwin actualDeviceTwin = Deencapsulation.getField(connection, "deviceTwin");
        assertNull(actualDeviceTwin);

        MqttMessaging actualDeviceMessaging = Deencapsulation.getField(connection, "deviceMessaging");
        assertNull(actualDeviceMessaging);

        new Verifications()
        {
            {
                mockDeviceMessaging.stop();
                times = 1;
                mockDeviceMethod.stop();
                times = 1;
                mockDeviceTwin.stop();
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionNotYetOpened() throws IOException
    {
        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.close();

        State expectedState = State.CLOSED;
        State actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                mockDeviceMessaging.stop();
                times = 0;
                mockDeviceMethod.stop();
                times = 0;
                mockDeviceTwin.stop();
                times = 0;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionAlreadyClosed() throws IOException
    {
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        connection.close();
        connection.close();

        new Verifications()
        {
            {
                mockDeviceMessaging.stop();
                maxTimes = 1;
                mockDeviceMethod.stop();
                maxTimes = 1;
                mockDeviceTwin.stop();
                maxTimes = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_008: [The function shall send an event message to the IoT Hub
    // given in the configuration.]
    // Tests_SRS_MQTTIOTHUBCONNECTION_15_009: [The function shall send the message payload.]
    // Tests_SRS_MQTTIOTHUBCONNECTION_15_011: [If the message was successfully received by the service,
    // the function shall return status code OK_EMPTY.]
    @Test
    public void sendEventSendsMessageCorrectlyToIotHub() throws IOException
    {
        baseExpectations();
        openExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = msgBody;
                mockDeviceMessaging.send(mockedMessage);
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(mockedMessage);

        assertEquals(IotHubStatusCode.OK_EMPTY, result);

        new Verifications()
        {
            {
                mockDeviceMessaging.send(mockedMessage);
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
    // the function shall return status code BAD_FORMAT.]
    @Test
    public void sendEventReturnsBadFormatIfMessageIsNull() throws IOException
    {
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(null);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
    // the function shall return status code BAD_FORMAT.]
    @Test
    public void sendEventReturnsBadFormatIfMessageHasNullBody() throws IOException
    {
        baseExpectations();
        openExpectations();

        final byte[] msgBody = null;
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = msgBody;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(null);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
    // the function shall return status code BAD_FORMAT.]
    @Test
    public void sendEventReturnsBadFormatIfMessageHasEmptyBody() throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = new byte[0];
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(mockedMessage);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendEventFailsIfConnectionNotYetOpened() throws IOException
    {
        baseExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = msgBody;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.sendEvent(mockedMessage);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendEventFailsIfConnectionClosed() throws IOException
    {
        baseExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = msgBody;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        connection.close();
        connection.sendEvent(mockedMessage);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_012: [If the message was not successfully received by the service,
    // the function shall return status code ERROR.]
    @Test
    public void sendEventReturnsErrorIfMessageNotReceived() throws IOException
    {
        baseExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockedMessage.getBytes();
                result = msgBody;
                mockDeviceMessaging.send(mockedMessage);
                result = new IOException(anyString);
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode actualStatus = connection.sendEvent(mockedMessage);

        IotHubStatusCode expectedStatus = IotHubStatusCode.ERROR;
        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void sendEventSendsDeviceTwinMessage(@Mocked final IotHubTransportMessage mockDeviceTwinMsg) throws IOException
    {
        baseExpectations();
        openExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceTwinMsg.getBytes();
                result = msgBody;
                mockDeviceTwinMsg.getMessageType();
                result = MessageType.DEVICE_TWIN;

            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(mockDeviceTwinMsg);

        assertEquals(IotHubStatusCode.OK_EMPTY, result);

        new Verifications()
        {
            {
                mockDeviceMethod.send((IotHubTransportMessage)any);
                times = 0;
                mockDeviceMessaging.send(mockDeviceTwinMsg);
                times = 0;
                mockDeviceTwin.start();
                times = 1;
                mockDeviceTwin.send(mockDeviceTwinMsg);
                times = 1;
            }
        };
    }

    @Test
    public void sendEventSendsDeviceMethodMessage(@Mocked final IotHubTransportMessage mockDeviceMethodMsg) throws IOException
    {
        baseExpectations();
        openExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethodMsg.getBytes();
                result = msgBody;
                mockDeviceMethodMsg.getMessageType();
                result = MessageType.DEVICE_METHODS;

            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(mockDeviceMethodMsg);

        assertEquals(IotHubStatusCode.OK_EMPTY, result);

        new Verifications()
        {
            {
                mockDeviceMethod.start();
                times = 1;
                mockDeviceMethod.send(mockDeviceMethodMsg);
                times = 1;
                mockDeviceMessaging.send(mockDeviceMethodMsg);
                times = 0;
                mockDeviceTwin.send(mockDeviceMethodMsg);
                times = 0;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_014: [The function shall attempt to consume a message
    // from the received messages queue.]
    @Test
    public void receiveMessageSucceeds() throws IOException
    {
        baseExpectations();
        openExpectations();
        new NonStrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = null;
                mockDeviceTwin.receive();
                result = null;
                mockDeviceMessaging.receive();
                result = mockedMessage;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        Message message = connection.receiveMessage();
        byte[] actualMessageBody = message.getBytes();

        for (int i = 0; i < expectedMessageBody.length; i++)
        {
            assertEquals(expectedMessageBody[i], actualMessageBody[i]);
        }

        new Verifications()
        {
            {
                mockDeviceTwin.receive();
                times = 1;
                mockDeviceMethod.receive();
                times = 1;
                mockDeviceMessaging.receive();
                times = 1;
            }
        };
    }

    @Test
    public void receiveDeviceTwinMessageSucceeds() throws IOException
    {
        baseExpectations();
        openExpectations();
        final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = null;
                mockDeviceTwin.receive();
                result = new IotHubTransportMessage(expectedMessageBody, MessageType.DEVICE_TWIN);
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        Message message = connection.receiveMessage();
        byte[] actualMessageBody = message.getBytes();

        assertNotNull(message);

        for (int i = 0; i < expectedMessageBody.length; i++)
        {
            assertEquals(expectedMessageBody[i], actualMessageBody[i]);
        }

        new Verifications()
        {
            {
                mockDeviceMethod.receive();
                times = 1;
                mockDeviceMessaging.receive();
                times = 0;
            }
        };
    }

    @Test
    public void receiveDeviceMethodMessageSucceeds() throws IOException
    {
        baseExpectations();
        openExpectations();
        final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = new IotHubTransportMessage(expectedMessageBody, MessageType.DEVICE_TWIN);
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        Message message = connection.receiveMessage();
        byte[] actualMessageBody = message.getBytes();

        assertNotNull(message);

        for (int i = 0; i < expectedMessageBody.length; i++)
        {
            assertEquals(expectedMessageBody[i], actualMessageBody[i]);
        }

        new Verifications()
        {
            {
                mockDeviceMethod.receive();
                times = 1;
                mockDeviceTwin.receive();
                times = 0;
                mockDeviceMessaging.receive();
                times = 0;
            }
        };
    }
    // Tests_SRS_MQTTIOTHUBCONNECTION_15_015: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void receiveMessageFailsIfConnectionClosed() throws IOException
    {
        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.receiveMessage();
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_015: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void receiveMessageFailsIfConnectionAlreadyClosed() throws IOException
    {
        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        connection.close();
        connection.receiveMessage();
    }

    // Codes_SRS__MQTTIOTHUBCONNECTION_34_016: [If any of the messaging clients throw an exception, The associated message will be removed from the queue and the exception will be propagated up to the receive task.]
    @Test (expected = IOException.class)
    public void messagingClientThrowsPropagatesUpCorrectly(@Mocked final Mqtt mockMqtt) throws IOException
    {
        final MqttDeviceMethod method = mockDeviceMethod;

        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        Deencapsulation.setField(connection, "deviceMethod", method);


        new Expectations()
        {
            {
                method.receive();
                result = new IOException();
            }
        };

        connection.receiveMessage();
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_020: [If the config has no shared access token, device key, or x509 certificates, this constructor shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorConfigMissingTokenKeyAndCertThrowsIllegalArgument()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = "someValidHost";
                mockConfig.getDeviceId();
                result = "someValidDeviceId";
                mockConfig.getIotHubName();
                result = "someValidHubName";

                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = null;
                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = null;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        //act
        new MqttIotHubConnection(mockConfig);
    }


    //Tests_SRS_MQTTIOTHUBCONNECTION_34_027: [If this function is called while using websockets and x509 authentication, an UnsupportedOperation shall be thrown.]
    @Test (expected = IOException.class)
    public void websocketWithX509ThrowsAtOpen() throws IOException
    {
        baseExpectations();

        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
                mockConfig.isUseWebsocket();
                result = true;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_030: [This function shall instantiate this object's MqttMessaging object with this object as the listener.]
    @Test
    public void openSavesListenerToMessagingClient() throws IOException
    {
        //arrange
        final String expectedSasToken = "someToken";
        baseExpectations();

        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken();
                result = expectedSasToken;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        final MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);

        //act
        connection.open();

        //assert
        new Verifications()
        {
            {
                new MqttMessaging((MqttConnection) any, anyString, (MqttConnectionStateListener) any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_028: [If this object's connection state callback is not null, this function shall fire that callback with the saved context and status CONNECTION_DROP.]
    @Test
    public void connectionDropFiresCallback()
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.invoke(connection, "registerConnectionStateCallback", mockConnectionStateCallback, new Object());

        //act
        connection.connectionLost();

        //assert
        new Verifications()
        {
            {
                mockConnectionStateCallback.execute(IotHubConnectionState.CONNECTION_DROP, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_029: [If this object's connection state callback is not null, this function shall fire that callback with the saved context and status CONNECTION_SUCCESS.]
    @Test
    public void connectionEstablishedFiresCallback()
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.invoke(connection, "registerConnectionStateCallback", mockConnectionStateCallback, new Object());

        //act
        connection.connectionEstablished();

        //assert
        new Verifications()
        {
            {
                mockConnectionStateCallback.execute(IotHubConnectionState.CONNECTION_SUCCESS, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_033: [If the provided callback object is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void registerConnectionStateCallbackThrowsForNullCallback()
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);

        //act
        Deencapsulation.invoke(connection, "registerConnectionStateCallback", new Class[] {IotHubConnectionStateCallback.class, Object.class}, null, new Object());
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_034: [This function shall save the provided callback and callback context.]
    @Test
    public void registerConnectionStateCallbackSavesCallbackAndContext()
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        final Object callbackContext = new Object();

        //act
        Deencapsulation.invoke(connection, "registerConnectionStateCallback", mockConnectionStateCallback, callbackContext);

        //assert
        IotHubConnectionStateCallback actualConnectionStateCallback = Deencapsulation.getField(connection, "stateCallback");
        Object actualConnectionStateCallbackContext = Deencapsulation.getField(connection, "stateCallbackContext");
        assertEquals(mockConnectionStateCallback, actualConnectionStateCallback);
        assertEquals(callbackContext, actualConnectionStateCallbackContext);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_035: [If the sas token saved in the config has expired and needs to be renewed, this function shall return UNAUTHORIZED.]
    //Tests_SRS_MQTTIOTHUBCONNECTION_34_036: [If the sas token saved in the config has expired and needs to be renewed and if there is a connection state callback saved, this function shall invoke that callback with Status SAS_TOKEN_EXPIRED.]
    @Test
    public void sendMessageChecksForExpiredSasToken() throws IOException
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;

                mockConfig.getSasTokenAuthentication();
                result = mockedSasTokenAuthenticationProvider;

                mockedSasTokenAuthenticationProvider.isRenewalNecessary();
                result = true;
            }
        };
        Deencapsulation.invoke(connection, "registerConnectionStateCallback", mockConnectionStateCallback, new Object());
        connection.open();

        //act
        IotHubStatusCode statusCode = connection.sendEvent(mockedMessage);

        //assert
        assertEquals(IotHubStatusCode.UNAUTHORIZED, statusCode);
        new Verifications()
        {
            {
                mockConnectionStateCallback.execute(IotHubConnectionState.SAS_TOKEN_EXPIRED, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_037: [If an IOException is encountered while closing the mqtt connection, this function shall set this object's state to CLOSED and rethrow that exception.]
    @Test
    public void closeThrowsIOExceptionSetsStateToCLOSED() throws IOException
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        new NonStrictExpectations()
        {
            {
                mockDeviceMessaging.stop();
                result = new IOException();
            }
        };

        //act
        try
        {
            connection.close();
        }
        catch (IOException e)
        {
            //expecting this exception, but not testing for it, so ignore
        }

        //assert
        State actualState = Deencapsulation.getField(connection, "state");
        assertEquals(State.CLOSED, actualState);
    }

    private void baseExpectations()
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname(); result = iotHubHostName;
                mockConfig.getIotHubName(); result = hubName;
                mockConfig.getDeviceId(); result = deviceId;
                mockConfig.getIotHubConnectionString().getSharedAccessKey();
                result = deviceKey;

                mockedMessage.getBytes();
                result = expectedMessageBody;

                mockedMessage.getMessageType();
                result = MessageType.UNKNOWN;
            }
        };
    }

    private void openExpectations() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubConnectionString().getSharedAccessToken();
                result = expectedToken;
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, anyString, anyString, anyString, anyString, any);
                result = mockedMqttConnection;
                new MqttMessaging(mockedMqttConnection, anyString, (MqttConnectionStateListener) any);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                Deencapsulation.invoke(mockDeviceMessaging, "setDeviceClientConfig", mockConfig);
                new MqttDeviceTwin(mockedMqttConnection);
                result = mockDeviceTwin;
                new MqttDeviceMethod(mockedMqttConnection);
                result = mockDeviceMethod;
                mockDeviceMessaging.start();
                result = null;
            }
        };
    }
}