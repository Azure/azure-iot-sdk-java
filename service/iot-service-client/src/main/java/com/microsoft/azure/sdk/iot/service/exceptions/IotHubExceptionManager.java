/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.nio.charset.StandardCharsets;

/**
 * Provide static function to verify results and throw appropriate exception.
 */
public class IotHubExceptionManager
{
    /**
     * Verify Http response using response status
     *
     * @param httpResponse Http response object to verify
     * @throws IotHubBadFormatException This exception is thrown if the response status equal 400
     * @throws IotHubUnauthorizedException This exception is thrown if the response status equal 401
     * @throws IotHubTooManyDevicesException This exception is thrown if the response status equal 403
     * @throws IotHubNotFoundException This exception is thrown if the response status equal 404
     * @throws IotHubPreconditionFailedException This exception is thrown if the response status equal 412
     * @throws IotHubTooManyRequestsException This exception is thrown if the response status equal 429
     * @throws IotHubInternalServerErrorException This exception is thrown if the response status equal 500
     * @throws IotHubBadGatewayException This exception is thrown if the response status equal 502
     * @throws IotHubServerBusyException This exception is thrown if the response status equal 503
     * @throws IotHubGatewayTimeoutException This exception is thrown if the response status equal 504
     * @throws IotHubException This exception is thrown if the response status none of them above and greater then 300
     */
    public static void httpResponseVerification(HttpResponse httpResponse)
            throws 
            IotHubBadFormatException,
            IotHubUnauthorizedException,
            IotHubTooManyDevicesException,
            IotHubPreconditionFailedException,
            IotHubTooManyRequestsException,
            IotHubInternalServerErrorException,
            IotHubServerBusyException,
            IotHubBadGatewayException,
            IotHubNotFoundException,
            IotHubGatewayTimeoutException,
            IotHubException
    {
        int responseStatus = httpResponse.getStatus();

        String errorMessage = ErrorMessageParser.bestErrorMessage(new String(httpResponse.getErrorReason(), StandardCharsets.UTF_8));
        int errorCode = ErrorMessageParser.bestErrorCode(errorMessage);

        if (errorCode == ErrorMessageParser.getDefaultErrorCode())
        {
            //Some error messages contain a full error code such as 404001, but others do not.
            //if no error code was found in body of error message, default to the http response status code.
            errorCode = responseStatus;
        }

        ErrorCodeDescription errorCodeDescription = ErrorCodeDescription.Parse(errorCode);

        if (400 == responseStatus)
        {
            throw new IotHubBadFormatException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (401 == responseStatus)
        {
            throw new IotHubUnauthorizedException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (403 == responseStatus)
        {
            throw new IotHubTooManyDevicesException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (404 == responseStatus)
        {
            throw new IotHubNotFoundException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (409 == responseStatus)
        {
            throw new IotHubConflictException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (412 == responseStatus)
        {
            throw new IotHubPreconditionFailedException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (429 == responseStatus)
        {
            throw new IotHubTooManyRequestsException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (500 == responseStatus)
        {
            throw new IotHubInternalServerErrorException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (502 == responseStatus)
        {
            throw new IotHubBadGatewayException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (503 == responseStatus)
        {
            throw new IotHubServerBusyException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (504 == responseStatus)
        {
            throw new IotHubGatewayTimeoutException(errorMessage, errorCode, errorCodeDescription);
        }
        else if (responseStatus > 300)
        {
            if (errorMessage.isEmpty())
            {
                throw new IotHubException("Unknown error reason");
            }
            else
            {
                throw new IotHubException(errorMessage, errorCode, errorCodeDescription);
            }
        }
    }

    /**
     * Return a new exception instance that best matches the given HTTP status code and description
     * @param responseStatus The HTTP status code (404, 500, etc.)
     * @param description The HTTP response body
     * @return a new exception instance that best matches the given HTTP status code
     */
    public static IotHubException mapException(int responseStatus, String description)
    {
        ErrorCodeDescription errorCodeDescription = ErrorCodeDescription.Parse(responseStatus);

        if (400 == responseStatus)
        {
            return new IotHubBadFormatException(description, responseStatus, errorCodeDescription);
        }
        else if (401 == responseStatus)
        {
            return new IotHubUnauthorizedException(description, responseStatus, errorCodeDescription);
        }
        else if (403 == responseStatus)
        {
            return new IotHubTooManyDevicesException(description, responseStatus, errorCodeDescription);
        }
        else if (404 == responseStatus)
        {
            return new IotHubNotFoundException(description, responseStatus, errorCodeDescription);
        }
        else if (409 == responseStatus)
        {
            return new IotHubConflictException(description, responseStatus, errorCodeDescription);
        }
        else if (412 == responseStatus)
        {
            return new IotHubPreconditionFailedException(description, responseStatus, errorCodeDescription);
        }
        else if (429 == responseStatus)
        {
            return new IotHubTooManyRequestsException(description, responseStatus, errorCodeDescription);
        }
        else if (500 == responseStatus)
        {
            return new IotHubInternalServerErrorException(description, responseStatus, errorCodeDescription);
        }
        else if (502 == responseStatus)
        {
            return new IotHubBadGatewayException(description, responseStatus, errorCodeDescription);
        }
        else if (503 == responseStatus)
        {
            return new IotHubServerBusyException(description, responseStatus, errorCodeDescription);
        }
        else if (504 == responseStatus)
        {
            return new IotHubGatewayTimeoutException(description, responseStatus, errorCodeDescription);
        }
        else
        {
            return new IotHubException(description, responseStatus, errorCodeDescription);
        }
    }
}
