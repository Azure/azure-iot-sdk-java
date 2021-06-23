// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.deps.auth;


import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;

public class IotHubSSLContext
{
    SSLContext sslContext;

    public IotHubSSLContext() throws NoSuchAlgorithmException
    {
        this.sslContext = SSLContext.getDefault();
    }

    public IotHubSSLContext(SSLContext sslContext)
    {
        this.sslContext = sslContext;
    }

    public SSLContext getSSLContext()
    {
        return sslContext;
    }
}
