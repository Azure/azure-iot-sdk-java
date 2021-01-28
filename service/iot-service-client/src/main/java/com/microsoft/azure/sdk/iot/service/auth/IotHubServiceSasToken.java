/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.apache.commons.codec.binary.Base64.decodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;

/** 
 * Grants device access to an IoT Hub for the specified amount of time. 
 */
public final class IotHubServiceSasToken
{
    private static long DEFAULT_TOKEN_LIFESPAN_SECONDS = 60 * 60; // 1 hour

    /**
     * The SAS token format. The parameters to be interpolated are, in order:
     * the signature
     * the resource URI
     * the expiry time
     * the key name
     * Example: {@code SharedAccessSignature sr=IOTHUBURI&sig=SIGNATURE&se=EXPIRY&skn=SHAREDACCESSKEYNAME}
     */
    public static final String TOKEN_FORMAT = "SharedAccessSignature sr=%s&sig=%s&se=%s&skn=%s";

    /* The URI for a connection to an IoT Hub */
    protected final String resourceUri;
    /* The value of the SharedAccessKey */
    protected final String keyValue;
    /* The time, as a UNIX timestamp, before which the token is valid. */
    protected final long expiryTimeSeconds;
    /* The value of SharedAccessKeyName */
    protected final String keyName;
    /* The SAS token that grants access. */
    protected final String token;

    protected final long tokenLifespanSeconds;

    /**
     * Constructor. Generates a SAS token that grants access to an IoT Hub for
     * the specified amount of time. (1 year specified in DEFAULT_TOKEN_LIFESPAN_SECONDS)
     *
     * @param iotHubConnectionString Connection string object containing the connection parameters
     */
    public IotHubServiceSasToken(IotHubConnectionString iotHubConnectionString)
    {
        this(iotHubConnectionString, DEFAULT_TOKEN_LIFESPAN_SECONDS);
    }

    /**
     * Constructor. Generates a SAS token that grants access to an IoT Hub for
     * the specified amount of time. (1 year specified in DEFAULT_TOKEN_LIFESPAN_SECONDS)
     *
     * @param iotHubConnectionString Connection string object containing the connection parameters.
     * @param tokenLifespanSeconds The number of seconds that the created SAS token will be valid for.
     */
    public IotHubServiceSasToken(IotHubConnectionString iotHubConnectionString, long tokenLifespanSeconds)
    {
        if (iotHubConnectionString == null)
        {
            throw new IllegalArgumentException();
        }

        if (tokenLifespanSeconds <= 0)
        {
            tokenLifespanSeconds = DEFAULT_TOKEN_LIFESPAN_SECONDS;
        }

        this.tokenLifespanSeconds = tokenLifespanSeconds;
        this.resourceUri = iotHubConnectionString.getHostName();
        this.keyValue = iotHubConnectionString.getSharedAccessKey();
        this.keyName = iotHubConnectionString.getSharedAccessKeyName();
        this.expiryTimeSeconds = buildExpiresOn();
        this.token =  buildToken();
    }

    /**
     * @return the number of milliseconds since the UNIX Epoch when this token will expire
     */
    public final long getExpiryTimeMillis()
    {
        // Multiply by 1000 to convert from seconds to milliseconds
        return (this.expiryTimeSeconds * 1000);
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
            targetUri = URLEncoder.encode(this.resourceUri.toLowerCase(), StandardCharsets.UTF_8.name());
            String toSign = targetUri + "\n" + this.expiryTimeSeconds;

            byte[] keyBytes = decodeBase64(this.keyValue.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec signingKey = new SecretKeySpec(keyBytes, "HmacSHA256");

            // Get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            // Compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(toSign.getBytes(StandardCharsets.UTF_8));

            // Convert raw bytes to Hex
            String signature = URLEncoder.encode(encodeBase64String(rawHmac), StandardCharsets.UTF_8.name());

            return String.format(TOKEN_FORMAT, targetUri, signature, this.expiryTimeSeconds, this.keyName);
        }
        catch (Exception e)
        {
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
        expiresOnDate += this.tokenLifespanSeconds * 1000;
        return expiresOnDate / 1000;
    }

    /**
     * @return The number of seconds that this token is valid for. Not to be confused with how many seconds the SAS token
     * is still valid for at the time of calling this method.
     */
    public long getTokenLifespanSeconds()
    {
        return this.tokenLifespanSeconds;
    }

    /**
     * Returns the string representation of the SAS token.
     *
     * @return The string representation of the SAS token.
     */
    @Override
    public String toString()
    {
        return this.token;
    }
}

