/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.auth;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Class used to abstract the different 
 * authentication methods used to use IoT Hub SDK. 
 * 
 */
public abstract class AuthenticationMethod
{
    /**
     * Authentication policy name
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    String policyName;

    /**
     * Shared access policy key (if applies)
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    String key;

    /**
     * Shared access policy token (if applies)
     */
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    String token;

    /**
     * Abstract helper function to populate {@code IotHubConnectionString} class with
     * proper authentication properties
     * 
     * @param iotHubConnectionStringBuilder  The {@link IotHubConnectionString} that is
     *                                       to be populated with the authentication
     *                                       properties.
     * @return An instance of {@link IotHubConnectionString} populated with the
     * the authentication properties.
     */
    protected abstract IotHubConnectionString populate(IotHubConnectionString iotHubConnectionStringBuilder);
}
