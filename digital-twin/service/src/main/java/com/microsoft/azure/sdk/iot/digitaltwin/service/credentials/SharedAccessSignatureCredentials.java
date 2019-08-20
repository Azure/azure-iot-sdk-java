// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

public class SharedAccessSignatureCredentials extends IoTServiceClientCredentials
{
	private final String sasKey;

	public SharedAccessSignatureCredentials(String sasKey)
	{
		this.sasKey = sasKey;
	}

	protected String getSASToken()
	{
		return this.sasKey;
	}
}
