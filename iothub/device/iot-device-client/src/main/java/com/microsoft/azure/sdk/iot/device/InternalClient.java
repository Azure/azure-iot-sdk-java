/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.auth.IotHubAuthenticationProvider;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.RetryPolicy;
import com.microsoft.azure.sdk.iot.device.twin.*;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.azure.sdk.iot.device.IotHubClientProtocol.HTTPS;

@Slf4j
public class InternalClient
{
    private static final String TWIN_OVER_HTTP_ERROR_MESSAGE =
        "Twin operations are only supported over MQTT, MQTT_WS, AMQPS, and AMQPS_WS";

    private static final String METHODS_OVER_HTTP_ERROR_MESSAGE =
        "Direct methods are only supported over MQTT, MQTT_WS, AMQPS, and AMQPS_WS";

    protected static final int DEFAULT_TIMEOUT_MILLISECONDS = 60 * 1000;

    ClientConfiguration config;
    private DeviceIO deviceIO;

    boolean isMultiplexed = false;

    private IotHubConnectionStatusChangeCallback connectionStatusChangeCallback;
    private Object connectionStatusChangeCallbackContext;

    private DeviceTwin twin;
    private DirectMethod method;

    InternalClient(IotHubConnectionString iotHubConnectionString, IotHubClientProtocol protocol, ClientOptions clientOptions)
    {
        commonConstructorVerification(iotHubConnectionString, protocol);
        this.config = new ClientConfiguration(iotHubConnectionString, protocol, clientOptions);
        this.deviceIO = new DeviceIO(this.config);
        setClientOptionValues(clientOptions);
    }

    InternalClient(IotHubAuthenticationProvider iotHubAuthenticationProvider, IotHubClientProtocol protocol)
    {
        this.config = new ClientConfiguration(iotHubAuthenticationProvider, protocol);
        this.deviceIO = new DeviceIO(this.config);
    }

    InternalClient(String uri, String deviceId, SecurityProvider securityProvider, IotHubClientProtocol protocol, ClientOptions clientOptions) throws IOException
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

        this.config = new ClientConfiguration(connectionString, securityProvider, protocol, clientOptions);
        this.deviceIO = new DeviceIO(this.config);
        setClientOptionValues(clientOptions);
    }

    InternalClient(String hostName, String deviceId, String moduleId, SasTokenProvider sasTokenProvider, IotHubClientProtocol protocol, ClientOptions clientOptions)
    {
        if (hostName == null)
        {
            throw new IllegalArgumentException("Host name cannot be null");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("Protocol cannot be null.");
        }

        this.config = new ClientConfiguration(hostName, sasTokenProvider, protocol, clientOptions, deviceId, moduleId);
        this.deviceIO = new DeviceIO(this.config);
        setClientOptionValues(clientOptions);
    }

    private void setClientOptionValues(ClientOptions clientOptions)
    {
        if (clientOptions != null)
        {
            if (clientOptions.getMessagesSentPerSendInterval() <= 0)
            {
                throw new IllegalArgumentException("ClientOption messagesSentPerSendInterval must be greater than 0");
            }

            if (clientOptions.getSendInterval() <= 0)
            {
                throw new IllegalArgumentException("ClientOption sendInterval must be greater than 0");
            }

            if (clientOptions.getReceiveInterval() <= 0)
            {
                throw new IllegalArgumentException("ClientOption receiveInterval must be greater than 0");
            }

            if (clientOptions.getProxySettings() != null)
            {
                if (this.isMultiplexed)
                {
                    throw new UnsupportedOperationException(
                        "Cannot set the proxy settings of a multiplexed device. " +
                            "Proxy settings for the multiplexed connection can only be set at multiplexing client constructor time.");
                }

                verifyRegisteredIfMultiplexing();
            }

            this.deviceIO.setMaxNumberOfMessagesSentPerSendThread(clientOptions.getMessagesSentPerSendInterval());
            this.deviceIO.setSendPeriodInMilliseconds(clientOptions.getSendInterval());
            this.deviceIO.setReceivePeriodInMilliseconds(clientOptions.getReceiveInterval());
        }
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
     * @param withRetry if true, this open call will apply the retry policy to allow for the open call to be retried if
     * it fails. Both the operation timeout set in {@link #setOperationTimeout(long)} and the retry policy set in
     * {{@link #setRetryPolicy(RetryPolicy)}} will be respected while retrying to open the connection.
     *
     * @throws IotHubClientException if a connection to an IoT hub cannot be established or if the connection can be
     * established but the service rejects it for any reason.
     */
    public void open(boolean withRetry) throws IotHubClientException
    {
        this.deviceIO.open(withRetry);
    }

    /**
     * Close the client.
     */
    public void close()
    {
        this.deviceIO.close();
        this.method = null;
        this.twin = null;
    }

    /**
     * Synchronously sends a message to IoT hub.
     *
     * @param message the message to be sent.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the telemetry to be acknowledged by the service.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void sendEvent(Message message) throws InterruptedException, IllegalStateException, IotHubClientException
    {
        sendEvent(message, DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Synchronously sends a message to IoT hub.
     *
     * @param message the message to be sent.
     * @param timeoutMilliseconds The maximum number of milliseconds to wait for the service to acknowledge this message.
     * If 0, then it will wait indefinitely.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the telemetry to be acknowledged by the service.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void sendEvent(Message message, int timeoutMilliseconds) throws InterruptedException, IllegalStateException, IotHubClientException
    {
        verifyRegisteredIfMultiplexing();
        message.setConnectionDeviceId(this.config.getDeviceId());

        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IotHubClientException> iotHubClientExceptionReference = new AtomicReference<>();
        MessageSentCallback eventCallback = (sentMessage, exception, callbackContext) ->
        {
            iotHubClientExceptionReference.set(exception);
            latch.countDown();
        };

        this.sendEventAsync(message, eventCallback, null);

        if (timeoutMilliseconds == 0)
        {
            latch.await();
        }
        else
        {
            boolean timedOut = !latch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubClientException(IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for service to acknowledge telemetry");
            }
        }

        IotHubClientException exception = iotHubClientExceptionReference.get();
        if (exception != null)
        {
            // This exception was thrown from an internal thread that the user does not directly call, so its stacktrace
            // is not very traceable for a user. Rather than throw the exception as is, create a new one so the stacktrace
            // the user receives points them to this synchronous method and has a nested exception with the internal thread's
            // stacktrace that can be used for our debugging purposes.
            throw new IotHubClientException(exception.getStatusCode(), exception.getMessage(), exception);
        }
    }

    /**
     * Synchronously sends a batch of messages to IoT hub
     *
     * This operation is only supported over HTTPS.
     *
     * Maximum payload size for HTTPS is 255KB
     *
     * @param messages the messages to be sent.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the telemetry to be acknowledged by the service.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     * @throws UnsupportedOperationException if the client is not using HTTPS.
     */
    public void sendEvents(List<Message> messages)
            throws InterruptedException, IllegalStateException, IotHubClientException, UnsupportedOperationException
    {
        this.sendEvents(messages, DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Synchronously sends a batch of messages to IoT hub
     *
     * This operation is only supported over HTTPS.
     *
     * Maximum payload size for HTTPS is 255KB
     *
     * @param messages the messages to be sent.
     * @param timeoutMilliseconds The maximum number of milliseconds to wait for the service to acknowledge this batch message.
     * If 0, then it will wait indefinitely.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the telemetry to be acknowledged by the service.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     * @throws UnsupportedOperationException if the client is not using HTTPS.
     */
    public void sendEvents(List<Message> messages, int timeoutMilliseconds)
            throws InterruptedException, IllegalStateException, IotHubClientException, UnsupportedOperationException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IotHubClientException> iotHubClientExceptionReference = new AtomicReference<>();
        MessagesSentCallback eventCallback = (sentMessages, exception, callbackContext) ->
        {
            iotHubClientExceptionReference.set(exception);
            latch.countDown();
        };

        this.sendEventsAsync(messages, eventCallback, null);

        if (timeoutMilliseconds == 0)
        {
            latch.await();
        }
        else
        {
            boolean timedOut = !latch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubClientException(IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for service to acknowledge telemetry");
            }
        }

        IotHubClientException exception = iotHubClientExceptionReference.get();
        if (exception != null)
        {
            // This exception was thrown from an internal thread that the user does not directly call, so its stacktrace
            // is not very traceable for a user. Rather than throw the exception as is, create a new one so the stacktrace 
            // the user receives points them to this synchronous method and has a nested exception with the internal thread's
            // stacktrace that can be used for our debugging purposes.
            throw new IotHubClientException(exception.getStatusCode(), exception.getMessage(), exception);
        }
    }

    /**
     * Start receiving desired property updates for this client. After subscribing to desired properties, this client can
     * freely send reported property updates and make getTwin calls.
     * <p>
     * This call can only be made after the client has been successfully opened.
     * </p>
     * <p>
     * This subscription is preserved between reconnect attempts. However, it is not preserved after a client has
     * been closed because the user called {@link #close()} or because this client lost its connection and its retry
     * policy was exhausted.
     * </p>
     * @param desiredPropertiesCallback The callback to execute each time a desired property update message is received
     * from the service. This will contain one or many properties updated at once.
     * @param desiredPropertiesCallbackContext The context that will be included in the callback of desiredPropertiesCallback. May be null.
     * @throws InterruptedException if the operation is interrupted while waiting on the subscription request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void subscribeToDesiredProperties(DesiredPropertiesCallback desiredPropertiesCallback, Object desiredPropertiesCallbackContext)
        throws InterruptedException, IllegalStateException, IotHubClientException
    {
        subscribeToDesiredProperties(desiredPropertiesCallback, desiredPropertiesCallbackContext, DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Start receiving desired property updates for this client. After subscribing to desired properties, this client can
     * freely send reported property updates and make getTwin calls.
     * <p>
     * This call can only be made after the client has been successfully opened.
     * </p>
     * <p>
     * This subscription is preserved between reconnect attempts. However, it is not preserved after a client has
     * been closed because the user called {@link #close()} or because this client lost its connection and its retry
     * policy was exhausted.
     * </p>
     * @param desiredPropertiesCallback The callback to execute each time a desired property update message is received
     * from the service. This will contain one or many properties updated at once.
     * @param desiredPropertiesCallbackContext The context that will be included in the callback of desiredPropertiesCallback. May be null.
     * @param timeoutMilliseconds The maximum number of milliseconds this call will wait for the service to acknowledge the subscription request. If 0,
     * then it will wait indefinitely.
     * @throws InterruptedException if the operation is interrupted while waiting on the subscription request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void subscribeToDesiredProperties(DesiredPropertiesCallback desiredPropertiesCallback, Object desiredPropertiesCallbackContext, int timeoutMilliseconds)
        throws InterruptedException, IllegalStateException, IotHubClientException
    {
        AtomicReference<IotHubClientException> iotHubClientExceptionReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        this.subscribeToDesiredPropertiesAsync(
            desiredPropertiesCallback,
            desiredPropertiesCallbackContext,
            (exception, context) ->
            {
                iotHubClientExceptionReference.set(exception);
                latch.countDown();
            },
            null);

        if (timeoutMilliseconds == 0)
        {
            latch.await();
        }
        else
        {
            boolean timedOut = !latch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubClientException(IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for service to acknowledge desired properties subscription request");
            }
        }

        IotHubClientException exception = iotHubClientExceptionReference.get();
        if (exception != null)
        {
            // This exception was thrown from an internal thread that the user does not directly call, so its stacktrace
            // is not very traceable for a user. Rather than throw the exception as is, create a new one so the stacktrace 
            // the user receives points them to this synchronous method and has a nested exception with the internal thread's
            // stacktrace that can be used for our debugging purposes.
            throw new IotHubClientException(exception.getStatusCode(), exception.getMessage(), exception);
        }
    }

    /**
     * Patch this client's twin with the provided reported properties. This client must have subscribed to desired
     * properties before this method can be called.
     *
     * @param reportedProperties The reported property key/value pairs to add/update in the twin. To delete a particular
     * reported property, set the value to null.
     * @return The new reported properties version.
     * @throws InterruptedException if the operation is interrupted while waiting on the reported property update request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public ReportedPropertiesUpdateResponse updateReportedProperties(TwinCollection reportedProperties)
        throws InterruptedException, IllegalStateException, IotHubClientException
    {
        return updateReportedProperties(reportedProperties, DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Patch this client's twin with the provided reported properties. This client must have subscribed to desired
     * properties before this method can be called. This client must have subscribed to desired
     * properties before this method can be called.
     *
     * @param reportedProperties The reported property key/value pairs to add/update in the twin. To delete a particular
     * reported property, set the value to null.
     * @param timeoutMilliseconds The maximum number of milliseconds this call will wait for the service to acknowledge the reported properties update request. If 0,
     * then it will wait indefinitely.
     * @return The new reported properties version.
     * @throws InterruptedException if the operation is interrupted while waiting on the reported property update request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public ReportedPropertiesUpdateResponse updateReportedProperties(TwinCollection reportedProperties, int timeoutMilliseconds)
        throws InterruptedException, IllegalStateException, IotHubClientException
    {
        AtomicReference<IotHubClientException> iotHubClientExceptionAtomicReference = new AtomicReference<>();
        AtomicReference<ReportedPropertiesUpdateResponse> responseAtomicReference = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        this.updateReportedPropertiesAsync(
            reportedProperties,
            (statusCode, response, e, callbackContext) ->
            {
                iotHubClientExceptionAtomicReference.set(e);
                responseAtomicReference.set(response);
                latch.countDown();
            },
            null);

        if (timeoutMilliseconds == 0)
        {
            latch.await();
        }
        else
        {
            boolean timedOut = !latch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubClientException(IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for service to acknowledge reported properties update");
            }
        }

        IotHubClientException exception = iotHubClientExceptionAtomicReference.get();
        if (exception != null)
        {
            // This exception was thrown from an internal thread that the user does not directly call, so its stacktrace
            // is not very traceable for a user. Rather than throw the exception as is, create a new one so the stacktrace 
            // the user receives points them to this synchronous method and has a nested exception with the internal thread's
            // stacktrace that can be used for our debugging purposes.
            throw new IotHubClientException(exception.getStatusCode(), exception.getMessage(), exception);
        }

        return responseAtomicReference.get();
    }

    /**
     * Get the twin for this client. This client must have subscribed to desired properties before this method can be called.
     *
     * @return The twin for this client
     * @throws InterruptedException if the operation is interrupted while waiting on the getTwin request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public Twin getTwin() throws InterruptedException, IllegalStateException, IotHubClientException
    {
        return getTwin(DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Get the twin for this client. This client must have subscribed to desired properties before this method can be called.
     *
     * @param timeoutMilliseconds The maximum number of milliseconds this call will wait for the service to return the twin.
     * If 0, then it will wait indefinitely.
     * @return The twin for this client
     * @throws InterruptedException if the operation is interrupted while waiting on the getTwin request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public Twin getTwin(int timeoutMilliseconds) throws InterruptedException, IllegalStateException, IotHubClientException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<com.microsoft.azure.sdk.iot.device.twin.Twin> twinAtomicReference = new AtomicReference<>();
        AtomicReference<IotHubClientException> iotHubClientExceptionReference = new AtomicReference<>();
        getTwinAsync(
            (twin, exception, callbackContext) ->
            {
                twinAtomicReference.set(twin);
                iotHubClientExceptionReference.set(exception);
                latch.countDown();
            },
            null);

        if (timeoutMilliseconds == 0)
        {
            latch.await();
        }
        else
        {
            boolean timedOut = !latch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubClientException(IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for service to respond to getTwin request");
            }
        }

        IotHubClientException exception = iotHubClientExceptionReference.get();
        if (exception != null)
        {
            // This exception was thrown from an internal thread that the user does not directly call, so its stacktrace
            // is not very traceable for a user. Rather than throw the exception as is, create a new one so the stacktrace 
            // the user receives points them to this synchronous method and has a nested exception with the internal thread's
            // stacktrace that can be used for our debugging purposes.
            throw new IotHubClientException(exception.getStatusCode(), exception.getMessage(), exception);
        }

        return twinAtomicReference.get();
    }

    /**
     * Subscribes to direct methods.
     * <p>
     * This call can only be made after the client has been successfully opened.
     * </p>
     * <p>
     * This subscription is preserved between reconnect attempts. However, it is not preserved after a client has
     * been closed because the user called {@link #close()} or because this client lost its connection and its retry
     * policy was exhausted.
     * </p>
     * @param methodCallback Callback on which direct methods shall be invoked. Cannot be {@code null}.
     * @param methodCallbackContext Context for device method callback. Can be {@code null}.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the subscription request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void subscribeToMethods(MethodCallback methodCallback, Object methodCallbackContext)
        throws IllegalStateException, InterruptedException, IotHubClientException
    {
        this.subscribeToMethods(methodCallback, methodCallbackContext, DEFAULT_TIMEOUT_MILLISECONDS);
    }

    /**
     * Subscribes to direct methods.
     * <p>
     * This call can only be made after the client has been successfully opened.
     * </p>
     * <p>
     * This subscription is preserved between reconnect attempts. However, it is not preserved after a client has
     * been closed because the user called {@link #close()} or because this client lost its connection and its retry
     * policy was exhausted.
     * </p>
     * @param methodCallback Callback on which direct methods shall be invoked. Cannot be {@code null}.
     * @param methodCallbackContext Context for device method callback. Can be {@code null}.
     * @param timeoutMilliseconds The maximum number of milliseconds this call will wait for the service to return the twin.
     * If 0, then it will wait indefinitely.
     *
     * @throws InterruptedException if the operation is interrupted while waiting on the subscription request to be acknowledged by the service.
     * @throws IllegalStateException if this client is not open.
     * @throws IotHubClientException if the request is rejected by the service for any reason of if the synchronous operation times out.
     */
    public void subscribeToMethods(MethodCallback methodCallback, Object methodCallbackContext, int timeoutMilliseconds)
        throws IllegalStateException, InterruptedException, IotHubClientException
    {
        final CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<IotHubClientException> iotHubClientExceptionReference = new AtomicReference<>();
        subscribeToMethodsAsync(
            methodCallback,
            methodCallbackContext,
            (exception, callbackContext) ->
            {
                iotHubClientExceptionReference.set(exception);
                latch.countDown();
            },
            null);

        if (timeoutMilliseconds == 0)
        {
            latch.await();
        }
        else
        {
            boolean timedOut = !latch.await(timeoutMilliseconds, TimeUnit.MILLISECONDS);

            if (timedOut)
            {
                throw new IotHubClientException(IotHubStatusCode.DEVICE_OPERATION_TIMED_OUT, "Timed out waiting for service to respond to direct method subscription request");
            }
        }

        IotHubClientException exception = iotHubClientExceptionReference.get();
        if (exception != null)
        {
            // This exception was thrown from an internal thread that the user does not directly call, so its stacktrace
            // is not very traceable for a user. Rather than throw the exception as is, create a new one so the stacktrace 
            // the user receives points them to this synchronous method and has a nested exception with the internal thread's
            // stacktrace that can be used for our debugging purposes.
            throw new IotHubClientException(exception.getStatusCode(), exception.getMessage(), exception);
        }
    }

    /**
     * Asynchronously sends a message to IoT hub.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     */
    public void sendEventAsync(Message message, MessageSentCallback callback, Object callbackContext)
        throws IllegalStateException
    {
        verifyRegisteredIfMultiplexing();
        message.setConnectionDeviceId(this.config.getDeviceId());
        deviceIO.sendEventAsync(message, callback, callbackContext, this.config.getDeviceId());
    }

    /**
     * Asynchronously sends a batch of messages to the IoT hub
     *
     * This operation is only supported over HTTPS. This API call is an all-or-nothing single HTTPS message and the
     * callback will be triggered once this batch message has been sent.
     *
     * Maximum payload size for HTTPS is 255KB
     *
     * @param messages the list of message to be sent.
     * @param callback the callback to be invoked when a response is received.
     * Can be {@code null}.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     *
     * @throws IllegalArgumentException if the message provided is {@code null}.
     * @throws IllegalStateException if the client has not been opened yet or is already closed.
     * @throws UnsupportedOperationException if the client is not using HTTPS.
     */
    public void sendEventsAsync(List<Message> messages, MessagesSentCallback callback, Object callbackContext)
        throws IllegalStateException, UnsupportedOperationException
    {
        verifyRegisteredIfMultiplexing();

        for (Message message: messages)
        {
            message.setConnectionDeviceId(this.config.getDeviceId());
        }

        // wrap the message sent callback such that when the batch message sends, we notify the user that their list of messages have been sent
        MessageSentCallback messageSentCallback =
                (sentMessage, clientException, callbackContext1) -> callback.onMessagesSent(messages, clientException, callbackContext1);

        if (this.config.getProtocol() != HTTPS)
        {
            throw new UnsupportedOperationException("Batch messaging is only supported over HTTPS");
        }

        Message message = new BatchMessage(messages);

        deviceIO.sendEventAsync(message, messageSentCallback, callbackContext, this.config.getDeviceId());
    }

    /**
     * Start receiving desired property updates for this client asynchronously. After subscribing to desired properties, this client can
     * freely send reported property updates and make getTwin calls.
     * <p>
     * This call can only be made after the client has been successfully opened.
     * </p>
     * <p>
     * This subscription is preserved between reconnect attempts. However, it is not preserved after a client has
     * been closed because the user called {@link #close()} or because this client lost its connection and its retry
     * policy was exhausted.
     * </p>
     * @param subscriptionAcknowledgedCallback The callback to execute once the service has acknowledged the subscription request.
     * @param desiredPropertiesSubscriptionCallbackContext The context that will be included in the callback of desiredPropertiesSubscriptionCallback. May be null.
     * @param desiredPropertiesCallback The callback to execute each time a desired property update message is received
     * from the service. This will contain one or many properties updated at once.
     * @param desiredPropertiesCallbackContext The context that will be included in each callback of desiredPropertiesCallback. May be null.
     * @throws IllegalStateException if this client is not open.
     */
    public void subscribeToDesiredPropertiesAsync(
        DesiredPropertiesCallback desiredPropertiesCallback,
        Object desiredPropertiesCallbackContext,
        SubscriptionAcknowledgedCallback subscriptionAcknowledgedCallback,
        Object desiredPropertiesSubscriptionCallbackContext)
            throws IllegalStateException
    {
        verifyRegisteredIfMultiplexing();
        verifyTwinOperationsAreSupported();

        if (!this.deviceIO.isOpen())
        {
            throw new IllegalStateException("Open the client connection before using it");
        }

        this.twin = new DeviceTwin(this);
        this.twin.subscribeToDesiredPropertiesAsync(
            subscriptionAcknowledgedCallback,
            desiredPropertiesSubscriptionCallbackContext,
            desiredPropertiesCallback,
            desiredPropertiesCallbackContext);
    }

    /**
     * Patch this client's twin with the provided reported properties asynchronously. This client must have subscribed
     * to desired properties before this method can be called.
     *
     * @param reportedProperties The reported property key/value pairs to add/update in the twin. To delete a particular
     * reported property, set the value to null.
     * @param reportedPropertiesCallback The callback to be executed once the reported properties update request
     * has been acknowledged by the service.
     * @param callbackContext The context that will be included in the callback of reportedPropertiesCallback. May be null.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     */
    public void updateReportedPropertiesAsync(
        TwinCollection reportedProperties,
        ReportedPropertiesCallback reportedPropertiesCallback,
        Object callbackContext)
            throws IllegalStateException
    {
        this.updateReportedPropertiesAsync(
            reportedProperties,
            new ReportedPropertiesUpdateCorrelatingMessageCallback()
            {
                @Override
                public void onRequestQueued(Message message, Object callbackContext)
                {
                    // do nothing, user opted not to care about this event by using this API
                }

                @Override
                public void onRequestSent(Message message, Object callbackContext)
                {
                    // do nothing, user opted not to care about this event by using this API
                }

                @Override
                public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
                {
                    // do nothing, user opted not to care about this event by using this API
                }

                @Override
                public void onResponseReceived(Message message, Object callbackContext, IotHubStatusCode statusCode, ReportedPropertiesUpdateResponse response, IotHubClientException e)
                {
                    reportedPropertiesCallback.onReportedPropertiesUpdateAcknowledged(statusCode, response, e, callbackContext);
                }

                @Override
                public void onResponseAcknowledged(Message message, Object callbackContext)
                {
                    // do nothing, user opted not to care about this event by using this API
                }
            },
            callbackContext);
    }

    /**
     * Patch this client's twin with the provided reported properties asynchronously. This client must have subscribed
     * to desired properties before this method can be called.
     *
     * <p>
     * This overload utilizes a more verbose callback than {@link #updateReportedPropertiesAsync(TwinCollection, ReportedPropertiesCallback, Object)}
     * and is only intended for users who need insight on the state of this process every step of the way.
     * </p>
     *
     * @param reportedProperties The reported property key/value pairs to add/update in the twin. To delete a particular
     * reported property, set the value to null.
     * @param reportedPropertiesUpdateCorrelatingMessageCallback The callback to be executed once the state of the reported
     * properties update request message has changed. This provides context on when the message is queued, sent, acknowledged, etc.
     * @param callbackContext The context that will be included in each callback of updateReportedPropertiesCallback. May be null.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     */
    public void updateReportedPropertiesAsync(
        TwinCollection reportedProperties,
        ReportedPropertiesUpdateCorrelatingMessageCallback reportedPropertiesUpdateCorrelatingMessageCallback,
        Object callbackContext)
            throws IllegalStateException
    {
        if (this.twin == null)
        {
            this.twin = new DeviceTwin(this);
        }

        this.twin.updateReportedPropertiesAsync(reportedProperties, reportedPropertiesUpdateCorrelatingMessageCallback, callbackContext);
    }

    /**
     * Get the twin for this client asynchronously. This client must have subscribed to desired properties before this
     * method can be called.
     * 
     * @param twinCallback The callback to be executed once the twin is received from the service.
     * @param callbackContext The context that will be included in the callback of twinCallback. May be null.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     */
    public void getTwinAsync(GetTwinCallback twinCallback, Object callbackContext)
        throws IllegalStateException
    {
        this.getTwinAsync(new GetTwinCorrelatingMessageCallback()
        {

            @Override
            public void onRequestQueued(Message message, Object callbackContext)
            {
                // do nothing, user opted not to care about this event by using this API
            }

            @Override
            public void onRequestSent(Message message, Object callbackContext)
            {
                // do nothing, user opted not to care about this event by using this API
            }

            @Override
            public void onRequestAcknowledged(Message message, Object callbackContext, IotHubClientException e)
            {
                // do nothing, user opted not to care about this event by using this API
            }

            @Override
            public void onResponseReceived(Twin twin, Message message, Object callbackContext, IotHubStatusCode statusCode, IotHubClientException e)
            {
                log.trace("Executing twin callback for message {}", message);
                twinCallback.onTwinReceived(twin, e, callbackContext);
                log.trace("Twin callback returned for message {}", message);
            }

            @Override
            public void onResponseAcknowledged(Message message, Object callbackContext)
            {
                // do nothing, user opted not to care about this event by using this API
            }
        }, callbackContext);
    }

    /**
     * Get the twin for this client asynchronously. This client must have subscribed to desired properties before this
     * method can be called.
     *
     * <p>
     * This overload utilizes a more verbose callback than {@link #getTwinAsync(GetTwinCallback, Object)}
     * and is only intended for users who need insight on the state of this process every step of the way.
     * </p>
     *
     * @param twinCallback The callback to be executed once the state of the getTwin request message has changed. This
     * provides context on when the message is queued, sent, acknowledged, etc.
     * @param callbackContext The context that will be included in each callback of twinCallback. May be null.
     * @throws IllegalStateException if this client is not open or if this client has not subscribed to desired properties yet.
     */
    public void getTwinAsync(GetTwinCorrelatingMessageCallback twinCallback, Object callbackContext)
        throws IllegalStateException
    {
        if (!this.deviceIO.isOpen())
        {
            throw new IllegalStateException("Open the client connection before using it");
        }

        if (this.twin == null)
        {
            this.twin = new DeviceTwin(this);
        }

        this.twin.getTwinAsync(twinCallback, callbackContext);
    }

    /**
     * Subscribes to direct methods.
     * <p>
     * This call can only be made after the client has been successfully opened.
     * </p>
     * <p>
     * This subscription is preserved between reconnect attempts. However, it is not preserved after a client has
     * been closed because the user called {@link #close()} or because this client lost its connection and its retry
     * policy was exhausted.
     * </p>
     * @param methodCallback Callback on which direct methods shall be invoked. Cannot be {@code null}.
     * @param methodCallbackContext Context for device method callback. Can be {@code null}.
     * @param methodStatusCallback Callback for providing IotHub status for direct methods. Cannot be {@code null}.
     * @param methodStatusCallbackContext Context for device method status callback. Can be {@code null}.
     *
     * @throws IllegalStateException if called when client is not opened.
     * @throws IllegalArgumentException if either callback are null.
     */
    public void subscribeToMethodsAsync(
        MethodCallback methodCallback,
        Object methodCallbackContext,
        SubscriptionAcknowledgedCallback methodStatusCallback,
        Object methodStatusCallbackContext)
            throws IllegalStateException
    {
        verifyRegisteredIfMultiplexing();
        verifyMethodsAreSupported();

        if (!this.deviceIO.isOpen())
        {
            throw new IllegalStateException("Open the client connection before using it");
        }

        if (methodCallback == null || methodStatusCallback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        this.method = new DirectMethod(this, methodStatusCallback, methodStatusCallbackContext);
        this.method.subscribeToDirectMethods(methodCallback, methodCallbackContext);
    }

    /**
     * Sets the callback to be executed when the connection status of the device changes. The callback will be fired
     * with a status and a reason why the device's status changed. When the callback is fired, the provided context will
     * be provided alongside the status and reason.
     *
     * This connection status callback is not triggered by any upstream connection change events. For example, if
     * if the connection status callback is set for a module on an IoT Edge device and that IoT Edge device
     * loses connection to the cloud, this connection status callback won't onStatusChanged since the connection
     * between the module and the IoT Edge device hasn't changed.
     *
     * <p>Note that the thread used to deliver this callback should not be used to call open()/closeNow() on the client
     * that this callback belongs to. All open()/close() operations should be done on a separate thread</p>
     *
     * @param callback The callback to be fired when the connection status of the device changes. Can be null to
     *                 unset this listener as long as the provided callbackContext is also null.
     * @param callbackContext a context to be passed to the callback. Can be {@code null}.
     */
    public void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext)
    {
        this.connectionStatusChangeCallback = callback;
        this.connectionStatusChangeCallbackContext = callbackContext;

        if (this.deviceIO != null)
        {
            this.deviceIO.setConnectionStatusChangeCallback(callback, callbackContext, this.getConfig().getDeviceId());
        }
    }

    /**
     * Sets the given retry policy on the underlying transport
     * <a href="https://github.com/Azure/azure-iot-sdk-java/blob/main/device/iot-device-client/devdoc/requirement_docs/com/microsoft/azure/iothub/retryPolicy.md">
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
    public ClientConfiguration getConfig()
    {
        return this.config;
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
            this.deviceIO.setConnectionStatusChangeCallback(
                    this.connectionStatusChangeCallback,
                    this.connectionStatusChangeCallbackContext,
                    this.getConfig().getDeviceId());
        }
    }

    void setAsMultiplexed()
    {
        this.isMultiplexed = true;
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
