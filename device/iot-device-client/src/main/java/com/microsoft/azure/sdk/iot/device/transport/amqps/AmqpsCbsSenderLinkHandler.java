// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every SAS token based authentication over AMQP requires a CBS session with a sender and receiver link. This
 * class defines the sender link which proactively sends renewed sas tokens to keep the device sessions authenticated.
 */
@Slf4j
public final class AmqpsCbsSenderLinkHandler extends AmqpsSenderLinkHandler
{
    private static final String SENDER_LINK_ENDPOINT_PATH = "$cbs";

    private static final String SENDER_LINK_TAG_PREFIX = "cbs-sender";

    private static final String CBS_TO = "$cbs";
    private static final String CBS_REPLY = "cbs";
    private static final String LINK_TYPE = "cbs";

    private static final String OPERATION_KEY = "operation";
    private static final String TYPE_KEY = "type";
    private static final String NAME_KEY = "name";

    private static final String OPERATION_VALUE = "put-token";
    private static final String TYPE_VALUE = "servicebus.windows.net:sastoken";

    private static final String DEVICES_PATH = "/devices/";

    AmqpsCbsSenderLinkHandler(Sender sender, AmqpsLinkStateCallback amqpsLinkStateCallback)
    {
        super(sender, amqpsLinkStateCallback, UUID.randomUUID().toString());

        this.senderLinkTag = SENDER_LINK_TAG_PREFIX;
        this.senderLinkAddress = SENDER_LINK_ENDPOINT_PATH;
    }

    static String getCbsTag()
    {
        return SENDER_LINK_TAG_PREFIX;
    }

    @Override
    public String getLinkInstanceType()
    {
        return LINK_TYPE;
    }

    UUID sendAuthenticationMessage(DeviceClientConfig deviceClientConfig) throws TransportException
    {
        UUID correlationId = UUID.randomUUID();
        MessageImpl outgoingMessage = createCBSAuthenticationMessage(deviceClientConfig, correlationId);

        AmqpsSendResult sendResult = this.sendMessageAndGetDeliveryTag(outgoingMessage);

        //This message will be ignored when this send is acknowledged, so just provide an empty message for the map
        inProgressMessages.put(sendResult.getDeliveryTag(), new Message());

        return correlationId;
    }

    // The warning is for how getSasTokenAuthentication() may return null, but this code only executes when our config
    // uses SAS_TOKEN auth, and that is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    @SuppressWarnings("ConstantConditions")
    private MessageImpl createCBSAuthenticationMessage(DeviceClientConfig deviceClientConfig, UUID correlationId) throws TransportException
    {
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        Properties properties = new Properties();

        // Note that setting "messageId = correlationId" is intentional.
        // IotHub only responds correctly if this correlation id is set this way
        properties.setMessageId(correlationId);

        properties.setTo(CBS_TO);
        properties.setReplyTo(CBS_REPLY);
        outgoingMessage.setProperties(properties);

        Map<String, Object> userProperties = new HashMap<>(3);
        userProperties.put(OPERATION_KEY, OPERATION_VALUE);
        userProperties.put(TYPE_KEY, TYPE_VALUE);

        String host = deviceClientConfig.getGatewayHostname();
        if (host == null || host.isEmpty())
        {
            host = deviceClientConfig.getIotHubHostname();
        }

        userProperties.put(NAME_KEY, host + DEVICES_PATH + deviceClientConfig.getDeviceId());
        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        Section section;
        try
        {
            section = new AmqpValue(String.valueOf(deviceClientConfig.getSasTokenAuthentication().getSasToken()));
            outgoingMessage.setBody(section);
        }
        catch (IOException e)
        {
            log.error("Failed to renew sas token while building new cbs authentication message", e);
            throw new TransportException(e);
        }

        return outgoingMessage;
    }
}
