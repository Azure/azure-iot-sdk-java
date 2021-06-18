// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.*;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceClientException;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadClient;
import com.microsoft.azure.sdk.iot.device.transport.amqps.IoTHubConnectionType;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;

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
    private static final long SEND_PERIOD_MILLIS = 10L;
    private static final long RECEIVE_PERIOD_MILLIS_AMQPS = 10L;
    private static final long RECEIVE_PERIOD_MILLIS_MQTT = 10L;
    private static final long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/ //TODO this value is ridiculously long

    protected long RECEIVE_PERIOD_MILLIS;

    private IoTHubConnectionType ioTHubConnectionType = IoTHubConnectionType.UNKNOWN;

    private FileUploadClient fileUpload;

    private static final String MULTIPLEXING_CLOSE_ERROR_MESSAGE = "Cannot close a multiplexed client through this method. Must use multiplexingClient.unregisterDeviceClient(deviceClient)";
    private static final String MULTIPLEXING_OPEN_ERROR_MESSAGE = "Cannot open a multiplexed client through this method. Must use multiplexingClient.registerDeviceClient(deviceClient)";

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
     * @throws SecurityProviderException If the SecurityProvider throws any exception while authenticating
     */
    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol) throws URISyntaxException, SecurityProviderException
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
     * @throws SecurityProviderException If the SecurityProvider throws any exception while authenticating
     */
    public static DeviceClient createFromSecurityProvider(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, SecurityProviderException
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
     * @throws SecurityProviderException If the SecurityProvider throws any exception while authenticating
     */
    private DeviceClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws URISyntaxException, SecurityProviderException
    {
        super(uri, deviceId, securityProvider, protocol, SEND_PERIOD_MILLIS, getReceivePeriod(protocol), clientOptions);
        commonConstructorSetup();
    }

    void closeFileUpload()
    {
        if (this.fileUpload != null)
        {
            this.fileUpload.close();
        }
    }

    private void commonConstructorSetup()
    {
        this.ioTHubConnectionType = IoTHubConnectionType.SINGLE_CLIENT;
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
     * @throws DeviceClientException if a connection to an IoT Hub cannot be established.
     */
    public void open() throws DeviceClientException
    {
        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_MULTIPLEXING_CLIENT)
        {
            throw new UnsupportedOperationException(MULTIPLEXING_OPEN_ERROR_MESSAGE);
        }

        super.open();

        log.info("Device client opened successfully");
    }

    /**
     * Completes all current outstanding requests and closes the IoT Hub client.
     * Must be called to terminate the background thread that is sending data to
     * IoT Hub. After {@code closeNow()} is called, the IoT Hub client is no longer
     * usable. If the client is already closed, the function shall do nothing.
     * @deprecated : As of release 1.1.25 this call is replaced by {@link #closeNow()}
     *
     * @throws DeviceClientException if the connection to an IoT Hub cannot be closed.
     */
    @Deprecated
    public void close() throws DeviceClientException
    {
        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_MULTIPLEXING_CLIENT)
        {
            throw new UnsupportedOperationException(MULTIPLEXING_CLOSE_ERROR_MESSAGE);
        }

        log.info("Closing device client...");
        super.close();

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
     * @throws DeviceClientException if the connection to an IoT Hub cannot be closed.
     */
    public void closeNow() throws DeviceClientException
    {
        if (this.ioTHubConnectionType == IoTHubConnectionType.USE_MULTIPLEXING_CLIENT)
        {
            throw new UnsupportedOperationException(MULTIPLEXING_CLOSE_ERROR_MESSAGE);
        }

        log.info("Closing device client...");
        super.closeNow();
        this.closeFileUpload();

        log.info("Device client closed successfully");
    }

    /**
     * Get a file upload SAS URI which the Azure Storage SDK can use to upload a file to blob for this device. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#initialize-a-file-upload">this documentation</a> for more details.
     * @param request The request details for getting the SAS URI, including the destination blob name.
     * @return The file upload details to be used with the Azure Storage SDK in order to upload a file from this device.
     * @throws DeviceClientException If this HTTPS request fails to send.
     */
    public FileUploadSasUriResponse getFileUploadSasUri(FileUploadSasUriRequest request) throws DeviceClientException
    {
        if (this.fileUpload == null)
        {
            this.fileUpload = new FileUploadClient(this.config);
        }

        return fileUpload.getFileUploadSasUri(request);
    }

    /**
     * Notify IoT Hub that a file upload has been completed, successfully or otherwise. See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-file-upload#notify-iot-hub-of-a-completed-file-upload">this documentation</a> for more details.
     * @param notification The notification details, including if the file upload succeeded.
     * @throws DeviceClientException If this HTTPS request fails to send.
     */
    public void completeFileUpload(FileUploadCompletionNotification notification) throws DeviceClientException
    {
        if (this.fileUpload == null)
        {
            this.fileUpload = new FileUploadClient(this.config);
        }

        fileUpload.sendNotification(notification);
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
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.
     * @param <Type1> The type of the desired property key. Since the twin is a json object, the key will always be a String.
     * @param <Type2> The type of the desired property value.
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public <Type1, Type2> void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object deviceTwinStatusCallbackContext,
                                        PropertyCallBack<Type1, Type2> genericPropertyCallBack, Object genericPropertyCallBackContext)
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
     * @param genericPropertiesCallBack the TwinPropertyCallBack callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallBackContext the context to be passed to the property callback. Can be {@code null}.
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object deviceTwinStatusCallbackContext,
                                TwinPropertiesCallback genericPropertiesCallBack, Object genericPropertyCallBackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        this.startTwinInternal(deviceTwinStatusCallback, deviceTwinStatusCallbackContext, genericPropertiesCallBack, genericPropertyCallBackContext);
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

    // Used by multiplexing clients to signal to this client what kind of multiplexing client is using this device client
    @SuppressWarnings("SameParameterValue") // The connection type is currently only set to "multiplexing client", but it can be set to the deprecated transport client as well.
    void setConnectionType(IoTHubConnectionType connectionType)
    {
        this.ioTHubConnectionType = connectionType;
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
     */
    public void setOption(String optionName, Object value) throws DeviceClientException
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

        // deviceIO is only ever null when a client was registered to a multiplexing client, became unregistered, and hasn't be re-registered yet.
        if (this.deviceIO == null)
        {
            throw new UnsupportedOperationException("Must re-register this client to a multiplexing client before using it");
        }

        switch (optionName)
        {
            case SET_MINIMUM_POLLING_INTERVAL:
            case SET_RECEIVE_INTERVAL:
            case SET_HTTPS_CONNECT_TIMEOUT:
            case SET_HTTPS_READ_TIMEOUT:
            case SET_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT:
            case SET_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT:
            {
                break;
            }
            case SET_SAS_TOKEN_EXPIRY_TIME:
            {
                setOption_SetSASTokenExpiryTime(value);
                return;
            }
            default:
            {
                throw new IllegalArgumentException("optionName is unknown = " + optionName);
            }
        }

        super.setOption(optionName, value);
    }

    // The warning is for how getSasTokenAuthentication() may return null, but the check that our config uses SAS_TOKEN
    // auth is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    @SuppressWarnings("ConstantConditions")
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
}