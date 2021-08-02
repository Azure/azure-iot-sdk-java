/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Base64;

public class SecurityProviderSymmetricKey extends SecurityProvider
{
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private final byte[] primaryKey;
    private byte[] secondaryKey;
    private final String registrationId;

    /**
     * Constructor for Symmetric key security provider
     * @param symmetricKey Symmetric key to be used. For authenticating devices that are under an individual enrollment,
     *                     this symmetric key is the same symmetric key that the enrollment has. For authenticating devices
     *                     that are under a group enrollment, this symmetric key is derived from the enrollment group's symmetric key
     * @param registrationId Registration ID to be used
     */
    public SecurityProviderSymmetricKey(byte[] symmetricKey, String registrationId)
    {
        if (symmetricKey == null)
        {
            throw new IllegalArgumentException("Symmetric key cannot be null");
        }

        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("Registration ID cannot be null");
        }

        this.primaryKey = symmetricKey;
        this.registrationId = registrationId;
    }

    /**
     * Constructor for Symmetric key security provider that takes both keys
     * @param primaryKey Primary key to be used
     * @param secondaryKey Secondary key to be used
     * @param registrationId Registration ID to be used
     */
    public SecurityProviderSymmetricKey(String primaryKey, String secondaryKey, String registrationId)
    {
        if (primaryKey == null || primaryKey.isEmpty() || secondaryKey == null || secondaryKey.isEmpty())
        {
            throw new IllegalArgumentException("Symmetric key cannot be null");
        }

        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("Registration ID cannot be null");
        }

        this.primaryKey = primaryKey.getBytes();
        this.secondaryKey = secondaryKey.getBytes();
        this.registrationId = registrationId;
    }

    /**
     * Getter for Symmetric key
     * @return Returns Symmetric Key byte array
     */
    public byte[] getSymmetricKey()
    {
        return primaryKey;
    }

    /**
     * Getter for Secondary key
     * @return Returns Secondary Key byte array
     */
    public byte[] getSecondaryKey()
    {
        return secondaryKey;
    }

    /**
     * Unique id required for registration
     *
     * @return Returns the registration Id used needed for the service
     * @throws SecurityProviderException If registration id with the underlying implementation could not be retrieved
     */
    @Override
    public String getRegistrationId() throws SecurityProviderException
    {
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new SecurityProviderException("Registration is null or empty");
        }
        return registrationId;
    }

    /**
     * Signs data using the provided base 64 decoded key using HMAC SHA 256
     * @param signature Data to be signed
     * @param base64DecodedKey Key used for signing
     * @return Returns signed data
     * @throws SecurityProviderException If signing was not successful
     */
    public byte[] HMACSignData(byte[] signature, byte[] base64DecodedKey) throws SecurityProviderException
    {
        if (signature == null || signature.length == 0 || base64DecodedKey == null || base64DecodedKey.length == 0)
        {
            throw new SecurityProviderException("Signature or Key cannot be null or empty");
        }

        try
        {
            SecretKeySpec secretKey = new SecretKeySpec(base64DecodedKey, HMAC_SHA_256);
            Mac hMacSha256 = Mac.getInstance(HMAC_SHA_256);
            hMacSha256.init(secretKey);
            return hMacSha256.doFinal(signature);
        }
        catch (NoSuchAlgorithmException | InvalidKeyException e)
        {
            throw new SecurityProviderException(e);
        }
    }

    /**
     * Compute the derived symmetric key for authenticating your device based on it's group enrollment's symmetric key and
     * from the device's registrationId. This step is only necessary for devices in group enrollments. Devices that
     * are registered via an individual enrollment can use the individual enrollment's symmetric key directly.
     * @param enrollmentGroupSymmetricKey The primary or secondary key of the group enrollment for which a device is computing it's derived symmetric key. Must be a UTF-8 byte array.
     * @param deviceId The Id of the particular device to compute the derived key for. The user is expected to decide what the
     *                 deviceId should be prior to attempting to provision the device.
     * @return The derived key, in base64 encoded bytes.
     * @throws InvalidKeyException If any exception occurs while initializing the HMAC with the provided key
     * @throws NoSuchAlgorithmException If your device does not support HMAC_SHA256
     */
    public static byte[] ComputeDerivedSymmetricKey(byte[] enrollmentGroupSymmetricKey, String deviceId) throws InvalidKeyException, NoSuchAlgorithmException
    {
        // The symmetric key, as provided by the Azure Portal, is a base64 encoded string, so first we need to decode it
        byte[] masterKeyBytes = Base64.getDecoder().decode(enrollmentGroupSymmetricKey);
        SecretKeySpec secretKey = new SecretKeySpec(masterKeyBytes, HMAC_SHA_256);
        Mac hMacSha256 = Mac.getInstance(HMAC_SHA_256);
        hMacSha256.init(secretKey);
        return Base64.getEncoder().encode(hMacSha256.doFinal(deviceId.getBytes()));
    }
}
