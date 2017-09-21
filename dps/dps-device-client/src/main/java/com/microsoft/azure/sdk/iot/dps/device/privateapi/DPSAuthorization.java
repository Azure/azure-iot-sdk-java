/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi;

import javax.net.ssl.SSLContext;
import java.security.cert.Certificate;

public class DPSAuthorization
{
    private String sasToken = null;
    private SSLContext sslContext = null;

    public DPSAuthorization()
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
