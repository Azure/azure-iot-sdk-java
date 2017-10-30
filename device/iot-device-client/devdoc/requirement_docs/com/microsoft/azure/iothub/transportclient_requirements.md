# TransportClient Requirements

## Overview

Provides transport abstraction interface to device client.

## References

## Exposed API

```java
public final class TransportClient
{
    public TransportClient(IotHubClientProtocol protocol);
    public void open() throws IOException;
    public void closeNow() throws IOException;
    public void setSendInterval(long newIntervalInMilliseconds);
    void registerDeviceClient(DeviceClient deviceClient);
    void updateRegisteredDeviceClient(DeviceClient deviceClient)
    TransportClientState getTransportClientState()
}
```

### TransportClient
```java
public TransportClient(IotHubClientProtocol protocol);
```

**SRS_TRANSPORTCLIENT_12_001: [**If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.**]**

**SRS_TRANSPORTCLIENT_12_002: [**The constructor shall store the provided protocol.**]** 

**SRS_TRANSPORTCLIENT_12_003: [**The constructor shall set the the deviceIO to null.**]**

**SRS_TRANSPORTCLIENT_12_004: [**The constructor shall initialize the device list member.**]**


### open
```java
public void open() throws IOException;
```

**SRS_TRANSPORTCLIENT_12_008: [**The function shall throw  IllegalStateException if the connection is already open.**]**

**SRS_TRANSPORTCLIENT_12_009: [**The function shall do nothing if the the registration list is empty.**]**

**SRS_TRANSPORTCLIENT_12_010: [**The function shall renew each device client token if it is expired.**]**

**SRS_TRANSPORTCLIENT_12_011: [**The function shall create a new DeviceIO using the first registered device client's configuration.**]**

**SRS_TRANSPORTCLIENT_12_012: [**The function shall set the created DeviceIO to all registered device client.**]**

**SRS_TRANSPORTCLIENT_12_013: [**The function shall open the transport in multiplexing mode.**]**


### close
```java
public void closeNow() throws IOException;
```

**SRS_TRANSPORTCLIENT_12_014: [**If the deviceIO not null the function shall call multiplexClose on the deviceIO and set the deviceIO to null.**]**

**SRS_TRANSPORTCLIENT_12_015: [**If the registered device list is not empty the function shall call closeFileUpload on all devices.**]**

**SRS_TRANSPORTCLIENT_12_016: [**The function shall clear the registered device list.**]**


### setSendInterval

```java
public void setSendInterval(long newIntervalInMilliseconds);
```

**SRS_TRANSPORTCLIENT_12_017: [**The function shall throw IllegalArgumentException if the newIntervalInMilliseconds parameter is less or equql to zero.**]**

**SRS_TRANSPORTCLIENT_12_023: [**The function shall throw  IllegalStateException if the connection is already open.**]**

**SRS_TRANSPORTCLIENT_12_018: [**The function shall set the new interval on the underlying device IO it the transport client is not open.**]**


### registerDeviceClient
```java
void registerDeviceClient(DeviceClient deviceClient);
```

**SRS_TRANSPORTCLIENT_12_005: [**The function shall throw IllegalArgumentException if the deviceClient parameter is null.**]**

**SRS_TRANSPORTCLIENT_12_006: [**The function shall throw IllegalStateException if the connection is already open.**]**

**SRS_TRANSPORTCLIENT_12_007: [**The function shall add the given device client to the deviceClientList.**]**


### updateDeviceConfig

```java
void updateRegisteredDeviceClient(DeviceClient deviceClient)
```

**SRS_TRANSPORTCLIENT_12_020: [**The function shall throw IllegalArgumentException if the deviceClient parameter is null.**]**

**SRS_TRANSPORTCLIENT_12_021: [**The function shall throw IllegalStateException if the connection is already open.**]**

**SRS_TRANSPORTCLIENT_12_022: [**The function shall find the device client in the registered client list and update it.**]**


### getTransportClientState

```java
TransportClientState getTransportClientState()
```

**SRS_TRANSPORTCLIENT_12_019: [**The getter shall return with the value of the transportClientState.**]**

