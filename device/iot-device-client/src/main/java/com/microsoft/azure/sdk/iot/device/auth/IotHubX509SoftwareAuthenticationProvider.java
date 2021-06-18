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

public class IotHubX509SoftwareAuthenticationProvider extends IotHubAuthenticationProvider
{
    @SuppressWarnings("CanBeFinal") // Class can be inherited
    protected IotHubX509 iotHubX509;

    /**
     * Constructor that takes in a connection string and certificate/private key pair needed to use x509 authentication
     * @param hostname the IotHub host name
     * @param gatewayHostname The gateway hostname to use, or null if connecting to an IotHub
     * @param deviceId The device to be authenticated.
     * @param moduleId The module to be authenticated. May be null if this authentication is not for a module
     * @param publicKeyCertificate The PEM encoded string for the public key certificate or the path to a file containing it
     * @param isCertificatePath If the provided publicKeyCertificate is a path to the PEM encoded public key certificate file
     * @param privateKey The PEM encoded string for the private key or the path to a file containing it.
     * @param isPrivateKeyPath If the provided privateKey is a path to the PEM encoded private key file
     * @throws IllegalArgumentException if the public key certificate or private key is null or empty
     */
    public IotHubX509SoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, String publicKeyCertificate, boolean isCertificatePath, String privateKey, boolean isPrivateKeyPath) throws IllegalArgumentException
    {
        super(hostname, gatewayHostname, deviceId, moduleId);

        //Codes_SRS_IOTHUBX509AUTHENTICATION_34_002: [This constructor will create and save an IotHubX509 object using the provided public key certificate and private key.]
        this.iotHubX509 = new IotHubX509(publicKeyCertificate, isCertificatePath, privateKey, isPrivateKeyPath);
    }

    public IotHubX509SoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext) throws IllegalArgumentException
    {
        super(hostname, gatewayHostname, deviceId, moduleId, sslContext);
        this.iotHubX509 = null;
    }

    //TODO need to change the APIs here to not take publicKeyCertificate + privateKey
}
