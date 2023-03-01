package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.mqtt.MqttVersion;
import lombok.Builder;
import lombok.Getter;

import javax.net.ssl.SSLContext;

@Builder
public class MqttConnectSettings
{
    @Getter
    private String clientId;

    @Getter
    private String username;

    @Getter
    private char[] password;

    @Getter
    private MqttVersion mqttVersion;

    @Getter
    private int keepAlivePeriod;

    @Getter
    private String serverUri;

    @Getter
    private SSLContext sslContext;

    @Getter
    private ProxySettings proxySettings;

    @Getter
    private boolean cleanSession;
}
