package com.microsoft.azure.sdk.iot.device.transport;

import lombok.Getter;
import lombok.Setter;

/**
 * represents the connection state of a single multiplexed device client.
 */
public class MultiplexedDeviceState
{
    public MultiplexedDeviceState(IotHubConnectionStatus connectionStatus)
    {
        this(connectionStatus, null);
    }

    public MultiplexedDeviceState(IotHubConnectionStatus connectionStatus, Throwable lastException)
    {
        this.connectionStatus = connectionStatus;
        this.lastException = lastException;
    }

    /**
     * The current connection status of this multiplexed device.
     */
    @Getter
    @Setter
    private IotHubConnectionStatus connectionStatus;

    /**
     * The last thrown exception related to this connection. Used to save the last cause of a disconnection event for
     * reporting and retry purposes.
     */
    @Getter
    @Setter
    private Throwable lastException;

    /**
     * The time that this multiplexed device session has started reconnecting, measured in milliseconds since the UNIX epoch
     */
    @Getter
    @Setter
    private long startReconnectTime;

    /**
     * The current reconnect attempt number for this multiplexed device. Each unsuccessful reconnect attempts will increment
     * this value by 1.
     */
    @Getter
    @Setter
    private int reconnectionAttemptNumber;

    protected void incrementReconnectionAttemptNumber()
    {
        this.reconnectionAttemptNumber++;
    }
}
