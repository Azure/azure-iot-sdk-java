/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TpmRegistrationResult
{
    @Getter
    protected String authenticationKey;
}
