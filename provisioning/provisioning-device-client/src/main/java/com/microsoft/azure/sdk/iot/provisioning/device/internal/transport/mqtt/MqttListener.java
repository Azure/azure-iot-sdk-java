/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.transport.mqtt;

public interface MqttListener
{
    /**
     * Called when the message gets received by PAHO
     * @param message the received Mqtt message
     */
    void messageReceived(MqttMessage message);

    /**
     * Called by PAHO when the connection is lost
     * @param throwable the disconnection reason.
     */
    void connectionLost(Throwable throwable);
}
