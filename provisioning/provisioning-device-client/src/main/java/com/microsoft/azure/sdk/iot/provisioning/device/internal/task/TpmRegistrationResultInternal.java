/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.TpmRegistrationResult;

public class TpmRegistrationResultInternal extends TpmRegistrationResult
{
    void setAuthenticationKey(String authenticationKey)
    {
        this.authenticationKey = authenticationKey;
    }
}
