// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import lombok.Getter;
import lombok.Setter;

/**
 * Extends Message, adding transport artifacts.
 */
public class IotHubTransportMessage extends Message
{
    /// <summary>
    /// [Required in IoTHubTransportManager] Used to specify the method invoked for the message (POST, GET).
    /// </summary>
    private HttpsMethod iotHubMethod;

    /// <summary>
    /// [Required in IoTHubTransportManager] Used to specify the URI path of this message.
    /// </summary>
    private String uriPath;

    private String methodName;
    private Integer version;
    private String requestId;
    private String status;
    private DeviceOperations operationType;
    private MessageCallback messageCallback;
    private Object messageCallbackContext;

    @Getter
    @Setter
    private int qualityOfService;

    /**
     * Constructor with binary data and message type
     * @param data The byte array of the message.
     * @param messageType The messageType of the message.
     */
    public IotHubTransportMessage(byte[] data, MessageType messageType)
    {
        super(data);
        super.setMessageType(messageType);
        this.methodName = null;
        this.version = null;
        this.requestId = null;
        this.status = null;
        this.operationType = DeviceOperations.DEVICE_OPERATION_UNKNOWN;
    }

    /**
     * Constructor.
     * @param body The body of the new Message instance. It is internally serialized to a byte array using UTF-8 encoding.
     */
    public IotHubTransportMessage(String body)
    {
        super(body);
        super.setMessageType(MessageType.UNKNOWN);
        this.methodName = null;
        this.version = null;
        this.requestId = null;
        this.status = null;
        this.operationType = DeviceOperations.DEVICE_OPERATION_UNKNOWN;
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

    public MessageCallback getMessageCallback()
    {
        return messageCallback;
    }

    public void setMessageCallback(MessageCallback messageCallback)
    {
        this.messageCallback = messageCallback;
    }

    public Object getMessageCallbackContext()
    {
        return messageCallbackContext;
    }

    public void setMessageCallbackContext(Object messageCallbackContext)
    {
        this.messageCallbackContext = messageCallbackContext;
    }

    /**
     * Setter for the message version
     * @param version The String containing the version.
     */
    public void setVersion(Integer version)
    {
        this.version = version;
    }

    /**
     * Getter for the message version
     * @return the String containing the version.
     */
    public Integer getVersion()
    {
        return this.version;
    }

    /**
     * Setter for the message id
     * @param id The String containing the id.
     */
    public void setRequestId(String id)
    {
        this.requestId = id;
    }

    /**
     * Getter for the request id
     * @return the String containing the request id.
     */
    public String getRequestId()
    {
        return this.requestId;
    }

    /**
     * Setter for the status
     * @param status The String containing the status.
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * Getter for the request status
     * @return the String containing the request status.
     */
    public String getStatus()
    {
        return this.status;
    }

    /**
     * Setter for the device operation type
     * @param deviceOperationType The DeviceOperations enum value.
     */
    public void setDeviceOperationType(DeviceOperations deviceOperationType)
    {
        this.operationType = deviceOperationType;
    }

    /**
     * Getter for the device operation type
     * @return the DeviceOperations eum value with the current operation type.
     */
    public DeviceOperations getDeviceOperationType()
    {
        return this.operationType;
    }

    public boolean isMessageAckNeeded(IotHubClientProtocol protocol)
    {
        if (protocol == IotHubClientProtocol.MQTT || protocol == IotHubClientProtocol.MQTT_WS)
        {
            //This is a SUBSCRIBE action in MQTT which we currently treat synchronously, so there is no message ack
            // unlike other send operations
            return this.operationType != DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST
                    && this.operationType != DeviceOperations.DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST
                    && this.operationType != DeviceOperations.DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST;
        }

        return true;
    }

    /**
     * Setter for the method name of device method operation
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

    /**
     * Getter for the method name of device method operation
     * @return the methodName of device method operation.
     */
    public String getMethodName()
    {
        return methodName;
    }

    /**
     * Setter for the IoT Hub method
     * @param iotHubMethod The HTTPS method.
     */
    public void setIotHubMethod(HttpsMethod iotHubMethod)
    {
        this.iotHubMethod = iotHubMethod;
    }

    /**
     * Setter for the URI path
     * @param uriPath The string with the URI path.
     */
    public void setUriPath(String uriPath)
    {
        this.uriPath = uriPath;
    }

    /**
     * Getter for the HTTPS method
     * @return the IoT Hub method (POST, GET).
     */
    public HttpsMethod getIotHubMethod()
    {
        return this.iotHubMethod;
    }

    /**
     * Getter for the URI path
     * @return the string with the URI path.
     */
    public String getUriPath()
    {
        return uriPath;
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
