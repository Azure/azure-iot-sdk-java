/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

public interface ProvisioningDeviceClientRegistrationCallback
{
    /**
     * Callback user to provide registration results such as iothub uri, device id or any exception thrown during the process of registration
     * @param provisioningDeviceClientRegistrationResult An object that holds information about the registration result
     * @param e Exception thrown during the process of registration. Can be {@code null}.
     * @param context Context for this callback
     */
    void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception e, Object context);
}
