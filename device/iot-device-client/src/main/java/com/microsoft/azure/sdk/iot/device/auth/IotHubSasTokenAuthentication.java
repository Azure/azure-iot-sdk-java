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
import java.security.cert.CertificateException;

public class IotHubSasTokenAuthentication
{
    /**
     * The number of seconds after which the generated SAS token for a message
     * will become invalid. We also use the expiry time, which is computed as
     * {@code currentTime() + DEVICE_KEY_VALID_LENGTH}, as a salt when generating our
     * SAS token.
     */
    private long tokenValidSecs = 3600;

    private static final long MILLISECONDS_PER_SECOND = 1000L;
    private static final long MINIMUM_EXPIRATION_TIME_OFFSET = 1L;

    private IotHubSasToken sasToken;
    private IotHubSSLContext iotHubSSLContext;

    private String hostname;
    private String deviceId;
    private String deviceKey;

    private String iotHubTrustedCert;
    private String pathToIotHubTrustedCert;

    private boolean sslContextNeedsUpdate;

    /**
     * Constructor that takes a connection string containing a sas token or a device key
     *
     * @param hostname the IotHub host name
     * @param deviceId the IotHub device id
     * @param deviceKey the device key for the device. Must be null if the provided sharedAccessToken is not
     * @param sharedAccessToken the sas token string for accessing the device. Must be null if the provided deviceKey is not.
     * @throws SecurityException if the provided sas token has expired
     */
    public IotHubSasTokenAuthentication(String hostname, String deviceId, String deviceKey, String sharedAccessToken) throws SecurityException
    {
        this.hostname = hostname;
        this.deviceId = deviceId;
        this.deviceKey = deviceKey;

        this.sslContextNeedsUpdate = false;

        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_002: [This constructor shall save the provided connection string.]
        this.sasToken = new IotHubSasToken(hostname, deviceId, deviceKey, sharedAccessToken, tokenValidSecs);
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall renew it if possible
     *
     * @return The value of SasToken
     */
    public String getRenewedSasToken()
    {
        if (this.deviceKey != null && this.sasToken.isExpired())
        {
            //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_004: [If the saved sas token has expired and there is a device key present, the saved sas token shall be renewed.]
            Long expiryTime = (System.currentTimeMillis() / MILLISECONDS_PER_SECOND) + this.tokenValidSecs + MINIMUM_EXPIRATION_TIME_OFFSET;
            this.sasToken = new IotHubSasToken(this.hostname, this.deviceId, this.deviceKey, null, expiryTime);
        }

        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_005: [This function shall return the saved sas token.]
        return this.sasToken.toString();
    }

    /**
     * Getter for SasToken. If the saved token has expired, this method shall not renew it even if possible
     *
     * @return The value of SasToken
     */
    public String getCurrentSasToken()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_018: [This function shall return the current sas token without renewing it.]
        return this.sasToken.toString();
    }

    /**
     * Getter for SSLContext
     * @throws IOException if an error occurs when generating the SSLContext
     * @return The value of SSLContext
     */
    public SSLContext getSSLContext() throws IOException
    {
        try
        {
            if (this.iotHubSSLContext == null || this.sslContextNeedsUpdate)
            {
                this.iotHubSSLContext = generateSSLContext();
            }

            //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_008: [This function shall return the generated IotHubSSLContext.]
            return this.iotHubSSLContext.getSSlContext();
        }
        catch (CertificateException |  NoSuchAlgorithmException | KeyManagementException | KeyStoreException e)
        {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Setter for TokenValidSecs
     *
     * @param tokenValidSecs the value to set
     */
    public void setTokenValidSecs(long tokenValidSecs)
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_012: [This function shall save the provided tokenValidSecs as the number of seconds that created sas tokens are valid for.]
        this.tokenValidSecs = tokenValidSecs;
    }

    /**
     * Returns true if the saved sas token has expired and cannot be auto-renewed through the device key
     * @return if the sas token needs manual renewal
     */
    public boolean isRenewalNecessary()
    {
        //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_017: [If the saved sas token has expired and cannot be renewed, this function shall return true.]
        return (this.sasToken != null && this.sasToken.isExpired() && this.deviceKey == null);
    }

    /**
     *
     * @throws KeyStoreException  if no Provider supports a KeyStoreSpi implementation for the specified type or
     *                            if the keystore has not been initialized,
     *                            or the given alias already exists and does not identify an entry containing a trusted certificate,
     *                            or this operation fails for some other reason.
     * @throws KeyManagementException As per https://docs.oracle.com/javase/7/docs/api/java/security/KeyManagementException.html
     * @throws IOException If the certificate provided was null or invalid
     * @throws CertificateException As per https://docs.oracle.com/javase/7/docs/api/java/security/cert/CertificateException.html
     * @throws NoSuchAlgorithmException if the default SSL Context cannot be created
     */
    private IotHubSSLContext generateSSLContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        if (this.iotHubTrustedCert != null)
        {
            // Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.]
            return new IotHubSSLContext(this.iotHubTrustedCert, false);
        }
        else if (this.pathToIotHubTrustedCert != null)
        {
            // Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.]
            return new IotHubSSLContext(this.pathToIotHubTrustedCert, true);
        }
        else
        {
            // Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.]
            return new IotHubSSLContext();
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
            //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
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
            //Codes_SRS_IOTHUBSASTOKENAUTHENTICATION_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
            this.sslContextNeedsUpdate = true;
        }

        // Codes_SRS_IOTHUBX509AUTHENTICATION_34_064: [This function shall save the provided pathToIotHubTrustedCert.]
        this.iotHubTrustedCert = certificate;
    }
}
