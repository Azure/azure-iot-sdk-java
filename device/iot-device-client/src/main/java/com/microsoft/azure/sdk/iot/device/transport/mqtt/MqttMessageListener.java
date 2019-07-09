/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttMessageListener
{
    public void onMessageArrived(String topic, MqttMessage mqttMessage);
}
