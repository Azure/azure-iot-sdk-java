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
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class AmqpsMethodsLinksHandler extends AmqpsLinksHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "methods:";

    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME = "IoThub-methodname";
    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_STATUS = "IoThub-status";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
    private static final String DEVICE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";

    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/methods/devicebound";
    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/methods/devicebound";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_devicemethods-";
    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_devicemethods-";

    private DeviceClientConfig deviceClientConfig;

    /**
     * This constructor creates an instance of AmqpsMethodsLinksHandler class and initializes member variables
     */
    AmqpsMethodsLinksHandler(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSDEVICETWIN_34_051: [This constructor shall call super with the provided user agent string.]
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

    @Override
    public String getLinkInstanceType()
    {
        return "methods";
    }

    /**
     * Read the message from Proton if the link name matches
     * Set the message type to methods
     *
     * @param linkName The receiver link's name to read from
     * @return the received message
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName)
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
     * Converts an AMQPS message to a corresponding IoT Hub message.
     *
     * @param protonMsg the AMQPS message.
     * @param deviceClientConfig the config of the device that is reading this message
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(AmqpsMessage protonMsg, DeviceClientConfig deviceClientConfig)
    {
        if ((protonMsg.getAmqpsMessageType() == MessageType.DEVICE_METHODS) &&
                (this.deviceClientConfig.getDeviceId().equals(deviceClientConfig.getDeviceId())))
        {
            IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg, deviceClientConfig);
            iotHubTransportMessage.setMessageType(MessageType.DEVICE_METHODS);
            iotHubTransportMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

            MessageCallback messageCallback = deviceClientConfig.getDeviceMethodsMessageCallback();
            Object messageContext = deviceClientConfig.getDeviceMethodsMessageContext();

            iotHubTransportMessage.setMessageCallback(messageCallback);
            iotHubTransportMessage.setMessageCallbackContext(messageContext);

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

        return null;
    }

    /**
     * Creates a proton message from the IoTHub message.
     * @param message the IoTHub input message.
     * @return the proton message.
     */
    @Override
    protected MessageImpl iotHubMessageToProtonMessage(Message message)
    {
        if (message.getMessageType() == MessageType.DEVICE_METHODS)
        {
            MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);
            IotHubTransportMessage deviceMethodMessage = (IotHubTransportMessage)message;

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

            Map<String, Object> userProperties = new HashMap<>();
            if (deviceMethodMessage.getStatus() != null)
            {
                userProperties.put(APPLICATION_PROPERTY_KEY_IOTHUB_STATUS, Integer.parseInt(deviceMethodMessage.getStatus()));
            }

            Map<String, Object> applicationPropertiesMap;
            if (protonMessage.getApplicationProperties() != null && protonMessage.getApplicationProperties().getValue() != null)
            {
                applicationPropertiesMap = protonMessage.getApplicationProperties().getValue();
                userProperties.putAll(applicationPropertiesMap);
            }

            ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
            protonMessage.setApplicationProperties(applicationProperties);

            return protonMessage;
        }

        return null;
    }
}
