/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class ServiceUnknownException extends IotHubServiceException
{
    public ServiceUnknownException()
    {
        super();
    }

    public ServiceUnknownException(String message)
    {
        super(message);
    }

    public ServiceUnknownException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ServiceUnknownException(Throwable cause)
    {
        super(cause);
    }
}
