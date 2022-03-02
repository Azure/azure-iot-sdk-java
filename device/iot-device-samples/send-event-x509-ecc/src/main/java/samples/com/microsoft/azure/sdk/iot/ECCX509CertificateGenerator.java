// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for generating self signed certificates, and computing their thumbprints
 */
public class ECCX509CertificateGenerator
{
    // subject name is the same as the issuer string because it is self signed.
    private static final String ISSUER_STRING = "C=US, O=Microsoft, L=Redmond, OU=Azure";
    private static final String SIGNATURE_ALGORITHM = "SHA256withECDSA";
    private static final String KEY_PAIR_ALGORITHM = "ECDSA";

    /**
     * Create a new self signed ECC x509 certificate with the specified common name
     */
    public static ECCX509Certificate generateCertificate(String commonName, ECCX509Certificate issuer) throws OperatorCreationException, CertificateException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchProviderException, SignatureException
    {
        BouncyCastleProvider prov = new BouncyCastleProvider();
        Security.addProvider(prov);

        ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime256v1");
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_PAIR_ALGORITHM);
        keyGen.initialize(ecSpec);
        KeyPair keypair = keyGen.generateKeyPair();

        StringBuilder issuerStringBuilder = new StringBuilder(ISSUER_STRING);
        if (commonName != null && !commonName.isEmpty())
        {
            issuerStringBuilder.append(", CN=").append(commonName);
        }

        X500Name name = new X500Name(issuerStringBuilder.toString());

        // If there is no issuer, we self-sign our certificate.
        X500Name issuerName;
        PrivateKey issuerKey;
        if (issuer == null) {
            issuerName = name;
            issuerKey = keypair.getPrivate();
        } else {
            issuerName = new X500Name(issuer.certificate.getSubjectDN().getName());
            issuerKey = issuer.privateKey;
        }

        BigInteger serial = BigInteger.ONE;
        Date notBefore = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24)); // valid from 24 hours earlier as well, to avoid clock skew issues with start time
        Date notAfter = new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)); //2 hour lifetime
        X500Name subject = new X500Name(issuerStringBuilder.toString());
        PublicKey publicKey = keypair.getPublic();
        JcaX509v3CertificateBuilder v3Bldr = new JcaX509v3CertificateBuilder(issuerName, serial, notBefore, notAfter, subject, publicKey);

        X509CertificateHolder certHldr = v3Bldr.build(new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(issuerKey));
        X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHldr);
        cert.checkValidity(new Date());
        cert.verify(keypair.getPublic());
        return new ECCX509Certificate(keypair.getPrivate(), cert);
    }
}
