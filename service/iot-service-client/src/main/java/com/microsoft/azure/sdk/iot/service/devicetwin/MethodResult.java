/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

/**
 * Store the status and payload received as result of a method invoke.
 */
public final class MethodResult
{
    /**
     * Status of the Invoke Method.
     */
    private Integer status;

    /**
     * Payload with the result of the Invoke Method
     */
    private Object payload;

    public MethodResult(Integer status, Object payload)
    {
        /* Codes_SRS_METHODRESULT_21_001: [The constructor shall save the status and payload representing the method invoke result.] */
        /* Codes_SRS_METHODRESULT_21_002: [There is no restrictions for these values, it can be empty, or null.] */
        this.status = status;
        this.payload = payload;
    }

    public Integer getStatus()
    {
        /* Codes_SRS_METHODRESULT_21_003: [The getStatus shall return the status stored by the constructor.] */
        return this.status;
    }

    public Object getPayload()
    {
        /* Codes_SRS_METHODRESULT_21_004: [The getPayload shall return the payload stored by the constructor.] */
        return this.payload;
    }
}
