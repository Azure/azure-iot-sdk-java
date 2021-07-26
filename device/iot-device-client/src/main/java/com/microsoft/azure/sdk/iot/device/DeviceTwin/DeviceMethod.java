// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DeviceMethod
{
    private DeviceMethodCallback deviceMethodCallback;
    protected Object deviceMethodCallbackContext;
    protected final IotHubEventCallback deviceMethodStatusCallback;
    protected final Object deviceMethodStatusCallbackContext;
    protected final ObjectLock DEVICE_METHOD_LOCK = new ObjectLock();

    protected boolean isSubscribed = false;

    protected final DeviceIO deviceIO;
    protected final DeviceClientConfig config;

    protected MessageCallback deviceMethodResponseCallback = new deviceMethodResponseCallback();
    protected IotHubEventCallback deviceMethodRequestMessageCallback = new deviceMethodRequestMessageCallback();

    private final class deviceMethodResponseCallback implements MessageCallback
    {
        final DeviceClientConfig nestedConfig = config;

        /*
        **Codes_SRS_DEVICEMETHOD_25_007: [**On receiving a message from IOTHub with for method invoke, the callback DeviceMethodResponseMessageCallback is triggered.**]**
         */
        @Override
        public IotHubMessageResult execute(Message message, Object callbackContext)
        {
            synchronized (DEVICE_METHOD_LOCK)
            {
                IotHubStatusCode iotHubStatus = IotHubStatusCode.ERROR;
                IotHubMessageResult result = IotHubMessageResult.ABANDON;

                if (message.getMessageType() != MessageType.DEVICE_METHODS)
                {
                    /*
                    **Codes_SRS_DEVICEMETHOD_25_009: [**If the received message is not of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Abandon **]**
                     */
                    log.error("Unexpected message type received {}", message.getMessageType());
                    deviceMethodStatusCallback.execute(iotHubStatus, deviceMethodStatusCallbackContext);
                    return IotHubMessageResult.ABANDON;
                }

                IotHubTransportMessage methodMessage = (IotHubTransportMessage) message;

                if (methodMessage.getDeviceOperationType() == DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST)
                {
                    if (deviceMethodCallback != null)
                    {
                        if (!isSubscribed)
                        {
                            isSubscribed = true;
                        }
                        try
                        {
                            /*
                             **Codes_SRS_DEVICEMETHOD_25_008: [**If the message is of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user registered device method callback gets invoked providing the user with method name and payload along with the user context. **]**
                             */
                            log.trace("Executing method invocation callback for method name {} for message {}", methodMessage.getMethodName(), methodMessage);
                            DeviceMethodData responseData = deviceMethodCallback.call(methodMessage.getMethodName(), methodMessage.getBytes(), deviceMethodCallbackContext);
                            log.trace("Method invocation callback returned for method name {} for message {}", methodMessage.getMethodName(), methodMessage);

                            /*
                             **Codes_SRS_DEVICEMETHOD_25_010: [**User is expected to provide response message and status upon invoking the device method callback.**]**
                             */
                            if (responseData != null)
                            {
                                /*
                                 **Codes_SRS_DEVICEMETHOD_25_011: [**If the user callback is successful and user has successfully provided the response message and status, then this method shall build a device method message of type DEVICE_OPERATION_METHOD_SEND_RESPONSE, serilize the user data by invoking MethodParser from serializer and save the user data as payload in the message before sending it to IotHub via sendeventAsync before marking the result as complete**]**
                                 **Codes_SRS_DEVICEMETHOD_25_015: [**User can provide null response message upon invoking the device method callback which will be serialized as is, before sending it to IotHub.**]**
                                 */
                                MethodParser methodParserObject = new MethodParser(responseData.getResponseMessage());
                                IotHubTransportMessage responseMessage = new IotHubTransportMessage(methodParserObject.toJson().getBytes(), MessageType.DEVICE_METHODS);
                                /*
                                 **Codes_SRS_DEVICEMETHOD_25_012: [**The device method message sent to IotHub shall have same the request id as the invoking message.**]**
                                 */
                                responseMessage.setRequestId(methodMessage.getRequestId());

                                // Codes_SRS_DEVICEMETHOD_34_016: [The device method message sent to IotHub shall have the sending device's id set as the connection device id.]
                                responseMessage.setConnectionDeviceId(this.nestedConfig.getDeviceId());

                                /*
                                 **Codes_SRS_DEVICEMETHOD_25_013: [**The device method message sent to IotHub shall have the status provided by the user as the message status.**]**
                                 */
                                responseMessage.setStatus(String.valueOf(responseData.getStatus()));
                                responseMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_SEND_RESPONSE);

                                deviceIO.sendEventAsync(responseMessage, new deviceMethodRequestMessageCallback(), null, nestedConfig.getDeviceId());
                                result = IotHubMessageResult.COMPLETE;
                            }
                            else
                            {
                                log.info("User callback did not send any data for response");
                                result = IotHubMessageResult.REJECT;
                                /*
                                 **Codes_SRS_DEVICEMETHOD_25_014: [**If the user invoked callback failed for any reason then the user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Rejected.**]**
                                 */
                                deviceMethodStatusCallback.execute(iotHubStatus, deviceMethodStatusCallbackContext);
                            }
                        } catch (Exception e)
                        {
                            log.info("User callback did not succeed");
                            result = IotHubMessageResult.REJECT;
                            /*
                             **Codes_SRS_DEVICEMETHOD_25_014: [**If the user invoked callback failed for any reason then the user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Rejected.**]**
                             */
                            deviceMethodStatusCallback.execute(iotHubStatus, deviceMethodStatusCallbackContext);
                        }
                    }
                    else
                    {
                        log.warn("Received device method request, but device has not setup device method");
                    }
                }
                else
                {
                    log.warn("Received unknown type message for device methods");
                }

                return result;
            }
        }
    }

    private final class deviceMethodRequestMessageCallback implements IotHubEventCallback
    {
        @Override
        public void execute(IotHubStatusCode responseStatus, Object callbackContext)
        {
            deviceMethodStatusCallback.execute(responseStatus, deviceMethodStatusCallbackContext);
        }
    }

    /**
     * This constructor creates an instance of device method class which helps facilitate the interation for device methods
     * between the user and IotHub.
     * @param deviceMethodStatusCallback Callback to provide status for device method state with IotHub. Cannot be {@code null}.
     * @param deviceMethodStatusCallbackContext Context to be passed when device method status is invoked. Can be {@code null}
     * @param deviceIO  Device client  object for this connection instance for the device. Cannot be {@code null}
     * @param config  Device client  configuration Cannot be {@code null}
     * @throws  IllegalArgumentException This exception is thrown if either deviceIO or config or deviceMethodStatusCallback are null
     */
    public DeviceMethod(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IllegalArgumentException
    {
        if (deviceIO == null || config == null)
        {
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        if (deviceMethodStatusCallback == null)
        {
            throw new IllegalArgumentException("Status call back cannot be null");
        }

        this.deviceIO = deviceIO;
        this.config = config;
        this.deviceMethodStatusCallback = deviceMethodStatusCallback;
        this.deviceMethodStatusCallbackContext = deviceMethodStatusCallbackContext;
        this.config.setDeviceMethodsMessageCallback(new deviceMethodResponseCallback(), null);
    }

    /**
     * A method which subscribes to receive device method invocation for the user with the IotHub.
     * @param deviceMethodCallback Callback where upon receiving the request the
     *                             invoke a method shall be triggered.
     * @param deviceMethodCallbackContext Context to be passed on when invoking the
     *                                    callback.
     * @throws IllegalArgumentException This exception is thrown when deviceMethodCallback is provided null.
     */
    public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext) throws IllegalArgumentException
    {
        if (deviceMethodCallback == null)
        {
            throw new IllegalArgumentException("Callback cannot be null");
        }

        this.deviceMethodCallback = deviceMethodCallback;
        this.deviceMethodCallbackContext = deviceMethodCallbackContext;

        if (!isSubscribed)
        {
            IotHubTransportMessage subscribeMessage = new IotHubTransportMessage(new byte[0], MessageType.DEVICE_METHODS);
            subscribeMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
            subscribeMessage.setConnectionDeviceId(this.config.getDeviceId());
            this.deviceIO.sendEventAsync(subscribeMessage, deviceMethodRequestMessageCallback, null, this.config.getDeviceId());
        }
    }
}
