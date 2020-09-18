// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsConnection;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.HttpProxySocketFactory;
import mockit.*;
import org.junit.Assert;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/** Unit tests for HttpsConnection.
 * Lines: 89%
 * Methods: 93%
 */
public class HttpsConnectionTest
{
    @Mocked
    URL mockUrl;
    @Mocked
    HttpsURLConnection mockUrlConn;
    @Mocked
    HttpURLConnection mockHttpURLConnection;
    @Mocked
    ProxySettings mockProxySettings;

    // Tests_SRS_HTTPSCONNECTION_11_001: [The constructor shall open a connection to the given URL.]
    @Test
    public void constructorOpensHttpsConnection() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
            }
        };

        new HttpsConnection(mockUrl, httpsMethod);

        new Verifications()
        {
            {
                mockUrl.openConnection();
            }
        };
    }

    @Test
    public void constructorOpensHttpConnection() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "http";
                mockUrl.openConnection();
                result = mockUrlConn;
            }
        };

        new HttpsConnection(mockUrl, httpsMethod, null, false);

        new Verifications()
        {
            {
                mockUrl.openConnection();
            }
        };
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfHttpsProtocolIsNotExpected() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
            }
        };

        new HttpsConnection(mockUrl, httpsMethod, null, false);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsIfHttpProtocolIsNotExpected() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "http";
                mockUrl.openConnection();
                result = mockUrlConn;
            }
        };

        new HttpsConnection(mockUrl, httpsMethod, null, true);
    }

    // Tests_SRS_HTTPSCONNECTION_11_002: [The constructor shall throw a TransportException if the connection was unable to be opened.]
    @Test(expected = TransportException.class)
    public void constructorThrowsIoExceptionIfCannotOpenConnection() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = new IOException();
            }
        };

        new HttpsConnection(mockUrl, httpsMethod);
    }

    // Tests_SRS_HTTPSCONNECTION_11_021: [The constructor shall set the HTTPS method to the given method.]
    @Test
    public void constructorSetsRequestMethod() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };

        new HttpsConnection(mockUrl, httpsMethod);

        new Verifications()
        {
            {
                mockUrl.openConnection();
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_022: [If the URI given does not use the HTTPS or HTTP protocol, the constructor shall throw an IllegalArgumentException.]
    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNonHttpsUrl() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "unix";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };

        new HttpsConnection(mockUrl, httpsMethod);
    }

    // Tests_SRS_HTTPSCONNECTION_11_003: [The function shall send a request to the URL given in the constructor.]
    @Test
    public void connectUsesCorrectUrl() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.connect();

        new Verifications()
        {
            {
                mockUrl.openConnection().connect();
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_004: [The function shall stream the request body, if present, through the connection.]
    // Tests_SRS_HTTPSCONNECTION_11_009: [The function shall save the body to be sent with the request.]
    @Test
    public void connectStreamsRequestBody() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        byte[] body = { 1, 2, 3 };
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.writeOutput(body);
        body[0] = 5;
        conn.connect();

        final byte[] expectedBody = { 1, 2, 3 };
        new Verifications()
        {
            {
                mockUrl.openConnection().getOutputStream().write(expectedBody);
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_005: [The function shall throw a TransportException if the connection could not be established, or the server responded with a bad status code.]
    @Test(expected = TransportException.class)
    public void connectThrowsIoExceptionIfCannotConnect() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.connect();
                result = new IOException();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.connect();
    }

    // Tests_SRS_HTTPSCONNECTION_11_006: [The function shall set the request method.]
    @Test
    public void setRequestMethodSetsRequestMethod() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.PUT;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.setRequestMethod(httpsMethod);

        new Verifications()
        {
            {
                mockUrlConn.setRequestMethod(httpsMethod.name());
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_007: [The function shall throw an IllegalArgumentException if the request currently has a non-empty body and the new method is not a POST or a PUT.]
    @Test(expected = IllegalArgumentException.class)
    public void setRequestMethodRejectsNonPostOrPutIfHasBody() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final HttpsMethod illegalHttpsMethod = HttpsMethod.DELETE;
        final byte[] body = { 1, 2, 3 };
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                returns(httpsMethod.name(), illegalHttpsMethod.name());
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.writeOutput(body);
        conn.setRequestMethod(illegalHttpsMethod);
    }

    // Tests_SRS_HTTPSCONNECTION_11_008: [The function shall set the given request header field.]
    @Test
    public void setRequestHeaderSetsRequestHeader() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String field = "test-field";
        final String value = "test-value";
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.setRequestHeader(field, value);

        new Verifications()
        {
            {
                mockUrl.openConnection().setRequestProperty(field, value);
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_023: [The function shall set the read timeout to the given value.]
    @Test
    public void setReadTimeoutSetsRequestTimeout() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String field = "test-field";
        final String value = "test-value";
        final int timeout = 1;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.setReadTimeout(timeout);

        final int expectedTimeout = timeout;
        new Verifications()
        {
            {
                mockUrl.openConnection().setReadTimeout(expectedTimeout);
            }
        };
    }

    @Test
    public void setConnectTimeoutSetsConnectTimeout() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String field = "test-field";
        final String value = "test-value";
        final int timeout = 1;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.setConnectTimeout(timeout);

        final int expectedTimeout = timeout;
        new Verifications()
        {
            {
                mockUrl.openConnection().setConnectTimeout(expectedTimeout);
            }
        };
    }

    //Tests_SRS_HTTPSCONNECTION_25_024: [**The function shall set the the SSL context with the given value.**]**
    @Test
    public void setSSLContextSetsContext(@Mocked final SSLContext mockedContext) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String field = "test-field";
        final String value = "test-value";
        final int timeout = 1;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        final HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        Deencapsulation.invoke(conn, "setSSLContext", mockedContext);

        new Verifications()
        {
            {
                mockUrlConn.setSSLSocketFactory(mockedContext.getSocketFactory());
                times = 1;
            }
        };
    }

    @Test
    public void setSSLContextSetsContextWithProxy(@Mocked final SSLContext mockedContext, @Mocked final SSLSocketFactory mockedSocketFactory, @Mocked final HttpProxySocketFactory mockedHttpProxySocketFactory) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockedContext.getSocketFactory();
                result = mockedSocketFactory;
                new HttpProxySocketFactory(mockedSocketFactory, mockProxySettings);
                result = mockedHttpProxySocketFactory;
            }
        };
        final HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod, mockProxySettings);

        Deencapsulation.invoke(conn, "setSSLContext", mockedContext);

        new Verifications()
        {
            {
                mockUrlConn.setSSLSocketFactory(mockedHttpProxySocketFactory);
                times = 1;
            }
        };
    }

    //Tests_SRS_HTTPSCONNECTION_34_026: [If this object uses HTTP, this function shall throw an UnsupportedOperationException.]
    @Test (expected = IllegalArgumentException.class)
    public void HttpsConnectionConstructorThrowsIfHttp(@Mocked final SSLContext mockedContext) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String field = "test-field";
        final String value = "test-value";
        final int timeout = 1;
        new Expectations()
        {
            {
                mockUrl.getProtocol();
                result = "http";
            }
        };
        final HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
    }


    //Tests_SRS_HTTPSCONNECTION_25_025: [The function shall throw IllegalArgumentException if the context is null value.**]**
    @Test (expected = IllegalArgumentException.class)
    public void setSSLContextThrowsOnNullContext(@Mocked final SSLContext mockedContext) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        final String field = "test-field";
        final String value = "test-value";
        final int timeout = 1;
        new Expectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
            }
        };
        final HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        Deencapsulation.invoke(conn, "setSSLContext", SSLContext.class);
    }

    // Tests_SRS_HTTPSCONNECTION_11_010: [The function shall throw an IllegalArgumentException if the request does not currently use method POST or PUT and the body is non-empty.]
    @Test(expected = IllegalArgumentException.class)
    public void writeOutputFailsWhenMethodIsNotPostOrPut() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] body = { 1, 2 };
        new Expectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.writeOutput(body);
    }

    // Tests_SRS_HTTPSCONNECTION_11_010: [The function shall throw an IllegalArgumentException if the request does not currently use method POST or PUT and the body is non-empty.]
    @Test
    public void writeOutputDoesNotFailWhenBodyIsEmpty() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        conn.writeOutput(body);
    }

    // Tests_SRS_HTTPSCONNECTION_11_011: [The function shall read from the input stream (response stream) and return the response.]
    @Test
    public void readInputCompletelyReadsInputStream(
            @Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getInputStream();
                result = mockIs;
                mockIs.read();
                returns(1, 2, 3, -1);
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        byte[] testResponse = conn.readInput();

        byte[] expectedResponse = { 1, 2, 3 };
        assertThat(testResponse, is(expectedResponse));
    }

    // Tests_SRS_HTTPSCONNECTION_11_019: [The function shall close the input stream after it has been completely read.]
    @Test
    public void readInputClosesStreamIfExceptionWhileReading(
            @Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getInputStream();
                result = mockIs;
                mockIs.read();
                result = new TransportException("This is a test exception");
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        //act
        try
        {
            byte[] testResponse = conn.readInput();
        }
        catch (TransportException e)
        {
            //expected exception, but not testing for it, so it can be ignored
        }

        new Verifications()
        {
            {
                mockIs.close();
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_012: [The function shall throw a TransportException if the input stream could not be accessed.]
    @Test(expected = TransportException.class)
    public void readInputFailsIfCannotAccessInputStream() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getInputStream();
                result = new IOException();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        conn.readInput();
    }

    // Tests_SRS_HTTPSCONNECTION_34_027: [If an UnknownHostException or a NoRouteToHostException is encountered, the thrown TransportException shall be retryable.]
    @Test
    public void readInputFailsIfCannotAccessInputStreamAndIsNoRouteToHost() throws IOException, TransportException
    {
        boolean exceptionCaught = false;
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getInputStream();
                result = new NoRouteToHostException();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        try
        {
            conn.readInput();
        }
        catch (TransportException e)
        {
            exceptionCaught = true;
            if (!e.isRetryable())
            {
                fail("NoRouteToHostException should yield retriable TransportException");
            }
        }

        assertTrue(exceptionCaught);
    }

    // Tests_SRS_HTTPSCONNECTION_11_019: [The function shall close the input stream after it has been completely read.]
    @Test
    public void readInputClosesInputStream(@Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getInputStream();
                result = mockIs;
                mockIs.read();
                result = -1;
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        conn.readInput();

        new Verifications()
        {
            {
                mockIs.close();
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_013: [The function shall read from the error stream and return the response.]
    @Test
    public void readErrorCompletelyReadsErrorStream(
            @Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getErrorStream();
                result = mockIs;
                mockIs.read();
                returns(1, 2, 3, -1);
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        byte[] testError = conn.readError();

        byte[] expectedError = { 1, 2, 3 };
        assertThat(testError, is(expectedError));
    }

    // Tests_SRS_HTTPSCONNECTION_11_020: [The function shall close the error stream after it has been completely read.]
    @Test
    public void readErrorAlwaysClosesStream(
            @Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getErrorStream();
                result = mockIs;
                mockIs.read();
                result = new TransportException("This is a test exception");
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        //act
        try
        {
            byte[] testError = conn.readError();
        }
        catch (TransportException e)
        {
            //expected exception, but not testing for it, so it can be ignored
        }

        new Verifications()
        {
            {
                mockIs.close();
                times = 1;
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_013: [The function shall read from the error stream and return the response.]
    @Test
    public void readErrorReturnsEmptyErrorReasonIfNoErrorReason() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getErrorStream();
                result = null;
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        byte[] testError = conn.readError();

        byte[] expectedError = {};
        assertThat(testError, is(expectedError));
    }

    // Tests_SRS_HTTPSCONNECTION_11_014: [The function shall throw a TransportException if the error stream could not be accessed.]
    @Test(expected = TransportException.class)
    public void readErrorFailsIfCannotAccessErrorStream() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getErrorStream();
                result = new IOException();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        conn.readError();
    }

    // Tests_SRS_HTTPSCONNECTION_11_020: [The function shall close the error stream after it has been completely read.]
    @Test
    public void readErrorClosesErrorStream(@Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getErrorStream();
                result = mockIs;
                mockIs.read();
                result = -1;
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        conn.readError();

        new Verifications()
        {
            {
                mockIs.close();
            }
        };
    }

    // Tests_SRS_HTTPSCONNECTION_11_015: [The function shall return the response status code.]
    @Test
    public void getResponseStatusReturnsResponseStatus(
            @Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final int status = 204;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getResponseCode();
                result = status;
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        int testStatus = conn.getResponseStatus();

        int expectedStatus = status;
        assertThat(testStatus, is(expectedStatus));
    }

    // Tests_SRS_HTTPSCONNECTION_11_016: [The function shall throw a TransportException if no response was received.]
    @Test(expected = TransportException.class)
    public void getResponseStatusFailsIfDidNotReceiveResponse(
            @Mocked final InputStream mockIs) throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getResponseCode();
                result = new IOException();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        conn.getResponseStatus();
    }

    // Tests_SRS_HTTPSCONNECTION_11_017: [The function shall return a mapping of header field names to the values associated with the header field name.]
    @Test
    public void getResponseHeadersReturnsResponseHeaders() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        final String field0 = "test-field0";
        final String value0 = "test-value0";
        final String field1 = "test-field1";
        final String value1 = "test-value1";
        final List<String> values0 = new LinkedList<>();
        values0.add(value0);
        final List<String> values1 = new LinkedList<>();
        values1.add(value1);
        final Map<String, List<String>> responseHeaders = new HashMap<>();
        responseHeaders.put(field0, values0);
        responseHeaders.put(field1, values1);
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getHeaderFields();
                result = responseHeaders;
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        Map<String, List<String>> testResponseHeaders =
                conn.getResponseHeaders();

        final Map<String, List<String>> expectedResponseHeaders =
                responseHeaders;
        assertThat(testResponseHeaders.size(),
                is(expectedResponseHeaders.size()));
        // the list of values for each field is of size 1, so the lists
        // can be directly compared.
        assertThat(testResponseHeaders.get(field0),
                is(expectedResponseHeaders.get(field0)));
        assertThat(testResponseHeaders.get(field1),
                is(expectedResponseHeaders.get(field1)));
    }

    // Tests_SRS_HTTPSCONNECTION_11_018: [The function shall throw an IOException if no response was received.]
    @Test(expected = IOException.class)
    public void getResponseHeadersFailsIfDidNotReceiveResponse() throws IOException, TransportException
    {
        final HttpsMethod httpsMethod = HttpsMethod.GET;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getHeaderFields();
                result = new IOException();
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);
        conn.connect();

        conn.getResponseHeaders();
    }

    //Tests_SRS_HTTPSCONNECTION_34_031: [The function shall return the saved body.]
    @Test
    public void getBodyReturnsSavedBody() throws IOException, TransportException
    {
        //arrange
        final HttpsMethod httpsMethod = HttpsMethod.POST;
        new NonStrictExpectations()
        {
            {
                mockUrl.getProtocol();
                result = "https";
                mockUrl.openConnection();
                result = mockUrlConn;
                mockUrlConn.getRequestMethod();
                result = httpsMethod.name();
                mockUrlConn.getURL();
                result = mockUrl;
            }
        };
        HttpsConnection conn = new HttpsConnection(mockUrl, httpsMethod);

        byte[] expectedBody = "test".getBytes();
        conn.writeOutput(expectedBody);

        //act
        byte[] actual = Deencapsulation.invoke(conn, "getBody");

        //assert
        Assert.assertArrayEquals(expectedBody, actual);
    }
}
