// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Implementation of {@link TokenCredential} that uses an instance of {@link AzureSasCredential} to provide shared access
 * signatures to be the authentication tokens. For internal use.
 */
@Slf4j
class AzureSasTokenCredential implements TokenCredential
{
    private static final String EXPIRY_KEY = "se=";
    private AzureSasCredential azureSasCredential;

    public AzureSasTokenCredential(AzureSasCredential azureSasCredential)
    {
        this.azureSasCredential = azureSasCredential;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext)
    {
        String signature = this.azureSasCredential.getSignature();

        // split "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s" into "SharedAccessSignature" "sr=%s&sig=%s&se=%s&skn=%s"
        String[] signatureParts = signature.split(" ");

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
                    expiryTimeSeconds = Integer.valueOf(expiryTimeValue);
                } catch (NumberFormatException e)
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

        return Mono.just(new AccessToken(signature, sasTokenExpiryOffsetDateTime));
    }
}
