// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import com.microsoft.azure.sdk.iot.digitaltwin.service.util.Base64;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
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

    @NonNull
    private final String hostName;
    @NonNull
    private final String sharedAccessKeyName;
    @NonNull
    private final String sharedAccessKey;

    private int timeToLiveInSecs;
    private String cachedSasToken;
    private long tokenExpiresOn;

    public SasTokenProviderWithSharedAccessKey(String hostName, String sharedAccessKeyName, String sharedAccessKey, int timeToLiveInSecs) {
        this.hostName = hostName;
        this.sharedAccessKeyName = sharedAccessKeyName;
        this.sharedAccessKey = sharedAccessKey;
        this.timeToLiveInSecs = timeToLiveInSecs;
    }

    @Override
    public String getSasToken() {
        if (hasCachedTokenExpired()) {
            this.cachedSasToken = buildToken();
        }
        return this.cachedSasToken;
    }

    /**
     * Helper function to build the token string
     *
     * @return Valid token string
     */
    private String buildToken() {
        String targetUri;
        tokenExpiresOn = tokenExpiresOn();
        try {
            // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
            targetUri = URLEncoder.encode(hostName.toLowerCase(), String.valueOf(StandardCharsets.UTF_8));
            // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string (one year)]
            String toSign = targetUri + "\n" + tokenExpiresOn;

            // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = Base64.decodeBase64Local(sharedAccessKey.getBytes("UTF-8"));
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes("UTF-8"));
            // Convert raw bytes to Hex
            String signature = URLEncoder.encode(
                    Base64.encodeBase64StringLocal(rawHmac), "UTF-8");

            // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
            return String.format(TOKEN_FORMAT, targetUri, signature, tokenExpiresOn, sharedAccessKeyName);
        }
        catch (Exception e) {
            // Codes_SRS_SERVICE_SDK_JAVA_IOTHUBSERVICESASTOKEN_12_007: [The constructor shall throw Exception if building the token failed]
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper function to calculate token expiry
     *
     * @return Seconds from now to expiry
     */
    private long tokenExpiresOn() {
        int ttl = timeToLiveInSecs != 0 ? timeToLiveInSecs : DEFAULT_TOKEN_TIME_TO_LIVE_IN_SECS;
        long expiresOnDate = System.currentTimeMillis();
        expiresOnDate += ttl * 1000;
        return expiresOnDate / 1000;
    }

    /**
     * Helper function to calculate if the generated sas token has expired
     * @return Boolean result evaluating if the cached token has expired
     */
    private boolean hasCachedTokenExpired() {
        return (tokenExpiresOn * 1000) < System.currentTimeMillis();
    }
}
