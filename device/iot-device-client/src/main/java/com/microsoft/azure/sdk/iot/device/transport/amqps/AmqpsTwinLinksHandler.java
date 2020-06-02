// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.*;

@Slf4j
public final class AmqpsTwinLinksHandler extends AmqpsLinksHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "twin:";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/twin";
    private static final String DEVICE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/twin";

    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/twin";
    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/twin";

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
     * This constructor creates an instance of AmqpsTwinLinksHandler class and initializes member variables
     * @throws IllegalArgumentException if deviceId argument is null or empty
     */
    AmqpsTwinLinksHandler(DeviceClientConfig deviceClientConfig)
    {
        super();

        this.deviceClientConfig = deviceClientConfig;

        String moduleId = deviceClientConfig.getModuleId();
        String deviceId = deviceClientConfig.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            this.senderLinkTag = SENDER_LINK_TAG_PREFIX + deviceId + "/" + moduleId + "-" + this.linkCorrelationId;
            this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + deviceId + "/" + moduleId + "-" + this.linkCorrelationId;

            this.senderLinkAddress = String.format(MODULE_SENDER_LINK_ENDPOINT_PATH, deviceId, moduleId);
            this.receiverLinkAddress = String.format(MODULE_RECEIVER_LINK_ENDPOINT_PATH, deviceId, moduleId);
        }
        else
        {
            this.senderLinkTag = SENDER_LINK_TAG_PREFIX + deviceId + "-" + this.linkCorrelationId;
            this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + deviceId + "-" + this.linkCorrelationId;

            this.senderLinkAddress = String.format(DEVICE_SENDER_LINK_ENDPOINT_PATH, deviceId);
            this.receiverLinkAddress = String.format(DEVICE_RECEIVER_LINK_ENDPOINT_PATH, deviceId);
        }

        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX +  UUID.randomUUID().toString()));
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());

        this.correlationIdList = new HashMap<>();
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
    protected synchronized AmqpsSendReturnValue sendMessageAndGetDeliveryTag(MessageType messageType, byte[] msgData, int offset, int length, byte[] deliveryTag)
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

    @Override
    public String getLinkInstanceType()
    {
        return "twin";
    }

    /**
     * Read the message from Proton if the link name matches
     * Set the message type to twin
     *
     * @param linkName The receiver link's name to read from
     * @return the received message
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName)
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
     * Creates a proton message for subscribing to desired properties on DeviceTwins
     *
     * @return The constructed proton message
     */
    protected MessageImpl buildSubscribeToDesiredPropertiesProtonMessage()
    {
        MessageImpl protonMessage = (MessageImpl) Proton.message();

        Properties properties = new Properties();

        properties.setMessageId(UUID.randomUUID());
        properties.setCorrelationId(UUID.randomUUID());

        protonMessage.setProperties(properties);

        setMessageAnnotationMapOnProtonMessage(
                protonMessage,
                DeviceOperations.DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST,
                null
        );

        return protonMessage;
    }

    /**
     * Converts an AMQPS message to a corresponding IoT Hub message.
     *
     * @param protonMsg the AMQPS message.
     * @param deviceClientConfig the config of the device receiving this message
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(AmqpsMessage protonMsg, DeviceClientConfig deviceClientConfig)
    {
        if ((protonMsg.getAmqpsMessageType() == MessageType.DEVICE_TWIN) &&
                (this.deviceClientConfig.getDeviceId().equals(deviceClientConfig.getDeviceId())))
        {
            IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg, deviceClientConfig);

            MessageCallback messageCallback = deviceClientConfig.getDeviceTwinMessageCallback();
            Object messageContext = deviceClientConfig.getDeviceTwinMessageContext();

            iotHubTransportMessage.setMessageCallback(messageCallback);
            iotHubTransportMessage.setMessageCallbackContext(messageContext);

            iotHubTransportMessage.setMessageType(MessageType.DEVICE_TWIN);
            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);

            MessageAnnotations messageAnnotations = protonMsg.getMessageAnnotations();
            if (messageAnnotations != null)
            {
                for (Map.Entry<Symbol, Object> entry : messageAnnotations.getValue().entrySet())
                {
                    Symbol key = entry.getKey();
                    Object value = entry.getValue();

                    if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_STATUS))
                    {
                        iotHubTransportMessage.setStatus(value.toString());
                    }
                    else if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_VERSION))
                    {
                        iotHubTransportMessage.setVersion(value.toString());
                    }
                    else if (key.toString().equals(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE) && value.toString().equals(MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_DESIRED))
                    {
                        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);
                    }
                }
            }

            Properties properties = protonMsg.getProperties();
            if (properties != null && properties.getCorrelationId() != null)
            {
                iotHubTransportMessage.setCorrelationId(properties.getCorrelationId().toString());

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
                            log.error("Unrecognized device operation type during conversion of proton message into an iothub message");
                    }
                    this.correlationIdList.remove(properties.getCorrelationId().toString());
                }
            }
            else if (iotHubTransportMessage.getDeviceOperationType() == DEVICE_OPERATION_UNKNOWN)
            {
                iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE);

                if (iotHubTransportMessage.getStatus() == null || iotHubTransportMessage.getStatus().isEmpty())
                {
                    iotHubTransportMessage.setStatus(DEFAULT_STATUS_CODE);
                }
            }

            return iotHubTransportMessage;
        }

        return null;
    }

    /**
     * Creates a proton message from the IoTHub message.
     * @param message the IoTHub input message.
     * @return the proton message.
     */
    @Override
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        if (message.getMessageType() == MessageType.DEVICE_TWIN)
        {
            MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);
            IotHubTransportMessage deviceTwinMessage = (IotHubTransportMessage)message;

            if (deviceTwinMessage.getCorrelationId() != null)
            {
                protonMessage.getProperties().setCorrelationId(UUID.fromString(deviceTwinMessage.getCorrelationId()));
                this.correlationIdList.put(deviceTwinMessage.getCorrelationId(), deviceTwinMessage.getDeviceOperationType());
            }

            setMessageAnnotationMapOnProtonMessage(protonMessage, deviceTwinMessage.getDeviceOperationType(), deviceTwinMessage.getVersion());

            return protonMessage;
        }

        return null;
    }

    private void setMessageAnnotationMapOnProtonMessage(
            MessageImpl protonMessage,
            DeviceOperations deviceOperationType,
            String deviceTwinMessageVersion)
    {
        Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();
        switch (deviceOperationType)
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
                if(deviceTwinMessageVersion != null)
                {
                    try
                    {
                        messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_VERSION), Long.parseLong(deviceTwinMessageVersion));
                    }
                    catch (NumberFormatException e)
                    {
                        log.error("Failed to convert device twin version into a long, can't add version annotation to message.");
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
                log.error("Unrecognized device operation type during conversion of iothub message into proton message");
        }

        if (protonMessage.getMessageAnnotations() != null && protonMessage.getMessageAnnotations().getValue() != null)
        {
            messageAnnotationsMap.putAll(protonMessage.getMessageAnnotations().getValue());
        }

        MessageAnnotations messageAnnotations = new MessageAnnotations(messageAnnotationsMap);
        protonMessage.setMessageAnnotations(messageAnnotations);
    }
}
