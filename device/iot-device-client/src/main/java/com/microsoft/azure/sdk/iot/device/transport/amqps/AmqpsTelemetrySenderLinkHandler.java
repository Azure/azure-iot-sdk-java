// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import com.microsoft.azure.sdk.iot.device.MessageType;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.HashMap;
import java.util.Map;

final class AmqpsTelemetrySenderLinkHandler extends AmqpsSenderLinkHandler
{
    private static final String CORRELATION_ID_KEY = "com.microsoft:channel-correlation-id";

    private static final String DEVICE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/messages/events";
    private static final String MODULE_SENDER_LINK_ENDPOINT_PATH = "/devices/%s/modules/%s/messages/events";

    private static final String SENDER_LINK_TAG_PREFIX = "sender_link_telemetry-";

    private static final String LINK_TYPE = "telemetry";

    AmqpsTelemetrySenderLinkHandler(Sender sender, AmqpsLinkStateCallback amqpsLinkStateCallback, DeviceClientConfig deviceClientConfig, String linkCorrelationId)
    {
        super(sender, amqpsLinkStateCallback, linkCorrelationId);

        this.senderLinkAddress = getAddress(deviceClientConfig);

        this.amqpProperties.put(Symbol.getSymbol(VERSION_IDENTIFIER_KEY), deviceClientConfig.getProductInfo().getUserAgentString());

        String deviceId = deviceClientConfig.getDeviceId();
        String moduleId = deviceClientConfig.getModuleId();
        if (moduleId != null && !moduleId.isEmpty())
        {
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(deviceId + "/" + moduleId));
        }
        else
        {
            this.amqpProperties.put(Symbol.getSymbol(CORRELATION_ID_KEY), Symbol.getSymbol(deviceId));
        }
    }

    static String getTag(DeviceClientConfig clientConfig, String linkCorrelationId)
    {
        String moduleId = clientConfig.getModuleId();
        String deviceId = clientConfig.getDeviceId();
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
