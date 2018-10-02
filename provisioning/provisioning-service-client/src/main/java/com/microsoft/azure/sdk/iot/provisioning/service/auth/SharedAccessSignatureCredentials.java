// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.OkHttpClient.Builder;

public class SharedAccessSignatureCredentials implements ServiceClientCredentials
{
	private String sasToken = null;

	public SharedAccessSignatureCredentials(String sharedAccessSignature)
	{
		this.sasToken = sharedAccessSignature;
	}

	@Override
	public void applyCredentialsFilter(Builder clientBuilder)
	{
		// TODO Auto-generated method stub
		clientBuilder.addInterceptor(new BasicAuthInterceptor(this.sasToken)).build();
	}
}

