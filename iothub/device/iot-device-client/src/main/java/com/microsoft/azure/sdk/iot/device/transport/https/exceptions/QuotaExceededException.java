/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.https.exceptions;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.transport.IotHubServiceException;

public class QuotaExceededException extends IotHubServiceException
{
    public QuotaExceededException()
    {
        super();
    }

    public QuotaExceededException(String message)
    {
        super(message);
    }

    public QuotaExceededException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public QuotaExceededException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public IotHubStatusCode getStatusCode()
    {
        return IotHubStatusCode.QUOTA_EXCEEDED;
    }
}
