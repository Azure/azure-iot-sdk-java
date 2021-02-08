// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.digitaltwin.authentication;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Implementation of {@link ServiceClientCredentials} that provides RBAC based bearer tokens.
 */
@AllArgsConstructor
public class ServiceClientBearerTokenCredentialProvider implements ServiceClientCredentials {

    private static final String AUTHORIZATION = "Authorization";

    @NonNull
    private final BearerTokenProvider tokenProvider;

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {
        Interceptor authenticationInterceptor = chain -> {
            String authorizationValue = tokenProvider.getBearerToken();
            Request authenticatedRequest = chain.request()
                                                .newBuilder()
                                                .header(AUTHORIZATION, authorizationValue)
                                                .build();
            return chain.proceed(authenticatedRequest);
        };
        clientBuilder.interceptors().add(authenticationInterceptor);
    }
}
