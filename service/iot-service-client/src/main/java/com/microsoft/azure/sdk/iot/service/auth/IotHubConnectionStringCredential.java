// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.auth;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

/**
 * Implementation of {@link TokenCredential} that takes your IoT Hub's connection string and generates SAS tokens.
 * This implementation will cache previously generated SAS tokens and will only renew them once the previous SAS token
 * is close to expiring.
 */
public class IotHubConnectionStringCredential implements TokenCredential
{
    private IotHubConnectionString iotHubConnectionString;
    private IotHubServiceSasToken serviceSasToken;
    private Long tokenLifespanSeconds;

    /**
     * Construct a new {@link IotHubConnectionStringCredential}.
     * @param iotHubConnectionString The connection string for your IoT Hub.
     */
    public IotHubConnectionStringCredential(String iotHubConnectionString)
    {
        this(iotHubConnectionString, 0);
    }

    /**
     * Construct a new {@link IotHubConnectionStringCredential}.
     * @param iotHubConnectionString The connection string for your IoT Hub.
     * @param tokenLifespanSeconds The number of seconds that the generated SAS tokens should be valid for. If less than
     *                             or equal to 0, the default time to live will be used.
     */
    public IotHubConnectionStringCredential(String iotHubConnectionString, long tokenLifespanSeconds)
    {
        this.iotHubConnectionString = IotHubConnectionString.createIotHubConnectionString(iotHubConnectionString);
        this.tokenLifespanSeconds = tokenLifespanSeconds;
    }

    /**
     * Get a valid SAS token. The returned token may be a cached SAS token from previous calls if that token is still
     * valid. This function will proactively renew the token ahead of its expiry time in order to avoid clock skew issues.
     * @param tokenRequestContext the context that the token will be used for
     * @return a non-expired SAS token built from the connection string that was provided in the constructor.
     */
    @Override
    public Mono<AccessToken> getToken(TokenRequestContext tokenRequestContext)
    {
        // Only one thread within the SDK will call this method, but this synchronization is in case a user provides the same
        // instance of this class to multiple service clients.
        synchronized (this)
        {
            if (this.serviceSasToken == null)
            {
                this.serviceSasToken = new IotHubServiceSasToken(this.iotHubConnectionString, this.tokenLifespanSeconds);
            }

            long millisecondsToExpiry = this.serviceSasToken.getExpiryTimeMillis() - System.currentTimeMillis();

            // Want to proactively renew the token at 15% of the SAS token's lifespan in order to avoid clock skew issues
            // Note that this equation is basically this.serviceSasToken.getTokenLifespanSeconds() * .15 * 1000, but this equation
            // doesn't require casting any doubles to ints so it is a bit safer
            long proactiveRenewalTimeMillis = this.serviceSasToken.getTokenLifespanSeconds() * 150;

            if (millisecondsToExpiry <= proactiveRenewalTimeMillis)
            {
                // by instantiating a new IotHubServiceSasToken, a new SAS token can be retrieved
                this.serviceSasToken = new IotHubServiceSasToken(this.iotHubConnectionString, this.tokenLifespanSeconds);
            }

            OffsetDateTime sasTokenExpiryOffsetDateTime =
                    OffsetDateTime.ofInstant(
                            Instant.ofEpochMilli(this.serviceSasToken.getExpiryTimeMillis()), ZoneId.systemDefault());

            AccessToken accessToken = new AccessToken(this.serviceSasToken.toString(), sasTokenExpiryOffsetDateTime);

            return Mono.just(accessToken);
        }
    }
}
