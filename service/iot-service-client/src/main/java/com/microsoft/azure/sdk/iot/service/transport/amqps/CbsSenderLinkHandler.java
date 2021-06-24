// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.messaging.Properties;
import org.apache.qpid.proton.amqp.messaging.Section;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache.IOTHUB_PUBLIC_SCOPE;

/**
 * Every token based authentication over AMQP requires a CBS session with a sender and receiver link. This
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

    private TokenCredential credential;
    private AccessToken currentAccessToken;
    private String sasToken;
    private AzureSasCredential sasTokenProvider;

    private static final String BEARER = "Bearer";
    private static final String SAS_TOKEN = "servicebus.windows.net:sastoken";
    private static final String EXPIRY_KEY = "se=";

    CbsSenderLinkHandler(Sender sender, LinkStateCallback linkStateCallback, TokenCredential credential)
    {
        super(sender, UUID.randomUUID().toString(), linkStateCallback);

        this.senderLinkTag = SENDER_LINK_TAG_PREFIX;
        this.senderLinkAddress = SENDER_LINK_ENDPOINT_PATH;
        this.credential = credential;
    }

    CbsSenderLinkHandler(Sender sender, LinkStateCallback linkStateCallback, AzureSasCredential sasTokenProvider)
    {
        super(sender, UUID.randomUUID().toString(), linkStateCallback);

        this.senderLinkTag = SENDER_LINK_TAG_PREFIX;
        this.senderLinkAddress = SENDER_LINK_ENDPOINT_PATH;
        this.sasTokenProvider = sasTokenProvider;
    }

    CbsSenderLinkHandler(Sender sender, LinkStateCallback linkStateCallback, String sasToken)
    {
        super(sender, UUID.randomUUID().toString(), linkStateCallback);

        this.senderLinkTag = SENDER_LINK_TAG_PREFIX;
        this.senderLinkAddress = SENDER_LINK_ENDPOINT_PATH;
        this.sasToken = sasToken;
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

        Map<String, Object> applicationProperties = new HashMap<>();
        applicationProperties.put(PUT_TOKEN_OPERATION, PUT_TOKEN_OPERATION_VALUE);

        if (credential != null)
        {
            TokenRequestContext context = new TokenRequestContext().addScopes(IOTHUB_PUBLIC_SCOPE);
            this.currentAccessToken = credential.getToken(context).block();

            if (this.currentAccessToken == null)
            {
                log.error("The AccessToken supplied by the TokenCredential for the CbsSenderLinkHandler was null.");
                return -1;
            }

            applicationProperties.put(PUT_TOKEN_EXPIRY, Date.from(this.currentAccessToken.getExpiresAt().toInstant()));
            applicationProperties.put(PUT_TOKEN_TYPE, BEARER);
            Section section = new AmqpValue("Bearer " + this.currentAccessToken.getToken());
            outgoingMessage.setBody(section);
        }
        else if (this.sasTokenProvider != null)
        {
            String sasToken = this.sasTokenProvider.getSignature();
            this.currentAccessToken = getAccessTokenFromSasToken(sasToken);
            applicationProperties.put(PUT_TOKEN_TYPE, SAS_TOKEN);
            Section section = new AmqpValue(sasToken);
            outgoingMessage.setBody(section);
        }
        else
        {
            this.currentAccessToken = getAccessTokenFromSasToken(this.sasToken);
            applicationProperties.put(PUT_TOKEN_TYPE, SAS_TOKEN);
            Section section = new AmqpValue(this.sasToken);
            outgoingMessage.setBody(section);
        }


        applicationProperties.put(PUT_TOKEN_AUDIENCE, this.senderLink.getSession().getConnection().getHostname());

        outgoingMessage.setApplicationProperties(new ApplicationProperties(applicationProperties));

        return this.sendMessageAndGetDeliveryTag(outgoingMessage);
    }

    private AccessToken getAccessTokenFromSasToken(String sasToken)
    {
        // split "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s" into "SharedAccessSignature" "sr=%s&sig=%s&se=%s&skn=%s"
        String[] signatureParts = sasToken.split(" ");

        if (signatureParts.length != 2)
        {
            RuntimeException runtimeException = new RuntimeException("failed to parse shared access signature, unable to get the signature's time to live");
            log.error("Failed to get token from AzureSasCredential", runtimeException);
            throw runtimeException;
        }

        // split "sr=%s&sig=%s&se=%s&skn=%s" into "sr=%s" "sig=%s" "se=%s" "skn=%s"
        String[] signatureKeyValuePairs = signatureParts[1].split("&");

        int expiryTimeSeconds = -1;
        for (String signatureKeyValuePair : signatureKeyValuePairs)
        {
            if (signatureKeyValuePair.startsWith(EXPIRY_KEY))
            {
                // substring "se=%s" into "%s"
                String expiryTimeValue = signatureKeyValuePair.substring(EXPIRY_KEY.length());

                try
                {
                    expiryTimeSeconds = Integer.parseInt(expiryTimeValue);
                }
                catch (NumberFormatException e)
                {
                    RuntimeException runtimeException = new RuntimeException("Failed to parse shared access signature, unable to parse the signature's time to live to an integer", e);
                    log.error("Failed to get token from AzureSasCredential", runtimeException);
                    throw runtimeException;
                }
            }
        }

        if (expiryTimeSeconds == -1)
        {
            RuntimeException runtimeException = new RuntimeException("Failed to parse shared access signature, signature does not include key value pair for expiry time");
            log.error("Failed to get token from AzureSasCredential", runtimeException);
            throw runtimeException;
        }

        OffsetDateTime sasTokenExpiryOffsetDateTime =
                OffsetDateTime.ofInstant(
                        Instant.ofEpochSecond(expiryTimeSeconds), ZoneId.systemDefault());

        return new AccessToken(sasToken, sasTokenExpiryOffsetDateTime);
    }

    public AccessToken getCurrentAccessToken()
    {
        return this.currentAccessToken;
    }
}
