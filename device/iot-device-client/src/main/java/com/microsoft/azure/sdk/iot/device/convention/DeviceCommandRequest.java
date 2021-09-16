// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.deps.convention.PayloadConvention;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class DeviceCommandRequest
{
    @Getter
    /**
     * The component this command is for.
     */
    private String componentName;

    @Getter
    /**
        The name of the command.
     */
    private String commandName;

    /**
     * The payload of the command.
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    byte[] payload;

    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    PayloadConvention payloadConvention;

    public <T> T GetPayloadAsObject(Class<T> typeOfData) {
        return payloadConvention.getObjectFromBytes(payload, typeOfData);
    }

    public byte[] GetPayloadAsBytes() {
        return payload;
    }

    public String GetPayloadAsString() {
        return payloadConvention.getPayloadEncoder().decodeByteArrayToString(payload);
    }

}
