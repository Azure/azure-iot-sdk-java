# AmqpsDeviceAuthenticationCBS Requirements

## Overview

Child class of AmqpsDeviceOperations to provide CBS authentication specific attributes and functionality. 


## References

## Exposed API


```java
class AmqpsDeviceAuthenticationCBS extends AmqpsDeviceAuthentication
{
    AmqpsDeviceAuthenticationCBSSendTask();
    void sendAuthenticationMessages();
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    protected Boolean authenticationMessageReceived(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId);
    protected void setSslDomain(Transport transport, SSLContext sslContext);
    protected void authenticate(DeviceClientConfig deviceClientConfig) throws IOException;
    protected Boolean isLinkFound(String linkName);
```


### AmqpsDeviceAuthenticationCBS

```java
AmqpsDeviceAuthenticationCBSSendTask(AmqpsDeviceAuthenticationCBS amqpsDeviceAuthenticationCBS);
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_001: [**The constructor shall set the sender and receiver endpoint path to IoTHub specific values.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_002: [**The constructor shall concatenate a sender specific prefix to the sender link tag's current value.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_003: [**The constructor shall set the sender and receiver endpoint path.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_004: [**The constructor shall add API version key and API version value to the amqpProperties.**]**


### sendAuthenticationMessages

```java
void sendAuthenticationMessages();
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_005: [**If there is no message in the queue to send the function shall do nothing.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_006: [**The function shall read the message from the queue.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_007: [**The function shall encode the message to a buffer.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_008: [**The function shall double the buffer if encode throws BufferOverflowException.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_009: [**The function shall set the delivery tag for the sender.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_010: [**The function shall call the super class sendMessageAndGetDeliveryHash.**]**


### getMessageFromReceiverLink

```java
protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_023: [**The function shall call the super to get the message.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_024: [**The function shall set the message type to CBS authentication if the message is not null.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_025: [**The function shall return the message.**]**


### authenticationMessageReceived

```java
protected Boolean authenticationMessageReceived(AmqpsMessage amqpsMessage, UUID authenticationCorrelationId);
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_026: [**The function shall return false if the amqpdMessage parameter is null or does not have Properties and Application properties.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_027: [**The function shall read the correlationId property and compare it to the given correlationId and if they are different return false.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_028: [**The function shall read the application properties and if the status code property is not 200 return false.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_029: [**The function shall return true If both the correlationID and status code matches.**]**


### setSslDomain
```java
protected void setSslDomain(Transport transport, SSLContext sslContext);
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_011: [**The function shall set get the sasl layer from the transport.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_012: [**The function shall set the sasl mechanism to PLAIN.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_013: [**The function shall set the SslContext on the domain.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_014: [**The function shall set the domain on the transport.**]**


### authenticate

```java
protected void authenticate(DeviceClientConfig deviceClientConfig) throws IOException;
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_030: [**The function shall create a CBS authentication message using the device configuration and the correlationID.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_031: [**The function shall set the CBS related properties on the message.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_032: [**The function shall set the CBS related application properties on the message.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_033: [**The function shall set the the SAS token to the message body.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_034: [**THe function shall put the message into the waiting queue.**]**



### isLinkFound
```java
protected Boolean isLinkFound(String linkName);
```

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_020: [**The function shall return true and set the sendLinkState to OPENED if the senderLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_021: [**The function shall return true and set the recvLinkState to OPENED if the receiverLinkTag is equal to the given linkName.**]**

**SRS_AMQPSDEVICEAUTHENTICATIONCBS_12_022: [**The function shall return false if neither the senderLinkTag nor the receiverLinkTag is matcing with the given linkName.**]**

