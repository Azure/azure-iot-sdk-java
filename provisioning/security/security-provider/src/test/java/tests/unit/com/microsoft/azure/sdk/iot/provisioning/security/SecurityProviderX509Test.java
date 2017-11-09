/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.security;

import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderX509;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityClientException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.LinkedList;

import static org.junit.Assert.assertEquals;

/*
 *  Unit tests for SecurityProviderX509 and SecurityProvider
 *  Coverage : 97% lines, 100% methods
 */
public class SecurityProviderX509Test
{
    private static final String TEST_COMMON_NAME = "testCN";

    @Mocked
    X509Certificate mockedX509Certificate;

    @Mocked
    Key mockedKey;

    @Mocked
    SSLContext mockedSslContext;

    @Mocked
    KeyStore mockedKeyStore;

    @Mocked
    CertificateFactory mockedCertificateFactory;

    @Mocked
    TrustManagerFactory mockedTrustManagerFactory;

    @Mocked
    TrustManager mockedTrustManager;

    @Mocked
    X509TrustManager mockedX509TrustManager;

    @Mocked
    KeyManagerFactory mockedKeyManagerFactory;

    @Mocked
    KeyManager mockedKeyManager;

    @Mocked
    X509KeyManager mockedX509KeyManager;

    class SecurityProviderX509TestImpl extends SecurityProviderX509
    {
        private String cn;
        private X509Certificate x509Certificate;
        private Key key;
        private Collection<X509Certificate> certificates;

        SecurityProviderX509TestImpl(String cn, X509Certificate x509Certificate, Key key, Collection<X509Certificate> certificates)
        {
            this.cn = cn;
            this.x509Certificate = x509Certificate;
            this.key = key;
            this.certificates = certificates;
        }

        @Override
        public String getClientCertificateCommonName()
        {
            return cn;
        }

        @Override
        public X509Certificate getClientCertificate()
        {
            return x509Certificate;
        }

        @Override
        public Key getClientPrivateKey()
        {
            return key;
        }

        @Override
        public Collection<X509Certificate> getIntermediateCertificatesChain()
        {
            return certificates;
        }

        @Override
        public String getRegistrationId() throws SecurityClientException
        {
            return super.getRegistrationId();
        }

        @Override
        public SSLContext getSSLContext() throws SecurityClientException
        {
            return super.getSSLContext();
        }
    }

    //SRS_SecurityClientX509_25_001: [ This method shall retrieve the commonName of the client certificate and return as registration Id. ]
    @Test
    public void getRegistrationIdSucceeds() throws SecurityClientException
    {

        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);
        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, mockedKey, certificates);

        assertEquals(TEST_COMMON_NAME, securityClientX509Test.getRegistrationId());
    }

    //SRS_SecurityClientX509_25_002: [ This method shall generate the SSL context. ]
    //SRS_SecurityClientX509_25_007: [ This method shall use random UUID as a password for keystore. ]
    //SRS_SecurityClientX509_25_008: [ This method shall create a TLSv1.2 instance. ]
    //SRS_SecurityClientX509_25_009: [ This method shall retrieve the keystore loaded with trusted certs. ]
    //SRS_SecurityClientX509_25_010: [ This method shall load all the provided X509 certs (leaf with both public certificate and private key,
    // intermediate certificates(if any) to the Key store. ]
    //SRS_SecurityClientX509_25_011: [ This method shall initialize the ssl context with X509KeyManager and X509TrustManager for the keystore. ]
    //SRS_SecurityClientX509_25_012: [ This method shall return the ssl context created as above to the caller. ]
    //SRS_SecurityClient_25_001: [ This method shall retrieve the default instance of keystore using default algorithm type. ]
    //SRS_SecurityClient_25_002: [ This method shall retrieve the default CertificateFactory instance. ]
    //SRS_SecurityClient_25_003: [ This method shall load all the trusted certificates to the keystore. ]
    @Test
    public void getSslContextSucceeds() throws SecurityClientException, KeyManagementException, KeyStoreException
    {
        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);

        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, mockedKey, certificates);
        new NonStrictExpectations()
        {
            {
                mockedKeyManagerFactory.getKeyManagers();
                result = mockedX509KeyManager;
                mockedTrustManagerFactory.getTrustManagers();
                result = mockedX509TrustManager;
            }
        };

        //act
        securityClientX509Test.getSSLContext();

        //assert
        new Verifications()
        {
            {
                mockedKeyStore.setKeyEntry(anyString, mockedKey,  (char[]) any, (X509Certificate[]) any );
                times = 1;
                mockedSslContext.init((KeyManager[]) any, (TrustManager[]) any, (SecureRandom) any);
                times = 1;
            }
        };
    }

    //SRS_SecurityClientX509_25_006: [ This method shall throw IllegalArgumentException if input parameters are null. ]
    @Test (expected = IllegalArgumentException.class)
    public void getSslContextThrowsOnNullLeaf() throws SecurityClientException, KeyManagementException, KeyStoreException
    {
        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);

        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, null, mockedKey, certificates);

        //act
        securityClientX509Test.getSSLContext();
    }


    @Test (expected = IllegalArgumentException.class)
    public void getSslContextThrowsOnNullPrivateKey() throws SecurityClientException, KeyManagementException, KeyStoreException
    {
        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);

        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, null, certificates);

        //act
        securityClientX509Test.getSSLContext();
    }

    @Test (expected = IllegalArgumentException.class)
    public void getSslContextThrowsOnNullIntermediates() throws SecurityClientException, KeyManagementException, KeyStoreException
    {
        //arrange
        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, mockedKey, null);

        //act
        securityClientX509Test.getSSLContext();

    }

    //SRS_SecurityClientX509_25_005: [ This method shall throw SecurityClientException if X509 Key Manager is not found. ]
    @Test (expected = SecurityClientException.class)
    public void getSslContextThrowsIfX509KeyManagerNotFound() throws SecurityClientException, KeyManagementException, KeyStoreException
    {

        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);

        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, mockedKey, certificates);
        new NonStrictExpectations()
        {
            {
                mockedKeyManagerFactory.getKeyManagers();
                result = mockedKeyManager; // not necessarily X509
            }
        };

        //act
        securityClientX509Test.getSSLContext();
    }

    //SRS_SecurityClientX509_25_004: [ This method shall throw SecurityClientException if X509 Trust Manager is not found. ]
    @Test (expected = SecurityClientException.class)
    public void getSslContextThrowsIfX509TrustManagerNotFound() throws SecurityClientException, KeyManagementException, KeyStoreException
    {
        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);

        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, mockedKey, certificates);
        new NonStrictExpectations()
        {
            {
                mockedKeyManagerFactory.getKeyManagers();
                result = mockedX509KeyManager;
                mockedTrustManagerFactory.getTrustManagers();
                result = mockedTrustManager; // not necessarily X509
            }
        };

        //act
        securityClientX509Test.getSSLContext();
    }

    //SRS_SecurityClientX509_25_003: [ This method shall throw SecurityClientException chained with the exception thrown from underlying API calls to SSL library. ]
    @Test (expected = SecurityClientException.class)
    public void getSslContextThrowsIfAnyOfTheUnderlyingAPIFails() throws SecurityClientException, KeyManagementException, KeyStoreException
    {
        //arrange
        Collection<X509Certificate> certificates = new LinkedList<>();
        certificates.add(mockedX509Certificate);

        SecurityProviderX509 securityClientX509Test = new SecurityProviderX509TestImpl(TEST_COMMON_NAME, mockedX509Certificate, mockedKey, certificates);
        new NonStrictExpectations()
        {
            {
                mockedKeyManagerFactory.getKeyManagers();
                result = mockedX509KeyManager;
                mockedTrustManagerFactory.getTrustManagers();
                result = mockedX509TrustManager;
                mockedSslContext.init((KeyManager[]) any, (TrustManager[]) any, (SecureRandom) any);
                result = new KeyManagementException();
            }
        };

        //act
        securityClientX509Test.getSSLContext();
    }
}
