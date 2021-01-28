// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.TokenCredentialType;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Every SAS token based authentication over AMQP requires a CBS session with a sender and receiver link. This
 * class defines the sender link which proactively sends renewed sas tokens to keep the device sessions authenticated.
 */
@Slf4j
public final class CbsSenderLinkHandler extends SenderLinkHandler
{
    private static final String SENDER_LINK_ENDPOINT_PATH = "$cbs";

    private static final String SENDER_LINK_TAG_PREFIX = "cbs-sender";

    private static final String CBS_TO = "$cbs";
    private static final String CBS_REPLY = "cbs";
    private static final String LINK_TYPE = "cbs";

    private static final String PUT_TOKEN_TYPE = "type";
    private static final String PUT_TOKEN_AUDIENCE = "name";
    private static final String PUT_TOKEN_EXPIRY = "expiration";
    private static final String PUT_TOKEN_OPERATION = "operation";
    private static final String PUT_TOKEN_OPERATION_VALUE = "put-token";

    private final TokenCredential authenticationTokenProvider;
    private final TokenCredentialType authorizationType;
    private AccessToken currentAccessToken;

    CbsSenderLinkHandler(Sender sender, LinkStateCallback linkStateCallback, TokenCredential authenticationTokenProvider, TokenCredentialType authorizationType)
    {
        super(sender, UUID.randomUUID().toString(), linkStateCallback);

        this.senderLinkTag = SENDER_LINK_TAG_PREFIX;
        this.senderLinkAddress = SENDER_LINK_ENDPOINT_PATH;
        this.authenticationTokenProvider = authenticationTokenProvider;
        this.authorizationType = authorizationType;
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

    public int sendAuthenticationMessage(UUID authenticationMessageCorrelationId)
    {
        MessageImpl outgoingMessage = (MessageImpl) Proton.message();

        Properties properties = new Properties();

        // Note that setting "messageId = correlationId" is intentional.
        // IotHub only responds correctly if this correlation id is set this way
        properties.setMessageId(authenticationMessageCorrelationId);

        properties.setTo(CBS_TO);
        properties.setReplyTo(CBS_REPLY);
        outgoingMessage.setProperties(properties);

        Map<String, Object> userProperties = new HashMap<>(4);

        //TODO need more context on this TokenRequestContext object, and what we are expected to give it
        this.currentAccessToken = authenticationTokenProvider.getToken(new TokenRequestContext()).block();

        userProperties.put(PUT_TOKEN_OPERATION, PUT_TOKEN_OPERATION_VALUE);
        userProperties.put(PUT_TOKEN_EXPIRY, Date.from(this.currentAccessToken.getExpiresAt().toInstant()));
        userProperties.put(PUT_TOKEN_TYPE, authorizationType.getTokenType());
        userProperties.put(PUT_TOKEN_AUDIENCE, this.senderLink.getSession().getConnection().getHostname());

        ApplicationProperties applicationProperties = new ApplicationProperties(userProperties);
        outgoingMessage.setApplicationProperties(applicationProperties);

        Section section = new AmqpValue(this.currentAccessToken.getToken());
        outgoingMessage.setBody(section);

        return this.sendMessageAndGetDeliveryTag(outgoingMessage);
    }

    public AccessToken getCurrentAccessToken()
    {
        return this.currentAccessToken;
    }
}
