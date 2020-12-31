/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorMessageParser;

/**
 * Provide static function to verify the Device Provisioning Service results and throw appropriate exception.
 */
public class ProvisioningServiceClientExceptionManager
{
    /**
     * Verify response using response status
     *
     * <pre>
     * {@code
     *     ProvisioningServiceClientServiceException [any exception reported in the http response]
     *         |
     *         |
     *         +-->ProvisioningServiceClientBadUsageException [any http response 4xx]
     *         |        |
     *         |        +-->ProvisioningServiceClientBadFormatException [400]
     *         |        +-->ProvisioningServiceClientUnathorizedException [401]
     *         |        +-->ProvisioningServiceClientNotFoundException [404]
     *         |        +-->ProvisioningServiceClientPreconditionFailedException [412]
     *         |        +-->ProvisioningServiceClientTooManyRequestsException [429]
     *         |
     *         +-->ProvisioningServiceClientTransientException [any http response 5xx]
     *         |        |
     *         |        +-->ProvisioningServiceClientInternalServerErrorException [500]
     *         |
     *         +-->ProvisioningServiceClientUnknownException [any other http response >300, but not 4xx or 5xx]
     * }
     * </pre>
     *
     * @param responseStatus is the response status
     * @param errorReason is the error description
     * @throws ProvisioningServiceClientBadFormatException This exception is thrown if the response status equal 400
     * @throws ProvisioningServiceClientUnathorizedException This exception is thrown if the response status equal 401
     * @throws ProvisioningServiceClientNotFoundException This exception is thrown if the response status equal 404
     * @throws ProvisioningServiceClientPreconditionFailedException This exception is thrown if the response status equal 412
     * @throws ProvisioningServiceClientInternalServerErrorException This exception is thrown if the response status equal 500
     * @throws ProvisioningServiceClientServiceException This exception is thrown if the response status none of them above and greater then 300
     */
    public static void httpResponseVerification(int responseStatus, String errorReason)
            throws ProvisioningServiceClientServiceException
    {
        // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_013: [If the httpresponse contains a reason message, the function must print this reason in the error message]
        String errorMessage = ErrorMessageParser.bestErrorMessage(errorReason);

        if((responseStatus >= 400) && (responseStatus < 500))
        {
            // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_015: [The function shall throw ProvisioningServiceClientBadUsageException or one of its child if the response status is in the interval of 400 and 499]
            throwProvisioningServiceClientBadUsageException(responseStatus, errorMessage);
        }
        else if((responseStatus >= 500) && (responseStatus < 600))
        {
            // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_016: [The function shall throw ProvisioningServiceClientTransientException or one of its child if the response status is in the interval of 500 and 599]
            throwProvisioningServiceClientTransientException(responseStatus, errorMessage);
        }
        else if (responseStatus > 300)
        {
            // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_12_011: [The function shall throw ProvisioningServiceClientUnknownException if the Http response status none of them above and greater than 300 copying the error Http reason to the exception]
            if(errorMessage.isEmpty())
            {
                throw new ProvisioningServiceClientUnknownException("Http response unknown error reason " + responseStatus);
            }
            else
            {
                throw new ProvisioningServiceClientUnknownException(errorMessage);
            }
        }
        // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_012: [The function shall return without exception if the response status equal or less than 300]
    }

    private static void throwProvisioningServiceClientBadUsageException(int responseStatus, String errorMessage)
            throws ProvisioningServiceClientBadUsageException
    {
        switch (responseStatus)
        {
            case 400:
                // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_001: [The function shall throw ProvisioningServiceClientBadFormatException if the response status equal 400]
                throw new ProvisioningServiceClientBadFormatException(errorMessage);
            case 401:
                // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_002: [The function shall throw ProvisioningServiceClientUnathorizedException if the response status equal 401]
                throw new ProvisioningServiceClientUnathorizedException(errorMessage);
            case 404:
                // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_004: [The function shall throw ProvisioningServiceClientNotFoundException if the response status equal 404]
                throw new ProvisioningServiceClientNotFoundException(errorMessage);
            case 412:
                // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_005: [The function shall throw ProvisioningServiceClientPreconditionFailedException if the Http response status equal 412]
                throw new ProvisioningServiceClientPreconditionFailedException(errorMessage);
            case 429:
                // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_006: [The function shall throw ProvisioningServiceClientTooManyRequestsException if the response status equal 429]
                throw new ProvisioningServiceClientTooManyRequestsException(errorMessage);
            default:
                if(errorMessage.isEmpty())
                {
                    throw new ProvisioningServiceClientBadUsageException("Http response bad usage " + responseStatus);
                }
                else
                {
                    throw new ProvisioningServiceClientBadUsageException(errorMessage);
                }
        }
    }

    private static void throwProvisioningServiceClientTransientException(int responseStatus, String errorMessage)
            throws ProvisioningServiceClientTransientException
    {
        switch (responseStatus)
        {
            case 500:
                // Codes_SRS_SERVICE_SDK_JAVA_PROVISIONINGSERVICECLIENTEXCEPTIONMANAGER_21_007: [The function shall throw ProvisioningServiceClientInternalServerErrorException if the response status equal 500]
                throw new ProvisioningServiceClientInternalServerErrorException(errorMessage);
            default:
                if(errorMessage.isEmpty())
                {
                    throw new ProvisioningServiceClientTransientException("Http response transient error " + responseStatus);
                }
                else
                {
                    throw new ProvisioningServiceClientTransientException(errorMessage);
                }
        }
    }
}
