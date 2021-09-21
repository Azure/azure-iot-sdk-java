/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class IotHubX509
{
    private String publicKeyCertificate;
    private String privateKey;

    private String pathToPublicKeyCertificate;
    private String pathToPrivateKey;

    /**
     * Constructor that takes a public key certificate and a private key.
     * @throws IllegalArgumentException if the public key certificate or private key is null or empty
     */
    IotHubX509(String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IllegalArgumentException
    {
        if (publicKeyCertificate == null || publicKeyCertificate.isEmpty())
        {
            //Codes_SRS_IOTHUBX509_34_001: [If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Public key certificate cannot be null or empty");
        }

        if (privateKey == null || privateKey.isEmpty())
        {
            //Codes_SRS_IOTHUBX509_34_001: [If the provided public key certificate or private key is null or empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Private key certificate cannot be null or empty");
        }

        if (isPathForPublic)
        {
            //Codes_SRS_IOTHUBX509_34_013: [If a path is provided for the public key certificate, the path will be saved and the contents of the file shall be read and saved as a string.]
            this.pathToPublicKeyCertificate = publicKeyCertificate;
        }
        else
        {
            //Codes_SRS_IOTHUBX509_34_014: [If the public key certificate is not provided as a path, no path will be saved and the value of the public key certificate will be saved as a string.]
            this.publicKeyCertificate = publicKeyCertificate;
        }

        if (isPathForPrivate)
        {
            //Codes_SRS_IOTHUBX509_34_015: [If a path is provided for the private key, the path will be saved and the contents of the file shall be read and saved as a string.]
            this.pathToPrivateKey = privateKey;
        }
        else
        {
            //Codes_SRS_IOTHUBX509_34_016: [If the private key is not provided as a path, no path will be saved and the value of the private key will be saved as a string.]
            this.privateKey = privateKey;
        }
    }

    /**
     * Getter for PublicKeyCertificate
     * @throws IOException if there was an exception thrown reading the certificate or key from a file
     * @return The value of PublicKeyCertificate
     */
    String getPublicKeyCertificate() throws IOException
    {
        if (this.publicKeyCertificate == null && this.pathToPublicKeyCertificate != null)
        {
            //Codes_SRS_IOTHUBX509_34_017: [If the public key certificate was provided as a path in the constructor, this function shall read the public key certificate from its file.]
            this.publicKeyCertificate = readFromFile(this.pathToPublicKeyCertificate);
        }

        //Codes_SRS_IOTHUBX509_34_018: [This function shall return the saved public key certificate string.]
        return this.publicKeyCertificate;
    }

    /**
     * Getter for PrivateKey
     * @throws IOException if there was an exception thrown reading the certificate or key from a file
     * @return The value of PrivateKey
     */
    String getPrivateKey() throws IOException
    {
        if (this.privateKey == null && this.pathToPrivateKey != null)
        {
            //Codes_SRS_IOTHUBX509_34_019: [If the private key was provided as a path in the constructor, this function shall read the private key from its file.]
            this.privateKey = readFromFile(this.pathToPrivateKey);
        }

        //Codes_SRS_IOTHUBX509_34_020: [This function shall return the saved private key string.]
        return this.privateKey;
    }

    /**
     * Reads from a file into a string.
     * @param path the path to the file
     * @return the contents of the file
     * @throws IOException if an IO error occurs when reading from a file
     */
    private String readFromFile(String path) throws IOException
    {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
