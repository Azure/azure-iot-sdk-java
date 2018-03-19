/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.mqtt.exceptions;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;

/**
 *  This exception is thrown when an MQTT Connection Return code of 0x03 is encountered when opening an MQTT connection
 *
 *  In the context of IoT, this exception may be encountered when trying to connect to a disabled device
 *
 * <a href="http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csprd02/mqtt-v3.1.1-csprd02.html#_Toc385349771">
 *     MQTT Connect Return Code Documentation</a>
 */
public class MqttServerUnavailableException extends ProtocolException
{
    public MqttServerUnavailableException()
    {
        super();
        this.isRetryable = true;
    }

    public MqttServerUnavailableException(String message)
    {
        super(message);
        this.isRetryable = true;
    }

    public MqttServerUnavailableException(String message, Throwable cause)
    {
        super(message, cause);
        this.isRetryable = true;
    }

    public MqttServerUnavailableException(Throwable cause)
    {
        super(cause);
        this.isRetryable = true;
    }
}
