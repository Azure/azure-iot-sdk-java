/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.twin.DeviceMethod;
import com.microsoft.azure.sdk.iot.device.twin.DeviceMethodCallback;
import com.microsoft.azure.sdk.iot.device.twin.DeviceTwin;
import com.microsoft.azure.sdk.iot.device.twin.Pair;
import com.microsoft.azure.sdk.iot.device.twin.Property;
import com.microsoft.azure.sdk.iot.device.twin.PropertyCallback;
import com.microsoft.azure.sdk.iot.device.twin.TwinPropertiesCallback;
import com.microsoft.azure.sdk.iot.device.twin.TwinPropertyCallback;
import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.*;

@Slf4j
public class InternalClient
{
    // SET_MINIMUM_POLLING_INTERVAL is used for setting the interval for https message polling.
    private static final String SET_MINIMUM_POLLING_INTERVAL = "SetMinimumPollingInterval";
    // SET_RECEIVE_INTERVAL is used for setting the interval for handling MQTT and AMQP messages.
    private static final String SET_RECEIVE_INTERVAL = "SetReceiveInterval";
    private static final String SET_SEND_INTERVAL = "SetSendInterval";
    private static final String SET_MAX_MESSAGES_SENT_PER_THREAD = "SetMaxMessagesSentPerThread";
    private static final String SET_SAS_TOKEN_EXPIRY_TIME = "SetSASTokenExpiryTime";
    private static final String SET_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT = "SetAmqpOpenAuthenticationSessionTimeout";
    private static final String SET_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT = "SetAmqpOpenDeviceSessionsTimeout";

    private static final String SET_HTTPS_CONNECT_TIMEOUT = "SetHttpsConnectTimeout";
    private static final String SET_HTTPS_READ_TIMEOUT = "SetHttpsReadTimeout";

    private static final String TWIN_OVER_HTTP_ERROR_MESSAGE =
        "Twin operations are only supported over MQTT, MQTT_WS, AMQPS, and AMQPS_WS";

    private static final String METHODS_OVER_HTTP_ERROR_MESSAGE =
        "Direct methods are only supported over MQTT, MQTT_WS, AMQPS, and AMQPS_WS";

    DeviceClientConfig config;
    private DeviceIO deviceIO;

    boolean isMultiplexed = false;

    private IotHubConnectionStatusChangeCallback connectionStatusChangeCallback;
    private Object connectionStatusChangeCallbackContext;

    private DeviceTwin twin;
    private DeviceMethod method;

    InternalClient(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis, ClientOptions clientOptions)
    {
        commonConstructorVerification(iotHubConnectionString, protocol);

        this.config = new DeviceClientConfig(iotHubConnectionString, clientOptions);
        this.config.setProtocol(protocol);
        if (clientOptions != null) {
            this.config.modelId = clientOptions.getModelId();
        }

        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);
    }

    InternalClient(IotHubAuthenticationProvider iotHubAuthenticationProvider, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis)
    {
        this.config = new DeviceClientConfig(iotHubAuthenticationProvider);
        this.config.setProtocol(protocol);
        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);
    }

    InternalClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, long sendPeriodMillis, long receivePeriodMillis, ClientOptions clientOptions) throws URISyntaxException, IOException
    {
        if (protocol == null)
        {
            throw new IllegalArgumentException("The transport protocol cannot be null");
        }

        if (securityProvider == null)
        {
            throw new IllegalArgumentException("securityProvider cannot be null");
        }

        if (uri == null || uri.isEmpty())
        {
            throw new IllegalArgumentException("URI cannot be null or empty");
        }

        if (deviceId == null || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("deviceId cannot be null or empty");
        }

        IotHubConnectionString connectionString = new IotHubConnectionString(uri, deviceId, null, null);

        this.config = new DeviceClientConfig(connectionString, securityProvider);
        this.config.setProtocol(protocol);
        if (clientOptions != null) {
            this.config.modelId = clientOptions.getModelId();
        }

        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);
    }

    InternalClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions, long sendPeriodMillis, long receivePeriodMillis)
    {
        if (hostName == null)
        {
            throw new IllegalArgumentException("Host name cannot be null");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("Protocol cannot be null.");
        }

        this.config = new DeviceClientConfig(hostName, sasTokenProvider, clientOptions, deviceId, moduleId);
        this.config.setProtocol(protocol);
        if (clientOptions != null)
        {
            this.config.modelId = clientOptions.getModelId();
        }

        this.deviceIO = new DeviceIO(this.config, sendPeriodMillis, receivePeriodMillis);
    }

    //for mocking purposes only
    InternalClient()
    {
        this.config = null;
        this.deviceIO = null;
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT hub. If
     * the client is already open, the function shall do nothing.
     *
     * @throws IOException if a connection to an IoT hub cannot be established.
     */
    public void open() throws IOException
    {
        this.open(false);
    }

    /**
     * Starts asynchronously sending and receiving messages from an IoT hub. If
     * the client is already open, the function shall do nothing.
     *
     * @param withRetry if true, this open call will apply the retry policy to allow for the open call to be retried if
     * it fails. Both the operation timeout set in {@link #setOperationTimeout(long)} and the retry policy set in
     * {{@link #setRetryPolicy(RetryPolicy)}} will be respected while retrying to open the connection.
     *
     * @throws IOException if a connection to an IoT hub cannot be established.
     */
    // The warning is for how getSasTokenAuthentication() may return null, but the check that our config uses SAS_TOKEN
    // auth is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    @SuppressWarnings("ConstantConditions")
    public void open(boolean withRetry) throws IOException
    {
        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.config.getSasTokenAuthentication().isAuthenticationProviderRenewalNecessary())
        {
            throw new SecurityException("Your SasToken is expired");
        }

        this.deviceIO.open(withRetry);
    }

    public void close() throws IOException
    {
        //noinspection StatementWithEmptyBody
        while (!this.deviceIO.isEmpty())
        {
            // Don't do anything until the transport layer underneath has indicated that it doesn't have any more pending messages to send.
        }

        this.deviceIO.close();
    }

    public void closeNow() throws IOException
    {
        this.deviceIO.close();
    }

    /**
     * Asynchronously sends an event message to the IoT hub.
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
        verifyRegisteredIfMultiplexing();
        message.setConnectionDeviceId(this.config.getDeviceId());
        deviceIO.sendEventAsync(message, callback, callbackContext, this.config.getDeviceId());
    }

    /**
     * Asynchronously sends a batch of messages to the IoT hub
     * HTTPS messages will be sent in a single batch and MQTT and AMQP messages will be sent individually.
     * In case of HTTPS, This API call is an all-or-nothing single HTTPS message and the callback will be triggered only once.
     * Maximum payload size for HTTPS is 255KB
     *
     * @param messages the list of message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is
     * already closed.
     */
    public void sendEventBatchAsync(List<Message> messages, IotHubEventCallback callback, Object callbackContext)
    {
        verifyRegisteredIfMultiplexing();

        for (Message message: messages)
        {
            message.setConnectionDeviceId(this.config.getDeviceId());
        }

        Message message = new BatchMessage(messages);

        deviceIO.sendEventAsync(message, callback, callbackContext, this.config.getDeviceId());
    }

    /**
     * Subscribes to desired properties.
     *
     * This client will receive a callback each time a desired property is updated. That callback will either contain
     * the full desired properties set, or only the updated desired property depending on how the desired property was changed.
     * IoT hub supports a PUT and a PATCH on the twin. The PUT will cause this client to receive the full desired properties set, and the PATCH
     * will cause this client to only receive the updated desired properties. Similarly, the version
     * of each desired property will be incremented from a PUT call, and only the actually updated desired property will
     * have its version incremented from a PATCH call. The java service client library uses the PATCH call when updated desired properties,
     * but it builds the patch such that all properties are included in the patch. As a result, the device side will receive full twin
     * updates, not partial updates.
     *
     * See <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/replacedevicetwin">PUT</a> and
     * <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a>
     *
     * @param onDesiredPropertyChange the Map for desired properties and their corresponding callback and context. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     */
    public void subscribeToDesiredPropertiesAsync(Map<Property, Pair<PropertyCallback<String, Object>, Object>> onDesiredPropertyChange) throws IOException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (this.twin == null)
        {
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        this.twin.subscribeDesiredPropertiesNotification(onDesiredPropertyChange);
    }

    /**
     * Subscribes to desired properties
     *
     * @param onDesiredPropertyChange the Map for desired properties and their corresponding callback and context. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     */
    public void subscribeToTwinDesiredPropertiesAsync(Map<Property, Pair<TwinPropertyCallback, Object>> onDesiredPropertyChange) throws IOException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (this.twin == null)
        {
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

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
    public void sendReportedPropertiesAsync(Set<Property> reportedProperties) throws IOException, IllegalArgumentException
    {
        this.sendReportedPropertiesAsync(reportedProperties, null, null, null, null, null);
    }

    /**
     * Sends reported properties
     *
     * @param reportedProperties the Set for desired properties and their corresponding callback and context. Cannot be {@code null}.
     * @param version the Reported property version. Cannot be negative.
     *
     * @throws IOException if called when client is not opened or called before starting twin.
     * @throws IllegalArgumentException if reportedProperties is null or empty or if version is negative
     */
    public void sendReportedPropertiesAsync(Set<Property> reportedProperties, int version) throws IOException, IllegalArgumentException
    {
        if (version < 0)
        {
            throw new IllegalArgumentException("Version cannot be negative.");
        }
        this.sendReportedPropertiesAsync(reportedProperties, version, null, null, null, null);
    }

    /**
     * Sends reported properties
     * @param reportedPropertiesParameters Container for the reported properties parameters
     * @throws IOException if called when client is not opened or called before starting twin.
     * @throws IllegalArgumentException if reportedProperties is null or empty or if version specified in {#reportedPropertiesParameters} is negative
     */
    public void sendReportedPropertiesAsync(ReportedPropertiesParameters reportedPropertiesParameters) throws IOException, IllegalArgumentException
    {
        this.sendReportedPropertiesAsync(reportedPropertiesParameters.getReportedProperties(), reportedPropertiesParameters.getVersion(), reportedPropertiesParameters.getCorrelatingMessageCallback(), reportedPropertiesParameters.getCorrelatingMessageCallbackContext(), reportedPropertiesParameters.getReportedPropertiesCallback(), reportedPropertiesParameters.getReportedPropertiesCallbackContext());
    }

    /**
     * Sends reported properties
     *
     * @param reportedProperties the Set for desired properties and their corresponding callback and context. Cannot be {@code null}.
     * @param version the Reported property version. Cannot be negative.
     * @param reportedPropertiesCallback the Reported property callback to be set for this message. If set to {@code null} it will fall back to {@link #sendReportedPropertiesAsync(Set, int)}.
     * @param reportedPropertiesCallbackContext the Reported property callback context to be set for this message.
     * @param correlatingMessageCallback the correlation callback for this message.
     * @param correlatingMessageCallbackContext the correlation callback context for this message.
     * @throws IOException if called when client is not opened or called before starting twin.
     * @throws IllegalArgumentException if reportedProperties is null or empty or if version is negative
     */
    public void sendReportedPropertiesAsync(Set<Property> reportedProperties, Integer version, CorrelatingMessageCallback correlatingMessageCallback, Object correlatingMessageCallbackContext, IotHubEventCallback reportedPropertiesCallback, Object reportedPropertiesCallbackContext) throws IOException, IllegalArgumentException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        verifyReportedProperties(reportedProperties);

        this.twin.updateReportedProperties(reportedProperties, version, correlatingMessageCallback, correlatingMessageCallbackContext, reportedPropertiesCallback, reportedPropertiesCallbackContext);
    }

    /**
     * Sets the callback to be executed when the connection status of the device changes. The callback will be fired
     * with a status and a reason why the device's status changed. When the callback is fired, the provided context will
     * be provided alongside the status and reason.
     *
     * <p>Note that the thread used to deliver this callback should not be used to call open()/closeNow() on the client
     * that this callback belongs to. All open()/closeNow() operations should be done on a separate thread</p>
     *
     * @param callback The callback to be fired when the connection status of the device changes. Can be null to
     *                 unset this listener as long as the provided callbackContext is also null.
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     * @throws IllegalArgumentException if provided callback is null
     */
    public void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext) throws IllegalArgumentException
    {
        this.connectionStatusChangeCallback = callback;
        this.connectionStatusChangeCallbackContext = callbackContext;

        if (this.deviceIO != null)
        {
            this.deviceIO.registerConnectionStatusChangeCallback(callback, callbackContext, this.getConfig().getDeviceId());
        }
    }

    /**
     * Sets the given retry policy on the underlying transport
     * <a href="https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/retryPolicy.md">
     *     See more details about the default retry policy and about using custom retry policies here</a>
     * @param retryPolicy the new interval in milliseconds
     */
    public void setRetryPolicy(RetryPolicy retryPolicy)
    {
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
        this.config.setOperationTimeout(timeout);
    }

    public ProductInfo getProductInfo()
    {
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
     *	    - <b>SetMaxMessagesSentPerThread</b> - this option is applicable to all protocols.
     *	      This option specifies how many messages a given send thread should attempt to send before exiting.
     *	      This option can be used in conjunction with "SetSendInterval" to control the how frequently and in what
     *	      batch size messages are sent. By default, this client sends 10 messages per send thread, and spawns
     *	      a send thread every 10 milliseconds. This gives a theoretical throughput of 1000 messages per second.
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
    // The warning is for how getSasTokenAuthentication() may return null, but the check that our config uses SAS_TOKEN
    // auth is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    public void setOption(String optionName, Object value)
    {
        if (optionName == null)
        {
            throw new IllegalArgumentException("optionName is null");
        }
        else if (value == null)
        {
            throw new IllegalArgumentException("value is null");
        }
        else
        {
            switch (optionName)
            {
                case SET_MINIMUM_POLLING_INTERVAL:
                case SET_RECEIVE_INTERVAL:
                {
                    setOption_SetMinimumPollingInterval(value);
                    break;
                }
                case SET_SEND_INTERVAL:
                {
                    setOption_SetSendInterval(value);
                    break;
                }
                case SET_MAX_MESSAGES_SENT_PER_THREAD:
                {
                    setOption_SetMaxMessagesSentPerThread(value);
                    break;
                }
                case SET_SAS_TOKEN_EXPIRY_TIME:
                {
                    setOption_SetSASTokenExpiryTime(value);
                    break;
                }
                case SET_HTTPS_CONNECT_TIMEOUT:
                {
                    setOption_SetHttpsConnectTimeout(value);
                    break;
                }
                case SET_HTTPS_READ_TIMEOUT:
                {
                    setOption_SetHttpsReadTimeout(value);
                    break;
                }
                case SET_AMQP_OPEN_AUTHENTICATION_SESSION_TIMEOUT:
                {
                    setOption_SetAmqpOpenAuthenticationSessionTimeout(value);
                    return;
                }
                case SET_AMQP_OPEN_DEVICE_SESSIONS_TIMEOUT:
                {
                    setOption_SetAmqpOpenDeviceSessionsTimeout(value);
                    return;
                }
                default:
                {
                    throw new IllegalArgumentException("optionName is unknown = " + optionName);
                }
            }
        }
    }

    /**
     * Starts the twin for this client. This client will receive a callback with the current state of the full twin, including
     * reported properties and desired properties. After that callback is received, this client will receive a callback
     * each time a desired property is updated. That callback will either contain the full desired properties set, or
     * only the updated desired property depending on how the desired property was changed. IoT hub supports a PUT and a PATCH
     * on the twin. The PUT will cause this client to receive the full desired properties set, and the PATCH
     * will cause this client to only receive the updated desired properties. Similarly, the version
     * of each desired property will be incremented from a PUT call, and only the actually updated desired property will
     * have its version incremented from a PATCH call. The java service client library uses the PATCH call when updated desired properties,
     * but it builds the patch such that all properties are included in the patch. As a result, the device side will receive full twin
     * updates, not partial updates.
     *
     * See <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/replacedevicetwin">PUT</a> and
     * <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a>
     *
     * @param twinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param twinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallback the PropertyCallback callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallbackContext the context to be passed to the property callback. Can be {@code null}.
     * @param <Type1> The type of the desired property key. Since the twin is a json object, the key will always be a String.
     * @param <Type2> The type of the desired property value.
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public <Type1, Type2> void startTwinAsync(IotHubEventCallback twinStatusCallback, Object twinStatusCallbackContext,
                                 PropertyCallback<Type1, Type2> genericPropertyCallback, Object genericPropertyCallbackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (twinStatusCallback == null || genericPropertyCallback == null)
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
                    genericPropertyCallback,
                    genericPropertyCallbackContext);

            twin.getDeviceTwin();
        }
        else
        {
            throw new UnsupportedOperationException("You have already initialised twin");
        }
    }

    /**
     * Starts the twin for this client. This client will receive a callback with the current state of the full twin, including
     * reported properties and desired properties. After that callback is received, this client will receive a callback
     * each time a desired property is updated. That callback will either contain the full desired properties set, or
     * only the updated desired property depending on how the desired property was changed. IoT hub supports a PUT and a PATCH
     * on the twin. The PUT will cause this client to receive the full desired properties set, and the PATCH
     * will cause this client to only receive the updated desired properties. Similarly, the version
     * of each desired property will be incremented from a PUT call, and only the actually updated desired property will
     * have its version incremented from a PATCH call. The java service client library uses the PATCH call when updated desired properties,
     * but it builds the patch such that all properties are included in the patch. As a result, the device side will receive full twin
     * updates, not partial updates.
     *
     * See <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/replacedevicetwin">PUT</a> and
     * <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a>
     *
     * @param twinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param twinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertyCallback the TwinPropertyCallback callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallbackContext the context to be passed to the property callback. Can be {@code null}.     *
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public void startTwinAsync(IotHubEventCallback twinStatusCallback, Object twinStatusCallbackContext,
                               TwinPropertyCallback genericPropertyCallback, Object genericPropertyCallbackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (twinStatusCallback == null || genericPropertyCallback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        if (this.twin == null)
        {
            twin = new DeviceTwin(this.deviceIO, this.config, twinStatusCallback, twinStatusCallbackContext,
                    genericPropertyCallback, genericPropertyCallbackContext);
            twin.getDeviceTwin();
        }
        else
        {
            throw new UnsupportedOperationException("You have already initialised twin");
        }
    }

    /**
     * Starts the twin. This client will receive a callback with the current state of the full twin, including
     * reported properties and desired properties. After that callback is received, this client will receive a callback
     * each time a desired property is updated. That callback will either contain the full desired properties set, or
     * only the updated desired property depending on how the desired property was changed. IoT hub supports a PUT and a PATCH
     * on the twin. The PUT will cause this client to receive the full desired properties set, and the PATCH
     * will cause this client to only receive the updated desired properties. Similarly, the version
     * of each desired property will be incremented from a PUT call, and only the actually updated desired property will
     * have its version incremented from a PATCH call. The java service client library uses the PATCH call when updated desired properties,
     * but it builds the patch such that all properties are included in the patch. As a result, the device side will receive full twin
     * updates, not partial updates.
     *
     * See <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/replacedevicetwin">PUT</a> and
     * <a href="https://docs.microsoft.com/rest/api/iothub/service/twin/updatedevicetwin">PATCH</a>
     *
     * @param twinStatusCallback the IotHubEventCallback callback for providing the status of Device Twin operations. Cannot be {@code null}.
     * @param twinStatusCallbackContext the context to be passed to the status callback. Can be {@code null}.
     * @param genericPropertiesCallback the TwinPropertyCallback callback for providing any changes in desired properties. Cannot be {@code null}.
     * @param genericPropertyCallbackContext the context to be passed to the property callback. Can be {@code null}.
     *
     * @throws IllegalArgumentException if the callback is {@code null}
     * @throws UnsupportedOperationException if called more than once on the same device
     * @throws IOException if called when client is not opened
     */
    public void startTwinAsync(IotHubEventCallback twinStatusCallback, Object twinStatusCallbackContext,
                           TwinPropertiesCallback genericPropertiesCallback, Object genericPropertyCallbackContext)
            throws IOException, IllegalArgumentException, UnsupportedOperationException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        if (twinStatusCallback == null || genericPropertiesCallback == null)
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
                    genericPropertiesCallback,
                    genericPropertyCallbackContext);
            twin.getDeviceTwin();
        }
        else
        {
            throw new UnsupportedOperationException("You have already initialised twin");
        }
    }

    /**
     * Get the twin for this client. This method sends a request for the twin to the service and will asynchronously
     * provide the retrieved twin to the callback provided in {@link #startTwinAsync(IotHubEventCallback, Object, TwinPropertyCallback, Object)}.
     *
     * Users must call {@link #startTwinAsync(IotHubEventCallback, Object, TwinPropertyCallback, Object)} before using this method.
     * @throws IOException if the iot hub cannot be reached.
     */
    public void getTwinAsync() throws IOException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (this.twin == null)
        {
            throw new IOException("Start twin before using it");
        }

        if (!this.deviceIO.isOpen())
        {
            throw new IOException("Open the client connection before using it.");
        }

        this.twin.getDeviceTwin();
    }

    /**
     * Subscribes to direct methods
     *
     * @param methodCallback Callback on which direct methods shall be invoked. Cannot be {@code null}.
     * @param methodCallbackContext Context for device method callback. Can be {@code null}.
     * @param methodStatusCallback Callback for providing IotHub status for direct methods. Cannot be {@code null}.
     * @param methodStatusCallbackContext Context for device method status callback. Can be {@code null}.
     *
     * @throws IOException if called when client is not opened.
     * @throws IllegalArgumentException if either callback are null.
     */
    public void subscribeToMethodsAsync(DeviceMethodCallback methodCallback, Object methodCallbackContext,
                                        IotHubEventCallback methodStatusCallback, Object methodStatusCallbackContext)
            throws IOException
    {
        verifyRegisteredIfMultiplexing();
        verifyMethodsAreSupported();

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
     * Set the proxy settings for this client to connect through. If null then any previous settings will be erased
     * @param proxySettings the settings to be used when connecting to iothub through a proxy. If null, any previously saved
     *                      settings will be erased, and no proxy will be used
     */
    public void setProxySettings(ProxySettings proxySettings)
    {
        if (this.isMultiplexed)
        {
            throw new IllegalStateException(
                    "Cannot set the proxy settings of a multiplexed device. " +
                            "Proxy settings for the multiplexed connection can only be set at multiplexing client constructor time.");
        }

        verifyRegisteredIfMultiplexing();

        if (this.deviceIO.isOpen())
        {
            throw new IllegalStateException("Cannot set proxy after connection was already opened");
        }

        IotHubClientProtocol protocol = this.deviceIO.getProtocol();
        if (protocol != HTTPS && protocol != AMQPS_WS && protocol != MQTT_WS && proxySettings != null)
        {
            throw new IllegalArgumentException("Use of proxies is unsupported unless using HTTPS, MQTT_WS or AMQPS_WS");
        }

        this.config.setProxySettings(proxySettings);
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

    // only used by the MultiplexingClient class to signal to this client that it needs to re-register twin
    // callbacks
    void markTwinAsUnsubscribed()
    {
        this.twin = null;
    }

    // only used by the MultiplexingClient class to signal to this client that it needs to re-register methods
    // callbacks
    void markMethodsAsUnsubscribed()
    {
        this.method = null;
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
            throw new IllegalArgumentException("Cannot give non-null context for a null callback.");
        }

        this.config.setMessageCallback(callback, context);
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
        // deviceIO may be set to null in the case when a device client was multiplexing and was unregistered
        this.deviceIO = deviceIO;

        // Since connection status callbacks can be registered before associating a device client with a multiplexing client, the callback and its
        // context also need to be registered when the device IO is set.
        if (this.deviceIO != null && this.connectionStatusChangeCallback != null)
        {
            this.deviceIO.registerConnectionStatusChangeCallback(
                    this.connectionStatusChangeCallback,
                    this.connectionStatusChangeCallbackContext,
                    this.getConfig().getDeviceId());
        }
    }

    void setAsMultiplexed()
    {
        this.isMultiplexed = true;
    }

    private void setOption_SetHttpsConnectTimeout(Object value)
    {
        if (this.config.getProtocol() != HTTPS)
        {
            throw new UnsupportedOperationException("Cannot set the https connect timeout when using protocol " + this.config.getProtocol());
        }

        if (value instanceof Integer)
        {
            log.info("Setting HTTPS connect timeout to {} milliseconds", value);
            this.config.setHttpsConnectTimeout((int) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not int = " + value);
        }
    }

    private void setOption_SetHttpsReadTimeout(Object value)
    {
        if (this.config.getProtocol() != HTTPS)
        {
            throw new UnsupportedOperationException("Cannot set the https read timeout when using protocol " + this.config.getProtocol());
        }

        if (value instanceof Integer)
        {
            log.info("Setting HTTPS read timeout to {} milliseconds", value);
            this.config.setHttpsReadTimeout((int) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not int = " + value);
        }
    }

    private void setOption_SetSendInterval(Object value)
    {
        if (value instanceof Long)
        {
            verifyRegisteredIfMultiplexing();
            log.info("Setting send period to {} milliseconds", value);
            this.deviceIO.setSendPeriodInMilliseconds((long) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not long = " + value);
        }
    }

    private void setOption_SetMinimumPollingInterval(Object value)
    {
        if (value instanceof Long)
        {
            verifyRegisteredIfMultiplexing();
            log.info("Setting receive period to {} milliseconds", value);
            this.deviceIO.setReceivePeriodInMilliseconds((long) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not long = " + value);
        }
    }

    // The warning is for how getSasTokenAuthentication() may return null, but the check that our config uses SAS_TOKEN
    // auth is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    private void setOption_SetSASTokenExpiryTime(Object value)
    {
        if (this.config.getAuthenticationType() != DeviceClientConfig.AuthType.SAS_TOKEN || this.config.getSasTokenAuthentication() == null)
        {
            throw new IllegalStateException("Cannot set sas token validity time when not using sas token authentication");
        }

        long validTimeInSeconds;

        if (value instanceof Long)
        {
            validTimeInSeconds = (long) value;
        }
        else
        {
            throw new IllegalArgumentException("value is not long = " + value);
        }

        log.info("Setting generated SAS token lifespans to {} seconds", validTimeInSeconds);
        this.config.getSasTokenAuthentication().setTokenValidSecs(validTimeInSeconds);
    }

    private void setOption_SetAmqpOpenAuthenticationSessionTimeout(Object value)
    {
        if (this.config.getProtocol() != AMQPS && this.config.getProtocol() != AMQPS_WS)
        {
            throw new UnsupportedOperationException("Cannot set the open authentication session timeout when using protocol " + this.config.getProtocol());
        }

        if (this.config.getAuthenticationType() != DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            throw new UnsupportedOperationException("Cannot set the open authentication session timeout when using authentication type " + this.config.getAuthenticationType());
        }

        if (value instanceof Integer)
        {
            log.info("Setting generated AMQP authentication session timeout to {} seconds", value);
            this.config.setAmqpOpenAuthenticationSessionTimeout((int) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not int = " + value);
        }
    }

    private void setOption_SetAmqpOpenDeviceSessionsTimeout(Object value)
    {
        if (this.config.getProtocol() != AMQPS && this.config.getProtocol() != AMQPS_WS)
        {
            throw new UnsupportedOperationException("Cannot set the open device session timeout when using protocol " + this.config.getProtocol());
        }

        if (value instanceof Integer)
        {
            log.info("Setting generated AMQP device session timeout to {} seconds", value);
            this.config.setAmqpOpenDeviceSessionsTimeout((int) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not int = " + value);
        }
    }

    private void setOption_SetMaxMessagesSentPerThread(Object value)
    {
        if (value instanceof Integer)
        {
            log.info("Setting maximum number of messages sent per send thread {} messages", value);
            this.deviceIO.setMaxNumberOfMessagesSentPerSendThread((int) value);
        }
        else
        {
            throw new IllegalArgumentException("value is not int = " + value);
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

    private void verifyRegisteredIfMultiplexing()
    {
        // deviceIO is only ever null when a client was registered to a multiplexing client, became unregistered, and hasn't be re-registered yet.
        if (this.deviceIO == null && this.isMultiplexed)
        {
            throw new UnsupportedOperationException("Must re-register this client to a multiplexing client before using it");
        }
    }

    private void verifyReportedProperties(Set<Property> reportedProperties) throws IOException
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
    }

    private void verifyTwinOperationsAreSupported()
    {
        if (this.config.getProtocol() == HTTPS)
        {
            throw new UnsupportedOperationException(TWIN_OVER_HTTP_ERROR_MESSAGE);
        }
    }

    private void verifyMethodsAreSupported()
    {
        if (this.config.getProtocol() == HTTPS)
        {
            throw new UnsupportedOperationException(METHODS_OVER_HTTP_ERROR_MESSAGE);
        }
    }
}
