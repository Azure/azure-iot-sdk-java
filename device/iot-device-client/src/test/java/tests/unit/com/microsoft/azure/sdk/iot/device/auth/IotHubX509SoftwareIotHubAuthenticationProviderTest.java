/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.device.auth;

import com.microsoft.azure.sdk.iot.deps.auth.IotHubSSLContext;
import com.microsoft.azure.sdk.iot.device.auth.*;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.provisioning.security.exceptions.SecurityProviderException;
import mockit.*;
import org.junit.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

/**
 * Unit tests for IotHubX509SoftwareAuthenticationProvider.java
 * Methods: 100%
 * Lines: 100%
 */
public class IotHubX509SoftwareIotHubAuthenticationProviderTest
{
    private static final String publicKeyCertificate = "someCert";
    private static final String privateKey = "someKey";

    private static final String hostname = "hostname";
    private static final String gatewayHostname = "gateway";
    private static final String deviceId = "deviceId";
    private static final String moduleId = "moduleId";

    @Mocked IotHubSSLContext mockIotHubSSLContext;
    @Mocked IotHubX509 mockIotHubX509;
    @Mocked SSLContext mockSSLContext;

    @Test
    public void constructorSuccessWithSSLContext()
    {
        //arrange
        new Expectations()
        {
            {
                new IotHubSSLContext(mockSSLContext);
                result = mockIotHubSSLContext;
            }
        };

        //act
        IotHubX509SoftwareAuthenticationProvider provider = new IotHubX509SoftwareAuthenticationProvider(hostname, gatewayHostname, deviceId, moduleId, mockSSLContext);

        //assert
        assertNull(Deencapsulation.getField(provider, "iotHubX509"));
    }
}

