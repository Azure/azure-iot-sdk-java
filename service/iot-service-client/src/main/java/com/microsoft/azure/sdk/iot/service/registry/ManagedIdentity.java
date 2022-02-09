// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.registry;

import lombok.Getter;
import lombok.Setter;

public class ManagedIdentity {
    /**
     * The managed identity used to access the storage account for IoT hub import and export jobs.
     */
    @Setter
    @Getter
    public String userAssignedIdentity;
}
