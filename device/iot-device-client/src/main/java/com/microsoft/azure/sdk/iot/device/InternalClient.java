/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;

import java.io.IOError;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;

public class InternalClient
{
    static final String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    static final String SET_SEND_INTERVAL = "SetSendInterval";
    static final String SET_CERTIFICATE_PATH = "SetCertificatePath";
	static final String SET_CERTIFICATE_AUTHORITY = "SetCertificateAuthority";
    static final String SET_SAS_TOKEN_EXPIRY_TIME = "SetSASTokenExpiryTime";

    DeviceClientConfig config;
    DeviceIO deviceIO;

    private DeviceTwin twin;
    private DeviceMethod method;

    protected CustomLogger logger;

    InternalClient(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis)
    {
        /* Codes_SRS_INTERNALCLIENT_21_004: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.] */
        commonConstructorVerification(iotHubConnectionString, protocol);

        this.config = new DeviceClientConfig(iotHubConnectionString);
        this.config.setProtocol(protocol);

        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);

        this.logger = new CustomLogger(this.getClass());
    }

    InternalClient(IotHubAuthenticationProvider iotHubAuthenticationProvider, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis) throws IOException, TransportException
    {
        this.config = new DeviceClientConfig(iotHubAuthenticationProvider);
        this.config.setProtocol(protocol);
        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);
        this.logger = new CustomLogger(this.getClass());
    }

    InternalClient(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath, long sendPeriodMillis, long receivePeriodMillis) throws URISyntaxException
    {
        // Codes_SRS_INTERNALCLIENT_34_078: [If the connection string or protocol is null, this function shall throw an IllegalArgumentException.]
        commonConstructorVerification(iotHubConnectionString, protocol);

        // Codes_SRS_INTERNALCLIENT_34_079: [This function shall save a new config using the provided connection string, and x509 certificate information.]
        this.config = new DeviceClientConfig(iotHubConnectionString, publicKeyCertificate, isCertificatePath, privateKey, isPrivateKeyPath);
        this.config.setProtocol(protocol);

        // Codes_SRS_INTERNALCLIENT_34_080: [This function shall save a new DeviceIO instance using the created config and the provided send/receive periods.]
        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);

        this.logger = new CustomLogger(this.getClass());
    }

    InternalClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis) throws URISyntaxException, IOException
    {
        if (protocol == null)
        {
            //Codes_SRS_INTERNALCLIENT_34_072: [If the provided protocol is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("The transport protocol cannot be null");
        }

        if (securityProvider == null)
        {
            //Codes_SRS_INTERNALCLIENT_34_073: [If the provided securityProvider is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("securityProvider cannot be null");
        }

        if (uri == null || uri.isEmpty())
        {
            //Codes_SRS_INTERNALCLIENT_34_074: [If the provided uri is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("URI cannot be null or empty");
        }

        if (deviceId == null || deviceId.isEmpty())
        {
            //Codes_SRS_INTERNALCLIENT_34_075: [If the provided deviceId is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        //Codes_SRS_INTERNALCLIENT_34_065: [The provided uri and device id will be used to create an iotHubConnectionString that will be saved in config.]
        IotHubConnectionString connectionString = new IotHubConnectionString(uri, deviceId, null, null);

        //Codes_SRS_INTERNALCLIENT_34_066: [The provided security provider will be saved in config.]
        this.config = new DeviceClientConfig(connectionString, securityProvider);
        this.config.setProtocol(protocol);

        //Codes_SRS_INTERNALCLIENT_34_067: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);

        this.logger = new CustomLogger(this.getClass());
    }

    //unused
    InternalClient()
    {
        // Codes_SRS_INTERNALCLIENT_12_028: [The constructor shall shall set the config, deviceIO and tranportClient to null.]
        this.config = null;
        this.deviceIO = null;
    }

    public void open() throws IOException
    {
        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.config.getSasTokenAuthentication().isRenewalNecessary())
        {
            //Codes_SRS_INTERNALCLIENT_34_044: [If the SAS token has expired before this call, throw a Security Exception]
            throw new SecurityException("Your SasToken is expired");
        }

        //Codes_SRS_INTERNALCLIENT_21_006: [The open shall open the deviceIO connection.]
        this.deviceIO.open();
    }

    public void close() throws IOException
    {
        while (!this.deviceIO.isEmpty())
        {
            // Don't do anything, can be infinite.
        }

        //Codes_SRS_INTERNALCLIENT_21_042: [The closeNow shall closeNow the deviceIO connection.]
        this.deviceIO.close();
    }

    public void closeNow() throws IOException
    {
        //Codes_SRS_INTERNALCLIENT_21_008: [The closeNow shall closeNow the deviceIO connection.]
        this.deviceIO.close();
    }

    /**
     * Asynchronously sends an event message to the IoT Hub.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is
     * already closed.
     */
    public void sendEventAsync(Message message, IotHubEventCallback callback, Object callbackContext)
    {
        //Codes_SRS_INTERNALCLIENT_34_045: [This function shall set the provided message's connection device id to the config's saved device id.]
        message.setConnectionDeviceId(this.config.getDeviceId());

        //Codes_SRS_INTERNALCLIENT_21_010: [The sendEventAsync shall asynchronously send the message using the deviceIO connection.]
        deviceIO.sendEventAsync(message, callback, callbackContext, this.config.getDeviceId());
    }

    /**
     * Subscribes to desired properties
     *
     * @param onDesiredPropertyChange the Map for desired properties and their corresponding callback and context. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     */
    public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException
    {
        if (this.twin == null)
        {
            //Codes_SRS_INTERNALCLIENT_25_029: [If the client has not started twin before calling this method, the function shall throw an IOException.]
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            //Codes_SRS_INTERNALCLIENT_25_030: [If the client has not been open, the function shall throw an IOException.]
            throw new IOException("Open the client connection before using it.");
        }

        //Codes_SRS_INTERNALCLIENT_25_031: [This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.]
        this.twin.subscribeDesiredPropertiesNotification(onDesiredPropertyChange);
    }

    /**
     * Subscribes to desired properties
     *
     * @param onDesiredPropertyChange the Map for desired properties and their corresponding callback and context. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     */
    public void subscribeToTwinDesiredProperties(Map<Property, Pair<TwinPropertyCallBack, Object>> onDesiredPropertyChange) throws IOException
    {
        if (this.twin == null)
        {
            //Codes_SRS_INTERNALCLIENT_34_087: [If the client has not started twin before calling this method, the function shall throw an IOException.]
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            //Codes_SRS_INTERNALCLIENT_34_086: [If the client has not been open, the function shall throw an IOException.]
            throw new IOException("Open the client connection before using it.");
        }

        //Codes_SRS_INTERNALCLIENT_34_085: [This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.]
        this.twin.subscribeDesiredPropertiesTwinPropertyNotification(onDesiredPropertyChange);
    }

    /**
     * Sends reported properties
     *
     * @param reportedProperties the Set for desired properties and their corresponding callback and context. Cannot be {@code null}.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     * @throws IllegalArgumentException if reportedProperties is null or empty.
     */
    public void sendReportedProperties(Set<Property> reportedProperties) throws IOException, IllegalArgumentException
    {
        if (this.twin == null)
        {
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (reportedProperties == null || reportedProperties.isEmpty())
        {
            throw new IllegalArgumentException("Reported properties set cannot be null or empty.");
        }

        this.twin.updateReportedProperties(reportedProperties);
    }

    /**
     * Sends reported properties
     *
     * @param reportedProperties the Set for desired properties and their corresponding callback and context. Cannot be {@code null}.
     * @param version the Reported property version. Cannot be negative.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     * @throws IllegalArgumentException if reportedProperties is null or empty.
     */
    public void sendReportedProperties(Set<Property> reportedProperties, int version) throws IOException, IllegalArgumentException
    {
        if (this.twin == null)
        {
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (reportedProperties == null || reportedProperties.isEmpty())
        {
            throw new IllegalArgumentException("Reported properties set cannot be null or empty.");
        }

        if(version < 0)
        {
            throw new IllegalArgumentException("Version cannot be null.");
        }

        this.twin.updateReportedProperties(reportedProperties, version);
    }

    /**
     * Registers a callback to be executed when the connection status of the device changes. The callback will be fired
     * with a status and a reason why the device's status changed. When the callback is fired, the provided context will
     * be provided alongside the status and reason.
     *
     * @param callback The callback to be fired when the connection status of the device changes. Can be null to
     *                 unset this listener as long as the provided callbackContext is also null.
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     * @throws IllegalArgumentException if provided callback is null
     */
    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext) throws IllegalArgumentException
    {
        //Codes_SRS_INTERNALCLIENT_34_069: [This function shall register the provided callback and context with its device IO instance.]
        this.deviceIO.registerConnectionStatusChangeCallback(callback, callbackContext);
    }

    /**
     * Sets the given retry policy on the underlying transport
     * <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/retryPolicy.md">
     *     See more details about the default retry policy and about using custom retry policies here</a>
     * @param retryPolicy the new interval in milliseconds
     */
    public void setRetryPolicy(RetryPolicy retryPolicy)
    {
        //Codes_SRS_INTERNALCLIENT_28_001: [The function shall set the device config's RetryPolicy .]
        this.config.setRetryPolicy(retryPolicy);
    }

    /**
     * Set the length of time, in milliseconds, that any given operation will expire in. These operations include
     * reconnecting upon a connection drop and sending a message.
     * @param timeout the length in time, in milliseconds, until a given operation shall expire
     * @throws IllegalArgumentException if the provided timeout is 0 or negative
     */
    public void setOperationTimeout(long timeout) throws IllegalArgumentException
    {
        // Codes_SRS_INTERNALCLIENT_34_070: [The function shall set the device config's operation timeout .]
        this.config.setOperationTimeout(timeout);
    }

    public ProductInfo getProductInfo()
    {
        // Codes_SRS_INTERNALCLIENT_34_071: [This function shall return the product info saved in config.]
        return this.config.getProductInfo();
    }

    /**
     * Getter for the device client config.
     *
     * @return the value of the config.
     */
    public DeviceClientConfig getConfig()
    {
        return this.config;
    }

    /**
     * Sets a runtime option identified by parameter {@code optionName}
     * to {@code value}.
     *
     * The options that can be set via this API are:
     *	    - <b>SetMinimumPollingInterval</b> - this option is applicable only
     *	      when the transport configured with this client is HTTP. This
     *	      option specifies the interval in milliseconds between calls to
     *	      the service checking for availability of new messages. The value
     *	      is expected to be of type {@code long}.
     *	    - <b>SetCertificatePath</b> - this option is applicable only
     *	      when the transport configured with this client is AMQP. This
     *	      option specifies the path to the certificate used to verify peer.
     *	      The value is expected to be of type {@code String}.
     *      - <b>SetSASTokenExpiryTime</b> - this option is applicable for HTTP/
     *         AMQP/MQTT. This option specifies the interval in seconds after which
     *         SASToken expires. If the transport is already open then setting this
     *         option will restart the transport with the updated expiry time, and
     *         will use that expiry time length for all subsequently generated sas tokens.
     *         The value is expected to be of type {@code long}.
     *
     * @param optionName the option name to modify
     * @param value an object of the appropriate type for the option's value
     * @throws IllegalArgumentException if the provided optionName is null
     */
    public void setOption(String optionName, Object value)
    {
        if (optionName == null)
        {
            // Codes_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option handled by the client, then
            // it shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("optionName is null");
        }
        else if (value == null)
        {
            // Codes_SRS_DEVICECLIENT_12_026: [The function shall trow IllegalArgumentException if the value is null.]
            throw new IllegalArgumentException("optionName is null");
        }
        else
        {
            switch (optionName)
            {
                case SET_MINIMUM_POLLING_INTERVAL:
                {
                    if (this.deviceIO.isOpen())
                    {
                        throw new IllegalStateException("setOption " + SET_MINIMUM_POLLING_INTERVAL +
                                "only works when the transport is closed");
                    }
                    else
                    {
                        if (this.deviceIO.getProtocol() == IotHubClientProtocol.HTTPS)
                        {
                            setOption_SetMinimumPollingInterval(value);
                        }
                        else
                        {
                            throw new IllegalArgumentException("optionName is unknown = " + optionName + " for " + this.deviceIO.getProtocol().toString());
                        }
                    }

                    break;
                }
                case SET_SEND_INTERVAL:
                {
                    setOption_SetSendInterval(value);
                    break;
                }
                case SET_CERTIFICATE_PATH:
                {
                    if ((this.deviceIO != null) && (this.deviceIO.isOpen()))
                    {
                        throw new IllegalStateException("setOption " + SET_CERTIFICATE_PATH + " only works when the transport is closed");
                    }
                    else
                    {
                        if (this.deviceIO.getProtocol() != HTTPS)
                        {
                            // Codes_SRS_DEVICECLIENT_34_046: [If the option is SET_CERTIFICATE_PATH, and the saved
                            // protocol is not HTTPS, this function shall save the certificate path in config.]
                            setOption_SetCertificatePath(value);
                        }
                        else
                        {
                            // Codes_SRS_DEVICECLIENT_34_047: [If the option is SET_CERTIFICATE_PATH, and the saved
                            // protocol is HTTPS, this function shall throw an IllegalArgumentException.]
                            throw new IllegalArgumentException("option SetCertificatePath cannot be invoked when using HTTPS protocol");
                        }
                    }

                    break;
                }
                case SET_CERTIFICATE_AUTHORITY:
                {
                    if ((this.deviceIO != null) && (this.deviceIO.isOpen()))
                    {
                        throw new IllegalStateException("setOption " + SET_CERTIFICATE_PATH + " only works when the transport is closed");
                    }
                    else
                    {
                        setTrustedCertificates((String)value);
                    }

                    break;
                }
                case SET_SAS_TOKEN_EXPIRY_TIME:
                {
                    setOption_SetSASTokenExpiryTime(value);
                    break;
                }
                default:
                {
                    throw new IllegalArgumentException("optionName is unknown = " + optionName);
                }
            }
        }
    }

    /**
     * Starts the device twin.
     *
     * @param twinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param twinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallBack the PropertyCallBack callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.     *
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    void startTwinInternal(IotHubEventCallback twinStatusCallback, Object twinStatusCallbackContext,
                                 PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException

    {
        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (twinStatusCallback == null || genericPropertyCallBack == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        if (this.twin == null)
        {
            twin = new DeviceTwin(
                    this.deviceIO,
                    this.config,
                    twinStatusCallback,
                    twinStatusCallbackContext,
                    genericPropertyCallBack,
                    genericPropertyCallBackContext);

            twin.getDeviceTwin();
        }
        else
        {
            throw new UnsupportedOperationException("You have already initialised twin");
        }
    }

    /**
     * Starts the device twin.
     *
     * @param twinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param twinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallBack the TwinPropertyCallBack callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.     *
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     * @throws IllegalArgumentException if either callback is null
     */
    void startTwinInternal(IotHubEventCallback twinStatusCallback, Object twinStatusCallbackContext,
                                 TwinPropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        if (!this.deviceIO.isOpen())
        {
            //Codes_SRS_INTERNALCLIENT_34_081: [If device io has not been opened yet, this function shall throw an IOException.]
            throw new IOException("Open the client connection before using it.");
        }

        if (twinStatusCallback == null || genericPropertyCallBack == null)
        {
            //Codes_SRS_INTERNALCLIENT_34_082: [If either callback is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Callback cannot be null");
        }
        if (this.twin == null)
        {
            //Codes_SRS_INTERNALCLIENT_34_084: [This function shall initialize a DeviceTwin object and invoke getDeviceTwin on it.]
            twin = new DeviceTwin(this.deviceIO, this.config, twinStatusCallback, twinStatusCallbackContext,
                    genericPropertyCallBack, genericPropertyCallBackContext);
            twin.getDeviceTwin();
        }
        else
        {
            //Codes_SRS_INTERNALCLIENT_34_083: [If either callback is null, this function shall throw an IllegalArgumentException.]
            throw new UnsupportedOperationException("You have already initialised twin");
        }
    }

    /**
     * Get the current desired properties for this client
     * @throws IOException if the iot hub cannot be reached
     * @throws IOException if the twin has not been initialized yet
     * @throws IOException if the client has not been opened yet
     */
    void getTwinInternal() throws IOException
    {
        if (this.twin == null)
        {
            //Codes_SRS_INTERNALCLIENT_21_040: [If the client has not started twin before calling this method, the function shall throw an IOException.]
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {

            //Codes_SRS_INTERNALCLIENT_21_041: [If the client has not been open, the function shall throw an IOException.]
            throw new IOException("Open the client connection before using it.");
        }

        //Codes_SRS_INTERNALCLIENT_21_042: [The function shall get all desired properties by calling getDeviceTwin.]
        this.twin.getDeviceTwin();
    }

    /**
     * Sets the message callback.
     *
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed to the callback. Can be {@code null}.
     *
     * @throws IllegalArgumentException if the callback is {@code null} but a context is
     * passed in.
     * @throws IllegalStateException if the callback is set after the client is
     * closed.
     */
    void setMessageCallbackInternal(MessageCallback callback, Object context)
    {
        if (callback == null && context != null)
        {
            /* Codes_SRS_INTERNALCLIENT_11_014: [If the callback is null but the context is non-null, the function shall throw an IllegalArgumentException.] */
            throw new IllegalArgumentException("Cannot give non-null context for a null callback.");
        }

        /* Codes_SRS_INTERNALCLIENT_11_013: [The function shall set the message callback, with its associated context.] */
        this.config.setMessageCallback(callback, context);
    }

    /**
     * Subscribes to methods
     *
     * @param methodCallback Callback on which methods shall be invoked. Cannot be {@code null}.
     * @param methodCallbackContext Context for method callback. Can be {@code null}.
     * @param methodStatusCallback Callback for providing IotHub status for methods. Cannot be {@code null}.
     * @param methodStatusCallbackContext Context for method status callback. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened.
     * @throws IllegalArgumentException if either callback are null.
     */
    void subscribeToMethodsInternal(DeviceMethodCallback methodCallback, Object methodCallbackContext,
                                              IotHubEventCallback methodStatusCallback, Object methodStatusCallbackContext)
            throws IOException
    {
        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (methodCallback == null || methodStatusCallback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (this.method == null)
        {
            this.method = new DeviceMethod(this.deviceIO, this.config, methodStatusCallback, methodStatusCallbackContext);
        }

        this.method.subscribeToDeviceMethod(methodCallback, methodCallbackContext);
    }

    /**
     * Getter for the underlying DeviceIO for multiplexing scenarios.
     *
     * @return the value of the underlying DeviceIO.
     */
    DeviceIO getDeviceIO()
    {
        return this.deviceIO;
    }

    /**
     * Setter for the underlying DeviceIO for multiplexing scenarios.
     *
     * @param deviceIO is the DeviceIO to set.
     */
    void setDeviceIO(DeviceIO deviceIO)
    {
        this.deviceIO = deviceIO;
    }

    void setOption_SetCertificatePath(Object value)
    {
        if (value != null)
        {
            this.config.getAuthenticationProvider().setPathToIotHubTrustedCert((String) value);
        }
    }

    void setTrustedCertificates(String certificates)
    {
        this.config.getAuthenticationProvider().setIotHubTrustedCert(certificates);
    }

    void setOption_SetSendInterval(Object value)
    {
        if (value != null)
        {
            // Codes_SRS_DEVICECLIENT_21_041: ["SetSendInterval" needs to have value type long.]
            if (value instanceof Long)
            {
                try
                {
                    this.deviceIO.setSendPeriodInMilliseconds((long) value);
                }
                catch (IOException e)
                {
                    throw new IOError(e);
                }
            }
            else
            {
                throw new IllegalArgumentException("value is not long = " + value);
            }
        }
    }

    void setOption_SetMinimumPollingInterval(Object value)
    {
        if (value != null)
        {
            // Codes_SRS_DEVICECLIENT_02_018: ["SetMinimumPollingInterval" needs to have type long].
            if (value instanceof Long)
            {
                try
                {
                    this.deviceIO.setReceivePeriodInMilliseconds((long) value);
                }
                catch (IOException e)
                {
                    throw new IOError(e);
                }
            }
            else
            {
                throw new IllegalArgumentException("value is not long = " + value);
            }
        }
    }

    void setOption_SetSASTokenExpiryTime(Object value)
    {
        if (this.config.getAuthenticationType() != DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            throw new IllegalStateException("Cannot set sas token validity time when not using sas token authentication");
        }

        if (value != null)
        {
            long validTimeInSeconds;

            if (value instanceof Long)
            {
                validTimeInSeconds = (long) value;
            }
            else
            {
                throw new IllegalArgumentException("value is not long = " + value);
            }

            this.config.getSasTokenAuthentication().setTokenValidSecs(validTimeInSeconds);

            if (this.deviceIO != null)
            {
                if (this.deviceIO.isOpen())
                {
                    try
                    {
                        /* Codes_SRS_DEVICECLIENT_25_024: [**"SetSASTokenExpiryTime" shall restart the transport
                         *                                  1. If the device currently uses device key and
                         *                                  2. If transport is already open
                         *                                 after updating expiry time
                         */
                        if (this.config.getSasTokenAuthentication().canRefreshToken())
                        {
                            this.deviceIO.close();
                            this.deviceIO.open();
                        }
                    }
                    catch (IOException e)
                    {
                        // Codes_SRS_DEVICECLIENT_12_027: [The function shall throw IOError if either the deviceIO or the tranportClient's open() or closeNow() throws.]
                        throw new IOError(e);
                    }
                }
            }
        }
    }

    private void commonConstructorVerification(IotHubConnectionString connectionString, IotHubClientProtocol protocol)
    {
        if (connectionString == null)
        {
            throw new IllegalArgumentException("Connection string cannot be null");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("Protocol cannot be null.");
        }

        String gatewayHostName = connectionString.getGatewayHostName();
        if (gatewayHostName != null && !gatewayHostName.isEmpty() && protocol == HTTPS)
        {
            throw new UnsupportedOperationException("Communication with edgehub only supported by MQTT/MQTT_WS and AMQPS/AMQPS_WS");
        }
    }
}
