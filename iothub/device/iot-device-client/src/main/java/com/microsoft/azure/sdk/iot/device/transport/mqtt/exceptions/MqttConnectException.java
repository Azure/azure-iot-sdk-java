package com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions;

import lombok.Getter;
import lombok.Setter;

public class MqttConnectException extends Exception
{
    @Getter
    @Setter
    private int reasonCode;

    @Getter
    @Setter
    private Exception cause;
}
