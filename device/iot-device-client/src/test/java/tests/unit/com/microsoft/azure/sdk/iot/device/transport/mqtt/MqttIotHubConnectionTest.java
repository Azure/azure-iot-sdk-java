// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.State;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.mqtt.*;
import mockit.*;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/* Unit tests for MqttIotHubConnection
 * Code coverage: 100% methods, 95% lines
 */
public class MqttIotHubConnectionTest
{
    private static String sslPrefix = "ssl://";
    private static String sslPortSuffix = ":8883";
    final String iotHubHostName = "test.host.name";
    final String hubName = "test.iothub";
    final String deviceId = "test-deviceId";
    final String deviceKey = "test-devicekey?&test";
    final String API_VERSION = "api-version=2016-11-14";
    final String resourceUri = "test-resource-uri";
    final int qos = 1;
    final String publishTopic = "devices/test-deviceId/messages/events/";
    final String subscribeTopic = "devices/test-deviceId/messages/devicebound/#";

    @Mocked
    protected DeviceClientConfig mockConfig;

    @Mocked
    private MqttDeviceTwin mockDeviceTwin;

    @Mocked
    private MqttDeviceTwin[] mockDeviceTwinArray;

    @Mocked
    private MqttMessaging mockDeviceMessaging;

    @Mocked
    protected IotHubSasToken mockToken;

    @Mocked
    IotHubUri mockIotHubUri;

    @Mocked
    IotHubSSLContext mockIotHubSSLContext;

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_001: [The constructor shall save the configuration.]
    @Test
    public void constructorSavesCorrectConfig() throws IOException {

        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);

        DeviceClientConfig actualClientConfig = Deencapsulation.getField(connection, "config");
        DeviceClientConfig expectedClientConfig = mockConfig;

        assertEquals(expectedClientConfig, actualClientConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsEmpty(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = "";
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfHostNameIsNull(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = null;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIDIsEmpty(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = "";
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfDeviceIDIsNull(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = null;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfUserNameIsEmpty(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = "";
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfUserNameIsNull(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = hubName;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = null;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfSasTokenIsEmpty(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = "";
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_003: [The constructor shall throw a new IllegalArgumentException
    // if any of the parameters of the configuration is null or empty.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsIllegalArgumentExceptionIfSasTokenIsNull(){
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostName;
                mockConfig.getIotHubName();
                result = null;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getDeviceKey();
                result = deviceKey;
            }
        };

        new MqttIotHubConnection(mockConfig);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_004: [The function shall establish an MQTT connection with an IoT Hub
    // using the provided host name, user name, device ID, and sas token.]
    // Tests_SRS_MQTTIOTHUBCONNECTION_25_019: [The function shall establish an MQTT connection with a server uri as ssl://<hostName>:8883 if websocket was not enabled.]
    @Test
    public void openEstablishesConnectionUsingCorrectConfig(@Mocked final MqttDeviceMethod mockDeviceMethod) throws IOException
    {
        baseExpectations();
        openExpectations();

        new Expectations()
        {
            {
                new MqttDeviceMethod();
                result = mockDeviceMethod;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        final String actualIotHubUserName = Deencapsulation.getField(connection, "iotHubUserName");

        String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION, "UTF-8");
        assertEquals(iotHubHostName + "/" + deviceId + "/" + API_VERSION + "/" + clientIdentifier, actualIotHubUserName);

        String expectedSasToken = mockToken.toString();
        String actualUserPassword = Deencapsulation.getField(connection, "iotHubUserPassword");

        assertEquals(expectedSasToken, actualUserPassword);

        State expectedState = State.OPEN;
        State actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                new MqttDeviceMethod();
                times = 1;
                new MqttMessaging(anyString, anyString, anyString, anyString, mockIotHubSSLContext);
                mockDeviceMessaging.start();
                times = 1;
                new MqttDeviceTwin();
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

        baseExpectations();
        openExpectations();

        new NonStrictExpectations()
        {
            {
                mockConfig.isUseWebsocket();
                result = true;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        final String actualIotHubUserName = Deencapsulation.getField(connection, "iotHubUserName");

        String clientIdentifier = "DeviceClientType=" + URLEncoder.encode(TransportUtils.JAVA_DEVICE_CLIENT_IDENTIFIER + TransportUtils.CLIENT_VERSION, "UTF-8");
        assertEquals(iotHubHostName + "/" + deviceId + "/" + API_VERSION + "/" + clientIdentifier, actualIotHubUserName);

        String expectedSasToken = mockToken.toString();
        String actualUserPassword = Deencapsulation.getField(connection, "iotHubUserPassword");

        assertEquals(expectedSasToken, actualUserPassword);

        State expectedState = State.OPEN;
        State actualState =  Deencapsulation.getField(connection, "state");
        assertEquals(expectedState, actualState);

        new Verifications()
        {
            {
                new MqttMessaging(sslPrefix + iotHubHostName + sslPortSuffix, deviceId, anyString, anyString, mockIotHubSSLContext);
                times = 0;
                new MqttMessaging(WS_SSLPrefix + iotHubHostName + WS_RAW_PATH + WS_QUERY, deviceId, anyString, anyString, mockIotHubSSLContext);
                times = 1;
                mockDeviceMessaging.start();
                times = 1;
                new MqttDeviceTwin();
                times = 1;
                new MqttDeviceMethod();
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_005: [If an MQTT connection is unable to be established for any reason,
    // the function shall throw an IOException.]
    @Test(expected = IOException.class)
    public void openThrowsIOExceptionIfConnectionFails() throws IOException {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                result = mockToken;
                new MqttMessaging(sslPrefix + iotHubHostName + sslPortSuffix, deviceId, anyString, anyString, mockIotHubSSLContext);
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


                }
            };

            throw e;
        }
    }

    @Test(expected = IOException.class)
    public void openThrowsIOExceptionIfConnectionFailsInMethod(@Mocked final MqttDeviceMethod mockMethod, @Mocked final MqttMessaging mockDeviceMessaging) throws IOException {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                result = mockToken;
                new MqttMessaging(anyString, anyString, anyString, anyString, mockIotHubSSLContext);
                result = mockDeviceMessaging;
                new MqttDeviceMethod();
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
                }
            };

            throw e;
        }
    }

    @Test(expected = IOException.class)
    public void openThrowsIOExceptionIfConnectionFailsInTwin(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                result = mockToken;
                new MqttMessaging(sslPrefix + iotHubHostName + sslPortSuffix, deviceId, anyString, anyString, mockIotHubSSLContext);
                result = mockDeviceMessaging;
                new MqttDeviceMethod();
                result = mockDeviceMethods;
                new MqttDeviceTwin();
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
                    mockDeviceMethods.stop();
                    times = 1;
                }
            };

            throw e;
        }
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_006: [If the MQTT connection is already open, the function shall do nothing.]
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
                new MqttMessaging(sslPrefix + iotHubHostName + sslPortSuffix, deviceId, anyString, anyString, mockIotHubSSLContext);
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_005: [The function shall close the MQTT connection.]
    @Test
    public void closeClosesMqttConnection(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
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

        MqttDeviceTwin actualDeviceMessaging = Deencapsulation.getField(connection, "deviceMessaging");
        assertNull(actualDeviceMessaging);

        new Verifications()
        {
            {
                mockDeviceMessaging.stop();
                times = 1;
                mockDeviceMethods.stop();
                times = 1;
                mockDeviceTwin.stop();
                times = 1;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionNotYetOpened(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
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
                mockDeviceMethods.stop();
                times = 0;
                mockDeviceTwin.stop();
                times = 0;
            }
        };
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_007: [If the MQTT connection is closed, the function shall do nothing.]
    @Test
    public void closeDoesNothingIfConnectionAlreadyClosed(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
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
                mockDeviceMethods.stop();
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
    public void sendEventSendsMessageCorrectlyToIotHub(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();
        openExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = msgBody;
                mockDeviceMessaging.send(mockMsg);
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(mockMsg);

        assertEquals(IotHubStatusCode.OK_EMPTY, result);

        new Verifications()
        {
            {
                mockDeviceMessaging.send(mockMsg);
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
    public void sendEventReturnsBadFormatIfMessageHasNullBody(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();
        openExpectations();

        final byte[] msgBody = null;
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
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
    public void sendEventReturnsBadFormatIfMessageHasEmptyBody(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();

        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = new byte[0];
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode result = connection.sendEvent(mockMsg);

        assertEquals(IotHubStatusCode.BAD_FORMAT, result);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendEventFailsIfConnectionNotYetOpened(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = msgBody;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.sendEvent(mockMsg);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_013: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendEventFailsIfConnectionClosed(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = msgBody;
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        connection.close();
        connection.sendEvent(mockMsg);
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_012: [If the message was not successfully received by the service,
    // the function shall return status code ERROR.]
    @Test
    public void sendEventReturnsErrorIfMessageNotReceived(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();

        final byte[] msgBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockMsg.getBytes();
                result = msgBody;
                mockDeviceMessaging.send(mockMsg);
                result = new IOException(anyString);
            }
        };

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();
        IotHubStatusCode actualStatus = connection.sendEvent(mockMsg);

        IotHubStatusCode expectedStatus = IotHubStatusCode.ERROR;
        assertEquals(expectedStatus, actualStatus);
    }

    @Test
    public void sendEventSendsDeviceTwinMessage(@Mocked final IotHubTransportMessage mockDeviceTwinMsg,
                                                @Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
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
                mockDeviceMethods.send((IotHubTransportMessage)any);
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
    public void sendEventSendsDeviceMethodMessage(@Mocked final IotHubTransportMessage mockDeviceMethodMsg, @Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
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
                mockDeviceMethods.start();
                times = 1;
                mockDeviceMethods.send(mockDeviceMethodMsg);
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
    public void receiveMessageSucceeds(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
    {
        baseExpectations();
        openExpectations();
        final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethods.receive();
                result = null;
                mockDeviceTwin.receive();
                result = null;
                mockDeviceMessaging.receive();
                result = new Message(expectedMessageBody);
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
                mockDeviceMethods.receive();
                times = 1;
                mockDeviceMessaging.receive();
                times = 1;
            }
        };
    }

    @Test
    public void receiveDeviceTwinMessageSucceeds(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
    {
        baseExpectations();
        openExpectations();
        final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethods.receive();
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
                mockDeviceMethods.receive();
                times = 1;
                mockDeviceMessaging.receive();
                times = 0;
            }
        };
    }

    @Test
    public void receiveDeviceMethodMessageSucceeds(@Mocked final MqttDeviceMethod mockDeviceMethods) throws IOException
    {
        baseExpectations();
        openExpectations();
        final byte[] expectedMessageBody = { 0x61, 0x62, 0x63 };
        new NonStrictExpectations()
        {
            {
                mockDeviceMethods.receive();
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
                mockDeviceMethods.receive();
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
    public void receiveMessageFailsIfConnectionClosed(@Mocked final Message mockMsg) throws IOException
    {
        baseExpectations();

        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.receiveMessage();
    }

    // Tests_SRS_MQTTIOTHUBCONNECTION_15_015: [If the MQTT connection is closed,
    // the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void receiveMessageFailsIfConnectionAlreadyClosed(@Mocked final Message mockMsg) throws IOException
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
        final MqttDeviceMethod method = new MqttDeviceMethod();

        //arrange
        baseExpectations();
        MqttIotHubConnection connection = new MqttIotHubConnection(mockConfig);
        connection.open();

        Deencapsulation.setField(connection, "deviceMethod", method);

        try
        {
            final Queue<Pair<String, byte[]>> actualQueue = new ConcurrentLinkedQueue<>();
            Pair p = new MutablePair<String, byte[]>("$iothub/methods/POST/", "test".getBytes());
            actualQueue.add(p);

            Deencapsulation.setField(method, "allReceivedMessages", actualQueue);

            new Expectations()
            {
                {
                    method.peekMessage();
                    result = actualQueue;
                }
            };

            connection.receiveMessage();
        }
        catch (IOException e)
        {
            Queue<Pair<String, byte[]>> receivedMessagesQueue = Deencapsulation.getField(method, "allReceivedMessages");
            assertEquals(receivedMessagesQueue.size(), 0);
            throw e;
        }
    }

    private void baseExpectations()
    {
        new NonStrictExpectations() {
            {
                mockConfig.getIotHubHostname(); result = iotHubHostName;
                mockConfig.getIotHubName(); result = hubName;
                mockConfig.getDeviceId(); result = deviceId;
                mockConfig.getDeviceKey(); result = deviceKey;
            }
        };
    }

    private void openExpectations() throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new IotHubSasToken(mockConfig, anyLong);
                result = mockToken;
                new MqttMessaging(anyString, deviceId, anyString, anyString, mockIotHubSSLContext);
                result = mockDeviceMessaging;
                Deencapsulation.invoke(mockDeviceMessaging, "setDeviceClientConfig", mockConfig);
                new MqttDeviceTwin();
                result = mockDeviceTwin;
                mockDeviceMessaging.start();
                result = null;
            }
        };
    }
}