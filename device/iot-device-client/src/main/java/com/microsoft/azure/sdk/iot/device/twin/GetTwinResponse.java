// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import lombok.Getter;

public class GetTwinResponse
{
    @Getter
    Twin twin;

    @Getter
    Object context;

    GetTwinResponse(Twin twin, Object context)
    {
        this.twin = twin;
        this.context = context;
    }
}
