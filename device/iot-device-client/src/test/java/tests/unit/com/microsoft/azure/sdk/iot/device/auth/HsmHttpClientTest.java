/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.HsmHttpClient;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmErrorResponse;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignRequest;
import com.microsoft.azure.sdk.iot.device.auth.HttpHsmSignResponse;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class HsmHttpClientTest
{
    @Mocked
    HttpHsmSignResponse mockedHttpHsmSignResponse;

    @Mocked
    HttpHsmSignRequest mockedHttpHsmSignRequest;

    @Mocked
    URL mockedURL;

    @Mocked
    HttpsConnection mockedHttpsConnection;

    @Mocked
    HttpsRequest mockedHttpsRequest;

    @Mocked
    HttpsResponse mockedHttpsResponse;

    @Mocked
    HttpHsmErrorResponse mockedHttpHsmErrorResponse;

    private static final String expectedBaseUrl = "some.base.url";
    private static final String expectedApiVersion = "1.2.3";
    private static final String expectedName = "someModuleName";

    // Tests_SRS_HSMHTTPCLIENT_34_001: [This constructor shall save the provided baseUrl.]
    @Test
    public void constructorSavesBaseUrl() throws TransportException, UnsupportedEncodingException, MalformedURLException
    {
        //act
        HsmHttpClient client = new HsmHttpClient(expectedBaseUrl);

        //assert
        assertEquals(expectedBaseUrl, Deencapsulation.getField(client, "baseUrl"));
    }


    // Tests_SRS_HSMHTTPCLIENT_34_002: [This function shall build an http request with the url in the format
    // <base url>/modules/<url encoded name>/sign?api-version=<url encoded api version>.]
    // Tests_SRS_HSMHTTPCLIENT_34_003: [This function shall build an http request with headers ContentType and Accept with value application/json.]
    // Tests_SRS_HSMHTTPCLIENT_34_004: [If the response from the http call is 200, this function shall return the HttpHsmSignResponse built from the response body json.]
    @Test
    public void signSuccess() throws UnsupportedEncodingException, MalformedURLException, TransportException
    {
        //arrange
        final String expectedUrl = expectedBaseUrl + "/modules/" + URLEncoder.encode(expectedName, "UTF-8") + "/sign?api-version=" + URLEncoder.encode(expectedApiVersion, "UTF-8");
        final String expectedJson = "some json";
        final String expectedResponseBody = "some json response";
        HsmHttpClient client = new HsmHttpClient(expectedBaseUrl);
        new NonStrictExpectations()
        {
            {
                mockedHttpHsmSignRequest.toJson();
                result = expectedJson;

                new URL(expectedUrl);
                result = mockedURL;

                new HttpsRequest(mockedURL, HttpsMethod.POST, expectedJson.getBytes(), TransportUtils.USER_AGENT_STRING);
                result = mockedHttpsRequest;

                mockedHttpsRequest.send();
                result = mockedHttpsResponse;

                mockedHttpsResponse.getStatus();
                result = 200;

                mockedHttpsResponse.getBody();
                result = expectedResponseBody.getBytes();
            }
        };

        //act
        client.sign(expectedApiVersion, expectedName, mockedHttpHsmSignRequest);

        //assert
        new Verifications()
        {
            {
                mockedHttpsRequest.setHeaderField("ContentType", "application/json");
                times = 1;

                mockedHttpsRequest.setHeaderField("Accept", "application/json");
                times = 1;

                new URL(expectedUrl);
                times = 1;

                HttpHsmSignResponse.fromJson(expectedResponseBody);
                times = 1;
            }
        };
    }

    // Tests_SRS_HSMHTTPCLIENT_34_005: [If the response from the http call is not 200, this function shall throw a transport exception.]
    @Test
    public void signThrowsIfResponseIsNot200() throws UnsupportedEncodingException, MalformedURLException, TransportException
    {
        //arrange
        final String expectedUrl = expectedBaseUrl + "/modules/" + URLEncoder.encode(expectedName, "UTF-8") + "/sign?api-version=" + URLEncoder.encode(expectedApiVersion, "UTF-8");
        final String expectedJson = "some json";
        final String expectedResponseBody = "some json response";
        HsmHttpClient client = new HsmHttpClient(expectedBaseUrl);
        new NonStrictExpectations()
        {
            {
                mockedHttpHsmSignRequest.toJson();
                result = expectedJson;

                new URL(expectedUrl);
                result = mockedURL;

                new HttpsRequest(mockedURL, HttpsMethod.POST, expectedJson.getBytes(), TransportUtils.USER_AGENT_STRING);
                result = mockedHttpsRequest;

                mockedHttpsRequest.send();
                result = mockedHttpsResponse;

                mockedHttpsResponse.getStatus();
                result = 401;

                mockedHttpsResponse.getBody();
                result = expectedResponseBody.getBytes();
            }
        };

        boolean transportExceptionThrown = false;

        //act
        try
        {
            client.sign(expectedApiVersion, expectedName, mockedHttpHsmSignRequest);
        }
        catch (TransportException e)
        {
            transportExceptionThrown = true;
        }

        //assert
        assertTrue(transportExceptionThrown);
        new Verifications()
        {
            {
                HttpHsmErrorResponse.fromJson(expectedResponseBody);
                times = 1;
            }
        };
    }

}
