// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.methods;

import lombok.Builder;
import lombok.Getter;

@Builder
public final class DirectMethodRequestOptions
{
    /**
     * The payload of the direct method request. This parameter can be
         * Null: the DirectMethodRequestOptions object will not include the "payload" field;
         * Primitive type/String/Array/List/Map/custom type: will be serialized as value of the "payload" field using GSON.
     * For a full list of end-to-end tests with different payload types, please refer to:
     * https://github.com/Azure/azure-iot-sdk-java/blob/main/iot-e2e-tests/common/src/test/java/tests/integration/com/microsoft/azure/sdk/iot/iothub/methods/DirectMethodsTests.java
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
