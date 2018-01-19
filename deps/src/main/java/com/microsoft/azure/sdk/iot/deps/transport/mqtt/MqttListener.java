/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.mqtt;

public interface MqttListener
{
    /**
     * Called when the message gets received by PAHO
     * @param message the received Mqtt message
     */
    void messageReceived(MqttMessage message);

    /**
     * Called when PAHO establishes a connection to a server
     */
    void connectionEstablished();

    /**
     * Called by PAHO when the connection is lost
     * @param throwable the disconnection reason.
     */
    void connectionLost(Throwable throwable);
}
