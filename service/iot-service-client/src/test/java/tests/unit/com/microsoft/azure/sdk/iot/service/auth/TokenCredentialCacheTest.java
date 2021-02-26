// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.unit.com.microsoft.azure.sdk.iot.service.auth;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.sun.scenario.effect.Offset;
import mockit.Expectations;
import mockit.Mocked;
import org.joda.time.field.OffsetDateTimeField;
import org.junit.Test;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;

public class TokenCredentialCacheTest
{
    @Mocked
    TokenCredential mockTokenCredential;

    @Mocked
    AccessToken mockAccessToken;

    @Mocked
    AccessToken mockAccessToken2;

    @Mocked
    Mono<AccessToken> mockTokenTask;

    @Test
    public void tokenCredentialCachesToken()
    {
        TokenCredentialCache cache = new TokenCredentialCache(mockTokenCredential);

        new Expectations()
        {
            {
                mockTokenCredential.getToken((TokenRequestContext) any).block();
                result = mockAccessToken;

                mockAccessToken.getExpiresAt();
                result = OffsetDateTime.MAX;
            }
        };

        AccessToken accessToken = cache.getAccessToken();
        AccessToken accessToken2 = cache.getAccessToken();

        assertEquals(mockAccessToken, accessToken);
        assertEquals(mockAccessToken, accessToken2);
    }

    @Test
    public void tokenCredentialProactivelyRenewsToken()
    {
        TokenCredentialCache cache = new TokenCredentialCache(mockTokenCredential);

        new Expectations()
        {
            {
                mockTokenCredential.getToken((TokenRequestContext) any).block();
                result = mockAccessToken;

                mockAccessToken.getExpiresAt();
                result = OffsetDateTime.MAX;
            }
        };

        AccessToken accessToken = cache.getAccessToken();

        // 8 minutes from the current time, should fit within the proactive renewal range
        final long milliseconds = System.currentTimeMillis() + (8 * 60 * 1000);
        new Expectations()
        {
            {
                mockAccessToken.getExpiresAt();
                result = Instant.ofEpochMilli(milliseconds).atOffset(ZoneOffset.UTC);

                mockTokenCredential.getToken((TokenRequestContext) any).block();
                result = mockAccessToken2;
            }
        };

        // act
        AccessToken accessToken2 = cache.getAccessToken();

        // assert
        assertEquals(mockAccessToken, accessToken);
        assertEquals(mockAccessToken2, accessToken2);
    }

    @Test
    public void tokenCredentialRenewsExpiredToken()
    {
        TokenCredentialCache cache = new TokenCredentialCache(mockTokenCredential);

        new Expectations()
        {
            {
                mockTokenCredential.getToken((TokenRequestContext) any).block();
                result = mockAccessToken;

                mockAccessToken.getExpiresAt();
                result = OffsetDateTime.MAX;
            }
        };

        AccessToken accessToken = cache.getAccessToken();

        // Token expired one minute ago
        final long milliseconds = System.currentTimeMillis() - (60 * 1000);
        new Expectations()
        {
            {
                mockAccessToken.getExpiresAt();
                result = Instant.ofEpochMilli(milliseconds).atOffset(ZoneOffset.UTC);

                mockTokenCredential.getToken((TokenRequestContext) any).block();
                result = mockAccessToken2;
            }
        };

        // act
        AccessToken accessToken2 = cache.getAccessToken();

        // assert
        assertEquals(mockAccessToken, accessToken);
        assertEquals(mockAccessToken2, accessToken2);
    }

    @Test
    public void tokenCredentialDoesNotRenewTooProactively()
    {
        TokenCredentialCache cache = new TokenCredentialCache(mockTokenCredential);

        new Expectations()
        {
            {
                mockTokenCredential.getToken((TokenRequestContext) any).block();
                result = mockAccessToken;

                mockAccessToken.getExpiresAt();
                result = OffsetDateTime.MAX;
            }
        };

        AccessToken accessToken = cache.getAccessToken();

        // 12 minutes from the current time, should not fit within the proactive renewal range, so the cached token shouldn't be renewed
        final long milliseconds = System.currentTimeMillis() + (12 * 60 * 1000);
        new Expectations()
        {
            {
                mockAccessToken.getExpiresAt();
                result = Instant.ofEpochMilli(milliseconds).atOffset(ZoneOffset.UTC);
            }
        };

        // act
        AccessToken accessToken2 = cache.getAccessToken();

        // assert
        assertEquals(mockAccessToken, accessToken);
        assertEquals(mockAccessToken, accessToken2);
    }
}
