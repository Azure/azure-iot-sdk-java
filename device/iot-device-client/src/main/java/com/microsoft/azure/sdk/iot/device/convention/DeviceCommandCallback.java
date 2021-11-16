// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

/**
 * The command callback to be executed for all commands.
 */
public interface DeviceCommandCallback
{
    /**
     * The call to be implemented.
     * @param deviceCommandRequest A populated command request that will contain the component, command name, and payload.
     * @return The response to the command.
     */
    DeviceCommandResponse onDeviceCommandReceived(DeviceCommandRequest deviceCommandRequest);
}
