// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import com.microsoft.azure.sdk.iot.service.digitaltwin.helpers.Base64;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class SasTokenProviderWithSharedAccessKey implements SasTokenProvider {

    /**
     * The SAS token format. The parameters to be interpolated are, in order:
     * the signature
     * the resource URI
     * the expiry time
     * the key name
     * Example: {@code SharedAccessSignature sr=IOTHUBURI&sig=SIGNATURE&se=EXPIRY&skn=SHAREDACCESSKEYNAME}
     */
    private static final String TOKEN_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";
    private static final int DEFAULT_TOKEN_TIME_TO_LIVE_IN_SECS = 60 * 60;
    private final Lock lock;

    private final String hostName;
    private final String sharedAccessKeyName;
    private final String sharedAccessKey;

    private final int timeToLiveInSecs;
    private String cachedSasToken;
    private long tokenExpiryTimeInMilliSecs;

    @Builder
    private SasTokenProviderWithSharedAccessKey(@NonNull String hostName, @NonNull String sharedAccessKeyName, @NonNull String sharedAccessKey, Integer timeToLiveInSecs) {
        if (timeToLiveInSecs == null) {
            timeToLiveInSecs = DEFAULT_TOKEN_TIME_TO_LIVE_IN_SECS;
        }

        this.hostName = hostName;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKey;
        this.timeToLiveInSecs = timeToLiveInSecs;
        this.lock = new ReentrantLock();
    }

    @Override
    public String getSasToken() throws IOException {
        try {
            lock.lock();

            if (isTokenExpired()) {
                cachedSasToken = buildToken();
            }

            return cachedSasToken;
        }
        finally {
            lock.unlock();
        }
    }

    private String buildToken() throws IOException {
        log.debug("Generating new SAS token");

        String targetUri;
        long newTokenExpiryTimeInSecs = getTokenExpiryTimeInMilliSecs() / 1000;
        try {
            targetUri = URLEncoder.encode(hostName.toLowerCase(), UTF_8.toString());
            String toSign = targetUri + "\n" + newTokenExpiryTimeInSecs;

            byte[] keyBytes = Base64.decodeBase64Local(sharedAccessKey.getBytes(UTF_8));
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(toSign.getBytes(UTF_8));
            String signature = URLEncoder.encode(
                    Base64.encodeBase64StringLocal(rawHmac), UTF_8.toString());

            String newToken = String.format(TOKEN_FORMAT, targetUri, signature, newTokenExpiryTimeInSecs, sharedAccessKeyName);
            tokenExpiryTimeInMilliSecs = newTokenExpiryTimeInSecs * 1000;

            log.debug("Generated new SAS token");
            return newToken;
        }
        catch (Exception e) {
            throw new IOException("Generation of new SAS token failed", e);
        }
    }

    private long getTokenExpiryTimeInMilliSecs() {
        return System.currentTimeMillis() + timeToLiveInSecs * 1000L;
    }

    private boolean isTokenExpired() {
        return cachedSasToken == null || tokenExpiryTimeInMilliSecs <= System.currentTimeMillis();
    }
}
