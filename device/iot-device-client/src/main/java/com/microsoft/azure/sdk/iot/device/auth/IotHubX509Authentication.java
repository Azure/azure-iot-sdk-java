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

public abstract class IotHubX509Authentication
{
    IotHubSSLContext iotHubSSLContext;

    /**
     * Getter for SSLContext
     * @return the saved SSLContext instance
     * @throws IOException if an exception occurs while retrieving the SSLContext
     */
    public abstract SSLContext getSSLContext() throws IOException;

    /**
     * Setter for the providing trusted certificate.
     * @param pathToCertificate path to the certificate for one way authentication.
     */
    public abstract void setPathToIotHubTrustedCert(String pathToCertificate);

    /**
     * Setter for the user trusted certificate
     * @param certificate valid user trusted certificate string
     */
    public abstract void setIotHubTrustedCert(String certificate);
}
