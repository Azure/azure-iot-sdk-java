package com.microsoft.azure.sdk.iot.provisioning.samples;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import org.bouncycastle.asn1.pkcs.CertificationRequest;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.security.spec.*;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;

import static com.microsoft.azure.sdk.iot.provisioning.samples.CertificateType.ECC;
import static com.microsoft.azure.sdk.iot.provisioning.samples.CertificateType.RSA;

public class CertificateSigningRequestGenerator
{
    private final String signature;
    private final KeyPairGenerator keyGen;
    private final String commonName;

    /**
     * @param certificateType RSA or ECC
     * @param commonName The common name of the certificate signing request. For this sample's purposes,
     * this value should equal the registration Id being used in DPS.
     */
    public CertificateSigningRequestGenerator(CertificateType certificateType, String commonName) throws CertificateException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        BouncyCastleProvider prov = new BouncyCastleProvider();
        Security.addProvider(prov);

        if (certificateType == RSA)
        {
            this.keyGen = KeyPairGenerator.getInstance("RSA", prov);
            this.signature = "SHA256withRSA";
            this.keyGen.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
        }
        else if (certificateType == ECC)
        {
            this.keyGen = KeyPairGenerator.getInstance("EC", prov);
            this.signature = "SHA256withECDSA";
            this.keyGen.initialize(new ECGenParameterSpec("prime256v1"));
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized certificate type");
        }

        this.commonName = commonName;
    }

    public CertificateSigningRequest GenerateNewCertificateSigningRequest() throws InvalidKeyException, IOException, CertificateException, SignatureException, OperatorCreationException
    {
        KeyPair keypair = this.keyGen.generateKeyPair();
        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        org.bouncycastle.asn1.x500.X500Name name = new X500Name("CN=" + this.commonName);
        PKCS10CertificationRequestBuilder reqBuilder = new JcaPKCS10CertificationRequestBuilder(name, publicKey);
        JcaContentSignerBuilder signerBuilder = new JcaContentSignerBuilder(this.signature);
        PKCS10CertificationRequest csr = reqBuilder.build(signerBuilder.build(privateKey));

        String base64EncodedPKCS10 = Base64.getEncoder().encodeToString(csr.getEncoded());
        return new CertificateSigningRequest(publicKey, privateKey, base64EncodedPKCS10);
    }
}
