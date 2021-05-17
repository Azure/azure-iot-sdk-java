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

/**
 * This class generates AAD authentication tokens from a TokenCredential but caches previous tokens when they aren't near
 * expiry.
 */
@Slf4j
public class TokenCredentialCache
{
    private final static int MINUTES_BEFORE_PROACTIVE_RENEWAL = 9;
    private final TokenCredential tokenCredential;
    private AccessToken accessToken;
    private final String[] authenticationScopes;

    private static final String[] IOTHUB_PUBLIC_SCOPES = new String[]{"https://iothubs.azure.net/.default"};
    private static final String[] IOTHUB_FAIRFAX_SCOPES = new String[]{"https://iothubs.azure.us"};

    // All IoT Hubs in the Fairfax cloud instance have a hostname that ends with this suffix
    private static final String FAIRFAX_HUB_SUFFIX = "azure-devices.us";

    private static final String BEARER_TOKEN_PREFIX = "Bearer ";

    /**
     * Construct a new TokenCredentialCache instance. This cache can only be used to generate authentication tokens
     * for public cloud IoT Hubs and some private cloud IoT Hubs. For Fairfax IoT Hubs, use {@link #TokenCredentialCache(TokenCredential, String)}.
     * @param tokenCredential The tokenCredential instance that this cache will use to generate new tokens.
     */
    @SuppressWarnings("unused") // Unused by our codebase, but removing it would be a breaking change
    public TokenCredentialCache(TokenCredential tokenCredential)
    {
        this(tokenCredential, null);
    }

    /**
     * Construct a new TokenCredentialCache instance.
     * @param tokenCredential The tokenCredential instance that this cache will use to generate new tokens.
     * @param hostname The hostname of the IoT Hub that this token credential cache will generate tokens for.
     * This argument is used to determine if the tokens generated should be compatible with public cloud IoT Hubs or
     * with private cloud instances.
     */
    public TokenCredentialCache(TokenCredential tokenCredential, String hostname)
    {
        Objects.requireNonNull(tokenCredential, "tokenCredential cannot be null");

        this.tokenCredential = tokenCredential;
        this.authenticationScopes = getAuthenticationScopes(hostname);
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
            this.accessToken = tokenCredential.getToken(new TokenRequestContext().addScopes(this.authenticationScopes)).block();
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
     * @param hostname The hostname of the IoT Hub that the tokens will be used to authenticate against.
     * @return The authentication scopes to be used when generating AAD access tokens.
     */
    public static String[] getAuthenticationScopes(String hostname)
    {
        if (isFairfaxHub(hostname))
        {
            log.debug("Fairfax IoT Hub detected based on hostname, supplying Fairfax specific authentication scopes.");
            return IOTHUB_FAIRFAX_SCOPES;
        }

        return IOTHUB_PUBLIC_SCOPES;
    }

    private static boolean isAccessTokenCloseToExpiry(AccessToken accessToken)
    {
        Duration remainingTimeToLive = Duration.between(Instant.now(), accessToken.getExpiresAt().toInstant());
        return remainingTimeToLive.toMinutes() <= MINUTES_BEFORE_PROACTIVE_RENEWAL;
    }

    /**
     * Checks if the provided hostname is associated with an IoT Hub deployed in the Fairfax cloud instance.
     * @param hostname The hostname of the IoT Hub.
     * @return True if the IoT Hub is deployed in the Fairfax cloud instance, and false otherwise.
     */
    private static boolean isFairfaxHub(String hostname)
    {
        if (hostname == null)
        {
            return false;
        }

        // Java doesn't have a case insensitive endsWith() function
        return hostname.toLowerCase().endsWith(FAIRFAX_HUB_SUFFIX.toLowerCase());
    }
}
