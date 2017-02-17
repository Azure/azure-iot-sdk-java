// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;

import java.io.IOException;

public class MqttDeviceMethods extends Mqtt{

    @Override
    String parseTopic() throws IOException
    {
        return null;
    }

    @Override
    byte[] parsePayload(String topic) throws IOException
    {
        return null;
    }

    public MqttDeviceMethods() throws IOException
    {
        super();
    }
}
