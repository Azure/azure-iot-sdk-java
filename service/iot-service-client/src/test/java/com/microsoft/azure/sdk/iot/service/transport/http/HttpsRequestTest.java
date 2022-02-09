/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.http;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** Unit tests for HttpRequest. */
@SuppressWarnings("EmptyMethod")
@RunWith(JMockit.class)
public class HttpsRequestTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_HTTPSREQUEST_12_001: [The function shall open a connection with the given URL as the endpoint.]
    @Test
    public void constructorOpensConnection(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.GET;
        final byte[] body = new byte[0];
        // Act
        new HttpRequest(mockUrl, httpsMethod, body, "");
        // Assert
        new Verifications()
        {
            {
                mockUrl.openConnection();
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_HTTPSREQUEST_12_003: [The function shall use the given HTTPS method (i.e. GET) as the request method.]
    @Test
    public void constructorSetsHttpsMethodCorrectly(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.GET;
        final byte[] body = new byte[0];
        new Expectations()
        {
            {
                mockUrl.openConnection();
                result = mockConn;
                mockConn.setRequestMethod("GET");
            }
        };
        // Act
        new HttpRequest(mockUrl, httpsMethod, body, "");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_HTTPSREQUEST_12_004: [If an IOException occurs in setting up the HTTPS connection, the function shall throw an IOException.]
    // Assert
    @Test(expected = IOException.class)
    public void constructorThrowsIoExceptionIfCannotSetupConnection(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.GET;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockUrl.openConnection();
                result = new IOException();
            }
        };
        // Act
        new HttpRequest(mockUrl, httpsMethod, body, "");
    }

    // Tests_SRS_SERVICE_SDK_JAVA_HTTPSREQUEST_12_007: [If the client cannot connect to the server, the function shall throw an IOException.]
    // Assert
    @Test(expected = IOException.class)
    public void sendThrowsIoExceptionIfCannotConnect(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException, IotHubException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.POST;
        final byte[] body = new byte[0];
        new NonStrictExpectations()
        {
            {
                mockConn.connect();
                result = new IOException();
                mockConn.getHeaderFields();
                result = new IOException();
                mockConn.getResponseCode();
                result = new IOException();
                mockConn.getInputStream();
                result = new IOException();
                mockConn.getErrorStream();
                result = new IOException();
            }
        };
        HttpRequest request = new HttpRequest(mockUrl, httpsMethod, body, "");
        // Act
        request.send();
    }

    // Tests_SRS_SERVICE_SDK_JAVA_HTTPSREQUEST_12_009: [The function shall set the header field with the given name to the given value.]
    @Test
    public void setHeaderFieldSetsHeaderField(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.POST;
        final byte[] body = new byte[0];
        final String field = "test-field";
        final String value = "test-value";
        HttpRequest request = new HttpRequest(mockUrl, httpsMethod, body, "");
        // Act
        request.setHeaderField(field, value);
        // Assert
        new Verifications()
        {
            {
                mockConn.setRequestProperty(field, value);
            }
        };
    }

    // Tests_SRS_SERVICE_SDK_JAVA_HTTPSREQUEST_12_010: [The function shall set the read timeout for the request to the given value.]
    @Test
    public void setReadTimeoutSetsReadTimeout(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.POST;
        final byte[] body = new byte[0];
        final int readTimeout = 1;
        HttpRequest request = new HttpRequest(mockUrl, httpsMethod, body, "");
        // Act
        request.setReadTimeoutSeconds(readTimeout);
        // Assert
        new Verifications()
        {
            {
                mockConn.setReadTimeout(readTimeout * 1000);
            }
        };
    }

    @Test
    public void setConnectTimeoutSetsConnectTimeout(@Mocked final HttpsURLConnection mockConn, final @Mocked URL mockUrl) throws IOException
    {
        // Arrange
        final HttpMethod httpsMethod = HttpMethod.POST;
        final byte[] body = new byte[0];
        final int readTimeout = 1;
        HttpRequest request = new HttpRequest(mockUrl, httpsMethod, body, "");
        // Act
        request.setConnectTimeoutSeconds(readTimeout);
        // Assert
        new Verifications()
        {
            {
                mockConn.setConnectTimeout(readTimeout * 1000);
            }
        };
    }
}
