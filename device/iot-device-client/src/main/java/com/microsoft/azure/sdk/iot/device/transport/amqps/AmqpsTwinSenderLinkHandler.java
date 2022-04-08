// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.ClientConfiguration;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
final class AmqpsTwinSenderLinkHandler extends AmqpsSenderLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "twin:";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/twin";

    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/twin";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_devicetwin-";

    private static final String MESSAGE_ANNOTATION_FIELD_KEY_OPERATION = "operation";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE = "resource";
    private static final String MESSAGE_ANNOTATION_FIELD_KEY_VERSION = "version";

    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_GET = "GET";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PATCH = "PATCH";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PUT = "PUT";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_DELETE = "DELETE";

    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED = "/properties/reported";
    private static final String MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED = "/notifications/twin/properties/desired";

    private static final String LINK_TYPE = "twin";

    //This map is shared between the twin sender and receiver links. Twin sender links attach a correlation Id to each
    // request, and the receiver link needs to know those correlation Ids so that it can tell what type of twin
    // response each message received is.
    private final Map<String, DeviceOperations> twinOperationCorrelationMap;

    AmqpsTwinSenderLinkHandler(Sender sender, AmqpsLinkStateCallback amqpsLinkStateCallback, ClientConfiguration clientConfiguration, String linkCorrelationId, Map<String, DeviceOperations> twinOperationCorrelationMap)
    {
        super(sender, amqpsLinkStateCallback, linkCorrelationId, clientConfiguration.getModelId());

        this.senderLinkAddress = getAddress(clientConfiguration);

        //Note that this correlation id value must be equivalent to the correlation id in the twin receiver link that it is paired with
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX + this.linkCorrelationId));
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), clientConfiguration.getProductInfo().getUserAgentString());

        this.twinOperationCorrelationMap = twinOperationCorrelationMap;
    }

    /**
     * Creates a proton message for subscribing to twin desired properties
     *
     * @return The constructed proton message
     */
    private static MessageImpl buildSubscribeToDesiredPropertiesProtonMessage()
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

    private static void setMessageAnnotationMapOnProtonMessage(
            MessageImpl protonMessage,
            DeviceOperations deviceOperationType,
            Integer deviceTwinMessageVersion)
    {
        Map<Symbol, Object> messageAnnotationsMap = new HashMap<>();
        switch (deviceOperationType)
        {
            case DEVICE_OPERATION_TWIN_GET_REQUEST:
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_GET);
                break;
            case DEVICE_OPERATION_TWIN_UPDATE_REPORTED_PROPERTIES_REQUEST:
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_PATCH);
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE), MESSAGE_ANNOTATION_FIELD_VALUE_PROPERTIES_REPORTED);
                if (deviceTwinMessageVersion != null)
                {
                    messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_VERSION), deviceTwinMessageVersion);
                }
                break;
            case DEVICE_OPERATION_TWIN_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_PUT);
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_RESOURCE), MESSAGE_ANNOTATION_FIELD_VALUE_NOTIFICATIONS_TWIN_PROPERTIES_DESIRED);
                break;
            case DEVICE_OPERATION_TWIN_UNSUBSCRIBE_DESIRED_PROPERTIES_REQUEST:
                messageAnnotationsMap.put(Symbol.valueOf(MESSAGE_ANNOTATION_FIELD_KEY_OPERATION), MESSAGE_ANNOTATION_FIELD_VALUE_DELETE);
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

    static String getTag(ClientConfiguration clientConfiguration, String linkCorrelationId)
    {
        String moduleId = clientConfiguration.getModuleId();
        String deviceId = clientConfiguration.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            return SENDER_LINK_TAG_PREFIX + deviceId + "/" + moduleId + "-" + linkCorrelationId;
        }
        else
        {
            return SENDER_LINK_TAG_PREFIX + deviceId + "-" + linkCorrelationId;
        }
    }

    private static String getAddress(ClientConfiguration clientConfiguration)
    {
        String moduleId = clientConfiguration.getModuleId();
        String deviceId = clientConfiguration.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            return String.format(MODULE_SENDER_LINK_ENDPOINT_PATH, deviceId, moduleId);
        }
        else
        {
            return String.format(DEVICE_SENDER_LINK_ENDPOINT_PATH, deviceId);
        }
    }

    @Override
    public String getLinkInstanceType()
    {
        return LINK_TYPE;
    }

    @Override
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message)
    {
        if (message.getMessageType() == MessageType.DEVICE_TWIN)
        {
            MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);
            IotHubTransportMessage deviceTwinMessage = (IotHubTransportMessage) message;

            if (deviceTwinMessage.getCorrelationId() != null)
            {
                protonMessage.getProperties().setCorrelationId(UUID.fromString(deviceTwinMessage.getCorrelationId()));
                this.twinOperationCorrelationMap.put(deviceTwinMessage.getCorrelationId(), deviceTwinMessage.getDeviceOperationType());
            }

            setMessageAnnotationMapOnProtonMessage(protonMessage, deviceTwinMessage.getDeviceOperationType(), deviceTwinMessage.getVersion());

            return protonMessage;
        }

        return null;
    }

    public int sendDesiredPropertiesSubscriptionMessage()
    {
        log.debug("Sending desired properties subscription message");
        MessageImpl desiredPropertiesSubscriptionMessage = buildSubscribeToDesiredPropertiesProtonMessage();
        AmqpsSendResult sendResult = this.sendMessageAndGetDeliveryTag(desiredPropertiesSubscriptionMessage);

        //This message will be ignored when this send is acknowledged, so just provide an empty message for the map
        inProgressMessages.put(sendResult.getDeliveryTag(), new Message());

        return sendResult.getDeliveryTag();
    }
}
