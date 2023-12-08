/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.apache.commons.codec.binary.Base32;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

/*
     Unit tests for SecurityProviderTpm and SecurityProvider
     Coverage :
     SecurityProvider : 100% lines, 100% methods
     SecurityProviderTpm : 95% lines, 100% methods

 */
public class SecurityProviderSymmetricKeyTest
{
    private static final byte[] testSymKey = "symmkey".getBytes(StandardCharsets.UTF_8);
    private static final String testRegId = "regId";

    private static final String testPrimaryKey = "12345";
    private static final String testSecondaryKey = "6789";

    @Mocked
    SecretKeySpec mockedSecretKeySpec;

    @Mocked
    Mac mockedMac;

    @Mocked
    MessageDigest mockedMessageDigest;

    @Mocked
    Base32 mockedBase32;

    @Mocked
    SSLContext mockedSslContext;

    @Mocked
    KeyStore mockedKeyStore;

    @Mocked
    TrustManagerFactory mockedTrustManagerFactory;

    @Mocked
    UUID mockedUUID;

    @Test
    public void testConstructorSucceeds() throws SecurityProviderException
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //assert
        assertEquals(securityProviderSymmetricKey.getSymmetricKey(), testSymKey);
        assertEquals(securityProviderSymmetricKey.getRegistrationId(), testRegId);
    }

    @Test
    public void testConstructorWithBothKeysSucceeds() throws SecurityProviderException
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testPrimaryKey, testSecondaryKey, testRegId);

        //assert
        assertEquals(testPrimaryKey, new String(securityProviderSymmetricKey.getSymmetricKey(), StandardCharsets.UTF_8));
        assertEquals(testSecondaryKey, new String(securityProviderSymmetricKey.getSecondaryKey(), StandardCharsets.UTF_8));
        assertEquals(testRegId, securityProviderSymmetricKey.getRegistrationId());
    }


    @Test (expected = IllegalArgumentException.class)
    public void testSymmetrickeyNull()
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(null, testRegId);
        //assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithBothKeysThrowsIfPrimaryNull()
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(null, testSecondaryKey, testRegId);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithBothKeysThrowsIfPrimaryEmpty()
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey("", testSecondaryKey, testRegId);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithBothKeysThrowsIfSecondaryNull()
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testPrimaryKey, null, testRegId);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorWithBothKeysThrowsIfSecondaryEmpty()
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testPrimaryKey, "", testRegId);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testRegIdNull()
    {
        //act
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, null);
        //assert
    }

    @Test
    public void testSignData() throws SecurityProviderException
    {
        final String TEST_SIGNATURE = "testSignature";

        // Semmle flags this as sensitive call, but it is a false positive since it is for test purposes
        final String TEST_BASE64_DECODED_KEY = "base64DecodedKey"; //lgtm

        final String HMAC_SHA_256 = "HmacSHA256";

        //arrange
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //act
        securityProviderSymmetricKey.HMACSignData(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8), TEST_BASE64_DECODED_KEY.getBytes(StandardCharsets.UTF_8));
        //assert
        new Verifications()
        {
            {
                new SecretKeySpec((byte[]) any, HMAC_SHA_256);
                times = 1;
                mockedMac.doFinal(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8));
                times = 1;
            }
        };

    }

    @Test (expected = SecurityProviderException.class)
    public void testSignDataThrowsSecurityProviderExceptionOnNullKey() throws SecurityProviderException
    {
        final String TEST_SIGNATURE = "testSignature";
        final String TEST_BASE64_DECODED_KEY = "";
        final String HMAC_SHA_256 = "HmacSHA256";
        //arrange
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //act
        securityProviderSymmetricKey.HMACSignData(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8), null);
        //assert
        new Verifications()
        {
            {
                new SecretKeySpec((byte[]) any, HMAC_SHA_256);
                times = 1;
                mockedMac.doFinal(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8));
                times = 1;
            }
        };
    }

    @Test (expected = SecurityProviderException.class)
    public void testSignDataThrowsSecurityProviderExceptionOnEmptyKey() throws SecurityProviderException
    {
        final String TEST_SIGNATURE = "testSignature";
        final String TEST_BASE64_DECODED_KEY = "";
        final String HMAC_SHA_256 = "HmacSHA256";
        //arrange
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //act
        securityProviderSymmetricKey.HMACSignData(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8), "".getBytes(StandardCharsets.UTF_8));
        //assert
        new Verifications()
        {
            {
                new SecretKeySpec((byte[]) any, HMAC_SHA_256);
                times = 1;
                mockedMac.doFinal(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8));
                times = 1;
            }
        };
    }

    @Test (expected = SecurityProviderException.class)
    public void testSignDataThrowsSecurityProviderExceptionOnNullSignature() throws SecurityProviderException
    {
        final String TEST_SIGNATURE = "testSignature";
        final String TEST_BASE64_DECODED_KEY = "";
        final String HMAC_SHA_256 = "HmacSHA256";
        //arrange
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //act
        securityProviderSymmetricKey.HMACSignData(null, TEST_BASE64_DECODED_KEY.getBytes(StandardCharsets.UTF_8));
        //assert
        new Verifications()
        {
            {
                new SecretKeySpec((byte[]) any, HMAC_SHA_256);
                times = 1;
                mockedMac.doFinal(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8));
                times = 1;
            }
        };
    }

    @Test (expected = SecurityProviderException.class)
    public void testSignDataThrowsSecurityProviderExceptionOnEmptySignature() throws SecurityProviderException
    {
        final String TEST_SIGNATURE = "testSignature";
        final String TEST_BASE64_DECODED_KEY = "";
        final String HMAC_SHA_256 = "HmacSHA256";
        //arrange
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //act
        securityProviderSymmetricKey.HMACSignData("".getBytes(StandardCharsets.UTF_8), TEST_BASE64_DECODED_KEY.getBytes(StandardCharsets.UTF_8));
        //assert
        new Verifications()
        {
            {
                new SecretKeySpec((byte[]) any, HMAC_SHA_256);
                times = 1;
                mockedMac.doFinal(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8));
                times = 1;
            }
        };
    }

    @Test (expected = SecurityProviderException.class)
    public void testSignDataThrowsSecurityProviderExceptionOnInvalidKey() throws SecurityProviderException, InvalidKeyException
    {
        final String TEST_SIGNATURE = "testSignature";

        // Semmle flags this as sensitive call, but it is a false positive since it is for test purposes
        final String TEST_BASE64_DECODED_KEY = "InvalidKey"; // lgtm

        final String HMAC_SHA_256 = "HmacSHA256";
        new Expectations()
        {
            {
                mockedMac.init((Key) any);
                result = new InvalidKeyException();
            }
        };
        //arrange
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);
        //act
        securityProviderSymmetricKey.HMACSignData(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8), TEST_BASE64_DECODED_KEY.getBytes(StandardCharsets.UTF_8));
        //assert
        new Verifications()
        {
            {
                new SecretKeySpec((byte[]) any, HMAC_SHA_256);
                times = 1;
                mockedMac.doFinal(TEST_SIGNATURE.getBytes(StandardCharsets.UTF_8));
                times = 0;
            }
        };
    }

    @Test
    public void getSSLContextSucceeds() throws SecurityProviderException, KeyManagementException, NoSuchAlgorithmException
    {
        //arrange
        new Expectations()
        {
            {
                SSLContext.getInstance("TLSv1.2");
                result = mockedSslContext;

                mockedSslContext.init(null, null, (SecureRandom) any);
            }
        };

        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, testRegId);

        //act
        securityProviderSymmetricKey.getSSLContext();
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructorThrowsForInvalidChar() throws SecurityProviderException
    {
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, "some/invalidRegistrationId");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructorThrowsForInvalidChar2() throws SecurityProviderException
    {
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(testSymKey, "some\\invalidRegistrationId");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructorThrowsForInvalidChar3() throws SecurityProviderException
    {
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(new byte[10], "some/invalidRegistrationId");
    }

    @Test (expected = IllegalArgumentException.class)
    public void testConstructorThrowsForInvalidChar4() throws SecurityProviderException
    {
        SecurityProviderSymmetricKey securityProviderSymmetricKey = new SecurityProviderSymmetricKey(new byte[10], "some\\invalidRegistrationId");
    }
}
