// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;

public class AmqpsExceptionTranslator
{
    static TransportException convertToAmqpException(String exceptionCode)
    {
        switch (exceptionCode)
        {
            case AmqpConnectionForcedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_014: [The function shall map amqp exception code "amqp:connection:forced" to TransportException "AmqpConnectionForcedException".]
                return new AmqpConnectionForcedException();
            case AmqpConnectionRedirectException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_016: [The function shall map amqp exception code "amqp:connection:redirect" to TransportException "AmqpConnectionRedirectException".]
                return new AmqpConnectionRedirectException();
            case AmqpDecodeErrorException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_004: [The function shall map amqp exception code "amqp:decode-error" to TransportException "AmqpDecodeErrorException".]
                return new AmqpDecodeErrorException();
            case AmqpLinkDetachForcedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_021: [The function shall map amqp exception code "amqp:link:detach-forced" to TransportException "AmqpLinkDetachForcedException".]
                return new AmqpLinkDetachForcedException();
            case AmqpSessionErrantLinkException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_018: [The function shall map amqp exception code "amqp:session:errant-link" to TransportException "AmqpSessionErrantLinkException".]
                return new AmqpSessionErrantLinkException();
            case AmqpFrameSizeTooSmallException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_013: [The function shall map amqp exception code "amqp:frame-size-too-small" to TransportException "AmqpFrameSizeTooSmallException".]
                return new AmqpFrameSizeTooSmallException();
            case AmqpConnectionFramingErrorException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_015: [The function shall map amqp exception code "amqp:connection:framing-error" to TransportException "AmqpConnectionFramingErrorException".]
                return new AmqpConnectionFramingErrorException();
            case AmqpSessionHandleInUseException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_019: [The function shall map amqp exception code "amqp:session:handle-in-use" to TransportException "AmqpSessionHandleInUseException".]
                return new AmqpSessionHandleInUseException();
            case AmqpIllegalStateException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_012: [The function shall map amqp exception code "amqp:illegal-state" to TransportException "AmqpIllegalStateException".]
                return new AmqpIllegalStateException();
            case AmqpInternalErrorException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_001: [The function shall map amqp exception code "amqp:internal-error" to TransportException "AmqpInternalErrorException".]
                return new AmqpInternalErrorException();
            case AmqpInvalidFieldException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_007: [The function shall map amqp exception code "amqp:invalid-field" to TransportException "AmqpInvalidFieldException".]
                return new AmqpInvalidFieldException();
            case AmqpLinkRedirectException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_024: [The function shall map amqp exception code "amqp:link:redirect" to TransportException "AmqpLinkRedirectException".]
                return new AmqpLinkRedirectException();
            case AmqpLinkStolenException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_025: [The function shall map amqp exception code "amqp:link:stolen" to TransportException "AmqpLinkStolenException".]
                return new AmqpLinkStolenException();
            case AmqpLinkMessageSizeExceededException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_023: [The function shall map amqp exception code "amqp:link:message-size-exceeded" to TransportException "AmqpLinkMessageSizeExceededException".]
                return new AmqpLinkMessageSizeExceededException();
            case AmqpNotAllowedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_006: [The function shall map amqp exception code "amqp:not-allowed" to TransportException "AmqpNotAllowedException".]
                return new AmqpNotAllowedException();
            case AmqpNotFoundException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_002: [The function shall map amqp exception code "amqp:not-found" to TransportException "AmqpNotFoundException".]
                return new AmqpNotFoundException();
            case AmqpNotImplementedException.errorCode:
                // Codes_SRS_AAMQPSEXCEPTIONTRANSLATOR_34_008: [The function shall map amqp exception code "amqp:not-implemented" to TransportException "AmqpNotImplementedException".]
                return new AmqpNotImplementedException();
            case AmqpPreconditionFailedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_010: [The function shall map amqp exception code "amqp:precondition-failed" to TransportException "AmqpPreconditionFailedException".]
                return new AmqpPreconditionFailedException();
            case AmqpResourceDeletedException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_011: [The function shall map amqp exception code "amqp:resource-deleted" to TransportException "AmqpResourceDeletedException".]
                return new AmqpResourceDeletedException();
            case AmqpResourceLimitExceededException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_005: [The function shall map amqp exception code "amqp:resource-limit-exceeded" to TransportException "AmqpResourceLimitExceededException".]
                return new AmqpResourceLimitExceededException();
            case AmqpResourceLockedException.errorCode:
                // Codes_SRS_AMQPSIOTHUBCONNECTION_34_071: [The function shall map amqp exception code "amqp:resource-locked" to TransportException "AmqpResourceLockedException".]
                return new AmqpResourceLockedException();
            case AmqpLinkTransferLimitExceededException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_022: [The function shall map amqp exception code "amqp:link:transfer-limit-exceeded" to TransportException "AmqpLinkTransferLimitExceededException".]
                return new AmqpLinkTransferLimitExceededException();
            case AmqpSessionUnattachedHandleException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_020: [The function shall map amqp exception code "amqp:session:unattached-handle" to TransportException "AmqpSessionUnattachedHandleException".]
                return new AmqpSessionUnattachedHandleException();
            case AmqpUnauthorizedAccessException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_003: [The function shall map amqp exception code "amqp:unauthorized-access" to TransportException "AmqpUnauthorizedAccessException".]
                return new AmqpUnauthorizedAccessException();
            case AmqpSessionWindowViolationException.errorCode:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_017: [The function shall map amqp exception code "amqp:session:window-violation" to TransportException "AmqpSessionWindowViolationException".]
                return new AmqpSessionWindowViolationException();
            default:
                // Codes_SRS_AMQPSEXCEPTIONTRANSLATOR_34_026: [The function shall map all other amqp exception codes to the generic TransportException "ProtocolException".]
                return new ProtocolException();
        }
    }

}
