# AmqpsIotHubConnection Requirements
 
## Overview

An AMQPS IotHub connection between a device and an IoTHub. This class contains functionality for sending/receiving a message, and logic to re-establish the connection with the IoTHub in case it gets lost.

## References

## Exposed API

```java
public final class AmqpsIotHubConnection extends BaseHandler
{
	public AmqpsIotHubConnection(DeviceClientConfig config, Boolean useWebSockets);
	public void open() throws IOException;
	public void close();
    public Integer sendMessage(Message message);
    public Boolean sendMessageResult(AmqpsMessage message, IotHubMessageResult result);
	
	public void onConnectionInit(Event event);
	public void onConnectionBound(Event event);
	public void onReactorInit(Event event);
	public void onDelivery(Event event);
	public void onLinkFlow(Event event);
	public void onLinkRemoteClose(Event event);
	public void onLinkRemoteOpen(Event event);
	public void onLinkInit(Event event);
	public void onTransportError(Event event);
}
```


### AmqpsIotHubConnection

```java
public AmqpsIotHubConnection(DeviceClientConfig config, Boolean useWebSockets);
```

**SRS_AMQPSIOTHUBCONNECTION_15_001: [**The constructor shall throw IllegalArgumentException if any of the parameters of the configuration is null or empty.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_002: [**The constructor shall save the configuration into private member variables.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_001: [**The constructor shall save the device operation list to private member variable.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_004: [**The constructor shall initialize a new Handshaker (Proton) object to handle communication handshake.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_005: [**The constructor shall initialize a new FlowController (Proton) object to handle communication flow.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_006: [**The constructor shall set its state to CLOSED.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_002: [**The constructor shall create a Proton reactor.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_003: [**The constructor shall throw IOException if the Proton reactor creation failed.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_053: [**If the config is using x509 Authentication, the created Proton reactor shall not have SASL enabled by default.**]**


### open

```java
public void open() throws IOException
```

**SRS_AMQPSIOTHUBCONNECTION_15_007: [**If the AMQPS connection is already open, the function shall do nothing.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_009: [**The function shall trigger the Reactor (Proton) to begin running.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_010: [**The function shall wait for the reactor to be ready and for enough link credit to become available.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_011: [**If any exception is thrown while attempting to trigger the reactor, the function shall close the connection and throw an IOException.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_052: [**If the config is not using sas token authentication, then the created iotHubReactor shall omit the Sasl.**]**


### close

```java
public synchronized void close()
```

**SRS_AMQPSIOTHUBCONNECTION_15_048 [**If the AMQPS connection is already closed, the function shall do nothing.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_012: [**The function shall set the status of the AMQPS connection to CLOSED.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_013: [**The function shall close the AMQPS sender and receiver links, the AMQP session and the AMQP connection.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_014: [**The function shall stop the Proton reactor.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_004: [**The function shall throw IOException if the waitLock throws.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_005: [**The function shall throw IOException if the executor shutdown is interrupted.**]**


### sendMessage

```java
public Integer sendMessage(Message message)
```

**SRS_AMQPSIOTHUBCONNECTION_15_015: [**If the state of the connection is CLOSED or there is not enough credit, the function shall return -1.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_016: [**The function shall encode the message and copy the contents to the byte buffer.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_017: [**The function shall set the delivery tag for the sender.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_021: [**The function shall return the delivery hash.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_006: [**The function shall call sendMessageAndGetDeliveryHash on all device operation objects.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_007: [**The function shall doubles the buffer if encode throws BufferOverflowException.**]**

### sendMessageResult

```java
public Boolean sendMessageResult(AmqpsMessage message, IotHubMessageResult result)
```

**SRS_AMQPSIOTHUBCONNECTION_15_022: [**If the AMQPS Connection is closed, the function shall return false.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_023: [**If the message result is COMPLETE, ABANDON, or REJECT, the function shall acknowledge the last message with acknowledgement type COMPLETE, ABANDON, or REJECT respectively.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_024: [**The function shall return true after the message was acknowledged.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_008: [**The function shall return false if message acknowledge throws exception.**]**


## onConnectionInit

```java
public void onConnectionInit(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_025: [**The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_026: [**The event handler shall create a Session (Proton) object from the connection.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_029: [**The event handler shall open the connection, session, sender and receiver objects.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_009: [**The event handler shall calls the openLink on all device operation objects.**]**


## onConnectionBound

```java
public void onConnectionBound(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_030: [**The event handler shall get the Transport (Proton) object from the event.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_031: [**The event handler shall set the SASL_PLAIN authentication on the transport using the given user name and sas token.**]**

**SRS_AMQPSIOTHUBCONNECTION_25_049: [**The event handler shall set the SSL Context to IOTHub SSL context containing valid certificates.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_032: [**The event handler shall set VERIFY_PEER authentication mode on the domain of the Transport.**]**


## onConnectionUnbound

```java
public void onConnectionUnbound(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_12_010: [**The function sets the state to closed.**]**


## onReactorInit

```java
public void onReactorInit(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_033: [**The event handler shall set the current handler to handle the connection events.**]**


## onReactorFinal

```java
public void onReactorFinal(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_12_011: [**The function shall call notify lock on close lock.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_012: [**The function shall set the reactor member variable to null.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_013: [**The function shall call openAsync and disable reconnection if it is a reconnection attempt.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_014: [**The function shall log the error if openAsync failed.**]**


## onDelivery

```java
public void onDelivery(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_038: [**If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_039: [**The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_050: [**All the listeners shall be notified that a message was received from the server.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_015: [**The function shall call getMessageFromReceiverLink on all device operation objects.**]**


## onLinkFlow

```java
public void onLinkFlow(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_040 [**The event handler shall save the remaining link credit.**]**


## onLinkRemoteOpen

```java
public void onLinkRemoteOpen(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_041 [**The connection state shall be considered OPEN when the sender link is open remotely.**]**
**SRS_AMQPSIOTHUBCONNECTION_99_001 [**All server listeners shall be notified when that the connection has been established.**]**
**SRS_AMQPSIOTHUBCONNECTION_21_051 [**The open lock shall be notified when that the connection has been established.**]**

## onLinkRemoteClose

```java
public void onLinkRemoteClose(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_042 [**The event handler shall attempt to reconnect to the IoTHub.**]**


## onLinkInit

```java
public void onLinkInit(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_12_016: [** The function shall get the link from the event and call device operation objects with it. **]**


## onTransportError

```java
public void onTransportError(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_048 [**The event handler shall attempt to reconnect to IoTHub.**]**
