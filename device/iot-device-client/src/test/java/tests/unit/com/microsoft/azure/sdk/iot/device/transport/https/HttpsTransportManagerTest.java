// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsSingleMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit test for https transport manager.
 * 100% methods, 95% lines covered
 */
public class HttpsTransportManagerTest
{
    @Mocked
    private DeviceClientConfig mockConfig;
    @Mocked
    private HttpsIotHubConnection mockConn;
    @Mocked
    private Message mockMsg;
    @Mocked
    private IotHubTransportMessage mockTransportMsg;
    @Mocked
    private HttpsSingleMessage mockHttpsMessage;
    @Mocked
    private ResponseMessage mockResponseMessage;
    @Mocked
    private IotHubTransportMessage mockedTransportMessage;

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_001: [The constructor shall store the device client configuration `config`.] */
    @Test
    public void constructorSucceed()
    {
        // arrange
        final DeviceClientConfig config = mockConfig;

        // act
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, config);

        // assert
        assertEquals(config, Deencapsulation.getField(httpsTransportManager, "config"));
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_002: [If the provided `config` is null, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullConfig()
    {
        // arrange
        final DeviceClientConfig config = null;

        // act
        Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, config);
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_003: [The open shall create and store a new transport connection `HttpsIotHubConnection`.] */
    @Test
    public void openSucceed()
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(httpsTransportManager, "open");

        // assert
        assertNotNull(Deencapsulation.getField(httpsTransportManager, "httpsIotHubConnection"));
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_004: [The open shall create and store a new transport connection `HttpsIotHubConnection`.] */
    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_005: [The open shall ignore the parameter `topics`.] */
    @Test
    public void openWithTopicsSucceed()
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String[] topics = new String[]{ "a", "b"};
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                times = 1;
            }
        };

        // act
        Deencapsulation.invoke(httpsTransportManager, "open", new Class<?>[] {String[].class}, (Object)topics);

        // assert
        assertNotNull(Deencapsulation.getField(httpsTransportManager, "httpsIotHubConnection"));
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_006: [The close shall destroy the transport connection `HttpsIotHubConnection`.] */
    @Test
    public void closeSucceed()
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);

        // act
        Deencapsulation.invoke(httpsTransportManager, "close");

        // assert
        assertNull(Deencapsulation.getField(httpsTransportManager, "httpsIotHubConnection"));
    }


    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_007: [The send shall create a new instance of the `HttpMessage`, by parsing the Message with `parseHttpsJsonMessage` from `HttpsSingleMessage`.] */
    @Test
    public void sendCreateHttpMessageSucceed() throws IOException, TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = mockHttpsMessage;
                mockTransportMsg.getIotHubMethod();
                result = IotHubMethod.POST;
                mockTransportMsg.getUriPath();
                result = uriPath;
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                times = 1;
            }
        };
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_008: [If send failed to parse the message, it shall bypass the exception.] */
    @Test (expected = IllegalArgumentException.class)
    public void sendParseHttpsJsonMessageThrows()
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = new IllegalArgumentException();
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_009: [If the IotHubMethod is `GET`, the send shall set the httpsMethod as `GET`.] */
    @Test
    public void sendMethodGETSucceed() throws IOException, TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = mockHttpsMessage;
                mockTransportMsg.getIotHubMethod();
                result = IotHubMethod.GET;
                mockTransportMsg.getUriPath();
                result = uriPath;
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, HttpsMethod.GET, (String)any);
                times = 1;
            }
        };
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_010: [If the IotHubMethod is `POST`, the send shall set the httpsMethod as `POST`.] */
    @Test
    public void sendMethodPOSTSucceed() throws IOException, TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = mockHttpsMessage;
                mockTransportMsg.getIotHubMethod();
                result = IotHubMethod.POST;
                mockTransportMsg.getUriPath();
                result = uriPath;
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, HttpsMethod.POST, (String)any);
                times = 1;
            }
        };
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_011: [If the IotHubMethod is not `GET` or `POST`, the send shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void sendInvalidMethodThrows()
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = mockHttpsMessage;
                mockTransportMsg.getIotHubMethod();
                result = null;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_012: [The send shall set the httpsPath with the uriPath in the message.] */
    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_013: [The send shall call `sendHttpsMessage` from `HttpsIotHubConnection` to send the message.] */
    @Test
    public void sendSucceed() throws IOException, TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = mockHttpsMessage;
                mockTransportMsg.getIotHubMethod();
                result = IotHubMethod.POST;
                mockTransportMsg.getUriPath();
                result = uriPath;
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, HttpsMethod.POST, uriPath);
                times = 1;
            }
        };
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_014: [If `sendHttpsMessage` failed, the send shall bypass the exception.] */
    @Test (expected = IOException.class)
    public void sendSendHttpsMessageThrows() throws IOException, TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                Deencapsulation.invoke(HttpsSingleMessage.class, "parseHttpsJsonMessage", new Class[] {Message.class}, mockTransportMsg);
                result = mockHttpsMessage;
                mockTransportMsg.getIotHubMethod();
                result = IotHubMethod.POST;
                mockTransportMsg.getUriPath();
                result = uriPath;
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any);
                result = new IOException();
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class}, mockTransportMsg);
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_015: [The receive shall receive and bypass message from `HttpsIotHubConnection`, by calling `receiveMessage`.] */
    @Test
    public void receiveSucceed() throws TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                httpsIotHubConnection.receiveMessage();
                result = mockedTransportMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "receive");

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.receiveMessage();
                times = 1;
            }
        };
    }

    /* Tests_SRS_HTTPSTRANSPORTMANAGER_21_016: [If `receiveMessage` failed, the receive shall bypass the exception.] */
    @Test (expected = IOException.class)
    public void receiveReceiveMessageThrows() throws TransportException
    {
        // arrange
        final HttpsIotHubConnection httpsIotHubConnection = mockConn;
        final String uriPath = "/files/notifications";
        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(HttpsIotHubConnection.class, new Class[] {DeviceClientConfig.class}, mockConfig);
                result = httpsIotHubConnection;
                httpsIotHubConnection.receiveMessage();
                result = new IOException();
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "receive");
    }
}
