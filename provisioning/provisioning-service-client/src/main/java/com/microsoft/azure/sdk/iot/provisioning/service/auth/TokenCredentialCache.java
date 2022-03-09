// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * This class generates AAD authentication tokens from a TokenCredential but caches previous tokens when they aren't near
 * expiry.
 */
public class TokenCredentialCache
{
    private final static int MINUTES_BEFORE_PROACTIVE_RENEWAL = 9;
    private final TokenCredential tokenCredential;
    private AccessToken accessToken;

    public static final String[] PROVISIONING_PUBLIC_SCOPE = new String[]{"https://azure-devices-provisioning.net/.default"};
    public static final String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * Construct a new TokenCredentialCache instance.
     * @param tokenCredential The tokenCredential instance that this cache will use to generate new tokens.
     */
    public TokenCredentialCache(TokenCredential tokenCredential)
    {
        Objects.requireNonNull(tokenCredential, "tokenCredential cannot be null");

        this.tokenCredential = tokenCredential;
    }

    /**
     * Get a valid AAD authentication token. This may be the same as a previously returned token if it is not near
     * expiration time yet. If a token is less than or equal to 9 minutes away from expiring or is expired already, the
     * token will be renewed. Otherwise, a cached token will be returned.
     * @return a valid AAD authentication token.
     */
    public AccessToken getAccessToken()
    {
        if (this.accessToken == null || isAccessTokenCloseToExpiry(this.accessToken))
        {
            this.accessToken = tokenCredential.getToken(new TokenRequestContext().addScopes(PROVISIONING_PUBLIC_SCOPE)).block();
        }

        return this.accessToken;
    }

    /**
     * Get the access token string, including the Bearer prefix.
     * @return the access token string, including the Bearer prefix.
     */
    public String getTokenString()
    {
        return BEARER_TOKEN_PREFIX + getAccessToken().getToken();
    }

    /**
     * @return the TokenCredential instance that was set in the constructor.
     */
    public TokenCredential getTokenCredential()
    {
        return this.tokenCredential;
    }

    private static boolean isAccessTokenCloseToExpiry(AccessToken accessToken)
    {
        Duration remainingTimeToLive = Duration.between(Instant.now(), accessToken.getExpiresAt().toInstant());
        return remainingTimeToLive.toMinutes() <= MINUTES_BEFORE_PROACTIVE_RENEWAL;
    }
}
