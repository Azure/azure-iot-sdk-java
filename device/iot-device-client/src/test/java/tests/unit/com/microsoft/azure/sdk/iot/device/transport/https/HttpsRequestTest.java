// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsRequest;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/** Unit tests for HttpsRequest. */
public class HttpsRequestTest
{
    // Tests_SRS_HTTPSREQUEST_11_001: [The function shall open a connection with the given URL as the endpoint.]
    @Test
    public void constructorOpensConnection(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request = new HttpsRequest(mockUrl, httpsMethod, body, "");

        request.send();
        new Verifications()
        {
            {
                new HttpsConnection(mockUrl, (HttpsMethod) any);
            }
        };
    }

    // Tests_SRS_HTTPSREQUEST_11_002: [The function shall write the body to the connection.]
    @Test
    public void constructorWritesBodyToConnection(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] body = { 1, 2, 3 };
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request = new HttpsRequest(mockUrl, httpsMethod, body, "");

        request.send();

        final byte[] expectedBody = body;
        new Verifications()
        {
            {
                new HttpsConnection(mockUrl, (HttpsMethod) any)
                        .writeOutput(expectedBody);
            }
        };
    }

    // Tests_SRS_HTTPSREQUEST_11_004: [The function shall use the given HTTPS method (i.e. GET) as the request method.]
    @Test
    public void constructorSetsHttpsMethodCorrectly(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request = new HttpsRequest(mockUrl, httpsMethod, body, "");
        request.send();

        new Verifications()
        {
            {
                new HttpsConnection((URL) any, httpsMethod);
            }
        };
    }

    // Tests_SRS_HTTPSREQUEST_11_008: [The function shall send an HTTPS request as formatted in the constructor.]
    @Test
    public void sendHasCorrectHttpsMethod() throws TransportException, MalformedURLException
    {
        final HttpsMethod expectedMethod = HttpsMethod.GET;
        final byte[] body = new byte[0];
        new MockUp<HttpsConnection>()
        {
            HttpsMethod testMethod;

            @Mock
            public void $init(URL url, HttpsMethod method)
            {
                this.testMethod = method;
            }

            @Mock
            public void connect()
            {
                assertThat(testMethod, is(expectedMethod));
            }

            @Mock
            public void setRequestMethod(HttpsMethod method)
            {
                this.testMethod = method;
            }

            // every method that is used must be manually mocked.
            @Mock
            public void setRequestHeader(String field, String value)
            {

            }

            @Mock
            public void writeOutput(byte[] body)
            {

            }

            @Mock
            public byte[] readInput()
            {
                return new byte[0];
            }

            @Mock
            public byte[] readError()
            {
                return new byte[0];
            }

            @Mock
            public int getResponseStatus()
            {
                return 0;
            }

            @Mock
            public Map<String, List<String>> getResponseHeaders()
            {
                return new HashMap<>();
            }
        };

        URL mockUrl = new URL("https://www.microsoft.com");

        HttpsRequest request =
                new HttpsRequest(mockUrl, expectedMethod, body, "");
        request.send();
    }

    // Tests_SRS_HTTPSREQUEST_11_008: [The function shall send an HTTPS request as formatted in the constructor.]
    @Test
    public void sendSetsHeaderFieldsCorrectly() throws TransportException, MalformedURLException
    {
        final HttpsMethod expectedMethod = HttpsMethod.GET;
        final byte[] body = new byte[0];
        final String field0 = "test-field0";
        final String value0 = "test-value0";
        final String field1 = "test-field1";
        final String value1 = "test-value1";
        final String userAgent = "User-Agent";
        final String userAgentValue = TransportUtils.USER_AGENT_STRING;
        new MockUp<HttpsConnection>()
        {
            Map<String, String> testHeaderFields = new HashMap<>();

            @Mock
            public void $init(URL url, HttpsMethod method)
            {
            }

            @Mock
            public void connect()
            {
                assertThat(testHeaderFields.size(), is(4));
                assertThat(testHeaderFields.get(field0), is(value0));
                assertThat(testHeaderFields.get(field1), is(value1));
                assertThat(testHeaderFields.get(userAgent), is(userAgentValue));
            }

            @Mock
            public void setRequestHeader(String field, String value)
            {
                testHeaderFields.put(field, value);
            }

            // every method that is used must be manually mocked.
            @Mock
            public void setRequestMethod(HttpsMethod method)
            {

            }

            @Mock
            public void writeOutput(byte[] body)
            {

            }

            @Mock
            public byte[] readInput()
            {
                return new byte[0];
            }

            @Mock
            public byte[] readError()
            {
                return new byte[0];
            }

            @Mock
            public int getResponseStatus()
            {
                return 0;
            }

            @Mock
            public Map<String, List<String>> getResponseHeaders()
                    throws IOException
            {
                return new HashMap<>();
            }
        };


        URL mockUrl = new URL("Http://www.microsoft.com");

        HttpsRequest request = new HttpsRequest(mockUrl, expectedMethod, body, userAgentValue);
        request.setHeaderField(field0, value0);
        request.setHeaderField(field1, value1);
        request.send();
    }

    // Tests_SRS_HTTPSREQUEST_11_008: [The function shall send an HTTPS request as formatted in the constructor.]
    @Test
    public void sendWritesBodyToOutputStream() throws TransportException, MalformedURLException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final byte[] expectedBody = { 1, 2, 3 };
        new MockUp<HttpsConnection>()
        {
            byte[] testBody;

            @Mock
            public void $init(URL url, HttpsMethod method)
            {
            }

            @Mock
            public void connect()
            {
                assertThat(testBody, is(expectedBody));
            }

            @Mock
            public void writeOutput(byte[] body)
            {
                this.testBody = body;
            }

            // every method that is used must be manually mocked.
            @Mock
            public void setRequestHeader(String field, String value)
            {

            }

            @Mock
            public void setRequestMethod(HttpsMethod method)
            {

            }

            @Mock
            public byte[] readInput()
            {
                return new byte[0];
            }

            @Mock
            public byte[] readError()
            {
                return new byte[0];
            }

            @Mock
            public int getResponseStatus()
            {
                return 0;
            }

            @Mock
            public Map<String, List<String>> getResponseHeaders()
            {
                return new HashMap<>();
            }
        };
        URL mockUrl = new URL("https://www.microsoft.com");

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, expectedBody, "");
        request.send();
    }

    // Tests_SRS_HTTPSREQUEST_11_009: [The function shall return the HTTPS response received, including the status code, body, header fields, and error reason (if any).]
    @Test
    public void sendReadsStatusCode(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] body = new byte[0];
        final int status = 204;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockConn.getResponseStatus();
                result = status;
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, body, "");
        HttpsResponse response = request.send();
        int testStatus = response.getStatus();

        final int expectedStatus = status;
        assertThat(testStatus, is(expectedStatus));
    }

    // Tests_SRS_HTTPSREQUEST_11_009: [The function shall return the HTTPS response received, including the status code, body (if 200 status code), header fields, and error reason (if any).]
    @Test
    public void sendReturnsBody(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] requestBody = new byte[0];
        final byte[] responseBody = { 1, 2, 3, 0, 4 };
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockConn.readInput();
                result = responseBody;
                mockConn.getResponseStatus();
                result = 200;
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, requestBody, "");
        HttpsResponse response = request.send();
        byte[] testBody = response.getBody();

        final byte[] expectedBody = responseBody;
        assertThat(testBody, is(expectedBody));
    }

    // Tests_SRS_HTTPSREQUEST_11_009: [The function shall return the HTTPS response received, including the status code, body, header fields, and error reason (if any).]
    @Test
    public void sendReturnsHeaderFields(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final Map<String, List<String>> headerFields = new HashMap<>();
        final String field = "test-field";
        final List<String> values = new LinkedList<>();
        final String value0 = "test-value0";
        final String value1 = "test-value1";
        values.add(value0);
        values.add(value1);
        headerFields.put(field, values);
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockConn.getResponseHeaders();
                result = headerFields;
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, body, "");
        HttpsResponse response = request.send();
        String testValues = response.getHeaderField(field);

        final String expectedValues = value0 + "," + value1;
        assertThat(testValues, is(expectedValues));
    }

    // Tests_SRS_HTTPSREQUEST_11_013: [The function shall set the header field with the given name to the given value.]
    @Test
    public void setHeaderFieldSetsHeaderField(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final byte[] body = new byte[0];
        final String field = "test-field";
        final String value = "test-value";
        new NonStrictExpectations()
        {
            {
                new HttpsConnection((URL) any, httpsMethod);
                result = mockConn;

                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, body, "");
        request.setHeaderField(field, value);
        request.send();
        new Verifications()
        {
            {
                mockConn.setRequestHeader(field, value);
            }
        };
    }

    // Tests_SRS_HTTPSREQUEST_11_014: [The function shall set the read timeout for the request to the given value.]
    @Test
    public void setReadTimeoutSetsReadTimeout(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final byte[] body = new byte[0];
        final int readTimeout = 1;
        new NonStrictExpectations()
        {
            {
                new HttpsConnection(mockUrl, httpsMethod);
                result = mockConn;

                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, body, "");
        request.setReadTimeoutMillis(readTimeout);

        final int expectedReadTimeout = readTimeout;

        request.send();
        new Verifications()
        {
            {
                mockConn.setReadTimeoutMillis(expectedReadTimeout);
            }
        };
    }

    //Tests_SRS_HTTPSREQUEST_25_016: [The function shall set the SSL context for the IotHub.]
    @Test
    public void setSSLContextSetsSSLContext(@Mocked final HttpsConnection mockConn,
                                            @Mocked final SSLContext mockedContext,
                                            final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, body, "");
        request.setSSLContext(mockedContext);

        request.send();

        new Verifications()
        {
            {
                Deencapsulation.invoke(mockConn, "setSSLContext", mockedContext);
            }
        };
    }

    //Tests_SRS_HTTPSREQUEST_25_015: [The function shall throw IllegalArgumentException if argument is null.]
    @Test (expected = IllegalArgumentException.class)
    public void setSSLContextThrowsOnNull(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
            }
        };

        HttpsRequest request =
                new HttpsRequest(mockUrl, httpsMethod, body, "");
        request.setSSLContext(null);
    }

    // Tests_SRS_HTTPSREQUEST_34_020: [The function shall return the request headers saved in this object's connection instance.]
    // Tests_SRS_HTTPSREQUEST_34_017: [The function shall return the body saved in this object's connection instance.]
    @Test
    public void gettersWork(@Mocked final HttpsConnection mockConn, final @Mocked URL mockUrl) throws TransportException
    {
        //arrange
        HttpsRequest request = new HttpsRequest(mockUrl, HttpsMethod.POST, "some body".getBytes(), "some user agent string");

        final String expectedRequestHeaders = "User-Agent: some user agent string\r\n";
        final String expectedMethod = "POST";
        final byte[] expectedBody = "some body".getBytes();
        new NonStrictExpectations()
        {
            {
                new HttpsConnection((URL) any, HttpsMethod.POST);
                result = mockConn;

                Deencapsulation.invoke(mockConn, "getBody");
                result = expectedBody;
            }
        };

        //act
        String actualRequestHeaders = request.getRequestHeaders();
        String actualMethod = request.getHttpMethod();
        byte[] actualBody = request.getBody();
        URL actualURL = request.getRequestUrl();
        request.send();

        //assert
        assertEquals(expectedMethod, actualMethod);
        assertEquals(expectedRequestHeaders, actualRequestHeaders);
        assertEquals(mockUrl, actualURL);
        Assert.assertArrayEquals(expectedBody, actualBody);
    }

    // Tests_SRS_HTTPSREQUEST_34_031: [The function shall save the provided arguments to be used when the http connection is built during the call to send().]
    @Test
    public void constructorSavesFields() throws MalformedURLException, TransportException
    {
        //arrange
        URL url = new URL("http://www.microsoft.com");
        HttpsMethod method = HttpsMethod.POST;
        byte[] body = new byte[2];
        String userAgentString = "user agent";

        //act
        HttpsRequest request = new HttpsRequest(url, method, body, userAgentString);

        //assert
        assertEquals(url, Deencapsulation.getField(request, "url"));
        assertEquals(method, Deencapsulation.getField(request, "method"));
        assertTrue(Arrays.equals(body, (byte[]) Deencapsulation.getField(request, "body")));
        assertEquals(userAgentString, ((Map<String, List<String>>)Deencapsulation.getField(request, "headers")).get("User-Agent").get(0));
    }
}
