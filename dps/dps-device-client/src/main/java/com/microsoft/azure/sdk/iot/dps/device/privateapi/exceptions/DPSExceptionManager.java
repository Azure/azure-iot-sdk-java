/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions;

import com.microsoft.azure.sdk.iot.deps.serializer.ErrorMessageParser;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;

import java.nio.charset.StandardCharsets;

public class DPSExceptionManager
{
    public static void verifyHttpResponse(HttpResponse httpResponse) throws DPSHubException
    {
        int responseStatus = httpResponse.getStatus();

        String errorMessage = ErrorMessageParser.bestErrorMessage(new String(httpResponse.getErrorReason(), StandardCharsets.UTF_8));

        switch (responseStatus)
        {
            case 400:
                throw new DPSHubException(errorMessage);
            case 401:
                throw new DPSHubException(errorMessage);
            case 403:
                throw new DPSHubException(errorMessage);
            case 404:
                throw new DPSHubException(errorMessage);
            case 412:
                throw new DPSHubException(errorMessage);
            case 429:
                throw new DPSHubException(errorMessage);
            case 500:
                throw new DPSHubException(errorMessage);
            case 502:
                throw new DPSHubException(errorMessage);
            case 503:
                throw new DPSHubException(errorMessage);
            case 504:
                throw new DPSHubException(errorMessage);
            default:
                if (responseStatus > 300)
                {
                    throw new DPSHubException(errorMessage);
                }
                else
                {
                    // good case move forward
                    break;
                }
        }
    }
}
