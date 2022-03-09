/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import org.apache.commons.codec.binary.Base32;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public abstract class SecurityProviderTpm extends SecurityProvider
{
    private static final String SHA_256 = "SHA-256";
    private static final String EQUALS = "=";

    abstract public byte[] activateIdentityKey(byte[] key) throws SecurityProviderException;
    abstract public byte[] getEndorsementKey() throws SecurityProviderException;
    abstract public byte[] getStorageRootKey() throws SecurityProviderException;
    abstract public byte[] signWithIdentity(byte[] data) throws SecurityProviderException;

    @Override
    public String getRegistrationId() throws SecurityProviderException
    {
        try
        {
            byte[] enrollmentKey = this.getEndorsementKey();

            MessageDigest digest = MessageDigest.getInstance(SHA_256);
            byte[] hash = digest.digest(enrollmentKey);

            Base32 base32 = new Base32();
            byte[] base32Encoded = base32.encode(hash);

            String registrationId = new String(base32Encoded, StandardCharsets.UTF_8).toLowerCase();
            if (registrationId.contains(EQUALS))
            {
                registrationId = registrationId.replace(EQUALS, "").toLowerCase();
            }
            return registrationId;
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new SecurityProviderException(e);
        }
    }
}
