/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import lombok.Getter;
import lombok.Setter;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.message.impl.MessageImpl;

/**
 * Extension of the QPID-Proton-J MessageImpl class which implements the Message interface. Adds a Delivery object as a
 * private member variable and adds a new ACK_TYPE enum. Adds the ability to easily acknowledge a single message.
 */
public class AmqpsMessage extends MessageImpl
{
    @Getter
    @Setter
    private Delivery delivery;

    /**
     * Sends acknowledgement of this message using the provided ACK_TYPE.
     *
     * @param ackType acknowledgement type to send
     */
    public void acknowledge(DeliveryState ackType)
    {
        delivery.disposition(ackType);
        delivery.settle();
    }
}
