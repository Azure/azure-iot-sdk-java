package com.microsoft.azure.sdk.iot.deps.convention;

import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import lombok.Getter;
import lombok.Setter;

public class CommandParser extends MethodParser
{
    @Getter
    @Setter
    private String componentName;

    public CommandParser(String responseMessage)
    {
        super(responseMessage);
    }
}
