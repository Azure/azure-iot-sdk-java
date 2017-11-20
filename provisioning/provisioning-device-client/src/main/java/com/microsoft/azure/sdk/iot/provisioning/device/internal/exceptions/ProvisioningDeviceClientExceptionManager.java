/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorMessageParser;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;

import java.nio.charset.StandardCharsets;

public class ProvisioningDeviceClientExceptionManager
{
    public static void verifyHttpResponse(HttpResponse httpResponse) throws ProvisioningDeviceHubException
    {
        int responseStatus = httpResponse.getStatus();

        String errorMessage = ErrorMessageParser.bestErrorMessage(new String(httpResponse.getErrorReason(), StandardCharsets.UTF_8));

        switch (responseStatus)
        {
            case 400:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 401:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 403:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 404:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 412:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 429:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 500:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 502:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 503:
                throw new ProvisioningDeviceHubException(errorMessage);
            case 504:
                throw new ProvisioningDeviceHubException(errorMessage);
            default:
                if (responseStatus > 300)
                {
                    throw new ProvisioningDeviceHubException(errorMessage);
                }
                else
                {
                    // good case move forward
                    break;
                }
        }
    }
}
