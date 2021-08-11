package com.microsoft.azure.sdk.iot.device;

/**
 * The type of the device client. Used to differentiate between device clients that own their own connection from device
 * clients that are multiplexing.
 */
public enum ClientType
{
    /**
     * The connection type is a non-multiplexed, single device identity connection.
     */
    SINGLE_CLIENT,

    /**
     * The connection type is a multiplexed connection using {@link com.microsoft.azure.sdk.iot.device.MultiplexingClient}
     */
    USE_MULTIPLEXING_CLIENT
}
