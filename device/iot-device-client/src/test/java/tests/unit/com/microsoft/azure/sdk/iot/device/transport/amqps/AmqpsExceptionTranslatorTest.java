/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.exceptions.ProtocolException;
import com.microsoft.azure.sdk.iot.device.transport.amqps.*;
import com.microsoft.azure.sdk.iot.device.transport.amqps.exceptions.*;
import mockit.*;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AmqpsExceptionTranslatorTest
{
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_001: [The function shall map amqp exception code "amqp:internal-error" to TransportException "AmqpInternalErrorException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_002: [The function shall map amqp exception code "amqp:not-found" to TransportException "AmqpNotFoundException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_003: [The function shall map amqp exception code "amqp:unauthorized-access" to TransportException "AmqpUnauthorizedAccessException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_004: [The function shall map amqp exception code "amqp:decode-error" to TransportException "AmqpDecodeErrorException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_005: [The function shall map amqp exception code "amqp:resource-limit-exceeded" to TransportException "AmqpResourceLimitExceededException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_006: [The function shall map amqp exception code "amqp:not-allowed" to TransportException "AmqpNotAllowedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_007: [The function shall map amqp exception code "amqp:invalid-field" to TransportException "AmqpInvalidFieldException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_008: [The function shall map amqp exception code "amqp:not-implemented" to TransportException "AmqpNotImplementedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_009: [The function shall map amqp exception code "amqp:resource-locked" to TransportException "AmqpResourceLockedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_010: [The function shall map amqp exception code "amqp:precondition-failed" to TransportException "AmqpPreconditionFailedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_011: [The function shall map amqp exception code "amqp:resource-deleted" to TransportException "AmqpResourceDeletedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_012: [The function shall map amqp exception code "amqp:illegal-state" to TransportException "AmqpIllegalStateException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_013: [The function shall map amqp exception code "amqp:frame-size-too-small" to TransportException "AmqpFrameSizeTooSmallException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_014: [The function shall map amqp exception code "amqp:connection:forced" to TransportException "AmqpConnectionForcedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_015: [The function shall map amqp exception code "amqp:connection:framing-error" to TransportException "AmqpConnectionFramingErrorException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_016: [The function shall map amqp exception code "amqp:connection:redirect" to TransportException "AmqpConnectionRedirectException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_017: [The function shall map amqp exception code "amqp:session:window-violation" to TransportException "AmqpSessionWindowViolationException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_018: [The function shall map amqp exception code "amqp:session:errant-link" to TransportException "AmqpSessionErrantLinkException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_019: [The function shall map amqp exception code "amqp:session:handle-in-use" to TransportException "AmqpSessionHandleInUseException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_020: [The function shall map amqp exception code "amqp:session:unattached-handle" to TransportException "AmqpSessionUnattachedHandleException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_021: [The function shall map amqp exception code "amqp:link:detach-forced" to TransportException "AmqpLinkDetachForcedException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_022: [The function shall map amqp exception code "amqp:link:transfer-limit-exceeded" to TransportException "AmqpLinkTransferLimitExceededException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_023: [The function shall map amqp exception code "amqp:link:message-size-exceeded" to TransportException "AmqpLinkMessageSizeExceededException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_024: [The function shall map amqp exception code "amqp:link:redirect" to TransportException "AmqpLinkRedirectException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_025: [The function shall map amqp exception code "amqp:link:stolen" to TransportException "AmqpLinkStolenException".]
    //Tests_SRS_AMQPSEXCEPTIONTRANSLATOR_34_026: [The function shall map all other amqp exception codes to the generic TransportException "ProtocolException".]
    @Test
    public void getConnectionStatusExceptionFromAMQPExceptionCodeReturnsExpectedMappings()
    {
        //assert
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpConnectionForcedException.errorCode) instanceof AmqpConnectionForcedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpUnauthorizedAccessException.errorCode) instanceof AmqpUnauthorizedAccessException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpConnectionFramingErrorException.errorCode) instanceof AmqpConnectionFramingErrorException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpNotFoundException.errorCode) instanceof AmqpNotFoundException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpResourceDeletedException.errorCode) instanceof AmqpResourceDeletedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpResourceLockedException.errorCode) instanceof AmqpResourceLockedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpConnectionRedirectException.errorCode) instanceof AmqpConnectionRedirectException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpDecodeErrorException.errorCode) instanceof AmqpDecodeErrorException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpLinkDetachForcedException.errorCode) instanceof AmqpLinkDetachForcedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpSessionErrantLinkException.errorCode) instanceof AmqpSessionErrantLinkException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpFrameSizeTooSmallException.errorCode) instanceof AmqpFrameSizeTooSmallException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpSessionHandleInUseException.errorCode) instanceof AmqpSessionHandleInUseException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpIllegalStateException.errorCode) instanceof AmqpIllegalStateException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpInternalErrorException.errorCode) instanceof AmqpInternalErrorException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpInvalidFieldException.errorCode) instanceof AmqpInvalidFieldException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpSessionWindowViolationException.errorCode) instanceof AmqpSessionWindowViolationException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpSessionUnattachedHandleException.errorCode) instanceof AmqpSessionUnattachedHandleException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpLinkTransferLimitExceededException.errorCode) instanceof AmqpLinkTransferLimitExceededException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpResourceLimitExceededException.errorCode) instanceof AmqpResourceLimitExceededException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpPreconditionFailedException.errorCode) instanceof AmqpPreconditionFailedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpNotImplementedException.errorCode) instanceof AmqpNotImplementedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpNotAllowedException.errorCode) instanceof AmqpNotAllowedException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpLinkMessageSizeExceededException.errorCode) instanceof AmqpLinkMessageSizeExceededException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpLinkStolenException.errorCode) instanceof AmqpLinkStolenException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", AmqpLinkRedirectException.errorCode) instanceof AmqpLinkRedirectException);
        assertTrue(Deencapsulation.invoke(AmqpsExceptionTranslator.class, "convertToAmqpException", "Not a protocol standard error code") instanceof ProtocolException);
    }
}
