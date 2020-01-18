// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.service.credentials;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@AllArgsConstructor
public class ServiceClientCredentialsProvider implements ServiceClientCredentials {

    private static final String AUTHORIZATION = "Authorization";
    @NonNull
    private final SasTokenProvider sasTokenProvider;

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        Interceptor authenticationInterceptor = chain -> {
            Request request = chain.request();
            Request authenticatedRequest = request.newBuilder()
                                                  .header(AUTHORIZATION, sasTokenProvider.getSasToken())
                                                  .build();
            return chain.proceed(authenticatedRequest);
        };
        clientBuilder.interceptors().add(authenticationInterceptor);
    }
}
