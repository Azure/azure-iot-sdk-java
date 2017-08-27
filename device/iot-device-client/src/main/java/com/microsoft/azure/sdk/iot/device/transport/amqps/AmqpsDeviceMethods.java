// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AmqpsDeviceMethods extends AmqpsDeviceOperations
{
    private final static String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private final static String APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME = "IoThub-methodname";
    private final static String APPLICATION_PROPERTY_KEY_IOTHUB_STATUS = "IoThub-status";

    private final String SENDER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
    private final String RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";

    private final String SENDER_LINK_TAG_PREFIX = "sender_link_devicemethods-";
    private final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_devicemethods-";

    /**
     * This constructor creates an instance of AmqpsDeviceMethods class and initializes member variables
     */
    AmqpsDeviceMethods(String deviceId) throws IllegalArgumentException
    {
        super();

        // Codes_SRS_AMQPSDEVICEMETHODS_12_001: [The constructor shall throw IllegalArgumentException if the deviceId argument is null or empty.]
        if ((deviceId == null) || (deviceId.isEmpty()))
        {
            throw new IllegalArgumentException("The deviceId cannot be null or empty.");
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_002: [The constructor shall set the sender and receiver endpoint path to IoTHub specific values.]
        this.senderLinkEndpointPath = SENDER_LINK_ENDPOINT_PATH;
        this.receiverLinkEndpointPath = RECEIVER_LINK_ENDPOINT_PATH;

        // Codes_SRS_AMQPSDEVICEMETHODS_12_003: [The constructor shall concatenate a sender specific prefix to the sender link tag's current value.]
        this.senderLinkTag = SENDER_LINK_TAG_PREFIX + senderLinkTag;
        // Codes_SRS_AMQPSDEVICEMETHODS_12_004: [The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.]
        this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + senderLinkTag;

        // Codes_SRS_AMQPSDEVICEMETHODS_12_005: [The constructor shall insert the given deviceId argument to the sender and receiver link address.]
        this.senderLinkAddress = String.format(senderLinkEndpointPath, deviceId);
        this.receiverLinkAddress = String.format(receiverLinkEndpointPath, deviceId);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_006: [The constructor shall add API version key and API version value to the amqpProperties.]
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), API_VERSION_VALUE);
        // Codes_SRS_AMQPSDEVICEMETHODS_12_007: [The constructor shall add correlation ID key and deviceId value to the amqpProperties.]
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(deviceId));
    }

    @Override
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
    {
        if (messageType == MessageType.DEVICE_METHODS)
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_010: [The function shall call the super function if the MessageType is DEVICE_METHODS, and return with it's return value.]
            return super.sendMessageAndGetDeliveryHash(messageType, msgData, offset, length, deliveryTag);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_011: [The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.]
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException
    {
        // Codes_SRS_AMQPSDEVICEMETHODS_12_012: [The function shall call the super function.]
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(linkName);
        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_013: [The function shall set the MessageType to DEVICE_METHODS if the super function returned not null.]
            amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_METHODS);
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_014: [The function shall return the super function return value.]
        return amqpsMessage;
    }

    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig)
    {
        if (amqpsMessage.getAmqpsMessageType() == MessageType.DEVICE_METHODS)
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

    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message)
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
     *
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg)
    {
        byte[] msgBody;

        Data d = (Data) protonMsg.getBody();
        if (d != null)
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_018: [The function shall shall create a new buffer for message body and copy the proton message body to it.]
            Binary b = d.getValue();
            msgBody = new byte[b.getLength()];
            ByteBuffer buffer = b.asByteBuffer();
            buffer.get(msgBody);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICEMETHODS_12_017: [The function shall create a new empty buffer for message body if the proton message body is null.]
            msgBody = new byte[0];
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_019: [The function shall create a new IotHubTransportMessage using the Proton message body and set the message type to DEVICE_METHODS.]
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(msgBody, MessageType.DEVICE_METHODS);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_025: [The function shall copy the correlationId, messageId, To and userId properties to the IotHubTransportMessage properties.]
        Properties properties = protonMsg.getProperties();
        if (properties != null)
        {
            if (properties.getCorrelationId() != null)
            {
                iotHubTransportMessage.setRequestId(properties.getCorrelationId().toString());
            }

            if (properties.getMessageId() != null)
            {
                iotHubTransportMessage.setMessageId(properties.getMessageId().toString());
            }

            if (properties.getTo() != null)
            {
                iotHubTransportMessage.setProperty(AMQPS_APP_PROPERTY_PREFIX + TO_KEY, properties.getTo());
            }

            if (properties.getUserId() != null)
            {
                iotHubTransportMessage.setProperty(AMQPS_APP_PROPERTY_PREFIX + USER_ID_KEY, properties.getUserId().toString());
            }
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_025: [The function shall copy the method name from Proton application properties and set IotHubTransportMessage method name with it.]
        // Codes_SRS_AMQPSDEVICEMETHODS_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
        if (protonMsg.getApplicationProperties() != null)
        {
            Map<String, String> applicationProperties = protonMsg.getApplicationProperties().getValue();
            for (Map.Entry<String, String> entry : applicationProperties.entrySet())
            {
                String propertyKey = entry.getKey();

                if (propertyKey.equals(APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME))
                {
                    iotHubTransportMessage.setMethodName(entry.getValue());
                }
                else
                {
                    if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(propertyKey))
                    {
                        iotHubTransportMessage.setProperty(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
        // Codes_SRS_AMQPSDEVICEMETHODS_12_046: [The function shall set the device operation type to DEVICE_OPERATION_METHOD_RECEIVE_REQUEST on IotHubTransportMessage.]
        iotHubTransportMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        return iotHubTransportMessage;
    }

    /**
     * Creates a proton message from the IoTHub message.
     * @param message the IoTHub input message.
     * @return the proton message.
     */
    @Override
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        IotHubTransportMessage deviceMethodMessage = (IotHubTransportMessage)message;

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_015: [The function shall create a new Proton message using the IoTHubMessage body.]
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        // Codes_SRS_AMQPSDEVICEMETHODS_12_031: [The function shall copy the correlationId, messageId properties to the Proton message properties.]
        Properties properties = new Properties();
        if (deviceMethodMessage.getMessageId() != null)
        {
            properties.setMessageId(deviceMethodMessage.getMessageId());
        }

        if (deviceMethodMessage.getRequestId() != null)
        {
            properties.setCorrelationId(UUID.fromString(deviceMethodMessage.getRequestId()));
        }

        outgoingMessage.setProperties(properties);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_032: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
        int propertiesLength = deviceMethodMessage.getProperties().length;
        Map<String, Object> userProperties = new HashMap<>(propertiesLength);
        if (propertiesLength > 0)
        {
            for(MessageProperty messageProperty : deviceMethodMessage.getProperties())
            {
                if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(messageProperty.getName()))
                {
                    userProperties.put(messageProperty.getName(), messageProperty.getValue());
                }
            }
        }

        // Codes_SRS_AMQPSDEVICEMETHODS_12_033: [The function shall set the proton message status field to the value of IoTHubTransportMessage status field.]
        if (deviceMethodMessage.getStatus() != null)
        {
            userProperties.put(APPLICATION_PROPERTY_KEY_IOTHUB_STATUS, Integer.parseInt(deviceMethodMessage.getStatus()));
        }

        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        // Codes_SRS_AMQPSDEVICEMETHODS_12_040: [The function shall set the proton message body using the IotHubTransportMessage body.]
        Binary binary = new Binary(deviceMethodMessage.getBytes());
        Section section = new Data(binary);
        outgoingMessage.setBody(section);
        return outgoingMessage;
    }
}
