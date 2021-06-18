 /*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */



package tests.unit.com.microsoft.azure.sdk.iot.provisioning.security.hsm;

 import mockit.*;

 import java.io.StringReader;
 import java.security.KeyPair;
 import java.security.PrivateKey;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;


 public class SecurityProviderX509CertTest
{
    @Mocked PrivateKey mockedPrivateKey;
    @Mocked X509Certificate mockedX509Certificate;
    @Mocked StringReader mockedStringReader;
    @Mocked KeyPair mockedKeyPair;
    @Mocked CertificateFactory mockedCertificateFactory;

    //TODO tests
}
