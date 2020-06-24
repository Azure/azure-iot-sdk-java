/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

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
import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating self signed certificates, and computing their thumbprints
 */
public class X509CertificateGenerator
{
    // subject name is the same as the issuer string because it is self signed.
    private static final String ISSUER_STRING = "C=US, O=Microsoft, L=Redmond, OU=Azure";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final String KEY_PAIR_ALGORITHM = "RSA";

    private final static String PRIVATE_KEY_HEADER = "-----BEGIN PRIVATE KEY-----\n";
    private final static String PRIVATE_KEY_FOOTER = "\n-----END PRIVATE KEY-----\n";
    private final static String PUBLIC_CERT_HEADER = "-----BEGIN CERTIFICATE-----\n";
    private final static String PUBLIC_CERT_FOOTER = "\n-----END CERTIFICATE-----\n";

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
            throw new AssertionError("Failed to generate certificate for tests", e);
        }
    }

    /**
     * Constructor that generates a new self signed x509 certificate. Public certificate, private key, thumbprint, and the complete
     * certificate can be accessed by getters. The created certificate will have the provided common name.
     * @param commonName the common name to use for the created certs
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
     * Create a new self signed x509 certificate without specifying a common name
     */
    private static X509Certificate createX509CertificateFromKeyPair(KeyPair keyPair) throws CertificateException, NoSuchAlgorithmException, OperatorCreationException, SignatureException, NoSuchProviderException, InvalidKeyException
    {
        return createX509CertificateFromKeyPair(keyPair, null);
    }

    /**
     * Create a new self signed x509 certificate with the specified common name
     */
    private static X509Certificate createX509CertificateFromKeyPair(KeyPair keyPair, String commonName) throws OperatorCreationException, CertificateException, InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException
    {
        StringBuilder issuerStringBuilder = new StringBuilder(ISSUER_STRING);
        if (commonName != null && !commonName.isEmpty())
        {
            issuerStringBuilder.append(", CN=" + commonName);
        }

        X500Name issuer = new X500Name(issuerStringBuilder.toString());
        BigInteger serial = BigInteger.ONE;
        Date notBefore = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)); // valid from 24 hours earlier as well, to avoid clock skew issues with start time
        Date notAfter = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)); //2 hour lifetime
        X500Name subject = new X500Name(issuerStringBuilder.toString());
        PublicKey publicKey = keyPair.getPublic();
        JcaX509v3CertificateBuilder v3Bldr = new JcaX509v3CertificateBuilder(issuer, serial, notBefore, notAfter, subject, publicKey);
        X509CertificateHolder certHldr = v3Bldr.build(new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate()));
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHldr);
        cert.checkValidity(new Date());
        cert.verify(keyPair.getPublic());
        return cert;
    }

    private static String getPrivateKeyPem(PrivateKey privateKey)
    {
        return PRIVATE_KEY_HEADER + Base64.encodeBase64StringLocal(privateKey.getEncoded()) + PRIVATE_KEY_FOOTER;
    }

    private static String getPublicCertificatePem(X509Certificate certificate) throws CertificateEncodingException {
        return PUBLIC_CERT_HEADER + Base64.encodeBase64StringLocal(certificate.getEncoded()) + PUBLIC_CERT_FOOTER;
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
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        keyGen.initialize(4096, new SecureRandom());
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
}
