// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.microsoft.azure.sdk.iot.device.edge.MethodResult;

public class CommandResponse extends MethodResult
{
    public CommandResponse(String json)
    {
        super(json);
    }
}
