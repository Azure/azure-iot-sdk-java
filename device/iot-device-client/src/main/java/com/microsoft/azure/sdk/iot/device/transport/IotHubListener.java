/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.Message;

public interface IotHubListener
{
    /**
     * Method executed when a message was acknowledged by IoTHub.
     */
    void messageSent(Message message, Throwable e);

    //message received
    void messageReceived(Message message, Throwable e);

    //connection drop
    void connectionLost(Throwable e);

    //connection established
    void connectionEstablished(Throwable e);
}
