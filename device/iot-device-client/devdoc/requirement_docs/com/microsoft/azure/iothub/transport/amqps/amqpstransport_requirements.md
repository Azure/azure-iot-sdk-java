# AmqpsTransport Requirements
 
## Overview

An AMQPS transport. Contains functionality for adding messages and sending messages to an IoT Hub. Buffers unsent messages until they are received by an IoT Hub. A transport is bound at construction to the following parameters: IoT Hub name, device ID and device key. The transport also receives messages from IoT Hub and invokes a user-defined message callback if a message and callback are found.

## References

## Exposed API

```java
public final class AmqpsTransport implements IotHubTransport, ServerListener
{
    public AmqpsTransport(DeviceClientConfig config, Boolean useWebSockets);

    public void open() throws IOException;
    public void close() throws IOException;

    public void addMessage(Message message, IotHubEventCallback callback, Object callbackContext) throws IllegalStateException;
    public void addMessage(Message message, IotHubResponseCallback callback, Object callbackContext) throws UnsupportedOperationException;
    
    public void sendMessages() throws IOException, IllegalStateException;
    public synchronized void invokeCallbacks() throws IllegalStateException;

    public void handleMessage() throws IllegalStateException;
    
    public synchronized void messageSent(Integer messageHash, Boolean deliveryState);
    public synchronized void connectionLost();
    public void connectionEstablished();
    public synchronized void messageReceived(AmqpsMessage message);

    public boolean isEmpty();

    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
}
```


### AmqpsTransport

```java
public AmqpsTransport(DeviceClientConfig config, Boolean useWebSockets);
```

**SRS_AMQPSTRANSPORT_15_001: [**The constructor shall save the input parameters into instance variables.**]**

**SRS_AMQPSTRANSPORT_15_002: [**The constructor shall set the transport state to CLOSED.**]**

**SRS_AMQPSTRANSPORT_12_001: [**The constructor shall create device operation list with DeviceTelemetry, DeviceMethods and DeviceTwin objects.**]**


### open

```java
public void open() throws IOException;
```

**SRS_AMQPSTRANSPORT_15_003: [**If an AMQPS connection is already open, the function shall do nothing.**]**

**SRS_AMQPSTRANSPORT_15_004: [**The function shall open an AMQPS connection with the IoT Hub given in the configuration.**]**

**SRS_AMQPSTRANSPORT_15_005: [**The function shall add the transport to the list of listeners subscribed to the connection events.**]**

**SRS_AMQPSTRANSPORT_15_006: [**If the connection was opened successfully, the transport state shall be set to OPEN.**]**

**SRS_AMQPSTRANSPORT_12_004: [**The function shall throw IOException if connection open throws.**]**


### close

```java
public void close() throws IOException;
```

**SRS_AMQPSTRANSPORT_15_007: [**If the AMQPS connection is closed, the function shall do nothing.**]*

**SRS_AMQPSTRANSPORT_99_036: [**The method shall remove all the messages which are in progress or waiting to be sent and add them to the callback list.**]*

**SRS_AMQPSTRANSPORT_99_037: [**The method shall invoke all the callbacks.**]*

**SRS_AMQPSTRANSPORT_15_008: [**The function shall close an AMQPS connection with the IoT Hub given in the configuration.**]**

**SRS_AMQPSTRANSPORT_15_009: [**The function shall set the transport state to CLOSED.**]**

**SRS_AMQPSTRANSPORT_12_005: [**The function shall add a new outbound packet to the callback list.**]**


### addMessage

```java
public void addMessage(Message msg, IotHubEventCallback callback, Object callbackContext);
```

**SRS_AMQPSTRANSPORT_15_010: [**If the AMQPS session is closed, the function shall throw an IllegalStateException.**]**

**SRS_AMQPSTRANSPORT_15_011: [**The function shall add a packet containing the message, callback, and callback context to the queue of messages waiting to be sent.**]**


### addMessage

```java
public void addMessage(Message msg, IotHubResponseCallback callback, Object callbackContext) throws UnsupportedOperationException;
```

**SRS_AMQPSTRANSPORT_21_040: [**The function shall throws `UnsupportedOperationException`.**]**


### sendMessages

```java
public void sendMessages() throws IOException;
```

**SRS_AMQPSTRANSPORT_15_012: [**If the AMQPS session is closed, the function shall throw an IllegalStateException.**]**

**SRS_AMQPSTRANSPORT_15_013: [**If there are no messages in the waiting list, the function shall return.**]**

**SRS_AMQPSTRANSPORT_15_014: [**The function shall attempt to send every message on its waiting list, one at a time.**]**

**SRS_AMQPSTRANSPORT_15_015: [**The function shall skip messages with null or empty body.**]**

**SRS_AMQPSTRANSPORT_15_016: [**If the sent message hash is valid, it is added to the in progress map.**]**

**SRS_AMQPSTRANSPORT_15_017: [**If the sent message hash is not valid, it is buffered to be sent in a subsequent attempt.**]**

**SRS_AMQPSTRANSPORT_15_037: [**The function shall attempt to send the Proton message to IoTHub using the underlying AMQPS connection.**]**

**SRS_AMQPSTRANSPORT_15_039: [**If the message is expired, the function shall create a callback with the MESSAGE_EXPIRED status and add it to the callback list.**]**

**SRS_AMQPSTRANSPORT_12_002: [**The function shall call device operation objects to convert the IoTHubMessage to Proton message.**]**

**SRS_AMQPSTRANSPORT_12_003: [**The function throws IllegalStateException if none of the device operation object could handle the conversion.**]**

**SRS_AMQPSTRANSPORT_34_041: [**If the config is using sas token authentication and its sas token has expired and cannot be renewed, the message shall not be sent, an UNAUTHORIZED message callback shall be added to the callback queue and SAS_TOKEN_EXPIRED state callback shall be fired.**]**


### invokeCallbacks

```java
public void invokeCallbacks() throws IllegalStateException;
```

**SRS_AMQPSTRANSPORT_15_019: [**If the transport is closed, the function shall throw an IllegalStateException.**]**

**SRS_AMQPSTRANSPORT_15_020: [**The function shall invoke all the callbacks from the callback queue.**]**


### handleMessage

```java
public void handleMessage() throws IOException;
```

**SRS_AMQPSTRANSPORT_15_021: [**If the transport is closed, the function shall throw an IllegalStateException.**]**

**SRS_AMQPSTRANSPORT_15_023: [**The function shall attempt to consume a message from the IoT Hub.**]**

**SRS_AMQPSTRANSPORT_15_024: [**If no message was received from IotHub, the function shall return.**]**

**SRS_AMQPSTRANSPORT_15_026: [**The function shall invoke the callback on the message.**]**

**SRS_AMQPSTRANSPORT_15_027: [**The function shall return the message result (one of COMPLETE, ABANDON, or REJECT) to the IoT Hub.**]**

**SRS_AMQPSTRANSPORT_15_028: [**If the result could not be sent to IoTHub, the message shall be put back in the received messages queue to be processed again.**]**

**SRS_AMQPSTRANSPORT_12_006: [**The function shall call device operation objects to convert the Proton message to IoTHubMessage.**]**

**SRS_AMQPSTRANSPORT_12_007: [**The function throws IllegalStateException if none of the device operation object could handle the conversion.**]**

**SRS_AMQPSTRANSPORT_12_008: [**The function shall return if there is no message callback defined.**]**


### messageSent

```java
public synchronized void messageSent(Integer messageHash, Boolean deliveryState)
```

**SRS_AMQPSTRANSPORT_15_029: [**If the hash cannot be found in the list of keys for the messages in progress, the method returns.**]**

**SRS_AMQPSTRANSPORT_15_030: [**If the message was successfully delivered, its callback is added to the list of callbacks to be executed.]**]**

**SRS_AMQPSTRANSPORT_15_031: [**If the message was not delivered successfully, it is buffered to be sent again.**]**


### connectionLost

```java
public synchronized void connectionLost()
```

**SRS_AMQPSTRANSPORT_15_032: [**The messages in progress are buffered to be sent again.**]**

**SRS_AMQPSTRANSPORT_15_033: [**The map of messages in progress is cleared.**]**

**SRS_AMQPSTRANSPORT_99_001: [**Registered connection state callback is notified that the connection has been lost.**]**


### connectionEstablished

```java
public synchronized void connectionEstablished();
```

**SRS_AMQPSTRANSPORT_99_002: [**Registered connection state callback is notified that the connection has been established.**]**


### messageReceived

```java
public synchronized void messageReceived(AmqpsMessage message)
```

**SRS_AMQPSTRANSPORT_15_034: [**The message received is added to the list of messages to be processed.**]**


### isEmpty

```java
public boolean isEmpty();
```

**SRS_AMQPSTRANSPORT_15_035: [**The function shall return true if the waiting list, in progress list and callback list are all empty, and false otherwise.**]**


### registerConnectionStateCallback

```java
public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext)
```

**SRS_AMQPSTRANSPORT_99_003: [**The registerConnectionStateCallback shall register the connection state callback.**]**

**SRS_AMQPSTRANSPORT_34_042: [**If the provided callback is null, an IllegalArgumentException shall be thrown.**]**
