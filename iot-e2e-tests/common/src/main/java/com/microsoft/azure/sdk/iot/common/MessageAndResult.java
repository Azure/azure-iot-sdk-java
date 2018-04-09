/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

public class MessageAndResult
{
    public Message message;
    public IotHubStatusCode statusCode;

    public MessageAndResult(Message message, IotHubStatusCode statusCode)
    {
        this.statusCode = statusCode;
        this.message = message;
    }
}
