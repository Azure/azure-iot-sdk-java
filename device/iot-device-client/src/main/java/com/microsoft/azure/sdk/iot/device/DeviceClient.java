// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.transport.amqps.IoTHubConnectionType;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * <p>
 * The public-facing API. Allows a single logical or physical device to connect
 * to an IoT Hub. The IoT Hub client supports sending events to and receiving
 * messages from an IoT Hub.
 * </p>
 * <p>
 * To support these workflows, the client library will provide the following
 * abstractions: a message, with its payload and associated properties; and a
 * client, which sends and receives messages.
 * </p>
 * <p>
 * The client buffers messages while the network is down, and re-sends them when
 * the network comes back online. It also batches messages to improve
 * communication efficiency (HTTPS only).
 * </p>
 * The client supports HTTPS 1.1 and AMQPS 1.0 transports.
 */
public final class DeviceClient extends InternalClient implements Closeable
{
    /**
     * @deprecated as of release 1.2.27 this value is deprecated and will not be replaced.
     * The hostname attribute name in a connection string.
     */
    @Deprecated
    public static final String HOSTNAME_ATTRIBUTE = "HostName=";
    /**
     * @deprecated as of release 1.2.27 this value is deprecated and will not be replaced.
     * The device ID attribute name in a connection string.
     */
    @Deprecated
    public static final String DEVICE_ID_ATTRIBUTE = "DeviceId=";
    /**
     * @deprecated as of release 1.2.27 this value is deprecated and will not be replaced.
     * The shared access key attribute name in a connection string.
     */
    @Deprecated
    public static final String SHARED_ACCESS_KEY_ATTRIBUTE = "SharedAccessKey=";
    /**
     * @deprecated as of release 1.2.27 this value is deprecated and will not be replaced.
     * The shared access signature attribute name in a connection string.
     */
    @Deprecated
    public static final String SHARED_ACCESS_TOKEN_ATTRIBUTE = "SharedAccessSignature=";

    /**
     * @deprecated as of release 1.2.27 this value is deprecated and will not be replaced.
     * The charset used for URL-encoding the device ID in the connection
     * string.
     */
    @Deprecated
    public static final Charset CONNECTION_STRING_CHARSET = StandardCharsets.UTF_8;

    /**
     * @deprecated as of release 1.2.27 this value is deprecated and replaced by
     * {@link #setOption(String, Object)} <b>SetSendInterval</b> to change it.
     *
     * The number of milliseconds the transport will wait between
     * sending out messages.
     */
    @Deprecated
    public static long SEND_PERIOD_MILLIS = 10L;

    /**
     * @deprecated as of release 1.2.27 these value is deprecated and replaced by
     * {@link #setOption(String, Object)} <b>SetMinimumPollingInterval</b> to change it.
     *
     * The number of milliseconds the transport will wait between
     * polling for messages.
     */
    @Deprecated
    public static long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    @Deprecated
    public static long RECEIVE_PERIOD_MILLIS_MQTT = 10L;
    @Deprecated
    public static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    protected long RECEIVE_PERIOD_MILLIS;

    private IoTHubConnectionType ioTHubConnectionType = IoTHubConnectionType.UNKNOWN;

    private TransportClient transportClient;

    private FileUpload fileUpload;

    /**
     * Constructor that takes a connection string and a transport client as an argument.
     *
     * @param connString the connection string. The connection string is a set
     * of key-value pairs that are separated by ';', with the keys and values
     * separated by '='. It should contain values for the following keys:
     * {@code HostName}, {@code DeviceId}, and {@code SharedAccessKey}.
     *
     * @param transportClient the transport client to use by the device client.
     *
     * @throws IllegalArgumentException if {@code connString}
     * or if {@code connString} is missing one of the following
     * attributes:{@code HostName}, {@code DeviceId}, or
     * {@code SharedAccessKey} or if the IoT hub hostname does not conform to
     * RFC 3986 or if the provided {@code connString} is for an x509 authenticated device
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     * @throws UnsupportedOperationException if the connection string belongs to a module rather than a device
     */
    public DeviceClient(String connString, TransportClient transportClient) throws URISyntaxException, IllegalArgumentException, UnsupportedOperationException
    {
        // Codes_SRS_DEVICECLIENT_12_009: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
        this.config = new DeviceClientConfig(new IotHubConnectionString(connString));
        this.deviceIO = null;

        // Codes_SRS_DEVICECLIENT_12_018: [If the transportClient is null, the function shall throw an IllegalArgumentException.]
        if (transportClient == null)
        {
            throw new IllegalArgumentException("Transport client cannot be null.");
        }

        // Codes_SRS_DEVICECLIENT_12_010: [The constructor shall set the connection type to USE_TRANSPORTCLIENT.]
        this.ioTHubConnectionType = IoTHubConnectionType.USE_TRANSPORTCLIENT;

        // Codes_SRS_DEVICECLIENT_12_016: [The constructor shall save the transportClient parameter.]
        this.transportClient = transportClient;

        String moduleId = this.getConfig().getModuleId();
        if (!(moduleId == null || moduleId.isEmpty()))
        {
            // Codes_SRS_DEVICECLIENT_34_073: [If this constructor is called with a connection string that contains a moduleId, this function shall throw an UnsupportedOperationException.]
            throw new UnsupportedOperationException("Multiplexing with module connection strings is not supported");
        }

        this.getConfig().setProtocol(this.transportClient.getIotHubClientProtocol());

        // Codes_SRS_DEVICECLIENT_12_017: [The constructor shall register the device client with the transport client.]
        this.transportClient.registerDeviceClient(this);

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClient object is created successfully, method name is %s ", logger.getMethodName());
    }

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param connString the connection string. The connection string is a set
     * of key-value pairs that are separated by ';', with the keys and values
     * separated by '='. It should contain values for the following keys:
     * {@code HostName}, {@code DeviceId}, and {@code SharedAccessKey}.
     * @param protocol the communication protocol used (i.e. HTTPS).
     *
     * @throws IllegalArgumentException if any of {@code connString} or
     * {@code protocol} are {@code null}; or if {@code connString} is missing
     * one of the following attributes:{@code HostName}, {@code DeviceId}, or
     * {@code SharedAccessKey} or if the IoT hub hostname does not conform to
     * RFC 3986 or if the provided {@code connString} is for an x509 authenticated device
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException, IllegalArgumentException
    {
        // Codes_SRS_DEVICECLIENT_21_001: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
        super(new IotHubConnectionString(connString), protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));

        // Codes_SRS_DEVICECLIENT_34_075: [If the provided connection string contains a module id field, this function shall throw an UnsupportedOperationException.]
        commonConstructorVerifications();

        // Codes_SRS_DEVICECLIENT_12_012: [The constructor shall set the connection type to SINGLE_CLIENT.]
        // Codes_SRS_DEVICECLIENT_12_015: [The constructor shall set the transportClient to null.]
        commonConstructorSetup();
    }

    /**
     * Constructor that uses x509 authentication for communicating with IotHub
     *
     * @param connString the connection string for the x509 device to connect as (format: "HostName=...;DeviceId=...;x509=true")
     * @param protocol the protocol to use when communicating with IotHub
     * @param publicKeyCertificate the PEM formatted public key certificate or the path to a PEM formatted public key certificate file.
     *                             Append if there is any intermediate or chained certificates to the end of the public certificate file in the following format:
     *                             -----BEGIN CERTIFICATE-----
     *                             (Primary SSL certificate)
     *                             -----END CERTIFICATE-----
     *                             ----BEGIN CERTIFICATE-----
     *                             (Intermediate certificate)
     *                             -----END CERTIFICATE-----
     *                             -----BEGIN CERTIFICATE-----
     *                             (Root certificate)
     *                             -----END CERTIFICATE-----
     * @param isCertificatePath if the provided publicKeyCertificate is a path to a file containing the PEM formatted public key certificate
     * @param privateKey the PEM formatted private key or the path to a PEM formatted private key file
     * @param isPrivateKeyPath if the provided privateKey is a path to a file containing the PEM formatted private key
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    public DeviceClient(String connString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws URISyntaxException
    {
        // Codes_SRS_DEVICECLIENT_34_058: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
        // Codes_SRS_DEVICECLIENT_34_074: [If the provided connection string contains a module id field, this function shall throw an UnsupportedOperationException.]
        super(new IotHubConnectionString(connString), protocol, publicKeyCertificate, isCertificatePath, privateKey, isPrivateKeyPath, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));
        commonConstructorVerifications();

        // Codes_SRS_DEVICECLIENT_12_013: [The constructor shall set the connection type to SINGLE_CLIENT.]
        // Codes_SRS_DEVICECLIENT_12_014: [The constructor shall set the transportClient to null.]
        commonConstructorSetup();
    }

    /**
     * Creates a device client that uses the provided security provider for authentication.
     *
     * @param uri The connection string for iot hub to connect to (format: "yourHubName.azure-devices.net")
     * @param deviceId The id for the device to use
     * @param securityProvider The security provider for the device
     * @param protocol The protocol the device shall use for communication to the IoT Hub
     * @return The created device client instance
     * @throws URISyntaxException If the provided connString could not be parsed.
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws URISyntaxException, IOException
    {
        return new DeviceClient(uri, deviceId, securityProvider, protocol);
    }

    /**
     * Sets the message callback.
     *
     * @param callback the message callback. Can be {@code null}.
     * @param context the context to be passed to the callback. Can be {@code null}.
     *
     * @return itself, for fluent setting.
     *
     * @throws IllegalArgumentException if the callback is {@code null} but a context is
     * passed in.
     * @throws IllegalStateException if the callback is set after the client is
     * closed.
     */
    public DeviceClient setMessageCallback(MessageCallback callback, Object context) throws IllegalArgumentException
    {
        this.setMessageCallbackInternal(callback, context);
        return this;
    }

    /**
     * Creates a device client that uses the provided security provider for authentication.
     *
     * @param uri The connection string for iot hub to connect to (format: "yourHubName.azure-devices.net")
     * @param deviceId The id for the device to use
     * @param securityProvider The security provider for the device
     * @param protocol The protocol the device shall use for communication to the IoT Hub
     * @return The created device client instance
     * @throws URISyntaxException If the provided connString could not be parsed.
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    private DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws URISyntaxException, IOException
    {
        super(uri, deviceId, securityProvider, protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));
        commonConstructorSetup();
    }

    void closeFileUpload() throws IOException
    {
        if (this.fileUpload != null)
        {
            this.fileUpload.closeNow();
        }
    }

    private void commonConstructorSetup()
    {
        this.ioTHubConnectionType = IoTHubConnectionType.SINGLE_CLIENT;
        this.transportClient = null;
    }

    private void commonConstructorVerifications() throws UnsupportedOperationException
    {
        if (this.getConfig().getModuleId() != null && !this.getConfig().getModuleId().isEmpty())
        {
            throw new UnsupportedOperationException("DeviceClient connection string cannot contain module id. Use ModuleClient instead.");
        }
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT Hub. If
     * the client is already open, the function shall do nothing.
     *
     * @throws IOException if a connection to an IoT Hub cannot be established.
     */
    public void open() throws IOException
    {
        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
        {
            if (this.transportClient.getTransportClientState() == TransportClient.TransportClientState.CLOSED)
            {
                // Codes_SRS_DEVICECLIENT_12_007: [If the client has been initialized to use TransportClient and the TransportClient is not opened yet the function shall throw an IOException.]
                throw new IOException("Calling open() when using the TransportClient is not supported. Use TransportClient.open() instead.");
            }
            else
            {
                // Codes_SRS_DEVICECLIENT_12_019: [If the client has been initialized to use TransportClient and the TransportClient is already opened the function shall do nothing.]
                logger.LogInfo("Connection already opened by TransportClient.");
            }
        }
        else
        {
            // Codes_SRS_DEVICECLIENT_21_006: [The open shall invoke super.open().]
            super.open();
        }

        logger.LogInfo("Connection opened with success, method name is %s ", logger.getMethodName());
    }

    /**
     * Completes all current outstanding requests and closes the IoT Hub client.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code closeNow()} is called, the IoT Hub client is no longer
     * usable. If the client is already closed, the function shall do nothing.
     * @deprecated : As of release 1.1.25 this call is replaced by {@link #closeNow()}
     *
     * @throws IOException if the connection to an IoT Hub cannot be closed.
     */
    @Deprecated
    public void close() throws IOException
    {
        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
        {
            if (this.transportClient.getTransportClientState() == TransportClient.TransportClientState.OPENED)
            {
                // Codes_SRS_DEVICECLIENT_12_006: [If the client has been initialized to use TransportClient and the TransportClient is already opened the function shall throw an IOException.]
                throw new IOException("Calling closeNow() when using TransportClient is not supported. Use TransportClient.closeNow() instead.");
            }
            else
            {
                // Codes_SRS_DEVICECLIENT_12_020: [If the client has been initialized to use TransportClient and the TransportClient is not opened yet the function shall do nothing.]
                logger.LogInfo("Connection already closed by TransportClient.");
            }
        }
        else
        {
            //Codes_SRS_DEVICECLIENT_34_040: [If this object is not using a transport client, it shall invoke super.close().]
            super.close();
        }

        logger.LogInfo("Connection closed with success, method name is %s ", logger.getMethodName());
    }

    /**
     * Closes the IoT Hub client by releasing any resources held by client. When
     * closeNow is called all the messages that were in transit or pending to be
     * sent will be informed to the user in the callbacks and any existing
     * connection to IotHub will be closed.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code closeNow()} is called, the IoT Hub client is no longer
     * usable. If the client is already closed, the function shall do nothing.
     *
     * @throws IOException if the connection to an IoT Hub cannot be closed.
     */
    public void closeNow() throws IOException
    {
        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
        {
            if (this.transportClient.getTransportClientState() == TransportClient.TransportClientState.OPENED)
            {
                // Codes_SRS_DEVICECLIENT_12_005: [If the client has been initialized to use TransportClient and the TransportClient is already opened the function shall throw an IOException.]
                throw new IOException("Calling closeNow() when using TransportClient is not supported. Use TransportClient.closeNow() instead.");
            }
            else
            {
                // Codes_SRS_DEVICECLIENT_12_021: [If the client has been initialized to use TransportClient and the TransportClient is not opened yet the function shall do nothing.]
                logger.LogInfo("Connection already closed by TransportClient.");
            }
        }
        else
        {
            //Codes_SRS_DEVICECLIENT_34_041: [If this object is not using a transport client, it shall invoke super.closeNow().]
            super.closeNow();
            this.closeFileUpload();
        }

        logger.LogInfo("Connection closed with success, method name is %s ", logger.getMethodName());
    }

    /**
     * Asynchronously upload a stream to the IoT Hub.
     *
     * NOTE: IotHub does not currently support CA signed devices using file upload. Please use SAS based authentication or
     * self signed certificates.
     *
     * @param destinationBlobName is a string with the name of the file in the storage.
     * @param inputStream is a InputStream with the stream to upload in the blob.
     * @param streamLength is a long with the number of bytes in the stream to upload.
     * @param callback the callback to be invoked when a file is uploaded.
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     *
     * @throws IllegalArgumentException if the provided blob name, or the file path is {@code null},
     *          empty or not valid, or if the callback is {@code null}.
     * @throws IOException if the client cannot create a instance of the FileUpload or the transport.
     */
    public void uploadToBlobAsync(String destinationBlobName, InputStream inputStream, long streamLength,
                                  IotHubEventCallback callback, Object callbackContext) throws IllegalArgumentException, IOException
    {
        if (callback == null)
        {
            throw new IllegalArgumentException("Callback is null");
        }

        if (inputStream == null)
        {
            throw new IllegalArgumentException("The input stream cannot be null.");
        }

        if (streamLength < 0)
        {
            throw new IllegalArgumentException("Invalid stream size.");
        }

        ParserUtility.validateBlobName(destinationBlobName);

        if (this.fileUpload == null)
        {
            this.fileUpload = new FileUpload(this.config);
        }

        this.fileUpload.uploadToBlobAsync(destinationBlobName, inputStream, streamLength, callback, callbackContext);
    }

    /**
     * Retrieves the twin's latest desired properties
     * @throws IOException if the iothub cannot be reached
     */
    public void getDeviceTwin() throws IOException
    {
        this.getTwinInternal();
    }

    /**
     * Starts the device twin.
     *
     * @param deviceTwinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. If {@code null}, you will unsubscribe from twin.
     * @param deviceTwinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallBack the PropertyCallBack callback for providing any changes in desired properties. If {@code null}, you will unsubscribe from twin.
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.     *
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object deviceTwinStatusCallbackContext,
                                        PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        this.startTwinInternal(deviceTwinStatusCallback, deviceTwinStatusCallbackContext, genericPropertyCallBack, genericPropertyCallBackContext);
    }

    /**
     * Starts the device twin.
     *
     * @param deviceTwinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param deviceTwinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallBack the TwinPropertyCallBack callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.     *
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object deviceTwinStatusCallbackContext,
                                 TwinPropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        this.startTwinInternal(deviceTwinStatusCallback, deviceTwinStatusCallbackContext, genericPropertyCallBack, genericPropertyCallBackContext);
    }

    /**
     * Registers a callback to be executed whenever the connection to the device is lost or established.
     * @deprecated as of release 1.10.0 by {@link #registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)}
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     * @throws IllegalArgumentException if the provided callback is null
     */
    @Deprecated
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext) throws IllegalArgumentException
    {
        if (null == callback)
        {
            throw new IllegalArgumentException("Callback object cannot be null");
        }

        this.deviceIO.registerConnectionStateCallback(callback, callbackContext);
    }

    /**
     * Subscribes to device methods
     *
     * @param deviceMethodCallback Callback on which device methods shall be invoked. Cannot be {@code null}.
     * @param deviceMethodCallbackContext Context for device method callback. Can be {@code null}.
     * @param deviceMethodStatusCallback Callback for providing IotHub status for device methods. Cannot be {@code null}.
     * @param deviceMethodStatusCallbackContext Context for device method status callback. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened.
     * @throws IllegalArgumentException if either callback are null.
     */
    public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext,
                                        IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext)
            throws IOException, IllegalArgumentException
    {
        this.subscribeToMethodsInternal(deviceMethodCallback, deviceMethodCallbackContext, deviceMethodStatusCallback, deviceMethodStatusCallbackContext);
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
    public void setOption(String optionName, Object value) throws IllegalArgumentException
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

        switch (optionName)
        {
            // Codes_SRS_DEVICECLIENT_02_016: ["SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.]
            case SET_MINIMUM_POLLING_INTERVAL:
            {
                // Codes_SRS_DEVICECLIENT_12_023: [If the client configured to use TransportClient the SetMinimumPollingInterval shall throw IOException.]
                if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                {
                    throw new IllegalStateException("setOption " + SET_MINIMUM_POLLING_INTERVAL +
                            "only works with HTTP protocol");
                }
                break;
            }
            // Codes_SRS_DEVICECLIENT_21_040: ["SetSendInterval" - time in milliseconds between 2 consecutive message sends.]
            case SET_SEND_INTERVAL:
            {
                if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                {
                    // Codes_SRS_DEVICECLIENT_12_022: [If the client configured to use TransportClient the SetSendInterval shall throw IllegalStateException.]
                    throw new IllegalStateException("Setting send interval is not supported for single client if using TransportClient. " +
                            "Use TransportClient.setSendInterval() instead.");
                }
            }
            // Codes_SRS_DEVICECLIENT_25_019: ["SetCertificatePath" - path to the certificate to verify peer.]
            case SET_CERTIFICATE_PATH:
            {
                if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                {
                    if (this.transportClient.getTransportClientState() == TransportClient.TransportClientState.OPENED)
                    {
                        // Codes_SRS_DEVICECLIENT_12_029: [*SetCertificatePath" shall throw if the transportClient or deviceIO already open, otherwise set the path on the config.]
                        throw new IllegalStateException("setOption " + SET_CERTIFICATE_PATH + " only works when the transport is closed");
                    }
                    else
                    {
                        // Codes_SRS_DEVICECLIENT_12_030: [*SetCertificatePath" shall udate the config on transportClient if tranportClient used.]
                        setOption_SetCertificatePath(value);
                        return;
                    }
                }
                break;
            }
            // Codes_SRS_DEVICECLIENT_34_043: ["SetCertificateAuthority" - set the certificate to verify peer.]
            case SET_CERTIFICATE_AUTHORITY:
            {
                if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                {
                    if (this.transportClient.getTransportClientState() == TransportClient.TransportClientState.OPENED)
                    {
                        // Codes_SRS_DEVICECLIENT_34_042: [If this function is called with the SET_CERTIFICATE_AUTHORITY option, and is using an open transport client, this function shall throw an IllegalStateException]
                        throw new IllegalStateException("setOption " + SET_CERTIFICATE_PATH + " only works when the transport is closed");
                    }
                }
                break;
            }
            //Codes_SRS_DEVICECLIENT_25_021: ["SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.]
            case SET_SAS_TOKEN_EXPIRY_TIME:
            {
                if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                {
                    if (this.transportClient.getTransportClientState() == TransportClient.TransportClientState.OPENED)
                    {
                        throw new IllegalStateException("setOption " + SET_SAS_TOKEN_EXPIRY_TIME + " with TransportClient only works when the transport client is closed");
                    }
                    else
                    {
                        //Codes__SRS_DEVICECLIENT_25_023: ["SetSASTokenExpiryTime" is available for HTTPS/AMQP/MQTT/AMQPS_WS/MQTT_WS.]
                        setOption_SetSASTokenExpiryTime(value);
                        return;
                    }
                }
                else
                {
                    //Codes__SRS_DEVICECLIENT_25_023: ["SetSASTokenExpiryTime" is available for HTTPS/AMQP/MQTT/AMQPS_WS/MQTT_WS.]
                    setOption_SetSASTokenExpiryTime(value);
                    return;
                }
            }
            default:
            {
                throw new IllegalArgumentException("optionName is unknown = " + optionName);
            }
        }

        super.setOption(optionName, value);
    }

    @Override
    void setOption_SetSASTokenExpiryTime(Object value) throws IllegalArgumentException
    {
        logger.LogInfo("Setting SASTokenExpiryTime as %s seconds, method name is %s ", value, logger.getMethodName());

        if (this.getConfig().getAuthenticationType() != DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            //Codes_SRS_DEVICECLIENT_34_065: [""SetSASTokenExpiryTime" if this option is called when not using sas token authentication, an IllegalStateException shall be thrown.]
            throw new IllegalStateException("Cannot set sas token validity time when not using sas token authentication");
        }

        if (value != null)
        {
            //**Codes_SRS_DEVICECLIENT_25_022: [**"SetSASTokenExpiryTime" should have value type long**.]**
            long validTimeInSeconds;

            if (value instanceof Long)
            {
                validTimeInSeconds = (long) value;
            }
            else
            {
                throw new IllegalArgumentException("value is not long = " + value);
            }

            this.getConfig().getSasTokenAuthentication().setTokenValidSecs(validTimeInSeconds);

            if (this.getDeviceIO() != null)
            {
                if (this.getDeviceIO().isOpen())
                {
                    try
                    {
                        /* Codes_SRS_DEVICECLIENT_25_024: [**"SetSASTokenExpiryTime" shall restart the transport
                         *                                  1. If the device currently uses device key and
                         *                                  2. If transport is already open
                         *                                 after updating expiry time
                        */
                        if (this.getConfig().getSasTokenAuthentication().canRefreshToken())
                        {
                            if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                            {
                                // Codes_SRS_DEVICECLIENT_12_025: [If the client configured to use TransportClient the function shall use transport client closeNow() and open() for restart.]
                                this.transportClient.closeNow();
                                this.transportClient.open();
                            }
                            else
                            {
                                this.getDeviceIO().close();
                                this.getDeviceIO().open();
                            }
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

    private static long getReceivePeriod(IotHubClientProtocol protocol)
    {
        switch (protocol)
        {
            case HTTPS:
                return RECEIVE_PERIOD_MILLIS_HTTPS;
            case AMQPS:
            case AMQPS_WS:
                return RECEIVE_PERIOD_MILLIS_AMQPS;
            case MQTT:
            case MQTT_WS:
                return RECEIVE_PERIOD_MILLIS_MQTT;
            default:
                // should never happen.
                throw new IllegalStateException(
                        "Invalid client protocol specified.");
        }
    }

    @SuppressWarnings("unused")
    protected DeviceClient()
    {
        // Codes_SRS_DEVICECLIENT_12_028: [The constructor shall shall set the config, deviceIO and tranportClient to null.]
        this.transportClient = null;
    }
}