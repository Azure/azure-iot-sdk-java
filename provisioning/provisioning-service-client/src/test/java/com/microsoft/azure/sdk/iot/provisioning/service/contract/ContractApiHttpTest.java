// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.contract;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.provisioning.service.transport.https.HttpMethod;
import com.microsoft.azure.sdk.iot.provisioning.service.transport.https.HttpRequest;
import com.microsoft.azure.sdk.iot.provisioning.service.transport.https.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningSasToken;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientExceptionManager;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.TokenCredentialCache;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
    TokenCredential mockedTokenCredential;

    @Mocked
    TokenCredentialCache mockedTokenCredentialCache;

    @Mocked
    AzureSasCredential mockedAzureSasCredential;

    @Mocked
    HttpRequest mockedHttpRequest;

    @Mocked
    HttpResponse mockedHttpResponse;

    @Mocked
    URL mockedURL;

    private final String VALID_PATH = "a/b";
    private final Map<String, String> VALID_HEADER = new HashMap<>();
    private final String VALID_PAYLOAD = "{}";
    private final byte[] VALID_BODY = VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8);
    private final int VALID_SUCCESS_STATUS = 200;
    private final String VALID_SUCCESS_MESSAGE = "success";
    private final String VALID_SASTOKEN = "validSas";
    private final String VALID_TOKENCREDENTIAL_STRING = "validTokenCredential";
    private final String VALID_AZURE_SASTOKEN = "validAzureSas";
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
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8));
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = VALID_SUCCESS_STATUS;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes(StandardCharsets.UTF_8);
                mockedHttpResponse.getBody();
                result = VALID_BODY;
                mockedHttpResponse.getHeaderFields();
                result = VALID_HEADER;
                ProvisioningServiceClientExceptionManager.httpResponseVerification(VALID_SUCCESS_STATUS, VALID_SUCCESS_MESSAGE);
            }
        };
    }

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

    @Test (expected = IllegalArgumentException.class)
    public void privateConstructorThrowsOnNullConnectionString()
    {
        // arrange

        // act
        Deencapsulation.newInstance(ContractApiHttp.class, new Class[]{ProvisioningConnectionString.class}, (ProvisioningConnectionString)null);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void createFromConnectionStringThrowsOnNullConnectionString() throws ProvisioningServiceClientException
    {
        // arrange
        final ProvisioningConnectionString provisioningConnectionString = null;

        // act
        ContractApiHttp.createFromConnectionString(provisioningConnectionString);

        // assert
    }

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

    @Test
    public void tokenCredentialConstructorSucceeded()
    {
        //arrange
        final TokenCredential credential = mockedTokenCredential;

        //act
        ContractApiHttp contractApiHttp = new ContractApiHttp(VALID_HOST_NAME, credential);

        //assert
        assertNotNull(contractApiHttp);
    }

    @Test (expected = IllegalArgumentException.class)
    public void tokenCredentialConstructorThrowsOnNullHostName()
    {
        //arrange
        final TokenCredential credential = mockedTokenCredential;

        //act
        new ContractApiHttp(null, credential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void tokenCredentialConstructorThrowsOnEmptyHostName()
    {
        //arrange
        final TokenCredential credential = mockedTokenCredential;

        //act
        new ContractApiHttp("", credential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void tokenCredentialConstructorThrowsOnNullCredential()
    {
        //arrange
        final TokenCredential credential = null;

        //act
        new ContractApiHttp(VALID_HOST_NAME, credential);

        //assert
    }

    @Test
    public void azureSasCredentialConstructorSucceeded()
    {
        //arrange
        final AzureSasCredential azureSasCredential = mockedAzureSasCredential;

        //act
        ContractApiHttp contractApiHttp = new ContractApiHttp(VALID_HOST_NAME, azureSasCredential);

        //assert
        assertNotNull(contractApiHttp);
    }

    @Test (expected = IllegalArgumentException.class)
    public void azureSasCredentialConstructorThrowsOnNullHostName()
    {
        //arrange
        final AzureSasCredential azureSasCredential = mockedAzureSasCredential;

        //act
        new ContractApiHttp(null, azureSasCredential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void azureSasCredentialConstructorThrowsOnEmptyHostName()
    {
        //arrange
        final AzureSasCredential azureSasCredential = mockedAzureSasCredential;

        //act
        new ContractApiHttp("", azureSasCredential);

        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void azureSasCredentialConstructorThrowsOnNullCredential()
    {
        //arrange
        final AzureSasCredential azureSasCredential = null;

        //act
        new ContractApiHttp(VALID_HOST_NAME, azureSasCredential);

        //assert
    }

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
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8));
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = VALID_SUCCESS_STATUS;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes(StandardCharsets.UTF_8);
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

    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnNullPath() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
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

    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnEmptyPath() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
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

    @Test (expected = IllegalArgumentException.class)
    public void requestThrowsOnWrongPath() throws ProvisioningServiceClientException, IOException
    {
        // arrange

        new NonStrictExpectations()
        {
            {
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
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8));
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = VALID_SUCCESS_STATUS;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes(StandardCharsets.UTF_8);
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

    @Test (expected = ProvisioningServiceClientTransportException.class)
    public void requestThrowsOnHttpRequestFailed() throws ProvisioningServiceClientException, IOException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                mockedProvisioningSasToken.toString();
                result = VALID_SASTOKEN;
                mockedProvisioningConnectionString.getHostName();
                result = VALID_HOST_NAME;
                new URL((String)any);
                result = mockedURL;
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8));
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

    @Test
    public void requestCreateHttpHeaderFromConnectionString() throws ProvisioningServiceClientException, IOException
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

    @Test
    public void requestPullsAuthorizationFromTokenCredential() throws ProvisioningServiceClientException
    {
        //arrange
        new Expectations()
        {
            {
                new TokenCredentialCache(mockedTokenCredential);
                result = mockedTokenCredentialCache;
                mockedTokenCredentialCache.getTokenString();
                result = VALID_TOKENCREDENTIAL_STRING;
            }
        };
        ContractApiHttp contractApiHttp = new ContractApiHttp(VALID_HOST_NAME, mockedTokenCredential);

        //act
        contractApiHttp.request(HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        //assert
    }

    @Test
    public void requestPullsAuthorizationFromAzureSasCredential() throws ProvisioningServiceClientException
    {
        //arrange
        new Expectations()
        {
            {
                mockedAzureSasCredential.getSignature();
                result = VALID_AZURE_SASTOKEN;
            }
        };
        ContractApiHttp contractApiHttp = new ContractApiHttp(VALID_HOST_NAME, mockedAzureSasCredential);

        //act
        contractApiHttp.request(HttpMethod.PUT,
                VALID_PATH,
                VALID_HEADER,
                VALID_PAYLOAD);

        //assert
    }

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
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8));
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
                new HttpRequest(mockedURL, HttpMethod.PUT, VALID_PAYLOAD.getBytes(StandardCharsets.UTF_8));
                result = mockedHttpRequest;
                mockedHttpRequest.send();
                result = mockedHttpResponse;
                mockedHttpResponse.getStatus();
                result = 401;
                mockedHttpResponse.getErrorReason();
                result = VALID_SUCCESS_MESSAGE.getBytes(StandardCharsets.UTF_8);
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
