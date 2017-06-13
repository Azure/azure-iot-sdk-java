# HttpsTransportManager Requirements

## Overview

An HTTPS transport Manager. Contains functionality for send and receive messages to/from an IoT Hub. 
It is an implementation for IotHubTransportManager interface.

## References

## Exposed API

```java
public class HttpsTransportManager implements IotHubTransportManager
{
   public HttpsTransportManager(DeviceClientConfig config) throws IllegalArgumentException;

    public void open();
    public void open(String[] topics);
    public void close();
    
    public ResponseMessage send(Message message) throws IOException, IllegalArgumentException;
    public Message receive() throws IOException;
}
```

### HttpsTransportManager
```java
public HttpsTransportManager(DeviceClientConfig config) throws IllegalArgumentException;
```
**SRS_HTTPSTRANSPORTMANAGER_21_001: [**The constructor shall store the device client configuration `config`.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_002: [**If the provided `config` is null, the constructor shall throws IllegalArgumentException.**]**  

### open
```java
public void open();
```
**SRS_HTTPSTRANSPORTMANAGER_21_003: [**The open shall create and store a new transport connection `HttpsIotHubConnection`.**]**  

### open
```java
public void open(String[] topics);
```
**SRS_HTTPSTRANSPORTMANAGER_21_004: [**The open shall create and store a new transport connection `HttpsIotHubConnection`.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_005: [**The open shall ignore the parameter `topics`.**]**  

### close
```java
public void close();
```
**SRS_HTTPSTRANSPORTMANAGER_21_006: [**The close shall destroy the transport connection `HttpsIotHubConnection`.**]**  

### send
```java
public ResponseMessage send(Message message) throws IOException, IllegalArgumentException;
```
**SRS_HTTPSTRANSPORTMANAGER_21_007: [**The send shall create a new instance of the `HttpMessage`, by parsing the Message with `parseHttpsJsonMessage` from `HttpsSingleMessage`.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_008: [**If send failed to parse the message, it shall bypass the exception.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_009: [**If the IotHubMethod is `GET`, the send shall set the httpsMethod as `GET`.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_010: [**If the IotHubMethod is `POST`, the send shall set the httpsMethod as `POST`.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_011: [**If the IotHubMethod is not `GET` or `POST`, the send shall throws IllegalArgumentException.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_012: [**The send shall set the httpsPath with the uriPath in the message.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_013: [**The send shall call `sendHttpsMessage` from `HttpsIotHubConnection` to send the message.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_014: [**If `sendHttpsMessage` failed, the send shall bypass the exception.**]**  

### receive
```java
public Message receive() throws IOException;
```
**SRS_HTTPSTRANSPORTMANAGER_21_015: [**The receive shall receive and bypass message from `HttpsIotHubConnection`, by calling `receiveMessage`.**]**  
**SRS_HTTPSTRANSPORTMANAGER_21_016: [**If `receiveMessage` failed, the receive shall bypass the exception.**]**  
