// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.ClientConfiguration;
import com.microsoft.azure.sdk.iot.device.twin.DeviceOperations;
import com.microsoft.azure.sdk.iot.device.MessageCallback;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.engine.Receiver;

import java.util.Map;

final class AmqpsMethodsReceiverLinkHandler extends AmqpsReceiverLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "methods:";

    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_METHOD_NAME = "IoThub-methodname";

    private static final String DEVICE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
    private static final String MODULE_RECEIVER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/methods/devicebound";

    private static final String RECEIVER_LINK_TAG_PREFIX = "receiver_link_devicemethods-";

    private static final String LINK_TYPE = "methods";

    private final ClientConfiguration clientConfiguration;

    AmqpsMethodsReceiverLinkHandler(Receiver receiver, AmqpsLinkStateCallback amqpsLinkStateCallback, ClientConfiguration clientConfiguration, String linkCorrelationId)
    {
        super(receiver, amqpsLinkStateCallback, linkCorrelationId);

        this.clientConfiguration = clientConfiguration;

        this.receiverLinkAddress = getAddress(clientConfiguration);

        //Note that this correlation id value must be equivalent to the correlation id in the method sender link that it is paired with
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX + this.linkCorrelationId));
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), clientConfiguration.getProductInfo().getUserAgentString());
    }

    static String getTag(ClientConfiguration clientConfiguration, String linkCorrelationId)
    {
        String moduleId = clientConfiguration.getModuleId();
        String deviceId = clientConfiguration.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            return RECEIVER_LINK_TAG_PREFIX + deviceId + "/" + moduleId + "-" + linkCorrelationId;
        }
        else
        {
            return RECEIVER_LINK_TAG_PREFIX + deviceId + "-" + linkCorrelationId;
        }
    }

    private static String getAddress(ClientConfiguration clientConfiguration)
    {
        String moduleId = clientConfiguration.getModuleId();
        String deviceId = clientConfiguration.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            return String.format(MODULE_RECEIVER_LINK_ENDPOINT_PATH, deviceId, moduleId);
        }
        else
        {
            return String.format(DEVICE_RECEIVER_LINK_ENDPOINT_PATH, deviceId);
        }
    }

    @Override
    public String getLinkInstanceType()
    {
        return LINK_TYPE;
    }

    @Override
    protected IotHubTransportMessage protonMessageToIoTHubMessage(AmqpsMessage protonMsg)
    {
        IotHubTransportMessage iotHubTransportMessage = super.protonMessageToIoTHubMessage(protonMsg);
        iotHubTransportMessage.setMessageType(MessageType.DEVICE_METHODS);
        iotHubTransportMessage.setDeviceOperationType(DeviceOperations.DEVICE_OPERATION_METHOD_RECEIVE_REQUEST);

        MessageCallback messageCallback = clientConfiguration.getDirectMethodsMessageCallback();
        Object messageContext = clientConfiguration.getDirectMethodsMessageContext();

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
}
