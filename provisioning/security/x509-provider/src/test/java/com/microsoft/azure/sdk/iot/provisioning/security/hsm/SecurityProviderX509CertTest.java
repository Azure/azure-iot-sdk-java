 /*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */



package com.microsoft.azure.sdk.iot.provisioning.security.hsm;

 import com.microsoft.azure.sdk.iot.provisioning.security.hsm.SecurityProviderX509Cert;
 import mockit.*;
 import org.junit.Test;

 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.security.Key;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;

 import static org.junit.Assert.assertEquals;

 public class SecurityProviderX509CertTest
{
    private static final String expectedPrivateKeyString = "some private key string";
    private static final String expectedPublicKeyCertificateString = "some public key certificate string";

    @Mocked PrivateKey mockedPrivateKey;
    @Mocked X509Certificate mockedX509Certificate;
    @Mocked StringReader mockedStringReader;
    @Mocked KeyPair mockedKeyPair;
    @Mocked CertificateFactory mockedCertificateFactory;
}
