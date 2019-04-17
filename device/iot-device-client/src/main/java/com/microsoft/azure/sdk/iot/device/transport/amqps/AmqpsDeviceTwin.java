// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.message.impl.MessageImpl;

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

    private static final String SENDER_LINK_ENDPOINT_PATH_MODULES = "/devices/%s/modules/%s/twin";
    private static final String RECEIVER_LINK_ENDPOINT_PATH_MODULES = "/devices/%s/modules/%s/twin";

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

    private static final String DEFAULT_STATUS_CODE = "200";

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
        // Codes_SRS_AMQPSDEVICETWIN_34_051: [This constructor shall call super with the provided user agent string.]
        super(deviceClientConfig, SENDER_LINK_ENDPOINT_PATH, RECEIVER_LINK_ENDPOINT_PATH,
                SENDER_LINK_ENDPOINT_PATH_MODULES, RECEIVER_LINK_ENDPOINT_PATH_MODULES,
                SENDER_LINK_TAG_PREFIX, RECEIVER_LINK_TAG_PREFIX);

        this.deviceClientConfig = deviceClientConfig;

        
        // Codes_SRS_AMQPSDEVICETWIN_12_006: [The constructor shall add the API version key to the amqpProperties.]
        this.amqpProperties.put(Symbol.getSymbol(API_VERSION_KEY), TransportUtils.IOTHUB_API_VERSION);
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
    protected synchronized AmqpsSendReturnValue sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException
    {
        if (messageType == MessageType.DEVICE_TWIN)
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_010: [The function shall call the super function if the MessageType is DEVICE_TWIN, and return with it's return value.]
            return super.sendMessageAndGetDeliveryTag(messageType, msgData, offset, length, deliveryTag);
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
     * @throws TransportException if Proton throws
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, TransportException
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
     * @throws TransportException if the conversion fails
     * @return the converted message
     */
    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        if ((amqpsMessage.getAmqpsMessageType() == MessageType.DEVICE_TWIN) &&
            (this.deviceClientConfig.getDeviceId().equals(deviceClientConfig.getDeviceId())))
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_016: [The function shall convert the amqpsMessage to IoTHubTransportMessage.]
            Message message = protonMessageToIoTHubMessage(amqpsMessage);

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
     * @throws TransportException if the conversion fails
     * @return the converted message
     */
    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException
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
     * @throws TransportException if the conversion fails
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(MessageImpl protonMsg) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICETWIN_12_019: [The function shall create a new IotHubTransportMessage using the Proton message body and set the message type to DEVICE_TWIN.]
        IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg);
        iotHubTransportMessage.setMessageType(MessageType.DEVICE_TWIN);
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
        if (properties != null && properties.getCorrelationId() != null)
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
                        TransportUtils.throwTransportExceptionWithIotHubServiceType(
                                "Invalid device operation type in protonMessageToIoTHubMessage!",
                                TransportException.IotHubService.TWIN);
                }
                // Codes_SRS_AMQPSDEVICETWIN_12_043: [The function shall remove the correlation from the correlationId list.]
                this.correlationIdList.remove(properties.getCorrelationId().toString());
            }
        }
        else if (iotHubTransportMessage.getDeviceOperationType() == DEVICE_OPERATION_UNKNOWN)
        {
            // Codes_SRS_AMQPSDEVICETWIN_12_024: [The function shall set the operation type to SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE if the proton correlation ID is not present.]
            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

            if (iotHubTransportMessage.getStatus() == null || iotHubTransportMessage.getStatus().isEmpty())
            {
                iotHubTransportMessage.setStatus(DEFAULT_STATUS_CODE);
            }
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
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message) throws TransportException
    {
        MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);
        IotHubTransportMessage deviceTwinMessage = (IotHubTransportMessage)message;

        if (deviceTwinMessage.getCorrelationId() != null)
        {
            protonMessage.getProperties().setCorrelationId(UUID.fromString(deviceTwinMessage.getCorrelationId()));

            // Codes_SRS_AMQPSDEVICETWIN_12_045: [The function shall add the correlationId to the correlationIdList if it is not null.]
            this.correlationIdList.put(deviceTwinMessage.getCorrelationId(), deviceTwinMessage.getDeviceOperationType());
        }

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
                // Codes_SRS_AMQPSDEVICETWIN_21_049: [If the version is provided, the function shall set the proton message annotation resource field to "version" if the message version.]
                if(deviceTwinMessage.getVersion() != null)
                {
                    try
                    {
                        messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_VERSION), Long.parseLong(deviceTwinMessage.getVersion()));
                    }
                    catch (NumberFormatException e)
                    {
                        // Codes_SRS_AMQPSDEVICETWIN_21_050: [If the provided version is not `Long`, the function shall throw IOException.]
                        TransportUtils.throwTransportExceptionWithIotHubServiceType(e, TransportException.IotHubService.TWIN);
                    }
                }
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
                TransportUtils.throwTransportExceptionWithIotHubServiceType(
                        "Invalid device operation type in iotHubMessageToProtonMessage!",
                        TransportException.IotHubService.TWIN);
        }

        if (protonMessage.getMessageAnnotations() != null && protonMessage.getMessageAnnotations().getValue() != null)
        {
            messageAnnotationsMap.putAll(protonMessage.getMessageAnnotations().getValue());
        }

        MessageAnnotations messageAnnotations = new MessageAnnotations(messageAnnotationsMap);
        protonMessage.setMessageAnnotations(messageAnnotations);

        return protonMessage;
    }
}
