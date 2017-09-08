// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;

/**
 * Configuration settings for an IoT Hub client. Validates all user-defined
 * settings.
 */
public final class DeviceClientConfig
{
    /**
     * The number of seconds after which the generated SAS token for a message
     * will become invalid. We also use the expiry time, which is computed as
     * {@code currentTime() + DEVICE_KEY_VALID_LENGTH}, as a salt when generating our
     * SAS token. Use {@link #getTokenValidSecs()} instead in case the field becomes
     * configurable later on.
     */
    private long tokenValidSecs = 3600;

    /** The default value for readTimeoutMillis. */
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 240000;
    /** The default value for messageLockTimeoutSecs. */
    private static final int DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS = 180;

    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final long MINIMUM_EXPIRATION_TIME_OFFSET = 1L;

    /* information in the connection string that unique identify the device */
    private final IotHubConnectionString iotHubConnectionString;

    /* Certificates related to IotHub */
    private String userCertificateString;
    private String pathToCertificate;
    private IotHubSSLContext iotHubSSLContext;

    private boolean useWebsocket;

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

    /**
     * Constructor
     *
     * @param iotHubConnectionString is the string with the hostname, deviceId, and
     *                               deviceKey or token, which identify the device in
     *                               the Azure IotHub.
     * @throws IllegalArgumentException if the IoT Hub hostname does not contain
     * a valid IoT Hub name as its prefix.
     */
    public DeviceClientConfig(IotHubConnectionString iotHubConnectionString)
    {
        // Codes_SRS_DEVICECLIENTCONFIG_21_034: [If the provided `iotHubConnectionString` is null,
        // the constructor shall throw IllegalArgumentException.]
        if(iotHubConnectionString == null)
        {
            throw new IllegalArgumentException("connection string is null");
        }

        // Codes_SRS_DEVICECLIENTCONFIG_21_033: [The constructor shall save the IoT Hub hostname, hubname,
        // device ID, device key, and device token, provided in the `iotHubConnectionString`.]
        this.iotHubConnectionString = iotHubConnectionString;
        this.useWebsocket = false;

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClientConfig object is created successfully with IotHubName=%s, deviceID=%s , method name is %s ",
                this.iotHubConnectionString.getHostName(), this.iotHubConnectionString.getDeviceId(), logger.getMethodName());
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
     * Setter for the IotHub SSL Context
     * @param iotHubSSLContext IotHubSSLContext to be set
     */
    public void setIotHubSSLContext(IotHubSSLContext iotHubSSLContext)
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_031: [**The function shall set IotHub SSL Context**] **
        this.iotHubSSLContext = iotHubSSLContext;
    }

    /**
     * Getter for the IotHubSSLContext
     * @return IotHubSSLContext for this IotHub
     */
    public IotHubSSLContext getIotHubSSLContext()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_032: [**The function shall return the IotHubSSLContext.**] **
        return iotHubSSLContext;
    }

    /**
     * Setter for the providing trusted certificate.
     * @param pathToCertificate path to the certificate for one way authentication.
     */
    public void setPathToCert(String pathToCertificate)
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_028: [**The function shall set the path to the certificate**] **
        this.pathToCertificate = pathToCertificate;
    }

    /**
     * Getter for the path to the certificate.
     * @return the path to certificate.
     */
    public String getPathToCertificate()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_027: [**The function shall return the value of the path to the certificate.**] **
        return this.pathToCertificate;
    }

    /**
     * Setter for the user trusted certificate
     * @param userCertificateString valid user trusted certificate string
     */
    public void setUserCertificateString(String userCertificateString)
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_029: [**The function shall set user certificate String**] **
        this.userCertificateString = userCertificateString;
    }

    /**
     * Getter for the user trusted certificate
     * @return user trusted certificate as string
     */
    public String getUserCertificateString()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_25_030: [**The function shall return the value of the user certificate string.**] **
        return userCertificateString;
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
     * Getter for the device key.
     *
     * @return the device key.
     */
    public String getDeviceKey()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_004: [The function shall return the device key given in the constructor.]
        return this.iotHubConnectionString.getSharedAccessKey();
    }

    /**
     * Getter for the shared access signature.
     *
     * @return the shared access signature.
     */
    public String getSharedAccessToken() throws SecurityException
    {
        String currentToken = this.iotHubConnectionString.getSharedAccessToken();

        if (currentToken == null || (this.getDeviceKey() != null && IotHubSasToken.isSasTokenExpired(currentToken)))
        {
            Long expiryTime = (System.currentTimeMillis() / MILLISECONDS_PER_SECOND) + this.getTokenValidSecs() + MINIMUM_EXPIRATION_TIME_OFFSET;
            IotHubSasToken generatedSasToken = new IotHubSasToken(this, expiryTime);

            //Codes_SRS_DEVICECLIENTCONFIG_34_036: [If this function generates the returned SharedAccessToken from a device key, the previous SharedAccessToken shall be overwritten with the generated value.]
            this.iotHubConnectionString.setSharedAccessToken(generatedSasToken.toString());

            return generatedSasToken.toString();
        }

        // Codes_SRS_DEVICECLIENTCONFIG_25_018: [**The function shall return the SharedAccessToken given in the constructor.**] **
        return this.iotHubConnectionString.getSharedAccessToken();
    }

    /**
     * Getter for the number of seconds a SAS token should be valid for. A
     * message that arrives at an IoT Hub in time of length greater than this
     * value will be rejected by the IoT Hub.
     *
     * @return the number of seconds a message in transit to an IoT Hub is valid
     * for.
     */
    public long getTokenValidSecs()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_005: [The function shall return the value of tokenValidSecs.]
        return this.tokenValidSecs;
    }

    /**
     * Setter for the number of seconds a SAS token should be valid for. A
     * message that arrives at an IoT Hub in time of length greater than this
     * value will be rejected by the IoT Hub.
     *
     * @param expiryTime is the token valid time in seconds.
     */
    public void setTokenValidSecs(long expiryTime)
    {
        // Codes_SRS_DEVICECLIENTCONFIG_25_008: [The function shall set the value of tokenValidSecs.]
        this.tokenValidSecs = expiryTime;
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
     * Tells if the config needs to get a new sas token. If a device key is present in config, no token refresh is needed.
     * @return if the config needs a new sas token.
     */
    public boolean needsToRenewSasToken()
    {
        //Codes_SRS_DEVICECLIENTCONFIG_34_035: [If the saved sas token has expired and there is no device key present, this function shall return true.]
        String token = this.getSharedAccessToken();
        return (token != null && IotHubSasToken.isSasTokenExpired(token) && this.getDeviceKey() == null);
    }

    @SuppressWarnings("unused")
    protected DeviceClientConfig()
    {
        this.iotHubConnectionString = null;
        this.pathToCertificate = null;
        this.iotHubSSLContext = null;
    }
}
