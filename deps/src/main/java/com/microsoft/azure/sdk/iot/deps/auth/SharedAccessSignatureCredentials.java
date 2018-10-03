// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.auth;

import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.OkHttpClient.Builder;

public class SharedAccessSignatureCredentials implements ServiceClientCredentials
{
	private final ProvisioningConnectionString provisioningConnectionString;

	public SharedAccessSignatureCredentials(ProvisioningConnectionString connectionString)
	{
		this.provisioningConnectionString = connectionString;
	}

	@Override
	public void applyCredentialsFilter(Builder clientBuilder)
	{
		String sasToken = getSASToken(this.provisioningConnectionString);
		// TODO Auto-generated method stub
		clientBuilder.addInterceptor(new BasicAuthInterceptor(sasToken)).build();
	}
	
	private String getSASToken(ProvisioningConnectionString connectionString)
    {
        return new ProvisioningSasToken(connectionString).toString();
    }
}

