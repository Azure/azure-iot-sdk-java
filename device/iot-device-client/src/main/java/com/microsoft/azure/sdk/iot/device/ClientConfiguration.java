// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.convention.DefaultPayloadConvention;
import com.microsoft.azure.sdk.iot.device.convention.PayloadConvention;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;

/**
 * Configuration settings for an IoT Hub client. Validates all user-defined
 * settings.
 */
@Slf4j
public final class ClientConfiguration
{
    private static final int DEFAULT_HTTPS_READ_TIMEOUT_MILLIS = 240000;
    private static final int DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLIS = 0; //no connect timeout

    public static final int DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS = 230;

    public static final int DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS = 20;
    public static final int DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS = 60;

    /** The default value for messageLockTimeoutSecs. */
    private static final int DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS = 180;

    private static final long DEFAULT_OPERATION_TIMEOUT = 4 * 60 * 1000; //4 minutes

    private boolean useWebsocket;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private ProxySettings proxySettings;

    private final String deviceClientUniqueIdentifier = UUID.randomUUID().toString().substring(0,8);

    /**
     * The device ModelId to be used with Azure IoT Plug and Play devices. This value must be set with the {@link ClientOptions} configuration.
     *
     * @return The current value of the device ModelId.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    String modelId;

    /**
     * The {@link PayloadConvention} to be used with convention based opertations such as Azure IoT Plug and Play devices. This value must be set with the {@link ClientOptions} configuration.
     *
     * @return The current {@link PayloadConvention} to be used for this device.
     */
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private PayloadConvention payloadConvention = DefaultPayloadConvention.getInstance();

    // Initialize all the timeout values here instead of the constructor as the constructor is not always called.
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int httpsReadTimeout = DEFAULT_HTTPS_READ_TIMEOUT_MILLIS;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int httpsConnectTimeout = DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLIS;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int amqpOpenAuthenticationSessionTimeout = DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int amqpOpenDeviceSessionsTimeout = DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int keepAliveInterval = DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;

    private IotHubAuthenticationProvider authenticationProvider;

    /**
     * The callback to be invoked if a message of Device Method type received.
     */
    private MessageCallback directMethodsMessageCallback;

    /** The context to be passed in to the device method type message callback. */
    private Object directMethodsMessageContext;

    /**
     * The callback to be invoked if a message of Device Twin type received.
     */
    private MessageCallback deviceTwinMessageCallback;

    /** The context to be passed in to the device twin type message callback. */
    private Object deviceTwinMessageContext;

    /**
     * The callback to be invoked if a message is received.
     */
    private MessageCallback defaultDeviceTelemetryMessageCallback;

    /** The context to be passed in to the message callback. */
    private Object defaultDeviceTelemetryMessageContext;

    private final Map<String, Pair<MessageCallback, Object>> inputChannelMessageCallbacks = new HashMap<>();

    @Getter
    private ProductInfo productInfo;

    public enum AuthType
    {
        X509_CERTIFICATE,
        SAS_TOKEN
    }

    @Getter
    private long operationTimeout = DEFAULT_OPERATION_TIMEOUT;

    @Getter
    private final IotHubClientProtocol protocol;

    @NonNull
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private RetryPolicy retryPolicy = new ExponentialBackoffWithJitter();

    /**
     * Constructor
     *
     * @param iotHubConnectionString is the string with the hostname, deviceId, and
     *                               deviceKey or token, which identify the device in
     *                               the Azure IotHub.
     *
     * @throws IllegalArgumentException if the IoT Hub hostname does not contain
     * a valid IoT Hub name as its prefix.
     */
    ClientConfiguration(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol) throws IllegalArgumentException
    {
        this.protocol = protocol;
        configSasAuth(iotHubConnectionString);
    }

    private void configSasAuth(IotHubConnectionString iotHubConnectionString) {
        commonConstructorSetup(iotHubConnectionString);
        assertConnectionStringIsNotX509(iotHubConnectionString);

        this.authenticationProvider = new IotHubSasTokenSoftwareAuthenticationProvider(
                iotHubConnectionString.getHostName(),
                iotHubConnectionString.getGatewayHostName(),
                iotHubConnectionString.getDeviceId(),
                iotHubConnectionString.getModuleId(),
                iotHubConnectionString.getSharedAccessKey(),
                iotHubConnectionString.getSharedAccessToken());

        log.debug("Device configured to use software based SAS authentication provider");
    }

    ClientConfiguration(IotHubAuthenticationProvider authenticationProvider, IotHubClientProtocol protocol) throws IllegalArgumentException
    {
        if (!(authenticationProvider instanceof IotHubSasTokenAuthenticationProvider))
        {
            throw new UnsupportedOperationException("This constructor only support sas token authentication currently");
        }

        this.protocol = protocol;
        this.authenticationProvider = authenticationProvider;
        this.useWebsocket = false;
        this.productInfo = new ProductInfo();
    }

    ClientConfiguration(String hostName, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions, String deviceId, String moduleId)
    {
        SSLContext sslContext = clientOptions != null ? clientOptions.getSslContext() : null;
        this.protocol = protocol;
        setClientOptionValues(clientOptions);
        this.authenticationProvider =
                new IotHubSasTokenProvidedAuthenticationProvider(hostName, deviceId, moduleId, sasTokenProvider, sslContext);

        this.useWebsocket = false;
        this.productInfo = new ProductInfo();

        this.payloadConvention = clientOptions != null ? clientOptions.getPayloadConvention() : DefaultPayloadConvention.getInstance();

        log.debug("Device configured to use SAS token provided authentication provider");
    }

    ClientConfiguration(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol, ClientOptions clientOptions)
    {
        this.protocol = protocol;

        if (clientOptions != null && clientOptions.getSslContext() != null)
        {
            configSsl(iotHubConnectionString, clientOptions.getSslContext());
        }
        else
        {
            configSasAuth(iotHubConnectionString);
        }

        setClientOptionValues(clientOptions);
    }

    private void setClientOptionValues(ClientOptions clientOptions)
    {
        this.modelId = clientOptions != null && clientOptions.getModelId() != null ? clientOptions.getModelId() : null;
        this.keepAliveInterval = clientOptions != null && clientOptions.getKeepAliveInterval() != 0 ? clientOptions.getKeepAliveInterval() : DEFAULT_KEEP_ALIVE_INTERVAL_IN_SECONDS;
        this.httpsReadTimeout = clientOptions != null && clientOptions.getHttpsReadTimeout() != 0 ? clientOptions.getHttpsReadTimeout() : DEFAULT_HTTPS_READ_TIMEOUT_MILLIS;
        this.httpsConnectTimeout = clientOptions != null && clientOptions.getHttpsConnectTimeout() != 0 ? clientOptions.getHttpsConnectTimeout() : DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLIS;
        this.amqpOpenAuthenticationSessionTimeout = clientOptions != null && clientOptions.getAmqpAuthenticationSessionTimeout() != 0 ? clientOptions.getAmqpAuthenticationSessionTimeout() : DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS;
        this.amqpOpenDeviceSessionsTimeout = clientOptions != null && clientOptions.getAmqpDeviceSessionTimeout() != 0 ? clientOptions.getAmqpDeviceSessionTimeout() : DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS;
        this.proxySettings = clientOptions != null && clientOptions.getProxySettings() != null ? clientOptions.getProxySettings() : null;

        if (proxySettings != null)
        {
            IotHubClientProtocol protocol = this.getProtocol();

            if (protocol != HTTPS && protocol != AMQPS_WS && protocol != MQTT_WS)
            {
                throw new IllegalArgumentException("Use of proxies is unsupported unless using HTTPS, MQTT_WS or AMQPS_WS");
            }
        }

        if (this.getSasTokenAuthentication() != null && clientOptions != null)
        {
            if (clientOptions.getSasTokenExpiryTime() <= 0)
            {
                throw new IllegalArgumentException("ClientOption sasTokenExpiryTime must be greater than 0");
            }

            this.getSasTokenAuthentication().setTokenValidSecs(clientOptions.getSasTokenExpiryTime());
        }

        if (this.keepAliveInterval <= 0)
        {
            throw new IllegalArgumentException("ClientOption keepAliveInterval must be greater than 0");
        }

        if (this.httpsReadTimeout < 0)
        {
            throw new IllegalArgumentException("ClientOption httpsReadTimeout must be greater than or equal to 0");
        }

        if (this.httpsConnectTimeout < 0)
        {
            throw new IllegalArgumentException("ClientOption httpsConnectTimeout must be greater than or equal to 0");
        }

        if (this.amqpOpenAuthenticationSessionTimeout <= 0)
        {
            throw new IllegalArgumentException("ClientOption amqpAuthenticationSessionTimeout must be greater than 0");
        }

        if (this.amqpOpenDeviceSessionsTimeout <= 0)
        {
            throw new IllegalArgumentException("ClientOption amqpDeviceSessionTimeout must be greater than 0");
        }
    }

    ClientConfiguration(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol, SSLContext sslContext)
    {
        this.protocol = protocol;
        configSsl(iotHubConnectionString, sslContext);
    }

    private void configSsl(IotHubConnectionString iotHubConnectionString, SSLContext sslContext) {
        commonConstructorSetup(iotHubConnectionString);

        if (iotHubConnectionString.isUsingX509())
        {
            this.authenticationProvider = new IotHubX509SoftwareAuthenticationProvider(
                    iotHubConnectionString.getHostName(),
                    iotHubConnectionString.getGatewayHostName(),
                    iotHubConnectionString.getDeviceId(),
                    iotHubConnectionString.getModuleId(),
                    sslContext);

            log.debug("Device configured to use software based x509 authentication provider with custom SSLContext");
        }
        else
        {
            this.authenticationProvider = new IotHubSasTokenSoftwareAuthenticationProvider(
                    iotHubConnectionString.getHostName(),
                    iotHubConnectionString.getGatewayHostName(),
                    iotHubConnectionString.getDeviceId(),
                    iotHubConnectionString.getModuleId(),
                    iotHubConnectionString.getSharedAccessKey(),
                    iotHubConnectionString.getSharedAccessToken(),
                    sslContext);

            log.debug("Device configured to use software based SAS authentication provider with custom SSLContext");
        }
    }

    /**
     * Constructor for a device client config that retrieves the authentication method from a security provider instance
     * @param connectionString The connection string for the iot hub to connect with
     * @param securityProvider The security provider instance to be used for authentication of this device
     * @throws IOException if the provided security provider throws an exception while authenticating
     */
    ClientConfiguration(IotHubConnectionString connectionString, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws IOException
    {
        commonConstructorSetup(connectionString);

        this.protocol = protocol;

        if (securityProvider == null)
        {
            throw new IllegalArgumentException("security provider cannot be null");
        }

        if (securityProvider instanceof SecurityProviderTpm)
        {
            this.authenticationProvider = new IotHubSasTokenHardwareAuthenticationProvider(
                    connectionString.getHostName(),
                    connectionString.getGatewayHostName(),
                    connectionString.getDeviceId(),
                    connectionString.getModuleId(),
                    securityProvider);
        }
        else if (securityProvider instanceof SecurityProviderSymmetricKey)
        {
            this.authenticationProvider = new IotHubSasTokenSoftwareAuthenticationProvider(
                    connectionString.getHostName(),
                    connectionString.getGatewayHostName(),
                    connectionString.getDeviceId(),
                    connectionString.getModuleId(),
                    new String(((SecurityProviderSymmetricKey) securityProvider).getSymmetricKey(), StandardCharsets.UTF_8),
                    null);
        }
        else if (securityProvider instanceof SecurityProviderX509)
        {
            this.authenticationProvider = new IotHubX509HardwareAuthenticationProvider(
                    connectionString.getHostName(),
                    connectionString.getGatewayHostName(),
                    connectionString.getDeviceId(),
                    connectionString.getModuleId(),
                    securityProvider);
        }
        else
        {
            throw new UnsupportedOperationException("The provided security provider is not supported.");
        }
    }

    /**
     * Constructor for a device client config that retrieves the authentication method from a security provider instance and sets the keep alive interval
     * @param connectionString The connection string for the iot hub to connect with
     * @param securityProvider The security provider instance to be used for authentication of this device
     * @param clientOptions The client options that will be used to set the keep alive
     * @throws IOException if the provided security provider throws an exception while authenticating
     */
    ClientConfiguration(IotHubConnectionString connectionString, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws IOException
    {
        // When setting the ClientOptions and a SecurityProvider, the SecurityProvider is responsible for setting the sslContext
        // we do not need to set the context in this constructor.
        this(connectionString, securityProvider, protocol);
        setClientOptionValues(clientOptions);
    }

    private void commonConstructorSetup(IotHubConnectionString iotHubConnectionString)
    {
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("connection string cannot be null");
        }

        this.productInfo = new ProductInfo();
        this.useWebsocket = false;
    }

    private void assertConnectionStringIsNotX509(IotHubConnectionString iotHubConnectionString)
    {
        if (iotHubConnectionString.isUsingX509())
        {
            throw new IllegalArgumentException("Cannot use this constructor for x509 connection strings. Use constructor that takes public key certificate and private key or takes an SSLContext instance instead");
        }
    }

    /**
     * Getter for SasTokenAuthentication
     *
     * @return The value of SasTokenAuthentication, or null if this object isn't using sas token authentication
     */
    public IotHubSasTokenAuthenticationProvider getSasTokenAuthentication()
    {
        if (this.authenticationProvider instanceof IotHubSasTokenAuthenticationProvider)
        {
            return (IotHubSasTokenAuthenticationProvider) this.authenticationProvider;
        }

        return null;
    }

    public IotHubAuthenticationProvider getAuthenticationProvider()
    {
        return this.authenticationProvider;
    }

    /**
     * Getter for Websocket
     * @return true if set, false otherwise
     */
    public boolean isUsingWebsocket()
    {
        return this.useWebsocket;
    }

    /**
     * Setter for Websocket
     * @param useWebsocket true if to be set, false otherwise
     */
    void setUseWebsocket(boolean useWebsocket)
    {
        this.useWebsocket = useWebsocket;
    }

    /**
     * Setter for the message callback. Can be {@code null}.
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed in to the callback.
     */
    void setMessageCallback(MessageCallback callback, Object context)
    {
        this.defaultDeviceTelemetryMessageCallback = callback;
        this.defaultDeviceTelemetryMessageContext = context;
    }

    void setMessageCallback(String inputName, MessageCallback callback, Object context)
    {
        if (this.inputChannelMessageCallbacks.containsKey(inputName) && callback == null)
        {
            this.inputChannelMessageCallbacks.remove(inputName);
        }
        else
        {
            this.inputChannelMessageCallbacks.put(inputName, new Pair<>(callback, context));
        }
    }

    /**
     * Getter for the IoT Hub hostname.
     * @return the IoT Hub hostname.
     */
    public String getIotHubHostname()
    {
        return this.authenticationProvider.getHostname();
    }

    /**
     * Getter for the IoT Hub name.
     * @return the IoT Hub name.
     */
    public String getIotHubName()
    {
        return IotHubConnectionString.parseHubName(this.authenticationProvider.getHostname());
    }

    /**
     * Getter for the Gateway host name.
     * @return the name of the gateway host
     */
    public String getGatewayHostname() 
    {
        return this.authenticationProvider.getGatewayHostname();
    }

    /**
     * Getter for the device ID.
     *
     * @return the device ID.
     */
    public String getDeviceId()
    {
        return this.authenticationProvider.getDeviceId();
    }

    public String getModuleId()
    {
        return this.authenticationProvider.getModuleId();
    }

    public String getDeviceClientUniqueIdentifier()
    {
        // Use device Id if present, use module Id if no device Id is present, use a unique Identifier if neither was set.
        String identifierPrefix = getDeviceId();
        if (identifierPrefix == null || identifierPrefix.isEmpty())
        {
            identifierPrefix = getModuleId();
            if (identifierPrefix == null || identifierPrefix.isEmpty())
            {
                // If there is no device Id or module Id, set the identifier prefix to be
                identifierPrefix = UUID.randomUUID().toString().substring(0, 8);
            }
        }

        return identifierPrefix + "-" + this.deviceClientUniqueIdentifier;
    }

    /**
     * Getter for the message callback.
     *
     * @param inputName the inputName that the desired callback is tied to, or null for the default callback
     *
     * @return the message callback.
     */
    public MessageCallback getDeviceTelemetryMessageCallback(String inputName)
    {
        if (inputName == null || !this.inputChannelMessageCallbacks.containsKey(inputName))
        {
            return this.defaultDeviceTelemetryMessageCallback;
        }
        else
        {
            return this.inputChannelMessageCallbacks.get(inputName).getKey();
        }
    }

    /**
     * Getter for the context to be passed in to the message callback.
     *
     * @param inputName the inputName that the desired callback context is tied to, or null for the default callback context
     *
     * @return the message context.
     */
    public Object getDeviceTelemetryMessageContext(String inputName)
    {
        if (inputName == null || !this.inputChannelMessageCallbacks.containsKey(inputName))
        {
            return this.defaultDeviceTelemetryMessageContext;
        }
        else
        {
            return this.inputChannelMessageCallbacks.get(inputName).getValue();
        }
    }

    /**
     * Setter for the device method message callback.
     *
     * @param callback Callback for device method messages.
     * @param context is the context for the callback.
     */
    public void setDirectMethodsMessageCallback(MessageCallback callback, Object context)
    {
        this.directMethodsMessageCallback = callback;
        this.directMethodsMessageContext = context;
    }

    /**
     * Getter for the device twin message callback.
     *
     * @return the device method message callback.
     */
    public MessageCallback getDirectMethodsMessageCallback()
    {
        return this.directMethodsMessageCallback;
    }

    /**
     * Getter for the context to be passed in to the device twin message callback.
     *
     * @return the device method message context.
     */
    public Object getDirectMethodsMessageContext()
    {
        return this.directMethodsMessageContext;
    }

    /**
     * Setter for the device twin message callback.
     *
     * @param callback callback to be invoked for device twin messages.
     * @param context is the context for the callback.
     */
    public void setDeviceTwinMessageCallback(MessageCallback callback, Object context)
    {
        this.deviceTwinMessageCallback = callback;
        this.deviceTwinMessageContext = context;
    }

    /**
     * Getter for the device twin message callback.
     *
     * @return the device twin message callback.
     */
    public MessageCallback getDeviceTwinMessageCallback()
    {
        return this.deviceTwinMessageCallback;
    }

    /**
     * Getter for the context to be passed in to the device twin message callback.
     *
     * @return the device twin message context.
     */
    public Object getDeviceTwinMessageContext()
    {
        return this.deviceTwinMessageContext;
    }

    /**
     * Getter for the timeout, in seconds, for the lock that the client has on a
     * received message.
     *
     * @return the timeout, in seconds, for a received message lock.
     */
    public int getMessageLockTimeoutSecs()
    {
        return DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS;
    }

    /**
     * Getter for AuthenticationType
     *
     * @return The value of AuthenticationType
     */
    public AuthType getAuthenticationType()
    {
        if (this.authenticationProvider instanceof IotHubSasTokenAuthenticationProvider)
        {
            return AuthType.SAS_TOKEN;
        }
        else
        {
            return AuthType.X509_CERTIFICATE;
        }
    }

    /**
     * Sets the device operation timeout
     * @param timeout the amount of time, in milliseconds, that a given device operation can last before expiring
     * @throws IllegalArgumentException if timeout is 0 or negative
     */
    void setOperationTimeout(long timeout) throws IllegalArgumentException
    {
        if (timeout < 1)
        {
            throw new IllegalArgumentException("Operation timeout cannot be 0 or negative");
        }

        this.operationTimeout = timeout;
    }

    @SuppressWarnings("unused")
    protected ClientConfiguration()
    {
        this.authenticationProvider = null;
        this.directMethodsMessageCallback = null;
        this.defaultDeviceTelemetryMessageCallback = null;
        this.deviceTwinMessageCallback = null;
        this.directMethodsMessageContext = null;
        this.defaultDeviceTelemetryMessageContext = null;
        this.deviceTwinMessageContext = null;
        this.useWebsocket = false;
        this.protocol = AMQPS;
    }
}
