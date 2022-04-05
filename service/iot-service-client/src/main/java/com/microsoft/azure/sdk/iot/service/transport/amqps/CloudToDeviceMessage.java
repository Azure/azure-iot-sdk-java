// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.messaging.SendResult;
import lombok.Getter;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

class CloudToDeviceMessage
{
    private static final String DEVICE_PATH_FORMAT = "/devices/%s/messages/devicebound";
    private static final String MODULE_PATH_FORMAT = "/devices/%s/modules/%s/messages/devicebound";

    @Getter
    final Message iotHubMessage;

    @Getter
    final String targetDeviceId;

    @Getter
    final String targetModuleId;

    @Getter
    final Consumer<SendResult> onMessageSentCallback;

    @Getter
    final Object onMessageSentCallbackContext;

    @Getter
    final org.apache.qpid.proton.message.Message protonMessage;

    CloudToDeviceMessage(String targetDeviceId, String targetModuleId, Message iotHubMessage, Consumer<SendResult> onMessageSentCallback, Object onMessageSentCallbackContext)
    {
        this.targetDeviceId = targetDeviceId;
        this.targetModuleId = targetModuleId; // may be null
        this.iotHubMessage = iotHubMessage;
        this.onMessageSentCallback = onMessageSentCallback;
        this.onMessageSentCallbackContext = onMessageSentCallbackContext;

        if (this.targetModuleId != null)
        {
            this.protonMessage = createProtonMessage(this.targetDeviceId, this.targetModuleId, this.iotHubMessage);
        }
        else
        {
            this.protonMessage = createProtonMessage(this.targetDeviceId, this.iotHubMessage);
        }
    }

    String getCorrelationId()
    {
        return this.getIotHubMessage().getCorrelationId();
    }

    private static org.apache.qpid.proton.message.Message createProtonMessage(String deviceId, Message message)
    {
        return populateProtonMessage(String.format(DEVICE_PATH_FORMAT, deviceId), message);
    }

    private static org.apache.qpid.proton.message.Message createProtonMessage(String deviceId, String moduleId, Message message)
    {
        return populateProtonMessage(String.format(MODULE_PATH_FORMAT, deviceId, moduleId), message);
    }

    private static org.apache.qpid.proton.message.Message populateProtonMessage(String targetPath, Message message)
    {
        org.apache.qpid.proton.message.Message protonMessage = Proton.message();

        Properties properties = new Properties();
        properties.setMessageId(message.getMessageId());
        properties.setTo(targetPath);
        properties.setCorrelationId(message.getCorrelationId());
        if (message.getUserId() != null)
        {
            properties.setUserId(new Binary(message.getUserId().getBytes(StandardCharsets.UTF_8)));
        }

        protonMessage.setProperties(properties);

        if (message.getProperties() != null && message.getProperties().size() > 0)
        {
            Map<String, Object> applicationPropertiesMap = new HashMap<>(message.getProperties().size());
            applicationPropertiesMap.putAll(message.getProperties());

            ApplicationProperties applicationProperties = new ApplicationProperties(applicationPropertiesMap);
            protonMessage.setApplicationProperties(applicationProperties);
        }

        Binary binary;
        //Messages may have no payload, so check that the message has a payload before giving message.getBytes(StandardCharsets.UTF_8) as the payload
        if (message.getBytes() != null)
        {
            binary = new Binary(message.getBytes());
        }
        else
        {
            binary = new Binary(new byte[0]);
        }

        Section section = new Data(binary);
        protonMessage.setBody(section);
        return protonMessage;
    }
}
