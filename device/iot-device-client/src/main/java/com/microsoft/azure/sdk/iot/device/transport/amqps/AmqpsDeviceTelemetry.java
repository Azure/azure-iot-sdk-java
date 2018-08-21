// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.sdk.iot.device.DeviceTwin.DeviceOperations.DEVICE_OPERATION_UNKNOWN;

public final class AmqpsDeviceTelemetry extends AmqpsDeviceOperations
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private static final String SENDER_LINK_ENDPOINT_PATH = "/devices/%s/messages/events";
    private static final String RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/messages/devicebound";

    private static final String SENDER_LINK_ENDPOINT_PATH_MODULES = "/devices/%s/modules/%s/messages/events";
    private static final String RECEIVER_LINK_ENDPOINT_PATH_MODULES = "/devices/%s/modules/%s/messages/devicebound";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_telemetry-";
    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_telemetry-";

    private DeviceClientConfig deviceClientConfig;

    /**
     * This constructor creates an instance of AmqpsDeviceTelemetry class and initializes member variables
     *
     * @param deviceClientConfig The configuration settings for an IoT Hub client
     * @throws IllegalArgumentException if the deviceClientConfig argument is null
     */
    AmqpsDeviceTelemetry(DeviceClientConfig deviceClientConfig) throws IllegalArgumentException
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_34_050: [This constructor shall call super with the provided user agent string.]
        super(deviceClientConfig, SENDER_LINK_ENDPOINT_PATH, RECEIVER_LINK_ENDPOINT_PATH,
                SENDER_LINK_ENDPOINT_PATH_MODULES, RECEIVER_LINK_ENDPOINT_PATH_MODULES,
                SENDER_LINK_TAG_PREFIX, RECEIVER_LINK_TAG_PREFIX);

        this.deviceClientConfig = deviceClientConfig;

        String moduleId = this.deviceClientConfig.getModuleId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_34_034: [If a moduleId is present, the constructor shall set the sender and receiver endpoint path to IoTHub specific values for module communication.]
            this.senderLinkEndpointPath = SENDER_LINK_ENDPOINT_PATH_MODULES;
            this.receiverLinkEndpointPath = RECEIVER_LINK_ENDPOINT_PATH_MODULES;

            // Codes_SRS_AMQPSDEVICETELEMETRY_34_035: [If a moduleId is present, the constructor shall concatenate a sender specific prefix including the moduleId to the sender link tag's current value.]
            this.senderLinkTag = SENDER_LINK_TAG_PREFIX + this.deviceClientConfig.getDeviceId() + "/" + moduleId + "-" + senderLinkTag;
            this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + this.deviceClientConfig.getDeviceId() + "/" + moduleId + "-" + receiverLinkTag;

            // Codes_SRS_AMQPSDEVICETELEMETRY_34_036: [If a moduleId is present, the constructor shall insert the given deviceId and moduleId argument to the sender and receiver link address.]
            this.senderLinkAddress = String.format(senderLinkEndpointPath, this.deviceClientConfig.getDeviceId(), moduleId);
            this.receiverLinkAddress = String.format(receiverLinkEndpointPath, this.deviceClientConfig.getDeviceId(), moduleId);

            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(this.deviceClientConfig.getDeviceId() + "/" + moduleId));
        }
        else
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_002: [The constructor shall set the sender and receiver endpoint path to IoTHub specific values.]
            this.senderLinkEndpointPath = SENDER_LINK_ENDPOINT_PATH;
            this.receiverLinkEndpointPath = RECEIVER_LINK_ENDPOINT_PATH;

            // Codes_SRS_AMQPSDEVICETELEMETRY_12_003: [The constructor shall concatenate a sender specific prefix to the sender link tag's current value.]
            this.senderLinkTag = SENDER_LINK_TAG_PREFIX + this.deviceClientConfig.getDeviceId() + "-" + senderLinkTag;
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_004: [The constructor shall concatenate a receiver specific prefix to the receiver link tag's current value.]
            this.receiverLinkTag = RECEIVER_LINK_TAG_PREFIX + this.deviceClientConfig.getDeviceId() + "-" + receiverLinkTag;

            // Codes_SRS_AMQPSDEVICETELEMETRY_12_005: [The constructor shall insert the given deviceId argument to the sender and receiver link address.]
            this.senderLinkAddress = String.format(senderLinkEndpointPath, this.deviceClientConfig.getDeviceId());
            this.receiverLinkAddress = String.format(receiverLinkEndpointPath, this.deviceClientConfig.getDeviceId());

            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(this.deviceClientConfig.getDeviceId()));
        }
    }

    /**
     * Identify if the given link is owned by the operation
     *
     * @return true if the link is owned by the operation, false otherwise
     */
    @Override
    protected Boolean isLinkFound(String linkName)
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_026: [The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getSenderLinkTag()))
        {
            this.amqpsSendLinkState = AmqpsDeviceOperationLinkState.OPENED;
            return true;
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_027: [The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.]
        if (linkName.equals(this.getReceiverLinkTag()))
        {
            this.amqpsRecvLinkState = AmqpsDeviceOperationLinkState.OPENED;
            return true;
        }

        // Codes_SRS_AMQPSDEVICETELEMETRY_12_028: [The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.]
        return false;
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

    /**
     * Read the message from Proton if the link name matches
     * Set the message type to telemetry
     *
     * @param linkName The receiver link's name to read from
     * @return the received message
     * @throws IllegalArgumentException if linkName argument is empty
     * @throws TransportException if Proton throws
     */
    @Override
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws TransportException
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
     * Convert Proton message to IoTHubMessage if the message type is telemetry
     *
     * @param amqpsMessage The Proton message to convert
     * @param deviceClientConfig The device client configuration
     * @return the converted message
     */
    @Override
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws TransportException
    {
        if (((amqpsMessage.getAmqpsMessageType() == null) || (amqpsMessage.getAmqpsMessageType() == MessageType.DEVICE_TELEMETRY)) &&
            (this.deviceClientConfig.getDeviceId() == deviceClientConfig.getDeviceId()))
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_009: [The function shall create a new IoTHubMessage using the Proton message body.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_010: [**The function shall copy the correlationId, messageId, To and userId properties to the IotHubMessage properties.]
            // Codes_SRS_AMQPSDEVICETELEMETRY_12_011: [The function shall copy the Proton application properties to IoTHubMessage properties excluding the reserved property names.]
            Message message = protonMessageToIoTHubMessage(amqpsMessage);

            MessageCallback messageCallback = deviceClientConfig.getDeviceTelemetryMessageCallback(message.getInputName());
            Object messageContext = deviceClientConfig.getDeviceTelemetryMessageContext(message.getInputName());

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

    /**
     * Convert IoTHubMessage to Proton message
     * Set the message type to telemetry
     *
     * @param message The IoTHubMessage to convert
     * @return the converted message
     */
    @Override
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws TransportException
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
     * @throws TransportException if the conversion fails
     * @return the corresponding IoT Hub message.
     */
    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(MessageImpl protonMsg) throws TransportException
    {
        // Codes_SRS_AMQPSDEVICETELEMETRY_12_009: [The function shall create a new IoTHubMessage using the Proton message body.]
        IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg);
        iotHubTransportMessage.setMessageType(MessageType.DEVICE_TELEMETRY);
        iotHubTransportMessage.setDeviceOperationType(DEVICE_OPERATION_UNKNOWN);
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

        if (message.getOutputName() != null)
        {
            // Codes_SRS_AMQPSDEVICETELEMETRY_34_051: [This function shall set the message's saved outputname in the application properties of the new proton message.]
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
}
