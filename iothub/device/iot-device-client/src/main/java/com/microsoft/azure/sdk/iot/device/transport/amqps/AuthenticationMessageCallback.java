package com.microsoft.azure.sdk.iot.device.transport.amqps;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.reactor.Reactor;

interface AuthenticationMessageCallback
{
    DeliveryState handleAuthenticationResponseMessage(int status, String description, Reactor reactor);
}
