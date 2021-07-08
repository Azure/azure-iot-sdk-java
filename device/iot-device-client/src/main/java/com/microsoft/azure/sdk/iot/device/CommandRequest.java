package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;

public class CommandRequest extends MethodRequest
{
    public CommandRequest(String methodName, String payload) throws IllegalArgumentException
    {
        super(methodName, payload);
    }
}
