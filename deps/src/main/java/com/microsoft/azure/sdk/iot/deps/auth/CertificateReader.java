package com.microsoft.azure.sdk.iot.deps.auth;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;

public class CertificateReader {

    private static final String BEGIN_MARKER = "-----BEGIN CERTIFICATE";
    private static final String END_MARKER = "-----END CERTIFICATE";

    private final LineReader server;
    private Collection<X509Certificate> certs;

    public CertificateReader(String keyContent) {
        this.server = new LineReader(keyContent);
    }

    public Collection<X509Certificate> getCertificates() throws IOException {
        if (this.certs == null) {
            this.certs = read();
        }
        return this.certs;
    }

    private Collection<X509Certificate> read() throws IOException {
        Collection<X509Certificate> result = new ArrayList<>();
        String line;
        CertificateFactory factory;

        while ((line = server.readLine()) != null) {
            if (line.indexOf(BEGIN_MARKER) != -1) {
                byte[] certBytes = readCertMaterial(END_MARKER);
                try {
                    factory = CertificateFactory.getInstance("X509");
                } catch (CertificateException e) {
                    throw new IOException("JCE error: " + e.getMessage());
                }
                try {
                    X509Certificate certificate =
                            (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
                    result.add(certificate);
                } catch (CertificateException e) {
                    throw new IOException("Invalid cert file: " + e.getMessage());
                }
            }
        }
        return result;
    }

    private byte[] readCertMaterial(String endMarker) throws IOException {
        String line = null;
        StringBuffer buf = new StringBuffer();

        while ((line = server.readLine()) != null) {
            if (line.indexOf(endMarker) != -1) {
                return Base64.getDecoder().decode(buf.toString());
            }

            buf.append(line.trim());
        }

        throw new IOException("Invalid cert file: No end marker");
    }
}
