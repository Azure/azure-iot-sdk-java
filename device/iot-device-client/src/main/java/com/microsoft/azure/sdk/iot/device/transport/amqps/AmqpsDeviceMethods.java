// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AmqpsDeviceMethods extends AmqpsDeviceOperations
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "methods:";

    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME = "IoThub-methodname";
    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_STATUS = "IoThub-status";

    private static final String SENDER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
    private static final String RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";

    private static final String SENDER_LINK_ENDPOINT_PATH_MODULES = "/devices/%s/modules/%s/methods/devicebound";
    private static final String RECEIVER_LINK_ENDPOINT_PATH_MODULES = "/devices/%s/modules/%s/methods/devicebound";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_devicemethods-";
    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_devicemethods-";

    private DeviceClientConfig deviceClientConfig;

    /**
     * This constructor creates an instance of AmqpsDeviceMethods class and initializes member variables
     */
    AmqpsDeviceMethods(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSDEVICEMETHODS_34_050: [This constructor shall call super with the provided user agent string.]
        super(deviceClientConfig, SENDER_LINK_ENDPOINT_PATH, RECEIVER_LINK_ENDPOINT_PATH,
                SENDER_LINK_ENDPOINT_PATH_MODULES, RECEIVER_LINK_ENDPOINT_PATH_MODULES,
                SENDER_LINK_TAG_PREFIX, RECEIVER_LINK_TAG_PREFIX);

        this.deviceClientConfig = deviceClientConfig;

        String moduleId = this.deviceClientConfig.getModuleId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_34_037: [If a moduleId is present, the constructor shall add correlation ID key and a UUID value to the amqpProperties.]
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX +  UUID.randomUUID().toString()));
        }
        else
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_007: [The constructor shall add correlation ID key and deviceId value to the amqpProperties.]
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX +  UUID.randomUUID().toString()));
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_006: [The constructor shall add API version key and API version value to the amqpProperties.]
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), TransportUtils.IOTHUB_API_VERSION);
    }

    /**
     * Identify if the given link is owned by the operation
     *
     * @return true if the link is owned by the operation, false otherwise
     */
    @Override
    protected Boolean isLinkFound(String linkName)
    {
        // Codes_SRS_AMQPSDEVICEMETHODS_12_047: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getSenderLinkTag()))
        {
            this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.OPENED;
            return true;
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_048: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getReceiverLinkTag()))
        {
            this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.OPENED;
            return true;
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_049: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
        return false;
    }

    /**
     * Sends the given message and returns with the delivery hash if the message type is methods
     *
     * @param msgData The binary array of the bytes to send
     * @param offset The start offset to copy the bytes from
     * @param length The number of bytes to be send related to the offset
     * @param deliveryTag The unique identfier of the delivery
     * @return delivery tag
     * @throws IllegalStateException if sender link has not been initialized
     * @throws IllegalArgumentException if deliveryTag's length is 0
     */
    @Override
    protected synchronized AmqpsSendReturnValue sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
    {
        if (messageType == MessageType.DEVICE_METHODS)
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_010: [The function shall call the super function if the MessageType is DEVICE_METHODS, and return with it's return value.]
            return super.sendMessageAndGetDeliveryTag(messageType, msgData, offset, length, deliveryTag);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_011: [The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.]
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    /**
     * Read the message from Proton if the link name matches
     * Set the message type to methods
     *
     * @param linkName The receiver link's name to read from
     * @return the received message
     * @throws TransportException if Proton throws
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICEMETHODS_12_012: [The function shall call the super function.]
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(linkName);
        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_013: [The function shall set the MessageType to DEVICE_METHODS if the super function returned not null.]
            amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_METHODS);
            amqpsMessage.setDeviceClientConfig(this.deviceClientConfig);
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_014: [The function shall return the super function return value.]
        return amqpsMessage;
    }

    /**
     * Convert Proton message to IoTHubMessage if the message type is methods
     *
     * @param amqpsMessage The Proton message to convert
     * @param deviceClientConfig The device client configuration
     * @return the converted message
     */
    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        if ((amqpsMessage.getAmqpsMessageType() == MessageType.DEVICE_METHODS) &&
            (this.deviceClientConfig.getDeviceId().equals(deviceClientConfig.getDeviceId())))
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_016: [The function shall convert the amqpsMessage to IoTHubTransportMessage.]
            Message message = protonMessageToIoTHubMessage(amqpsMessage);

            MessageCallback messageCallback = deviceClientConfig.getDeviceMethodsMessageCallback();
            Object messageContext = deviceClientConfig.getDeviceMethodsMessageContext();

            // Codes_SRS_AMQPSDEVICEMETHODS_12_027: [The function shall create a AmqpsConvertFromProtonReturnValue and set the message field to the new IotHubTransportMessage.]
            // Codes_SRS_AMQPSDEVICEMETHODS_12_028: [The function shall create a AmqpsConvertFromProtonReturnValue and copy the DeviceClientConfig callback and context to it.]
            return new AmqpsConvertFromProtonReturnValue(message, messageCallback, messageContext);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_015: [The function shall return null if the message type is not DEVICE_METHODS.]
            return null;
        }
    }

    /**
     * Convert IoTHubMessage to Proton message
     * Set the message type to methods
     *
     * @param message The IoTHubMessage to convert
     * @return the converted message
     */
    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException
    {
        if (message.getMessageType() == MessageType.DEVICE_METHODS)
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_030: [The function shall convert the IoTHubTransportMessage to a proton message.]
            MessageImpl protonMessage = iotHubMessageToProtonMessage(message);

            // Codes_SRS_AMQPSDEVICEMETHODS_12_041: [The function shall create a AmqpsConvertToProtonReturnValue and set the message field to the new proton message.]
            // Codes_SRS_AMQPSDEVICEMETHODS_12_042: [The function shall create a AmqpsConvertToProtonReturnValue and set the message type to DEVICE_METHODS.]
            return new AmqpsConvertToProtonReturnValue(protonMessage, MessageType.DEVICE_METHODS);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_029: [The function shall return null if the message type is not DEVICE_METHODS.]
            return null;
        }
    }

    /**
     * Converts an AMQPS message to a corresponding IoT Hub message.
     *
     * @param protonMsg the AMQPS message.
     * @throws TransportException if the conversion fails
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(MessageImpl protonMsg) throws TransportException
    {
        IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg);
        iotHubTransportMessage.setMessageType(MessageType.DEVICE_METHODS);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_046: [The function shall set the device operation type to DEVICE_OPERATION_METHOD_RECEIVE_REQUEST on IotHubTransportMessage.]
        iotHubTransportMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_025: [The function shall copy the method name from Proton application properties and set IotHubTransportMessage method name with it.]
        // Codes_SRS_AMQPSDEVICEMETHODS_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
        if (protonMsg.getApplicationProperties() != null && protonMsg.getApplicationProperties().getValue() != null)
        {
            Map<String, Object> applicationProperties = protonMsg.getApplicationProperties().getValue();

            if (applicationProperties.containsKey(APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME))
            {
                iotHubTransportMessage.setMethodName(applicationProperties.get(APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME).toString());
            }
        }

        if (protonMsg.getProperties() != null && protonMsg.getProperties().getCorrelationId() != null)
        {
            iotHubTransportMessage.setRequestId(protonMsg.getProperties().getCorrelationId().toString());
        }

        return iotHubTransportMessage;
    }

    /**
     * Creates a proton message from the IoTHub message.
     * @param message the IoTHub input message.
     * @throws TransportException if the conversion fails
     * @return the proton message.
     */
    @Override
    protected MessageImpl iotHubMessageToProtonMessage(Message message) throws TransportException
    {
        MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);
        IotHubTransportMessage deviceMethodMessage = (IotHubTransportMessage)message;

        // Codes_SRS_AMQPSDEVICEMETHODS_12_031: [The function shall copy the correlationId, messageId properties to the Proton message properties.]
        Properties properties;
        if (protonMessage.getProperties() != null)
        {
            properties = protonMessage.getProperties();
        }
        else
        {
            properties = new Properties();
        }

        if (deviceMethodMessage.getRequestId() != null)
        {
            properties.setCorrelationId(UUID.fromString(deviceMethodMessage.getRequestId()));
        }

        protonMessage.setProperties(properties);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_033: [The function shall set the proton message status field to the value of IoTHubTransportMessage status field.]
        Map<String, Object> userProperties = new HashMap<>();
        if (deviceMethodMessage.getStatus() != null)
        {
            userProperties.put(APPLICATION_PROPERTY_KEY_IOTHUB_STATUS, Integer.parseInt(deviceMethodMessage.getStatus()));
        }

        Map<String, Object> applicationPropertiesMap = new HashMap<>();
        if (protonMessage.getApplicationProperties() != null && protonMessage.getApplicationProperties().getValue() != null)
        {
            applicationPropertiesMap = protonMessage.getApplicationProperties().getValue();
            userProperties.putAll(applicationPropertiesMap);
        }

        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        protonMessage.setApplicationProperties(applicationProperties);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_040: [The function shall set the proton message body using the IotHubTransportMessage body.]
        return protonMessage;
    }
}
