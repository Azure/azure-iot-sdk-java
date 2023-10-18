// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.auth;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Builds the authorization signature as a composition of functions. */
@Slf4j
final class SignatureHelper
{
    /**
     * The device ID will be the prefix. The expiry time, as a UNIX
     * timestamp, will be the suffix.
     */
    private static final String RAW_SIGNATURE_FORMAT = "%s\n%s";

    /** The charset used for the raw and hashed signature. */
    private static final Charset SIGNATURE_CHARSET = StandardCharsets.UTF_8;

    /**
     * Builds the raw signature.
     *
     * @param resourceUri the resource URI.
     * @param expiryTime the signature expiry time, as a UNIX timestamp.
     *
     * @return the raw signature.
     */
    public static byte[] buildRawSignature(String resourceUri, long expiryTime)
    {
        return String.format(RAW_SIGNATURE_FORMAT, resourceUri, expiryTime)
                .getBytes(SIGNATURE_CHARSET);
    }

    /**
     * Decodes the deviceKey using Base64.
     *
     * @param deviceKey the device key.
     *
     * @return the Base64-decoded device key.
     */
    public static byte[] decodeDeviceKeyBase64(String deviceKey)
    {
        return Base64.getDecoder().decode(deviceKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Encrypts the signature using HMAC-SHA256.
     *
     * @param sig the unencrypted signature.
     * @param deviceKey the Base64-decoded device key.
     *
     * @return the HMAC-SHA256 encrypted signature.
     */
    public static byte[] encryptSignatureHmacSha256(byte[] sig,
            byte[] deviceKey)
    {
        String hmacSha256 = "HmacSHA256";

        SecretKeySpec secretKey = new SecretKeySpec(deviceKey, hmacSha256);

        byte[] encryptedSig = null;
        try
        {
            Mac hMacSha256 = Mac.getInstance(hmacSha256);
            hMacSha256.init(secretKey);
            encryptedSig = hMacSha256.doFinal(sig);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e)
        {
            // will never happen since the algorithm and input key are hard-coded. Don't want to bother
            // adding these exceptions to the thrown exceptions of this method because of that.
            log.error("Unexpected error encountered while encrypting signature", e);
        }

        return encryptedSig;
    }

    /**
     * Encodes the signature using Base64 and then further
     * encodes the resulting string using UTF-8 encoding.
     *
     * @param sig the HMAC-SHA256 encrypted signature.
     *
     * @return the Base64-encoded signature.
     */
    public static byte[] encodeSignatureBase64(byte[] sig)
    {
        return Base64.getEncoder().encode(sig);
    }

    /**
     * Encodes the signature using charset UTF-8.
     *
     * @param sig the HMAC-SHA256 encrypted, Base64-encoded signature.
     *
     * @return the signature encoded using charset UTF-8.
     */
    public static String encodeSignatureUtf8(byte[] sig)
    {
        return new String(sig, SIGNATURE_CHARSET);
    }


    /**
     * Safely escapes characters in the signature so that they can be
     * transmitted over the internet. Replaces unsafe characters with a '%'
     * followed by two hexadecimal digits (i.e. %2d).
     *
     * @param sig the HMAC-SHA256 encrypted, Base64-encoded, UTF-8 encoded
     * signature.
     *
     * @return the web-safe encoding of the signature.
     */
    public static String encodeSignatureWebSafe(String sig)
    {
        String strSig;
        try
        {
            strSig = URLEncoder.encode(sig, SIGNATURE_CHARSET.name());
        }
        catch (UnsupportedEncodingException e)
        {
            // should never happen, since the encoding is hard-coded.
            throw new IllegalStateException(e);
        }

        return strSig;
    }

    @SuppressWarnings("unused")
    protected SignatureHelper()
    {
    }
}
