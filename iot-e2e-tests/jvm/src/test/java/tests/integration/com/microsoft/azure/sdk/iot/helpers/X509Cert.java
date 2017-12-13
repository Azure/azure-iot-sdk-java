/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.deps.util.Base64;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.*;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;

// Generates RSA Certs and private Keys
public class X509Cert
{
    private static final String DN_ROOT = "CN=Root, L=Redmond, C=US";
    private static final String DN_INTERMEDIATE = "CN=Intermediate_%s, L=Redmond, C=US";
    private static final String DN_LEAF = "CN=Leaf, L=Redmond, C=US";
    private static final long ONE_DAY = 1*24*60*60;

    private class CertKeyPair
    {
        PrivateKey key;
        X509Certificate certificate;

        CertKeyPair(PrivateKey key, X509Certificate certificate)
        {
            this.key = key;
            this.certificate = certificate;
        }
    }
    private CertKeyPair root;
    private ArrayList<CertKeyPair> intermediates;
    private CertKeyPair leaf;
    private Collection<String> intermediatesPem;
    private boolean useDice;


    public  X509Cert(int intermediatesCount, boolean useDice) throws NoSuchAlgorithmException
    {
        intermediatesPem = new ArrayList<>(intermediatesCount);
        intermediates = new ArrayList<>(intermediatesCount);
        this.useDice = useDice;

        if (!useDice)
        {
            try
            {
                this.root = createCertAndKey(String.format(DN_ROOT), ONE_DAY);
                this.root.certificate = createSignedCertificate(this.root.certificate, this.root.certificate, this.root.key);
                for (int i = 0; i < intermediatesCount; i++)
                {
                    this.intermediates.add(createCertAndKey(String.format(DN_INTERMEDIATE, i), ONE_DAY));
                    if (i == 0)
                    {
                        this.intermediates.get(i).certificate = createSignedCertificate(this.intermediates.get(i).certificate, this.root.certificate, this.root.key);
                    }
                    else
                    {
                        this.intermediates.get(i).certificate = createSignedCertificate(this.intermediates.get(i).certificate,
                                                                                    this.intermediates.get(i - 1).certificate,
                                                                                    this.intermediates.get(i - 1).key);
                    }
                }
                this.leaf = createCertAndKey(String.format(DN_LEAF), ONE_DAY);
                if (intermediatesCount > 0)
                {
                    this.leaf.certificate = createSignedCertificate(this.leaf.certificate, this.intermediates.get(intermediatesCount - 1).certificate,
                                                                    this.intermediates.get(intermediatesCount - 1).key);
                }
                else
                {
                    this.leaf.certificate = createSignedCertificate(this.leaf.certificate, this.root.certificate,
                                                                    this.root.key);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            // use DICE to generate certs when it supports
        }
    }

    /**
     *
     * @param DN eg "CN=Test, L=Redmond, C=GB"
     * @param validity 24 * 60 * 60 is 1 Day
     * @return A private key and X509 certificate
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws IOException
     * @throws CertificateException
     * @throws SignatureException
     */
    private CertKeyPair createCertAndKey(String DN, long validity) throws
                                                                                   NoSuchAlgorithmException,
                                                                                   NoSuchProviderException,
                                                                                   InvalidKeyException, IOException,
                                                                                   CertificateException,
                                                                                   SignatureException
    {
        //Generate ROOT certificate
        CertAndKeyGen keyGen = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
        keyGen.generate(1024);

        PrivateKey key = keyGen.getPrivateKey();

        X509Certificate x509Certificate = keyGen.getSelfCertificate(new X500Name(DN), validity);
        return new CertKeyPair(key, x509Certificate);
    }

    private static X509Certificate createSignedCertificate(X509Certificate certificate, X509Certificate issuerCertificate,
                                                           PrivateKey issuerPrivateKey)
            throws CertificateException, IOException, NoSuchProviderException,
                   NoSuchAlgorithmException, InvalidKeyException, SignatureException
    {

            Principal issuer = issuerCertificate.getSubjectDN();
            String issuerSigAlg = issuerCertificate.getSigAlgName();

            byte[] inCertBytes = certificate.getTBSCertificate();
            X509CertInfo info = new X509CertInfo(inCertBytes);
            info.set(X509CertInfo.ISSUER, issuer);

            CertificateExtensions exts=new CertificateExtensions();
            BasicConstraintsExtension bce = new BasicConstraintsExtension(true, -1);
            exts.set(BasicConstraintsExtension.NAME,new BasicConstraintsExtension(false, bce.getExtensionValue()));
            info.set(X509CertInfo.EXTENSIONS, exts);

            X509CertImpl outCert = new X509CertImpl(info);
            outCert.sign(issuerPrivateKey, issuerSigAlg);

            return outCert;
    }

    public String getPrivateKeyLeafPem()
    {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN RSA PRIVATE KEY-----\n");
        pem.append(new String (Base64.encodeBase64Local(this.leaf.key.getEncoded())));
        pem.append("\n-----END RSA PRIVATE KEY-----\n");
        return pem.toString();
    }

    public String getPublicCertLeafPem() throws IOException, GeneralSecurityException
    {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        pem.append(new String (Base64.encodeBase64Local(this.leaf.certificate.getEncoded())));
        pem.append("\n-----END CERTIFICATE-----\n");
        return pem.toString();
    }

    public String getPublicCertRootPem() throws IOException, GeneralSecurityException
    {
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        pem.append(new String (Base64.encodeBase64Local(this.root.certificate.getEncoded())));
        pem.append("\n-----END CERTIFICATE-----\n");
        return pem.toString();
    }

    public Collection getIntermediatesPem() throws CertificateEncodingException
    {
        for (CertKeyPair c : this.intermediates)
        {
            StringBuilder pem = new StringBuilder();
            pem.append("-----BEGIN CERTIFICATE-----\n");
            pem.append(new String(Base64.encodeBase64Local(c.certificate.getEncoded())));
            pem.append("\n-----END CERTIFICATE-----\n");
            intermediatesPem.add(pem.toString());
        }
        return intermediatesPem;
    }
}
