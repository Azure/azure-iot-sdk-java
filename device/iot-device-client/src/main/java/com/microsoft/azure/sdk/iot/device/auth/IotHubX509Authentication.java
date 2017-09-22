/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class IotHubX509Authentication
{
    private IotHubX509 iotHubX509;
    private IotHubSSLContext iotHubSSLContext;

    private String iotHubTrustedCert;
    private String pathToIotHubTrustedCert;

    private boolean sslContextNeedsUpdate;

    /**
     * Constructor that takes in a connection string and certificate/private key pair needed to use x509 authentication
     * @param publicKeyCertificate The PEM encoded string for the public key certificate or the path to a file containing it
     * @param isCertificatePath If the provided publicKeyCertificate is a path to the PEM encoded public key certificate file
     * @param privateKey The PEM encoded string for the private key or the path to a file containing it.
     * @param isPrivateKeyPath If the provided privateKey is a path to the PEM encoded private key file
     * @throws IllegalArgumentException if the public key certificate or private key is null or empty
     */
    public IotHubX509Authentication(String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws IllegalArgumentException
    {
        //Codes_SRS_IOTHUBX509AUTHENTICATION_34_002: [This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.]
        this.iotHubX509 = new IotHubX509(publicKeyCertificate, isCertificatePath, privateKey, isPrivateKeyPath);
        this.sslContextNeedsUpdate = false;
    }

    /**
     * Getter for IotHubSSLContext
     * @throws IOException if an error occurs when generating the SSLContext
     * @return The value of IotHubSSLContext
     */
    public SSLContext getSSLContext() throws IOException
    {
        try
        {
            //Codes_SRS_IOTHUBX509AUTHENTICATION_34_005: [This function shall return the saved IotHubSSLContext.]
            if (this.iotHubSSLContext == null || this.sslContextNeedsUpdate)
            {
                this.iotHubSSLContext = generateSSLContext();
                this.sslContextNeedsUpdate = false;
            }

            return this.iotHubSSLContext.getSSlContext();
        }
        catch (CertificateException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Creates the SSLContext for this object using the saved public and private keys
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     * @throws NoSuchAlgorithmException if the default SSL Context cannot be created
     */
    private IotHubSSLContext generateSSLContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException, UnrecoverableKeyException
    {
        if (this.iotHubTrustedCert != null)
        {
            // Codes_SRS_IOTHUBX509AUTHENTICATION_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert and with the saved public and private key combo.]
            return new IotHubSSLContext(this.iotHubX509.getPublicKeyCertificate(), this.iotHubX509.getPrivateKey(), this.iotHubTrustedCert, false);
        }
        else if (this.pathToIotHubTrustedCert != null)
        {
            // Codes_SRS_IOTHUBX509AUTHENTICATION_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert and with the saved public and private key combo.]
            return new IotHubSSLContext(this.iotHubX509.getPublicKeyCertificate(), this.iotHubX509.getPrivateKey(), this.pathToIotHubTrustedCert, true);
        }
        else
        {
            // Codes_SRS_IOTHUBX509AUTHENTICATION_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new IotHubSSLContext object with the saved public and private key combo.]
            return new IotHubSSLContext(this.iotHubX509.getPublicKeyCertificate(), this.iotHubX509.getPrivateKey());
        }
    }

    /**
     * Setter for the providing trusted certificate.
     * @param pathToCertificate path to the certificate for one way authentication.
     */
    public void setPathToIotHubTrustedCert(String pathToCertificate)
    {
        if (this.pathToIotHubTrustedCert == null || !this.pathToIotHubTrustedCert.equals(pathToCertificate))
        {
            //Codes_SRS_IOTHUBX509AUTHENTICATION_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
            this.sslContextNeedsUpdate = true;
        }

        //Codes_SRS_IOTHUBX509AUTHENTICATION_34_059: [This function shall save the provided iotHubTrustedCert.]
        this.pathToIotHubTrustedCert = pathToCertificate;
    }

    /**
     * Setter for the user trusted certificate
     * @param certificate valid user trusted certificate string
     */
    public void setIotHubTrustedCert(String certificate)
    {
        if (this.iotHubTrustedCert == null || !this.iotHubTrustedCert.equals(certificate))
        {
            //Codes_SRS_IOTHUBX509AUTHENTICATION_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
            this.sslContextNeedsUpdate = true;
        }

        // Codes_SRS_IOTHUBX509AUTHENTICATION_34_064: [This function shall save the provided pathToIotHubTrustedCert.]
        this.iotHubTrustedCert = certificate;
    }
}
