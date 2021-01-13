// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.contract;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningSasToken;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.SDKUtils;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientExceptionManager;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for Device Provisioning Service Contract APIs for HTTP.
 * 100% methods, 98% lines covered
 *    * impossible enum condition on getHttpMethodFromDeviceRegistrationMethod
 */
public class ContractApiHttpTest
{
    @Mocked
    ProvisioningSasToken mockedProvisioningSasToken;

    @Mocked
    ProvisioningConnectionString mockedProvisioningConnectionString;

    @Mocked
    HttpRequest mockedHttpRequest;

    @Mocked
    HttpResponse mockedHttpResponse;

    @Mocked
    URL mockedURL;

    private final String VALID_PATH = "a/b";
    private final Map<String, String> VALID_HEADER = new HashMap<>();
    private final String VALID_PAYLOAD = "{}";
    private final byte[] VALID_BODY = VALID_PAYLOAD.getBytes();
    private final int VALID_SUCCESS_STATUS = 200;
    private final String VALID_SUCCESS_MESSAGE = "success";
    private final String VALID_SASTOKEN = "validSas";
    private final String VALID_HOST_NAME = "testProvisioningHostName.azure.net";

    private void requestNonStrictExpectations() throws IOException, ProvisioningServiceClientException
    {
        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes());
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = VALID_SUCCESS_STATUS;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes();
                mockedHttpResponse.getBody();
                result = VALID_BODY;
                mockedHttpResponse.getHeaderFields();
                result = VALID_HEADER;
                ProvisioningServiceClientExceptionManager.httpResponseVerification(VALID_SUCCESS_STATUS, VALID_SUCCESS_MESSAGE);
            }
        };
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_001: [The constructor shall store the provided connection string.] */
    @Test
    public void privateConstructorSucceeded()
    {
        // arrange

        // act
        ContractApiHttp httpDeviceRegistrationClient = Deencapsulation.newInstance(ContractApiHttp.class, new Class[]{ProvisioningConnectionString.class}, mockedProvisioningConnectionString);

        // assert
        assertNotNull(httpDeviceRegistrationClient);
        assertEquals(mockedProvisioningConnectionString, Deencapsulation.getField(httpDeviceRegistrationClient, "provisioningConnectionString"));
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_002: [The constructor shall throw IllegalArgumentException if the connection string is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void privateConstructorThrowsOnNullConnectionString()
    {
        // arrange

        // act
        Deencapsulation.newInstance(ContractApiHttp.class, new Class[]{ProvisioningConnectionString.class}, (ProvisioningConnectionString)null);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_003: [The createFromConnectionString shall throw IllegalArgumentException if the input string is null, threw by the constructor.] */
    @Test (expected = IllegalArgumentException.class)
    public void createFromConnectionStringThrowsOnNullConnectionString() throws ProvisioningServiceClientException
    {
        // arrange
        final ProvisioningConnectionString provisioningConnectionString = null;

        // act
        ContractApiHttp.createFromConnectionString(provisioningConnectionString);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_004: [The createFromConnectionString shall create a new ContractApiHttp instance and return it.] */
    @Test
    public void createFromConnectionStringSucceeded() throws ProvisioningServiceClientException
    {
        // arrange
        final ProvisioningConnectionString provisioningConnectionString = mockedProvisioningConnectionString;

        // act
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(provisioningConnectionString);

        // assert
        assertNotNull(contractApiHttp);
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_005: [The request shall create a SAS token based on the connection string.*/
    @Test
    public void requestCreatesSasToken() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        new Expectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes());
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = VALID_SUCCESS_STATUS;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes();
                mockedHttpResponse.getBody();
                result = VALID_BODY;
                mockedHttpResponse.getHeaderFields();
                result = VALID_HEADER;
                ProvisioningServiceClientExceptionManager.httpResponseVerification(VALID_SUCCESS_STATUS, VALID_SUCCESS_MESSAGE);
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
        requestNonStrictExpectations();

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_006: [If the request get problem to create the SAS token, it shall throw IllegalArgumentException.*/
    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnSasToken() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = new IllegalArgumentException("");
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_007: [The request shall create a HTTP URL based on the Device Registration path.*/
    @Test
    public void requestCreatesURL() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        requestNonStrictExpectations();
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
        String expectedURL = "https://" + VALID_HOST_NAME + "/" + VALID_PATH + "?api-version=" + SDKUtils.getServiceApiVersion();

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
        new Verifications()
        {
            {
                new URL(expectedURL);
                times = 1;
            }
        };
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_008: [If the provided path is null or empty, the request shall throw IllegalArgumentException.*/
    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnNullPath() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                null,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_009: [If the provided path contains not valid characters, the request shall throw IllegalArgumentException.*/
    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnEmptyPath() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                "",
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_009: [If the provided path contains not valid characters, the request shall throw IllegalArgumentException.*/
    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnWrongPath() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = new MalformedURLException();
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_010: [The request shall create a new HttpRequest.*/
    @Test
    public void requestCreatesHttpRequest() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        new Expectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes());
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = VALID_SUCCESS_STATUS;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes();
                mockedHttpResponse.getBody();
                result = VALID_BODY;
                mockedHttpResponse.getHeaderFields();
                result = VALID_HEADER;
                ProvisioningServiceClientExceptionManager.httpResponseVerification(VALID_SUCCESS_STATUS, VALID_SUCCESS_MESSAGE);
            }
        };
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
        requestNonStrictExpectations();

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_011: [If the request get problem creating the HttpRequest, it shall throw ProvisioningServiceClientTransportException.*/
    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void requestThrowsOnHttpRequestFailed() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes());
                result = new IOException();
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_012: [The request shall fill the http header with the standard parameters.] */
    @Test
    public void requestCreateHttpHeader() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
        requestNonStrictExpectations();

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
        new Verifications()
        {
            {
                mockedHttpRequest.setHeaderField("authorization", VALID_SASTOKEN);
                times = 1;
                mockedHttpRequest.setHeaderField("Request-Id", "1001");
                times = 1;
                mockedHttpRequest.setHeaderField("Accept", "application/json");
                times = 1;
                mockedHttpRequest.setHeaderField("Content-Type", "application/json");
                times = 1;
                mockedHttpRequest.setHeaderField("Content-Length", String.valueOf(VALID_PAYLOAD.length()));
                times = 1;
                mockedHttpRequest.setHeaderField("charset", "utf-8");
                times = 1;
            }
        };
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_013: [The request shall add the headerParameters to the http header, if provided.] */
    @Test
    public void requestAddHttpHeader() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        final Map<String, String> header = new HashMap<String, String>()
        {
            {
                put("key1", "val1");
                put("key2", "val2");
                put("key3", "val3");
            }
        };
        requestNonStrictExpectations();
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                header,
                VALID_PAYLOAD);

        // assert
        new Verifications()
        {
            {
                mockedHttpRequest.setHeaderField("key1", "val1");
                times = 1;
                mockedHttpRequest.setHeaderField("key2", "val2");
                times = 1;
                mockedHttpRequest.setHeaderField("key3", "val3");
                times = 1;
            }
        };
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_014: [The request shall send the request to the Device Provisioning Service by using the HttpRequest.send().*/
    @Test
    public void requestSendsHttpRequest() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);
        requestNonStrictExpectations();

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
        new Verifications()
        {
            {
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_015: [If the HttpRequest failed send the message, the request shall throw ProvisioningServiceClientTransportException, threw by the callee.*/
    @Test (expected = ProvisioningServiceClientException.class)
    public void requestThrowsOnSendHttpRequestFailed() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes());
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = new ProvisioningServiceClientException();
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }

    /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_016: [If the Device Provisioning Service respond to the HttpRequest with any error code, the request shall throw the appropriated ProvisioningServiceClientException, by calling ProvisioningServiceClientExceptionManager.responseVerification().*/
    @Test (expected = ProvisioningServiceClientException.class)
    public void requestThrowsOnDeviceProvisioningServiceError() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
                new ProvisioningSasToken(mockedProvisioningConnectionString);
                result = mockedProvisioningSasToken;
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes());
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = 401;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes();
            }
        };

        ContractApiHttp contractApiHttp = ContractApiHttp.createFromConnectionString(mockedProvisioningConnectionString);

        // act
        contractApiHttp.request(
                HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        // assert
    }
}
