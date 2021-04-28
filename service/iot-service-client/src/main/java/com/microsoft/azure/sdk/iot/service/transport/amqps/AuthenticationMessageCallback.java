package com.microsoft.azure.sdk.iot.service.transport.amqps;

import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.reactor.Reactor;

/**
 * Interface that defines the callback used by the CBS receiver link to notify the CBS session when an authentication
 * message has been received from the service
 */
interface AuthenticationMessageCallback
{
    /**
     * This callback executes when the CBS receiver link receives an authentication message from the service. This
     * message contains a status code and a description.
     * @param status The status of the authentication process (200 is successful).
     * @param description The human readable explanation for the status code.
     * @return The AMQP acknowledgement type for the receiver link to acknowledge the authentication message with.
     */
    DeliveryState handleAuthenticationResponseMessage(int status, String description);
}
