package com.microsoft.azure.sdk.iot.deps.auth;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class PrivateKeyReader {

    // Private key file using PKCS #8 encoding
    private static final String P8_BEGIN_MARKER = "-----BEGIN PRIVATE KEY";
    private static final String P8_END_MARKER = "-----END PRIVATE KEY";

    // Private key file using ECDSA encoding
    private static final String ECDSA_BEGIN_MARKER = "-----BEGIN EC PRIVATE KEY";
    private static final String ECDSA_END_MARKER = "-----END EC PRIVATE KEY";
    // "30 81bf 020100 301006072a8648ce3d020106052b81040022 0481a7"
    private static final byte[] ECDSA_HEADER = new byte[] {0x30, (byte) 0x81, (byte) 0xbf, 0x02, 0x01, 0x00, 0x30, 0x10,
            0x06, 0x07, 0x2a, (byte) 0x86, 0x48, (byte) 0xce, 0x3d, 0x02, 0x01, 0x06, 0x05, 0x2b, (byte) 0x81, 0x04,
            0x00, 0x22, 0x04, (byte) 0x81, (byte) 0xa7};

    private final LineReader server;
    private PrivateKey key;

    public PrivateKeyReader(String keyContent) {
        this.server = new LineReader(keyContent);
    }

    public PrivateKey getPrivateKey() throws IOException {
        if (this.key == null) {
            this.key = read();
        }
        return this.key;
    }

    private PrivateKey read() throws IOException {

        String line;

        KeyFactory factory;

        while ((line = server.readLine()) != null) {
            if (line.indexOf(P8_BEGIN_MARKER) != -1) {
                byte[] keyBytes = readKeyMaterial(P8_END_MARKER);
                EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
                try {
                    factory = KeyFactory.getInstance("RSA");
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException("JCE error: " + e.getMessage());
                }
                try {
                    return factory.generatePrivate(keySpec);
                } catch (InvalidKeySpecException e) {
                    throw new IOException("Invalid PKCS#8 PEM file: " + e.getMessage());
                }
            } else if (line.indexOf(ECDSA_BEGIN_MARKER) != -1) {
                // https://stackoverflow.com/questions/41927859/how-do-i-load-an-elliptic-curve-pem-encoded-private-key
                byte[] keyBytes = readKeyMaterial(ECDSA_END_MARKER);
                EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(join(ECDSA_HEADER, keyBytes));
                try {
                    factory = KeyFactory.getInstance("EC");
                } catch (NoSuchAlgorithmException e) {
                    throw new IOException("JCE error: " + e.getMessage());
                }
                try {
                    return factory.generatePrivate(keySpec);
                } catch (InvalidKeySpecException e) {
                    throw new IOException("Invalid ECDSA PEM file: " + e.getMessage());
                }
            }
        }

        throw new IOException("Invalid PEM file: no begin marker");
    }

    private byte[] join(byte[] a, byte[] b) {
        byte[] result = new byte[a.length + b.length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    private byte[] readKeyMaterial(String endMarker) throws IOException {
        String line = null;
        StringBuffer buf = new StringBuffer();

        while ((line = server.readLine()) != null) {
            if (line.indexOf(endMarker) != -1) {

                return Base64.getDecoder().decode(buf.toString().getBytes());
            }

            buf.append(line.trim());
        }

        throw new IOException("Invalid PEM file: No end marker");
    }

}
