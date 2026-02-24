package com.microsoft.azure.sdk.iot.provisioning.samples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sun.security.pkcs10.PKCS10;
import sun.security.x509.X500Name;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;

@AllArgsConstructor
public class CertificateSigningRequest
{
    @Getter
    private final PublicKey publicKey;

    @Getter
    private final PrivateKey privateKey;

    @Getter
    private final String base64EncodedPKCS10;
}
