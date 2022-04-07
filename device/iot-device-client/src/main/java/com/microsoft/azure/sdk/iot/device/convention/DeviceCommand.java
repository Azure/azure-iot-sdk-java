// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.twin.DirectMethod;
import com.microsoft.azure.sdk.iot.device.twin.SubscriptionAcknowledgedCallback;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * The command callback execution class.
 */
@Slf4j
public class DeviceCommand extends DirectMethod
{
    private DeviceCommandCallback deviceCommandCallback;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    PayloadConvention payloadConvention;

    private final class deviceCommandResponseCallback implements MessageCallback
    {
        final ClientConfiguration nestedConfig = config;

        @Setter(AccessLevel.PRIVATE)
        @Getter(AccessLevel.PRIVATE)
        private PayloadConvention payloadConvention;

        public deviceCommandResponseCallback(PayloadConvention payloadConvention)
        {
            setPayloadConvention(payloadConvention);
        }

        @Override
        public IotHubMessageResult onCloudToDeviceMessageReceived(Message message, Object callbackContext)
        {
            synchronized (DEVICE_METHOD_LOCK)
            {
                IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
                IotHubMessageResult result = IotHubMessageResult.ABANDON;

                if (message.getMessageType() != MessageType.DEVICE_METHODS)
                {
                    log.error("Unexpected message type received {}", message.getMessageType());
                    deviceMethodStatusCallback.onMessageSent(message, IotHubStatusCode.toException(iotHubStatus), deviceMethodStatusCallbackContext);
                    return IotHubMessageResult.ABANDON;
                }

                IotHubTransportMessage methodMessage = (IotHubTransportMessage) message;

                if (methodMessage.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST)
                {
                    if (deviceCommandCallback != null)
                    {
                        if (!isSubscribed)
                        {
                            isSubscribed = true;
                        }
                        try
                        {
                            String componentName = null;
                            String commandName = methodMessage.getMethodName();

                            // The current format for command names with components is componentName*commandName
                            // This set of operations will attempt to split on the * character. A note for the future.
                            // There is a consideration for multiple nested components so we will need to alter this section to support that.
                            if (commandName.contains("*"))
                            {
                                String[] componentAndMethod = methodMessage.getMethodName().split("\\*");
                                componentName = componentAndMethod[0];
                                commandName = componentAndMethod[1];

                            }

                            log.trace("Executing command invocation callback for component {} with command name {} for message {}",  componentName == null ? "default" : componentName , commandName, methodMessage);
                            DeviceCommandRequest commandRequest = new DeviceCommandRequest(componentName, commandName, methodMessage.getBytes(), payloadConvention);
                            DeviceCommandResponse responseData = deviceCommandCallback.onDeviceCommandReceived(commandRequest);
                            log.trace("Command invocation callback returned for component {} with command name {} for message {}", componentName == null ? "default" : componentName , commandName, methodMessage);

                            if (responseData != null)
                            {
                                IotHubTransportMessage responseMessage = new IotHubTransportMessage(getPayloadConvention().getObjectBytes(responseData.getResponseMessage()), MessageType.DEVICE_METHODS);

                                responseMessage.setRequestId(methodMessage.getRequestId());

                                responseMessage.setConnectionDeviceId(this.nestedConfig.getDeviceId());

                                responseMessage.setStatus(String.valueOf(responseData.getStatus()));
                                responseMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_SEND_RESPONSE);

                                client.sendEventAsync(responseMessage, new DirectMethodRequestMessageCallback(), null);
                                result = IotHubMessageResult.COMPLETE;
                            }
                            else
                            {
                                log.info("User callback did not send any data for command response");
                                result = IotHubMessageResult.REJECT;
                                deviceMethodStatusCallback.onMessageSent(message, IotHubStatusCode.toException(iotHubStatus), deviceMethodStatusCallbackContext);
                            }
                        }
                        catch (Exception e)
                        {
                            log.info("User callback did not succeed");
                            result = IotHubMessageResult.REJECT;
                            deviceMethodStatusCallback.onMessageSent(message, IotHubStatusCode.toException(iotHubStatus), deviceMethodStatusCallbackContext);
                        }
                    }
                    else
                    {
                        log.warn("Received device command request, but device has not setup device command");
                    }
                }
                else
                {
                    log.warn("Received unknown type message for device commands");
                }

                return result;
            }
        }
    }

    private final class DirectMethodRequestMessageCallback implements MessageSentCallback
    {
        @Override
        public void onMessageSent(Message sentMessage, IotHubClientException exception, Object callbackContext)
        {
            deviceMethodStatusCallback.onMessageSent(sentMessage, exception, deviceMethodStatusCallbackContext);
        }
    }

    /**
     * This constructor creates an instance of device command class which helps facilitate the interation for device commands
     * between the user and IotHub.
     *
     * @param client  Device client  object for this connection instance for the device. Cannot be {@code null}
     * @param deviceMethodStatusCallback Callback to provide status for device method state with IotHub. Cannot be {@code null}.
     * @param deviceMethodStatusCallbackContext Context to be passed when device method status is invoked. Can be {@code null}
     * @param deviceMethodStatusCallbackContext Context to be passed when device command status is invoked. Can be {@code null}
     * @param payloadConvention                 The payload convention to be used for the command
     * @throws IllegalArgumentException This exception is thrown if either deviceIO or config or deviceCommandStatusCallback are null
     */
    public DeviceCommand(InternalClient client, SubscriptionAcknowledgedCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext, PayloadConvention payloadConvention) throws IllegalArgumentException
    {
        super(client, deviceMethodStatusCallback, deviceMethodStatusCallbackContext);
        setPayloadConvention(payloadConvention);
        client.getConfig().setDirectMethodsMessageCallback(new deviceCommandResponseCallback(payloadConvention), null);
    }

    /**
     * A method which subscribes to receive device method invocation for the user with the IotHub.
     * @param deviceMethodCallback Callback where upon receiving the request the
     *                             invoke a method shall be triggered.
     * @param deviceMethodCallbackContext Context to be passed on when invoking the
     *                                    callback.
     * @throws IllegalArgumentException This exception is thrown when deviceMethodCallback is provided null.
     */
    public void subscribeToDeviceCommand(DeviceCommandCallback deviceMethodCallback, Object deviceMethodCallbackContext) throws IllegalArgumentException
    {
        if (deviceMethodCallback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        this.deviceCommandCallback = deviceMethodCallback;
        this.deviceMethodStatusCallbackContext = deviceMethodCallbackContext;

        if (!isSubscribed)
        {
            IotHubTransportMessage subscribeMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);
            subscribeMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
            subscribeMessage.setConnectionDeviceId(this.config.getDeviceId());
            this.client.sendEventAsync(subscribeMessage, new DirectMethodRequestMessageCallback(), null);
        }
    }
}
