// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.DeviceTwin;

import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.device.*;

public final class DeviceMethod
{
    private DeviceMethodCallback deviceMethodCallback;
    private Object deviceMethodCallbackContext;
    private IotHubEventCallback deviceMethodStatusCallback;
    private Object deviceMethodStatusCallbackContext;
    private final ObjectLock DEVICE_METHOD_LOCK = new ObjectLock();

    private boolean isSubscribed = false;

    private DeviceIO deviceIO;
    private DeviceClientConfig config;

    private final CustomLogger logger = new CustomLogger(this.getClass());

    private final class deviceMethodResponseCallback implements MessageCallback
    {
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

                if (message.getMessageType() != MessageType.DeviceMethods)
                {
                    /*
                    **Codes_SRS_DEVICEMETHOD_25_009: [**If the received message is not of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Abandon **]**
                     */
                    logger.LogFatal("Unexpected message type received");
                    deviceMethodStatusCallback.execute(iotHubStatus, deviceMethodStatusCallbackContext);
                    return IotHubMessageResult.ABANDON;
                }

                DeviceMethodMessage methodMessage = (DeviceMethodMessage) message;

                switch (methodMessage.getDeviceOperationType())
                {
                    case DEVICE_OPERATION_METHOD_RECEIVE_REQUEST:

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
                                DeviceMethodData responseData = deviceMethodCallback.call(methodMessage.getMethodName(), methodMessage.getBytes(), deviceMethodCallbackContext);
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
                                    DeviceMethodMessage responseMessage = new DeviceMethodMessage(methodParserObject.toJson().getBytes());
                                    /*
                                    **Codes_SRS_DEVICEMETHOD_25_012: [**The device method message sent to IotHub shall have same the request id as the invoking message.**]**
                                     */
                                    responseMessage.setRequestId(methodMessage.getRequestId());
                                    /*
                                    **Codes_SRS_DEVICEMETHOD_25_013: [**The device method message sent to IotHub shall have the status provided by the user as the message status.**]**
                                     */
                                    responseMessage.setStatus(String.valueOf(responseData.getStatus()));
                                    responseMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_SEND_RESPONSE);

                                    deviceIO.sendEventAsync(responseMessage, new deviceMethodRequestMessageCallback(), null);
                                    result = IotHubMessageResult.COMPLETE;
                                }
                                else
                                {
                                    logger.LogInfo("User callback did not send any data for response");
                                    result = IotHubMessageResult.REJECT;
                                    /*
                                    **Codes_SRS_DEVICEMETHOD_25_014: [**If the user invoked callback failed for any reason then the user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Rejected.**]**
                                     */
                                    deviceMethodStatusCallback.execute(iotHubStatus, deviceMethodStatusCallbackContext);
                                }
                            }
                            catch (Exception e)
                            {
                                logger.LogInfo("User callback did not succeed");
                                result = IotHubMessageResult.REJECT;
                                /*
                                **Codes_SRS_DEVICEMETHOD_25_014: [**If the user invoked callback failed for any reason then the user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Rejected.**]**
                                 */
                                deviceMethodStatusCallback.execute(iotHubStatus, deviceMethodStatusCallbackContext);
                            }
                        }
                        else
                        {
                            logger.LogInfo("Received device method request, but device has not setup device method");
                        }
                        break;

                    default:
                        logger.LogFatal("Received unknown type message for device methods");
                        break;
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
            if (deviceMethodStatusCallback != null)
            {
                deviceMethodStatusCallback.execute(responseStatus, deviceMethodStatusCallbackContext);
            }

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
     *
     */
    public DeviceMethod(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IllegalArgumentException
    {

        if (deviceIO == null || config == null)
        {
            /*
            **Codes_SRS_DEVICEMETHOD_25_001: [**The constructor shall throw IllegalArgument Exception if any of the parameters i.e deviceIO, config, deviceMethodStatusCallback are null. **]**
             */
            throw new IllegalArgumentException("Client or config cannot be null");
        }

        if (deviceMethodStatusCallback == null)
        {
            throw new IllegalArgumentException("Status call back cannot be null");
        }

        /*
        **Codes_SRS_DEVICEMETHOD_25_003: [**The constructor shall save all the parameters specified i.e deviceIO, config, deviceMethodStatusCallback, deviceMethodStatusCallbackContext.**]**
         */
        this.deviceIO = deviceIO;
        this.config = config;
        this.deviceMethodStatusCallback = deviceMethodStatusCallback;
        this.deviceMethodStatusCallbackContext = deviceMethodStatusCallbackContext;

        /*
        **Codes_SRS_DEVICEMETHOD_25_002: [**The constructor shall save the device method messages callback callback, by calling setDeviceMethodMessageCallback, where any further messages for device method shall be delivered.**]**
         */
        this.config.setDeviceMethodMessageCallback(new deviceMethodResponseCallback(), null);
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
            /*
            **Codes_SRS_DEVICEMETHOD_25_004: [**If deviceMethodCallback parameter is null then this method shall throw IllegalArgumentException**]**
             */
            throw new IllegalArgumentException("Callback cannot be null");
        }

        this.deviceMethodCallback = deviceMethodCallback;
        this.deviceMethodCallbackContext = deviceMethodCallbackContext;

        if (!isSubscribed)
        {
            /*
            **Codes_SRS_DEVICEMETHOD_25_005: [**If not already subscribed then this method shall create a device method message with empty payload and set its type as DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST.**]**
            **Codes_SRS_DEVICEMETHOD_25_006: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**
             */
            DeviceMethodMessage subscribeMessage = new DeviceMethodMessage(new byte[0]);
            subscribeMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST);
            this.deviceIO.sendEventAsync(subscribeMessage, new deviceMethodRequestMessageCallback(), null);
        }

    }

}
