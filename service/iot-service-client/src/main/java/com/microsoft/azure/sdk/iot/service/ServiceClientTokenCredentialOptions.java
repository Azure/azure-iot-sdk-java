package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.auth.AuthenticationScope;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;

/**
 * Configurable options for all service client operations
 */
@Builder
public class ServiceClientTokenCredentialOptions
{
    @Getter
    @Builder.Default
    private final AuthenticationScope tokenCredentialAuthenticationScopes = AuthenticationScope.DEFAULT;
}
