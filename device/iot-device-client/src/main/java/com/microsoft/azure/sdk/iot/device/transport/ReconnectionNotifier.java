/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

public final class ReconnectionNotifier
{
    private final static String THREAD_NAME="azure-iot-sdk-ReconnectionTask";

    private ReconnectionNotifier(){}

    public static void notifyDisconnectAsync(final Throwable connectionLossCause, final IotHubListener listener, final String connectionId)
    {
        new Thread(
                new Runnable()
                {
                    @Override public void run()
                    {
                        listener.onConnectionLost(connectionLossCause,connectionId);
                    }
                },
                THREAD_NAME+":"+connectionId
        ).start();
    }
}