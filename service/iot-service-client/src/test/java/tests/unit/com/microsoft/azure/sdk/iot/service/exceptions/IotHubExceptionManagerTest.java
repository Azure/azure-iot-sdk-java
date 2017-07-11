/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.service.exceptions.*;
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

/**
 * Unit test for IoTHub Exception Manager
 * 100% methods, 100% lines covered
 */
@RunWith(JMockit.class)
public class IotHubExceptionManagerTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_001: [The function shall throw IotHubBadFormatException if the Http response status equal 400]
    // Assert
    @Test (expected = IotHubBadFormatException.class)
    public void httpResponseVerification400() throws IotHubException
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
    public void httpResponseVerification401() throws IotHubException
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
    public void httpResponseVerification403() throws IotHubException
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
    public void httpResponseVerification404() throws IotHubException
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
    public void httpResponseVerification412() throws IotHubException
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
    public void httpResponseVerification429() throws IotHubException
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
    public void httpResponseVerification500() throws IotHubException
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
    public void httpResponseVerification502() throws IotHubException
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
    public void httpResponseVerification503() throws IotHubException
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
    public void httpResponseVerification504() throws IotHubException
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
    public void httpResponseVerification301ErrorReasonOk() throws IotHubException
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
    public void httpResponseVerification301ErrorReasonInvalid() throws IotHubException
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

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_011: [The function shall throw IotHubException if the Http response status none of them above and greater than 300 copying the error Http reason to the exception]
    // Assert
    @Test (expected = IotHubException.class)
    public void httpResponseVerification301ErrorInvalidEmptyReason() throws IotHubException
    {
        // Arrange
        final int status = 301;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = {};
        HttpResponse response = new HttpResponse(status, body, headerFields, errorReason);
        // Act
        IotHubExceptionManager.httpResponseVerification(response);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_012: [The function shall return without exception if the response status equal or less than 300]
    @Test
    public void httpResponseVerification300() throws IotHubException
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
    public void httpResponseVerification301WithErrorReason() throws IotHubException
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
            assertThat(expected.getMessage(), is("{\"ExceptionMessage\":\"This is the error message\"}"));
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
    public void httpResponseVerification400WithErrorReason() throws IotHubException
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
            assertThat(expected.getMessage(), is("Bad message format! {\"ExceptionMessage\":\"This is the error message\"}"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithNULLErrorReason() throws IotHubException
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
            assertThat(expected.getMessage(), is("Bad message format! {\"ExceptionMessage\":null}"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithEmptyErrorReason() throws IotHubException
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
            assertThat(expected.getMessage(), is("Bad message format! {\"ExceptionMessage\":}"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithMessageAndException() throws IotHubException
    {
        // Arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"Message\":\"ErrorCode:IotHubUnauthorizedAccess;Unauthorized\",\"ExceptionMessage\":\"Tracking ID:(tracking id)-TimeStamp:12/14/2016 03:15:17\"}".getBytes();
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
            assertThat(expected.getMessage(), is("Bad message format! ErrorCode:IotHubUnauthorizedAccess;Unauthorized Tracking ID:(tracking id)-TimeStamp:12/14/2016 03:15:17"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithInnerMessageAndException() throws IotHubException
    {
        // Arrange
        final int status = 400;
        final byte[] body = { 1 };
        final Map<String, List<String>> headerFields = new HashMap<>();
        final byte[] errorReason = "{\"Message\":\"ErrorCode:ArgumentInvalid;Error: BadRequest {\\\"Message\\\":\\\"ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag.\\\",\\\"ExceptionMessage\\\":\\\"Tracking ID:1234-TimeStamp:06/26/2017 20:56:33\\\"}\",\"ExceptionMessage\":\"Tracking ID:5678-G:10-TimeStamp:06/26/2017 20:56:33\"}".getBytes();
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
            assertThat(expected.getMessage(), is("Bad message format! ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag. Tracking ID:1234-TimeStamp:06/26/2017 20:56:33"));
        }
    }
}
