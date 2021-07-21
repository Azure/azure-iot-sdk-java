/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.Tools;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.apache.commons.codec.binary.Base64.encodeBase64String;

/**
 * Store primary and secondary keys
 * Provide function for key length validation
 */
public class SymmetricKey
{
    private static final int MinKeyLengthInBytes = 16;
    private static final int MaxKeyLengthInBytes = 64;
    private static final String DeviceKeyLengthInvalid = "DeviceKeyLengthInvalid";
    private static final String EncryptionMethod = "AES";

    private String primaryKey;
    private String secondaryKey;

    /**
     * Constructor for initialization
     */
    public SymmetricKey()
    {
        try
        {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(EncryptionMethod);
            keyGenerator.init(new SecureRandom());
            this.primaryKey = encodeBase64String(keyGenerator.generateKey().getEncoded());
            this.secondaryKey = encodeBase64String(keyGenerator.generateKey().getEncoded());
        }
        catch (NoSuchAlgorithmException e)
        {
            //encryption method is hardcoded, so this should never be caught
        }
    }

    /**
     * Getter for primary key
     * @return Primary key part of the symmetric key
     */
    public String getPrimaryKey()
    {
        return primaryKey;
    }

    /**
     * Setter for primary key
     * Validates the length of the key
     *
     * @param primaryKey Primary key part of the symmetric key
     */
    public final void setPrimaryKey(String primaryKey)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_001: [The function shall throw IllegalArgumentException if the length of the key less than 16 or greater than 64]
        validateDeviceAuthenticationKey(primaryKey);
        // Codes_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_002: [The function shall set the private primaryKey member to the given value if the length validation passed]
        this.primaryKey = primaryKey;
    }

    /**
     * Getter for secondary key
     * @return Secondary key part of the symmetric key
     */
    public String getSecondaryKey()
    {
        return secondaryKey;
    }

    /**
     * Setter for secondary key
     * Validates the length of the key
     *
     * @param secondaryKey Secondary key part of the symmetric key
     */
    public final void setSecondaryKey(String secondaryKey)
    {
        // Codes_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_003: [The function shall throw IllegalArgumentException if the length of the key less than 16 or greater than 64]
        validateDeviceAuthenticationKey(secondaryKey);
        // Codes_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_003: [The function shall throw IllegalArgumentException if the length of the key less than 16 or greater than 64]
        this.secondaryKey = secondaryKey;
    }

    /**
     * Validate the length of the key
     * @param key The key to validate
     * @throws IllegalArgumentException if the key has an invalid length
     */
    private void validateDeviceAuthenticationKey(String key) throws IllegalArgumentException
    {
        if (key != null)
        {
            if ((key.length() < MinKeyLengthInBytes) || (key.length() > MaxKeyLengthInBytes))
            {
                throw new IllegalArgumentException(DeviceKeyLengthInvalid);
            }
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof SymmetricKey)
        {
            SymmetricKey otherSymmetricKey = (SymmetricKey) other;
            return (Tools.areEqual(this.getPrimaryKey(), otherSymmetricKey.getPrimaryKey())
                    && Tools.areEqual(this.getSecondaryKey(), otherSymmetricKey.getSecondaryKey()));
        }

        return false;
    }
}
