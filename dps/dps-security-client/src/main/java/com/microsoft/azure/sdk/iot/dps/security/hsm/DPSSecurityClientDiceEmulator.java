/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.security.hsm;

import com.microsoft.msr.RiotEmulator.RIoT;

import com.microsoft.azure.sdk.iot.dps.security.DPSSecurityClientX509;
import com.microsoft.azure.sdk.iot.dps.security.SecurityType;

import java.security.Key;
import java.security.cert.Certificate;

public class DPSSecurityClientDiceEmulator extends DPSSecurityClientX509
{
    final static String commonName = "riotcorenew";
    final static String commonName_signer = "riotsignernew";
    final static String commonName_root = "riotrootnew";

    // read this data from DICE HW after boot
    private static final byte[] FWID = {
                                        0x11, 0x12, 0x13, 0x14, 0x05, 0x06, 0x07, 0x08,
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                                        };
    private static final byte[] SEED = {
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                                        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
                                        };

    private RIoT.DeviceAuthBundle diceBundle;

    public DPSSecurityClientDiceEmulator()
    {
        this.setSecurityType(SecurityType.X509);

        this.diceBundle = RIoT.CreateDeviceAuthBundle(
                SEED,
                FWID,
                false,
                commonName, commonName_signer, commonName_root);
        /*System.out.println(this.diceBundle.RootCertPem);
        System.out.println(this.diceBundle.RootPrivateKeyPem);
        System.out.println(this.diceBundle.AliasCertPem);
        System.out.println(this.diceBundle.AliasPrivateKeyPem);*/
    }

    @Override
    public String getDeviceCommonName()
    {
        return commonName_root;
    }

    @Override
    public Certificate getAliasCert()
    {
        return this.diceBundle.AliasCert;
    }

    @Override
    public Key getAliasKey()
    {
        return this.diceBundle.AliasPrivateKey;
    }

    @Override
    public Certificate getDeviceSignerCert()
    {
        return this.diceBundle.RootCert;
    }

    /*private PrivateKey getPemPrivateKey(String pemPrivateKey) throws Exception
    {
        String algorithm = "ECDSA";
        String temp = new String(pemPrivateKey.getBytes());
        String privKeyPEM = temp.replace("-----BEGIN EC PRIVATE KEY-----\n", "");
        privKeyPEM = privKeyPEM.replace("-----END EC PRIVATE KEY-----\n", "");
        System.out.println("Private key\n"+ privKeyPEM);

        BASE64Decoder b64=new BASE64Decoder();
        byte[] decoded = b64.decodeBuffer(privKeyPEM);

        *//*ECNamedCurveParameterSpec ecGenSpec = ECNamedCurveTable.getParameterSpec("P-256");

        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(1, decoded), ecGenSpec);*//*

        //X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance(algorithm);
        return kf.generatePrivate(spec);
    }

    private X509Certificate convertToX509Cert(String certificateString) throws IOException
    {
        X509Certificate certificate = null;
        CertificateFactory cf = null;
        try {
            if (certificateString != null && !certificateString.trim().isEmpty())
            {
                certificateString = certificateString.replace("-----BEGIN CERTIFICATE-----\n", "")
                        .replace("-----END CERTIFICATE-----\n", ""); // NEED FOR PEM FORMAT CERT STRING
                System.out.println("Cert \n " + certificateString);

                BASE64Decoder b64 = new BASE64Decoder();
                byte[] certBytes = b64.decodeBuffer(certificateString);
                InputStream is = new ByteArrayInputStream(certBytes);
                *//*
                byte[] certificateData = Base64.getDecoder().decode(certificateString);
                cf = CertificateFactory.getInstance("X509");
                certificate = (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(certificateData));*//*
            }
        } catch (IOException e)
        {
            throw e;
        }
        return certificate;
    }*/
}
