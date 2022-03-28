// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
public final class DeviceClient extends InternalClient
{
    private DeviceClientType deviceClientType = DeviceClientType.SINGLE_CLIENT;

    private FileUpload fileUpload;

    private static final String MULTIPLEXING_CLOSE_ERROR_MESSAGE = "Cannot close a multiplexed client through this method. Must use multiplexingClient.unregisterDeviceClient(deviceClient)";
    private static final String MULTIPLEXING_OPEN_ERROR_MESSAGE = "Cannot open a multiplexed client through this method. Must use multiplexingClient.registerDeviceClient(deviceClient)";

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param connectionString the connection string. The connection string is a set
     * of key-value pairs that are separated by ';', with the keys and values
     * separated by '='. It should contain values for the following keys:
     * {@code HostName}, {@code DeviceId}, and {@code SharedAccessKey}.
     * @param protocol the communication protocol used (i.e. HTTPS).
     *
     * @throws IllegalArgumentException if any of {@code connectionString} or
     * {@code protocol} are {@code null}; or if {@code connectionString} is missing
     * one of the following attributes:{@code HostName}, {@code DeviceId}, or
     * {@code SharedAccessKey} or if the IoT hub hostname does not conform to
     * RFC 3986 or if the provided {@code connectionString} is for an x509 authenticated device
     */
    public DeviceClient(String connectionString, IotHubClientProtocol protocol) throws IllegalArgumentException
    {
        this(connectionString, protocol, null);
    }

    /**
     * Constructor that takes a connection string as an argument.
     *
     * @param connectionString the connection string. The connection string is a set
     * of key-value pairs that are separated by ';', with the keys and values
     * separated by '='. It should contain values for the following keys:
     * {@code HostName}, {@code DeviceId}, and {@code SharedAccessKey}.
     * @param protocol the communication protocol used (i.e. HTTPS)
     * @param clientOptions The options that allow configuration of the device client instance during initialization
     *
     * @throws IllegalArgumentException if any of {@code connectionString} or
     * {@code protocol} are {@code null}; or if {@code connectionString} is missing
     * one of the following attributes:{@code HostName}, {@code DeviceId}, or
     * {@code SharedAccessKey} or if the IoT hub hostname does not conform to
     * RFC 3986 or if the provided {@code connectionString} is for an x509 authenticated device
     */
    public DeviceClient(String connectionString, IotHubClientProtocol protocol, ClientOptions clientOptions) throws IllegalArgumentException
    {
        super(new IotHubConnectionString(connectionString), protocol, clientOptions);
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
        super(hostName, deviceId, null, sasTokenProvider, protocol, clientOptions);
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
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    public DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws IOException
    {
        this(uri, deviceId, securityProvider, protocol, null);
    }

    /**
     * Creates a device client that uses the provided security provider for authentication.
     *
     * @param uri The hostname of the iot hub to connect to (format: "yourHubName.azure-devices.net")
     * @param deviceId The id for the device to use
     * @param securityProvider The security provider for the device
     * @param protocol The protocol the device shall use for communication to the IoT Hub
     * @param clientOptions The options that allow configuration of the device client instance during initialization
     * @throws IOException If the SecurityProvider throws any exception while authenticating
     */
    public DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws IOException
    {
        super(uri, deviceId, securityProvider, protocol, clientOptions);
        commonConstructorSetup();
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

    private void commonConstructorSetup()
    {
        log.debug("Initialized a DeviceClient instance using SDK version {}", TransportUtils.CLIENT_VERSION);
    }

    private void commonConstructorVerifications() throws UnsupportedOperationException
    {
        if (this.getConfig().getModuleId() != null && !this.getConfig().getModuleId().isEmpty())
        {
            throw new UnsupportedOperationException("DeviceClient connection string cannot contain module id. Use ModuleClient instead.");
        }
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT hub. If
     * the client is already open, the function shall do nothing.
     *
     * @param withRetry if true, this open call will apply the retry policy to allow for the open call to be retried if
     * it fails. Both the operation timeout set in {@link #setOperationTimeout(long)} and the retry policy set in
     * {{@link #setRetryPolicy(RetryPolicy)}} will be respected while retrying to open the connection.
     *
     * @throws IotHubClientException if a connection to an IoT hub cannot be established or if the connection can be
     * established but the service rejects it for any reason.
     */
    public void open(boolean withRetry) throws IotHubClientException
    {
        if (this.deviceClientType == DeviceClientType.USE_MULTIPLEXING_CLIENT)
        {
            throw new UnsupportedOperationException(MULTIPLEXING_OPEN_ERROR_MESSAGE);
        }

        super.open(withRetry);

        log.info("Device client opened successfully");
    }

    /**
     * Closes the IoT hub client by releasing any resources held by client. When
     * close is called all the messages that were in transit or pending to be
     * sent will be informed to the user in the callbacks and any existing
     * connection to IotHub will be closed.
     * Must be called to terminate the background thread that is sending data to
     * IoT hub. After close is called, the IoT hub client must be opened again
     * before it can be used again. If the client is already closed,
     * the function shall do nothing.
     *
     * @throws UnsupportedOperationException if called on a device that is multiplexed.
     */
    public void close()
    {
        if (this.deviceClientType == DeviceClientType.USE_MULTIPLEXING_CLIENT)
        {
            throw new UnsupportedOperationException(MULTIPLEXING_CLOSE_ERROR_MESSAGE);
        }

        log.info("Closing device client...");

        if (this.fileUpload != null)
        {
            this.fileUpload.close();
        }

        super.close();

        log.info("Device client closed successfully");
    }

    /**
     * Get a file upload SAS URI which the Azure Storage SDK can use to upload a file to blob for this device. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#initialize-a-file-upload">this documentation</a> for more details.
     * @param request The request details for getting the SAS URI, including the destination blob name.
     * @return The file upload details to be used with the Azure Storage SDK in order to upload a file from this device.
     * @throws IOException If this HTTPS request fails to send.
     */
    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws IotHubClientException
    {
        if (this.fileUpload == null)
        {
            this.fileUpload = new FileUpload(new HttpsTransportManager(this.config));
        }

        return this.fileUpload.getFileUploadSasUri(request);
    }

    /**
     * Notify IoT hub that a file upload has been completed, successfully or otherwise. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#notify-iot-hub-of-a-completed-file-upload">this documentation</a> for more details.
     * @param notification The notification details, including if the file upload succeeded.
     * @throws IOException If this HTTPS request fails to send.
     */
    public void completeFileUpload(FileUploadCompletionNotification notification) throws IotHubClientException
    {
        if (this.fileUpload == null)
        {
            this.fileUpload = new FileUpload(new HttpsTransportManager(this.config));
        }

        this.fileUpload.sendNotification(notification);
    }

    /**
     * Returns if this client is or ever was registered to a {@link MultiplexingClient} instance. Device clients that were
     * cannot be used in non-multiplexed connections. Device clients that aren't registered to any multiplexing client
     * will still return true.
     * @return true if this client is or ever was registered to a {@link MultiplexingClient} instance, false otherwise.
     */
    public boolean isMultiplexed()
    {
        return this.isMultiplexed;
    }

    // Used by multiplexing clients to signal to this client what kind of multiplexing client is using this device client
    void markAsMultiplexed()
    {
        this.deviceClientType = DeviceClientType.USE_MULTIPLEXING_CLIENT;
    }

    @SuppressWarnings("unused")
    private DeviceClient()
    {
        // empty constructor for mocking purposes only
    }
}
