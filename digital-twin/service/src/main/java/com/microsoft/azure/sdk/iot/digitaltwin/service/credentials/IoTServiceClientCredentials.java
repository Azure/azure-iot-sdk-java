// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import okhttp3.OkHttpClient;

public abstract class IoTServiceClientCredentials implements ServiceClientCredentials
{
    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder)
    {
        String sasToken = getSASToken();
        clientBuilder.addInterceptor(new BasicAuthInterceptor(sasToken)).build();
    }

    protected abstract String getSASToken();

}
