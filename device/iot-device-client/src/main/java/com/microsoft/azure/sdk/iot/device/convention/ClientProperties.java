// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.convention;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.device.twin.Tools;
import com.microsoft.azure.sdk.iot.device.twin.Twin;
import com.microsoft.azure.sdk.iot.device.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.device.twin.TwinGsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Container for the properties that are writable and reported from the client
 */
@AllArgsConstructor
public class ClientProperties
{
    @Getter
    @Setter
    private ClientPropertyCollection writableProperties;

    @Getter
    @Setter
    private ClientPropertyCollection reportedFromClient;
}
