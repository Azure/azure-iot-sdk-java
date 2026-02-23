package samples.com.microsoft.azure.sdk.iot;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import javax.security.auth.x500.X500Principal;

import sun.security.pkcs10.*;
import sun.security.x509.*;

public class CertificateSigningRequest
{
    public final PublicKey publicKey;
    public final PrivateKey privateKey;
    public final byte[] encodedPKCS10;
    /**
     * @param algorithm "RSA" or "ECC"
     * @param commonName The common name of the certificate signing request. For this sample's purposes,
     * this value should equal the registration Id being used in DPS.
     */
    private CertificateSigningRequest(String algorithm, String commonName) throws CertificateException, NoSuchAlgorithmException, IOException, SignatureException, InvalidKeyException
    {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
        keyGen.initialize(2048, new SecureRandom());
        KeyPair keypair = keyGen.generateKeyPair();
        this.publicKey = keypair.getPublic();
        this.privateKey = keypair.getPrivate();
        this.encodedPKCS10 = generatePKCS10(commonName);
    }

    public byte[] generatePKCS10(String CN) throws NoSuchAlgorithmException, InvalidKeyException, IOException, CertificateException, SignatureException
    {
        // generate PKCS10 certificate request
        String sigAlg = "MD5WithRSA";
        PKCS10 pkcs10 = new PKCS10(publicKey);
        Signature signature = Signature.getInstance(sigAlg);
        signature.initSign(privateKey);
        X500Principal principal = new X500Principal( "CN=" + CN);

        //     pkcs10CertificationRequest kpGen = new PKCS10CertificationRequest(sigAlg, principal, publicKey, null, privateKey);
        //   byte[] c = kpGen.getEncoded();
        X500Name x500name;
        x500name= new X500Name(principal.getEncoded());
        pkcs10.encodeAndSign(x500name, signature);
        return pkcs10.getEncoded();
    }
}
