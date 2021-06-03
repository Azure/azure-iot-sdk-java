// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.auth;

/**
 * This class contains the most commonly used Azure Active Directory token audience scopes. They should be used as
 * inputs when setting the authentication scopes in service client options classes such as
 * {@link com.microsoft.azure.sdk.iot.service.RegistryManagerOptions}.
 */
public class IotHubAuthenticationScopes
{
    // This private constructor exists to prevent users from constructing an instance of this class. This class
    // is just used to house static values, so it will never need to be instantiated.
    private IotHubAuthenticationScopes()
    {

    }

    /**
     * The default authentication scopes for IoT Hub. This value is the default value for all service client options, and
     * is the correct value to use for all users of public cloud deployed IoT Hubs and for all users of private cloud
     * deployed IoT Hubs other than those in the Fairfax cloud. For users of IoT Hubs deployed in the Fairfax cloud, the
     * {@link #FAIRFAX_AUTHENTICATION_SCOPES} should be used instead of this.
     */
    public static final String[] DEFAULT_AUTHENTICATION_SCOPES = new String[]{"https://iothubs.azure.net/.default"};

    /**
     * The authentication scopes for IoT Hubs deployed in the Fairfax private cloud. Users must provide this value when
     * constructing the client's options when using role based access credentials.
     */
    public static final String[] FAIRFAX_AUTHENTICATION_SCOPES = new String[]{"https://iothubs.azure.us/.default"};
}
