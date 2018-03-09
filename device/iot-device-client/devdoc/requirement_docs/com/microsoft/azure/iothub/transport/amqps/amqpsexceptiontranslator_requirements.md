# AmqpsExceptionTranslator Requirements

## Overview

Class to translator amqp exception code to TransportException. 

## References

## Exposed API

```java
public class AmqpsExceptionTranslator
{
    static TransportException convertToAmqpException(String exceptionCode);
}
```

### convertToAmqpException
```java
static TransportException convertToAmqpException(String exceptionCode)
```
**SRS_AMQPSEXCEPTIONTRANSLATOR_34_001: [**The function shall map amqp exception code "amqp:internal-error" to TransportException "AmqpInternalErrorException".**]**
    
**SRS_AMQPSEXCEPTIONTRANSLATOR_34_002: [**The function shall map amqp exception code "amqp:not-found" to TransportException "AmqpNotFoundException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_003: [**The function shall map amqp exception code "amqp:unauthorized-access" to TransportException "AmqpUnauthorizedAccessException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_004: [**The function shall map amqp exception code "amqp:decode-error" to TransportException "AmqpDecodeErrorException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_005: [**The function shall map amqp exception code "amqp:resource-limit-exceeded" to TransportException "AmqpResourceLimitExceededException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_006: [**The function shall map amqp exception code "amqp:not-allowed" to TransportException "AmqpNotAllowedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_007: [**The function shall map amqp exception code "amqp:invalid-field" to TransportException "AmqpInvalidFieldException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_008: [**The function shall map amqp exception code "amqp:not-implemented" to TransportException "AmqpNotImplementedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_009: [**The function shall map amqp exception code "amqp:resource-locked" to TransportException "AmqpResourceLockedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_010: [**The function shall map amqp exception code "amqp:precondition-failed" to TransportException "AmqpPreconditionFailedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_011: [**The function shall map amqp exception code "amqp:resource-deleted" to TransportException "AmqpResourceDeletedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_012: [**The function shall map amqp exception code "amqp:illegal-state" to TransportException "AmqpIllegalStateException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_013: [**The function shall map amqp exception code "amqp:frame-size-too-small" to TransportException "AmqpFrameSizeTooSmallException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_014: [**The function shall map amqp exception code "amqp:connection:forced" to TransportException "AmqpConnectionForcedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_015: [**The function shall map amqp exception code "amqp:connection:framing-error" to TransportException "AmqpConnectionFramingErrorException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_016: [**The function shall map amqp exception code "amqp:connection:redirect" to TransportException "AmqpConnectionRedirectException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_017: [**The function shall map amqp exception code "amqp:session:window-violation" to TransportException "AmqpSessionWindowViolationException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_018: [**The function shall map amqp exception code "amqp:session:errant-link" to TransportException "AmqpSessionErrantLinkException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_019: [**The function shall map amqp exception code "amqp:session:handle-in-use" to TransportException "AmqpSessionHandleInUseException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_020: [**The function shall map amqp exception code "amqp:session:unattached-handle" to TransportException "AmqpSessionUnattachedHandleException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_021: [**The function shall map amqp exception code "amqp:link:detach-forced" to TransportException "AmqpLinkDetachForcedException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_022: [**The function shall map amqp exception code "amqp:link:transfer-limit-exceeded" to TransportException "AmqpLinkTransferLimitExceededException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_023: [**The function shall map amqp exception code "amqp:link:message-size-exceeded" to TransportException "AmqpLinkMessageSizeExceededException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_024: [**The function shall map amqp exception code "amqp:link:redirect" to TransportException "AmqpLinkRedirectException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_025: [**The function shall map amqp exception code "amqp:link:stolen" to TransportException "AmqpLinkStolenException".**]**

**SRS_AMQPSEXCEPTIONTRANSLATOR_34_026: [**The function shall map all other amqp exception codes to the generic TransportException "ProtocolException".**]**
