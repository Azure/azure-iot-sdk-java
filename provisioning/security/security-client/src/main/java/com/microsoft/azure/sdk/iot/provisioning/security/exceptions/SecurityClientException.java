/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security.exceptions;

public class SecurityClientException extends Exception
{
    public SecurityClientException(Throwable e)
    {
        super(e);
    }

    public SecurityClientException(String message, Throwable e)
    {
        super(message, e);
    }

    public SecurityClientException(String message)
    {
        super(message);
    }
}
