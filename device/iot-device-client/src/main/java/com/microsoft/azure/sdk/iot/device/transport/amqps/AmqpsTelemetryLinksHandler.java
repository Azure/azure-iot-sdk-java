// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_UNKNOWN;

public final class AmqpsTelemetryLinksHandler extends AmqpsLinksHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/messages/events";
    private static final String DEVICE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/messages/devicebound";

    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/messages/events";
    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH_EDGEHUB = "/devices/%s/modules/%s/messages/events";
    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/messages/devicebound";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_telemetry-";
    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_telemetry-";

    private static final String INPUT_NAME_PROPERTY_KEY = "x-opt-input-name";

    private DeviceClientConfig deviceClientConfig;

    /**
     * This constructor creates an instance of AmqpsTelemetryLinksHandler class and initializes member variables
     *
     * @param deviceClientConfig The configuration settings for an IoT Hub client
     * @throws IllegalArgumentException if the deviceClientConfig argument is null
     */
    AmqpsTelemetryLinksHandler(DeviceClientConfig deviceClientConfig)
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
            this.receiverLinkAddress = String.format(deviceClientConfig.getGatewayHostname() != null ? MODULE_RECEIVER_LINK_ENDPOINT_PATH_EDGEHUB : MODULE_RECEIVER_LINK_ENDPOINT_PATH, deviceId, moduleId);
        }
        else
        {
            this.senderLinkTag = SENDER_LINK_TAG_PREFIX + deviceId + "-" + this.linkCorrelationId;
            this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + deviceId + "-" + this.linkCorrelationId;

            this.senderLinkAddress = String.format(DEVICE_SENDER_LINK_ENDPOINT_PATH, deviceId);
            this.receiverLinkAddress = String.format(DEVICE_RECEIVER_LINK_ENDPOINT_PATH, deviceId);
        }

        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());

        if (moduleId != null && !moduleId.isEmpty())
        {
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(this.deviceClientConfig.getDeviceId() + "/" + moduleId));
        }
        else
        {
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(this.deviceClientConfig.getDeviceId()));
        }
    }

    /**
     * Sends the given message and returns with the delivery hash if the message type is telemetry
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
        if (messageType == MessageType.DEVICE_TELEMETRY)
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_007: [The function shall call the super function with the arguments and return with it's return value.]
            return super.sendMessageAndGetDeliveryTag(messageType, msgData, offset, length, deliveryTag);
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_006: [The function shall return an AmqpsSendReturnValue object with false and -1 if the message type is not DEVICE_TELEMETRY.]
            return new AmqpsSendReturnValue(false, -1);
        }
    }

    @Override
    public String getLinkInstanceType()
    {
        return "telemetry";
    }

    /**
     * Read the message from Proton if the link name matches
     * Set the message type to telemetry
     *
     * @param linkName The receiver link's name to read from
     * @return the received message
     * @throws IllegalArgumentException if linkName argument is empty
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName)
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_020: [The function shall call the super function.]
        AmqpsMessage amqpsMessage = super.getMessageFromReceiverLink(linkName);
        if (amqpsMessage != null)
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_021: [The function shall set the MessageType to DEVICE_TELEMETRY if the super function returned not null.]
            amqpsMessage.setAmqpsMessageType(MessageType.DEVICE_TELEMETRY);
            amqpsMessage.setDeviceClientConfig(this.deviceClientConfig);
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_022: [The function shall return the super function return value.]
        return amqpsMessage;
    }

    /**
     * Converts an AMQPS message to a corresponding IoT Hub message.
     *
     * @param protonMsg the AMQPS message.
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(AmqpsMessage protonMsg, DeviceClientConfig deviceClientConfig)
    {
        if (((protonMsg.getAmqpsMessageType() == null) || (protonMsg.getAmqpsMessageType() == MessageType.DEVICE_TELEMETRY)) &&
                (this.deviceClientConfig.getDeviceId().equals(deviceClientConfig.getDeviceId())))
        {
            IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg, deviceClientConfig);
            iotHubTransportMessage.setMessageType(MessageType.DEVICE_TELEMETRY);
            iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);

            if (protonMsg.getMessageAnnotations() != null && protonMsg.getMessageAnnotations().getValue() != null)
            {
                Map<Symbol, Object> applicationProperties = protonMsg.getMessageAnnotations().getValue();
                for (Map.Entry<Symbol, Object> entry : applicationProperties.entrySet())
                {
                    String propertyKey = entry.getKey().toString();
                    if (propertyKey.equals(INPUT_NAME_PROPERTY_KEY))
                    {
                        iotHubTransportMessage.setInputName(entry.getValue().toString());
                    }
                }
            }

            //inputName may be null, and if it is, then the default callback and default callback context will be used from config
            String inputName = iotHubTransportMessage.getInputName();
            MessageCallback messageCallback = deviceClientConfig.getDeviceTelemetryMessageCallback(inputName);
            Object messageContext = deviceClientConfig.getDeviceTelemetryMessageContext(inputName);

            iotHubTransportMessage.setMessageCallback(messageCallback);
            iotHubTransportMessage.setMessageCallbackContext(messageContext);

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
        if ((message.getMessageType() == null) || (message.getMessageType() == MessageType.DEVICE_TELEMETRY))
        {
            MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);

            if (message.getOutputName() != null)
            {
                if (protonMessage.getApplicationProperties() != null && protonMessage.getApplicationProperties().getValue() != null)
                {
                    Map<String, Object> userProperties = new HashMap<>();
                    userProperties.put(MessageProperty.OUTPUT_NAME_PROPERTY, message.getOutputName());
                    userProperties.putAll(protonMessage.getApplicationProperties().getValue());
                    protonMessage.setApplicationProperties(new ApplicationProperties(userProperties));
                }
            }

            return protonMessage;
        }

        return null;
    }
}
