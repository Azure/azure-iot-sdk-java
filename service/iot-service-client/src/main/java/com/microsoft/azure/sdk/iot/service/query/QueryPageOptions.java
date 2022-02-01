// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import lombok.Builder;
import lombok.Getter;

@Builder
public class QueryPageOptions
{
    @Getter
    @Builder.Default
    int pageSize = 50;

    @Getter
    String continuationToken;
}
