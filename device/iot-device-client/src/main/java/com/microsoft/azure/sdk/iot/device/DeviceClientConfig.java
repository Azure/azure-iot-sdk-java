// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;

import java.io.IOException;

/**
 * Configuration settings for an IoT Hub client. Validates all user-defined
 * settings.
 */
public final class DeviceClientConfig
{
    /** The default value for readTimeoutMillis. */
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 240000;
    /** The default value for messageLockTimeoutSecs. */
    private static final int DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS = 180;

    private boolean useWebsocket;

    private IotHubX509AuthenticationProvider x509Authentication;
    private IotHubSasTokenAuthenticationProvider sasTokenAuthentication;

    /* information in the connection string that unique identify the device */
    private IotHubConnectionString iotHubConnectionString;

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
    private MessageCallback deviceTelemetryMessageCallback;
    /** The context to be passed in to the message callback. */
    private Object deviceTelemetryMessageContext;

    private CustomLogger logger;

    public enum AuthType
    {
        X509_CERTIFICATE,
        SAS_TOKEN
    }

    private AuthType authenticationType;

    /**
     * Constructor
     *
     * @param iotHubConnectionString is the string with the hostname, deviceId, and
     *                               deviceKey or token, which identify the device in
     *                               the Azure IotHub.
     * @param authType is the authentication type to be used
     *
     * @throws IllegalArgumentException if the IoT Hub hostname does not contain
     * a valid IoT Hub name as its prefix.
     */
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, AuthType authType) throws IllegalArgumentException
    {
        // Codes_SRS_DEVICECLIENTCONFIG_21_034: [If the provided `iotHubConnectionString` is null,
        // the constructor shall throw IllegalArgumentException.]
        if(iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("connection string cannot be null");
        }

        if (iotHubConnectionString.isUsingX509())
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_076: [If the provided `iotHubConnectionString` uses x509 authentication, the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Cannot use this constructor for x509 connection strings. Use constructor that takes public key certificate and private key instead");
        }

        if (authType == AuthType.X509_CERTIFICATE)
        {
            // Codes_SRS_DEVICECLIENTCONFIG_12_002: [If the authentication type is X509 the constructor shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Cannot use this constructor for x509 authentication type. Use constructor that takes public key certificate and private key instead");
        }

        this.useWebsocket = false;

        // Codes_SRS_DEVICECLIENTCONFIG_12_001: [The constructor shall set the authentication type to the given authType value.]
        this.authenticationType = authType;
        this.iotHubConnectionString = iotHubConnectionString;

        this.sasTokenAuthentication = new IotHubSasTokenSoftwareAuthenticationProvider(
                this.iotHubConnectionString.getHostName(),
                this.iotHubConnectionString.getDeviceId(),
                this.iotHubConnectionString.getSharedAccessKey(),
                this.iotHubConnectionString.getSharedAccessToken());

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClientConfig object is created successfully with IotHubName=%s, deviceID=%s , method name is %s ",
                iotHubConnectionString.getHostName(), iotHubConnectionString.getDeviceId(), logger.getMethodName());
    }

    /**
     * Constructor for device configs that use x509 authentication
     *
     * @param iotHubConnectionString The connection string for the device. (format: "HostName=...;DeviceId=...;x509=true")
     * @param publicKeyCertificate The PEM encoded public key certificate or the path to the PEM encoded public key certificate file
     * @param isPathForPublic If the provided publicKeyCertificate is a path to the actual public key certificate
     * @param privateKey The PEM encoded private key or the path to the PEM encoded private key file
     * @param isPathForPrivate If the provided privateKey is a path to the actual private key
     */
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate)
    {
        if(iotHubConnectionString == null)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_069: [If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("connection string cannot be null");
        }

        if (!iotHubConnectionString.isUsingX509())
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_069: [If the provided connection string is null or does not use x509 auth, and IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Cannot use this constructor for connection strings that don't use x509 authentication.");
        }

        this.authenticationType = AuthType.X509_CERTIFICATE;
        this.useWebsocket = false;
        this.iotHubConnectionString = iotHubConnectionString;

        //Codes_SRS_DEVICECLIENTCONFIG_34_069: [This function shall generate a new SSLContext and set this to using X509 authentication.]
        this.x509Authentication = new IotHubX509SoftwareAuthenticationProvider(publicKeyCertificate, isPathForPublic, privateKey, isPathForPrivate);

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClientConfig object is created successfully with IotHubName=%s, deviceID=%s , method name is %s ",
                iotHubConnectionString.getHostName(), iotHubConnectionString.getDeviceId(), logger.getMethodName());
    }

    /**
     * Constructor for a device client config that retrieves the authentication method from a security provider instance
     * @param connectionString The connection string for the iot hub to connect with
     * @param securityProvider The security provider instance to be used for authentication of this device
     * @throws IOException if the provided security provider throws an exception while authenticating
     */
    DeviceClientConfig(IotHubConnectionString connectionString, SecurityProvider securityProvider) throws IOException
    {
        if (connectionString == null)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided connection string cannot be null");
        }

        if (securityProvider == null)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_080: [If the provided connectionString or security provider is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("security provider cannot be null");
        }

        if (securityProvider instanceof SecurityProviderTpm)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_083: [If the provided security provider is a SecurityProviderTpm instance, this function shall set its auth type to SAS and create its IotHubSasTokenAuthenticationProvider instance using the security provider.]
            this.authenticationType = AuthType.SAS_TOKEN;
            this.sasTokenAuthentication = new IotHubSasTokenHardwareAuthenticationProvider(connectionString.getHostName(), connectionString.getDeviceId(), securityProvider);
        }
        else if (securityProvider instanceof SecurityProviderX509)
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_082: [If the provided security provider is a SecurityProviderX509 instance, this function shall set its auth type to X509 and create its IotHubX509AuthenticationProvider instance using the security provider's ssl context.]
            this.authenticationType = AuthType.X509_CERTIFICATE;
            this.x509Authentication = new IotHubX509HardwareAuthenticationProvider(securityProvider);
        }
        else
        {
            //Codes_SRS_DEVICECLIENTCONFIG_34_084: [If the provided security provider is neither a SecurityProviderX509 instance nor a SecurityProviderTpm instance, this function shall throw an UnsupportedOperationException.]
            throw new UnsupportedOperationException("The provided security provider is not supported.");
        }

        this.useWebsocket = false;

        //Codes_SRS_DEVICECLIENTCONFIG_34_081: [This constructor shall save the provided connection string.]
        this.iotHubConnectionString = connectionString;

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClientConfig object is created successfully with IotHubName=%s, deviceID=%s , method name is %s ",
                connectionString.getHostName(), connectionString.getDeviceId(), logger.getMethodName());
    }

    /**
     * Getter for X509Authentication
     *
     * @return The value of X509Authentication
     */
    public IotHubX509AuthenticationProvider getX509Authentication()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_077: [This function shall return the saved IotHubX509AuthenticationProvider object.]
        return this.x509Authentication;
    }

    /**
     * Getter for SasTokenAuthentication
     *
     * @return The value of SasTokenAuthentication
     */
    public IotHubSasTokenAuthenticationProvider getSasTokenAuthentication()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_078: [This function shall return the saved IotHubSasTokenAuthenticationProvider object.]
        return this.sasTokenAuthentication;
    }

    /**
     * Getter for IotHubConnectionString
     *
     * @return The value of IotHubConnectionString
     */
    public IotHubConnectionString getIotHubConnectionString()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_34_079: [This function shall return the saved IotHubConnectionString object.]
        return this.iotHubConnectionString;
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
    public void setMessageCallback(MessageCallback callback,
                                   Object context)
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_006: [The function shall set the message callback, with its associated context.]
        this.deviceTelemetryMessageCallback = callback;
        this.deviceTelemetryMessageContext = context;
    }

    /**
     * Getter for the IoT Hub hostname.
     * @return the IoT Hub hostname.
     */
    public String getIotHubHostname()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
        return this.iotHubConnectionString.getHostName();
    }

    /**
     * Getter for the IoT Hub name.
     * @return the IoT Hub name.
     */
    public String getIotHubName()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_007: [The function shall return the IoT Hub name given in the constructor, where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
        return this.iotHubConnectionString.getHubName();
    }

    /**
     * Getter for the device ID.
     *
     * @return the device ID.
     */
    public String getDeviceId()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_003: [The function shall return the device ID given in the constructor.]
        return this.iotHubConnectionString.getDeviceId();
    }

    /**
     * Getter for the timeout, in milliseconds, after a connection is
     * established for the server to respond to the request.
     *
     * @return the timeout, in milliseconds, after a connection is established
     * for the server to respond to the request.
     */
    public int getReadTimeoutMillis()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_012: [The function shall return 240000ms.]
        return DEFAULT_READ_TIMEOUT_MILLIS;
    }

    /**
     * Getter for the message callback.
     *
     * @return the message callback.
     */
    public MessageCallback getDeviceTelemetryMessageCallback()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_010: [The function shall return the current message callback.]
        return this.deviceTelemetryMessageCallback;
    }

    /**
     * Getter for the context to be passed in to the message callback.
     *
     * @return the message context.
     */
    public Object getDeviceTelemetryMessageContext()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_011: [The function shall return the current message context.]
        return this.deviceTelemetryMessageContext;
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
        //Codes_SRS_DEVICECLIENTCONFIG_34_039: [This function shall return the type of authentication that the config is set up to use.]
        return authenticationType;
    }

    @SuppressWarnings("unused")
    protected DeviceClientConfig()
    {
        this.sasTokenAuthentication = null;
        this.x509Authentication = null;
        this.iotHubConnectionString = null;
        this.deviceMethodsMessageCallback = null;
        this.deviceTelemetryMessageCallback = null;
        this.deviceTwinMessageCallback = null;
        this.deviceMethodsMessageContext = null;
        this.deviceTelemetryMessageContext = null;
        this.deviceTwinMessageContext = null;
        this.logger = null;
        this.useWebsocket = false;
    }
}
