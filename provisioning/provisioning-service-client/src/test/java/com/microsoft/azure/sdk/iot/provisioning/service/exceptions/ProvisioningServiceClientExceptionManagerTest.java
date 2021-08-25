/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;

/**
 * Unit test for ProvisioningServiceClient Exception Manager
 * 100% methods, 100% lines covered
 */
@RunWith(JMockit.class)
public class ProvisioningServiceClientExceptionManagerTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_001: [The function shall throw ProvisioningServiceClientBadFormatException if the response status equal 400]
    // Assert
    @Test (expected = ProvisioningServiceClientBadFormatException.class)
    public void httpResponseVerification400() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";
        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_002: [The function shall throw ProvisioningServiceClientUnathorizedException if the response status equal 401]
    // Assert
    @Test (expected = ProvisioningServiceClientUnathorizedException.class)
    public void httpResponseVerification401() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 401;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";
        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_004: [The function shall throw ProvisioningServiceClientNotFoundException if the response status equal 404]
    // Assert
    @Test (expected = ProvisioningServiceClientNotFoundException.class)
    public void httpResponseVerification404() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 404;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_005: [The function shall throw ProvisioningServiceClientPreconditionFailedException if the response status equal 412]
    // Assert
    @Test (expected = ProvisioningServiceClientPreconditionFailedException.class)
    public void httpResponseVerification412() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 412;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_006: [The function shall throw ProvisioningServiceClientTooManyRequestsException if the response status equal 429]
    // Assert
    @Test (expected = ProvisioningServiceClientTooManyRequestsException.class)
    public void httpResponseVerification429() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 429;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_007: [The function shall throw ProvisioningServiceClientInternalServerErrorException if the response status equal 500]
    // Assert
    @Test (expected = ProvisioningServiceClientInternalServerErrorException.class)
    public void httpResponseVerification500() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 500;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_011: [The function shall throw ProvisioningServiceClientUnknownException if the response status none of them above and greater than 300 copying the error reason to the exception]
    // Assert
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void httpResponseVerification301ErrorReasonOk() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 301;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_011: [The function shall throw ProvisioningServiceClientUnknownException if the response status none of them above and greater than 300 copying the error reason to the exception]
    // Assert
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void httpResponseVerification301ErrorReasonNull() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 301;
        final String errorReason = null;

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_011: [The function shall throw ProvisioningServiceClientUnknownException if the response status none of them above and greater than 300 copying the error reason to the exception]
    // Assert
    @Test (expected = ProvisioningServiceClientServiceException.class)
    public void httpResponseVerification301ErrorInvalidEmptyReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 301;
        final String errorReason = "";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_012: [The function shall return without exception if the response status equal or less than 300]
    @Test
    public void httpResponseVerification300() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 300;
        final String errorReason = "{\"ExceptionMessage\":\"This is a valid message\"}";

        // Act
        ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
        ProvisioningServiceClientException dpsException = new ProvisioningServiceClientException();
        ProvisioningServiceClientExceptionManager dpsExceptionManager = new ProvisioningServiceClientExceptionManager();
        // Assert
        assertNotEquals(null, dpsException);
        assertNotEquals(null, dpsExceptionManager);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification301WithErrorReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 301;
        final String errorReason = "{\"ExceptionMessage\":\"This is the error message\"}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientUnknownException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("{\"ExceptionMessage\":\"This is the error message\"}"));
        }
        ProvisioningServiceClientUnknownException dpsException = new ProvisioningServiceClientUnknownException("error message");
        ProvisioningServiceClientExceptionManager dpsExceptionManager = new ProvisioningServiceClientExceptionManager();
        // Assert
        assertNotEquals(null, dpsException);
        assertNotEquals(null, dpsExceptionManager);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithErrorReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = "{\"ExceptionMessage\":\"This is the error message\"}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format! {\"ExceptionMessage\":\"This is the error message\"}"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithNULLErrorReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = "{\"ExceptionMessage\":null}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format! {\"ExceptionMessage\":null}"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithEmptyErrorReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = "{\"ExceptionMessage\":}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format! {\"ExceptionMessage\":}"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithMessageAndException() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = "{\"Message\":\"ErrorCode:ProvisioningServiceClientUnauthorizedAccess;Unauthorized\",\"ExceptionMessage\":\"Tracking ID:(tracking id)-TimeStamp:12/14/2016 03:15:17\"}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format! ErrorCode:ProvisioningServiceClientUnauthorizedAccess;Unauthorized Tracking ID:(tracking id)-TimeStamp:12/14/2016 03:15:17"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithInnerMessageAndException() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = "{\"Message\":\"ErrorCode:ArgumentInvalid;Error: BadRequest {\\\"Message\\\":\\\"ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag.\\\",\\\"ExceptionMessage\\\":\\\"Tracking ID:1234-TimeStamp:06/26/2017 20:56:33\\\"}\",\"ExceptionMessage\":\"Tracking ID:5678-G:10-TimeStamp:06/26/2017 20:56:33\"}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format! ErrorCode:ArgumentInvalid;Missing or invalid etag for job type ScheduleUpdateTwin. ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag. Tracking ID:1234-TimeStamp:06/26/2017 20:56:33"));
        }
    }

    // Tests_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the errorReason contains a reason message, the function must print this reason in the error message]
    // Assert
    @Test
    public void httpResponseVerification400WithoutMessage() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 400;
        final String errorReason = null;

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadFormatException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("Bad message format!"));
        }
    }

    // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_015: [The function shall throw ProvisioningServiceClientBadUsageException or one of its child if the response status is in the interval of 400 and 499]
    // Assert
    @Test
    public void httpResponseVerificationUnknownBadUsageReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 499;
        final String errorReason = "{\"ExceptionMessage\":\"This is the error message\"}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientBadUsageException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("{\"ExceptionMessage\":\"This is the error message\"}"));
        }
    }

    // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_016: [The function shall throw ProvisioningServiceClientTransientException or one of its child if the response status is in the interval of 500 and 599]
    // Assert
    @Test
    public void httpResponseVerificationUnknownTransientReason() throws ProvisioningServiceClientServiceException
    {
        // Arrange
        final int status = 599;
        final String errorReason = "{\"ExceptionMessage\":\"This is the error message\"}";

        // Act
        try
        {
            ProvisioningServiceClientExceptionManager.httpResponseVerification(status, errorReason);
            assert true;
        }
        catch (ProvisioningServiceClientTransientException expected)
        {
            // Expected throw.
            assertThat(expected.getMessage(), is("{\"ExceptionMessage\":\"This is the error message\"}"));
        }
    }

}
