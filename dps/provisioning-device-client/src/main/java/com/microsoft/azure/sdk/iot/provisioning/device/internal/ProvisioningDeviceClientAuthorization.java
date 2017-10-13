/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal;

import javax.net.ssl.SSLContext;

public class ProvisioningDeviceClientAuthorization
{
    private String sasToken = null;
    private SSLContext sslContext = null;

    public ProvisioningDeviceClientAuthorization()
    {
        this.sasToken = null;
        this.sslContext = null;
    }

    public SSLContext getSslContext()
    {
        return sslContext;
    }

    public void setSslContext(SSLContext sslContext)
    {
        this.sslContext = sslContext;
    }

    public String getSasToken()
    {
        return sasToken;
    }

    public void setSasToken(String sasToken)
    {
        this.sasToken = sasToken;
    }
}
