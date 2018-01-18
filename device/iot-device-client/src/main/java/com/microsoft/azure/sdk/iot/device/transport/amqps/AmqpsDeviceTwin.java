// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;

public final class AmqpsDeviceTwin extends AmqpsDeviceOperations
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "twin:";

    private static final String SENDER_LINK_ENDPOINT_PATH = "/devices/%s/twin";
    private static final String RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/twin";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_devicetwin-";
    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_devicetwin-";

    private static final String MESSAGE_ANNOTATION_FIELD_KEY_OPERATION = "operation";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE = "resource";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_STATUS = "status";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_VERSION = "version";

    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_GET = "GET";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PATCH = "PATCH";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PUT = "PUT";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_DELETE = "DELETE";

    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED = "/properties/reported";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED = "/notifications/twin/properties/desired";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED = "/properties/desired";

    Map<String, DeviceOperations> correlationIdList;

    private DeviceClientConfig deviceClientConfig;

    /**
     * This constructor creates an instance of AmqpsDeviceTwin class and initializes member variables
     * @throws IllegalArgumentException if deviceId argument is null or empty
     */
    AmqpsDeviceTwin(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSDEVICETWIN_12_001: [The constructor shall throw IllegalArgumentException if the deviceClientConfig argument is null or empty.]
        if (deviceClientConfig == null)
        {
            throw new IllegalArgumentException("The deviceClientConfig cannot be null or empty.");
        }

        this.deviceClientConfig = deviceClientConfig;

        // Codes_SRS_AMQPSDEVICETWIN_12_002: [The constructor shall set the sender and receiver endpoint path to IoTHub specific values.]
        this.senderLinkEndpointPath = SENDER_LINK_ENDPOINT_PATH;
        this.receiverLinkEndpointPath = RECEIVER_LINK_ENDPOINT_PATH;

        // Codes_SRS_AMQPSDEVICETWIN_12_003: [The constructor shall concatenate a sender specific prefix to the sender link tag's current value.]
        this.senderLinkTag = SENDER_LINK_TAG_PREFIX + this.deviceClientConfig.getDeviceId() + "-" + senderLinkTag;
        // Codes_SRS_AMQPSDEVICETWIN_12_004: [The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.]
        this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + this.deviceClientConfig.getDeviceId() + "-" + receiverLinkTag;

        // Codes_SRS_AMQPSDEVICETWIN_12_005: [The constructor shall insert the given deviceId argument to the sender and receiver link address.]
        this.senderLinkAddress = String.format(senderLinkEndpointPath, this.deviceClientConfig.getDeviceId());
        this.receiverLinkAddress = String.format(receiverLinkEndpointPath, this.deviceClientConfig.getDeviceId());

        // Codes_SRS_AMQPSDEVICETWIN_12_006: [The constructor shall add the API version key to the amqpProperties.]
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), API_VERSION_VALUE);
        // Codes_SRS_AMQPSDEVICETWIN_12_007: [The constructor shall generate a UUID amd add it as a correlation ID to the amqpProperties.]
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX +  UUID.randomUUID().toString()));

        // Codes_SRS_AMQPSDEVICETWIN_12_009: [The constructor shall create a HashMap for correlationId list.]
        this.correlationIdList = new HashMap<>();
    }

    /**
     * Identify if the given link is owned by the operation
     *
     * @return true if the link is owned by the operation, false otherwise
     */
    @Override
    protected Boolean isLinkFound(String linkName)
    {
        // Codes_SRS_AMQPSDEVICETWIN_12_046: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getSenderLinkTag()))
        {
            this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.OPENED;
            return true;
        }

        // Codes_SRS_AMQPSDEVICETWIN_12_047: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getReceiverLinkTag()))
        {
            this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.OPENED;
            return true;
        }

        // Codes_SRS_AMQPSDEVICETWIN_12_048: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
        return false;
    }

    /**
     * Sends the given message and returns with the delivery hash if the message type is twin
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
    protected AmqpsSendReturnValue sendMessageAndGetDeliveryHash(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
    {
        if (messageType == MessageType.DEVICE_TWIN)
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_010: [The function shall call the super function if the MessageType is DEVICE_TWIN, and return with it's return value.]
            return super.sendMessageAndGetDeliveryHash(messageType, msgData, offset, length, deliveryTag);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_011: [The function shall return with AmqpsSendReturnValue with false success and -1 delivery hash.]
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    /**
     * Read the message from Proton if the link name matches
     * Set the message type to twin
     *
     * @param linkName The receiver link's name to read from
     * @return the received message
     * @throws IllegalArgumentException if linkName argument is empty
     * @throws IOException if Proton throws
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException
    {
        // Codes_SRS_AMQPSDEVICETWIN_12_012: [The function shall call the super function.]
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(linkName);
        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_013: [The function shall set the MessageType to DEVICE_TWIN if the super function returned not null.]
            amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TWIN);
            amqpsMessage.setDeviceClientConfig(this.deviceClientConfig);
        }

        // Codes_SRS_AMQPSDEVICETWIN_12_014: [The function shall return the super function return value.]
        return amqpsMessage;
    }

    /**
     * Convert Proton message to IoTHubMessage if the message type is twin
     *
     * @param amqpsMessage The Proton message to convert
     * @param deviceClientConfig The device client configuration
     * @return the converted message
     */
    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException
    {
        if ((amqpsMessage.getAmqpsMessageType() == MessageType.DEVICE_TWIN) &&
            (this.deviceClientConfig.getDeviceId() == deviceClientConfig.getDeviceId()))
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_016: [The function shall convert the amqpsMessage to IoTHubTransportMessage.]
            Message message = protonMessageToIoTHubMessage(amqpsMessage);
            message.setIotHubConnectionString(this.deviceClientConfig.getIotHubConnectionString());

            MessageCallback messageCallback = deviceClientConfig.getDeviceTwinMessageCallback();
            Object messageContext = deviceClientConfig.getDeviceTwinMessageContext();

            // Codes_SRS_AMQPSDEVICETWIN_12_027: [The function shall create a AmqpsConvertFromProtonReturnValue and set the message field to the new IotHubTransportMessage.]
            // Codes_SRS_AMQPSDEVICETWIN_12_028: [The function shall create a AmqpsConvertFromProtonReturnValue and copy the DeviceClientConfig callback and context to it.]
            return new AmqpsConvertFromProtonReturnValue(message, messageCallback, messageContext);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_015: [The function shall return null if the message type is not DEVICE_TWIN.]
            return null;
        }
    }

    /**
     * Convert IoTHubMessage to Proton message
     * Set the message type to twin
     *
     * @param message The IoTHubMessage to convert
     * @return the converted message
     */
    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException
    {
        if (message.getMessageType() == MessageType.DEVICE_TWIN)
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_030: [The function shall convert the IoTHubTransportMessage to a proton message.]
            MessageImpl protonMessage = iotHubMessageToProtonMessage(message);

            // Codes_SRS_AMQPSDEVICETWIN_12_041: [The function shall create a AmqpsConvertToProtonReturnValue and set the message field to the new proton message.]
            // Codes_SRS_AMQPSDEVICETWIN_12_042: [The function shall create a AmqpsConvertToProtonReturnValue and set the message type to DEVICE_TWIN.]
            return new AmqpsConvertToProtonReturnValue(protonMessage, MessageType.DEVICE_TWIN);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_029: [*The function shall return null if the message type is not DEVICE_TWIN.]
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
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg) throws IOException
    {
        byte[] msgBody;

        Data d = (Data) protonMsg.getBody();
        if (d != null)
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_018: [The function shall shall create a new buffer for message body and copy the proton message body to it.]
            Binary b = d.getValue();
            msgBody = new byte[b.getLength()];
            ByteBuffer buffer = b.asByteBuffer();
            buffer.get(msgBody);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_017: [The function shall create a new empty buffer for message body if the proton message body is null.]
            msgBody = new byte[0];
        }

        // Codes_SRS_AMQPSDEVICETWIN_12_019: [The function shall create a new IotHubTransportMessage using the Proton message body and set the message type to DEVICE_TWIN.]
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(msgBody, MessageType.DEVICE_TWIN);
        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);

        MessageAnnotations messageAnnotations = protonMsg.getMessageAnnotations();
        if (messageAnnotations != null)
        {
            for (Map.Entry<Symbol, Object> entry : messageAnnotations.getValue().entrySet())
            {
                Symbol key = entry.getKey();
                Object value = entry.getValue();

                // Codes_SRS_AMQPSDEVICETWIN_12_020: [The function shall read the proton message annotations and set the status to the value of STATUS key.]
                if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_STATUS))
                {
                    iotHubTransportMessage.setStatus(value.toString());
                }
                // Codes_SRS_AMQPSDEVICETWIN_12_021: [The function shall read the proton message annotations and set the version to the value of VERSION key.]
                else if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_VERSION))
                {
                    iotHubTransportMessage.setVersion(value.toString());
                }
                // Codes_SRS_AMQPSDEVICETWIN_12_022: [The function shall read the proton message annotations and set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the PROPERTIES_DESIRED resource exist.]
                else if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE) && value.toString().equals(MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED))
                {
                    iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                }
            }
        }

        Properties properties = protonMsg.getProperties();
        if (properties != null)
        {
            if (properties.getCorrelationId() != null)
            {
                // Codes_SRS_AMQPSDEVICETWIN_12_044: [The function shall set the IotHubTransportMessage correlationID to the proton correlationId.]
                iotHubTransportMessage.setCorrelationId(properties.getCorrelationId().toString());

                // Codes_SRS_AMQPSDEVICETWIN_12_023: [The function shall find the proton correlation ID in the correlationIdList and if it is found, set the operation type to the related response.]
                if (correlationIdList.containsKey(properties.getCorrelationId().toString()))
                {
                    DeviceOperations deviceOperations = correlationIdList.get(properties.getCorrelationId().toString());
                    switch (deviceOperations)
                    {
                        case DEVICE_OPERATION_TWIN_GET_REQUEST:
                            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_GET_RESPONSE);
                            break;
                        case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
                            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_RESPONSE);
                            break;
                        case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                            break;
                        case DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                            break;
                        default:
                            throw new IOException("Invalid device operation type in protonMessageToIoTHubMessage!");
                    }
                    // Codes_SRS_AMQPSDEVICETWIN_12_043: [The function shall remove the correlation from the correlationId list.]
                    this.correlationIdList.remove(properties.getCorrelationId().toString());
                }
            }
            else
            {
                // Codes_SRS_AMQPSDEVICETWIN_12_024: [THe function shall set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the proton correlation ID is null.]
                if (iotHubTransportMessage.getDeviceOperationType() == DEVICE_OPERATION_UNKNOWN)
                {
                    iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                }
            }

            // Codes_SRS_AMQPSDEVICETWIN_12_025: [The function shall copy the correlationId, messageId, To and userId properties to the IotHubTransportMessage properties.]
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

        // Codes_SRS_AMQPSDEVICETWIN_12_026: [The function shall copy the Proton application properties to IotHubTransportMessage properties excluding the reserved property names.]
        if (protonMsg.getApplicationProperties() != null)
        {
            Map<String, Object> applicationProperties = protonMsg.getApplicationProperties().getValue();
            for (Map.Entry<String, Object> entry : applicationProperties.entrySet())
            {
                String propertyKey = entry.getKey();
                if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(propertyKey))
                {
                    iotHubTransportMessage.setProperty(entry.getKey(), entry.getValue().toString());
                }
            }
        }

        return iotHubTransportMessage;
    }

    /**
     * Creates a proton message from the IoTHub message.
     * @param message the IoTHub input message.
     * @return the proton message.
     */
    @Override
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message) throws IOException
    {
        IotHubTransportMessage deviceTwinMessage = (IotHubTransportMessage)message;

        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        // Codes_SRS_AMQPSDEVICETWIN_12_031: [The function shall copy the correlationId, messageId properties to the Proton message properties.]
        Properties properties = new Properties();
        if (deviceTwinMessage.getMessageId() != null)
        {
            properties.setMessageId(deviceTwinMessage.getMessageId());
        }

        if (deviceTwinMessage.getCorrelationId() != null)
        {
            properties.setCorrelationId(UUID.fromString(deviceTwinMessage.getCorrelationId()));
            // Codes_SRS_AMQPSDEVICETWIN_12_045: [The function shall add the correlationId to the correlationIdList if it is not null.]
            this.correlationIdList.put(deviceTwinMessage.getCorrelationId(), deviceTwinMessage.getDeviceOperationType());
        }
        outgoingMessage.setProperties(properties);

        // Codes_SRS_AMQPSDEVICETWIN_12_032: [The function shall copy the user properties to Proton message application properties excluding the reserved property names.]
        int propertiesLength = deviceTwinMessage.getProperties().length;
        Map<String, Object> userProperties = new HashMap<>(propertiesLength);
        if (propertiesLength > 0)
        {
            for(MessageProperty messageProperty : deviceTwinMessage.getProperties())
            {
                if (!MessageProperty.RESERVED_PROPERTY_NAMES.contains(messageProperty.getName()))
                {
                    userProperties.put(messageProperty.getName(), messageProperty.getValue());
                }
            }
        }
        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();
        switch (deviceTwinMessage.getDeviceOperationType())
        {
            case DEVICE_OPERATION_TWIN_GET_REQUEST:
                // Codes_SRS_AMQPSDEVICETWIN_12_033: [The function shall set the proton message annotation operation field to GET if the IotHubTransportMessage operation type is GET_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_GET);
                break;
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
                // Codes_SRS_AMQPSDEVICETWIN_12_034: [The function shall set the proton message annotation operation field to PATCH if the IotHubTransportMessage operation type is UPDATE_REPORTED_PROPERTIES_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_PATCH);
                // Codes_SRS_AMQPSDEVICETWIN_12_035: [The function shall set the proton message annotation resource field to "/properties/reported" if the IotHubTransportMessage operation type is UPDATE_REPORTED_PROPERTIES_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE), MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED);
                break;
            case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                // Codes_SRS_AMQPSDEVICETWIN_12_036: [The function shall set the proton message annotation operation field to PUT if the IotHubTransportMessage operation type is SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_PUT);
                // Codes_SRS_AMQPSDEVICETWIN_12_037: [The function shall set the proton message annotation resource field to "/notifications/twin/properties/desired" if the IotHubTransportMessage operation type is SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE), MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED);
                break;
            case DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                // Codes_SRS_AMQPSDEVICETWIN_12_038: [The function shall set the proton message annotation operation field to DELETE if the IotHubTransportMessage operation type is UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_DELETE);
                // Codes_SRS_AMQPSDEVICETWIN_12_039: [The function shall set the proton message annotation resource field to "/notifications/twin/properties/desired" if the IotHubTransportMessage operation type is UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST.]
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE), MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED);
                break;
            default:
                throw new IOException("Invalid device operation type in iotHubMessageToProtonMessage!");
        }
        MessageAnnotations messageAnnotations = new MessageAnnotations(messageAnnotationsMap);
        outgoingMessage.setMessageAnnotations(messageAnnotations);

        // Codes_SRS_AMQPSDEVICETWIN_12_040: [The function shall set the proton message body using the IotHubTransportMessage body.]
        Binary binary = new Binary(deviceTwinMessage.getBytes());
        Section section = new Data(binary);
        outgoingMessage.setBody(section);
        return outgoingMessage;
    }
}
