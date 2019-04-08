// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.*;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/*
 * Unit tests for MqttIotHubConnection
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
    final String API_VERSION = Deencapsulation.getField(MqttIotHubConnection.class, "API_VERSION");
    final String resourceUri = "test-resource-uri";
    final int qos = 1;
    final String publishTopic = "devices/test-deviceId/messages/events/";
    final String subscribeTopic = "devices/test-deviceId/messages/devicebound/#";
    final String expectedToken = "someToken";
    final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };

    @Mocked
    private ProductInfo mockedProductInfo;

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
    private Message mockedMessage;

    @Mocked
    private IotHubSasTokenAuthenticationProvider mockedSasTokenAuthenticationProvider;

    @Mocked
    private Queue<DeviceClientConfig> mockedQueue;

    @Mocked
    private IotHubListener mockedIotHubListener;

    @Mocked
    private TransportException mockedTransportException;

    @Mocked
    private ProtocolException mockedProtocolConnectionStatusException;

    @Mocked
    private IotHubServiceException mockedIotHubConnectionStatusException;

    @Mocked
    private IotHubTransportMessage mockedTransportMessage;

    @Mocked
    private MessageCallback mockedMessageCallback;

    @Mocked
    private ScheduledExecutorService mockedScheduledExecutorService;

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_001: [The constructor shall save the configuration.]
    @Test
    public void constructorSavesCorrectConfigAndListener() throws IOException, TransportException
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
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsEmpty() throws TransportException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = "";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfConfigIsNull() throws TransportException
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = "";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
            }
        };

        new MqttIotHubConnection(null);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsNull() throws TransportException
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
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIDIsEmpty() throws TransportException
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
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIDIsNull() throws TransportException
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
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfSasTokenIsEmpty() throws TransportException
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
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfSasTokenIsNull() throws TransportException
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
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_004: [The function shall establish an MQTT connection with an IoT Hub
    // using the provided host name, user name, device ID, and sas token.]
    // Tests_SRS_MQTTIOTHUBCONNECTION_25_019: [The function shall establish an MQTT connection with a server uri as ssl://<hostName>:8883 if websocket was not enabled.]
    @Test
    public void openEstablishesConnectionUsingCorrectConfig() throws IOException, TransportException
    {
        final String expectedSasToken = "someToken";
        final String serverUri = SSL_PREFIX + iotHubHostName + SSL_PORT_SUFFIX;
        baseExpectations();

        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedSasToken;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        final String actualIotHubUserName = Deencapsulation.getField(connection, "iotHubUserName");

        String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.USER_AGENT_STRING, "UTF-8").replaceAll("\\+", "%20");
        assertTrue(actualIotHubUserName.contains(iotHubHostName + "/" + deviceId + "/" + API_VERSION));

        final String actualUserPassword = Deencapsulation.getField(connection, "iotHubUserPassword");

        assertEquals(expectedSasToken, actualUserPassword);

        IotHubConnectionStatus expectedState = IotHubConnectionStatus.CONNECTED;
        IotHubConnectionStatus actualState =  Deencapsulation.getField(connection, "state");
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
    public void openEstablishesWSConnectionUsingCorrectConfig() throws IOException, TransportException
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
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedToken;
                mockConfig.isUseWebsocket();
                result = true;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        final String actualIotHubUserName = Deencapsulation.getField(connection, "iotHubUserName");

        assertTrue(actualIotHubUserName.contains(iotHubHostName + "/" + deviceId + "/" + API_VERSION + "&"));

        String actualUserPassword = Deencapsulation.getField(connection, "iotHubUserPassword");

        assertEquals(expectedToken, actualUserPassword);

        IotHubConnectionStatus expectedState = IotHubConnectionStatus.CONNECTED;
        IotHubConnectionStatus actualState =  Deencapsulation.getField(connection, "state");
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
    // the function shall throw a TransportException.]
    @Test(expected = TransportException.class)
    public void openThrowsTransportExceptionIfConnectionFails() throws IOException, TransportException
    {
        baseExpectations();
        final String serverUri = SSL_PREFIX + iotHubHostName + SSL_PORT_SUFFIX;

        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedToken;
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
            connection.open(mockedQueue, mockedScheduledExecutorService);
        }
        catch (Exception e)
        {
            new Verifications()
            {
                {
                    new MqttMessaging(mockedMqttConnection, anyString, mockedIotHubListener, null, null, anyString, anyBoolean);
                    times = 0;
                    Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                    times = 0;
                    new MqttDeviceTwin(mockedMqttConnection, anyString);
                    times = 0;
                    new MqttDeviceMethod(mockedMqttConnection, anyString);
                    times = 0;
                    mockDeviceMessaging.start();
                    times = 0;
                }
            };

            throw e;
        }
    }

    @Test(expected = TransportException.class)
    public void openThrowsTransportExceptionIfConnectionFailsInMethod() throws IOException, TransportException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
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
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedToken;
                new MqttMessaging(mockedMqttConnection, anyString, (IotHubListener) any, null, null, anyString, anyBoolean);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                result = null;
                new MqttDeviceMethod(mockedMqttConnection, anyString);
                result = new IOException(anyString);
            }
        };

        try
        {
            MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
            connection.open(mockedQueue, mockedScheduledExecutorService);
        }
        catch (TransportException e)
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

    @Test(expected = TransportException.class)
    public void openThrowsTransportExceptionIfConnectionFailsInTwin() throws IOException, TransportException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
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
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedToken;
                new MqttMessaging(mockedMqttConnection, anyString, (IotHubListener) any, null, null, anyString, anyBoolean);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                new MqttDeviceMethod(mockedMqttConnection, anyString);
                result = mockDeviceMethod;
                new MqttDeviceTwin(mockedMqttConnection, anyString);
                result = new IOException(anyString);
            }
        };

        try
        {
            MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
            connection.open(mockedQueue, mockedScheduledExecutorService);
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
    public void openDoesNothingIfAlreadyOpened() throws IOException, TransportException
    {
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        connection.open(mockedQueue, mockedScheduledExecutorService);

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
    public void closeClosesMqttConnection() throws IOException, TransportException
    {
        baseExpectations();
        openExpectations();


        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        connection.close();

        IotHubConnectionStatus expectedState = IotHubConnectionStatus.DISCONNECTED;
        IotHubConnectionStatus actualState =  Deencapsulation.getField(connection, "state");
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

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_022: [If the list of device client configuration objects is larger than 1, this function shall throw an UnsupportedOperationException.]
    @Test (expected = UnsupportedOperationException.class)
    public void openThrowsForMultiplexing() throws TransportException
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Queue<DeviceClientConfig> configs = new ConcurrentLinkedQueue<>();
        configs.add(mockConfig);
        configs.add(mockConfig);

        //act
        connection.open(configs, mockedScheduledExecutorService);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_021: [If a TransportException is encountered while closing the three clients, this function shall set this object's state to closed and then rethrow the exception.]
    @Test
    public void closeThrowsHandled() throws IOException, TransportException
    {
        //arrange
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        new NonStrictExpectations()
        {
            {
                mockDeviceMethod.stop();
                result = mockedTransportException;
            }
        };

        boolean exceptionRethrown = false;

        //act
        try
        {
            connection.close();
        }
        catch (TransportException e)
        {
            exceptionRethrown = true;
        }

        //assert
        assertTrue(exceptionRethrown);
        IotHubConnectionStatus actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(IotHubConnectionStatus.DISCONNECTED, actualState);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionNotYetOpened() throws IOException, TransportException
    {
        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.close();

        IotHubConnectionStatus expectedState = IotHubConnectionStatus.DISCONNECTED;
        IotHubConnectionStatus actualState =  Deencapsulation.getField(connection, "state");
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
    public void closeDoesNothingIfConnectionAlreadyClosed() throws IOException, TransportException
    {
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
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
    public void sendEventSendsMessageCorrectlyToIotHub() throws IOException, TransportException
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
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        IotHubStatusCode result = connection.sendMessage(mockedMessage);

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
    public void sendEventReturnsBadFormatIfMessageIsNull() throws IOException, TransportException
    {
        baseExpectations();
        openExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        IotHubStatusCode result = connection.sendMessage(null);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
    // the function shall return status code BAD_FORMAT.]
    @Test
    public void sendEventReturnsBadFormatIfMessageHasNullBody() throws IOException, TransportException
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
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);

        connection.open(mockedQueue, mockedScheduledExecutorService);
        IotHubStatusCode result = connection.sendMessage(null);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_010: [If the message is null or empty,
    // the function shall return status code BAD_FORMAT.]
    @Test
    public void sendEventReturnsBadFormatIfMessageHasEmptyBody() throws IOException, TransportException
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
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        IotHubStatusCode result = connection.sendMessage(mockedMessage);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendEventFailsIfConnectionNotYetOpened() throws TransportException
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
        connection.sendMessage(mockedMessage);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed, the function shall throw a IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendEventFailsIfConnectionClosed() throws TransportException
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
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);

        connection.open(mockedQueue, mockedScheduledExecutorService);
        connection.close();
        connection.sendMessage(mockedMessage);
    }

    @Test
    public void sendEventSendsDeviceTwinMessage(@Mocked final IotHubTransportMessage mockDeviceTwinMsg) throws IOException, TransportException
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
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        IotHubStatusCode result = connection.sendMessage(mockDeviceTwinMsg);

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
    public void sendEventSendsDeviceMethodMessage(@Mocked final IotHubTransportMessage mockDeviceMethodMsg) throws IOException, TransportException
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
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        IotHubStatusCode result = connection.sendMessage(mockDeviceMethodMsg);

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
    public void receiveMessageSucceeds() throws IOException, TransportException
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
                result = mockedTransportMessage;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        Message message = Deencapsulation.invoke(connection, "receiveMessage");
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
    public void receiveDeviceTwinMessageSucceeds() throws IOException, TransportException
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
                result = mockedTransportMessage;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        Message message = Deencapsulation.invoke(connection, "receiveMessage");
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
    public void receiveDeviceMethodMessageSucceeds() throws IOException, TransportException
    {
        baseExpectations();
        openExpectations();
        final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = mockedTransportMessage;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        Message message = Deencapsulation.invoke(connection, "receiveMessage");
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

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_027: [If this function is called while using websockets and x509 authentication, an UnsupportedOperationException shall be thrown.]
    @Test (expected = UnsupportedOperationException.class)
    public void websocketWithX509ThrowsAtOpen() throws TransportException
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
        connection.open(mockedQueue, mockedScheduledExecutorService);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_030: [This function shall instantiate this object's MqttMessaging object with this object as the listeners.]
    @Test
    public void openSavesListenerToMessagingClient() throws IOException, TransportException
    {
        //arrange
        final String expectedSasToken = "someToken";
        baseExpectations();

        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedSasToken;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        final MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);

        //act
        connection.open(mockedQueue, mockedScheduledExecutorService);

        //assert
        new Verifications()
        {
            {
                new MqttMessaging((MqttConnection) any, anyString, (IotHubListener) any, null, null, anyString, anyBoolean);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_065: [If the config contains a module id, this function shall create the clientId for the connection to be <deviceId>/<moduleId>.]
    @Test
    public void openWithModuleId() throws IOException, TransportException
    {
        //arrange
        final String expectedSasToken = "someToken";
        final String expectedModuleId = "someModule";
        final String expectedClientId = deviceId + "/" + expectedModuleId;
        final String expectedUserName = "hostname.com/" + expectedClientId + "/" + API_VERSION + "&" + "DeviceClientType=someUserAgentString";
        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedSasToken;
                mockConfig.isUseWebsocket();
                result = false;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getModuleId();
                result = expectedModuleId;
                mockConfig.getIotHubHostname();
                result = "hostname.com";
                mockConfig.getIotHubName();
                result = "hostname";
                mockConfig.getProductInfo();
                result = mockedProductInfo;
                mockedProductInfo.getUserAgentString();
                result = "someUserAgentString";
            }
        };

        final MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);

        //act
        connection.open(mockedQueue, mockedScheduledExecutorService);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, anyString, anyString, expectedUserName, anyString, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_065: [If the connection opens successfully, this function shall notify the listener that connection was established.]
    @Test
    public void openNotifiesListenerIfConnectionOpenedSuccessfully() throws IOException, TransportException
    {
        //arrange
        final String expectedSasToken = "someToken";
        baseExpectations();

        new Expectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = expectedSasToken;
                mockConfig.isUseWebsocket();
                result = false;
            }
        };

        final MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);

        //act
        connection.open(mockedQueue, mockedScheduledExecutorService);

        //assert
        new Verifications()
        {
            {
                mockedIotHubListener.onConnectionEstablished(anyString);
                times = 1;
            }
        };
    }


    //Tests_SRS_MQTTIOTHUBCONNECTION_34_049: [If the provided listener object is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void setListenerThrowsForNullListener() throws TransportException
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);

        //act
        connection.setListener(null);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_050: [This function shall save the provided listener object.]
    @Test
    public void setListenerSavesListener() throws TransportException
    {
        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);

        //act
        connection.setListener(mockedIotHubListener);

        //assert
        IotHubListener actualListener = Deencapsulation.getField(connection, "listener");
        assertEquals(mockedIotHubListener, actualListener);
    }


    //Tests_SRS_MQTTIOTHUBCONNECTION_34_051: [If this object has not received the provided message from the service, this function shall throw a TransportException.]
    @Test (expected = TransportException.class)
    public void sendMessageResultThrowsWhenMessageNotReceivedFirst() throws TransportException, IOException
    {
        //arrange
        baseExpectations();
        openExpectations();
        final IotHubMessageResult expectedResult = IotHubMessageResult.COMPLETE;
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();
        Deencapsulation.setField(connection, "receivedMessagesToAcknowledge", receivedMessagesToAcknowledge);

        //act
        connection.sendMessageResult(mockedTransportMessage, expectedResult);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_057: [If the provided message or result is null, this function shall throw a TransportException.]
    @Test (expected = TransportException.class)
    public void sendMessageResultThrowsForNullMessage() throws TransportException, IOException
    {
        //arrange
        baseExpectations();
        openExpectations();
        final IotHubMessageResult expectedResult = IotHubMessageResult.COMPLETE;
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        //act
        connection.sendMessageResult(null, expectedResult);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_057: [If the provided message or result is null, this function shall throw a TransportException.]
    @Test (expected = TransportException.class)
    public void sendMessageResultThrowsForNullResult() throws TransportException, IOException
    {
        //arrange
        baseExpectations();
        openExpectations();
        final IotHubMessageResult expectedResult = IotHubMessageResult.COMPLETE;
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);

        //act
        connection.sendMessageResult(mockedTransportMessage, null);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_053: [If the provided message has message type DEVICE_METHODS, this function shall invoke the methods client to send the ack and return the result.]
    //Tests_SRS_MQTTIOTHUBCONNECTION_34_056: [If the ack was sent successfully, this function shall remove the provided message from the saved map of messages to acknowledge.]
    //Tests_SRS_MQTTIOTHUBCONNECTION_34_052: [If this object has received the provided message from the service, this function shall retrieve the Mqtt messageId for that message.]
    @Test
    public void sendMessageResultForMethods() throws TransportException, IOException
    {
        //arrange
        baseExpectations();
        openExpectations();
        final IotHubMessageResult expectedResult = IotHubMessageResult.COMPLETE;
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        final int expectedMessageId = 12;
        Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();
        receivedMessagesToAcknowledge.put(mockedTransportMessage, expectedMessageId);
        Deencapsulation.setField(connection, "receivedMessagesToAcknowledge", receivedMessagesToAcknowledge);
        new NonStrictExpectations()
        {
            {
                mockedTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;

                Deencapsulation.invoke(mockDeviceMethod, "sendMessageAcknowledgement", expectedMessageId);
                result = true;

                Deencapsulation.invoke(mockDeviceTwin, "sendMessageAcknowledgement", expectedMessageId);
                result = true;

                Deencapsulation.invoke(mockDeviceMessaging, "sendMessageAcknowledgement", expectedMessageId);
                result = true;
            }
        };


        //act
        boolean sendMessageResult = connection.sendMessageResult(mockedTransportMessage, expectedResult);

        //assert
        receivedMessagesToAcknowledge = Deencapsulation.getField(connection, "receivedMessagesToAcknowledge");
        assertTrue(receivedMessagesToAcknowledge.isEmpty());
        assertTrue(sendMessageResult);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockDeviceMethod, "sendMessageAcknowledgement", expectedMessageId);
                times = 1;

                Deencapsulation.invoke(mockDeviceTwin, "sendMessageAcknowledgement", expectedMessageId);
                times = 0;

                Deencapsulation.invoke(mockDeviceMessaging, "sendMessageAcknowledgement", expectedMessageId);
                times = 0;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_054: [If the provided message has message type DEVICE_TWIN, this function shall invoke the twin client to send the ack and return the result.]
    @Test
    public void sendMessageResultForTwin() throws TransportException, IOException
    {
        //arrange
        baseExpectations();
        openExpectations();
        final IotHubMessageResult expectedResult = IotHubMessageResult.COMPLETE;
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        final int expectedMessageId = 12;
        Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();
        receivedMessagesToAcknowledge.put(mockedTransportMessage, expectedMessageId);
        Deencapsulation.setField(connection, "receivedMessagesToAcknowledge", receivedMessagesToAcknowledge);
        new NonStrictExpectations()
        {
            {
                mockedTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;

                Deencapsulation.invoke(mockDeviceMethod, "sendMessageAcknowledgement", expectedMessageId);
                result = true;

                Deencapsulation.invoke(mockDeviceTwin, "sendMessageAcknowledgement", expectedMessageId);
                result = true;

                Deencapsulation.invoke(mockDeviceMessaging, "sendMessageAcknowledgement", expectedMessageId);
                result = true;
            }
        };


        //act
        boolean sendMessageResult = connection.sendMessageResult(mockedTransportMessage, expectedResult);

        //assert
        assertTrue(sendMessageResult);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockDeviceMethod, "sendMessageAcknowledgement", expectedMessageId);
                times = 0;

                Deencapsulation.invoke(mockDeviceTwin, "sendMessageAcknowledgement", expectedMessageId);
                times = 1;

                Deencapsulation.invoke(mockDeviceMessaging, "sendMessageAcknowledgement", expectedMessageId);
                times = 0;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_055: [If the provided message has message type other than DEVICE_METHODS and DEVICE_TWIN, this function shall invoke the telemetry client to send the ack and return the result.]
    @Test
    public void sendMessageResultForTelemetry() throws TransportException, IOException
    {
        //arrange
        baseExpectations();
        openExpectations();
        final IotHubMessageResult expectedResult = IotHubMessageResult.COMPLETE;
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "listener", mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        final int expectedMessageId = 12;
        Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = new ConcurrentHashMap<>();
        receivedMessagesToAcknowledge.put(mockedTransportMessage, expectedMessageId);
        Deencapsulation.setField(connection, "receivedMessagesToAcknowledge", receivedMessagesToAcknowledge);
        new NonStrictExpectations()
        {
            {
                mockedTransportMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;

                Deencapsulation.invoke(mockDeviceMethod, "sendMessageAcknowledgement", expectedMessageId);
                result = true;

                Deencapsulation.invoke(mockDeviceTwin, "sendMessageAcknowledgement", expectedMessageId);
                result = true;

                Deencapsulation.invoke(mockDeviceMessaging, "sendMessageAcknowledgement", expectedMessageId);
                result = true;
            }
        };


        //act
        boolean sendMessageResult = connection.sendMessageResult(mockedTransportMessage, expectedResult);

        //assert
        assertTrue(sendMessageResult);
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockDeviceMethod, "sendMessageAcknowledgement", expectedMessageId);
                times = 0;

                Deencapsulation.invoke(mockDeviceTwin, "sendMessageAcknowledgement", expectedMessageId);
                times = 0;

                Deencapsulation.invoke(mockDeviceMessaging, "sendMessageAcknowledgement", expectedMessageId);
                times = 1;
            }
        };
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_058: [This function shall attempt to receive a message.]
    //Tests_SRS_MQTTIOTHUBCONNECTION_34_060: [If a transport message is successfully received, and the message has a type of DEVICE_TWIN, this function shall set the callback and callback context of this object from the saved values in config for methods.]
    //Tests_SRS_MQTTIOTHUBCONNECTION_34_063: [If a transport message is successfully received, this function shall notify its listener that a message was received and provide the received message.]
    @Test
    public void onMessageArrivedReceivesMessageForTwin() throws TransportException, IOException
    {
        //arrange
        final int expectedMessageId = 2000;
        final Object callbackContext = new Object();
        baseExpectations();
        openExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        new StrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = null;

                mockDeviceTwin.receive();
                result = mockedTransportMessage;

                mockedTransportMessage.getMessageType();
                result = MessageType.DEVICE_TWIN;

                mockConfig.getDeviceTwinMessageCallback();
                result = mockedMessageCallback;

                mockedTransportMessage.setMessageCallback(mockedMessageCallback);

                mockConfig.getDeviceTwinMessageContext();
                result = callbackContext;

                mockedTransportMessage.setMessageCallbackContext(callbackContext);

                mockedIotHubListener.onMessageReceived(mockedTransportMessage, null);
            }
        };

        //act
        connection.onMessageArrived(expectedMessageId);

        //assert
        Map<IotHubTransportMessage, Integer> receivedMessagesToAcknowledge = Deencapsulation.getField(connection, "receivedMessagesToAcknowledge");
        assertEquals(1, receivedMessagesToAcknowledge.size());
        assertTrue(receivedMessagesToAcknowledge.containsKey(mockedTransportMessage));
        assertEquals(expectedMessageId, (int) receivedMessagesToAcknowledge.get(mockedTransportMessage));

    }


    //Tests_SRS_MQTTIOTHUBCONNECTION_34_061: [If a transport message is successfully received, and the message has a type of DEVICE_METHODS, this function shall set the callback and callback context of this object from the saved values in config for twin.]
    @Test
    public void onMessageArrivedReceivesMessageForMethods() throws TransportException, IOException
    {
        //arrange
        final int expectedMessageId = 2000;
        final Object callbackContext = new Object();
        baseExpectations();
        openExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        new StrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = mockedTransportMessage;

                mockedTransportMessage.getMessageType();
                result = MessageType.DEVICE_METHODS;

                mockConfig.getDeviceMethodsMessageCallback();
                result = mockedMessageCallback;

                mockedTransportMessage.setMessageCallback(mockedMessageCallback);

                mockConfig.getDeviceMethodsMessageContext();
                result = callbackContext;

                mockedTransportMessage.setMessageCallbackContext(callbackContext);

                mockedIotHubListener.onMessageReceived(mockedTransportMessage, null);
            }
        };

        //act
        connection.onMessageArrived(expectedMessageId);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_062: [If a transport message is successfully received, and the message has a type of DEVICE_TELEMETRY, this function shall set the callback and callback context of this object from the saved values in config for telemetry.]
    @Test
    public void onMessageArrivedReceivesMessageForTelemetry() throws TransportException, IOException
    {
        //arrange
        final int expectedMessageId = 2000;
        final Object callbackContext = new Object();
        baseExpectations();
        openExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.setListener(mockedIotHubListener);
        connection.open(mockedQueue, mockedScheduledExecutorService);
        new StrictExpectations()
        {
            {
                mockDeviceMethod.receive();
                result = null;

                mockDeviceTwin.receive();
                result = null;

                mockDeviceMessaging.receive();
                result = mockedTransportMessage;

                mockedTransportMessage.getMessageType();
                result = MessageType.DEVICE_TELEMETRY;

                mockedTransportMessage.getInputName();
                result = "inputName";

                mockConfig.getDeviceTelemetryMessageCallback("inputName");
                result = mockedMessageCallback;

                mockedTransportMessage.setMessageCallback(mockedMessageCallback);

                mockedTransportMessage.getInputName();
                result = "inputName";

                mockConfig.getDeviceTelemetryMessageContext("inputName");
                result = callbackContext;

                mockedTransportMessage.setMessageCallbackContext(callbackContext);

                mockedIotHubListener.onMessageReceived(mockedTransportMessage, null);
            }
        };

        //act
        connection.onMessageArrived(expectedMessageId);
    }

    //Tests_SRS_MQTTIOTHUBCONNECTION_34_064: [This function shall return the saved connectionId.]
    @Test
    public void getConnectionIdReturnsSavedConnectionId() throws TransportException
    {
        //arrange
        String expectedConnectionId = "1234";
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        Deencapsulation.setField(connection, "connectionId", expectedConnectionId);

        //act
        String actualConnectionId = connection.getConnectionId();

        //assert
        assertEquals(expectedConnectionId, actualConnectionId);
    }

    private void baseExpectations()
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname(); result = iotHubHostName;
                mockConfig.getIotHubName(); result = hubName;
                mockConfig.getDeviceId(); result = deviceId;

                mockConfig.getProductInfo();
                result = mockedProductInfo;

                mockedProductInfo.getUserAgentString();
                result = "some user agent string";

                mockedMessage.getBytes();
                result = expectedMessageBody;

                mockedMessage.getMessageType();
                result = MessageType.UNKNOWN;
            }
        };
    }

    private void openExpectations() throws TransportException
    {
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(MqttConnection.class, new Class[] {String.class, String.class, String.class, String.class, SSLContext.class}, anyString, anyString, anyString, anyString, any);
                result = mockedMqttConnection;
                new MqttMessaging(mockedMqttConnection, anyString, (IotHubListener) any, null, null, anyString, anyBoolean);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockedMqttConnection, "setMqttCallback", mockDeviceMessaging);
                new MqttDeviceTwin(mockedMqttConnection, anyString);
                result = mockDeviceTwin;
                new MqttDeviceMethod(mockedMqttConnection, anyString);
                result = mockDeviceMethod;
                mockDeviceMessaging.start();
                result = null;
            }
        };
    }
}
