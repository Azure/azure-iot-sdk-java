// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.transport.ExponentialBackoffWithJitter;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration settings for an IoT Hub client. Validates all user-defined
 * settings.
 */
@Slf4j
public final class DeviceClientConfig
{
    private static final int DEFAULT_HTTPS_READ_TIMEOUT_MILLIS = 240000;
    private static final int DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLIS = 0; //no connect timeout

    private static final int DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS = 20;
    private static final int DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS = 60;

    /** The default value for messageLockTimeoutSecs. */
    private static final int DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS = 180;

    private static final long DEFAULT_OPERATION_TIMEOUT = 4 * 60 * 1000; //4 minutes

    private boolean useWebsocket;
    private ProxySettings proxySettings;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    String modelId;

    // Initialize all the timeout values here instead of the constructor as the constructor is not always called.
    @Getter
    @Setter
    private int httpsReadTimeout = DEFAULT_HTTPS_READ_TIMEOUT_MILLIS;

    @Getter
    @Setter
    private int httpsConnectTimeout = DEFAULT_HTTPS_CONNECT_TIMEOUT_MILLIS;

    @Getter
    @Setter
    private int amqpOpenAuthenticationSessionTimeout = DEFAULT_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT_IN_SECONDS;

    @Getter
    @Setter
    private int amqpOpenDeviceSessionsTimeout = DEFAULT_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT_IN_SECONDS;

    private IotHubAuthenticationProvider authenticationProvider;

    /**
     * The callback to be invoked if a message of Device Method type received.
     */
    private MessageCallback deviceMethodsMessageCallback;
    /** The context to be passed in to the device method type message callback. */
    private Object deviceMethodsMessageContext;

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

    private Map<String, Pair<MessageCallback, Object>> inputChannelMessageCallbacks = new HashMap<>();

    private ProductInfo productInfo;

    public enum AuthType
    {
        X509_CERTIFICATE,
        SAS_TOKEN
    }

    private long operationTimeout = DEFAULT_OPERATION_TIMEOUT;
    private IotHubClientProtocol protocol;

    // Codes_SRS_DEVICECLIENTCONFIG_28_001: [The class shall have ExponentialBackOff as the default retryPolicy.]
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
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString) throws IllegalArgumentException
    {
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

    public DeviceClientConfig(IotHubAuthenticationProvider authenticationProvider) throws IllegalArgumentException
    {
        if (!(authenticationProvider instanceof IotHubSasTokenAuthenticationProvider))
        {
            throw new UnsupportedOperationException("This constructor only support sas token authentication currently");
        }

        this.authenticationProvider = authenticationProvider;
        this.useWebsocket = false;
        this.productInfo = new ProductInfo();
    }


    public DeviceClientConfig(String hostName, SasTokenProvider sasTokenProvider, ClientOptions clientOptions, String deviceId, String moduleId)
    {
        this.authenticationProvider = new IotHubSasTokenProvidedAuthenticationProvider(
                hostName,
                deviceId,
                moduleId,
                sasTokenProvider,
                clientOptions != null ? clientOptions.sslContext : null);

        this.useWebsocket = false;
        this.productInfo = new ProductInfo();

        log.debug("Device configured to use SAS token provided authentication provider");
    }

    /**
     * Constructor for device configs that use x509 authentication
     *
     * @param iotHubConnectionString The connection string for the device. (format: "HostName=...;deviceId=...;x509=true")
     * @param publicKeyCertificate The PEM encoded public key certificate or the path to the PEM encoded public key certificate file
     * @param isPathForPublic If the provided publicKeyCertificate is a path to the actual public key certificate
     * @param privateKey The PEM encoded private key or the path to the PEM encoded private key file
     * @param isPathForPrivate If the provided privateKey is a path to the actual private key
     */
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate)
    {
        commonConstructorSetup(iotHubConnectionString);
        assertConnectionStringIsX509(iotHubConnectionString);

        //Codes_SRS_DEVICECLIENTCONFIG_34_069: [This function shall generate a new SSLContext and set this to using X509 authentication.]
        this.authenticationProvider = new IotHubX509SoftwareAuthenticationProvider(
                iotHubConnectionString.getHostName(),
                iotHubConnectionString.getGatewayHostName(),
                iotHubConnectionString.getDeviceId(),
                iotHubConnectionString.getModuleId(),
                publicKeyCertificate, isPathForPublic, privateKey, isPathForPrivate);

        log.debug("Device configured to use software based x509 authentication provider");
    }

    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, ClientOptions clientOptions)
    {
        if (clientOptions != null && clientOptions.sslContext != null)
        {
            configSsl(iotHubConnectionString, clientOptions.sslContext);
        }
        else
        {
            configSasAuth(iotHubConnectionString);
        }
    }

    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, SSLContext sslContext)
    {
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
    DeviceClientConfig(IotHubConnectionString connectionString, SecurityProvider securityProvider) throws IOException
    {
        commonConstructorSetup(connectionString);

        if (securityProvider == null)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
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
            //Codes_SRS_DEVICECLIENTCONFIG_34_083: [If the provided security provider is a SecurityProviderTpm instance, this function shall set its auth type to SAS and create its IotHubSasTokenAuthenticationProvider instance using the security provider.]
            this.authenticationProvider = new IotHubSasTokenSoftwareAuthenticationProvider(
                    connectionString.getHostName(),
                    connectionString.getGatewayHostName(),
                    connectionString.getDeviceId(),
                    connectionString.getModuleId(),
                    new String(((SecurityProviderSymmetricKey) securityProvider).getSymmetricKey()),
                    null);
        }
        else if (securityProvider instanceof SecurityProviderX509)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_082: [If the provided security provider is a SecurityProviderX509 instance, this function shall set its auth type to X509 and create its IotHubX509AuthenticationProvider instance using the security provider's ssl context.]
            this.authenticationProvider = new IotHubX509HardwareAuthenticationProvider(
                    connectionString.getHostName(),
                    connectionString.getGatewayHostName(),
                    connectionString.getDeviceId(),
                    connectionString.getModuleId(),
                    securityProvider);
        }
        else
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_084: [If the provided security provider is neither a SecurityProviderX509 instance nor a SecurityProviderTpm instance, this function shall throw an UnsupportedOperationException.]
            throw new UnsupportedOperationException("The provided security provider is not supported.");
        }
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

    private void assertConnectionStringIsX509(IotHubConnectionString iotHubConnectionString)
    {
        if (!iotHubConnectionString.isUsingX509())
        {
            throw new IllegalArgumentException("Cannot use this constructor for connection strings that don't use x509 authentication.");
        }
    }

    private void assertConnectionStringIsNotX509(IotHubConnectionString iotHubConnectionString)
    {
        if (iotHubConnectionString.isUsingX509())
        {
            throw new IllegalArgumentException("Cannot use this constructor for x509 connection strings. Use constructor that takes public key certificate and private key or takes an SSLContext instance instead");
        }
    }

    public IotHubClientProtocol getProtocol()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_047: [This function shall return the saved protocol.]
        return this.protocol;
    }

    void setProtocol(IotHubClientProtocol protocol)
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_048: [This function shall save the provided protocol.]
        this.protocol = protocol;
    }

    /**
     * Setter for RetryPolicy
     *
     * @param retryPolicy The types of retry policy to be used
     * @throws IllegalArgumentException if retry policy is null
     */
    public void setRetryPolicy(RetryPolicy retryPolicy) throws IllegalArgumentException
    {
        // Codes_SRS_DEVICECLIENTCONFIG_28_002: [This function shall throw IllegalArgumentException retryPolicy is null.]
        if (retryPolicy == null)
        {
            throw new IllegalArgumentException("Retry Policy cannot be null.");
        }

        // Codes_SRS_DEVICECLIENTCONFIG_28_003: [This function shall set retryPolicy.]
        this.retryPolicy = retryPolicy;
    }

    /**
     * Getter for RetryPolicy
     *
     * @return The value of RetryPolicy
     */
    public RetryPolicy getRetryPolicy()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_28_004: [This function shall return the saved RetryPolicy object.]
        return this.retryPolicy;
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
            // Codes_SRS_DEVICECLIENTCONFIG_34_055: [If the saved authentication provider uses sas tokens, this function return the saved authentication provider.]
            return (IotHubSasTokenAuthenticationProvider) this.authenticationProvider;
        }

        // Codes_SRS_DEVICECLIENTCONFIG_34_056: [If the saved authentication provider doesn't use sas tokens, this function return null.]
        return null;
    }

    public IotHubAuthenticationProvider getAuthenticationProvider()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_049: [This function return the saved authentication provider.]
        return this.authenticationProvider;
    }

    /**
     * Getter for Websocket
     * @return true if set, false otherwise
     */
    public boolean isUseWebsocket()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_037: [The function shall return the true if websocket is enabled, false otherwise.]
        return this.useWebsocket;
    }

    /**
     * Setter for Websocket
     * @param useWebsocket true if to be set, false otherwise
     */
    public void setUseWebsocket(boolean useWebsocket)
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_038: [The function shall save useWebsocket.]
        this.useWebsocket = useWebsocket;
    }

    /**
     * Setter for the message callback. Can be {@code null}.
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed in to the callback.
     */
    public void setMessageCallback(MessageCallback callback, Object context)
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
        this.defaultDeviceTelemetryMessageCallback = callback;
        this.defaultDeviceTelemetryMessageContext = context;
    }

    public void setMessageCallback(String inputName, MessageCallback callback, Object context)
    {
        if (this.inputChannelMessageCallbacks.containsKey(inputName) && callback == null)
        {
            // Codes_SRS_DEVICECLIENTCONFIG_34_058: [If the provided inputName is already saved in the message callbacks map, and the provided callback is null, this function
            // shall remove the inputName from the message callbacks map.]
            this.inputChannelMessageCallbacks.remove(inputName);
        }
        else
        {
            // Codes_SRS_DEVICECLIENTCONFIG_34_044: [The function shall map the provided inputName to the callback and context in the saved inputChannelMessageCallbacks map.]
            this.inputChannelMessageCallbacks.put(inputName, new Pair<>(callback, context));
        }
    }

    /**
     * Getter for the IoT Hub hostname.
     * @return the IoT Hub hostname.
     */
    public String getIotHubHostname()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
        return this.authenticationProvider.getHostname();
    }

    /**
     * Getter for the IoT Hub name.
     * @return the IoT Hub name.
     */
    public String getIotHubName()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_007: [The function shall return the IoT Hub name given in the constructor, where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
        return IotHubConnectionString.parseHubName(this.authenticationProvider.getHostname());
    }

    /**
     * Getter for the Gateway host name.
     * @return the name of the gateway host
     */
    public String getGatewayHostname() 
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_057: [The function shall return the gateway hostname, or null if this connection string does not contain a gateway hostname.]
        return this.authenticationProvider.getGatewayHostname();
    }

    /**
     * Getter for the device ID.
     *
     * @return the device ID.
     */
    public String getDeviceId()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_003: [The function shall return the device ID given in the constructor.]
        return this.authenticationProvider.getDeviceId();
    }

    public String getModuleId()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_050: [This function return the saved moduleId.]
        return this.authenticationProvider.getModuleId();
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
            // Codes_SRS_DEVICECLIENTCONFIG_34_010: [If the inputName is null, or the message callbacks map does not
            // contain the provided inputName, this function shall return the default message callback.]
            return this.defaultDeviceTelemetryMessageCallback;
        }
        else
        {
            // Codes_SRS_DEVICECLIENTCONFIG_34_045: [If the message callbacks map contains the provided inputName, this function
            // shall return the callback associated with that inputName.]
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
            // Codes_SRS_DEVICECLIENTCONFIG_34_011: [If the inputName is null, or the message callbacks map does not
            // contain the provided inputName, this function shall return the default message callback context.]
            return this.defaultDeviceTelemetryMessageContext;
        }
        else
        {
            // Codes_SRS_DEVICECLIENTCONFIG_34_046: [If the message callbacks map contains the provided inputName, this function
            // shall return the context associated with that inputName.]
            return this.inputChannelMessageCallbacks.get(inputName).getValue();
        }
    }

    /**
     * Setter for the device method message callback.
     *
     * @param callback Callback for device method messages.
     * @param context is the context for the callback.
     */
    public void setDeviceMethodsMessageCallback(MessageCallback callback, Object context)
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceMethod message callback.**] **
         */
        this.deviceMethodsMessageCallback = callback;

        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **
         */
        this.deviceMethodsMessageContext = context;
    }

    /**
     * Getter for the device twin message callback.
     *
     * @return the device method message callback.
     */
    public MessageCallback getDeviceMethodsMessageCallback()
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_021: [**The function shall return the current DeviceMethod message callback.**] **
         */
        return this.deviceMethodsMessageCallback;
    }

    /**
     * Getter for the context to be passed in to the device twin message callback.
     *
     * @return the device method message context.
     */
    public Object getDeviceMethodsMessageContext()
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **
         */
        return this.deviceMethodsMessageContext;
    }

    /**
     * Setter for the device twin message callback.
     *
     * @param callback callback to be invoked for device twin messages.
     * @param context is the context for the callback.
     */
    public void setDeviceTwinMessageCallback(MessageCallback callback, Object context)
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceTwin message callback.**] **
         */
        this.deviceTwinMessageCallback = callback;
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_024: [**The function shall set the DeviceTwin message context.**] **
         */
        this.deviceTwinMessageContext = context;
    }

    /**
     * Getter for the device twin message callback.
     *
     * @return the device twin message callback.
     */
    public MessageCallback getDeviceTwinMessageCallback()
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_025: [**The function shall return the current DeviceTwin message callback.**] **
         */
        return this.deviceTwinMessageCallback;
    }

    /**
     * Getter for the context to be passed in to the device twin message callback.
     *
     * @return the device twin message context.
     */
    public Object getDeviceTwinMessageContext()
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_026: [**The function shall return the current DeviceTwin message context.**] **
         */
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
        // Codes_SRS_DEVICECLIENTCONFIG_11_013: [The function shall return 180s.]
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
            // Codes_SRS_DEVICECLIENTCONFIG_34_051: [If the saved authentication provider uses sas tokens, this function return AuthType.SAS_TOKEN.]
            return AuthType.SAS_TOKEN;
        }
        else
        {
            // Codes_SRS_DEVICECLIENTCONFIG_34_052: [If the saved authentication provider uses x509, this function return AuthType.X509_CERTIFICATE.]
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
            //Codes_SRS_DEVICECLIENTCONFIG_34_030: [If the provided timeout is 0 or negative, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Operation timeout cannot be 0 or negative");
        }

        //Codes_SRS_DEVICECLIENTCONFIG_34_031: [This function shall save the provided operation timeout.]
        this.operationTimeout = timeout;
    }

    /**
     * Getter for the device operation timeout
     * @return the amount of time, in milliseconds, before any device operation expires
     */
    public long getOperationTimeout()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_34_032: [This function shall return the saved operation timeout.]
        return this.operationTimeout;
    }

    public ProductInfo getProductInfo()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_34_040: [This function shall return the saved product info.]
        return this.productInfo;
    }

    public void setProxy(ProxySettings proxySettings)
    {
        this.proxySettings = proxySettings;
    }

    public ProxySettings getProxySettings()
    {
        return this.proxySettings;
    }

    @SuppressWarnings("unused")
    protected DeviceClientConfig()
    {
        this.authenticationProvider = null;
        this.deviceMethodsMessageCallback = null;
        this.defaultDeviceTelemetryMessageCallback = null;
        this.deviceTwinMessageCallback = null;
        this.deviceMethodsMessageContext = null;
        this.defaultDeviceTelemetryMessageContext = null;
        this.deviceTwinMessageContext = null;
        this.useWebsocket = false;
    }
}
