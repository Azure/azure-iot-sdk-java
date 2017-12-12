# AmqpsSessionDeviceOperation Requirements

## Overview

Provide IoTHub Device Client functionalites (Telemetry, Methodas, Twin).

## References

## Exposed API


```java
class AmqpsSessionDeviceOperation
{
    AmqpsSessionDeviceOperation(DeviceClientConfig deviceClientConfig, AmqpsDeviceAuthentication amqpsDeviceAuthentication);
    public void close()
    public void authenticate() throws IOException;
    public void renewToken() throws IOException
    public AmqpsDeviceAuthenticationState getAmqpsAuthenticatorState();
    public Boolean operationLinksOpened();
    void openLinks(Session session) throws IOException, IllegalArgumentException;
    void closeLinks();
    void initLink(Link link) throws IOException, IllegalArgumentException
    Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, IotHubConnectionString iotHubConnectionString) throws IOException;
    AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    Boolean isLinkFound(String linkName);
    AmqpsConvertToProtonReturnValue convertToProton(Message message) throws IOException;
    AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```


### AmqpsSessionDeviceOperation

```java
AmqpsSessionDeviceOperation(DeviceClientConfig deviceClientConfig, AmqpsDeviceAuthentication amqpsDeviceAuthentication);
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_001: [**The constructor shall throw IllegalArgumentException if the deviceClientConfig or the amqpsDeviceAuthentication parameter is null.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_002: [**The constructor shall save the deviceClientConfig and amqpsDeviceAuthentication parameter value to a member variable.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_003: [**The constructor shall create AmqpsDeviceTelemetry, AmqpsDeviceMethods and AmqpsDeviceTwin and add them to the device operations list. **]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_004: [**The constructor shall set the authentication state to not authenticated if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_044: [**The constructor shall calculate the token renewal period as the 75% of the expiration period.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_048: [**The constructor saves the calculated renewal period if it is greater than zero.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_045: [**The constructor shall create AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_046: [**The constructor shall create and start a scheduler with the calculated renewal period for AmqpsDeviceAuthenticationCBSTokenRenewalTask if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_047: [**The constructor shall set the authentication state to authenticated if the authentication type is not CBS.**]**


### close()

```java
public void close()
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_058: [**The function shall shut down the executor threads.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_059: [**The function shall close the operation links.**]**


### authenticate

```java
public void authenticate() throws IOException;
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_005: [**The function shall set the authentication state to not authenticated if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_006: [**The function shall start the authentication if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_060: [**The function shall create a new UUID and add it to the correlationIdList if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_061: [**The function shall use the correlationID to call authenticate on the authentication object if the authentication type is CBS.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_062: [**The function shall start the authentication process and start the lock wait if the authentication type is CBS.**]**



### renewToken

```java
public void renewToken() throws IOException
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_050: [**The function shall renew the sas token if the authentication type is CBS and the authentication state is authenticated.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_051: [**The function start the authentication with the new token.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_052: [**The function shall restart the scheduler with the calculated renewal period if the authentication type is CBS.**]**


### getAmqpsAuthenticatorState

```java
public AmqpsDeviceAuthenticationState getAmqpsAuthenticatorState();
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_007: [**The function shall return the current authentication state.**]**


### operationLinksOpened

```java
public Boolean operationLinksOpened();
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_008: [**The function shall return true if all operation links are opene, otherwise return false.**]**


### openLinks

```java
void openLinks(Session session) throws IOException, IllegalArgumentException;
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_042: [**The function shall do nothing if the session parameter is null.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_009: [**The function shall call openLinks on all device operations if the authentication state is authenticated.**]**


### closeLinks

```java
void closeLinks();
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_010: [**The function shall call closeLinks on all device operations.**]**


### initLink

```java
void initLink(Link link) throws IOException, IllegalArgumentException;
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_043: [**The function shall do nothing if the link parameter is null.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_011: [**The function shall call initLink on all device operations.**]****]**


### sendMessage

```java
Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, IotHubConnectionString iotHubConnectionString) throws IOException;
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_012: [**The function shall return -1 if the state is not authenticated.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_013: [**The function shall return -1 if the deviceId int he connection string is not equeal to the deviceId in the config.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_014: [**The function shall encode the message and copy the contents to the byte buffer.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_015: [**The function shall doubles the buffer if encode throws BufferOverflowException.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_017: [**The function shall set the delivery tag for the sender.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_018: [**The function shall call sendMessageAndGetDeliveryHash on all device operation objects.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_019: [**The function shall return the delivery hash.**]**


### getMessageFromReceiverLink

```java
AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSESSIONDEVICEOPERATION_12_023: [**If the state is authenticating the function shall call getMessageFromReceiverLink on the authentication object.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_055: [**The function shall find the correlation ID in the correlationIdlist.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_053: [**The function shall call authenticationMessageReceived with the correlation ID on the authentication object and if it returns true set the authentication state to authenticated.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_054: [**The function shall call notify the lock if after receiving the message and the authentication is in authenticating state.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_056: [**The function shall remove the correlationId from the list if it is found.**]**

**SRS_AMQPSESSIONDEVICEOPERATION_12_057: [**If the state is other than authenticating the function shall try to read the message from the device operation objects.**]**


### isLinkFound

```java
Boolean isLinkFound(String linkName);
```    

**SRS_AMQPSESSIONDEVICEOPERATION_12_024: [**The function shall return true if any of the operation's link name is a match and return false otherwise.**]**


### convertToProton

```java
AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
```    

**SRS_AMQPSESSIONDEVICEOPERATION_12_040: [**The function shall call all device operation's convertToProton, and if any of them not null return with the value.**]**

### convertFromProton

```java
AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```    

**SRS_AMQPSESSIONDEVICEOPERATION_12_041: [**The function shall call all device operation's convertFromProton, and if any of them not null return with the value.**]**

