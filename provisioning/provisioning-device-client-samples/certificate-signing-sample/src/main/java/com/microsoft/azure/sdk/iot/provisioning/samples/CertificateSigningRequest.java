package com.microsoft.azure.sdk.iot.provisioning.samples;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.security.*;

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
