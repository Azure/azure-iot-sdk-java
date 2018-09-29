// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import java.io.IOException;

import com.microsoft.rest.credentials.ServiceClientCredentials;

import okhttp3.Authenticator;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class SharedAccessKeyCredentials implements ServiceClientCredentials{
	
	private String sasToken;

	public SharedAccessKeyCredentials(String connectionString) {
		sasToken = generateSASToken(connectionString);
	}
	@Override
	public void applyCredentialsFilter(Builder clientBuilder) {
		// TODO Auto-generated method stub
		clientBuilder.authenticator(new Authenticator() {

			@Override
			public Request authenticate(Route route, Response response) throws IOException {
				// TODO Auto-generated method stub
				return response.request().newBuilder().header("Authorization", sasToken).build();
			}
			
		}).build();
	}
	
	private String generateSASToken(String connectionString) {
		ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
		return new ProvisioningSasToken(provisioningConnectionString).toString();
	}

}
