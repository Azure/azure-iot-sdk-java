// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubBadFormatException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for Http requester
 */
public class DeviceOperationsTest
{
    private static final String STANDARD_HOSTNAME = "testHostName.azure.net";
    private static final String STANDARD_SHAREDACCESSKEYNAME = "testKeyName";
    private static final String STANDARD_SHAREDACCESSKEY = "1234567890ABCDEFGHIJKLMNOPQRESTUVWXYZ=";
    private static final String STANDARD_CONNECTIONSTRING =
            "HostName=" + STANDARD_HOSTNAME +
                    ";SharedAccessKeyName=" + STANDARD_SHAREDACCESSKEYNAME +
                    ";SharedAccessKey=" + STANDARD_SHAREDACCESSKEY;

    private static final String STANDARD_DEVICEID = "validDeviceId";
    private static final String STANDARD_URL = "https://" + STANDARD_HOSTNAME + "/twins/" + STANDARD_DEVICEID + "/methods/" ;
    private static final byte[] STANDARD_PAYLOAD = "testPayload".getBytes(StandardCharsets.UTF_8);
    private static final String STANDARD_REQUEST_ID = "abc123";

    private static final String AUTHORIZATION = "authorization";
    private static final String REQUEST_ID = "Request-Id";
    private static final String USER_AGENT = "User-Agent";
    private static final String ACCEPT = "Accept";
    private static final String ACCEPT_VALUE = "application/json";
    private static final String ACCEPT_CHARSET = "charset=utf-8";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final Integer DEFAULT_HTTP_TIMEOUT_MS = 24000;

    private IotHubConnectionString IOT_HUB_CONNECTION_STRING;
    private String STANDARD_SASTOKEN_STRING;

    @Before
    public void setUp() throws Exception
    {
        IOT_HUB_CONNECTION_STRING = IotHubConnectionStringBuilder.createConnectionString(STANDARD_CONNECTIONSTRING);
        STANDARD_SASTOKEN_STRING = (new IotHubServiceSasToken(IOT_HUB_CONNECTION_STRING)).toString();
    }


    /* Tests_SRS_DEVICE_OPERATIONS_21_001: [The request shall throw IllegalArgumentException if the provided `iotHubConnectionString` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void request_nullConnectionString_failed() throws Exception
    {
        //arrange

        //act
        HttpResponse response = DeviceOperations.request(
                null,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);

        //assert
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_002: [The request shall throw IllegalArgumentException if the provided `url` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void request_nullUrl_failed() throws Exception
    {
        //arrange
        final IotHubConnectionString iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                null,
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);

        //assert
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_003: [The request shall throw IllegalArgumentException if the provided `method` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void request_nullHttpMethod_failed() throws Exception
    {
        //arrange

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                null,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);

        //assert
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_004: [The request shall throw IllegalArgumentException if the provided `payload` is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void request_nullPayload_failed() throws Exception
    {
        //arrange

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                null,
                STANDARD_REQUEST_ID);

        //assert
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_005: [The request shall throw IllegalArgumentException if the provided `requestId` is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void request_nullRequestId_failed() throws Exception
    {
        //arrange

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                null);

        //assert
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_005: [The request shall throw IllegalArgumentException if the provided `requestId` is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void request_emptyRequestId_failed() throws Exception
    {
        //arrange

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                "");

        //assert
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_006: [The request shall create a new SASToken with the ServiceConnect rights.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnCreateIotHubServiceSasToken_failed() throws Exception
    {
        //arrange
        new MockUp<IotHubServiceSasToken>()
        {
            @Mock
            void $init(IotHubConnectionString iotHubConnectionString)
            {
                throw new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_007: [If the SASToken is null or empty, the request shall throw IOException.] */
    @Test (expected = IOException.class)
    public void invoke_throwOnSasTokenNull_failed(@Mocked IotHubServiceSasToken iotHubServiceSasToken) throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = null;
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_007: [If the SASToken is null or empty, the request shall throw IOException.] */
    @Test (expected = IOException.class)
    public void invoke_throwOnSasTokenEmpty_failed(@Mocked IotHubServiceSasToken iotHubServiceSasToken) throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = "";
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_008: [The request shall create a new HttpRequest with the provided `url`, http `method`, and `payload`.] */
    @Test (expected = IOException.class)
    public void invoke_throwOnHttpRequest_failed(@Mocked IotHubServiceSasToken iotHubServiceSasToken) throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
            }
        };
        new MockUp<HttpRequest>()
        {
            @Mock void $init(URL url, HttpMethod method, byte[] body) throws IOException
            {
                throw new IOException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_009: [The request shall add to the HTTP header an default timeout in milliseconds.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnsetReadTimeoutMillis_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_010: [The request shall add to the HTTP header an `authorization` key with the SASToken.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnAutorization_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_011: [The request shall add to the HTTP header a `Request-Id` key with a new unique string value for every request.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnRequestID_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_012: [The request shall add to the HTTP header a `User-Agent` key with the client Id and service version.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnUserAgent_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = httpRequest;
                httpRequest.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());
                result = new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_013: [The request shall add to the HTTP header a `Accept` key with `application/json`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnAccept_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = httpRequest;
                httpRequest.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());
                result = httpRequest;
                httpRequest.setHeaderField(ACCEPT, ACCEPT_VALUE);
                result = new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_014: [The request shall add to the HTTP header a `Content-Type` key with `application/json; charset=utf-8`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnContentType_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = httpRequest;
                httpRequest.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());
                result = httpRequest;
                httpRequest.setHeaderField(ACCEPT, ACCEPT_VALUE);
                result = httpRequest;
                httpRequest.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);
                result = new IllegalArgumentException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_015: [The request shall send the created request and get the response.] */
    @Test (expected = IOException.class)
    public void invoke_throwOnSend_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = httpRequest;
                httpRequest.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());
                result = httpRequest;
                httpRequest.setHeaderField(ACCEPT, ACCEPT_VALUE);
                result = httpRequest;
                httpRequest.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);
                result = httpRequest;
                httpRequest.send();
                result = new IOException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_016: [If the resulted HttpResponseStatus represents fail, the request shall throw proper Exception by calling httpResponseVerification.] */
    @Test (expected = IotHubBadFormatException.class)
    public void invoke_throwOnhttpResponseVerification_failed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"ExceptionMessage\":\"This is the error message\"}".getBytes();
        HttpResponse sendResponse = new HttpResponse(status, body, headerFields, errorReason);

        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = httpRequest;
                httpRequest.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());
                result = httpRequest;
                httpRequest.setHeaderField(ACCEPT, ACCEPT_VALUE);
                result = httpRequest;
                httpRequest.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);
                result = httpRequest;
                httpRequest.send();
                result = sendResponse;
                IotHubExceptionManager.httpResponseVerification(sendResponse);
                result = new IotHubBadFormatException();
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);
    }

    /* Tests_SRS_DEVICE_OPERATIONS_21_017: [If the resulted status represents success, the request shall return the http response.] */
    @Test
    public void invoke_succeed(
            @Mocked IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked HttpRequest httpRequest)
            throws Exception
    {
        //arrange
        final int status = 200;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "succeed".getBytes();
        HttpResponse sendResponse = new HttpResponse(status, body, headerFields, errorReason);

        new NonStrictExpectations()
        {
            {
                iotHubServiceSasToken.toString();
                result = STANDARD_SASTOKEN_STRING;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = httpRequest;
                httpRequest.setHeaderField(AUTHORIZATION, STANDARD_SASTOKEN_STRING);
                result = httpRequest;
                httpRequest.setHeaderField(REQUEST_ID, STANDARD_REQUEST_ID);
                result = httpRequest;
                httpRequest.setHeaderField(USER_AGENT, TransportUtils.getJavaServiceClientIdentifier() + TransportUtils.getServiceVersion());
                result = httpRequest;
                httpRequest.setHeaderField(ACCEPT, ACCEPT_VALUE);
                result = httpRequest;
                httpRequest.setHeaderField(CONTENT_TYPE, ACCEPT_VALUE + "; " + ACCEPT_CHARSET);
                result = httpRequest;
                httpRequest.send();
                result = sendResponse;
                IotHubExceptionManager.httpResponseVerification(sendResponse);
            }
        };

        //act
        HttpResponse response = DeviceOperations.request(
                IOT_HUB_CONNECTION_STRING,
                new URL(STANDARD_URL),
                HttpMethod.POST,
                STANDARD_PAYLOAD,
                STANDARD_REQUEST_ID);

        //assert
        assertEquals(response, sendResponse);
        new Verifications()
        {
            {
                iotHubServiceSasToken.toString();
                times = 1;
                httpRequest.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                times = 1;
                httpRequest.setHeaderField(anyString, anyString);
                times = 5;
                httpRequest.send();
                times = 1;
                IotHubExceptionManager.httpResponseVerification(sendResponse);
                times = 1;
            }
        };
    }

}
