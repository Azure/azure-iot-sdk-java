/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class ModuleClientException extends Exception
{
    public ModuleClientException()
    {
        super();
    }

    public ModuleClientException(String message)
    {
        super(message);
    }

    public ModuleClientException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public ModuleClientException(Throwable cause)
    {
        super(cause);
    }

    public ModuleClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
