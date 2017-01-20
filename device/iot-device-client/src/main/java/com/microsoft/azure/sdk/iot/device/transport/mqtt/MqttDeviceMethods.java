// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.Message;

import java.io.IOException;

public class MqttDeviceMethods extends Mqtt{

    @Override
    String parseTopic()  {
        return null;
    }

    @Override
    byte[] parsePayload(String topic) {
        return null;
    }

    @Override
    public Message receive() {
        return null;
    }


    @Override
    public void onReconnect() throws IOException
    {
        System.out.println("On reconnect in Device Methods");
    }

    @Override
    void onReconnectComplete(boolean status) throws IOException {

    }

    public MqttDeviceMethods() throws IOException
    {
        super();
    }
}
