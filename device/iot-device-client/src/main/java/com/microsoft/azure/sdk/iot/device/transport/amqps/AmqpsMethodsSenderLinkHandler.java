// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.ClientConfiguration;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageType;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class AmqpsMethodsSenderLinkHandler extends AmqpsSenderLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "methods:";

    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_STATUS = "IoThub-status";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/methods/devicebound";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_devicemethods-";

    private static final String LINK_TYPE = "methods";

    AmqpsMethodsSenderLinkHandler(Sender sender, AmqpsLinkStateCallback amqpsLinkStateCallback, ClientConfiguration clientConfiguration, String linkCorrelationId)
    {
        super(sender, amqpsLinkStateCallback, linkCorrelationId, clientConfiguration.getModelId());

        this.senderLinkAddress = getAddress(clientConfiguration);

        //Note that this correlation id value must be equivalent to the correlation id in the method receiver link that it is paired with
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX + this.linkCorrelationId));
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), clientConfiguration.getProductInfo().getUserAgentString());
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
    protected MessageImpl iotHubMessageToProtonMessage(Message message)
    {
        if (message.getMessageType() == MessageType.DEVICE_METHODS)
        {
            MessageImpl protonMessage = super.iotHubMessageToProtonMessage(message);
            IotHubTransportMessage directMethodMessage = (IotHubTransportMessage) message;

            Properties properties;
            if (protonMessage.getProperties() != null)
            {
                properties = protonMessage.getProperties();
            }
            else
            {
                properties = new Properties();
            }

            if (directMethodMessage.getRequestId() != null)
            {
                properties.setCorrelationId(UUID.fromString(directMethodMessage.getRequestId()));
            }

            protonMessage.setProperties(properties);

            Map<String, Object> userProperties = new HashMap<>();
            if (directMethodMessage.getStatus() != null)
            {
                userProperties.put(APPLICATION_PROPERTY_KEY_IOTHUB_STATUS, Integer.parseInt(directMethodMessage.getStatus()));
            }

            if (protonMessage.getApplicationProperties() != null && protonMessage.getApplicationProperties().getValue() != null)
            {
                Map<String, Object> applicationPropertiesMap = protonMessage.getApplicationProperties().getValue();
                userProperties.putAll(applicationPropertiesMap);
            }

            ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
            protonMessage.setApplicationProperties(applicationProperties);

            return protonMessage;
        }

        return null;
    }
}
