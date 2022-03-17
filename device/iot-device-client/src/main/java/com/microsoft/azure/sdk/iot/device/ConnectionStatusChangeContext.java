package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The context surrounding a connection status change event for this client.
 */
@AllArgsConstructor
public class ConnectionStatusChangeContext
{
    /**
     * The new connection status of the client.
     */
    @Getter
    IotHubConnectionStatus newStatus;

    /**
     * The previous status of this client.
     */
    @Getter
    IotHubConnectionStatus previousStatus;

    /**
     * The reason why the sdk changed to this status.
     */
    @Getter
    IotHubConnectionStatusChangeReason newStatusReason;

    /**
     * The throwable that caused the change in status. May be null if there wasn't an associated throwable.
     */
    @Getter
    Throwable cause;

    /**
     * The user provided context object that was set when setting the connection status change callback on the client.
     */
    @Getter
    Object callbackContext;
}
