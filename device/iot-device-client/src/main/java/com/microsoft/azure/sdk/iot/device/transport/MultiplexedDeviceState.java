package com.microsoft.azure.sdk.iot.device.transport;

import lombok.Getter;
import lombok.Setter;

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

    @Getter
    @Setter
    private IotHubConnectionStatus connectionStatus;

    @Getter
    @Setter
    private Throwable lastException;

    @Getter
    @Setter
    private long startReconnectTime;

    @Getter
    @Setter
    private int reconnectionAttemptNumber;

    protected void incrementReconnectionAttemptNumber()
    {
        this.reconnectionAttemptNumber++;
    }
}
