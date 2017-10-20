// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public final class AmqpsDeviceTelemetry extends AmqpsDeviceOperations
{
    private final String SENDER_LINK_ENDPOINT_PATH = "/devices/%s/messages/events";
    private final String RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/messages/devicebound";

    private final String SENDER_LINK_TAG_PREFIX = "sender_link_telemetry-";
    private final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_telemetry-";
    
    private final String AMQP_DIAGNOSTIC_ID_KEY = "Diagnostic-Id";
    private final String AMQP_DIAGNOSTIC_CONTEXT_KEY = "Correlation-Context";

    /**
     * This constructor creates an instance of AmqpsDeviceTelemetry class and initializes member variables
     */
    AmqpsDeviceTelemetry(String deviceId) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_001: [The constructor shall throw IllegalArgumentException if the deviceId argument is null or empty.]
        if ((deviceId == null) || (deviceId.isEmpty()))
        {
            throw new IllegalArgumentException("The deviceId cannot be null or empty.");
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_002: [The constructor shall set the sender and receiver endpoint path to IoTHub specific values.]
        this.senderLinkEndpointPath = SENDER_LINK_ENDPOINT_PATH;
        this.receiverLinkEndpointPath = RECEIVER_LINK_ENDPOINT_PATH;

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_003: [The constructor shall concatenate a sender specific prefix to the sender link tag's current value.]
        this.senderLinkTag = SENDER_LINK_TAG_PREFIX + senderLinkTag;
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_004: [The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.]
        this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + receiverLinkTag;

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_005: [The constructor shall insert the given deviceId argument to the sender and receiver link address.]
        this.senderLinkAddress = String.format(senderLinkEndpointPath, deviceId);
        this.receiverLinkAddress = String.format(receiverLinkEndpointPath, deviceId);
    }

    @Override
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
    {
        if (messageType == MessageType.DEVICE_TELEMETRY)
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_007: [The function shall call the super function with the arguments and return with it's return value.]
            return super.sendMessageAndGetDeliveryHash(messageType, msgData, offset, length, deliveryTag);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_006: [The function shall return an AmqpsSendReturnValue object with false and -1 if the message type is not DEVICE_TELEMETRY.]
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_020: [The function shall call the super function.]
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(linkName);
        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_021: [The function shall set the MessageType to DEVICE_TELEMETRY if the super function returned not null.]
            amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TELEMETRY);
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_022: [The function shall return the super function return value.]
        return amqpsMessage;
    }

    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig)
    {
        if ((amqpsMessage.getAmqpsMessageType() == null) || (amqpsMessage.getAmqpsMessageType() == MessageType.DEVICE_TELEMETRY))
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_009: [The function shall create a new IoTHubMessage using the Proton message body.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_010: [**The function shall copy the correlationId, messageId, To and userId properties to the IotHubMessage properties.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_011: [The function shall copy the Proton application properties to IoTHubMessage properties excluding the reserved property names.]
            Message message = protonMessageToIoTHubMessage(amqpsMessage);

            MessageCallback messageCallback = deviceClientConfig.getDeviceTelemetryMessageCallback();
            Object messageContext = deviceClientConfig.getDeviceTelemetryMessageContext();

            // Codes_SRS_AMQPSDEVICETELEMETRY_12_012: [The function shall create a new AmqpsConvertFromProtonReturnValue object and fill it with the converted message and the user callback and user context values from the deviceClientConfig.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_013: [The function shall return with the new AmqpsConvertFromProtonReturnValue object.]
            return new AmqpsConvertFromProtonReturnValue(message, messageCallback, messageContext);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_008: [The function shall return null if the Proton message type is not null or DEVICE_TELEMETRY.]
            return null;
        }
    }

    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message)
    {
        if ((message.getMessageType() == null) || (message.getMessageType() == MessageType.DEVICE_TELEMETRY))
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_015: [The function shall create a new Proton message using the IoTHubMessage body.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_016: [The function shall copy the correlationId, messageId properties to the Proton message properties.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_017: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
            MessageImpl protonMessage = iotHubMessageToProtonMessage(message);

            // Codes_SRS_AMQPSDEVICETELEMETRY_12_018: [The function shall create a new AmqpsConvertToProtonReturnValue object and fill it with the Proton message and the message type.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_019: [The function shall return with the new AmqpsConvertToProtonReturnValue object.]
            return new AmqpsConvertToProtonReturnValue(protonMessage, MessageType.DEVICE_TELEMETRY);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_014: [The function shall return null if the Proton message type is not null or DEVICE_TELEMETRY.]
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
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_024: [The function shall shall create a new buffer for message body and copy the proton message body to it.]
            Binary b = d.getValue();
            msgBody = new byte[b.getLength()];
            ByteBuffer buffer = b.asByteBuffer();
            buffer.get(msgBody);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_025: [The function shall create a new empty buffer for message body if the proton message body is null.]
            msgBody = new byte[0];
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_009: [The function shall create a new IoTHubMessage using the Proton message body.]
        Message message = new Message(msgBody);
        message.setMessageType(MessageType.DEVICE_TELEMETRY);

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_010: [The function shall copy the correlationId, messageId, To and userId properties to the IotHubMessage properties.]
        Properties properties = protonMsg.getProperties();
        if (properties != null)
        {
            if (properties.getCorrelationId() != null)
            {
                message.setCorrelationId(properties.getCorrelationId().toString());
            }

            if (properties.getMessageId() != null)
            {
                message.setMessageId(properties.getMessageId().toString());
            }

            if (properties.getTo() != null)
            {
                message.setProperty(AMQPS_APP_PROPERTY_PREFIX + TO_KEY, properties.getTo());
            }

            if (properties.getUserId() != null)
            {
                message.setProperty(AMQPS_APP_PROPERTY_PREFIX + USER_ID_KEY, properties.getUserId().toString());
            }
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_011: [The function shall copy the Proton application properties to IoTHubMessage properties excluding the reserved property names.]
        if (protonMsg.getApplicationProperties() != null)
        {
            Map<String, String> applicationProperties = protonMsg.getApplicationProperties().getValue();
            for (Map.Entry<String, String> entry : applicationProperties.entrySet())
            {
                String propertyKey = entry.getKey();
                if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(propertyKey))
                {
                    message.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }

        return message;
    }

    /**
     * Creates a proton message from the IoTHub message.
     * @param message the IoTHub input message.
     * @return the proton message.
     */
    @Override
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_015: [The function shall create a new Proton message using the IoTHubMessage body.]
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_016: [The function shall copy the correlationId, messageId properties to the Proton message properties.]
        Properties properties = new Properties();
        if (message.getMessageId() != null)
        {
            properties.setMessageId(message.getMessageId());
        }

        if (message.getCorrelationId() != null)
        {
            properties.setCorrelationId(message.getCorrelationId());
        }

        outgoingMessage.setProperties(properties);

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_017: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
        if (message.getProperties().length > 0)
        {
            Map<String, String> userProperties = new HashMap<>(message.getProperties().length);
            for(MessageProperty messageProperty : message.getProperties())
            {
                if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(messageProperty.getName()))
                {
                    userProperties.put(messageProperty.getName(), messageProperty.getValue());
                }

            }

            ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
            outgoingMessage.setApplicationProperties(applicationProperties);
        }

        if (message.getDiagnosticPropertyData() != null) {
            // Codes_SRS_AMQPSDEVICETELEMETRY_13_001: [The function shall add diagnostic information as AMQP message annotation.]
            Map<Symbol, Object> annotationMap = new HashMap<>();
            annotationMap.put(Symbol.getSymbol(AMQP_DIAGNOSTIC_ID_KEY), message.getDiagnosticPropertyData().getDiagnosticId());
            annotationMap.put(Symbol.getSymbol(AMQP_DIAGNOSTIC_CONTEXT_KEY), message.getDiagnosticPropertyData().getCorrelationContext());
            outgoingMessage.setMessageAnnotations(new MessageAnnotations(annotationMap));
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_023: [The function shall set the proton message body using the IotHubTransportMessage body.]
        Binary binary = new Binary(message.getBytes());
        Section section = new Data(binary);
        outgoingMessage.setBody(section);
        return outgoingMessage;
    }
}
