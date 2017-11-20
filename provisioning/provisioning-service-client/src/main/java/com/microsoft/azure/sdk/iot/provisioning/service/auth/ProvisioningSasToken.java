/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.azure.sdk.iot.deps.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/** 
 * Grants device access to an Provisioning for the specified amount of time.
 */
public final class ProvisioningSasToken
{
    private static final long TOKEN_VALID_SECS = 365*24*60*60;
    private static final long ONE_SECOND_IN_MILLISECONDS = 1000;

    /**
     * The SAS token format. The parameters to be interpolated are, in order:
     * the signature
     * the resource URI
     * the expiry time
     * the key name
     * Example: {@code SharedAccessSignature sr=DEVICEPROVISIONINGSERVICEURI&sig=SIGNATURE&se=EXPIRY&skn=SHAREDACCESSKEYNAME}
     */
    private static final String TOKEN_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";

    /* The URI for a connection to an Provisioning */
    private final String resourceUri;
    /* The value of the SharedAccessKey */
    private final String keyValue;
    /* The time, as a UNIX timestamp, before which the token is valid. */
    private final long expiryTime;
    /* The value of SharedAccessKeyName */
    private final String keyName;
    /* The SAS token that grants access. */
    private final String token;

    /**
     * Constructor. Generates a SAS token that grants access to an Provisioning for
     * the specified amount of time. (1 year specified in TOKEN_VALID_SECS)
     *
     * @param provisioningConnectionString Connection string object containing the connection parameters
     * @throws IllegalArgumentException if the provided provisioning connection string is null
     */
    public ProvisioningSasToken(ProvisioningConnectionString provisioningConnectionString) throws IllegalArgumentException
    {
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_001: [The constructor shall throw IllegalArgumentException if the input object is null]
        if (provisioningConnectionString == null)
        {
            throw new IllegalArgumentException("provisioningConnectionString is null");
        }
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string (one year)]
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
        this.resourceUri = provisioningConnectionString.getHostName();
        this.keyValue = provisioningConnectionString.getSharedAccessKey();
        this.keyName = provisioningConnectionString.getSharedAccessKeyName();
        this.expiryTime = buildExpiresOn();
        this.token =  buildToken();
    }

    /**
     * Helper function to build the token string
     *
     * @return Valid token string
     */
    private String buildToken()
    {
        String targetUri;
        try
        {
            // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_002: [The constructor shall create a target uri from the url encoded host name)]
            targetUri = URLEncoder.encode(this.resourceUri.toLowerCase(), String.valueOf(StandardCharsets.UTF_8));
            // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_003: [The constructor shall create a string to sign by concatenating the target uri and the expiry time string (one year)]
            String toSign = targetUri + "\n" + this.expiryTime;

            // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_004: [The constructor shall create a key from the shared access key signing with HmacSHA256]
            // Get an hmac_sha1 key from the raw key bytes
            byte[] keyBytes = Base64.decodeBase64Local(this.keyValue.getBytes("UTF-8"));
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_005: [The constructor shall compute the final signature by url encoding the signed key]
            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes("UTF-8"));
            // Convert raw bytes to Hex
            String signature = URLEncoder.encode(
                    Base64.encodeBase64StringLocal(rawHmac), "UTF-8");

            // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_006: [The constructor shall concatenate the target uri, the signature, the expiry time and the key name using the format: "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s"]
            return String.format(TOKEN_FORMAT, targetUri, signature, this.expiryTime, this.keyName);
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException e)
        {
            // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_007: [The constructor shall throw Exception if building the token failed]
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper function to calculate token expiry
     *
     * @return Seconds from now to expiry
     */
    private long buildExpiresOn()
    {
        long expiresOnDate = System.currentTimeMillis();
        expiresOnDate += TOKEN_VALID_SECS * ONE_SECOND_IN_MILLISECONDS;
        return expiresOnDate / ONE_SECOND_IN_MILLISECONDS;
    }

    /**
     * Returns the string representation of the SAS token.
     *
     * @return The string representation of the SAS token.
     */
    @Override
    public String toString()
    {
        // Codes_SRS_PROVISIONING_SERVICE_SASTOKEN_12_008: [The constructor shall return with the generated token]
        return this.token;
    }
}

