// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.net.ssl.SSLContext;

import static com.microsoft.azure.sdk.iot.device.DeviceClientConfig.DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

/**
 * The optional settings for creating a {@link MultiplexingClient}. If set, these values will supersede any device client
 * level settings on these parameters.
 */
@Builder
public class MultiplexingClientOptions
{
    /**
     * The details of the proxy to connect to, if set. If not set, the multiplexing client will not connect through a proxy.
     */
    @Getter
    @Setter
    private ProxySettings proxySettings;

    /**
     * The SSLContext to use during SSL handshake with the server. If not set, a default SSLContext will be generated for you.
     */
    @Getter
    @Setter
    private SSLContext sslContext;

    /**
     * The period, in seconds, for how often a thread will spawn to handle queued outgoing messages. For instance, if this
     * is set to 10, then a thread will spawn every 10 seconds. If unset, this will default to {@link MultiplexingClient#DEFAULT_SEND_PERIOD_MILLIS}.
     */
    @Getter
    @Setter
    private long sendPeriod;

    /**
     * The period, in seconds, for how often a thread will spawn to handle queued incoming messages. For instance, if this
     * is set to 10, then a thread will spawn every 10 seconds. If unset, this will default to {@link MultiplexingClient#DEFAULT_RECEIVE_PERIOD_MILLIS}.
     */
    @Getter
    @Setter
    private long receivePeriod;

    /**
     * This option specifies how many messages a given send thread should attempt to send before exiting.
     * This option can be used in conjunction with the "sendPeriod" option to control the how frequently and in what
     * batch size messages are sent. If unset, this will default to {@link MultiplexingClient#DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD}.
     */
    @Getter
    @Setter
    private int maxMessagesSentPerSendThread;

    /**
     * Gets the keep alive interval in seconds. This value defines the
     * maximum time interval between messages sent or received. It enables the
     * client to detect if the server is no longer available, without having to wait
     * for the TCP/IP timeout. The client will ensure that at least one message
     * travels across the network within each keep alive period. In the absence of a
     * data-related message during the time period, the client sends a very small
     * "ping" message, which the server will acknowledge. The default value is 230 seconds.
     */
    @Getter
    @Builder.Default
    public int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

    /**
     * Sets the keep alive interval in seconds. This value defines the
     * maximum time interval between messages sent or received. It enables the
     * client to detect if the server is no longer available, without having to wait
     * for the TCP/IP timeout. The client will ensure that at least one message
     * travels across the network within each keep alive period. In the absence of a
     * data-related message during the time period, the client sends a very small
     * "ping" message, which the server will acknowledge. The default value is 230 seconds.
     *
     * @param keepAliveInterval the number of seconds that the keep alive interval will be. Must be greater than 0.
     */
    public void setKeepAliveInterval(int keepAliveInterval)
    {
        if (keepAliveInterval <= 0)
        {
            throw new IllegalArgumentException("Keep alive interval must be greater than 0 seconds");
        }

        this.keepAliveInterval = keepAliveInterval;
    }
}
