# AmqpsIotHubConnection Requirements
 
## Overview

An AMQPS IotHub connection between a device and an IoTHub. This class contains functionality for sending/receiving a message, and logic to re-establish the connection with the IoTHub in case it gets lost.

## References

## Exposed API

```java
public final class AmqpsIotHubConnection extends BaseHandler
{
    public AmqpsIotHubConnection(DeviceClientConfig config, Boolean useWebSockets);
    public void addDeviceOperationSession(DeviceClientConfig deviceClientConfig);
    public void open() throws IOException;
    public void authenticate() throws IOException;
    public void openLinks() throws IOException;
    public void close();
    public Integer sendMessage(Message message)
    public Boolean sendMessageResult(AmqpsMessage message, IotHubMessageResult result);

    public void onReactorInit(Event event);
    public void onReactorFinal(Event event)

    public void onConnectionInit(Event event);
    public void onConnectionBound(Event event);
    public void onConnectionUnbound(Event event)

    public void onDelivery(Event event);

    public void onLinkInit(Event event);
    public void onLinkFlow(Event event);
    public void onLinkRemoteOpen(Event event);
    public void onLinkRemoteClose(Event event);

    public void onTransportError(Event event);

    public void addListener(IotHubListener listener);
    protected AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
    protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;

    static ConnectionStatusException getConnectionStatusExceptionFromAMQPExceptionCode(String exceptionCode);

}
```


### AmqpsIotHubConnection

```java
public AmqpsIotHubConnection(DeviceClientConfig config, Boolean useWebSockets);
```

**SRS_AMQPSIOTHUBCONNECTION_15_001: [**The constructor shall throw IllegalArgumentException if any of the parameters of the configuration is null or empty.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_002: [**The constructor shall save the configuration into private member variables.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_004: [**The constructor shall initialize a new Handshaker (Proton) object to handle communication handshake.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_005: [**The constructor shall initialize a new FlowController (Proton) object to handle communication flow.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_006: [**The constructor shall set its state to CLOSED.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_002: [**The constructor shall create a Proton reactor.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_003: [**The constructor shall throw IOException if the Proton reactor creation failed.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_001: [**The constructor shall initialize the AmqpsSessionManager member variable with the given config.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_017: [**The constructor shall set the AMQP socket port using the configuration.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_053: [**If the config is using x509 Authentication, the created Proton reactor shall not have SASL enabled by default.**]**


### addDeviceOperationSession

```java
public void addDeviceOperationSession(DeviceClientConfig deviceClientConfig)
```

**SRS_AMQPSIOTHUBCONNECTION_12_018: [**The function shall do nothing if the deviceClientConfig parameter is null.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_019: [**The function shall call AmqpsSessionManager.addDeviceOperationSession with the given deviceClientConfig.**]**


### open

```java
public void open() throws IOException
```

**SRS_AMQPSIOTHUBCONNECTION_15_007: [**If the AMQPS connection is already open, the function shall do nothing.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_009: [**The function shall trigger the Reactor (Proton) to begin running.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_010: [**The function shall wait for the reactor to be ready and for enough link credit to become available.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_011: [**If any exception is thrown while attempting to trigger the reactor, the function shall close the connection and throw an IOException.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_052: [**If the config is not using sas token authentication, then the created iotHubReactor shall omit the Sasl.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_059: [**The function shall call waitlock on openlock.**]** 

**SRS_AMQPSIOTHUBCONNECTION_12_057: [**The function shall call the connection to authenticate.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_058: [**The function shall call the connection to open device client links.**]**


### authenticate

```java
public void authenticate() throws IOException;
```	

**SRS_AMQPSIOTHUBCONNECTION_12_020: [**The function shall do nothing if the authentication is already open.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_021: [**The function shall call AmqpsSessionManager.authenticate.**]**


### openLinks

```java
public void openLinks() throws IOException;
```	

**SRS_AMQPSIOTHUBCONNECTION_12_022: [**The function shall do nothing if the authentication is already open.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_023: [**The function shall call AmqpsSessionManager.openDeviceOperationLinks.**]**


### close

```java
public synchronized void close()
```

**SRS_AMQPSIOTHUBCONNECTION_15_048: [**If the AMQPS connection is already closed, the function shall do nothing.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_012: [**The function shall set the status of the AMQPS connection to CLOSED.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_013: [**The function shall close the AmqpsSessionManager and the AMQP connection.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_014: [**The function shall stop the Proton reactor.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_004: [**The function shall throw IOException if the waitLock throws.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_005: [**The function shall throw IOException if the executor shutdown is interrupted.**]**


### sendMessage

```java
public Integer sendMessage(Message message)
```

**SRS_AMQPSIOTHUBCONNECTION_15_015: [**If the state of the connection is CLOSED or there is not enough credit, the function shall return -1.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_024: [**The function shall call AmqpsSessionManager.sendMessage with the given parameters.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_021: [**The function shall return the delivery hash.**]**


### sendMessageResult

```java
public Boolean sendMessageResult(AmqpsMessage message, IotHubMessageResult result)
```

**SRS_AMQPSIOTHUBCONNECTION_15_022: [**If the AMQPS Connection is closed, the function shall return false.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_023: [**If the message result is COMPLETE, ABANDON, or REJECT, the function shall acknowledge the last message with acknowledgement type COMPLETE, ABANDON, or REJECT respectively.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_024: [**The function shall return true after the message was acknowledged.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_008: [**The function shall return false if message acknowledge throws exception.**]**


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


## onConnectionInit

```java
public void onConnectionInit(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_025: [**The event handler shall get the Connection (Proton) object from the event handler and set the host name on the connection.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_029: [**The event handler shall open the connection.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_009: [**The event handler shall call the AmqpsSessionManager.onConnectionInit function with the connection.**]**


## onConnectionBound

```java
public void onConnectionBound(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_030: [**The event handler shall get the Transport (Proton) object from the event.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_031: [**The event handler shall call the AmqpsSessionManager.onConnectionBound with the transport and the SSLContext.**]**

**SRS_AMQPSIOTHUBCONNECTION_25_049: [**If websocket enabled the event handler shall configure the transport layer for websocket.**]**


## onConnectionUnbound

```java
public void onConnectionUnbound(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_12_010: [**The function sets the state to closed.**]**


## onDelivery

```java
public void onDelivery(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_038: [**If this link is the Sender link and the event type is DELIVERY, the event handler shall get the Delivery (Proton) object from the event.**]**

**SRS_AMQPSIOTHUBCONNECTION_15_039: [**The event handler shall note the remote delivery state and use it and the Delivery (Proton) hash code to inform the AmqpsIotHubConnection of the message receipt.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_015: [**The function shall call AmqpsSessionManager.getMessageFromReceiverLink.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_089: [**If an amqp message can be received from the receiver link, and that amqp message contains a status code that is not 200 or 204, this function shall notify this object's listeners that that message was received and provide the status code's mapped exception.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_090: [**If an amqp message can be received from the receiver link, and that amqp message contains a status code that is 200 or 204, this function shall notify this object's listeners that that message was received with a null exception.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_091: [**If an amqp message can be received from the receiver link, and that amqp message contains no status code, this function shall notify this object's listeners that that message was received with a null exception.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_092: [**If an amqp message can be received from the receiver link, and that amqp message contains no application properties, this function shall notify this object's listeners that that message was received with a null exception.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_093: [**If an amqp message can be received from the receiver link, and that amqp message contains a status code, but that status code cannot be parsed to an integer, this function shall notify this object's listeners that that message was received with a null exception.**]**


## onLinkInit

```java
public void onLinkInit(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_12_016: [** The function shall get the link from the event and call device operation objects with it. **]**


## onLinkFlow

```java
public void onLinkFlow(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_040 [**The event handler shall save the remaining link credit.**]**


## onLinkRemoteOpen

```java
public void onLinkRemoteOpen(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_041: [**The connection state shall be considered OPEN when the sender link is open remotely.**]**

**SRS_AMQPSIOTHUBCONNECTION_99_001: [**All server listeners shall be notified when that the connection has been established.**]**

**SRS_AMQPSIOTHUBCONNECTION_21_051: [**The open lock shall be notified when that the connection has been established.**]**

**SRS_AMQPSIOTHUBCONNECTION_12_052: [**The function shall call AmqpsSessionManager.onLinkRemoteOpen with the given link.**]**

## onLinkRemoteClose

```java
public void onLinkRemoteClose(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_042 [**The event handler shall attempt to reconnect to the IoTHub.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_061 [**If the provided event object's transport holds a remote error condition object, this function shall report the associated ConnectionStatusException to this object's listeners.**]**


## onTransportError

```java
public void onTransportError(Event event)
```

**SRS_AMQPSIOTHUBCONNECTION_15_048 [**The event handler shall attempt to reconnect to IoTHub.**]**

**SRS_AMQPSIOTHUBCONNECTION_34_060 [**If the provided event object's transport holds an error condition object, this function shall report the associated ConnectionStatusException to this object's listeners.**]**


### addListener

```java
public void addListener(IotHubListener listener);
```

**SRS_AMQPSIOTHUBCONNECTION_12_054: [**The function shall add the given listener to the listener list.**]**


### convertToProton

```java
protected AmqpsConvertToProtonReturnValue convertToProton(com.microsoft.azure.sdk.iot.device.Message message) throws IOException;
```

**SRS_AMQPSIOTHUBCONNECTION_12_055: [**The function shall call AmqpsSessionManager.convertToProton with the given message.**]**


### convertFromProton

```java
protected AmqpsConvertFromProtonReturnValue convertFromProton(AmqpsMessage amqpsMessage, DeviceClientConfig deviceClientConfig) throws IOException;
```

**SRS_AMQPSIOTHUBCONNECTION_12_056: [**The function shall call AmqpsSessionManager.convertFromProton with the given message.**]**


### getConnectionStatusExceptionFromAMQPExceptionCode
```java
static ConnectionStatusException getConnectionStatusExceptionFromAMQPExceptionCode(String exceptionCode)
```

**SRS_AMQPSIOTHUBCONNECTION_34_063: [**The function shall map amqp exception code "amqp:internal-error" to ConnectionStatusException "ConnectionStatusAmqpInternalErrorException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_064: [**The function shall map amqp exception code "amqp:not-found" to ConnectionStatusException "ConnectionStatusAmqpNotFoundException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_065: [**The function shall map amqp exception code "amqp:unauthorized-access" to ConnectionStatusException "ConnectionStatusAmqpUnauthorizedAcessException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_066: [**The function shall map amqp exception code "amqp:decode-error" to ConnectionStatusException "ConnectionStatusAmqpDecodeErrorException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_067: [**The function shall map amqp exception code "amqp:resource-limit-exceeded" to ConnectionStatusException "ConnectionStatusAmqpResourceLimitExceededException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_068: [**The function shall map amqp exception code "amqp:not-allowed" to ConnectionStatusException "ConnectionStatusAmqpNotAllowedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_069: [**The function shall map amqp exception code "amqp:invalid-field" to ConnectionStatusException "ConnectionStatusAmqpInvalidFieldException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_070: [**The function shall map amqp exception code "amqp:not-implemented" to ConnectionStatusException "ConnectionStatusAmqpNotImplementedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_071: [**The function shall map amqp exception code "amqp:resource-locked" to ConnectionStatusException "ConnectionStatusAmqpResourceLockedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_072: [**The function shall map amqp exception code "amqp:precondition-failed" to ConnectionStatusException "ConnectionStatusAmqpPreconditionFailedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_073: [**The function shall map amqp exception code "amqp:resource-deleted" to ConnectionStatusException "ConnectionStatusAmqpResourceDeletedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_074: [**The function shall map amqp exception code "amqp:illegal-state" to ConnectionStatusException "ConnectionStatusAmqpIllegalStateException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_075: [**The function shall map amqp exception code "amqp:frame-size-too-small" to ConnectionStatusException "ConnectionStatusAmqpFrameSizeTooSmallException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_076: [**The function shall map amqp exception code "amqp:connection:forced" to ConnectionStatusException "ConnectionStatusAmqpConnectionForcedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_077: [**The function shall map amqp exception code "amqp:connection:framing-error" to ConnectionStatusException "ConnectionStatusAmqpFramingErrorException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_078: [**The function shall map amqp exception code "amqp:connection:redirect" to ConnectionStatusException "ConnectionStatusAmqpConnectionRedirectException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_079: [**The function shall map amqp exception code "amqp:session:window-violation" to ConnectionStatusException "ConnectionStatusAmqpWindowViolationException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_080: [**The function shall map amqp exception code "amqp:session:errant-link" to ConnectionStatusException "ConnectionStatusAmqpErrantLinkException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_081: [**The function shall map amqp exception code "amqp:session:handle-in-use" to ConnectionStatusException "ConnectionStatusAmqpHandleInUseException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_082: [**The function shall map amqp exception code "amqp:session:unattached-handle" to ConnectionStatusException "ConnectionStatusAmqpUnattachedHandleException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_083: [**The function shall map amqp exception code "amqp:link:detach-forced" to ConnectionStatusException "ConnectionStatusAmqpDetachForcedException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_084: [**The function shall map amqp exception code "amqp:link:transfer-limit-exceeded" to ConnectionStatusException "ConnectionStatusAmqpTransferLimitExceededException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_085: [**The function shall map amqp exception code "amqp:link:message-size-exceeded" to ConnectionStatusException "ConnectionStatusAmqpMessageSizeExceededException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_086: [**The function shall map amqp exception code "amqp:link:redirect" to ConnectionStatusException "ConnectionStatusAmqpLinkRedirectException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_087: [**The function shall map amqp exception code "amqp:link:stolen" to ConnectionStatusException "ConnectionStatusAmqpLinkStolenException".**]**

**SRS_AMQPSIOTHUBCONNECTION_34_088: [**The function shall map all other amqp exception codes to the generic ConnectionStatusException "ProtocolConnectionStatusException".**]**
