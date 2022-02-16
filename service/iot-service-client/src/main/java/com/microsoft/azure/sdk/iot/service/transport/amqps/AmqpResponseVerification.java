/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.AmqpError;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

@Slf4j
class AmqpResponseVerification implements AmqpError
{
    private Symbol errorCondition = null;
    private String errorDescription = null;
    private IotHubException exception = null;

    AmqpResponseVerification(DeliveryState state)
    {
        if (state == null)
        {
            this.exception = new IotHubException();
            return;
        }

        if (state.getClass().equals(Rejected.class))
        {
            Rejected rejectedState = (Rejected) state;
            ErrorCondition errorCond = rejectedState.getError();
            this.errorCondition = errorCond.getCondition();
            this.errorDescription = errorCond.getDescription();
            this.amqpResponseVerifier();
        }
        else if (state.getClass().equals(Accepted.class)
                || state.getClass().equals(Received.class)
                || state.getClass().equals(Released.class)
                || state.getClass().equals(Modified.class))
        {
            log.trace("Received an acceptable delivery state: {}", state.getType());
        }
        else
        {
            this.exception = new IotHubException("Unknown delivery state: " + state.getType());
        }
    }

    private void amqpResponseVerifier()
    {
        if (errorCondition == null)
        {
            this.exception = new IotHubException();
        }
        else if (this.errorCondition.equals(NOT_FOUND))
        {
            this.exception = new IotHubNotFoundException(errorDescription);
        }
        else if (this.errorCondition.equals(NOT_IMPLEMENTED))
        {
            this.exception =  new IotHubNotSupportedException(errorDescription);
        }
        else if (this.errorCondition.equals(NOT_ALLOWED))
        {
            this.exception =  new IotHubInvalidOperationException(errorDescription);
        }
        else if (this.errorCondition.equals(UNAUTHORIZED_ACCESS))
        {
            this.exception = new IotHubUnathorizedException(errorDescription);
        }
        else if (this.errorCondition.equals(RESOURCE_LIMIT_EXCEEDED))
        {
            this.exception = new IotHubDeviceMaximumQueueDepthExceededException(errorDescription);
        }
    }

    IotHubException getException()
    {
        return this.exception;
    }
}
