/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.security;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public abstract class DPSSecurityClientKey implements DPSSecurityClient
{
    private DPSHsmType DPSHsmType;
    private SecurityType securityType;

    abstract public byte[] importKey(byte[] key) throws IOException;
    abstract public byte[] getDeviceEk();
    abstract public byte[] getDeviceSRK();
    abstract public byte[] signData(byte[] data);

    @Override
    public void setSecurityType(SecurityType type)
    {
        if (type != SecurityType.Key)
            throw new IllegalArgumentException("Type can be Device Key only");

        this.securityType = type;
    }

    @Override
    public SecurityType getSecurityType()
    {
        return securityType;
    }

    @Override
    public String getRegistrationId()
    {
        return null;
    }

    @Override
    public SSLContext getSSLContext() throws SecurityException
    {
        return null;
    }
}
