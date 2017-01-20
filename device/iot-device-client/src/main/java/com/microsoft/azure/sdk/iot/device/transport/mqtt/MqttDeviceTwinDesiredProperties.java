// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;

import java.io.IOException;

public class MqttDeviceTwinDesiredProperties extends MqttDeviceTwin{

    public MqttDeviceTwinDesiredProperties() throws IOException
    {
        super();
    }
    @Override
    String parseTopic()  throws IOException
    {
        return null;
    }

    @Override
    byte[] parsePayload(String topic) throws IOException {
        return null;
    }

    @Override
    public Message receive() throws  IOException{
        return null;
    }


}
