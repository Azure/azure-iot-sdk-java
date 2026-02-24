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

public class CertificateSigningRequest
{
    public final PublicKey publicKey;
    public final PrivateKey privateKey;
    public final byte[] encodedPKCS10;
    public final String base64EncodedPKCS10;

    /**
     * @param algorithm "RSA" or "ECDSA"
     * @param commonName The common name of the certificate signing request. For this sample's purposes,
     * this value should equal the registration Id being used in DPS.
     */
    public CertificateSigningRequest(String algorithm, String commonName) throws CertificateException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException, InvalidAlgorithmParameterException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        Signature signature;
        if (algorithm.equalsIgnoreCase("RSA"))
        {
            signature = Signature.getInstance("SHA256withRSA");
            keyGen.initialize(new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4));
        }
        else if (algorithm.equalsIgnoreCase("ECC"))
        {
            signature = Signature.getInstance("SHA256withECDSA");
            keyGen.initialize(new ECGenParameterSpec("prime256v1"));
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized encryption algorithm: " + algorithm);
        }

        KeyPair keypair = keyGen.generateKeyPair();
        this.publicKey = keypair.getPublic();
        this.privateKey = keypair.getPrivate();

        // generate PKCS10 certificate request
        PKCS10 pkcs10 = new PKCS10(this.publicKey);

        signature.initSign(this.privateKey);
        X500Principal principal = new X500Principal( "CN=" + commonName);
        X500Name x500name;
        x500name= new X500Name(principal.getEncoded());
        pkcs10.encodeAndSign(x500name, signature);
        signature.initSign(this.privateKey);

        this.encodedPKCS10 = pkcs10.getEncoded();
        this.base64EncodedPKCS10 = Base64.getEncoder().encodeToString(this.encodedPKCS10);
    }
}
