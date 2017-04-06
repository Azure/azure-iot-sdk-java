// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.net.URISyntaxException;

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
    public static final int DEFAULT_READ_TIMEOUT_MILLIS = 240000;
    /** The default value for messageLockTimeoutSecs. */
    public static final int DEFAULT_MESSAGE_LOCK_TIMEOUT_SECS = 180;

    protected final String iotHubHostname;
    protected final String iotHubName;
    protected final String deviceId;
    protected final String deviceKey;
    protected final String sharedAccessToken;

    /* Certificates related to IotHub */
    private String userCertificateString;
    private String pathToCertificate;
    private IotHubSSLContext iotHubSSLContext;

    /**
     * The callback to be invoked if a message of Device Method type received.
     */
    protected MessageCallback deviceMethodCallback;
    /** The context to be passed in to the device method type message callback. */
    protected Object deviceMethodMessageContext;

    /**
     * The callback to be invoked if a message of Device Twin type received.
     */
    protected MessageCallback deviceTwinMessageCallback;
    /** The context to be passed in to the device twin type message callback. */
    protected Object deviceTwinMessageContext;

    /**
     * The callback to be invoked if a message is received.
     */
    protected MessageCallback messageCallback;
    /** The context to be passed in to the message callback. */
    protected Object messageContext;

    protected CustomLogger logger;
    /**
     * Constructor.
     *
     * @param iotHubHostname the IoT Hub hostname.
     * @param deviceId the device ID.
     * @param deviceKey the device key.
     * @param sharedAccessToken the shared access token.
     *
     *
     * @throws URISyntaxException if the IoT Hub hostname does not conform to RFC 3986.
     * @throws IllegalArgumentException if the IoT Hub hostname does not contain
     * a valid IoT Hub name as its prefix.
     */
    public DeviceClientConfig(String iotHubHostname, String deviceId,
                              String deviceKey, String sharedAccessToken) throws URISyntaxException
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_014: [If the IoT Hub hostname is
        // not valid URI, the constructor shall throw a URISyntaxException.]
        new URI(iotHubHostname);

        // Codes_SRS_DEVICECLIENTCONFIG_11_015: [If the IoT Hub hostname does not contain a '.', the function shall throw an IllegalArgumentException.]
        int iotHubNameEndIdx = iotHubHostname.indexOf(".");
        if (iotHubNameEndIdx == -1)
        {
            String errStr = String.format(
                    "%s did not include a valid IoT Hub name as its prefix. "
                            + "An IoT Hub hostname has the following format: "
                            + "[iotHubName].[valid HTML chars]+",
                    iotHubHostname);
            throw new IllegalArgumentException(errStr);
        }

        // Codes_SRS_DEVICECLIENTCONFIG_11_001: [The constructor shall save the IoT Hub hostname, device ID, and device key.]
        this.iotHubHostname = iotHubHostname;
        this.iotHubName = iotHubHostname.substring(0, iotHubNameEndIdx);
        this.deviceId = deviceId;
        this.deviceKey = deviceKey;
        // Codes_SRS_DEVICECLIENTCONFIG_25_017: [**The constructor shall save sharedAccessToken.**] **
        this.sharedAccessToken = sharedAccessToken;
        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClientConfig object is created successfully with IotHubName=%s, deviceID=%s , method name is %s ", this.iotHubName, this.deviceId, logger.getMethodName());
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
        this.messageCallback = callback;
        this.messageContext = context;
    }

    /**
     * Getter for the IoT Hub hostname.
     * @return the IoT Hub hostname.
     */
    public String getIotHubHostname()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub hostname given in the constructor.]
        return this.iotHubHostname;
    }

    /**
     * Getter for the IoT Hub name.
     * @return the IoT Hub name.
     */
    public String getIotHubName()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_002: [The function shall return the IoT Hub name given in the constructor, where the IoT Hub name is embedded in the IoT Hub hostname as follows: [IoT Hub name].[valid HTML chars]+.]
        return this.iotHubName;
    }

    /**
     * Getter for the device ID.
     *
     * @return the device ID.
     */
    public String getDeviceId()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_003: [The function shall return the device ID given in the constructor.]
        return this.deviceId;
    }

    /**
     * Getter for the device key.
     *
     * @return the device key.
     */
    public String getDeviceKey()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_004: [The function shall return the device key given in the constructor.]
        return this.deviceKey;
    }

    /**
     * Getter for the shared access signature.
     *
     * @return the shared access signature.
     */
    public String getSharedAccessToken()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_25_018: [**The function shall return the SharedAccessToken given in the constructor.**] **
        return this.sharedAccessToken;
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
     */
    public void setTokenValidSecs(long expiryTime)
    {
        // Codes_SRS_DEVICECLIENTCONFIG_25_016: [The function shall set the value of tokenValidSecs.]
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
    public MessageCallback getMessageCallback()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_010: [The function shall return the current message callback.]
        return this.messageCallback;
    }

    /**
     * Getter for the context to be passed in to the message callback.
     *
     * @return the message context.
     */
    public Object getMessageContext()
    {
        // Codes_SRS_DEVICECLIENTCONFIG_11_011: [The function shall return the current message context.]
        return this.messageContext;
    }

    /**
     * Setter for the device method message callback.
     * @param callback Callback for device method messages.
     *
     */
    public void setDeviceMethodMessageCallback(MessageCallback callback, Object context)
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_023: [**The function shall set the DeviceMethod message callback.**] **
         */
        this.deviceMethodCallback = callback;

        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **
         */
        this.deviceMethodMessageContext = context;
    }

    /**
     * Getter for the device twin message callback.
     *
     * @return the device method message callback.
     */
    public MessageCallback getDeviceMethodMessageCallback()
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_021: [**The function shall return the current DeviceMethod message callback.**] **
         */
        return this.deviceMethodCallback;
    }

    /**
     * Getter for the context to be passed in to the device twin message callback.
     *
     * @return the device method message context.
     */
    public Object getDeviceMethodMessageContext()
    {
        /*
        Codes_SRS_DEVICECLIENTCONFIG_25_022: [**The function shall return the current DeviceMethod message context.**] **
         */
        return this.deviceMethodMessageContext;
    }

    /**
     * Setter for the device twin message callback.
     * @param callback callback to be invoked for device twin messages.
     */
    public void setDeviceTwinMessageCallback(MessageCallback callback,Object context)
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



    protected DeviceClientConfig()
    {
        this.iotHubHostname = null;
        this.iotHubName = null;
        this.deviceId = null;
        this.deviceKey = null;
        this.sharedAccessToken = null;
        this.pathToCertificate = null;
        this.iotHubSSLContext = null;
    }

}
