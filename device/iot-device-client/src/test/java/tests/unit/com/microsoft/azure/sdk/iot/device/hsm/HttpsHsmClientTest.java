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
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
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

}
