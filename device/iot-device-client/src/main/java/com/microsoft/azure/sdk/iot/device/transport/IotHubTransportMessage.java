// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.*;

/**
 * Extends Message, adding transport artifacts.
 */
public class IotHubTransportMessage extends Message
{
    /// <summary>
    /// [Required in IoTHubTransportManager] Used to specify the method invoked for the message (POST, GET).
    /// </summary>
    private IotHubMethod iotHubMethod;

    /// <summary>
    /// [Required in IoTHubTransportManager] Used to specify the URI path of this message.
    /// </summary>
    private String uriPath;

    private String methodName;
    private String version;
    private String requestId;
    private String status;
    private DeviceOperations operationType;
    private MessageCallback messageCallback;
    private Object messageCallbackContext;

    /**
     * Constructor with binary data and message type
     * @param data The byte array of the message.
     * @param messageType The messageType of the message.
     */
    public IotHubTransportMessage(byte[] data, MessageType messageType)
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_001: [**If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.**]**
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_002: [**The constructor shall save the message body by calling super with the body as parameter.**]**
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_003: [**The constructor shall set the messageType to the given value by calling the super with the given value.**]**
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_015: [**The constructor shall initialize version, requestId and status to null.**]**
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_016: [**The constructor shall initialize operationType to UNKNOWN**]**
        */
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
        // Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_002: [This method shall throw IllegalArgumentException if the body argument is null.]
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
        //Codes_SRS_IOTHUBTRANSPORTMESSAGE_34_017: [This constructor shall return an instance of IotHubTransportMessage with provided bytes, messagetype, correlationid, messageid, and application properties.]
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
    public void setVersion(String version)
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_004: [**The function shall set the version.**]**
         */
        this.version = version;
    }

    /**
     * Getter for the message version
     * @return the String containing the version.
     */
    public String getVersion()
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_005: [**The function shall return the value of the version either set by the setter or the default (null) if unset so far.**]**
         */
        return  this.version;
    }

    /**
     * Setter for the message id
     * @param id The String containing the id.
     */
    public void setRequestId(String id)
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_006: [**The function shall save the request id.**]**
         */
        this.requestId = id;
    }

    /**
     * Getter for the request id
     * @return the String containing the request id.
     */
    public String getRequestId()
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_007: [**The function shall return the value of the request id either set by the setter or the default (null) if unset so far.**]**
         */
        return this.requestId;
    }

    /**
     * Setter for the status
     * @param status The String containing the status.
     */
    public void setStatus(String status)
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_008: [**The function shall save the status.**]**
         */
        this.status = status;
    }

    /**
     * Getter for the request status
     * @return the String containing the request status.
     */
    public String getStatus()
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_009: [**The function shall return the value of the status either set by the setter or the default (null) if unset so far.**]**
         */
        return this.status;
    }

    /**
     * Setter for the device operation type
     * @param deviceOperationType The DeviceOperations enum value.
     */
    public void setDeviceOperationType(DeviceOperations deviceOperationType)
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_010: [**The function shall save the device twin operation type.**]**
         */
        this.operationType = deviceOperationType;
    }

    /**
     * Getter for the device operation type
     * @return the DeviceOperations eum value with the current operation type.
     */
    public DeviceOperations getDeviceOperationType()
    {
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_011: [**The function shall return the operation type either set by the setter or the default if unset so far.**]**
         */
        return this.operationType;
    }

    /**
     * Setter for the method name of device method operation
     * @param methodName The String containing the method name of device method operation.
     */
    public void setMethodName(String methodName)
    {
        if (methodName == null)
        {
            /*
            **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_012: [**The function shall throw IllegalArgumentException if the methodName is null.**]**
            **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_013: [**The function shall set the methodName.**]**
             */
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
        /*
        **Codes_SRS_IOTHUBTRANSPORTMESSAGE_12_014: [**The function shall return the methodName either set by the setter or the default (null) if unset so far.**]**
         */
        return methodName;
    }

    /**
     * Setter for the IoT Hub method
     * @param iotHubMethod The enum containing the IoT Hub method.
     */
    public void setIotHubMethod(IotHubMethod iotHubMethod)
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_002: [The setIotHubMethod shall store the iotHubMethod. This function do not evaluates this parameter.] */
        this.iotHubMethod = iotHubMethod;
    }

    /**
     * Setter for the URI path
     * @param uriPath The string with the URI path.
     */
    public void setUriPath(String uriPath)
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_003: [The setUriPath shall store the uriPath. This function do not evaluates this parameter.] */
        this.uriPath = uriPath;
    }

    /**
     * Getter for the IoT Hub method
     * @return the IoT Hub method (POST, GET).
     */
    public IotHubMethod getIotHubMethod()
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_004: [The getIotHubMethod shall return the stored iotHubMethod.] */
        return this.iotHubMethod;
    }

    /**
     * Getter for the URI path
     * @return the string with the URI path.
     */
    public String getUriPath()
    {
        /* Codes_SRS_IOTHUBTRANSPORTMESSAGE_21_005: [The getUriPath shall return the stored uriPath.] */
        return uriPath;
    }
}
