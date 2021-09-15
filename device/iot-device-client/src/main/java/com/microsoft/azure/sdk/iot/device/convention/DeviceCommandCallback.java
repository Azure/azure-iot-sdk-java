// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceMethodData;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class DeviceCommandCallback
{
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    PayloadConvention payloadConvention;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    byte[] payload;

    public <T> T GetData(Class<T> typeOfData) {
        return payloadConvention.getObjectFromBytes(payload, typeOfData);
    }

    abstract public DeviceCommandData call(String componentname, String methodName);
}
