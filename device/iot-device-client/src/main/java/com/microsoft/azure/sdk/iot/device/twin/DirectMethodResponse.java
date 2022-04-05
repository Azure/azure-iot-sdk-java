// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

public class DirectMethodResponse
{
    private int status;
    private Object payload;

    /**
     * Constructor for a DirectMethodResponse.
     * @param status the status in DirectMethodResponse which is returned by the device
     * @param payload the payload attached to that method. This parameter can be
         * Null: the DirectMethodResponse object will not include the "payload" field
         * Primitive type (e.g., String, Int)/Array/List/Map/custom type: will be serialized as value of the "payload" field using GSON.
     */
    public DirectMethodResponse(int status, Object payload)
    {
        this.status = status;
        this.payload = payload;
    }

    public int getStatus()
    {
        return status;
    }

    public Object getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }
}
