// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class DeviceCommandCallback
{
    abstract public DeviceCommandResponse call(DeviceCommandRequest deviceCommandRequest);
}
