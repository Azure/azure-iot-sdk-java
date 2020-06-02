// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import lombok.Getter;
import lombok.Setter;

public final class ClientOptions
{
    /**
     * The Plug and Play Model Id associated with the device identity.
     */
    @Setter
    @Getter
    public String ModelId;
}
