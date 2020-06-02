// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import lombok.Getter;
import lombok.Setter;

/**
 * Options that allow configuration of the device client instance during initialization.
 */
public final class ClientOptions
{
    /**
     * The Digital Twin Model Id associated with the device identity.
     * Non plug and play users should not set this value
     */
    @Setter
    @Getter
    public String ModelId;
}
