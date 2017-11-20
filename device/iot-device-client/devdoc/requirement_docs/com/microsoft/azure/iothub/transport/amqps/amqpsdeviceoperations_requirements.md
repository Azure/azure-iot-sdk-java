# AmqpsDeviceOperations Requirements

## Overview

Base class to provide operation specific attributes and objects for Device Client. 
Collaborate with connection layer to handle operation specific links, link attributes and send operation.

## References

## Exposed API

```java
class AmqpsDeviceOperations
{
    AmqpsDeviceOperations();
    protected void openLinks(Session session) throws IOException, IllegalArgumentException;
    protected void closeLinks();
    protected void initLink(Link link) throws IOException, IllegalArgumentException;
    protected int sendMessageAndGetDeliveryHash(byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
    protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
    protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException;
    protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg) throws IOException;
    protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
    public Map<Symbol, Object> getAmqpProperties();
    public String getSenderLinkTag();
    public String getReceiverLinkTag();
    public String getSenderLinkAddress();
    public String getReceiverLinkAddress();
```

### AmqpsDeviceOperations

```java
AmqpsDeviceOperations();
```

**SRS_AMQPSDEVICEOPERATIONS_12_001: [**The constructor shall initialize amqpProperties with device client identifier and version.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_002: [**The constructor shall initialize sender and receiver tags with UUID string.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_003: [**The constructor shall initialize sender and receiver endpoint path members to empty string.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_004: [**The constructor shall initialize sender and receiver link address members to empty string.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_005: [**The constructor shall initialize sender and receiver link objects to null.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_032: [**The class has static members for version identifier, api version keys and api version value.**]**


### openLinks

```java
protected void openLinks(Session session) throws IOException, IllegalArgumentException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_006: [**The function shall throw IllegalArgumentException if the session argument is null.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_007: [**The function shall create receiver link with the receiverlinkTag member value.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_008: [**The function shall create sender link with the senderlinkTag member value.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_009: [**The function shall set both receiver and sender link properties to the amqpProperties member value.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_010: [**The function shall open both receiver and sender link.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_044: [**The function shall set the link's state to OPENING.**]**


### closeLinks

```java
protected void closeLinks();
```

**SRS_AMQPSDEVICEOPERATIONS_12_011: [**If the sender link is not null the function shall close it and sets it to null.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_012: [**If the receiver link is not null the function shall close it and sets it to null.**]**


### initLink

```java
protected void initLink(Link link) throws IOException, IllegalArgumentException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_013: [**The function shall throw IllegalArgumentException if the link argument is null.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_014: [**If the link is the sender link, the function shall create a new Target (Proton) object using the sender link address member variable.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_015: [**If the link is the sender link, the function shall set its target to the created Target (Proton) object.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_016: [**If the link is the sender link, the function shall set the SenderSettleMode to UNSETTLED.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_017: [**If the link is the receiver link, the function shall create a new Source (Proton) object using the receiver link address member variable.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_018: [**If the link is the receiver link and the linkState is OPENING, the function shall set its source to the created Source (Proton) object.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_045: [**The function do nothing if the either the receiver or the sender link state is other than OPENING.**]**



### sendMessageAndGetDeliveryHash

```java
protected int sendMessageAndGetDeliveryHash(byte[] msgData, int offset, int length, byte[] deliveryTag) throws IllegalStateException, IllegalArgumentException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_019: [**The function shall throw IllegalStateException if the sender link is not initialized.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_020: [**The function shall throw IllegalArgumentException if the deliveryTag length is zero.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_021: [**The function shall create a Delivery object using the sender link and the deliveryTag.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_022: [**The function shall try to send the message data using the sender link with the offset and length argument.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_023: [**The function shall advance the sender link.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_024: [**The function shall set the delivery hash to the value returned by the sender link.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_025: [**If proton failed sending the function shall advance the sender link, release the delivery object and sets the delivery hash to -1.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_026: [**The function shall return with the delivery hash.**]**


### getMessageFromReceiverLink

```java
protected AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_036: [**The function shall throw IllegalArgumentException if the linkName is empty.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_043: [**The function shall return null if the linkName does not match with the receiverLink tag.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_037: [**The function shall create a Delivery object from the link.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_033: [**The function shall try to read the full message from the delivery object and if it fails return null.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_034: [**The function shall read the full message into a buffer.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_035: [**The function shall advance the receiver link.**]**

**SRS_AMQPSDEVICEOPERATIONS_12_038: [**The function shall create a Proton message from the received buffer and return with it.**]**


### operationLinksOpened
```java
public Boolean operationLinksOpened();
```

**SRS_AMQPSDEVICEOPERATIONS_12_047: [**The function shall return true if all link are opened, false otherwise.**]**


### isLinkFound
```java
protected Boolean isLinkFound(String linkName)
```

**SRS_AMQPSDEVICEOPERATIONS_12_046: [**The prototype function shall return null.**]**


### convertFromProton

```java
protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_039: [**The prototype function shall return null.**]**


### convertToProton

```java
protected AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_040: [**The prototype function shall return null.**]**


### protonMessageToIoTHubMessage

```java
protected Message protonMessageToIoTHubMessage(MessageImpl protonMsg) throws IOException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_041: [**The prototype function shall return null.**]**


### iotHubMessageToProtonMessage

```java
protected MessageImpl iotHubMessageToProtonMessage(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
```

**SRS_AMQPSDEVICEOPERATIONS_12_042: [**The prototype function shall return null.**]**


### getAmqpProperties

```java
Map<Symbol, Object> getAmqpProperties();
```

**SRS_AMQPSDEVICEOPERATIONS_12_027: [**The getter shall return with the value of the amqpProperties.**]**


### getSenderLinkTag

```java
String getSenderLinkTag();
```

**SRS_AMQPSDEVICEOPERATIONS_12_028: [**The getter shall return with the value of the sender link tag.**]**


### getReceiverLinkTag

```java
String getReceiverLinkTag();
```


**SRS_AMQPSDEVICEOPERATIONS_12_029: [**The getter shall return with the value of the receiver link tag.**]**


### getSenderLinkAddress

```java
String getSenderLinkAddress();
```

**SRS_AMQPSDEVICEOPERATIONS_12_030: [**The getter shall return with the value of the sender link address.**]**


### getReceiverLinkAddress

```java
String getReceiverLinkAddress();
```

**SRS_AMQPSDEVICEOPERATIONS_12_031: [**The getter shall return with the value of the receiver link address.**]**
