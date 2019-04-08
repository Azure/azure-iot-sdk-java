// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.net.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.*;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit tests for HttpsIotHubConnection.
 * Methods: 100%
 * Lines: 90%
 */
public class HttpsIotHubConnectionTest
{
    @Mocked
    DeviceClientConfig mockConfig;
    @Mocked
    IotHubUri mockIotHubUri;
    @Mocked
    URL mockUrl;
    @Mocked
    HttpsSingleMessage mockMsg;
    @Mocked
    Message mockedMessage;
    @Mocked
    HttpsRequest mockRequest;
    @Mocked
    HttpsResponse mockResponse;
    @Mocked
    IotHubListener mockedListener;
    @Mocked
    ProtocolException mockedProtocolConnectionStatusException;
    @Mocked
    ResponseMessage mockResponseMessage;
    @Mocked
    IotHubTransportMessage mockedTransportMessage;
    @Mocked
    ScheduledExecutorService mockedScheduledExecutorService;

    private static final String testSasToken = "SharedAccessSignature sr=test&sig=test&se=0";

    @Before
    public void setup() throws IOException, TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result=testSasToken;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_002: [The function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/events?api-version=2016-02-03'.] 
    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_001: [The constructor shall save the client configuration.]
    @Test
    public void sendEventHasCorrectUrl(
            @Mocked final IotHubEventUri mockUri) throws IOException, TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String eventUri = "test-event-uri";
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubEventUri(iotHubHostname, deviceId, null);
                result = mockUri;
                mockUri.toString();
                result = eventUri;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final String expectedUrl = "https://" + eventUri;
        new Verifications()
        {
            {
                new URL(expectedUrl);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_003: [The function shall send a POST request.]
    @Test
    public void sendEventSendsPostRequest(
            @Mocked final IotHubEventUri mockUri) throws TransportException
    {
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final HttpsMethod expectedMethod = HttpsMethod.POST;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, expectedMethod, (byte[]) any, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_004: [The function shall set the request body to the message body.]
    @Test
    public void sendEventSendsMessageBody(
            @Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final byte[] body = { 0x61, 0x62 };
        new NonStrictExpectations()
        {
            {
                mockMsg.getBody();
                result = body;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final byte[] expectedBody = body;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, (HttpsMethod) any, expectedBody, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_005: [The function shall write each message property as a request header.]
    @Test
    public void sendEventSendsMessageProperties(
            @Mocked final IotHubEventUri mockUri,
            @Mocked final MessageProperty mockProperty) throws TransportException
    {
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        new NonStrictExpectations()
        {
            {
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final String expectedPropertyName = propertyName;
        final String expectedPropertyValue = propertyValue;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(expectedPropertyName,
                        expectedPropertyValue);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_006: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
    @Test
    public void sendEventSetsReadTimeout(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final int readTimeoutMillis = 10;
        new NonStrictExpectations()
        {
            {
                mockConfig.getReadTimeoutMillis();
                result = readTimeoutMillis;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final int expectedReadTimeoutMillis = readTimeoutMillis;
        new Verifications()
        {
            {
                mockRequest.setReadTimeoutMillis(expectedReadTimeoutMillis);
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_25_040: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
    @Test
    public void sendEventSetsIotHubSSLContext(@Mocked final IotHubEventUri mockUri,
                                              @Mocked final SSLContext mockContext) throws IOException, TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getSSLContext();
                result = mockContext;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        new Verifications()
        {
            {
                mockRequest.setSSLContext(mockContext);
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_007: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
    @Test
    public void sendEventSetsAuthToSasToken(@Mocked final IotHubEventUri mockUri) throws IOException, TransportException
    {
        final String iotHubHostname = "test-iothubname";
        final String deviceId = "test-device-key";
        final String deviceKey = "test-device-key";
        final String tokenStr = "test-token-str";
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = tokenStr;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final String expectedTokenStr = tokenStr;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)authorization"),
                        expectedTokenStr);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_008: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/events'.]
    @Test
    public void sendEventSetsIotHubToToPath(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                new IotHubEventUri((String)any, (String)any, null);
                result = mockUri;
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockUri.getPath();
                result = path;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final String expectedPath = path;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)iothub-to"),
                        expectedPath);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_34_073: [If the provided message has a content encoding, this function shall set the request header to include that value with the key "iothub-contentencoding".]
    // Tests_SRS_HTTPSIOTHUBCONNECTION_34_074: [If the provided message has a content type, this function shall set the request header to include that value with the key "iothub-contenttype".]
    @Test
    public void sendEventSetsIotHubContentTypeAndEncoding(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final String contentType = "application/json";
        final String contentEncoding = "utf-8";
        new NonStrictExpectations()
        {
            {
                new IotHubEventUri((String)any, (String)any, null);
                result = mockUri;
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockUri.getPath();
                result = "some path";
                mockedMessage.getContentType();
                result = contentType;
                mockedMessage.getContentEncoding();
                result = contentEncoding;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);

        //act
        conn.sendMessage(mockedMessage);

        new Verifications()
        {
            {
                mockRequest.setHeaderField(MessageProperty.IOTHUB_CONTENT_TYPE, contentType);
                mockRequest.setHeaderField(MessageProperty.IOTHUB_CONTENT_ENCODING, contentEncoding);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_34_075: [If the provided message has a creation time utc, this function shall set the request header to include that value with the key "iothub-contenttype".]
    @Test
    public void sendEventSetsCreationTimeUtcIfPresent(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final String expectedCreationTimeUTCString = "1969-12-31T16:00:00.0000000";
        new NonStrictExpectations()
        {
            {
                new IotHubEventUri((String)any, (String)any, null);
                result = mockUri;
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockUri.getPath();
                result = "some path";
                mockedMessage.getCreationTimeUTC();
                result = new Date(0);
                mockedMessage.getCreationTimeUTCString();
                result = expectedCreationTimeUTCString;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);

        //act
        conn.sendMessage(mockedMessage);

        new Verifications()
        {
            {
                mockRequest.setHeaderField(MessageProperty.IOTHUB_CREATION_TIME_UTC, expectedCreationTimeUTCString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_009: [The function shall set the header field 'content-type' to be the message content type.]
    @Test
    public void sendEventSetsContentTypeCorrectly(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final String contentType = "test-content-type";
        new NonStrictExpectations()
        {
            {
                new IotHubEventUri((String)any, (String)any, null);
                result = mockUri;
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockMsg.getContentType();
                result = contentType;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        conn.sendMessage(mockedMessage);

        final String expectedContentType = contentType;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)content-type"),
                        expectedContentType);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_010: [The function shall return a ResponseMessage with the status and payload.]
    @Test
    public void sendEventReturnsCorrectResponse(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final byte[] body = {'A', 'B', 'C', '\0'};
        final int statusVal = 200;
        final IotHubStatusCode status = IotHubStatusCode.getIotHubStatusCode(statusVal);
        new NonStrictExpectations()
        {
            {
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockRequest.send();
                result = mockResponse;
                mockResponse.getBody();
                result = body;
                mockResponse.getStatus();
                result = statusVal;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);

        IotHubStatusCode iotHubStatusCode = conn.sendMessage(mockedMessage);

        assertThat(iotHubStatusCode, is(status));
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_012: [If the IoT Hub could not be reached, the function shall throw a TransportException.]
    @Test(expected = TransportException.class)
    public void sendEventThrowsProtocolConnectionExceptionIfRequestFails(@Mocked final IotHubEventUri mockUri) throws TransportException
    {
        final ProtocolException exception = new ProtocolException();
        new NonStrictExpectations()
        {
            {
                new HttpsRequest((URL) any, (HttpsMethod) any, (byte[]) any, anyString);
                result = mockRequest;
                mockRequest.send();
                result = exception;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendMessage(mockedMessage);
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_041: [The function shall send a request to the URL https://[iotHubHostname]/[httpsPath]?api-version=2016-02-03.]
    @Test
    public void sendHttpsMessageHasCorrectUrl(
            @Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String eventUri = "test-event-uri";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, eventUri, new HashMap<String, String>());

        final String expectedUrl = "https://" + iotHubHostname + eventUri + "?" + IotHubUri.API_VERSION;
        new Verifications()
        {
            {
                new URL(expectedUrl);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_042: [The function shall send a `httpsMethod` request.]
    @Test
    public void sendHttpsMessageSendsPostRequest(
            @Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;

        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        new Verifications()
        {
            {
                new HttpsRequest((URL) any, httpsMethod, (byte[]) any, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_043: [The function shall set the request body to the message body.]
    @Test
    public void sendHttpsMessageSendsMessageBody(
            @Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final byte[] body = { 0x61, 0x62 };
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockMsg.getBody();
                result = body;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        final byte[] expectedBody = body;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, (HttpsMethod) any, expectedBody, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_044: [The function shall write each message property as a request header.]
    @Test
    public void sendHttpsMessageSendsMessageProperties(
            @Mocked final IotHubUri mockUri,
            @Mocked final MessageProperty mockProperty) throws TransportException
    {
        final MessageProperty[] properties = { mockProperty };
        final String propertyName = "test-property-name";
        final String propertyValue = "test-property-value";
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockMsg.getProperties();
                result = properties;
                mockProperty.getName();
                result = propertyName;
                mockProperty.getValue();
                result = propertyValue;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        final String expectedPropertyName = propertyName;
        final String expectedPropertyValue = propertyValue;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(expectedPropertyName,
                        expectedPropertyValue);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_045: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
    @Test
    public void sendHttpsMessageSetsReadTimeout(@Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final int readTimeoutMillis = 10;
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockConfig.getReadTimeoutMillis();
                result = readTimeoutMillis;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        final int expectedReadTimeoutMillis = readTimeoutMillis;
        new Verifications()
        {
            {
                mockRequest.setReadTimeoutMillis(expectedReadTimeoutMillis);
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_21_046: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
    @Test
    public void sendHttpsMessageSetsIotHubSSLContext(@Mocked final IotHubUri mockUri,
                                              @Mocked final SSLContext mockContext) throws IOException, TransportException
    {
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getSSLContext();
                result = mockContext;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        new Verifications()
        {
            {
                mockRequest.setSSLContext(mockContext);
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_047: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
    @Test
    public void sendHttpsMessageSetsAuthToSasToken(@Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final String iotHubHostname = "test-iothubname";
        final String deviceId = "test-device-key";
        final String deviceKey = "test-device-key";
        final String tokenStr = "test-token-str";
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = tokenStr;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        final String expectedTokenStr = tokenStr;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)authorization"),
                        expectedTokenStr);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_048: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/[path]'.]
    @Test
    public void sendHttpsMessageSetsIotHubToToPath(@Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockUri.getPath();
                result = path;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, path, new HashMap<String, String>());

        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)iothub-to"), path);
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_34_072: [The function shall set the additional header fields provided.]
    @Test
    public void sendHttpsMessageSetsAdditionalHeaders(@Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockUri.getPath();
                result = path;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);

        final String expectedKey1 = "key1";
        final String expectedValue1 = "value1";
        final String expectedKey2 = "key2";
        final String expectedValue2 = "value2";


        Map additionalHeaders = new HashMap<String, String>();
        additionalHeaders.put(expectedKey1, expectedValue1);
        additionalHeaders.put(expectedKey2, expectedValue2);

        //act
        conn.sendHttpsMessage(mockMsg, httpsMethod, path, additionalHeaders);

        //assert
        new Verifications()
        {
            {
                mockRequest.setHeaderField(expectedKey1, expectedValue1);
                times = 1;

                mockRequest.setHeaderField(expectedKey2, expectedValue2);
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_049: [The function shall set the header field 'content-type' to be the message content type.]
    @Test
    public void sendHttpsMessageSetsContentTypeCorrectly(@Mocked final IotHubUri mockUri) throws IOException, TransportException
    {
        final String contentType = "test-content-type";
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockMsg.getContentType();
                result = contentType;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        final String expectedContentType = contentType;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)content-type"),
                        expectedContentType);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_21_050: [The function shall return a ResponseMessage with the status and payload.]
    @Test
    public void sendHttpsMessageReturnsCorrectResponse(@Mocked final IotHubUri mockUri, final @Mocked IotHubStatusCode mockStatus) throws IOException, TransportException
    {
        final byte[] body = {'A', 'B', 'C', '\0'};
        final int statusVal = 200;
        final String uriPath = "/files";
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                new HttpsRequest((URL)any, HttpsMethod.POST, (byte[]) any, anyString);
                result = mockRequest;
                mockRequest.send();
                result = mockResponse;
                mockResponse.getBody();
                result = body;
                mockResponse.getStatus();
                result = statusVal;
                IotHubStatusCode.getIotHubStatusCode(statusVal);
                result = mockStatus;
                new ResponseMessage(body, mockStatus);
                result = mockResponseMessage;
                mockResponseMessage.getStatus();
                result = mockStatus;
                mockResponseMessage.getBytes();
                result = body;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);

        ResponseMessage testResponse = conn.sendHttpsMessage(mockMsg, httpsMethod, uriPath, new HashMap<String, String>());

        assertEquals(mockStatus, testResponse.getStatus());
        assertEquals(body, testResponse.getBytes());
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_013: [The function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound?api-version=2016-02-03'.]
    @Test
    public void receiveMessageHasCorrectUrl(@Mocked final IotHubMessageUri mockUri) throws IOException, TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String messageUri = "test-message-uri";
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubMessageUri(iotHubHostname, deviceId, null);
                result = mockUri;
                mockUri.toString();
                result = messageUri;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        final String expectedUrl = "https://" + messageUri;
        new Verifications()
        {
            {
                new URL(expectedUrl);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_014: [The function shall send a GET request.]
    @Test
    public void receiveMessageSendsGetRequest(@Mocked final IotHubMessageUri mockUri) throws TransportException
    {
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        final HttpsMethod expectedMethod = HttpsMethod.GET;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, expectedMethod, (byte[]) any, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_015: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
    @Test
    public void receiveMessageSetsReadTimeout(@Mocked final IotHubMessageUri mockUri) throws TransportException
    {
        final int readTimeoutMillis = 10;
        new NonStrictExpectations()
        {
            {
                mockConfig.getReadTimeoutMillis();
                result = readTimeoutMillis;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        final int expectedReadTimeoutMillis = readTimeoutMillis;
        new Verifications()
        {
            {
                mockRequest.setReadTimeoutMillis(expectedReadTimeoutMillis);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_016: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
    @Test
    public void receiveMessageSetsAuthToSasToken(@Mocked final IotHubMessageUri mockUri) throws IOException, TransportException
    {
        final String iotHubHostname = "test-iothubname";
        final String deviceId = "test-device-key";
        final String deviceKey = "test-device-key";
        final String tokenStr = "test-token-str";
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = tokenStr;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        final String expectedTokenStr = tokenStr;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)authorization"),
                        expectedTokenStr);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_017: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound'.]
    @Test
    public void receiveMessageSetsIotHubToToPath(@Mocked final IotHubMessageUri mockUri) throws TransportException
    {
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                mockUri.getPath();
                result = path;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        final String expectedPath = path;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)iothub-to"),
                        expectedPath);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_018: [The function shall set the header field 'iothub-messagelocktimeout' to be the configuration parameter messageLockTimeoutSecs.]
    @Test
    public void receiveMessageSetsMessageLockTimeout(@Mocked final IotHubMessageUri mockUri) throws TransportException
    {
        final int messageLockTimeoutSecs = 24;
        new NonStrictExpectations()
        {
            {
                mockConfig.getMessageLockTimeoutSecs();
                result = messageLockTimeoutSecs;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        final String expectedMessageLockTimeoutSecs =
                Integer.toString(messageLockTimeoutSecs);
        new Verifications()
        {
            {
                mockRequest.setHeaderField(
                        withMatch("(?i)iothub-messagelocktimeout"),
                        expectedMessageLockTimeoutSecs);
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_25_041: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
    @Test
    public void receiveMessageSetsIotHubSSLContext(@Mocked final IotHubMessageUri mockUri,
                                                   @Mocked final SSLContext mockContext) throws IOException, TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockConfig.getSasTokenAuthentication().getSSLContext();
                result = mockContext;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        new Verifications()
        {
            {
                mockRequest.setSSLContext(mockContext);
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_019: [If a response with IoT Hub status code OK is received, the function shall return the IoT Hub message included in the response.]
    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_070: [If the message status was OK this function shall save the received message and its eTag into its map.]
    @Test
    public void receiveMessageReturnsMessageBody(@Mocked final IotHubMessageUri mockUri, @Mocked final Message mockedMessage, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final byte[] body = { 0x61, 0x62 };
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getBody();
                result = body;
                mockResponse.getStatus();
                result = 200;
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);

        //act
        Message actualMessage = conn.receiveMessage();

        //assert
        assertEquals(actualMessage.getBytes(), mockedMessage.getBytes());
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        assertEquals(1, eTagMap.size());
        assertTrue(eTagMap.containsKey(actualMessage));
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_020: [If a response with IoT Hub status code OK is received, the function shall save the response header field 'etag'.]
    @Test
    public void receiveMessageSavesEtag(@Mocked final IotHubMessageUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                result = 200;
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();

        new Verifications()
        {
            {
                mockResponse.getHeaderField(withMatch("(?i)etag"));
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_021: [If a response with IoT Hub status code OK is not received, the function shall return null.]
    @Test
    public void receiveMessageReturnsNullIfNoMessageReceived(@Mocked final IotHubMessageUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                result = 204;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Message testMsg = conn.receiveMessage();

        Message expectedMsg = null;
        assertThat(testMsg, is(expectedMsg));
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_023: [If the IoT Hub could not be reached, the function shall throw a TransportException.]
    @Test(expected = TransportException.class)
    public void receiveMessageThrowsProtocolConnectionExceptionIfRequestFails(@Mocked final IotHubMessageUri mockUri) throws TransportException
    {
        new NonStrictExpectations()
        {
            {
                mockRequest.send();
                result = new TransportException();
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_024: [If the result is COMPLETE, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]?api-version=2016-02-03'.]
    // Tests_SRS_HTTPSIOTHUBCONNECTION_34_069: [If the IoT Hub status code in the response is OK_EMPTY or OK, the function shall remove the sent eTag from its map and return true.]
    @Test
    public void sendMessageResultWhenCompleteUsesCompleteUrl(@Mocked final IotHubCompleteUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws IOException, TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String completeUri = "test-complete-uri";
        final String eTag = "test-etag";
        final String expectedUrl = "https://" + completeUri;
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubCompleteUri(iotHubHostname, deviceId, eTag, null);
                result = mockUri;
                mockUri.toString();
                result = completeUri;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);

        //act
        boolean result = conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.COMPLETE);

        //assert
        assertTrue(result);
        assertTrue(eTagMap.isEmpty());
        new Verifications()
        {
            {
                new URL(expectedUrl);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_025: [If the result is COMPLETE, the function shall send a DELETE request.]
    @Test
    public void sendMessageResultWhenCompleteSendsDeleteRequest(@Mocked final IotHubCompleteUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.COMPLETE);

        final HttpsMethod expectedMethod = HttpsMethod.DELETE;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, expectedMethod, (byte[]) any, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_026: [If the result is COMPLETE, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.]
    @Test
    public void sendMessageResultWhenCompleteSetsIotHubToToPath(@Mocked final IotHubCompleteUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String completeUri = "test-complete-uri";
        final String eTag = "test-etag";
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubCompleteUri(iotHubHostname, deviceId, eTag, null);
                result = mockUri;
                mockUri.toString();
                result = completeUri;
                mockUri.getPath();
                result = path;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.COMPLETE);

        final String expectedPath = path;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)iothub-to"),
                        expectedPath);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_027: [If the result is ABANDON, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]/abandon?api-version=2016-02-03'.]
    @Test
    public void sendMessageResultWhenAbandonUsesAbandonUrl(@Mocked final IotHubAbandonUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws IOException, TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String abandonUri = "test-abandon-uri";
        final String eTag = "test-etag";

        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubAbandonUri(iotHubHostname, deviceId, eTag, null);
                result = mockUri;
                mockUri.toString();
                result = abandonUri;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.ABANDON);

        final String expectedUrl = "https://" + abandonUri;
        new Verifications()
        {
            {
                new URL(expectedUrl);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_028: [If the result is ABANDON, the function shall send a POST request.]
    @Test
    public void sendMessageResultWhenAbandonSendsPostRequest(@Mocked final IotHubAbandonUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.ABANDON);

        final HttpsMethod expectedMethod = HttpsMethod.POST;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, expectedMethod, (byte[]) any, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_029: [If the result is ABANDON, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]/abandon'.]
    @Test
    public void sendMessageResultWhenAbandonSetsIotHubToToPath(@Mocked final IotHubAbandonUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String abandonUri = "test-abandon-uri";
        final String eTag = "test-etag";
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubAbandonUri(iotHubHostname, deviceId, eTag, null);
                result = mockUri;
                mockUri.toString();
                result = abandonUri;
                mockUri.getPath();
                result = path;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.ABANDON);

        final String expectedPath = path;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)iothub-to"),
                        expectedPath);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_030: [If the result is REJECT, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]??reject=true&api-version=2016-02-03' (the query parameters can be in any order).]
    @Test
    public void sendMessageResultWhenRejectUsesRejectUrl(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws IOException, TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String rejectUri = "test-reject-uri";
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubRejectUri(iotHubHostname, deviceId, eTag, null);
                result = mockUri;
                mockUri.toString();
                result = rejectUri;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        final String expectedUrl = "https://" + rejectUri;
        new Verifications()
        {
            {
                new URL(expectedUrl);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_031: [If the result is REJECT, the function shall send a DELETE request.]
    @Test
    public void sendMessageResultWhenRejectSendsDeleteRequest(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        final HttpsMethod expectedMethod = HttpsMethod.DELETE;
        new Verifications()
        {
            {
                new HttpsRequest((URL) any, expectedMethod, (byte[]) any, anyString);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_032: [If the result is REJECT, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.]
    @Test
    public void sendMessageResultWhenRejectSetsIotHubToToPath(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String rejectUri = "test-reject-uri";
        final String eTag = "test-etag";
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubRejectUri(iotHubHostname, deviceId, eTag, null);
                result = mockUri;
                mockUri.toString();
                result = rejectUri;
                mockUri.getPath();
                result = path;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        final String expectedPath = path;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)iothub-to"),
                        expectedPath);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_033: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
    @Test
    public void sendMessageResultSetsReadTimeout(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        final int readTimeoutMillis = 23;
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getReadTimeoutMillis();
                result = readTimeoutMillis;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        final int expectedReadTimeoutMillis = readTimeoutMillis;
        new Verifications()
        {
            {
                mockRequest.setReadTimeoutMillis(expectedReadTimeoutMillis);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_034: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
    @Test
    public void sendMessageResultSetsAuthToSasToken(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws IOException, TransportException
    {
        final String eTag = "test-etag";
        final String iotHubHostname = "test-iothubname";
        final String deviceId = "test-device-key";
        final String deviceKey = "test-device-key";
        final String tokenStr = "test-token-str";
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                result = tokenStr;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        final String expectedTokenStr = tokenStr;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)authorization"),
                        expectedTokenStr);
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_035: [The function shall set the header field 'if-match' to be the e-tag saved when receiveMessage() was previously called.]
    @Test
    public void sendMessageResultSetsIfMatchToEtag(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        final String expectedEtag = eTag;
        new Verifications()
        {
            {
                mockRequest.setHeaderField(withMatch("(?i)if-match"),
                        expectedEtag);
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_25_042: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
    @Test
    public void sendMessageResultSetsIotHubSSLContext(@Mocked final IotHubRejectUri mockUri,
                                                      @Mocked final SSLContext mockedContext, final @Mocked IotHubStatusCode mockStatusCode) throws IOException, TransportException
    {
        final String eTag = "test-etag";
        final int readTimeoutMillis = 23;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getReadTimeoutMillis();
                result = readTimeoutMillis;
                mockConfig.getSasTokenAuthentication().getSSLContext();
                result = mockedContext;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);


        new Verifications()
        {
            {
                mockRequest.setSSLContext(mockedContext);
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_037: [If the IoT Hub could not be reached, the function shall throw a ProtocolException.]
    @Test(expected = ProtocolException.class)
    public void sendMessageResultThrowsProtocolExceptionIfRequestFails(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final ProtocolException protocolException = new ProtocolException();
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockRequest.send();
                result = protocolException;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_038: [If the IoT Hub status code in the response is not OK_EMPTY, the function shall throw an IotHubServiceException.]
    @Test(expected = IotHubServiceException.class)
    public void sendMessageResultThrowsProtocolConnectionExceptionIfBadResponseStatus(@Mocked final IotHubRejectUri mockUri, final @Mocked IotHubStatusCode mockStatusCode) throws TransportException
    {
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                result = 404;
                IotHubStatusCode.getIotHubStatusCode(404);
                result = IotHubStatusCode.HUB_OR_DEVICE_ID_NOT_FOUND;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);

        //act
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_039: [If the function is called before receiveMessage() returns a message, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendMessageResultFailsIfReceiveNotCalled(
            @Mocked final IotHubRejectUri mockUri) throws TransportException
    {
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.sendMessageResult(mockedMessage, IotHubMessageResult.REJECT);
    }

    // Tests_SRS_HTTPSIOTHUBCONNECTION_11_039: [If the function is called before receiveMessage() returns a message, the function shall throw an IllegalStateException.]
    @Test(expected = IllegalStateException.class)
    public void sendMessageResultFailsIfNoMessageReceived(
            @Mocked final IotHubRejectUri mockUri) throws TransportException
    {
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.receiveMessage();
        conn.sendMessageResult(mockedMessage, IotHubMessageResult.REJECT);
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_056: [This function shall retrieve a sas token from its config to use in the https request header.]
    @Test
    public void sendHttpsMessageRetrievesSasTokenFromConfig() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        HttpsIotHubConnection connection = new HttpsIotHubConnection(mockConfig);

        //act
        connection.sendHttpsMessage(mockMsg, HttpsMethod.GET, "somepath", new HashMap<String, String>());

        //assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_057: [This function shall retrieve a sas token from its config to use in the https request header.]
    @Test
    public void receiveMessageRetrievesSasTokenFromConfig() throws IOException, TransportException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.SAS_TOKEN;
            }
        };
        HttpsIotHubConnection connection = new HttpsIotHubConnection(mockConfig);

        //act
        connection.receiveMessage();

        //assert
        new Verifications()
        {
            {
                mockConfig.getSasTokenAuthentication().getRenewedSasToken(false, false);
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_059: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
    @Test
    public void sendEventPullsSSLContextFromAppropriateConfigAuthObject() throws IOException, TransportException
    {
        //arrange
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };

        //act
        conn.sendMessage(mockedMessage);

        //assert
        new Verifications()
        {
            {
                mockConfig.getAuthenticationProvider().getSSLContext();
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_060: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
    @Test
    public void sendHttpsMessagePullsSSLContextFromAppropriateConfigAuthObject() throws IOException, TransportException
    {
        //arrange
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        final String uriPath = "/files";
        final HttpsMethod expectedMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };

        //act
        conn.sendHttpsMessage(mockMsg, expectedMethod, uriPath, new HashMap<String, String>());

        //assert
        new Verifications()
        {
            {
                mockConfig.getAuthenticationProvider().getSSLContext();
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_061: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
    @Test
    public void receiveMessagePullsSSLContextFromAppropriateConfigAuthObject() throws IOException, TransportException
    {
        //arrange
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        new NonStrictExpectations()
        {
            {
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };

        //act
        conn.receiveMessage();

        //assert
        new Verifications()
        {
            {
                mockConfig.getAuthenticationProvider().getSSLContext();
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_062: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
    @Test
    public void sendMessageResultPullsSSLContextFromAppropriateConfigAuthObject(final @Mocked IotHubStatusCode mockStatusCode) throws IOException, TransportException
    {
        //arrange
        final String eTag = "test-etag";
        new NonStrictExpectations()
        {
            {
                mockResponse.getStatus();
                returns(200, 204);
                IotHubStatusCode.getIotHubStatusCode(200);
                result = IotHubStatusCode.OK;
                IotHubStatusCode.getIotHubStatusCode(204);
                result = IotHubStatusCode.OK_EMPTY;
                mockResponse.getHeaderField(withMatch("(?i)etag"));
                result = eTag;
                mockConfig.getAuthenticationType();
                result = DeviceClientConfig.AuthType.X509_CERTIFICATE;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        Map<Message, String> eTagMap = Deencapsulation.getField(conn, "messageToETagMap");
        eTagMap.put(mockedTransportMessage, eTag);

        //act
        conn.sendMessageResult(mockedTransportMessage, IotHubMessageResult.REJECT);

        new Verifications()
        {
            {
                mockConfig.getAuthenticationProvider().getSSLContext();
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_065: [If the provided listener object is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void addListenerThrowsForNullListener()
    {
        //arrange
        HttpsIotHubConnection connection = new HttpsIotHubConnection(mockConfig);

        //act
        connection.setListener(null);
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_066: [This function shall save the provided listener object.]
    @Test
    public void setListenerSavesListener()
    {
        //arrange
        HttpsIotHubConnection connection = new HttpsIotHubConnection(mockConfig);

        //act
        connection.setListener(mockedListener);

        //assert
        IotHubListener actualListener = Deencapsulation.getField(connection, "listener");
        assertEquals(mockedListener, actualListener);
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_067: [If the response from the service is OK or OK_EMPTY, this function shall notify its listener that a message was sent with no exception.]
    @Test
    public void sendMessageNotifiesListenerOnMessageSent(final @Mocked IotHubEventUri mockUri) throws TransportException
    {
        //arrange
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String eventUri = "test-event-uri";
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubEventUri(iotHubHostname, deviceId, null);
                result = mockUri;
                mockUri.toString();
                result = eventUri;

                mockRequest.send();
                result = mockResponse;

                mockResponse.getStatus();
                result = 200;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);

        //act
        conn.sendMessage(mockedMessage);

        //assert
        new Verifications()
        {
            {
                mockedListener.onMessageSent((IotHubTransportMessage) any, null);
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_068: [If the response from the service not OK or OK_EMPTY, this function shall notify its listener that a message was with the mapped IotHubServiceException.]
    @Test
    public void sendMessageNotifiesListenerOfIotHubServiceExceptionOnMessageSent(final @Mocked IotHubEventUri mockUri) throws TransportException
    {
        //arrange
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-device-id";
        final String eventUri = "test-event-uri";
        new NonStrictExpectations()
        {
            {
                mockConfig.getIotHubHostname();
                result = iotHubHostname;
                mockConfig.getDeviceId();
                result = deviceId;
                new IotHubEventUri(iotHubHostname, deviceId, null);
                result = mockUri;
                mockUri.toString();
                result = eventUri;

                mockRequest.send();
                result = mockResponse;

                mockResponse.getStatus();
                result = 404;
            }
        };

        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);
        conn.setListener(mockedListener);

        //act
        conn.sendMessage(mockedMessage);

        //assert
        new Verifications()
        {
            {
                mockedListener.onMessageSent((IotHubTransportMessage) any, (TransportException) any);
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSIOTHUBCONNECTION_34_071: [This function shall return the empty string.]
    @Test
    public void getConnectionIdReturnsEmptyString()
    {
        //arrange
        HttpsIotHubConnection conn = new HttpsIotHubConnection(mockConfig);

        //act
        String actualConnectionId = conn.getConnectionId();

        //assert
        assertEquals("", actualConnectionId);
    }

    //Just for code coverage over dummy methods
    @Test
    public void openAndCloseDoNothing() throws IOException, TransportException
    {
        HttpsIotHubConnection connection = new HttpsIotHubConnection(mockConfig);
        connection.open(null, mockedScheduledExecutorService);
        connection.close();
    }
}