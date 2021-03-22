// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
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

public final class AmqpsMethodsSenderLinkHandler extends AmqpsSenderLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";
    private static final String CORRELATION_ID_KEY_PREFIX = "methods:";

    private static final String APPLICATION_PROPERTY_KEY_IOTHUB_STATUS = "IoThub-status";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/methods/devicebound";
    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/methods/devicebound";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_devicemethods-";

    private static final String LINK_TYPE = "methods";

    AmqpsMethodsSenderLinkHandler(Sender sender, AmqpsLinkStateCallback amqpsLinkStateCallback, DeviceClientConfig deviceClientConfig, String linkCorrelationId)
    {
        super(sender, amqpsLinkStateCallback, linkCorrelationId);

        this.senderLinkAddress = getAddress(deviceClientConfig);

        //Note that this correlation id value must be equivalent to the correlation id in the method receiver link that it is paired with
        this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(CORRELATION_ID_KEY_PREFIX + this.linkCorrelationId));
        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());
    }

    static String getTag(DeviceClientConfig deviceClientConfig, String linkCorrelationId)
    {
        String moduleId = deviceClientConfig.getModuleId();
        String deviceId = deviceClientConfig.getDeviceId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            return SENDER_LINK_TAG_PREFIX + deviceId + "/" + moduleId + "-" + linkCorrelationId;
        }
        else
        {
            return SENDER_LINK_TAG_PREFIX + deviceId + "-" + linkCorrelationId;
        }
    }

    private static String getAddress(DeviceClientConfig deviceClientConfig)
    {
        String moduleId = deviceClientConfig.getModuleId();
        String deviceId = deviceClientConfig.getDeviceId();
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
            IotHubTransportMessage deviceMethodMessage = (IotHubTransportMessage) message;

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
