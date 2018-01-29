// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.transport.amqps.IoTHubConnectionType;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS;
import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.AMQPS_WS;

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
public final class DeviceClient implements Closeable
{
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

    private static final String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    private static final String SET_SEND_INTERVAL = "SetSendInterval";
    private static final String SET_CERTIFICATE_PATH = "SetCertificatePath";
    private static final String SET_SAS_TOKEN_EXPIRY_TIME = "SetSASTokenExpiryTime";

    private DeviceClientConfig config;
    private DeviceIO deviceIO;

    private DeviceTwin deviceTwin;
    private DeviceMethod deviceMethod;
    private FileUpload fileUpload;

    protected long RECEIVE_PERIOD_MILLIS;
    private CustomLogger logger;

    private IoTHubConnectionType ioTHubConnectionType = IoTHubConnectionType.UNKNOWN;

    private TransportClient transportClient;


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
     */
    public DeviceClient(String connString, TransportClient transportClient) throws URISyntaxException
    {
        // Codes_SRS_DEVICECLIENT_12_008: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.]
        if ((connString == null) || connString.isEmpty())
        {
            throw new IllegalArgumentException("IoT Hub connection string cannot be null.");
        }

        // Codes_SRS_DEVICECLIENT_12_018: [If the tranportClient is null, the function shall throw an IllegalArgumentException.]
        if (transportClient == null)
        {
            throw new IllegalArgumentException("Transport client cannot be null.");
        }

        // Codes_SRS_DEVICECLIENT_12_010: [The constructor shall set the connection type to USE_TRANSPORTCLIENT.]
        this.ioTHubConnectionType = IoTHubConnectionType.USE_TRANSPORTCLIENT;

        // Codes_SRS_DEVICECLIENT_12_016: [The constructor shall save the transportClient parameter.]
        this.transportClient = transportClient;

        // Codes_SRS_DEVICECLIENT_12_009: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
        IotHubConnectionString iotHubConnectionString = new IotHubConnectionString(connString);
        this.config = new DeviceClientConfig(iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        this.config.setProtocol(this.transportClient.getIotHubClientProtocol());

        // Codes_SRS_DEVICECLIENT_12_011: [The constructor shall set the deviceIO to null.]
        this.deviceIO = null;

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
    public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException
    {
        //Codes_SRS_DEVICECLIENT_21_004: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.]
        //Codes_SRS_DEVICECLIENT_21_005: [If protocol is null, the function shall throw an IllegalArgumentException.]
        commonConstructorVerification(connString, protocol);

        //Codes_SRS_DEVICECLIENT_21_001: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
        //Codes_SRS_DEVICECLIENT_34_055: [If the provided connection string contains an expired SAS token, a SecurityException shall be thrown.]
        IotHubConnectionString iotHubConnectionString = new IotHubConnectionString(connString);
        this.config = new DeviceClientConfig(iotHubConnectionString, DeviceClientConfig.AuthType.SAS_TOKEN);
        this.config.setProtocol(protocol);

        //Codes_SRS_DEVICECLIENT_21_002: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
        //Codes_SRS_DEVICECLIENT_21_003: [The constructor shall save the connection configuration using the object DeviceClientConfig.]
        //Codes_SRS_DEVICECLIENT_12_012: [The constructor shall set the connection type to SINGLE_CLIENT.]
        commonConstructorSetup(protocol);
    }

    /**
     * Constructor that uses x509 authentication for communicating with IotHub
     *
     * @param connString the connection string for the x509 device to connect as (format: "HostName=...;DeviceId=...;x509=true")
     * @param protocol the protocol to use when communicating with IotHub
     * @param publicKeyCertificate the PEM formatted public key certificate or the path to a PEM formatted public key certificate file
     * @param isCertificatePath if the provided publicKeyCertificate is a path to a file containing the PEM formatted public key certificate
     * @param privateKey the PEM formatted private key or the path to a PEM formatted private key file
     * @param isPrivateKeyPath if the provided privateKey is a path to a file containing the PEM formatted private key
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    public DeviceClient(String connString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws URISyntaxException
    {
        //Codes_SRS_DEVICECLIENT_34_061: [If the connection string is null or empty, the function shall throw an IllegalArgumentException.]
        //Codes_SRS_DEVICECLIENT_34_062: [If protocol is null, the function shall throw an IllegalArgumentException.]
        commonConstructorVerification(connString, protocol);

        //Codes_SRS_DEVICECLIENT_34_058: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.]
        //Codes_SRS_DEVICECLIENT_34_063: [This function shall save the provided certificate and key within its config.]
        IotHubConnectionString iotHubConnectionString = new IotHubConnectionString(connString);
        this.config = new DeviceClientConfig(iotHubConnectionString, publicKeyCertificate, isCertificatePath, privateKey, isPrivateKeyPath);
        this.config.setProtocol(protocol);

        //Codes_SRS_DEVICECLIENT_34_059: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
        //Codes_SRS_DEVICECLIENT_34_060: [The constructor shall save the connection configuration using the object DeviceClientConfig.]
        //Codes_SRS_DEVICECLIENT_12_013: [The constructor shall set the connection type to SINGLE_CLIENT.]
        commonConstructorSetup(protocol);
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
        if (protocol == null)
        {
            //Codes_SRS_DEVICECLIENT_34_064: [If the provided protocol is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("The transport protocol cannot be null");
        }

        //Codes_SRS_DEVICECLIENT_34_065: [The provided uri and device id will be used to create an iotHubConnectionString that will be saved in config.]
        //Codes_SRS_DEVICECLIENT_34_066: [The provided security provider will be saved in config.]
        IotHubConnectionString connectionString = new IotHubConnectionString(uri, deviceId, null, null);
        this.config = new DeviceClientConfig(connectionString, securityProvider);
        this.config.setProtocol(protocol);

        //Codes_SRS_DEVICECLIENT_34_067: [The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.]
        commonConstructorSetup(protocol);
    }

    /**
     * Throws an IllegalArgumentException if either of the arguments is null or if the connString is empty.
     * @param connString the connection string
     * @param protocol the transport protocol
     */
    private void commonConstructorVerification(String connString, IotHubClientProtocol protocol)
    {
        if (connString == null || connString.isEmpty())
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("Protocol cannot be null.");
        }
    }

    /**
     * Handles logic common to all constructors of DeviceClient. Sets the receive period based on the protocol, creates the config instance,
     * creates the deviceIO instance, and creates the logger instance
     * @param protocol The protocol the device client will communicate in
     * @throws IOException if any exception occurs when creating the authentication layer for this client
     * @throws IllegalArgumentException if the connection string is null, empty, or invalid, if the protocol is null, if the connection string is for x509 when x509 is not allowed
     * @throws SecurityException if the provided connection string contains an expired sas token
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    private void commonConstructorSetup(IotHubClientProtocol protocol)
    {
        switch (protocol)
        {
            case HTTPS:
                RECEIVE_PERIOD_MILLIS = RECEIVE_PERIOD_MILLIS_HTTPS;
                break;
            case AMQPS:
                RECEIVE_PERIOD_MILLIS = RECEIVE_PERIOD_MILLIS_AMQPS;
                break;
            case AMQPS_WS:
                RECEIVE_PERIOD_MILLIS = RECEIVE_PERIOD_MILLIS_AMQPS;
                break;
            case MQTT:
                RECEIVE_PERIOD_MILLIS = RECEIVE_PERIOD_MILLIS_MQTT;
                break;
            case MQTT_WS:
                RECEIVE_PERIOD_MILLIS = RECEIVE_PERIOD_MILLIS_MQTT;
                break;
            default:
                // should never happen.
                throw new IllegalStateException(
                        "Invalid client protocol specified.");
        }

        this.ioTHubConnectionType = IoTHubConnectionType.SINGLE_CLIENT;
        this.transportClient = null;
        this.deviceIO = new DeviceIO(this.config, SEND_PERIOD_MILLIS, RECEIVE_PERIOD_MILLIS);

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("DeviceClient object is created successfully, method name is %s ", logger.getMethodName());
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
            /* Codes_SRS_DEVICECLIENT_34_044: [If the SAS token has expired before this call, throw a Security Exception] */
            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.config.getSasTokenAuthentication().isRenewalNecessary())
            {
                throw new SecurityException("Your SasToken is expired");
            }

            /* Codes_SRS_DEVICECLIENT_21_006: [The open shall open the deviceIO connection.] */
            /* Codes_SRS_DEVICECLIENT_21_007: [If the opening a connection via deviceIO is not successful, the open shall throw IOException.] */
            this.deviceIO.open();
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
            // Codes_SRS_DEVICECLIENT_11_040: [The function shall finish all ongoing tasks.]
            // Codes_SRS_DEVICECLIENT_11_041: [The function shall cancel all recurring tasks.]
            while (!this.deviceIO.isEmpty())
            {
                // Don't do anything, can be infinite.
            }

            /* Codes_SRS_DEVICECLIENT_21_042: [The closeNow shall closeNow the deviceIO connection.] */
            /* Codes_SRS_DEVICECLIENT_21_043: [If the closing a connection via deviceIO is not successful, the closeNow shall throw IOException.] */
            this.deviceIO.close();
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
            /* Codes_SRS_DEVICECLIENT_21_008: [The closeNow shall closeNow the deviceIO connection.] */
            /* Codes_SRS_DEVICECLIENT_21_009: [If the closing a connection via deviceIO is not successful, the closeNow shall throw IOException.] */
            this.deviceIO.close();

            /* Codes_SRS_DEVICECLIENT_21_054: [If the fileUpload is not null, the closeNow shall call closeNow on fileUpload.] */
            closeFileUpload();
        }

        logger.LogInfo("Connection closed with success, method name is %s ", logger.getMethodName());
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
        /* Codes_SRS_DEVICECLIENT_21_010: [The sendEventAsync shall asynchronously send the message using the deviceIO connection.] */
        /* Codes_SRS_DEVICECLIENT_21_011: [If starting to send via deviceIO is not successful, the sendEventAsync shall bypass the threw exception.] */
        /* Codes_SRS_DEVICECLIENT_12_001: [The function shall call deviceIO.sendEventAsync with the client's config parameter to enable multiplexing.] */
        deviceIO.sendEventAsync(message, callback, callbackContext, this.config.getIotHubConnectionString());

        logger.LogInfo("Message with messageid %s along with callback and callbackcontext is added to the queue, method name is %s ", message.getMessageId(), logger.getMethodName());
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
    public DeviceClient setMessageCallback(
            MessageCallback callback,
            Object context)
    {
        /* Codes_SRS_DEVICECLIENT_11_014: [If the callback is null but the context is non-null, the function shall throw an IllegalArgumentException.] */
        if (callback == null && context != null)
        {
            throw new IllegalArgumentException("Cannot give non-null context for a null callback.");
        }

        /* Codes_SRS_DEVICECLIENT_11_013: [The function shall set the message callback, with its associated context.] */
        this.config.setMessageCallback(callback, context);
        return this;
    }

    /**
     * Starts the device twin.
     *
     * @param deviceTwinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param deviceTwinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallBack the PropertyCallBack callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.     *
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object deviceTwinStatusCallbackContext,
                                PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext)
            throws IOException
    {
        if (!this.deviceIO.isOpen())
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_027: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        if (deviceTwinStatusCallback == null || genericPropertyCallBack == null)
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Callback cannot be null");
        }
        if (this.deviceTwin == null)
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_025: [**The function shall create a new instance of class Device Twin and request all twin properties by calling getDeviceTwin**]**
             */
            deviceTwin = new DeviceTwin(this.deviceIO, this.config, deviceTwinStatusCallback, deviceTwinStatusCallbackContext,
                    genericPropertyCallBack, genericPropertyCallBackContext);
            deviceTwin.getDeviceTwin();
        }
        else
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_028: [**If this method is called twice on the same instance of the client then this method shall throw UnsupportedOperationException.**]**
             */
            throw new UnsupportedOperationException("You have already initialised twin");
        }
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
            throws IOException
    {
        if (!this.deviceIO.isOpen())
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_027: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        if (deviceTwinStatusCallback == null || genericPropertyCallBack == null)
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Callback cannot be null");
        }
        if (this.deviceTwin == null)
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_025: [**The function shall create a new instance of class Device Twin and request all twin properties by calling getDeviceTwin**]**
             */
            deviceTwin = new DeviceTwin(this.deviceIO, this.config, deviceTwinStatusCallback, deviceTwinStatusCallbackContext,
                    genericPropertyCallBack, genericPropertyCallBackContext);
            deviceTwin.getDeviceTwin();
        }
        else
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_028: [**If this method is called twice on the same instance of the client then this method shall throw UnsupportedOperationException.**]**
             */
            throw new UnsupportedOperationException("You have already initialised twin");
        }
    }

    public void getDeviceTwin() throws IOException
    {
        if (this.deviceTwin == null)
        {
            /*
             **Codes_SRS_DEVICECLIENT_21_040: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
             */
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            /*
             **Codes_SRS_DEVICECLIENT_21_041: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        /*
         **Codes_SRS_DEVICECLIENT_21_042: [**The function shall get all desired properties by calling getDeviceTwin.**]**
         */
        this.deviceTwin.getDeviceTwin();
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
        if (this.deviceTwin == null)
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_029: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
             */
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_030: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        /*
        **Tests_SRS_DEVICECLIENT_25_031: [**This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.**]**
         */
        this.deviceTwin.subscribeDesiredPropertiesNotification(onDesiredPropertyChange);
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
        if (this.deviceTwin == null)
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_029: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
             */
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_030: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        /*
         **Tests_SRS_DEVICECLIENT_25_031: [**This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.**]**
         */
        this.deviceTwin.subscribeDesiredPropertiesTwinPropertyNotification(onDesiredPropertyChange);
    }

    /**
     * Sends reported properties
     *
     * @param reportedProperties the Set for desired properties and their corresponding callback and context. Cannot be {@code null}.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     * @throws IllegalArgumentException if reportedProperties is null or empty.
     */
    public void sendReportedProperties(Set<Property> reportedProperties) throws IOException
    {
        if (this.deviceTwin == null)
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_032: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
             */
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_033: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        if (reportedProperties == null || reportedProperties.isEmpty())
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_034: [**If reportedProperties is null or empty, the function shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Reported properties set cannot be null or empty.");
        }

        /*
        **Codes_SRS_DEVICECLIENT_25_035: [**This method shall send to reported properties by calling updateReportedProperties on the twin object.**]**
         */
        this.deviceTwin.updateReportedProperties(reportedProperties);

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
    public void sendReportedProperties(Set<Property> reportedProperties, int version) throws IOException
    {
        if (this.deviceTwin == null)
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_032: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**
             */
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_033: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        if (reportedProperties == null || reportedProperties.isEmpty())
        {
            /*
             **Codes_SRS_DEVICECLIENT_25_034: [**If reportedProperties is null or empty, the function shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Reported properties set cannot be null or empty.");
        }

        if(version < 0)
        {
            /*
             **Codes_SRS_DEVICECLIENT_21_053: [**If version is negative, the function shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Version cannot be null.");
        }

        /*
         **Codes_SRS_DEVICECLIENT_25_035: [**This method shall send to reported properties by calling updateReportedProperties on the twin object.**]**
         */
        this.deviceTwin.updateReportedProperties(reportedProperties, version);
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
            throws IOException
    {
        if (!this.deviceIO.isOpen())
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_036: [**If the client has not been open, the function shall throw an IOException.**]**
             */
            throw new IOException("Open the client connection before using it.");
        }

        if (deviceMethodCallback == null || deviceMethodStatusCallback == null)
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_037: [**If deviceMethodCallback or deviceMethodStatusCallback is null, the function shall throw an IllegalArgumentException.**]**
             */
            throw new IllegalArgumentException("Callback cannot be null");
        }

        if (this.deviceMethod == null)
        {
            /*
            **Codes_SRS_DEVICECLIENT_25_038: [**This method shall subscribe to device methods by calling subscribeToDeviceMethod on DeviceMethod object which it created.**]**
             */
            this.deviceMethod = new DeviceMethod(this.deviceIO, this.config, deviceMethodStatusCallback, deviceMethodStatusCallbackContext);
        }

        /*
        **Codes_SRS_DEVICECLIENT_25_039: [**This method shall not create a new instance of deviceMethod if called twice.**]**
         */
        this.deviceMethod.subscribeToDeviceMethod(deviceMethodCallback, deviceMethodCallbackContext);
    }

    /**
     * Asynchronously upload a stream to the IoT Hub.
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
     * @throws UnsupportedOperationException if this method is called when using x509 authentication
     */
    public void uploadToBlobAsync(String destinationBlobName, InputStream inputStream, long streamLength,
                                  IotHubEventCallback callback, Object callbackContext)
            throws IllegalArgumentException, IOException
    {
        /* Codes_SRS_DEVICECLIENT_21_044: [The uploadToBlobAsync shall asynchronously upload the stream in `inputStream` to the blob in `destinationBlobName`.] */

        /* Codes_SRS_DEVICECLIENT_21_045: [If the `callback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if (callback == null)
        {
            throw new IllegalArgumentException("Callback is null");
        }

        /* Codes_SRS_DEVICECLIENT_21_046: [If the `inputStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if (inputStream == null)
        {
            throw new IllegalArgumentException("The input stream cannot be null.");
        }

        /* Codes_SRS_DEVICECLIENT_21_052: [If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        if (streamLength < 0)
        {
            throw new IllegalArgumentException("Invalid stream size.");
        }

        /* Codes_SRS_DEVICECLIENT_21_047: [If the `destinationBlobName` is null, empty or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.] */
        ParserUtility.validateBlobName(destinationBlobName);

        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
        {
            //Codes_SRS_DEVICECLIENT_34_066: [If this function is called when the device client is using x509 authentication, an UnsupportedOperationException shall be thrown.]
            throw new UnsupportedOperationException("File Upload does not support x509 authentication");
        }

        /* Codes_SRS_DEVICECLIENT_21_048: [If there is no instance of the FileUpload, the uploadToBlobAsync shall create a new instance of the FileUpload.] */
        if (this.fileUpload == null)
        {
            /* Codes_SRS_DEVICECLIENT_21_049: [If uploadToBlobAsync failed to create a new instance of the FileUpload, it shall bypass the exception.] */
            this.fileUpload = new FileUpload(this.config);
        }

        /* Codes_SRS_DEVICECLIENT_21_050: [The uploadToBlobAsync shall start the stream upload process, by calling uploadToBlobAsync on the FileUpload class.] */
        /* Codes_SRS_DEVICECLIENT_21_051: [If uploadToBlobAsync failed to start the upload using the FileUpload, it shall bypass the exception.] */
        this.fileUpload.uploadToBlobAsync(destinationBlobName, inputStream, streamLength, callback, callbackContext);
    }

    @SuppressWarnings("unused")
    protected DeviceClient()
    {
        // Codes_SRS_DEVICECLIENT_12_028: [The constructor shall shall set the config, deviceIO and tranportClient to null.]
        this.config = null;
        this.deviceIO = null;
        this.transportClient = null;
    }

    private void setOption_SetMinimumPollingInterval(Object value)
    {
        logger.LogInfo("Setting MinimumPollingInterval as %s milliseconds, method name is %s ", value, logger.getMethodName());

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

    private void setOption_SetSendInterval(Object value)
    {
        logger.LogInfo("Setting send Interval as %s milliseconds, method name is %s ", value, logger.getMethodName());

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

    private void setOption_SetCertificatePath(Object value)
    {
        logger.LogInfo("Setting CertificatePath as %s, method name is %s ", value, logger.getMethodName());

        if (value != null)
        {
            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                this.config.getSasTokenAuthentication().setPathToIotHubTrustedCert((String) value);
            }
            else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                this.config.getX509Authentication().setPathToIotHubTrustedCert((String) value);
            }
        }
    }

    private void setOption_SetSASTokenExpiryTime(Object value)
    {
        logger.LogInfo("Setting SASTokenExpiryTime as %s seconds, method name is %s ", value, logger.getMethodName());

        if (this.config.getAuthenticationType() != DeviceClientConfig.AuthType.SAS_TOKEN)
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

            this.config.getSasTokenAuthentication().setTokenValidSecs(validTimeInSeconds);

            boolean restart = false;

            if (deviceIO != null)
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
                        if (this.config.getIotHubConnectionString().getSharedAccessKey() != null)
                        {
                            restart = true;
                            if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                            {
                                // Codes_SRS_DEVICECLIENT_12_025: [If the client configured to use TransportClient the function shall use transport client closeNow() and open() for restart.]
                                this.transportClient.closeNow();
                            }
                            else
                            {
                                this.deviceIO.close();
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        // Codes_SRS_DEVICECLIENT_12_027: [The function shall throw IOError if either the deviceIO or the tranportClient's open() or closeNow() throws.]
                        throw new IOError(e);
                    }
                }

                if (restart)
                {
                    try
                    {
                        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                        {
                            // Codes_SRS_DEVICECLIENT_12_025: [If the client configured to use TransportClient the function shall use transport client closeNow() and open() for restart.]
                            this.transportClient.open();
                        }
                        else
                        {
                            this.deviceIO.open();
                        }
                    }
                    catch (IOException e)
                    {
                        throw new IOError(e);
                    }
                }
            }
        }
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
     *         option will restart the transport with the updated expiry time. The
     *         value is expected to be of type {@code long}.
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
                // Codes_SRS_DEVICECLIENT_02_016: ["SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.]
                case SET_MINIMUM_POLLING_INTERVAL:
                {
                    // Tests_SRS_DEVICECLIENT_12_023: [If the client configured to use TransportClient the SetMinimumPollingInterval shall throw IOException.]
                    if (this.ioTHubConnectionType == IoTHubConnectionType.USE_TRANSPORTCLIENT)
                    {
                        throw new IllegalStateException("setOption " + SET_MINIMUM_POLLING_INTERVAL +
                                "only works with HTTP protocol");
                    }
                    else
                    {
                        if (this.deviceIO.isOpen())
                        {
                            throw new IllegalStateException("setOption " + SET_MINIMUM_POLLING_INTERVAL +
                                    "only works when the transport is closed");
                        }
                        else
                        {
                            // Codes_SRS_DEVICECLIENT_02_017: [Option "SetMinimumPollingInterval" is available only for HTTP.]
                            if (this.deviceIO.getProtocol() == IotHubClientProtocol.HTTPS)
                            {
                                setOption_SetMinimumPollingInterval(value);
                            }
                            else
                            {
                                logger.LogError("optionName is unknown = %s for %s, method name is %s ", optionName, this.deviceIO.getProtocol().toString(), logger.getMethodName());
                                // Codes_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option
                                // handled by the client, then it shall throw IllegalArgumentException.]
                                throw new IllegalArgumentException("optionName is unknown = " + optionName + " for " + this.deviceIO.getProtocol().toString());
                            }
                        }
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
                    else
                    {
                        setOption_SetSendInterval(value);
                    }
                    break;
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
                        }
                    }
                    else
                    {
                        if ((this.deviceIO != null) && (this.deviceIO.isOpen()))
                        {
                            // Codes_SRS_DEVICECLIENT_12_029: [*SetCertificatePath" shall throw if the transportClient or deviceIO already open, otherwise set the path on the config.]
                            throw new IllegalStateException("setOption " + SET_CERTIFICATE_PATH + " only works when the transport is closed");
                        }
                        else
                        {
                            // Codes_SRS_DEVICECLIENT_25_020: ["SetCertificatePath" is available only for AMQP.]
                            if ((this.deviceIO.getProtocol() == AMQPS) || (this.deviceIO.getProtocol() == AMQPS_WS))
                            {
                                // Codes_SRS_DEVICECLIENT_12_030: [*SetCertificatePath" shall udate the config on transportClient if tranportClient used.]
                                setOption_SetCertificatePath(value);
                            }
                            else
                            {
                                logger.LogError("optionName is unknown = %s for %s, method name is %s ", optionName, this.deviceIO.getProtocol().toString(), logger.getMethodName());
                                // Codes_SRS_DEVICECLIENT_02_015: [If optionName is null or not an option handled by the
                                // client, then it shall throw IllegalArgumentException.]
                                throw new IllegalArgumentException("optionName is unknown = " + optionName + " for " + this.deviceIO.getProtocol().toString());
                            }
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
                        }
                    }
                    else
                    {
                        //Codes__SRS_DEVICECLIENT_25_023: ["SetSASTokenExpiryTime" is available for HTTPS/AMQP/MQTT/AMQPS_WS/MQTT_WS.]
                        setOption_SetSASTokenExpiryTime(value);
                    }
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
     * Registers a callback to be executed whenever the connection to the device is lost or established.
     *
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     */
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext)
    {
        //Codes_SRS_DEVICECLIENT_99_003: [If the callback is null the method shall throw an IllegalArgument exception.]
        if (null == callback)
        {
            throw new IllegalArgumentException("Callback object cannot be null");
        }

        //Codes_SRS_DEVICECLIENT_99_001: [The registerConnectionStateCallback shall register the callback with the Device IO even if the not open.]
        //Codes_SRS_DEVICECLIENT_99_002: [The registerConnectionStateCallback shall register the callback even if the client is not open.]
        this.deviceIO.registerConnectionStateCallback(callback, callbackContext);
    }

    /**
     * Closes FileUpload object if it is initialized.
     *
     * @throws IOException if file upload closeNow failed.
     */
    void closeFileUpload() throws IOException
    {
        if (this.fileUpload != null)
        {
            this.fileUpload.closeNow();
        }
    }

    /**
     * Getter for the device client config.
     *
     * @return the value of the config.
     */
    public DeviceClientConfig getConfig()
    {
        // Codes_SRS_DEVICECLIENT_12_002: [The function shall return with he current value of client config.]
        return this.config;
    }

    /**
     * Getter for the underlying DeviceIO for multiplexing scenarios.
     *
     * @return the value of the underlying DeviceIO.
     */
    DeviceIO getDeviceIO()
    {
        // Codes_SRS_DEVICECLIENT_12_003: [The function shall return with he current value of the client's underlying DeviceIO.]
        return this.deviceIO;
    }

    /**
     * Setter for the underlying DeviceIO for multiplexing scenarios.
     *
     * @param deviceIO is the DeviceIO to set.
     */
    void setDeviceIO(DeviceIO deviceIO)
    {
        // Codes_SRS_DEVICECLIENT_12_004: [The function shall set the client's underlying DeviceIO to the value of the given deviceIO parameter.]
        this.deviceIO = deviceIO;
    }
}