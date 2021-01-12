/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderTpm;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base32;
import org.junit.Test;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/*
     Unit tests for SecurityProviderTpm and SecurityProvider
     Coverage :
     SecurityProvider : 100% lines, 100% methods
     SecurityProviderTpm : 95% lines, 100% methods

 */
public class SecurityProviderTpmTest
{
    private static final byte[] ENROLLMENT_KEY = "testEk".getBytes();
    private static final byte[] STORAGE_ROOT_KEY = "testSRK".getBytes();
    private static final byte[] TEST_SIGN_DATA = "testSignData".getBytes();
    private static final byte[] TEST_ACTIVATION = "testActivation".getBytes();

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

    class SecurityProviderTPMTestImpl extends SecurityProviderTpm
    {
        byte[] ek;
        SecurityProviderTPMTestImpl(byte[] ek)
        {
            this.ek = ek;
        }

        @Override
        public byte[] activateIdentityKey(byte[] key) throws SecurityProviderException
        {
            return TEST_ACTIVATION;
        }

        @Override
        public byte[] getEndorsementKey() throws SecurityProviderException
        {
            return ek;
        }

        @Override
        public byte[] getStorageRootKey() throws SecurityProviderException
        {
            return STORAGE_ROOT_KEY;
        }

        @Override
        public byte[] signWithIdentity(byte[] data) throws SecurityProviderException
        {
            return TEST_SIGN_DATA;
        }

        @Override
        public String getRegistrationId() throws SecurityProviderException
        {
            return super.getRegistrationId();
        }

        @Override
        public SSLContext getSSLContext() throws SecurityProviderException
        {
            return super.getSSLContext();
        }
    }

    //SRS_SecurityClientTpm_25_001: [ This method shall retrieve the EnrollmentKey from the implementation of this abstract class. ]
    //SRS_SecurityClientTpm_25_002: [ This method shall hash the EnrollmentKey using SHA-256. ]
    //SRS_SecurityClientTpm_25_003: [ This method shall convert the resultant hash to Base32 to convert all the data to be case agnostic and remove "=" from the string. ]
    @Test
    public void getRegistrationIdSucceeds() throws SecurityProviderException, EncoderException
    {
        //arrange
        SecurityProviderTpm securityClientTpm = new SecurityProviderTPMTestImpl(ENROLLMENT_KEY);
        //act
        String actualRegistrationId = securityClientTpm.getRegistrationId();
        //assert
        assertNotNull(actualRegistrationId);
        assertEquals(actualRegistrationId, actualRegistrationId.toLowerCase());
        assertFalse(actualRegistrationId.contains("="));
        new Verifications()
        {
            {
                mockedMessageDigest.digest(ENROLLMENT_KEY);
                times = 1;
                mockedBase32.encode((byte[]) any);
                times = 1;
            }
        };
    }

    //SRS_SecurityClientTpm_25_008: [ This method shall throw SecurityProviderException if any of the underlying API's in generating registration Id. ]
    @Test (expected = SecurityProviderException.class)
    public void getRegistrationIdThrowsSecurityClientException() throws SecurityProviderException, NoSuchAlgorithmException
    {
        //arrange
        SecurityProviderTpm securityClientTpm = new SecurityProviderTPMTestImpl(ENROLLMENT_KEY);
        new NonStrictExpectations()
        {
            {
                MessageDigest.getInstance("SHA-256");
                result = new NoSuchAlgorithmException();
            }
        };

        //act
        securityClientTpm.getRegistrationId();
    }

    //SRS_SecurityClientTpm_25_004: [ This method shall generate SSLContext for this flow. ]
    //SRS_SecurityClientTpm_25_006: [ This method shall load the keystore with TrustedCerts. ]
    //SRS_SecurityClientTpm_25_007: [ This method shall initialize SSLContext with the default trustManager loaded with keystore. ]
    //SRS_SecurityClient_25_001: [ This method shall retrieve the default instance of keystore using default algorithm type. ]
    //SRS_SecurityClient_25_002: [ This method shall retrieve the default CertificateFactory instance. ]
    //SRS_SecurityClient_25_003: [ This method shall load all the trusted certificates to the keystore. ]
    @Test
    public void getSSLContextSucceeds() throws SecurityProviderException, KeyManagementException, KeyStoreException, CertificateException
    {
        //arrange
        SecurityProviderTpm securityClientTpm = new SecurityProviderTPMTestImpl(ENROLLMENT_KEY);

        //act
        securityClientTpm.getSSLContext();

        new Verifications()
        {
            {
                mockedKeyStore.setCertificateEntry(anyString, (Certificate) any);
                times = 3;
                mockedSslContext.init((KeyManager[]) any, (TrustManager[]) any, (SecureRandom) any);
                times = 1;
            }
        };
    }

    //SRS_SecurityClientTpm_25_005: [ This method shall throw SecurityProviderException if any of the underlying API's in generating SSL context fails. ]
    @Test (expected = SecurityProviderException.class)
    public void getSSLContextThrowsUnderlyingException() throws SecurityProviderException, KeyStoreException
    {
        //arrange
        SecurityProviderTpm securityClientTpm = new SecurityProviderTPMTestImpl(ENROLLMENT_KEY);
        new NonStrictExpectations()
        {
            {
                mockedKeyStore.setCertificateEntry(anyString, (Certificate) any);
                result = new KeyStoreException();
            }
        };

        //act
        securityClientTpm.getSSLContext();
    }
}
