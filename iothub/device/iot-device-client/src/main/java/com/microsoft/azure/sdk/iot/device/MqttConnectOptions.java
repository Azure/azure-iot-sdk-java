package com.microsoft.azure.sdk.iot.device;

import lombok.Builder;
import lombok.Getter;

@Builder
public class MqttConnectOptions
{
    @Getter
    private String clientId;

    @Getter
    private String username;

    @Getter
    private char[] password;

    @Getter
    private int mqttVersion; //TODO enum

    @Getter
    private int keepAlivePeriod;

    @Getter
    private String serverUri;
}
