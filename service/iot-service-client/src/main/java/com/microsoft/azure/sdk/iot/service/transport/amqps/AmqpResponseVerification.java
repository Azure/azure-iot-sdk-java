/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.exceptions.*;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.*;
import org.apache.qpid.proton.amqp.transport.AmqpError;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import java.util.Map;

public class AmqpResponseVerification implements AmqpError
{
    private Symbol errorCondition = null;
    private String errorDescription = null;
    private Map errorInfo = null;
    //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_006: [** The function shall save null exception if the amqp delivery state is accepted or received or released or modified **]**
    private IotHubException exception = null;

    public AmqpResponseVerification(DeliveryState state)
    {
        if (state == null)
        {
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_008: [** The function shall save IotHubException if the amqp delivery state is null or undefined as per AMQP spec. **]**
            this.exception = new IotHubException();
        }
        else if (state.getClass().equals(Accepted.class))
        {

        }
        else if (state.getClass().equals(Rejected.class))
        {
            Rejected rejectedState = (Rejected)state;
            ErrorCondition errorCond = rejectedState.getError();
            this.errorCondition = errorCond.getCondition();
            this.errorDescription = errorCond.getDescription();
            this.errorInfo = errorCond.getInfo();
            this.amqpResponseVerifier();
        }
        else if (state.getClass().equals(Received.class))
        {

        }
        else if (state.getClass().equals(Released.class))
        {

        }
        else if (state.getClass().equals(Modified.class))
        {

        }
        else
        {
            this.exception = new IotHubException("Unknown delivery state");
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
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_001: [** The function shall save IotHubNotFoundException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-found **]**
            this.exception = new IotHubNotFoundException(errorDescription);
        }
        else if (this.errorCondition.equals(NOT_IMPLEMENTED))
        {
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_002: [** The function shall save IotHubNotSupportedException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-implemented **]**
            this.exception =  new IotHubNotSupportedException(errorDescription);
        }
        else if (this.errorCondition.equals(NOT_ALLOWED))
        {
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_003: [** The function shall save IotHubInvalidOperationException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-allowed **]**
            this.exception =  new IotHubInvalidOperationException(errorDescription);
        }
        else if (this.errorCondition.equals(UNAUTHORIZED_ACCESS))
        {
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_004: [** The function shall save IotHubUnathorizedException if the amqp delivery state is rejected and error condition is amqp error code amqp:unauthorized-access **]**
            this.exception = new IotHubUnathorizedException(errorDescription);
        }
        else if (this.errorCondition.equals(RESOURCE_LIMIT_EXCEEDED))
        {
            //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_005: [** The function shall save IotHubDeviceMaximumQueueDepthExceededException if the amqp delivery state is rejected and error condition is amqp error code amqp:resource-limit-exceeded **]**
            this.exception = new IotHubDeviceMaximumQueueDepthExceededException(errorDescription);
        }
    }

    public IotHubException getException()
    {
        //Codes_SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_007: [** The function shall return the exception saved earlier by the constructor **]**
        return this.exception;
    }
}
