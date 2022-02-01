// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.query;

import lombok.Builder;
import lombok.Getter;

/**
 * The optional parameters for each query page request.
 */
@Builder
public class QueryPageOptions
{
    /**
     * The number of results per page. Larger page sizes will require the client to send fewer HTTP requests to continue
     * queries, but the HTTP requests will have larger payloads.
     */
    @Getter
    @Builder.Default
    private int pageSize = 50;

    /**
     * The query continuation token. For the first time running a query, this value should be null.
     */
    @Getter
    private String continuationToken;
}
