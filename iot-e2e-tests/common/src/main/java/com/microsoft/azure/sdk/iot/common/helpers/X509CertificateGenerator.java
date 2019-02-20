/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Utility class for generating self signed certificates, and computing their thumprints
 */
public class X509CertificateGenerator
{
    private String x509Thumbprint;
    private X509Certificate x509Certificate;
    private String publicCertificate;
    private String privateKey;

    /**
     * Constructor that generates a new self signed x509 certificate. Public certificate, private key, thumbprint, and the complete
     * certificate can be accessed by getters. No common name is given to the certificate.
     */
    public X509CertificateGenerator()
    {
        try
        {
            this.generateCertificate();
        }
        catch (Exception e)
        {
            System.out.println(Tools.getStackTraceFromThrowable(e));
        }
    }

    /**
     * Constructor that generates a new self signed x509 certificate. Public certificate, private key, thumbprint, and the complete
     * certificate can be accessed by getters. The created certificate will have the provided common name.
     */
    public X509CertificateGenerator(String commonName)
    {
        try
        {
            this.generateCertificate(commonName);
        }
        catch (Exception e)
        {
            System.out.println(Tools.getStackTraceFromThrowable(e));
        }
    }

    /**
     * generate a new certificate
     */
    private void generateCertificate() throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, SignatureException, NoSuchProviderException, InvalidKeyException
    {
        generateCertificate(null);
    }

    /**
     * generate a new certificate using the provided common name
     */
    private void generateCertificate(String commonName) throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, SignatureException, NoSuchProviderException, InvalidKeyException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024, new SecureRandom());
        KeyPair keypair = keyGen.generateKeyPair();
        this.x509Certificate = createX509CertificateFromKeyPair(keypair, commonName);
        this.x509Thumbprint = new String(Hex.encodeHex(DigestUtils.sha(x509Certificate.getEncoded())));
        this.publicCertificate = getPublicCertificatePem(x509Certificate);
        this.privateKey = getPrivateKeyPem(keypair.getPrivate());
    }

    /**
     * @return the thumbprint for the the generated x509 certificate
     */
    public String getX509Thumbprint()
    {
        return x509Thumbprint;
    }

    /**
     * @return the complete x509 certificate that was generated
     */
    public X509Certificate getX509Certificate()
    {
        return x509Certificate;
    }

    /**
     * @return the Pem formatted public certificate for the generated certificate key pair
     */
    public String getPublicCertificate()
    {
        return publicCertificate;
    }

    /**
     * @return the Pem formatted private key for the generated certificate key pair
     */
    public String getPrivateKey()
    {
        return privateKey;
    }

    /**
     * Create a new self signed x509 certificate without specifying a common name
     */
    private static X509Certificate createX509CertificateFromKeyPair(KeyPair keyPair) throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, SignatureException, NoSuchProviderException, InvalidKeyException
    {
        return createX509CertificateFromKeyPair(keyPair, null);
    }

    /**
     * Create a new self signed x509 certificate with the specified common name
     */
    private static X509Certificate createX509CertificateFromKeyPair(KeyPair keyPair, String commonName)
            throws OperatorCreationException, CertificateException, InvalidKeyException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException
    {
        String issuerString = "C=US, O=Microsoft, L=Redmond, OU=Azure";
        // subjects name - the same as we are self signed.
        String subjectString = "C=US, O=Microsoft, L=Redmond, OU=Azure";

        if (commonName != null && !commonName.isEmpty())
        {
            issuerString += ", CN=" +commonName;
            subjectString += ", CN=" +commonName;
        }

        X500Name issuer = new X500Name( issuerString );
        BigInteger serial = BigInteger.ONE;
        Date notBefore = new Date();
        Date notAfter = new Date( System.currentTimeMillis() + ( 60*60*1000 ) );
        X500Name subject = new X500Name( subjectString );
        PublicKey publicKey = keyPair.getPublic();
        JcaX509v3CertificateBuilder v3Bldr = new JcaX509v3CertificateBuilder(issuer,
                serial,
                notBefore,
                notAfter,
                subject,
                publicKey);
        X509CertificateHolder certHldr = v3Bldr
                .build( new JcaContentSignerBuilder( "SHA1WithRSA" ).build( keyPair.getPrivate() ) );
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate( certHldr );
        cert.checkValidity(new Date());
        cert.verify(keyPair.getPublic());
        return cert;
    }

    private static String getPrivateKeyPem(PrivateKey privateKey)
    {
        return "-----BEGIN PRIVATE KEY-----\n" +
                Base64.encodeBase64StringLocal(privateKey.getEncoded()) +
                "\n-----END PRIVATE KEY-----\n";
    }

    private static String getPublicCertificatePem(X509Certificate certificate) throws CertificateEncodingException {
        return "-----BEGIN CERTIFICATE-----\n" +
                Base64.encodeBase64StringLocal(certificate.getEncoded()) +
                "\n-----END CERTIFICATE-----\n";
    }
}
