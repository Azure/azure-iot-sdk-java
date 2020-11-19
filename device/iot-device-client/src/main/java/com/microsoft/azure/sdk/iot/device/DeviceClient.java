// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadTask;
import com.microsoft.azure.sdk.iot.device.transport.amqps.IoTHubConnectionType;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
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
@Slf4j
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
    private FileUploadTask fileUploadTask;

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
        this(connString, protocol, (ClientOptions) null);
    }

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param connString the connection string. The connection string is a set
     * of key-value pairs that are separated by ';', with the keys and values
     * separated by '='. It should contain values for the following keys:
     * {@code HostName}, {@code DeviceId}, and {@code SharedAccessKey}.
     * @param protocol the communication protocol used (i.e. HTTPS)
     * @param clientOptions The options that allow configuration of the device client instance during initialization
     *
     * @throws IllegalArgumentException if any of {@code connString} or
     * {@code protocol} are {@code null}; or if {@code connString} is missing
     * one of the following attributes:{@code HostName}, {@code DeviceId}, or
     * {@code SharedAccessKey} or if the IoT hub hostname does not conform to
     * RFC 3986 or if the provided {@code connString} is for an x509 authenticated device
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    public DeviceClient(String connString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, IllegalArgumentException
    {
        super(new IotHubConnectionString(connString), protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol), clientOptions);

        commonConstructorVerifications();

        commonConstructorSetup();
    }

    /**
     * Constructor that allows for the client's SAS token generation to be controlled by the user. Note that options in
     * this client such as setting the SAS token expiry time will throw {@link UnsupportedOperationException} since
     * the SDK no longer controls that when this constructor is used.
     * @param hostName The host name of the IoT Hub that this client will connect to.
     * @param deviceId The Id of the device that the connection will identify as.
     * @param sasTokenProvider The provider of all SAS tokens that are used during authentication.
     * @param protocol The protocol that the client will connect over.
     */
    public DeviceClient(String hostName, String deviceId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol)
    {
        this(hostName, deviceId, sasTokenProvider, protocol, null);
    }

    /**
     * Constructor that allows for the client's SAS token generation to be controlled by the user. Note that options in
     * this client such as setting the SAS token expiry time will throw {@link UnsupportedOperationException} since
     * the SDK no longer controls that when this constructor is used.
     * @param hostName The host name of the IoT Hub that this client will connect to.
     * @param deviceId The Id of the device that the connection will identify as.
     * @param sasTokenProvider The provider of all SAS tokens that are used during authentication.
     * @param protocol The protocol that the client will connect over.
     * @param clientOptions The options that allow configuration of the device client instance during initialization.
     */
    public DeviceClient(String hostName, String deviceId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions)
    {
        super(hostName, deviceId, null, sasTokenProvider, protocol, clientOptions, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));
        commonConstructorVerifications();
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
     * @deprecated For x509 authentication, use {@link #DeviceClient(String, IotHubClientProtocol, ClientOptions)} and provide
     * an SSLContext instance in the {@link ClientOptions} instance. For a sample on how to build this SSLContext,
     * see <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-samples/send-event-x509/src/main/java/samples/com/microsoft/azure/sdk/iot/SendEventX509.java">this code</a> which references
     * a helper class for building SSLContext objects for x509 authentication as well as for SAS based authentication.
     * When not using this deprecated constructor, you can safely exclude the Bouncycastle dependencies that this library declares.
     * See <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-samples/send-event-x509/pom.xml">this pom.xml</a> for an example of how to do this.
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    @Deprecated
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
     * Creates a device client that uses the provided SSLContext for SSL negotiation
     * @param connString the connection string for the device. May be an x509 connection string (format: "HostName=...;DeviceId=...;x509=true")
     *                   and it may be a SAS connection string (format: "HostName=...;DeviceId=...;SharedAccessKey=..."). If
     *                   this connection string is an x509 connection string, the client will use the provided SSLContext for authentication.
     * @param protocol the protocol to use when communicating with IotHub
     * @param sslContext the ssl context that will be used during authentication. If the provided connection string does not contain
     *                   SAS based credentials, then the sslContext will be used for x509 authentication. If the provided connection string
     *                   does contain SAS based credentials, the sslContext will still be used during SSL negotiation.
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     * @deprecated For x509 authentication, use {@link #DeviceClient(String, IotHubClientProtocol, ClientOptions)} and provide
     * an SSLContext instance in the {@link ClientOptions} instance. For a sample on how to build this SSLContext,
     * see <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-samples/send-event-x509/src/main/java/samples/com/microsoft/azure/sdk/iot/SendEventX509.java">this code</a> which references
     * a helper class for building SSLContext objects for x509 authentication as well as for SAS based authentication.
     * When not using this deprecated constructor, you can safely exclude the Bouncycastle dependencies that this library declares.
     * See <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-samples/send-event-x509/pom.xml">this pom.xml</a> for an example of how to do this.
     * @throws URISyntaxException if the hostname in the connection string is not a valid URI
     */
    @Deprecated
    public DeviceClient(String connString, IotHubClientProtocol protocol, SSLContext sslContext) throws URISyntaxException
    {
        super(new IotHubConnectionString(connString), protocol, sslContext, SEND_PERIOD_MILLIS, getReceivePeriod(protocol));
        commonConstructorVerifications();
        commonConstructorSetup();
    }

    /**
     * Creates a device client that uses the provided security provider for authentication.
     *
     * @param uri The hostname of the iot hub to connect to (format: "yourHubName.azure-devices.net")
     * @param deviceId The id for the device to use
     * @param securityProvider The security provider for the device
     * @param protocol The protocol the device shall use for communication to the IoT Hub
     * @return The created device client instance
     * @throws URISyntaxException If the provided connString could not be parsed.
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws URISyntaxException, IOException
    {
        return new DeviceClient(uri, deviceId, securityProvider, protocol, null);
    }

    /**
     * Creates a device client that uses the provided security provider for authentication.
     *
     * @param uri The hostname of the iot hub to connect to (format: "yourHubName.azure-devices.net")
     * @param deviceId The id for the device to use
     * @param securityProvider The security provider for the device
     * @param protocol The protocol the device shall use for communication to the IoT Hub
     * @param clientOptions The options that allow configuration of the device client instance during initialization
     * @return The created device client instance
     * @throws URISyntaxException If the provided connString could not be parsed.
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, IOException
    {
        return new DeviceClient(uri, deviceId, securityProvider, protocol, clientOptions);
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
     * @param uri The hostname of iot hub to connect to (format: "yourHubName.azure-devices.net")
     * @param deviceId The id for the device to use
     * @param securityProvider The security provider for the device
     * @param protocol The protocol the device shall use for communication to the IoT Hub
     * @throws URISyntaxException If the provided connString could not be parsed.
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    private DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, IOException
    {
        super(uri, deviceId, securityProvider, protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol), clientOptions);
        commonConstructorSetup();
    }

    void closeFileUpload() throws IOException
    {
        if (this.fileUpload != null)
        {
            this.fileUpload.closeNow();
        }

        if (this.fileUploadTask != null)
        {
            this.fileUploadTask.close();
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
                log.debug("Connection already opened by TransportClient.");
            }
        }
        else
        {
            // Codes_SRS_DEVICECLIENT_21_006: [The open shall invoke super.open().]
            super.open();
        }

        log.info("Device client opened successfully");
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
                log.info("Connection already closed by TransportClient.");
            }
        }
        else
        {
            //Codes_SRS_DEVICECLIENT_34_040: [If this object is not using a transport client, it shall invoke super.close().]
            log.info("Closing device client...");
            super.close();
        }

        log.info("Device client closed successfully");
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
                log.info("Connection already closed by TransportClient.");
            }
        }
        else
        {
            //Codes_SRS_DEVICECLIENT_34_041: [If this object is not using a transport client, it shall invoke super.closeNow().]
            log.info("Closing device client...");
            super.closeNow();
            this.closeFileUpload();
        }

        log.info("Device client closed successfully");
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
     * @deprecated Use {@link #getFileUploadSasUri(FileUploadSasUriRequest)} to get the SAS URI, use the azure storage SDK to upload a file
     * to that SAS URI, and then use {@link #completeFileUpload(FileUploadCompletionNotification)} to notify Iot Hub that
     * your file upload has completed, successfully or otherwise. This method does all three of these tasks for you, but has limited configuration options.
     */
    @Deprecated
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

        if (this.fileUpload == null)
        {
            this.fileUpload = new FileUpload(this.config);
        }

        this.fileUpload.uploadToBlobAsync(destinationBlobName, inputStream, streamLength, callback, callbackContext);
    }

    /**
     * Get a file upload SAS URI which the Azure Storage SDK can use to upload a file to blob for this device. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#initialize-a-file-upload">this documentation</a> for more details.
     * @param request The request details for getting the SAS URI, including the destination blob name.
     * @return The file upload details to be used with the Azure Storage SDK in order to upload a file from this device.
     * @throws IOException If this HTTPS request fails to send.
     * @throws URISyntaxException If the returned sas uri cannot be constructed correctly
     */
    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IOException, URISyntaxException
    {
        if (this.fileUploadTask == null)
        {
            this.fileUploadTask = new FileUploadTask(new HttpsTransportManager(this.config));
        }

        return fileUploadTask.getFileUploadSasUri(request);
    }

    /**
     * Notify IoT Hub that a file upload has been completed, successfully or otherwise. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#notify-iot-hub-of-a-completed-file-upload">this documentation</a> for more details.
     * @param notification The notification details, including if the file upload succeeded.
     * @throws IOException If this HTTPS request fails to send.
     * @deprecated This function is not actually async, so use {@link #completeFileUpload(FileUploadCompletionNotification)} to avoid confusion
     */
    @Deprecated
    public void completeFileUploadAsync(FileUploadCompletionNotification notification) throws IOException
    {
        this.completeFileUpload(notification);
    }

    /**
     * Notify IoT Hub that a file upload has been completed, successfully or otherwise. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#notify-iot-hub-of-a-completed-file-upload">this documentation</a> for more details.
     * @param notification The notification details, including if the file upload succeeded.
     * @throws IOException If this HTTPS request fails to send.
     */
    public void completeFileUpload(FileUploadCompletionNotification notification) throws IOException
    {
        if (this.fileUploadTask == null)
        {
            this.fileUploadTask = new FileUploadTask(new HttpsTransportManager(this.config));
        }

        fileUploadTask.sendNotification(notification);
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
     * Starts the device twin. This device client will receive a callback with the current state of the full twin, including
     * reported properties and desired properties. After that callback is received, this device client will receive a callback
     * each time a desired property is updated. That callback will either contain the full desired properties set, or
     * only the updated desired property depending on how the desired property was changed. IoT Hub supports a PUT and a PATCH
     * on the twin. The PUT will cause this device client to receive the full desired properties set, and the PATCH
     * will cause this device client to only receive the updated desired properties. Similarly, the version
     * of each desired property will be incremented from a PUT call, and only the actually updated desired property will
     * have its version incremented from a PATCH call. The java service client library uses the PATCH call when updated desired properties,
     * but it builds the patch such that all properties are included in the patch. As a result, the device side will receive full twin
     * updates, not partial updates.
     *
     * See <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/twin/replacedevicetwin">PUT</a> and
     * <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a>
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
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        this.startTwinInternal(deviceTwinStatusCallback, deviceTwinStatusCallbackContext, genericPropertyCallBack, genericPropertyCallBackContext);
    }

    /**
     * Starts the device twin. This device client will receive a callback with the current state of the full twin, including
     * reported properties and desired properties. After that callback is received, this device client will receive a callback
     * each time a desired property is updated. That callback will either contain the full desired properties set, or
     * only the updated desired property depending on how the desired property was changed. IoT Hub supports a PUT and a PATCH
     * on the twin. The PUT will cause this device client to receive the full desired properties set, and the PATCH
     * will cause this device client to only receive the updated desired properties. Similarly, the version
     * of each desired property will be incremented from a PUT call, and only the actually updated desired property will
     * have its version incremented from a PATCH call. The java service client library uses the PATCH call when updated desired properties,
     * but it builds the patch such that all properties are included in the patch. As a result, the device side will receive full twin
     * updates, not partial updates.
     *
     * See <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/twin/replacedevicetwin">PUT</a> and
     * <a href="https://docs.microsoft.com/en-us/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a>
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
     *
     *	    - <b>SetSendInterval</b> - this option is applicable to all protocols.
     *	      This value sets the period (in milliseconds) that this SDK spawns threads to send queued messages.
     *	      Even if no message is queued, this thread will be spawned.
     *
     *	    - <b>SetReceiveInterval</b> - this option is applicable to all protocols
     *	      in case of HTTPS protocol, this option acts the same as {@code SetMinimumPollingInterval}
     *	      in case of MQTT and AMQP protocols, this option specifies the interval in milliseconds
     *	      between spawning a thread that dequeues a message from the SDK's queue of received messages.
     *
     *	    - <b>SetCertificatePath</b> - this option is applicable only
     *	      when the transport configured with this client is AMQP. This
     *	      option specifies the path to the certificate used to verify peer.
     *	      The value is expected to be of type {@code String}.
     *
     *      - <b>SetSASTokenExpiryTime</b> - this option is applicable for HTTP/
     *         AMQP/MQTT. This option specifies the interval in seconds after which
     *         SASToken expires. If the transport is already open then setting this
     *         option will restart the transport with the updated expiry time, and
     *         will use that expiry time length for all subsequently generated sas tokens.
     *         The value is expected to be of type {@code long}.
     *
     *      - <b>SetHttpsReadTimeout</b> - this option is applicable for HTTPS.
     *         This option specifies the read timeout in milliseconds per https request
     *         made by this client. By default, this value is 4 minutes.
     *         The value is expected to be of type {@code int}.
     *
     *      - <b>SetHttpsConnectTimeout</b> - this option is applicable for HTTPS.
     *         This option specifies the connect timeout in milliseconds per https request
     *         made by this client. By default, this value is 0 (no connect timeout).
     *         The value is expected to be of type {@code int}.
     *
     *      - <b>SetAmqpOpenAuthenticationSessionTimeout</b> - this option is applicable for AMQP with SAS token authentication.
     *         This option specifies the timeout in seconds to wait to open the authentication session.
     *         By default, this value is 20 seconds.
     *         The value is expected to be of type {@code int}.
     *
     *      - <b>SetAmqpOpenDeviceSessionsTimeout</b> - this option is applicable for AMQP.
     *         This option specifies the timeout in seconds to open the device sessions.
     *         By default, this value is 60 seconds.
     *         The value is expected to be of type {@code int}.
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
            throw new IllegalArgumentException("value is null");
        }

        switch (optionName)
        {
            // Codes_SRS_DEVICECLIENT_02_016: ["SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.]
            case SET_MINIMUM_POLLING_INTERVAL:
            case SET_RECEIVE_INTERVAL:
            case SET_HTTPS_CONNECT_TIMEOUT:
            case SET_HTTPS_READ_TIMEOUT:
            case SET_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT:
            case SET_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT:
            {
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
        log.debug("Setting SASTokenExpiryTime as {} seconds", value);

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