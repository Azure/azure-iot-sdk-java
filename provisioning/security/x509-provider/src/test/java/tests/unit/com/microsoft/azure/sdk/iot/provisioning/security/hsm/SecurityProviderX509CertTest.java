 /*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */



package tests.unit.com.microsoft.azure.sdk.iot.provisioning.security.hsm;

 import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;
 import mockit.Deencapsulation;
 import mockit.Mocked;
 import mockit.NonStrictExpectations;
 import org.bouncycastle.openssl.PEMKeyPair;
 import org.bouncycastle.openssl.PEMParser;
 import org.bouncycastle.util.io.pem.PemObject;
 import org.bouncycastle.util.io.pem.PemReader;
 import org.junit.Test;

 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;

 import static org.junit.Assert.assertEquals;

 public class SecurityProviderX509CertTest
{
    private static final String expectedPrivateKeyString = "some private key string";
    private static final String expectedPublicKeyCertificateString = "some public key certificate string";

    @Mocked PrivateKey mockedPrivateKey;
    @Mocked X509Certificate mockedX509Certificate;
    @Mocked PEMKeyPair mockedPEMKeyPair;
    @Mocked PEMParser mockedPEMParser;
    @Mocked PemObject mockedPemObject;
    @Mocked PemReader mockedPemReader;
    @Mocked StringReader mockedStringReader;
    @Mocked KeyPair mockedKeyPair;
    @Mocked CertificateFactory mockedCertificateFactory;

    // Tests_SRS_SecurityClientDiceEmulator_34_001: [This function shall return a Private Key instance created by the provided PEM formatted privateKeyString.]
    @Test
    public void parsePrivateKeySuccess() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations(SecurityProviderX509Cert.class)
        {
            {
                new StringReader(expectedPrivateKeyString);
                result = mockedStringReader;

                new PEMParser(mockedStringReader);
                result = mockedPEMParser;

                mockedPEMParser.readObject();
                result = mockedPEMKeyPair;

                //Doing this instead of just mocking JCA converter because trying to mock the JCA converter causes strange errors to be thrown.
                Deencapsulation.invoke(SecurityProviderX509Cert.class, "getPrivateKeyFromPEMKeyPair", new Class[] {PEMKeyPair.class}, mockedPEMKeyPair);
                result = mockedPrivateKey;
            }
        };

        //act
        PrivateKey actualPrivateKey = Deencapsulation.invoke(SecurityProviderX509Cert.class, "parsePrivateKey", new Class[] {String.class}, expectedPrivateKeyString);

        //assert
        assertEquals(mockedPrivateKey, actualPrivateKey);
    }

    // Tests_SRS_SecurityClientDiceEmulator_34_002: [If any exception is encountered while attempting to create the private key instance, this function shall throw a CertificateException.]
    @Test (expected = CertificateException.class)
    public void parsePrivateKeyExceptionsWrappedInCertificateException() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new StringReader(expectedPrivateKeyString);
                result = new IOException();
            }
        };

        //act
        PrivateKey actualPrivateKey = Deencapsulation.invoke(SecurityProviderX509Cert.class, "parsePrivateKey", new Class[] {String.class}, expectedPrivateKeyString);
    }

    // Tests_SRS_SecurityClientDiceEmulator_34_003: [This function shall return an X509Certificate instance created by the provided PEM formatted publicKeyCertificateString.]
    @Test
    public void parsePublicKeyCertificateSuccess() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new PemReader(new StringReader(expectedPublicKeyCertificateString));
                result = mockedPemReader;

                mockedPemReader.readPemObject();
                result = mockedPemObject;

                CertificateFactory.getInstance("X.509");
                result = mockedCertificateFactory;

                mockedCertificateFactory.generateCertificate(new ByteArrayInputStream(mockedPemObject.getContent()));
                result = mockedX509Certificate;
            }
        };

        //act
        X509Certificate actualPublicKeyCertificate = Deencapsulation.invoke(SecurityProviderX509Cert.class, "parsePublicKeyCertificate", new Class[] {String.class}, expectedPublicKeyCertificateString);

        //assert
        assertEquals(mockedX509Certificate, actualPublicKeyCertificate);
    }

    // Tests_SRS_SecurityClientDiceEmulator_34_004: [If any exception is encountered while attempting to create the public key certificate instance, this function shall throw a CertificateException.]
    @Test (expected = CertificateException.class)
    public void parsePublicKeyCertificateExceptionsWrappedInCertificateException() throws CertificateException, IOException
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                new PemReader(new StringReader(expectedPublicKeyCertificateString));
                result = new IOException();
            }
        };

        //act
        X509Certificate actualPublicKeyCertificate = Deencapsulation.invoke(SecurityProviderX509Cert.class, "parsePublicKeyCertificate", new Class[] {String.class}, expectedPublicKeyCertificateString);

        //assert
        assertEquals(mockedX509Certificate, actualPublicKeyCertificate);
    }
}
