/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.device.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasToken;
import com.microsoft.azure.sdk.iot.device.auth.IotHubSasTokenAuthentication;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

/**
 * Unit tests for IotHubSasTokenAuthentication.java
 * Methods: 100%
 * Lines: 91%
 */
public class IotHubSasTokenAuthenticationTest
{
    @Mocked
    SSLContext mockSSLContext;

    @Mocked
    IotHubSSLContext mockIotHubSSLContext;

    @Mocked
    IotHubSasToken mockSasToken;

    private static String expectedDeviceId = "deviceId";
    private static String expectedHostname = "hostname";
    private static String expectedDeviceKey = "deviceKey";
    private static String expectedSasToken = "sasToken";
    private static long expectedExpiryTime = 3600;

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_004: [If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.]
    @Test
    public void getRenewedSasTokenAutoRenews() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new Expectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken, expectedExpiryTime);
                result = mockSasToken;
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        String actualSasToken = sasAuth.getRenewedSasToken();

        //assert
        assertNotEquals(mockSasToken.toString(), actualSasToken);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_005: [This function shall return the saved sas token.]
    @Test
    public void getSasTokenReturnsSavedValue() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new Expectations()
        {
            {
                Deencapsulation.newInstance(IotHubSasToken.class, new Class[] {String.class, String.class, String.class, String.class, long.class}, expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken, expectedExpiryTime);
                result = mockSasToken;
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        String actualSasToken = sasAuth.getRenewedSasToken();

        //assert
        assertEquals(mockSasToken.toString(), actualSasToken);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_008: [This function shall return the generated IotHubSSLContext.]
    @Test
    public void getIotHubSSLContextGets() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        Deencapsulation.setField(sasAuth, "iotHubSSLContext", mockIotHubSSLContext);

        //act
        SSLContext actualSSLContext = sasAuth.getSSLContext();

        //assert
        assertEquals(mockSSLContext, actualSSLContext);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
    @Test
    public void setTokenValidSecsSets() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        long newTokenValidSecs = 5000L;
        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        sasAuth.setTokenValidSecs(newTokenValidSecs);

        //assert
        long actualTokenValidSecs = Deencapsulation.getField(sasAuth, "tokenValidSecs");
        assertEquals(newTokenValidSecs, actualTokenValidSecs);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsTrueWhenTokenHasExpiredAndNoDeviceKeyIsPresent() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, null, expectedSasToken);

        //act
        boolean needsToRenew = sasAuth.isRenewalNecessary();

        //assert
        assertTrue(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsFalseDeviceKeyPresent() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = true;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        boolean needsToRenew = sasAuth.isRenewalNecessary();

        //assert
        assertFalse(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
    @Test
    public void isRenewalNecessaryReturnsFalseWhenTokenHasNotExpired() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockSasToken, "isExpired");
                result = false;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        boolean needsToRenew = sasAuth.isRenewalNecessary();

        //assert
        assertFalse(needsToRenew);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [This function shall return the current sas token without renewing it.]
    @Test
    public void getCurrentSasTokenGetsCurrentToken()
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockSasToken.toString();
                result = expectedSasToken;
            }
        };

        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        String actualCurrentSasToken = sasAuth.getCurrentSasToken();

        //assert
        assertEquals(expectedSasToken, actualCurrentSasToken);
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCert() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        final String expectedCert = "someTrustedCert";
        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        sasAuth.setIotHubTrustedCert(expectedCert);

        //act
        Deencapsulation.invoke(sasAuth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, expectedCert, false);
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.]
    @Test
    public void generateSSLContextUsesSavedTrustedCertPath() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        final String expectedCertPath = "someTrustedCertPath";
        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        sasAuth.setPathToIotHubTrustedCert(expectedCertPath);

        //act
        Deencapsulation.invoke(sasAuth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class, new Class[] {String.class, boolean.class}, expectedCertPath, true);
                times = 1;
            }
        };
    }

    // Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.]
    @Test
    public void generateSSLContextGeneratesDefaultIotHubSSLContext() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException
    {
        //arrange
        IotHubSasTokenAuthentication sasAuth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);

        //act
        Deencapsulation.invoke(sasAuth, "generateSSLContext");

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(IotHubSSLContext.class);
                times = 1;
            }
        };
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_059: [This function shall save the provided pathToCertificate.]
    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setPathToCertificateWorks() throws IOException
    {
        //arrange
        IotHubSasTokenAuthentication auth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        String pathToCert = "somePath";

        //act
        auth.setPathToIotHubTrustedCert(pathToCert);

        //assert
        String actualPathToCert = Deencapsulation.getField(auth, "pathToIotHubTrustedCert");
        assertEquals(pathToCert, actualPathToCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }

    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_064: [This function shall save the provided userCertificateString.]
    //Tests_SRS_IOTHUBSASTOKENAUTHENTICATION_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
    @Test
    public void setCertificateWorks() throws IOException
    {
        //arrange
        IotHubSasTokenAuthentication auth = new IotHubSasTokenAuthentication(expectedHostname, expectedDeviceId, expectedDeviceKey, expectedSasToken);
        String cert = "somePath";

        //act
        auth.setIotHubTrustedCert(cert);

        //assert
        String actualCert = Deencapsulation.getField(auth, "iotHubTrustedCert");
        assertEquals(cert, actualCert);
        boolean sslContextNeedsRenewal = Deencapsulation.getField(auth, "sslContextNeedsUpdate");
        assertTrue(sslContextNeedsRenewal);
    }
}
