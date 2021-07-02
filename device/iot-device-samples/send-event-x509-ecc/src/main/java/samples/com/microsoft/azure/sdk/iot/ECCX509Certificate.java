// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

final class ECCX509Certificate
{
    final PrivateKey privateKey;
    final X509Certificate certificate;
    final String x509ThumbPrint;

    ECCX509Certificate(PrivateKey privateKey, X509Certificate certificate) throws CertificateEncodingException
    {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.x509ThumbPrint = new String(Hex.encodeHex(DigestUtils.sha256(certificate.getEncoded())));
    }
}
