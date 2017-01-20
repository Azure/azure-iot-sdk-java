// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import java.io.IOException;

 abstract public class MqttDeviceTwin extends Mqtt {

    public MqttDeviceTwin() throws IOException
    {
        super();

    }

    @Override
    public void onReconnect() throws IOException
    {
        System.out.println("On reconnect in Device Twin");
    }

    @Override
    void onReconnectComplete(boolean status) throws IOException
    {

    }

}
