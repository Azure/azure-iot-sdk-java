/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.auth;

import javax.net.ssl.SSLContext;

public class IotHubX509SoftwareAuthenticationProvider extends IotHubAuthenticationProvider
{
    public IotHubX509SoftwareAuthenticationProvider(String hostname, String gatewayHostname, String deviceId, String moduleId, SSLContext sslContext) throws IllegalArgumentException
    {
        super(hostname, gatewayHostname, deviceId, moduleId, sslContext);
    }
}
