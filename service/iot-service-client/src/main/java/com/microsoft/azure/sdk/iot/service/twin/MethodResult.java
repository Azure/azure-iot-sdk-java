/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.twin;

import lombok.Getter;

/**
 * Store the status and payload received as result of a method invoke.
 */
public final class MethodResult
{
    /**
     * Status of the Invoke Method.
     */
    @Getter
    private final Integer status;

    /**
     * Payload with the result of the Invoke Method
     */
    @Getter
    private final Object payload;

    public MethodResult(Integer status, Object payload)
    {
        this.status = status;
        this.payload = payload;
    }
}
