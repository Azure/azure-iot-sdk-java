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

    public IotHubX509SoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext) throws IllegalArgumentException
    {
        super(hostname, gatewayHostname, deviceId, moduleId, sslContext);
        this.iotHubX509 = null;
    }

    //TODO need to change the APIs here to not take publicKeyCertificate + privateKey
}
