/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.hsm;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.hsm.HttpsRequestResponseSerializer;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class HttpsRequestResponseSerializerTest
{
    @Mocked
    HttpsConnection mockedHttpsConnection;

    @Mocked
    HttpsRequest mockedHttpsRequest;

    @Mocked
    HttpsResponse mockedHttpsResponse;

    @Mocked
    URI mockedURI;

    @Mocked
    URL mockedURL;

    private static final String expectedMethod = "POST";

    private void uriExpectations()
    {
        new NonStrictExpectations()
        {
            {
                mockedURI.getScheme();
                result = "unix";

                mockedURI.getHost();
                result = "localhost:8081";

                mockedURI.getPath();
                result = "/modules/testModule/sign";

                mockedHttpsRequest.getHttpMethod();
                result = expectedMethod;

            }
        };
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_003: [This function shall serialize the provided httpsRequest into the form:
    // POST /modules/<moduleName>/sign?api-version=2018-06-28 HTTP/1.1
    // Host: localhost:8081
    // Connection: close
    // <header>: <value>
    // <header>: <value1>; <value2>
    // .]
    @Test
    public void serializeTestWithQuery() throws MalformedURLException, TransportException, UnsupportedEncodingException, URISyntaxException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedHttpsRequest.getRequestUrl();
                result = mockedURL;

                mockedHttpsRequest.getRequestHeaders();
                result = "Connection: close\r\n" + "Content-Type: application/json\r\n";

                mockedHttpsRequest.getBody();
                result = "11111".getBytes();
            }
        };

        uriExpectations();

        String expected = "POST /modules/testModule/sign?api-version=2018-06-28 HTTP/1.1\r\nHost: localhost:8081\r\nConnection: close\r\nContent-Type: application/json\r\nContent-Length: 5\r\n\r\n";
        byte[] body = "11111".getBytes();
        HttpsRequest request = new HttpsRequest(new URL("https://localhost:8081/modules/testModule/sign?api-version=2018-06-28"), HttpsMethod.GET, body, null);
        request.setHeaderField("content-type", "application/json");
        request.setHeaderField("content-length", "5");

        //act
        byte[] httpsRequestData = HttpsRequestResponseSerializer.serializeRequest(request, "/modules/testModule/sign", "api-version=2018-06-28",  "localhost:8081");

        //assert
        String httpsRequestString = new String(httpsRequestData);
        assertEquals(expected, httpsRequestString);
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_003: [This function shall serialize the provided httpsRequest into the form:
    // POST /modules/<moduleName>/sign?api-version=2018-06-28 HTTP/1.1
    // Host: localhost:8081
    // Connection: close
    // <header>: <value>
    // <header>: <value1>; <value2>
    // .]
    @Test
    public void serializeTestWithoutQuery() throws MalformedURLException, TransportException, UnsupportedEncodingException, URISyntaxException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedHttpsRequest.getRequestUrl();
                result = mockedURL;

                mockedHttpsRequest.getRequestHeaders();
                result = "Connection: close\r\n" + "Content-Type: application/json\r\n";

                mockedHttpsRequest.getBody();
                result = "11111".getBytes();
            }
        };

        uriExpectations();

        String expected = "POST /modules/testModule/sign HTTP/1.1\r\nHost: localhost:8081\r\nConnection: close\r\nContent-Type: application/json\r\nContent-Length: 5\r\n\r\n";
        byte[] body = "11111".getBytes();
        HttpsRequest request = new HttpsRequest(new URL("https://localhost:8081/modules/testModule/sign"), HttpsMethod.GET, body, null);
        request.setHeaderField("content-type", "application/json");
        request.setHeaderField("content-length", "5");

        //act
        byte[] httpsRequestData = HttpsRequestResponseSerializer.serializeRequest(request, "/modules/testModule/sign", "",  "localhost:8081");

        //assert
        String httpsRequestString = new String(httpsRequestData);
        assertEquals(expected, httpsRequestString);
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_001: [If the provided request is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void serializeThrowsForNullRequest() throws UnsupportedEncodingException, URISyntaxException
    {
        //act
        HttpsRequestResponseSerializer.serializeRequest(null, "modules/testModule/sign", "api-version=2018-06-28",  "");
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_002: [If the provided request's url is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void serializeThrowsForRequestWithNullRequestUrl() throws UnsupportedEncodingException, URISyntaxException
    {
        new NonStrictExpectations()
        {
            {
                mockedHttpsRequest.getRequestUrl();
                result = null;
            }
        };

        //act
        HttpsRequestResponseSerializer.serializeRequest(mockedHttpsRequest, "modules/testModule/sign", "api-version=2018-06-28",  "");
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_004: [If the provided bufferedReader is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void deserializeThrowsForNullReader() throws IOException
    {
        //act
        HttpsRequestResponseSerializer.deserializeResponse(null);
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_005: [This function shall read lines from the provided buffered
    // reader with the following format:
    //  <version> <status code> <error reason>
    //  <header>:<value>
    //  <header>:<value>
    //  ...
    //  <http body content>
    // .]
    @Test
    public void deserializeSuccess() throws IOException
    {
        //arrange
        final byte[] expectedBody = ("test").getBytes();
        final byte[] expectedBodyWithNewLine = ("test\r\n").getBytes();

        final Map<String, List<String>> expectedHeaders = new HashMap<>();
        List<String> values1 = new ArrayList<>();
        List<String> values2 = new ArrayList<>();
        values1.add("value1");
        values2.add("value2");
        expectedHeaders.put("header1", values1);
        expectedHeaders.put("header2", values2);
        String stringToDeserialize =
                "HTTP/1.1 203 OK\r\n" +
                        "header1:value1\r\n" +
                        "header2:value2\r\n" +
                        "\r\n" +
                        new String(expectedBodyWithNewLine);


        new Expectations()
        {
            {
                new HttpsResponse(203, expectedBody, expectedHeaders, "OK".getBytes());
                result = mockedHttpsResponse;

                mockedHttpsResponse.getStatus();
                result = 200;

                mockedHttpsResponse.getBody();
                result = expectedBody;

                mockedHttpsResponse.getHeaderFields();
                result = expectedHeaders;
            }
        };

        //act
        HttpsResponse response = HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));

        //assert
        assertEquals(200, response.getStatus());
        assertEquals(new String(response.getBody()), "test");
        assertEquals(response.getHeaderFields(), expectedHeaders);
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_006: [If the buffered reader doesn't have at least one line, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void deserializeThrowsForMissingStatusLine() throws IOException
    {
        //arrange
        String stringToDeserialize = "";

        //act
        HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_006: [If the buffered reader's first line does not have the version, status code, and error reason split by a space, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void deserializeThrowsForStatusLineWithoutVersion() throws IOException
    {
        //arrange
        String stringToDeserialize = "200 OK\r\n";

        //act
        HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_006: [If the buffered reader's first line does not have the version, status code, and error reason split by a space, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void deserializeThrowsForStatusLineWithoutStatusCode() throws IOException
    {
        //arrange
        String stringToDeserialize = "HTTP/1.1 OK\r\n";

        //act
        HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_006: [If the buffered reader's first line does not have the version, status code, and error reason split by a space, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void deserializeThrowsForStatusLineWithoutReason() throws IOException
    {
        //arrange
        String stringToDeserialize = "HTTP/1.1 200\r\n";

        //act
        HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_007: [If the status code is not parsable into an int, this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void deserializeThrowsForStatusLineWithInvalidStatusCode() throws IOException
    {
        //arrange
        String stringToDeserialize = "HTTP/1.1 notAStatusCode OK\r\n";

        //act
        HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));
    }

    // Tests_SRS_HTTPREQUESTRESPONSESERIALIZER_34_008: [If a header is not separated from its value by a':', this function shall throw an IOException.]
    @Test (expected = IOException.class)
    public void deserializeThrowsForHeaderWithoutSeparators() throws IOException
    {
        //arrange
        final byte[] expectedBody = ("test").getBytes();
        final byte[] expectedBodyWithNewLine = ("test\r\n").getBytes();

        final Map<String, List<String>> expectedHeaders = new HashMap<>();
        List<String> values1 = new ArrayList<>();
        List<String> values2 = new ArrayList<>();
        values1.add("value1");
        values2.add("value2");
        expectedHeaders.put("header1", values1);
        expectedHeaders.put("header2", values2);
        String stringToDeserialize =
                "HTTP/1.1 203 OK\r\n" +
                        "header1 value1\r\n" +
                        "\r\n" +
                        new String(expectedBodyWithNewLine);

        //act
        HttpsRequestResponseSerializer.deserializeResponse(new BufferedReader(new StringReader(stringToDeserialize)));
    }
}