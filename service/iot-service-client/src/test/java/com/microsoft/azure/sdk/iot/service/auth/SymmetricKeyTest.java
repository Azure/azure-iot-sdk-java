/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import com.microsoft.azure.sdk.iot.service.auth.SymmetricKey;
import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Code coverage:
 * 100% Methods, 100% lines
 */
@RunWith(JMockit.class)
public class SymmetricKeyTest
{
    // Tests_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_002: [The function shall set the private primaryKey member to the given value if the length validation passed]
    @Test
    public void setPrimaryKey_length_good_case_min()
    {
        // Arrange
        String key = "0123456789012345";
        SymmetricKey symmetricKey = new SymmetricKey();
        // Act
        symmetricKey.setPrimaryKey(key);
        // Assert
        assertEquals(key, Deencapsulation.getField(symmetricKey, "primaryKey"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_002: [The function shall set the private primaryKey member to the given value if the length validation passed]
    @Test
    public void setPrimaryKey_length_good_case_max()
    {
        // Arrange
        String key = "0123456789012345678901234567890123456789012345678901234567890123";
        SymmetricKey symmetricKey = new SymmetricKey();
        // Act
        symmetricKey.setPrimaryKey(key);
        // Assert
        assertEquals(key, Deencapsulation.getField(symmetricKey, "primaryKey"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_003: [The function shall throw IllegalArgumentException if the length of the key less than 16 or greater than 64]
    @Test
    public void setSecondaryKey_length_good_case_min()
    {
        // Arrange
        String key = "0123456789012345";
        SymmetricKey symmetricKey = new SymmetricKey();
        // Act
        symmetricKey.setSecondaryKey(key);
        // Assert
        assertEquals(key, Deencapsulation.getField(symmetricKey, "secondaryKey"));
    }

    // Tests_SRS_SERVICE_SDK_JAVA_SYMMETRICKEY_12_003: [The function shall throw IllegalArgumentException if the length of the key less than 16 or greater than 64]
    @Test
    public void setSecondaryKey_length_good_case_max()
    {
        // Arrange
        String key = "0123456789012345678901234567890123456789012345678901234567890123";
        SymmetricKey symmetricKey = new SymmetricKey();
        // Act
        symmetricKey.setSecondaryKey(key);
        // Assert
        assertEquals(key, Deencapsulation.getField(symmetricKey, "secondaryKey"));
    }

    /**
     * Creates a symmetric key and uses reflection to set the primary and secondary keys
     * @param primaryKey the primary key value to set
     * @param secondaryKey the secondary key value to set
     * @return the created SymmetricKey
     */
    private SymmetricKey createTestSymmetricKey(String primaryKey, String secondaryKey)
    {
        SymmetricKey key = new SymmetricKey();
        Deencapsulation.setField(key, "primaryKey", primaryKey);
        Deencapsulation.setField(key, "secondaryKey", secondaryKey);
        return key;
    }
}
