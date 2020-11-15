package com.microsoft.azure.sdk.iot.device.transport.amqps;

/**
 * Type of the connection
 */
public enum IoTHubConnectionType
{
    UNKNOWN,

    /**
     * The connection type is a non-multiplexed, single device identity connection.
     */
    SINGLE_CLIENT,

    /**
     * Multiplexing should be done with {@link com.microsoft.azure.sdk.iot.device.MultiplexingClient} which uses {@link #USE_MULTIPLEXING_CLIENT}
     */
    @Deprecated
    USE_TRANSPORTCLIENT,

    /**
     * The connection type is a multiplexed connection using {@link com.microsoft.azure.sdk.iot.device.MultiplexingClient}
     */
    USE_MULTIPLEXING_CLIENT
}
