// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.*;
import lombok.Getter;
import lombok.Setter;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;

/**
 * Extends Message, adding transport artifacts.
 */
public class IotHubTransportMessage extends Message
{
    /**
     * The method invoked for the message (POST, GET).
     * <p>
     *     Required in IoTHubTransportManager
     * </p>
     */
    @Getter
    @Setter
    private HttpsMethod iotHubMethod;

    /**
     * The URI path of this message.
     * <p>
     *     Required in IoTHubTransportManager
     * </p>
     */
    @Getter
    @Setter
    private String uriPath;

    /**
     * The method name of device method operation
     */
    @Getter
    private String methodName;

    /**
     * The version for the message.
     */
    @Getter
    @Setter
    private Integer version;

    /**
     * The message id.
     */
    @Getter
    @Setter
    private String requestId;

    /**
     * The message status.
     */
    @Getter
    @Setter
    private String status;

    /**
     * The device operation type.
     */
    @Getter
    @Setter
    private DeviceOperations deviceOperationType;

    /**
     * The message callback.
     */
    @Getter
    @Setter
    private MessageCallback messageCallback;

    /**
     * The message callback context.
     */
    @Getter
    @Setter
    private Object messageCallbackContext;

    /**
     * Constructor with binary data and message type
     *
     * @param data        The byte array of the message.
     * @param messageType The messageType of the message.
     */
    public IotHubTransportMessage(byte[] data, MessageType messageType)
    {
        super(data);
        super.setMessageType(messageType);
        this.methodName = null;
        this.version = 0;
        this.requestId = null;
        this.status = null;
        this.deviceOperationType = DeviceOperations.DEVICE_OPERATION_UNKNOWN;
    }

    /**
     * Constructor.
     *
     * @param body The body of the new Message instance. It is internally serialized to a byte array using UTF-8 encoding.
     */
    public IotHubTransportMessage(String body)
    {
        super(body);
        super.setMessageType(MessageType.UNKNOWN);
        this.methodName = null;
        this.version = 0;
        this.requestId = null;
        this.status = null;
        this.deviceOperationType = DeviceOperations.DEVICE_OPERATION_UNKNOWN;
    }

    public IotHubTransportMessage(byte[] data, MessageType messageType, String messageId, String correlationId, MessageProperty[] messageProperties)
    {
        super(data);
        super.setMessageType(messageType);
        this.setMessageId(messageId);
        this.setCorrelationId(correlationId);

        for (MessageProperty messageProperty : messageProperties)
        {
            this.setProperty(messageProperty.getName(), messageProperty.getValue());
        }
    }

    public boolean isMessageAckNeeded(IotHubClientProtocol protocol)
    {
        if (protocol == IotHubClientProtocol.MQTT || protocol == IotHubClientProtocol.MQTT_WS)
        {
            //This is a SUBSCRIBE action in MQTT which we currently treat synchronously, so there is no message ack
            // unlike other send operations
            return this.deviceOperationType != DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST
                    && this.deviceOperationType != DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST
                    && this.deviceOperationType != DeviceOperations.DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST;
        }

        return true;
    }

    /**
     * The method name of device method operation
     *
     * @param methodName The String containing the method name of device method operation.
     */
    public void setMethodName(String methodName)
    {
        if (methodName == null)
        {
            throw new IllegalArgumentException("Method name cannot be null");
        }
        this.methodName = methodName;
    }

    @Override
    public String toString()
    {
        StringBuilder base = new StringBuilder(super.toString());
        if (this.requestId != null && !this.requestId.isEmpty())
        {
            base.append("Request Id [").append(this.requestId).append("] ");
        }

        if (this.getDeviceOperationType() != null && this.getDeviceOperationType() != DeviceOperations.DEVICE_OPERATION_UNKNOWN)
        {
            base.append("Device Operation Type [").append(this.getDeviceOperationType()).append("] ");
        }

        return base.toString();
    }
}
