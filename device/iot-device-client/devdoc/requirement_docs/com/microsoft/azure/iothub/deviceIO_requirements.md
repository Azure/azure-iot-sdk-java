# DeviceIO Requirements

## Overview

Allows a single logical or physical device to connect to an IoT Hub.

## References

## Exposed API

```java
public final class DeviceIO
{
    public DeviceIO(DeviceClientConfig config, IotHubClientProtocol protocol,
                    long sendPeriodInMilliseconds, long receivePeriodInMilliseconds)
            throws URISyntaxException;

    public void open() throws IOException;
    public void close() throws IOException;

    public void sendEventAsync(Message message,
                               IotHubEventCallback callback,
                               Object callbackContext);

    public void sendEventAsync(Message message,
                               IotHubResponseCallback callback,
                               Object callbackContext);

    public long getReceivePeriodInMilliseconds();
    public void setReceivePeriodInMilliseconds(long newIntervalInMilliseconds) throws IOException;
    public long getSendPeriodInMilliseconds();
    public void setSendPeriodInMilliseconds(long newIntervalInMilliseconds) throws IOException;

    public IotHubClientProtocol getProtocol();
    public boolean isOpen();
    public boolean isEmpty();
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
}
```


### DeviceIO
```java
public DeviceIO(DeviceClientConfig config, IotHubClientProtocol protocol,
                long sendPeriodInMilliseconds, long receivePeriodInMilliseconds)
        throws URISyntaxException
```
**SRS_DEVICE_IO_21_001: [**The constructor shall store the provided protocol and config information.**]**  
**SRS_DEVICE_IO_21_002: [**If the `config` is null, the constructor shall throw an IllegalArgumentException.**]**  
**SRS_DEVICE_IO_21_003: [**The constructor shall initialize the IoT Hub transport that uses the `protocol` specified.**]**  
**SRS_DEVICE_IO_21_004: [**If the `protocol` is null, the constructor shall throw an IllegalArgumentException.**]**  
**SRS_DEVICE_IO_21_005: [**If the `protocol` is not valid, the constructor shall throw an IllegalArgumentException.**]**  
**SRS_DEVICE_IO_21_006: [**The constructor shall set the `state` as `CLOSED`.**]**  
**SRS_DEVICE_IO_21_037: [**The constructor shall initialize the `sendPeriodInMilliseconds` with default value of 10 milliseconds.**]**  
**SRS_DEVICE_IO_21_038: [**The constructor shall initialize the `receivePeriodInMilliseconds` with default value of each protocol.**]**  

### open
```java
public void open() throws IOException
```
**SRS_DEVICE_IO_21_007: [**If the client is already open, the open shall do nothing.**]**  
**SRS_DEVICE_IO_21_012: [**The open shall open the transport to communicate with an IoT Hub.**]**  
**SRS_DEVICE_IO_21_013: [**The open shall schedule send tasks to run every SEND_PERIOD_MILLIS milliseconds.**]**  
**SRS_DEVICE_IO_21_014: [**The open shall schedule receive tasks to run every RECEIVE_PERIOD_MILLIS milliseconds.**]**  
**SRS_DEVICE_IO_21_015: [**If an error occurs in opening the transport, the open shall throw an IOException.**]**  
**SRS_DEVICE_IO_21_016: [**The open shall set the `state` as `OPEN`.**]**

### close
```java
public void close() throws IOException
```
**SRS_DEVICE_IO_21_017: [**The close shall finish all ongoing tasks.**]**  
**SRS_DEVICE_IO_21_018: [**The close shall cancel all recurring tasks.**]**  
**SRS_DEVICE_IO_21_019: [**The close shall close the transport.**]**  
**SRS_DEVICE_IO_21_020: [**If the client is already closed, the close shall do nothing.**]**  
**SRS_DEVICE_IO_21_021: [**The close shall set the `state` as `CLOSE`.**]**  

### sendEventAsync
```java
public void sendEventAsync(Message message,
                           IotHubEventCallback callback,
                           Object callbackContext)
```
**SRS_DEVICE_IO_21_022: [**The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.**]**  
**SRS_DEVICE_IO_21_023: [**If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.**]**  
**SRS_DEVICE_IO_21_024: [**If the client is closed, the sendEventAsync shall throw an IllegalStateException.**]**  

### sendEventAsync
```java
public void sendEventAsync(Message message,
                           IotHubResponseCallback callback,
                           Object callbackContext)
```
**SRS_DEVICE_IO_21_040: [**The sendEventAsync shall add the message, with its associated callback and callback context, to the transport.**]**  
**SRS_DEVICE_IO_21_041: [**If the message given is null, the sendEventAsync shall throw an IllegalArgumentException.**]**  
**SRS_DEVICE_IO_21_042: [**If the client is closed, the sendEventAsync shall throw an IllegalStateException.**]**  

### getReceivePeriodInMilliseconds
```java
public long getReceivePeriodInMilliseconds()
```
**SRS_DEVICE_IO_21_026: [**The getReceivePeriodInMilliseconds shall return the receive period in milliseconds.**]**  

### setReceivePeriodInMilliseconds
```java
public void setReceivePeriodInMilliseconds(long newIntervalInMilliseconds) throws IOException
```
**SRS_DEVICE_IO_21_027: [**The setReceivePeriodInMilliseconds shall store the new receive period in milliseconds.**]**  
**SRS_DEVICE_IO_21_028: [**If the task scheduler already exists, the setReceivePeriodInMilliseconds shall change the `scheduleAtFixedRate` for the receiveTask to the new value.**]**  
**SRS_DEVICE_IO_21_029: [**If the `receiveTask` is null, the setReceivePeriodInMilliseconds shall throw IOException.**]**  
**SRS_DEVICE_IO_21_030: [**If the the provided interval is zero or negative, the setReceivePeriodInMilliseconds shall throw IllegalArgumentException.**]**  

### getSendPeriodInMilliseconds
```java
public long getSendPeriodInMilliseconds()
```
**SRS_DEVICE_IO_21_032: [**The getSendPeriodInMilliseconds shall return the send period in milliseconds.**]**  

### setSendPeriodInMilliseconds
```java
public void setSendPeriodInMilliseconds(long newIntervalInMilliseconds) throws IOException
```
**SRS_DEVICE_IO_21_033: [**The setSendPeriodInMilliseconds shall store the new send period in milliseconds.**]**  
**SRS_DEVICE_IO_21_034: [**If the task scheduler already exists, the setSendPeriodInMilliseconds shall change the `scheduleAtFixedRate` for the sendTask to the new value.**]**  
**SRS_DEVICE_IO_21_035: [**If the `sendTask` is null, the setSendPeriodInMilliseconds shall throw IOException.**]**  
**SRS_DEVICE_IO_21_036: [**If the the provided interval is zero or negative, the setSendPeriodInMilliseconds shall throw IllegalArgumentException.**]**  

### getProtocol
```java
public IotHubClientProtocol getProtocol();
```
**SRS_DEVICE_IO_21_025: [**The getProtocol shall return the protocol for transport.**]**  

### isOpen
```java
public boolean isOpen()
```
**SRS_DEVICE_IO_21_031: [**The isOpen shall return the connection state, true if connection is open, false if it is closed.**]**  

### isEmpty
```java
public boolean isEmpty()
```
**SRS_DEVICE_IO_21_039: [**The isEmpty shall return the transport queue state, true if the queue is empty, false if there is pending messages in the queue.**]**  

### registerConnectionStateCallback
```java
public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
```

**SRS_DEVICE_IO_99_001: [**The registerConnectionStateCallback shall register the callback with the transport.**]**
