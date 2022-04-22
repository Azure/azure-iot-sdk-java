// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import lombok.Data;

/**
 * The response to a command to be sent by the client.
 */
@Data
public class DeviceCommandResponse
{
    //TODO is "Device" very future proof here?
    //TODO check that payload types are supported like in direct methods

    private Object responseMessage;
    private int status;

    /**
     * The device command data constructor. This will be used with the {@link com.microsoft.azure.sdk.iot.device.convention.PayloadConvention} set for the device client.
     * @param status A response status code.
     * @param responseMessage The response message that will be
     */
    public DeviceCommandResponse(int status, Object responseMessage)
    {
        this.status = status;
        this.responseMessage = responseMessage;
    }
}
