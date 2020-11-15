/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport;

public final class ReconnectionNotifier
{
    private final static String RECONNECTION_THREAD_NAME ="azure-iot-sdk-ConnectionReconnectionTask";
    private final static String DEVICE_SESSION_RECONNECTION_THREAD_NAME="azure-iot-sdk-DeviceSessionReconnectionTask";

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
                RECONNECTION_THREAD_NAME + ":" + connectionId
        ).start();
    }

    public static void notifyDeviceDisconnectAsync(final Throwable connectionLossCause, final IotHubListener listener, final String connectionId, final String deviceId)
    {
        new Thread(
                new Runnable()
                {
                    @Override public void run()
                    {
                        listener.onMultiplexedDeviceSessionLost(connectionLossCause,connectionId, deviceId);
                    }
                },
                DEVICE_SESSION_RECONNECTION_THREAD_NAME + ":" + connectionId
        ).start();
    }
}