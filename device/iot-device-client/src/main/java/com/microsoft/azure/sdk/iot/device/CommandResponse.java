package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.edge.MethodResult;

public class CommandResponse extends MethodResult
{
    public CommandResponse(String json)
    {
        super(json);
    }
}
