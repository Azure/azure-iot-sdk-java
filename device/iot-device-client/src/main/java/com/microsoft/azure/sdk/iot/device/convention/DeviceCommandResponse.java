// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import lombok.Data;

@Data
/**
 * The response to a command to be sent by the client.
 */
public class DeviceCommandResponse
{
    private Object responseMessage;
    private int status;

    /**
     * The device command data constructor. This will be used with the {@link com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention} set for the device client.
     * @param status A response status code.
     * @param responseMessage The response message that will be
     */
    public DeviceCommandResponse(int status, Object responseMessage)
    {
        this.status = status;
        this.responseMessage = responseMessage;
    }
}
