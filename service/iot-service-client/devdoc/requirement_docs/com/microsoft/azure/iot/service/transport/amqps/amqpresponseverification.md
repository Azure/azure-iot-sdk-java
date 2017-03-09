# IotHubExceptionManager Requirements

## Overview

Provide a amqp delivery status verifier to verify results and save appropriate exception. 

## References

## Exposed API

```java
public class IotHubExceptionManager
{
    public AmqpResponseVerification(DeliveryState state);
    public IotHubException getException();
}
```

### AmqpResponseVerification

```java
public AmqpResponseVerification(DeliveryState state);
```

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_001: [** The function shall save IotHubNotFoundException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-found **]**

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_002: [** The function shall save IotHubNotSupportedException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-implemented **]**

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_003: [** The function shall save IotHubInvalidOperationException if the amqp delivery state is rejected and error condition is amqp error code amqp:not-allowed **]**

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_004: [** The function shall save IotHubUnathorizedException if the amqp delivery state is rejected and error condition is amqp error code amqp:unauthorized-access **]**

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_005: [** The function shall save IotHubDeviceMaximumQueueDepthExceededException if the amqp delivery state is rejected and error condition is amqp error code amqp:resource-limit-exceeded **]**

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_006: [** The function shall save null exception if the amqp delivery state is accepted or received or released or modified **]**

**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_008: [** The function shall save IotHubException if the amqp delivery state is null or undefined as per AMQP spec. **]**

### getException

```java
public IotHubException getException();
```
**SRS_SERVICE_SDK_JAVA_AMQPRESPONSEVERIFICATION_25_007: [** The function shall return the exception saved earlier by the constructor **]**