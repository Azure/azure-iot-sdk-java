// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

/**
 * Converter for amqp style error codes to throwable transport exceptions
 */
public class AmqpsExceptionTranslator
{
    static TransportException convertFromAmqpException(ErrorCondition error)
    {
        String exceptionCode = error.getCondition() != null ? error.getCondition().toString() : "unknown";
        String description = error.getDescription();

        switch (exceptionCode)
        {
            case AmqpLinkCreationConflict.errorCode:
                return new AmqpLinkCreationConflict(description);
            case AmqpConnectionForcedException.errorCode:
                return new AmqpConnectionForcedException(description);
            case AmqpConnectionRedirectException.errorCode:
                return new AmqpConnectionRedirectException(description);
            case AmqpDecodeErrorException.errorCode:
                return new AmqpDecodeErrorException(description);
            case AmqpLinkDetachForcedException.errorCode:
                return new AmqpLinkDetachForcedException(description);
            case AmqpSessionErrantLinkException.errorCode:
                return new AmqpSessionErrantLinkException(description);
            case AmqpFrameSizeTooSmallException.errorCode:
                return new AmqpFrameSizeTooSmallException(description);
            case AmqpConnectionFramingErrorException.errorCode:
                return new AmqpConnectionFramingErrorException(description);
            case AmqpSessionHandleInUseException.errorCode:
                return new AmqpSessionHandleInUseException(description);
            case AmqpIllegalStateException.errorCode:
                return new AmqpIllegalStateException(description);
            case AmqpInternalErrorException.errorCode:
                return new AmqpInternalErrorException(description);
            case AmqpInvalidFieldException.errorCode:
                return new AmqpInvalidFieldException(description);
            case AmqpLinkRedirectException.errorCode:
                return new AmqpLinkRedirectException(description);
            case AmqpLinkStolenException.errorCode:
                return new AmqpLinkStolenException(description);
            case AmqpLinkMessageSizeExceededException.errorCode:
                return new AmqpLinkMessageSizeExceededException(description);
            case AmqpNotAllowedException.errorCode:
                return new AmqpNotAllowedException(description);
            case AmqpNotFoundException.errorCode:
                return new AmqpNotFoundException(description);
            case AmqpNotImplementedException.errorCode:
                return new AmqpNotImplementedException(description);
            case AmqpPreconditionFailedException.errorCode:
                return new AmqpPreconditionFailedException(description);
            case AmqpResourceDeletedException.errorCode:
                return new AmqpResourceDeletedException(description);
            case AmqpResourceLimitExceededException.errorCode:
                return new AmqpResourceLimitExceededException(description);
            case AmqpResourceLockedException.errorCode:
                return new AmqpResourceLockedException(description);
            case AmqpLinkTransferLimitExceededException.errorCode:
                return new AmqpLinkTransferLimitExceededException(description);
            case AmqpSessionUnattachedHandleException.errorCode:
                return new AmqpSessionUnattachedHandleException(description);
            case AmqpUnauthorizedAccessException.errorCode:
                return new AmqpUnauthorizedAccessException(description);
            case AmqpSessionWindowViolationException.errorCode:
                return new AmqpSessionWindowViolationException(description);
            case AmqpConnectionThrottledException.errorCode:
                return new AmqpConnectionThrottledException(description);
            case ProtonIOException.errorCode:
                return new ProtonIOException(description);
            default:
                TransportException t = new TransportException("An unknown transport exception occurred");
                t.setRetryable(true);
                return t;
        }
    }
}
