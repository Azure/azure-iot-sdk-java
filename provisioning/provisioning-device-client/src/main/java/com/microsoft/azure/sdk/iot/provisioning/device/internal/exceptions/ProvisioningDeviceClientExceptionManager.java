/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;

import java.nio.charset.StandardCharsets;

public class ProvisioningDeviceClientExceptionManager
{
    public static void verifyHttpResponse(HttpResponse httpResponse) throws ProvisioningDeviceHubException
    {
        int responseStatus = httpResponse.getStatus();

        byte[] errorReason = httpResponse.getErrorReason();

        String errorMessage = httpResponse.getStatus() + " : " + new String(errorReason, StandardCharsets.UTF_8);

        switch (responseStatus)
        {
            case 400:
            case 401:
            case 403:
            case 404:
            case 412:
            case 429:
            case 500:
            case 502:
            case 503:
            case 504:
            default:
                if (responseStatus > 300)
                {
                    throw new ProvisioningDeviceHubException(httpResponse.getStatus() + " : " + errorMessage);
                }
                else
                {
                    // no error
                    break;
                }
        }
    }
}
