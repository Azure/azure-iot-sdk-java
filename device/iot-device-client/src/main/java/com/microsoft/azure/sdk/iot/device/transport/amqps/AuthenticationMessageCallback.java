package com.microsoft.azure.sdk.iot.device.transport.amqps;

import org.apache.qpid.proton.amqp.transport.DeliveryState;

interface AuthenticationMessageCallback
{
    DeliveryState handleAuthenticationResponseMessage(int status, String description);
}
