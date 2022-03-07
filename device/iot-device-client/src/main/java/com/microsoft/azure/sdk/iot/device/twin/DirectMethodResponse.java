// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

public class DirectMethodResponse
{
    private int status;
    private Object responseMessage;

    public DirectMethodResponse(int status, Object responseMessage)
    {
        this.status = status;
        this.responseMessage = responseMessage;
    }

    public int getStatus()
    {
        return status;
    }

    public Object getResponseMessage()
    {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage)
    {
        this.responseMessage = responseMessage;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }
}
