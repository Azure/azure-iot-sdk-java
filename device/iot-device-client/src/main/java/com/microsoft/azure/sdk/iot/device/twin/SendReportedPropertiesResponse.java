// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.twin;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import lombok.Getter;

public class SendReportedPropertiesResponse
{
    @Getter
    IotHubStatusCode status;

    @Getter
    Object context;

    SendReportedPropertiesResponse(IotHubStatusCode status, Object context)
    {
        this.status = status;
        this.context = context;
    }
}
