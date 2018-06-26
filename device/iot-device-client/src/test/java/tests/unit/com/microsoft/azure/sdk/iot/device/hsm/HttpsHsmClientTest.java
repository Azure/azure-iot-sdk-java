/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.*;
import com.microsoft.azure.sdk.iot.device.hsm.parser.ErrorResponse;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest;
import com.microsoft.azure.sdk.iot.device.hsm.parser.SignResponse;
import com.microsoft.azure.sdk.iot.device.hsm.parser.TrustBundleResponse;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import mockit.*;
import org.junit.Test;

import java.io.*;
import java.net.*;
import java.nio.channels.Channels;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

public class HttpsHsmClientTest
{
    @Mocked
    SignResponse mockedSignResponse;

    @Mocked
    SignRequest mockedSignRequest;

    @Mocked
    HttpsConnection mockedHttpsConnection;

    @Mocked
    HttpsRequest mockedHttpsRequest;

    @Mocked
    HttpsResponse mockedHttpsResponse;

    @Mocked
    ErrorResponse mockedErrorResponse;

    @Mocked
    URL mockedURL;

    @Mocked
    HttpsRequestResponseSerializer mockedHttpsRequestResponseSerializer;

    @Mocked
    UnixSocketAddress mockedUnixSocketAddress;

    @Mocked
    UnixSocketChannel mockedUnixSocketChannel;

    @Mocked
    Channels mockedChannels;

    @Mocked
    OutputStream mockedOutputStream;

    @Mocked
    TrustBundleResponse mockedTrustBundleResponse;

    private static final String expectedBaseUrl = "some.base.url";
    private static final String expectedApiVersion = "1.2.3";
    private static final String expectedName = "someModuleName";
    private static final String expectedGenId = "gen1";
    private static final String expectedSchemeHttps = "Https";
    private static final String expectedSchemeHttp = "Http";
    private static final String expectedSchemeUnix = "Unix";



    // Tests_SRS_HSMHTTPCLIENT_34_001: [This constructor shall save the provided baseUrl.]
    @Test
    public void constructorSavesBaseUrl() throws TransportException, UnsupportedEncodingException, MalformedURLException, URISyntaxException
    {
        //act
        HttpsHsmClient client = new HttpsHsmClient(expectedBaseUrl);

        //assert
        assertEquals(expectedBaseUrl, Deencapsulation.getField(client, "baseUrl"));
    }


    // Tests_SRS_HSMHTTPCLIENT_34_002: [This function shall build an http request with the url in the format
    // <base url>/modules/<url encoded name>/genid/<url encoded gen id>/sign?api-version=<url encoded api version>.]
    // Tests_SRS_HSMHTTPCLIENT_34_003: [This function shall build an http request with headers ContentType and Accept with value application/json.]
    // Tests_SRS_HSMHTTPCLIENT_34_004: [If the response from the http call is 200, this function shall return the SignResponse built from the response body json.]
    @Test
    public void signSuccess() throws IOException, TransportException, URISyntaxException, HsmException
    {
        //arrange
        final String expectedUrl = expectedBaseUrl + "/modules/" + URLEncoder.encode(expectedName, "UTF-8") + "/genid/" + URLEncoder.encode(expectedGenId, "UTF-8") + "/sign?api-version=" + URLEncoder.encode(expectedApiVersion, "UTF-8");
        final String expectedJson = "some json";
        final String expectedResponseBody = "some json response";
        new NonStrictExpectations()
        {
            {
                mockedSignRequest.toJson();
                result = expectedJson;

                new HttpsRequest((URL) any, HttpsMethod.POST, expectedJson.getBytes(), TransportUtils.USER_AGENT_STRING);
                result = mockedHttpsRequest;

                mockedHttpsRequest.send();
                result = mockedHttpsResponse;

                mockedHttpsResponse.getStatus();
                result = 200;

                mockedHttpsResponse.getBody();
                result = expectedResponseBody.getBytes();
            }
        };

        HttpsHsmClient client = new HttpsHsmClient(expectedBaseUrl);
        Deencapsulation.setField(client, "scheme", expectedSchemeHttps);

        //act
        client.sign(expectedApiVersion, expectedName, mockedSignRequest, expectedGenId);

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

                SignResponse.fromJson(expectedResponseBody);
                times = 1;
            }
        };
    }

    // Tests_SRS_HSMHTTPCLIENT_34_006: [If the scheme of the provided url is Unix, this function shall send the http request using unix domain sockets.]
    @Test
    public void signSuccessWithUnix() throws IOException, TransportException, URISyntaxException, HsmException
    {
        //arrange
        final String expectedJson = "some json";
        final byte[] expectedMetaData = "some headers and such".getBytes();
        final byte[] expectedBody = "http request's body".getBytes();
        new NonStrictExpectations()
        {
            {
                mockedSignRequest.toJson();
                result = expectedJson;

                new HttpsRequest((URL) any, HttpsMethod.POST, expectedJson.getBytes(), TransportUtils.USER_AGENT_STRING);
                result = mockedHttpsRequest;

                HttpsRequestResponseSerializer.serializeRequest(mockedHttpsRequest);
                result = expectedMetaData;

                mockedHttpsRequest.getBody();
                result = expectedBody;


                new UnixSocketAddress(anyString);
                result = mockedUnixSocketAddress;

                UnixSocketChannel.open(mockedUnixSocketAddress);
                result = mockedUnixSocketChannel;

                Channels.newOutputStream(mockedUnixSocketChannel);
                result = mockedOutputStream;

                HttpsRequestResponseSerializer.deserializeResponse((BufferedReader) any);
                result = mockedHttpsResponse;

                mockedHttpsResponse.getStatus();
                result = 200;
            }
        };

        HttpsHsmClient client = new HttpsHsmClient(expectedBaseUrl);
        Deencapsulation.setField(client, "scheme", expectedSchemeUnix);

        //act
        client.sign(expectedApiVersion, expectedName, mockedSignRequest, expectedGenId);

        //assert
        new Verifications()
        {
            {
                HttpsRequestResponseSerializer.serializeRequest(mockedHttpsRequest);
                times = 1;

                HttpsRequestResponseSerializer.deserializeResponse((BufferedReader) any);
                times = 1;

                mockedHttpsRequest.send();
                times = 0;
            }
        };
    }

    // Tests_SRS_HSMHTTPCLIENT_34_005: [If the response from the http call is not 200, this function shall throw an HsmException.]
    @Test
    public void signThrowsIfResponseIsNot200() throws UnsupportedEncodingException, MalformedURLException, TransportException, URISyntaxException
    {
        //arrange
        final String expectedUrl = expectedBaseUrl + "/modules/" + URLEncoder.encode(expectedName, "UTF-8") + "/sign?api-version=" + URLEncoder.encode(expectedApiVersion, "UTF-8");
        final String expectedJson = "some json";
        final String expectedResponseBody = "some json response";
        new NonStrictExpectations()
        {
            {
                mockedSignRequest.toJson();
                result = expectedJson;

                new HttpsRequest((URL) any, HttpsMethod.POST, expectedJson.getBytes(), TransportUtils.USER_AGENT_STRING);
                result = mockedHttpsRequest;

                mockedHttpsRequest.send();
                result = mockedHttpsResponse;

                mockedHttpsResponse.getStatus();
                result = 401;

                mockedHttpsResponse.getBody();
                result = expectedResponseBody.getBytes();
            }
        };

        HttpsHsmClient client = new HttpsHsmClient(expectedUrl);
        Deencapsulation.setField(client, "scheme", expectedSchemeHttps);

        boolean transportExceptionThrown = false;

        //act
        try
        {
            client.sign(expectedApiVersion, expectedName, mockedSignRequest, expectedGenId);
        }
        catch (HsmException e)
        {
            transportExceptionThrown = true;
        }
        catch (IOException e)
        {
            fail("Unexpected exception encountered: " + e.getMessage());
        }

        //assert
        assertTrue(transportExceptionThrown);
        new Verifications()
        {
            {
                ErrorResponse.fromJson(expectedResponseBody);
                times = 1;
            }
        };
    }

    // Tests_SRS_HSMHTTPCLIENT_34_007: [If the provided api version is null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void getTrustBundleThrowsForNullApiVersion() throws URISyntaxException, TransportException, MalformedURLException, HsmException, UnsupportedEncodingException
    {
        //arrange
        HttpsHsmClient client = new HttpsHsmClient(expectedBaseUrl);

        //act
        client.getTrustBundle(null);
    }

    // Tests_SRS_HSMHTTPCLIENT_34_008: [This function shall build an http request with the url in the format
    // <base url>/trust-bundle?api-version=<url encoded api version>.]
    // Tests_SRS_HSMHTTPCLIENT_34_009: [This function shall send a GET http request to the built url.]
    // Tests_SRS_HSMHTTPCLIENT_34_010: [If the response from the http request is 200, this function shall return the trust bundle response.]
    @Test
    public void getTrustBundleSuccess() throws URISyntaxException, TransportException, MalformedURLException, HsmException, UnsupportedEncodingException
    {
        //arrange
        HttpsHsmClient client = new HttpsHsmClient(expectedBaseUrl);
        final String expectedUrl = expectedBaseUrl + "/trust-bundle?api-version=" + expectedApiVersion;

        //assert
        new StrictExpectations()
        {
            {
                new URL(expectedUrl);
                result = mockedURL;

                new HttpsRequest(mockedURL, HttpsMethod.GET, null, anyString);
                result = mockedHttpsRequest;

                mockedHttpsRequest.send();
                result = mockedHttpsResponse;
                times = 1;

                mockedHttpsResponse.getStatus();
                result = 200;

                mockedHttpsResponse.getBody();
                result = "some trust bundle".getBytes();

                new TrustBundleResponse("some trust bundle");
                result = mockedTrustBundleResponse;
            }
        };

        //act
        client.getTrustBundle(expectedApiVersion);
    }

    // Tests_SRS_HSMHTTPCLIENT_34_011: [If the response from the http request is not 200, this function shall throw an HSMException.]
    @Test
    public void getTrustBundleThrowsIfResponseNot200() throws URISyntaxException, TransportException, MalformedURLException, HsmException, UnsupportedEncodingException
    {
        //arrange
        HttpsHsmClient client = new HttpsHsmClient(expectedBaseUrl);
        final String expectedUrl = expectedBaseUrl + "/trust-bundle?api-version=" + expectedApiVersion;
        final int expectedStatusCode = 102;
        final String expectedErrorMessage = "The protons are out of control!";
        //assert
        new StrictExpectations()
        {
            {
                new URL(expectedUrl);
                result = mockedURL;

                new HttpsRequest(mockedURL, HttpsMethod.GET, null, anyString);
                result = mockedHttpsRequest;

                mockedHttpsRequest.send();
                result = mockedHttpsResponse;
                times = 1;

                mockedHttpsResponse.getStatus();
                result = expectedStatusCode;

                mockedHttpsResponse.getBody();
                result = "some trust bundle".getBytes();

                ErrorResponse.fromJson("some trust bundle");
                result = mockedErrorResponse;

                mockedErrorResponse.getMessage();
                result = expectedErrorMessage;
            }
        };

        boolean correctExceptionEncountered = false;

        //act
        try
        {
            client.getTrustBundle(expectedApiVersion);
        }
        catch (HsmException e)
        {
            assertTrue(e.getMessage().contains(""+expectedStatusCode));
            assertTrue(e.getMessage().contains(expectedErrorMessage));
            correctExceptionEncountered = true;
        }
        catch (Exception e)
        {
            fail("Unexpected exception encountered");
        }

        assertTrue("expected hsm exception, but no exception encountered", correctExceptionEncountered);
    }
}
