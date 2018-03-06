/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.exceptions;

public class HubOrDeviceIdNotFoundException extends ProtocolException
{
    public HubOrDeviceIdNotFoundException()
    {
        super();
    }

    public HubOrDeviceIdNotFoundException(String message)
    {
        super(message);
    }

    public HubOrDeviceIdNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HubOrDeviceIdNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
