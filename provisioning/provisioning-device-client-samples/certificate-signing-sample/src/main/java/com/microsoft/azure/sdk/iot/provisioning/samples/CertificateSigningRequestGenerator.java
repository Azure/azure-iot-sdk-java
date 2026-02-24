package com.microsoft.azure.sdk.iot.provisioning.samples;

import sun.security.pkcs10.PKCS10;
import sun.security.x509.X500Name;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;

public class CertificateSigningRequestGenerator
{
    private final Signature signature;
    private final KeyPairGenerator keyGen;
    private final String commonName;

    /**
     * @param algorithm "RSA" or "ECDSA"
     * @param commonName The common name of the certificate signing request. For this sample's purposes,
     * this value should equal the registration Id being used in DPS.
     */
    public CertificateSigningRequestGenerator(String algorithm, String commonName) throws CertificateException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        this.keyGen = KeyPairGenerator.getInstance(algorithm);
        if (algorithm.equalsIgnoreCase("RSA"))
        {
            this.signature = Signature.getInstance("SHA256withRSA");
            this.keyGen.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
        }
        else if (algorithm.equalsIgnoreCase("ECC"))
        {
            this.signature = Signature.getInstance("SHA256withECDSA");
            this.keyGen.initialize(new ECGenParameterSpec("prime256v1"));
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized encryption algorithm: " + algorithm);
        }

        this.commonName = commonName;
    }

    public CertificateSigningRequest GenerateNewCertificateSigningRequest() throws InvalidKeyException, IOException, CertificateException, SignatureException
    {
        KeyPair keypair = this.keyGen.generateKeyPair();
        PublicKey publicKey = keypair.getPublic();
        PrivateKey privateKey = keypair.getPrivate();

        // generate PKCS10 certificate request
        PKCS10 pkcs10 = new PKCS10(publicKey);

        this.signature.initSign(privateKey);
        X500Principal principal = new X500Principal( "CN=" + this.commonName);
        X500Name x500name;
        x500name= new X500Name(principal.getEncoded());
        pkcs10.encodeAndSign(x500name, this.signature);
        this.signature.initSign(privateKey);

        byte[] encodedPKCS10 = pkcs10.getEncoded();
        String base64EncodedPKCS10 = Base64.getEncoder().encodeToString(encodedPKCS10);
        return new CertificateSigningRequest(publicKey, privateKey, base64EncodedPKCS10);
    }
}
