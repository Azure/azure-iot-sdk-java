/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security.exceptions;

public class SecurityProviderException extends Exception
{
    public SecurityProviderException(Throwable e)
    {
        super(e);
    }

    public SecurityProviderException(String message, Throwable e)
    {
        super(message, e);
    }

    public SecurityProviderException(String message)
    {
        super(message);
    }
}
