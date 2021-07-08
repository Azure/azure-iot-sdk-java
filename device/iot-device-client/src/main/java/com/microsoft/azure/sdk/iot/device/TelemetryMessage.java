package com.microsoft.azure.sdk.iot.device;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

public class TelemetryMessage extends Message
{
    @Getter
    @Setter
    public ClientPropertyCollection Telemetry;
}
