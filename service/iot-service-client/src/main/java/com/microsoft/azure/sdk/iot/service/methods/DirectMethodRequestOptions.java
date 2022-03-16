// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.methods;

import lombok.Builder;
import lombok.Getter;

@Builder
public final class DirectMethodRequestOptions
{
    /**
     * The payload of the direct method request. This parameter can take
         * null: the DirectMethodRequestOptions object will not include the "payload" field;
         * String/List/Map/complex nested object: it will be serialized as value of "payload" field in DirectMethodRequestOptions.
     */
    @Getter
    private final Object payload;

    /**
     * The timeout (in seconds) before the direct method request will fail if the device fails to respond to the request.
     * By default, this is set to 200 seconds (the maximum allowed value).
     */
    @Getter
    @Builder.Default
    private final int methodResponseTimeoutSeconds = 200;

    /**
     * The timeout (in seconds) before the direct method request will fail if the request takes too long to reach the device.
     * By default, this is set to 200 seconds (the maximum allowed value).
     */
    @Getter
    @Builder.Default
    private final int methodConnectTimeoutSeconds = 200;
}
