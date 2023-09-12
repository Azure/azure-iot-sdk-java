// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

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

    /**
     * The prefix that will be applied to the names of all threads created by this client. If
     * {@link #useIdentifiableThreadNames} is set to true, then this value is ignored and this client will create the
     * prefix for you.
     */
    @Getter
    @Builder.Default
    private final String threadNamePrefix = null;

    /**
     * The suffix that will be applied to the names of all threads created by this client. If
     * {@link #useIdentifiableThreadNames} is set to true, then this value is ignored and this client will create the
     * suffix for you.
     */
    @Getter
    @Builder.Default
    private final String threadNameSuffix = null;

    /**
     * If true, all threads created by this client will use names that are unique. This is useful in applications that manage
     * multiple device/module clients and want to be able to correlate logs to a particular client. In addition,
     * the {@link #threadNamePrefix} and {@link #threadNameSuffix} values will be ignored.
     *
     * If false, all threads created by this client will use simple names that describe the thread's purpose, but are
     * indistinguishable from the same threads created by a different client instance. However, users may still alter
     * these thread names by providing values for the {@link #threadNamePrefix} and {@link #threadNameSuffix}.
     */
    @Builder.Default
    private final boolean useIdentifiableThreadNames = true;

    /**
     * This option allows for routine disconnect events (such as expired SAS token disconnects
     * when connecting over MQTT or MQTT_WS) to be logged at debug levels as opposed to error or
     * warn levels. By default, these routine disconnects are logged at error or warn levels.
     *
     * Note that there is a degree of speculation involved when this client labels a disconnect
     * as a routine disconnect. Generally, if the client is disconnected when the previous SAS
     * token was expired, it will assume it was a routine disconnect. However, there may be
     * legitimate disconnects due to network errors that could be mislabeled and not logged
     * at a warning or error level if they occur around the time that a routine error is expected.
     */
    @Builder.Default
    private final boolean logRoutineDisconnectsAsErrors = true;

    public boolean isUsingIdentifiableThreadNames()
    {
        // Using a manually written method here to override the name that Lombok would have given it
        return this.useIdentifiableThreadNames;
    }

    public boolean isLoggingRoutineDisconnectsAsErrors()
    {
        // Using a manually written method here to override the name that Lombok would have given it
        return this.logRoutineDisconnectsAsErrors;
    }
}
