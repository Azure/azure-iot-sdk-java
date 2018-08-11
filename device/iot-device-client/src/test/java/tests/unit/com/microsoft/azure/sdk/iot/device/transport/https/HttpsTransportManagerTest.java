// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.MethodResult;
import com.microsoft.azure.sdk.iot.device.exceptions.HubOrDeviceIdNotFoundException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsIotHubConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsSingleMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
    @Mocked
    private MethodRequest mockedMethodRequest;
    @Mocked
    private MethodResult mockedMethodResult;


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
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any, (Map) any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());

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
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());
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
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any, (Map) any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, HttpsMethod.GET, (String)any, (Map) any);
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
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any, (Map) any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, HttpsMethod.POST, (String)any, (Map) any);
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
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());
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
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any, (Map) any);
                result = mockResponseMessage;
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());

        // assert
        new Verifications()
        {
            {
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, HttpsMethod.POST, uriPath, (Map) any);
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
                httpsIotHubConnection.sendHttpsMessage(mockHttpsMessage, (HttpsMethod)any, (String)any, (Map) any);
                result = new IOException();
            }
        };
        HttpsTransportManager httpsTransportManager = Deencapsulation.newInstance(HttpsTransportManager.class, new Class[] {DeviceClientConfig.class}, mockConfig);
        Deencapsulation.invoke(httpsTransportManager, "open");

        // act
        Deencapsulation.invoke(httpsTransportManager, "send", new Class[] {IotHubTransportMessage.class, Map.class}, mockTransportMsg, new HashMap<String, String>());
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

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_017: [This function shall call invokeMethod with the provided request and
    // a uri in the format twins/<device id>/modules/<module id>/methods?api-version=<api_version>.]
    @Test
    public void invokeMethodOnModuleSuccess() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";
        final String expectedModuleId = "myModule";
        final String expectedSenderDeviceId = "mySenderDevice";
        final String expectedSenderModuleId = "mySenderModule";
        final String expectedMethodRequestJson = "someJson";
        final String expectedResponseBody = "some body";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockedMethodRequest.toJson();
                result = expectedMethodRequestJson;

                new IotHubTransportMessage(expectedMethodRequestJson);
                result = mockedTransportMessage;

                mockedTransportMessage.setIotHubMethod(IotHubMethod.POST);

                mockedTransportMessage.setUriPath("/twins/" + expectedDeviceId + "/modules/" + expectedModuleId +"/methods");

                mockConfig.getDeviceId();
                result = expectedSenderDeviceId;

                mockConfig.getModuleId();
                result = expectedSenderModuleId;

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;

                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK_EMPTY;

                mockResponseMessage.getBytes();
                result = expectedResponseBody.getBytes();

                new MethodResult(expectedResponseBody);
            }
        };

        //act
        transportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, expectedModuleId);
    }

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_018: [If a moduleId is provided, this function shall call invokeMethod with the provided request and
    // a uri in the format twins/<device id>/modules/<module id>/methods?api-version=<api_version>.]
    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_021: [This function shall set the methodrequest json as the body of the http message.]
    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_022: [This function shall set the http method to POST.]
    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_023: [This function shall set the http message's uri path to the provided uri path.]
    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_024 [This function shall set a custom property of 'x-ms-edge-moduleId' to the value of <device id>/<module id> of the sending module/device.]
    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_025 [This function shall send the built message.]
    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_027 [If the http response doesn't contain an error code, this function return a method result with the response message body as the method result body.]
    @Test
    public void invokeMethodOnDeviceSuccess() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";
        final String expectedModuleId = "myModule";
        final String expectedSenderDeviceId = "mySenderDevice";
        final String expectedSenderModuleId = "mySenderModule";
        final String expectedMethodRequestJson = "someJson";
        final String expectedResponseBody = "some body";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockedMethodRequest.toJson();
                result = expectedMethodRequestJson;

                new IotHubTransportMessage(expectedMethodRequestJson);
                result = mockedTransportMessage;

                mockedTransportMessage.setIotHubMethod(IotHubMethod.POST);

                mockedTransportMessage.setUriPath("/twins/" + expectedDeviceId + "/methods");

                mockConfig.getDeviceId();
                result = expectedSenderDeviceId;

                mockConfig.getModuleId();
                result = expectedSenderModuleId;

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;

                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK_EMPTY;

                mockResponseMessage.getBytes();
                result = expectedResponseBody.getBytes();

                new MethodResult(expectedResponseBody);
            }
        };

        //act
        transportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, "");
    }


    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_019: [If the provided method request is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodThrowsForNullRequest() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";

        //act
        transportManager.invokeMethod(null, expectedDeviceId, "");
    }

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_020: [If the provided uri is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void invokeMethodThrowsForNullUri() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";

        //act
        Deencapsulation.invoke(transportManager, "invokeMethod", new Class[] {MethodRequest.class, URI.class}, mockedMethodRequest, (URI) null);
    }


    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_026 [If the http response contains an error code, this function shall throw the associated exception.]
    @Test (expected = HubOrDeviceIdNotFoundException.class)
    public void invokeMethodOnDeviceThrowsIfIotHubRespondsWithErrorCode() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";
        final String expectedSenderDeviceId = "mySenderDevice";
        final String expectedSenderModuleId = "mySenderModule";
        final String expectedMethodRequestJson = "someJson";
        final String expectedResponseBody = "some body";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockedMethodRequest.toJson();
                result = expectedMethodRequestJson;

                new IotHubTransportMessage(expectedMethodRequestJson);
                result = mockedTransportMessage;

                mockedTransportMessage.setIotHubMethod(IotHubMethod.POST);

                mockedTransportMessage.setUriPath("/twins/" + expectedDeviceId + "/methods");

                mockConfig.getDeviceId();
                result = expectedSenderDeviceId;

                mockConfig.getModuleId();
                result = expectedSenderModuleId;

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;

                mockResponseMessage.getStatus();
                result = IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;

                mockResponseMessage.getBytes();
                result = expectedResponseBody.getBytes();
            }
        };

        //act
        transportManager.invokeMethod(mockedMethodRequest, expectedDeviceId, "");
    }

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_028 [This function shall set the uri path of the provided message to the
    // format devices/<deviceid>/modules/<moduleid>/files if a moduleId is present or
    // devices/<deviceid>/modules/<moduleid>/files otherwise, and then send it.]
    @Test
    public void sendFileUploadMessageSuccessWithModule() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";
        final String expectedModuleId = "myModule";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockConfig.getDeviceId();
                result = expectedDeviceId;

                mockConfig.getModuleId();
                result = expectedModuleId;

                mockedTransportMessage.setUriPath("/devices/" + expectedDeviceId + "/modules/" + expectedModuleId + "/files");

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;
            }
        };

        //act
        transportManager.sendFileUploadMessage(mockedTransportMessage);
    }

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_028 [This function shall set the uri path of the provided message to the
    // format devices/<deviceid>/modules/<moduleid>/files if a moduleId is present or
    // devices/<deviceid>/modules/<moduleid>/files otherwise, and then send it.]
    @Test
    public void sendFileUploadMessageSuccessWithoutModule() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockConfig.getDeviceId();
                result = expectedDeviceId;

                mockConfig.getModuleId();
                result = "";

                mockedTransportMessage.setUriPath("/devices/" + expectedDeviceId + "/files");

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;
            }
        };

        //act
        transportManager.sendFileUploadMessage(mockedTransportMessage);
    }

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_029 [This function shall set the uri path of the provided message to the
    // format devices/<deviceid>/modules/<moduleid>/files/notifications if a moduleId is present or
    // devices/<deviceid>/modules/<moduleid>/files/notifications otherwise, and then send it.]
    @Test
    public void sendFileUploadNotificationMessageSuccessWithModule() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";
        final String expectedModuleId = "myModule";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockConfig.getDeviceId();
                result = expectedDeviceId;

                mockConfig.getModuleId();
                result = expectedModuleId;

                mockedTransportMessage.setUriPath("/devices/" + expectedDeviceId + "/modules/" + expectedModuleId + "/files/notifications");

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;
            }
        };

        //act
        transportManager.sendFileUploadNotification(mockedTransportMessage);
    }

    //Tests_SRS_HTTPSTRANSPORTMANAGER_34_029 [This function shall set the uri path of the provided message to the
    // format devices/<deviceid>/modules/<moduleid>/files/notifications if a moduleId is present or
    // devices/<deviceid>/modules/<moduleid>/files/notifications otherwise, and then send it.]
    @Test
    public void sendFileUploadNotificationMessageSuccessWithoutModule() throws TransportException, IOException, URISyntaxException
    {
        //arrange
        final HttpsTransportManager transportManager = new HttpsTransportManager(mockConfig);
        final String expectedDeviceId = "myDevice";

        //assert
        new Expectations(HttpsTransportManager.class)
        {
            {
                mockConfig.getDeviceId();
                result = expectedDeviceId;

                mockConfig.getModuleId();
                result = "";

                mockedTransportMessage.setUriPath("/devices/" + expectedDeviceId + "/files/notifications");

                transportManager.send(mockedTransportMessage, (Map) any);
                result = mockResponseMessage;
            }
        };

        //act
        transportManager.sendFileUploadNotification(mockedTransportMessage);
    }
}
