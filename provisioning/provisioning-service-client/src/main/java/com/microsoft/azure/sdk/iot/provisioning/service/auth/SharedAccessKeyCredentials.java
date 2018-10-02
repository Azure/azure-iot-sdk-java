// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.OkHttpClient.Builder;

public class SharedAccessKeyCredentials implements ServiceClientCredentials
{
    private String sasToken;

    public SharedAccessKeyCredentials(ProvisioningConnectionString connectionString)
    {
        this.sasToken = generateSASToken(connectionString);
    }

    @Override
    public void applyCredentialsFilter(Builder clientBuilder)
    {
        // TODO Auto-generated method stub
        clientBuilder.addInterceptor(new BasicAuthInterceptor(this.sasToken))
                     .build();
    }

    private String generateSASToken(ProvisioningConnectionString connectionString)
    {
        return new ProvisioningSasToken(connectionString).toString();
    }
}
