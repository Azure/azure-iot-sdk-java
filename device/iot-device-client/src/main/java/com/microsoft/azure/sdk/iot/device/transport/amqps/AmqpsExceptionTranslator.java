// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;

public class AmqpsExceptionTranslator
{
    static TransportException convertToAmqpException(String exceptionCode, String description)
    {
        switch (exceptionCode)
        {
            case AmqpConnectionForcedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_014: [The function shall map amqp exception code "amqp:connection:forced" to TransportException "AmqpConnectionForcedException".]
                return new AmqpConnectionForcedException(description);
            case AmqpConnectionRedirectException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_016: [The function shall map amqp exception code "amqp:connection:redirect" to TransportException "AmqpConnectionRedirectException".]
                return new AmqpConnectionRedirectException(description);
            case AmqpDecodeErrorException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_004: [The function shall map amqp exception code "amqp:decode-error" to TransportException "AmqpDecodeErrorException".]
                return new AmqpDecodeErrorException(description);
            case AmqpLinkDetachForcedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_021: [The function shall map amqp exception code "amqp:link:detach-forced" to TransportException "AmqpLinkDetachForcedException".]
                return new AmqpLinkDetachForcedException(description);
            case AmqpSessionErrantLinkException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_018: [The function shall map amqp exception code "amqp:session:errant-link" to TransportException "AmqpSessionErrantLinkException".]
                return new AmqpSessionErrantLinkException(description);
            case AmqpFrameSizeTooSmallException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_013: [The function shall map amqp exception code "amqp:frame-size-too-small" to TransportException "AmqpFrameSizeTooSmallException".]
                return new AmqpFrameSizeTooSmallException(description);
            case AmqpConnectionFramingErrorException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_015: [The function shall map amqp exception code "amqp:connection:framing-error" to TransportException "AmqpConnectionFramingErrorException".]
                return new AmqpConnectionFramingErrorException(description);
            case AmqpSessionHandleInUseException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_019: [The function shall map amqp exception code "amqp:session:handle-in-use" to TransportException "AmqpSessionHandleInUseException".]
                return new AmqpSessionHandleInUseException(description);
            case AmqpIllegalStateException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_012: [The function shall map amqp exception code "amqp:illegal-state" to TransportException "AmqpIllegalStateException".]
                return new AmqpIllegalStateException(description);
            case AmqpInternalErrorException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_001: [The function shall map amqp exception code "amqp:internal-error" to TransportException "AmqpInternalErrorException".]
                return new AmqpInternalErrorException(description);
            case AmqpInvalidFieldException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_007: [The function shall map amqp exception code "amqp:invalid-field" to TransportException "AmqpInvalidFieldException".]
                return new AmqpInvalidFieldException(description);
            case AmqpLinkRedirectException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_024: [The function shall map amqp exception code "amqp:link:redirect" to TransportException "AmqpLinkRedirectException".]
                return new AmqpLinkRedirectException(description);
            case AmqpLinkStolenException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_025: [The function shall map amqp exception code "amqp:link:stolen" to TransportException "AmqpLinkStolenException".]
                return new AmqpLinkStolenException(description);
            case AmqpLinkMessageSizeExceededException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_023: [The function shall map amqp exception code "amqp:link:message-size-exceeded" to TransportException "AmqpLinkMessageSizeExceededException".]
                return new AmqpLinkMessageSizeExceededException(description);
            case AmqpNotAllowedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_006: [The function shall map amqp exception code "amqp:not-allowed" to TransportException "AmqpNotAllowedException".]
                return new AmqpNotAllowedException(description);
            case AmqpNotFoundException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_002: [The function shall map amqp exception code "amqp:not-found" to TransportException "AmqpNotFoundException".]
                return new AmqpNotFoundException(description);
            case AmqpNotImplementedException.errorCode:
                // Codes_SRS_AAMQPSEXCEPTIONTRANSLATOR_34_008: [The function shall map amqp exception code "amqp:not-implemented" to TransportException "AmqpNotImplementedException".]
                return new AmqpNotImplementedException(description);
            case AmqpPreconditionFailedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_010: [The function shall map amqp exception code "amqp:precondition-failed" to TransportException "AmqpPreconditionFailedException".]
                return new AmqpPreconditionFailedException(description);
            case AmqpResourceDeletedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_011: [The function shall map amqp exception code "amqp:resource-deleted" to TransportException "AmqpResourceDeletedException".]
                return new AmqpResourceDeletedException(description);
            case AmqpResourceLimitExceededException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_005: [The function shall map amqp exception code "amqp:resource-limit-exceeded" to TransportException "AmqpResourceLimitExceededException".]
                return new AmqpResourceLimitExceededException(description);
            case AmqpResourceLockedException.errorCode:
                // Codes_SRS_AMQPSIOTHUBCONNECTION_34_071: [The function shall map amqp exception code "amqp:resource-locked" to TransportException "AmqpResourceLockedException".]
                return new AmqpResourceLockedException(description);
            case AmqpLinkTransferLimitExceededException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_022: [The function shall map amqp exception code "amqp:link:transfer-limit-exceeded" to TransportException "AmqpLinkTransferLimitExceededException".]
                return new AmqpLinkTransferLimitExceededException(description);
            case AmqpSessionUnattachedHandleException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_020: [The function shall map amqp exception code "amqp:session:unattached-handle" to TransportException "AmqpSessionUnattachedHandleException".]
                return new AmqpSessionUnattachedHandleException(description);
            case AmqpUnauthorizedAccessException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_003: [The function shall map amqp exception code "amqp:unauthorized-access" to TransportException "AmqpUnauthorizedAccessException".]
                return new AmqpUnauthorizedAccessException(description);
            case AmqpSessionWindowViolationException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_017: [The function shall map amqp exception code "amqp:session:window-violation" to TransportException "AmqpSessionWindowViolationException".]
                return new AmqpSessionWindowViolationException(description);
            case AmqpConnectionThrottledException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_28_001: [The function shall map amqp exception code "com.microsoft:device-container-throttled" to TransportException "AmqpConnectionThrottledException".]
                return new AmqpConnectionThrottledException(description);
            case ProtonIOException.errorCode:
                return  new ProtonIOException(description);
            default:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_026: [The function shall map all other amqp exception codes to the generic TransportException "ProtocolException".]
                return new ProtocolException(description);
        }
    }
}
