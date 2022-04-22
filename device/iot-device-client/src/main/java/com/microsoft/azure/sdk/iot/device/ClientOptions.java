// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.convention.DefaultPayloadConvention;
import com.microsoft.azure.sdk.iot.device.convention.PayloadConvention;
import lombok.*;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;

import static com.microsoft.azure.sdk.iot.device.ClientConfiguration.DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

/**
 * Options that allow configuration of the device client instance during initialization.
 */
@Builder
public final class ClientOptions
{
    private static final int DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLISECONDS = 0;
    private static final int DEFAULT_HTTPS_READ_TIMEOUT_MILLISECONDS = 4 * 60 * 1000; // 4 minutes
    private static final long DEFAULT_SAS_TOKEN_EXPIRY_TIME_SECONDS = 60 * 60; // 1 hour
    private static final int DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD = 10;
    private static final int SEND_PERIOD_MILLIS = 10;
    private static final int RECEIVE_PERIOD_MILLIS = 10;

    /**
     * The Digital Twin Model Id associated with the device and module identity.
     * Non plug and play users should not set this value
     * This feature is currently supported only over MQTT, MQTT_WS, AMQPS, and AMQPS_WS.
     */
    @Getter
    private final String modelId;

    /**
     * The ssl context that will be used during authentication. If the provided connection string does not contain
     * SAS based credentials, then the sslContext will be used for x509 authentication. If the provided connection string
     * does contain SAS based credentials, the sslContext will still be used during SSL negotiation. By default, this SDK will
     * create an SSLContext instance for you that trusts the IoT Hub public certificates.
     */
    @Getter
    private final SSLContext sslContext;

    /**
     * The convention to be used for convention based operations.
     */
    @Setter
    @Getter
    @Builder.Default
    private PayloadConvention payloadConvention = DefaultPayloadConvention.getInstance();

    /**
     * The proxy settings for this client to connect through. If null then no proxy will be used.
     */
    @Getter
    private final ProxySettings proxySettings;

    /**
     * This value defines the maximum time interval between messages sent or received. It enables the
     * client to detect if the server is no longer available, without having to wait
     * for the TCP/IP timeout. The client will ensure that at least one message
     * travels across the network within each keep alive period. In the absence of a
     * data-related message during the time period, the client sends a very small
     * "ping" message, which the server will acknowledge. The default value is 230 seconds.
     *
     * <p>
     * This value is only used in stateful connection oriented protocols such as AMQPS, AMQPS_WS, MQTT, and MQTT_WS. If
     * the client is using HTTPS, then this value is ignored.
     * </p>
     */
    @Getter
    @Builder.Default
    private final int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

    /**
     * This option is only applicable for HTTPS.
     * This option specifies the connect timeout in milliseconds per https request
     * made by this client. By default, this value is 0 (no connect timeout).
     */
    @Getter
    @Builder.Default
    private final int httpsReadTimeout = DEFAULT_HTTPS_READ_TIMEOUT_MILLISECONDS;

    /**
     * this option is only applicable for HTTPS.
     * This option specifies the read timeout in milliseconds per https request
     * made by this client. By default, this value is 4 minutes.
     */
    @Getter
    @Builder.Default
    private final int httpsConnectTimeout = DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLISECONDS;

    /**
     * This option specifies the time to live (in seconds) for all SAS tokens generated for this client. By default,
     * this value is 1 hour.
     */
    @Getter
    @Builder.Default
    private final long sasTokenExpiryTime = DEFAULT_SAS_TOKEN_EXPIRY_TIME_SECONDS;

    /**
     * This option is applicable for AMQP with SAS token authentication.
     * This option specifies the timeout in seconds to wait to open the authentication session.
     * By default, this value is 20 seconds.
     */
    @Getter
    @Builder.Default
    private final int amqpAuthenticationSessionTimeout = ClientConfiguration.DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS;

    /**
     * This option is applicable for AMQP.
     * This option specifies the timeout in seconds to open the device sessions.
     * By default, this value is 60 seconds.
     */
    @Getter
    @Builder.Default
    private final int amqpDeviceSessionTimeout = ClientConfiguration.DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS;

    /**
     * This option is applicable to all protocols.
     * This option specifies how many messages a given send thread should attempt to send before exiting.
     * This option can be used in conjunction with the {@link #sendInterval} option to control the how frequently
     * and in what batch size messages are sent. By default, this client sends 10 messages per send thread, and spawns
     * a send thread every 10 milliseconds. This gives a theoretical throughput of 1000 messages per second.
     */
    @Getter
    @Builder.Default
    private final int messagesSentPerSendInterval = DEFAULT_MAX_MESSAGES_TO_SEND_PER_THREAD;

    /**
     * This option is applicable to all protocols. This option sets the interval (in milliseconds)
     * that this SDK awakens the send thread to send queued messages. The default value is 10 milliseconds.
     */
    @Getter
    @Builder.Default
    private final int sendInterval = SEND_PERIOD_MILLIS;

    /**
     * This option is applicable to all protocols. This option specifies the interval (in milliseconds)
     * between waking a thread that dequeues a message from the SDK's queue of received messages. The default value is
     * 10 milliseconds. For clients using HTTP, this option also controls how frequently polling messages are sent to check
     * for new cloud to device messages. Setting this option to a higher value will make the client send less frequent poll
     * requests.
     */
    @Getter
    @Builder.Default
    private final int receiveInterval = RECEIVE_PERIOD_MILLIS;
}
