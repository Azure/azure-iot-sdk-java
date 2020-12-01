/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Base class for providing authentication for a Device Client or Module Client, including x509 and SAS based authentication.
 */
public abstract class IotHubAuthenticationProvider
{
    protected String hostname;
    protected String gatewayHostname;
    protected String deviceId;
    protected String moduleId;

    protected IotHubSSLContext iotHubSSLContext;
    protected boolean sslContextNeedsUpdate;
    protected String iotHubTrustedCert;
    protected String pathToIotHubTrustedCert;

    public IotHubAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId)
    {
        if (hostname == null || hostname.isEmpty())
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_006: [If the provided hostname is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("hostname cannot be null");
        }

        if (deviceId == null || deviceId.isEmpty())
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_007: [If the provided device id is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("deviceId cannot be null");
        }

        // Codes_SRS_AUTHENTICATIONPROVIDER_34_001: [The constructor shall save the provided hostname, gatewayhostname, deviceid and moduleid.]
        this.hostname = hostname;
        this.gatewayHostname = gatewayHostname;
        this.deviceId = deviceId;
        this.moduleId = moduleId;
    }

    public IotHubAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext)
    {
        this(hostname, gatewayHostname, deviceId, moduleId);

        this.sslContextNeedsUpdate = false;

        if (sslContext != null)
        {
            this.iotHubSSLContext = new IotHubSSLContext(sslContext);
        }
    }

    public SSLContext getSSLContext() throws IOException
    {
        try
        {
            if (this.iotHubSSLContext == null || this.sslContextNeedsUpdate)
            {
                //Codes_SRS_AUTHENTICATIONPROVIDER_34_010: [If this object's ssl context has not been generated yet or if it needs to be re-generated, this function shall regenerate the ssl context.]
                this.iotHubSSLContext = generateSSLContext();
                this.sslContextNeedsUpdate = false;
            }

            //Codes_SRS_AUTHENTICATIONPROVIDER_34_011: [This function shall return the generated IotHubSSLContext.]
            return this.iotHubSSLContext.getSSLContext();
        }
        catch (CertificateException | NoSuchAlgorithmException | KeyManagementException | KeyStoreException e)
        {
            //Codes_SRS_AUTHENTICATIONPROVIDER_34_012: [If a CertificateException, NoSuchAlgorithmException, KeyManagementException, or KeyStoreException is thrown during this function, this function shall throw an IOException.]
            throw new IOException(e);
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
            //Codes_SRS_AUTHENTICATIONPROVIDER_34_030: [If the provided pathToCertificate is different than the saved path, this function shall set sslContextNeedsRenewal to true.]
            this.sslContextNeedsUpdate = true;
        }

        //Codes_SRS_AUTHENTICATIONPROVIDER_34_059: [This function shall save the provided iotHubTrustedCert.]
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
            //Codes_SRS_AUTHENTICATIONPROVIDER_34_031: [If the provided certificate is different than the saved certificate, this function shall set sslContextNeedsRenewal to true.]
            this.sslContextNeedsUpdate = true;
        }

        // Codes_SRS_AUTHENTICATIONPROVIDER_34_064: [This function shall save the provided pathToIotHubTrustedCert.]
        this.iotHubTrustedCert = certificate;
    }

    /**
     * Get the hostname
     * @return the saved hostname
     */
    public String getHostname()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_002: [This function shall return the saved hostname.]
        return this.hostname;
    }

    /**
     * Get the gatewayHostname
     * @return the saved gatewayHostname
     */
    public String getGatewayHostname()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_003: [This function shall return the saved gatewayHostname.]
        return this.gatewayHostname;
    }

    /**
     * Get the deviceId
     * @return the saved deviceId
     */
    public String getDeviceId()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_004: [This function shall return the saved deviceId.]
        return this.deviceId;
    }

    /**
     * Get the module id
     * @return the saved module id
     */
    public String getModuleId()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_005: [This function shall return the saved moduleId.]
        return this.moduleId;
    }

    public String getIotHubTrustedCert()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_008: [This function shall return the saved iotHubTrustedCert.]
        return this.iotHubTrustedCert;
    }

    public String getPathToIotHubTrustedCert()
    {
        // Codes_SRS_AUTHENTICATIONPROVIDER_34_009: [This function shall return the saved pathToIotHubTrustedCert.]
        return this.pathToIotHubTrustedCert;
    }

    private IotHubSSLContext generateSSLContext() throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException
    {
        if (this.iotHubTrustedCert != null)
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_019: [If this has a saved iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert as the trusted cert.]
            return new IotHubSSLContext(this.iotHubTrustedCert, false);
        }
        else if (this.pathToIotHubTrustedCert != null)
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_020: [If this has a saved path to a iotHubTrustedCert, this function shall generate a new IotHubSSLContext object with that saved cert path as the trusted cert.]
            return new IotHubSSLContext(this.pathToIotHubTrustedCert, true);
        }
        else
        {
            // Codes_SRS_AUTHENTICATIONPROVIDER_34_021: [If this has no saved iotHubTrustedCert or path, This function shall create and save a new default IotHubSSLContext object.]
            return new IotHubSSLContext();
        }
    }
}
