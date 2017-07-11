/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorMessageParser;
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
     * @throws IotHubUnathorizedException This exception is thrown if the response status equal 401
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
            IotHubUnathorizedException, 
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

        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
        String errorMessage = ErrorMessageParser.bestErrorMessage(new String(httpResponse.getErrorReason(), StandardCharsets.UTF_8));

        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_001: [The function shall throw IotHubBadFormatException if the Http response status equal 400]
        if (400 == responseStatus)
        {
            throw new IotHubBadFormatException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_002: [The function shall throw IotHubUnathorizedException if the Http response status equal 401]
        else if (401 == responseStatus)
        {
            throw new IotHubUnathorizedException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_003: [The function shall throw IotHubTooManyDevicesException if the Http response status equal 403]
        else if (403 == responseStatus)
        {
            throw new IotHubTooManyDevicesException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_004: [The function shall throw IotHubNotFoundException if the Http response status equal 404]
        else if (404 == responseStatus)
        {
            throw new IotHubNotFoundException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_005: [The function shall throw IotHubPreconditionFailedException if the Http response status equal 412]
        else if (412 == responseStatus)
        {
            throw new IotHubPreconditionFailedException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_006: [The function shall throw IotHubTooManyRequestsException if the Http response status equal 429]
        else if (429 == responseStatus)
        {
            throw new IotHubTooManyRequestsException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_007: [The function shall throw IotHubInternalServerErrorException if the Http response status equal 500]
        else if (500 == responseStatus)
        {
            throw new IotHubInternalServerErrorException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_008: [The function shall throw IotHubBadGatewayException if the Http response status equal 502]
        else if (502 == responseStatus)
        {
            throw new IotHubBadGatewayException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_009: [The function shall throw IotHubServerBusyException if the Http response status equal 503]
        else if (503 == responseStatus)
        {
            throw new IotHubServerBusyException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_21_010: [The function shall throw IotHubGatewayTimeoutException if the Http response status equal 504]
        else if (504 == responseStatus)
        {
            throw new IotHubGatewayTimeoutException(errorMessage);
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_011: [The function shall throw IotHubException if the Http response status none of them above and greater than 300 copying the error Http reason to the exception]
        else if (responseStatus > 300)
        {
            if(errorMessage.isEmpty())
            {
                throw new IotHubException("Unknown error reason");
            }
            else
            {
                throw new IotHubException(errorMessage);
            }
        }
        // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBEXCEPTIONMANAGER_12_012: [The function shall return without exception if the response status equal or less than 300]
    }
}
