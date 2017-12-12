# AmqpsSessionManager Requirements

## Overview

Provide management for multiple AmqpsSessionDeviceOperation.

## References

## Exposed API


```java
class AmqpsSessionManager
{
    AmqpsSessionManager(DeviceClientConfig deviceClientConfig);
    void addDeviceOperationSession(DeviceClientConfig deviceClientConfig);
    void closeNow();
    public void authenticate() throws IOException;
    public void openDeviceOperationLinks() throws IOException;
    void onConnectionInit(Connection connection) throws IOException;
    void onConnectionBound(Transport transport, SSLContext iotHubSSlContext);
    void onLinkInit(Link link) throws IOException, IllegalArgumentException;
    Boolean onLinkRemoteOpen(Event event);
    Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, IotHubConnectionString iotHubConnectionString) throws IOException;
    AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
    Boolean isLinkFound(String linkName);
    Boolean isAuthenticationOpened();
    AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
    AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```


### AmqpsSessionManager

```java
AmqpsSessionManager(DeviceClientConfig deviceClientConfig);
```

**SRS_AMQPSESSIONMANAGER_12_001: [**The constructor shall throw IllegalArgumentException if the deviceClientConfig parameter is null.**]**

**SRS_AMQPSESSIONMANAGER_12_002: [**The constructor shall save the deviceClientConfig parameter value to a member variable.**]**

**SRS_AMQPSESSIONMANAGER_12_005: [**The constructor shall create AmqpsDeviceAuthenticationCBSSendTask if the authentication type is CBS.**]**

**SRS_AMQPSESSIONMANAGER_12_006: [**The constructor shall create and start a scheduler for AmqpsDeviceAuthenticationCBSSendTask if the authentication type is CBS.**]**

**SRS_AMQPSESSIONMANAGER_12_007: [**The constructor shall add the create a AmqpsSessionDeviceOperation with the given deviceClientConfig.**]**


### addDeviceOperationSession

```java
void addDeviceOperationSession(DeviceClientConfig deviceClientConfig);
```

**SRS_AMQPSESSIONMANAGER_12_008: [**The function shall throw IllegalArgumentException if the deviceClientConfig parameter is null.**]**

**SRS_AMQPSESSIONMANAGER_12_009: [**The function shall create a new  AmqpsSessionDeviceOperation with the given deviceClietnConfig and add it to the session list.**]**


### close

```java
void closeNow();
```

**SRS_AMQPSESSIONMANAGER_12_010: [**The function shall call all device session to close links.**]**

**SRS_AMQPSESSIONMANAGER_12_011: [**The function shall close the authentication links.**]**

**SRS_AMQPSESSIONMANAGER_12_012: [**The function shall close the session.**]**

**SRS_AMQPSESSIONMANAGER_12_043: [**THe function shall shut down the scheduler.**]**


### authenticate

```java
public void authenticate() throws IOException;
```

**SRS_AMQPSESSIONMANAGER_12_014: [**The function shall do nothing if the authentication is not open.**]**

**SRS_AMQPSESSIONMANAGER_12_015: [**The function shall call authenticate on all session list members.**]**


### openDeviceOperationLinks

```java
public void openDeviceOperationLinks() throws IOException;
```

**SRS_AMQPSESSIONMANAGER_12_018: [**The function shall do nothing if the session is not open.**]**

**SRS_AMQPSESSIONMANAGER_12_019: [**The function shall call openLinks on all session list members.**]**

**SRS_AMQPSESSIONMANAGER_12_020: [**The function shall lock the execution with waitLock.**]**

**SRS_AMQPSESSIONMANAGER_12_021: [**The function shall throw IOException if the lock throws.**]**


### onConnectionInit

```java
void onConnectionInit(Connection connection) throws IOException;
```

**SRS_AMQPSESSIONMANAGER_12_023: [**The function shall initialize the session member variable from the connection if the session is null.**]**

**SRS_AMQPSESSIONMANAGER_12_024: [**The function shall open the initialized session.**]**

**SRS_AMQPSESSIONMANAGER_12_025: [**The function shall call authentication's openLink if the session is not null and the authentication is not open.**]**

**SRS_AMQPSESSIONMANAGER_12_042: [**The function shall call openLinks on all device sessions if the session is not null and the authentication is open.**]**


### onConnectionBound

```java
void onConnectionBound(Transport transport, SSLContext iotHubSSlContext);
```

**SRS_AMQPSESSIONMANAGER_12_026: [**The function shall call setSslDomain on authentication if the session is not null.**]**


### onLinkInit

```java
void onLinkInit(Link link) throws IOException, IllegalArgumentException;
```

**SRS_AMQPSESSIONMANAGER_12_027: [**The function shall call authentication initLink on all session list member if the authentication is open and the session is not null.**]**

**SRS_AMQPSESSIONMANAGER_12_028: [**The function shall call authentication initLink if the authentication is not open and the session is not null.**]**


### onLinkRemoteOpen

```java
Boolean onLinkRemoteOpen(Event event);
```

**SRS_AMQPSESSIONMANAGER_12_029: [**The function shall call authentication isLinkFound if the authentication is not open and return true if both links are open**]**

**SRS_AMQPSESSIONMANAGER_12_030: [**The function shall call authentication isLinkFound if the authentication is not open and return false if only one link is open**]**

**SRS_AMQPSESSIONMANAGER_12_031: [**The function shall call all all device session's isLinkFound, and if both links are opened notify the lock.**]**


### sendMessage

```java
Integer sendMessage(org.apache.qpid.proton.message.Message message, MessageType messageType, IotHubConnectionString iotHubConnectionString) throws IOException;
```

**SRS_AMQPSESSIONMANAGER_12_032: [**The function shall call sendMessage on all session list member and if there is a successful send return with the deliveryHash, otherwise return -1.**]**


### getMessageFromReceiverLink

```java
AmqpsMessage getMessageFromReceiverLink(String linkName) throws IllegalArgumentException, IOException;
```

**SRS_AMQPSESSIONMANAGER_12_033: [**The function shall do nothing and return null if the session is not open.**]**

**SRS_AMQPSESSIONMANAGER_12_034: [**The function shall call authentication getMessageFromReceiverLink if the authentication is not open.**]**

**SRS_AMQPSESSIONMANAGER_12_035: [**The function shall call device sessions getMessageFromReceiverLink if the authentication is open.**]**


### isLinkFound

```java
Boolean isLinkFound(String linkName);
```    

**SRS_AMQPSESSIONMANAGER_12_037: [**The function shall return with the authentication isLinkFound's return value if the authentication is not open.**]**

**SRS_AMQPSESSIONMANAGER_12_038: [**The function shall call all device session's isLinkFound, and if any of them true return true otherwise return false.**]**



### isAuthenticationOpened

```java
Boolean isAuthenticationOpened();
```    

**SRS_AMQPSESSIONMANAGER_12_039: [**The function shall return with the return value of authentication.operationLinksOpened.**]**


### convertToProton

```java
AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
```    

**SRS_AMQPSESSIONMANAGER_12_040: [**The function shall call all device session's convertToProton, and if any of them not null return with the value.**]**


```java
AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```    

**SRS_AMQPSESSIONMANAGER_12_041: [**The function shall call all device session's convertFromProton, and if any of them not null return with the value.**]**
