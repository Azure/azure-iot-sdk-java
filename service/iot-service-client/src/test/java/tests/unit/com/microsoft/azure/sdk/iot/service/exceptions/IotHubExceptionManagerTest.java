/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

@RunWith(JMockit.class)
public class IotHubExceptionManagerTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_001: [The function shall throw IotHubBadFormatException if the Http response status equal 400]
    // Assert
    @Test (expected = IotHubBadFormatException.class)
    public void httpResponseVerification_400() throws IotHubException
    {
        // Arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_002: [The function shall throw IotHubUnathorizedException if the Http response status equal 401]
    // Assert
    @Test (expected = IotHubUnathorizedException.class)
    public void httpResponseVerification_401() throws IotHubException
    {
        // Arrange
        final int status = 401;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_003: [The function shall throw IotHubTooManyDevicesException if the Http response status equal 403]
    // Assert
    @Test (expected = IotHubTooManyDevicesException.class)
    public void httpResponseVerification_403() throws IotHubException
    {
        // Arrange
        final int status = 403;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_004: [The function shall throw IotHubNotFoundException if the Http response status equal 404]
    // Assert
    @Test (expected = IotHubNotFoundException.class)
    public void httpResponseVerification_404() throws IotHubException
    {
        // Arrange
        final int status = 404;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_005: [The function shall throw IotHubPreconditionFailedException if the Http response status equal 412]
    // Assert
    @Test (expected = IotHubPreconditionFailedException.class)
    public void httpResponseVerification_412() throws IotHubException
    {
        // Arrange
        final int status = 412;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_006: [The function shall throw IotHubTooManyRequestsException if the Http response status equal 429]
    // Assert
    @Test (expected = IotHubTooManyRequestsException.class)
    public void httpResponseVerification_429() throws IotHubException
    {
        // Arrange
        final int status = 429;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_007: [The function shall throw IotHubInternalServerErrorException if the Http response status equal 500]
    // Assert
    @Test (expected = IotHubInternalServerErrorException.class)
    public void httpResponseVerification_500() throws IotHubException
    {
        // Arrange
        final int status = 500;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_008: [The function shall throw IotHubBadGatewayException if the Http response status equal 502]
    // Assert
    @Test (expected = IotHubBadGatewayException.class)
    public void httpResponseVerification_502() throws IotHubException
    {
        // Arrange
        final int status = 502;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_009: [The function shall throw IotHubServerBusyException if the Http response status equal 503]
    // Assert
    @Test (expected = IotHubServerBusyException.class)
    public void httpResponseVerification_503() throws IotHubException
    {
        // Arrange
        final int status = 503;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_010: [The function shall throw IotHubGatewayTimeoutException if the Http response status equal 504]
    // Assert
    @Test (expected = IotHubGatewayTimeoutException.class)
    public void httpResponseVerification_504() throws IotHubException
    {
        // Arrange
        final int status = 504;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3, 4, 5 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_011: [The function shall throw IotHubException if the Http response status none of them above and greater than 300 copying the error Http reason to the exception]
    // Assert
    @Test (expected = IotHubException.class)
    public void httpResponseVerification_301_error_reason_ok() throws IotHubException
    {
        // Arrange
        final int status = 301;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 123, 125 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_011: [The function shall throw IotHubException if the Http response status none of them above and greater than 300 copying the error Http reason to the exception]
    // Assert
    @Test (expected = IotHubException.class)
    public void httpResponseVerification_301_error_reason_invalid() throws IotHubException
    {
        // Arrange
        final int status = 301;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 2, 3 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_012: [The function shall return without exception if the response status equal or less than 300]
    @Test
    public void httpResponseVerification_300() throws IotHubException
    {
        // Arrange
        final int status = 300;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = { 123, 125 };
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
        IotHubException iotHubException = new IotHubException();
        IotHubExceptionManager iotHubExceptionManager = new IotHubExceptionManager();
        // Assert
        assertNotEquals(null, iotHubException);
        assertNotEquals(null, iotHubExceptionManager);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification_301_withErrorReason() throws IotHubException
    {
        // Arrange
        final int status = 301;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"ExceptionMessage\":\"This is the error message\"}".getBytes();
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        try
        {
            IotHubExceptionManager.httpResponseVerification(response);
            assert true;
        }
        catch (IotHubException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("This is the error message"));
        }
        IotHubException iotHubException = new IotHubException("error message");
        IotHubExceptionManager iotHubExceptionManager = new IotHubExceptionManager();
        // Assert
        assertNotEquals(null, iotHubException);
        assertNotEquals(null, iotHubExceptionManager);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification_400_withErrorReason() throws IotHubException
    {
        // Arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"ExceptionMessage\":\"This is the error message\"}".getBytes();
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        try
        {
            IotHubExceptionManager.httpResponseVerification(response);
            assert true;
        }
        catch (IotHubBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format! This is the error message"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification_400_withNULLErrorReason() throws IotHubException
    {
        // Arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"ExceptionMessage\":null}".getBytes();
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        try
        {
            IotHubExceptionManager.httpResponseVerification(response);
            assert true;
        }
        catch (IotHubBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format!"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification_400_withEmptyErrorReason() throws IotHubException
    {
        // Arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"ExceptionMessage\":}".getBytes();
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        try
        {
            IotHubExceptionManager.httpResponseVerification(response);
            assert true;
        }
        catch (IotHubBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format!"));
        }
    }

}
