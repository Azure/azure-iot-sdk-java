// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.auth;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static com.microsoft.azure.sdk.iot.service.auth.IotHubAuthenticationScopes.DEFAULT_AUTHENTICATION_SCOPES;

/**
 * This class generates AAD authentication tokens from a TokenCredential but caches previous tokens when they aren't
 * near expiry.
 */
@Slf4j
public class TokenCredentialCache
{
    private final static int MINUTES_BEFORE_PROACTIVE_RENEWAL = 9;
    private final TokenCredential tokenCredential;
    private AccessToken accessToken;
    private final String[] authenticationScopes;

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * Construct a new TokenCredentialCache instance. This cache can only be used to generate authentication tokens
     * for public cloud IoT Hubs and some private cloud IoT Hubs. For Fairfax IoT Hubs, use
     * {@link #TokenCredentialCache(TokenCredential, String[])}.
     * @param tokenCredential The tokenCredential instance that this cache will use to generate new tokens.
     */
    @SuppressWarnings("unused") // Unused by our codebase, but removing it would be a breaking change
    public TokenCredentialCache(TokenCredential tokenCredential)
    {
        this(tokenCredential, DEFAULT_AUTHENTICATION_SCOPES);

        new IotHubAuthenticationScopes();
    }

    /**
     * Construct a new TokenCredentialCache instance.
     * @param tokenCredential The tokenCredential instance that this cache will use to generate new tokens.
     * @param authenticationScopes The authentication scopes to be used when generating authentication tokens.
     */
    public TokenCredentialCache(TokenCredential tokenCredential, String[] authenticationScopes)
    {
        Objects.requireNonNull(tokenCredential, "tokenCredential cannot be null");
        Objects.requireNonNull(authenticationScopes, "authenticationScopes cannot be null");

        this.tokenCredential = tokenCredential;
        this.authenticationScopes = authenticationScopes;
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
            log.trace("Generating new access token.");
            this.accessToken = tokenCredential.getToken(getTokenRequestContext()).block();
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

    /**
     * Get the authentication scopes to be used when generating AAD access tokens.
     * @return The authentication scopes to be used when generating AAD access tokens.
     */
    public TokenRequestContext getTokenRequestContext()
    {
        return new TokenRequestContext().addScopes(this.authenticationScopes);
    }

    private static boolean isAccessTokenCloseToExpiry(AccessToken accessToken)
    {
        Duration remainingTimeToLive = Duration.between(Instant.now(), accessToken.getExpiresAt().toInstant());
        return remainingTimeToLive.toMinutes() <= MINUTES_BEFORE_PROACTIVE_RENEWAL;
    }
}
