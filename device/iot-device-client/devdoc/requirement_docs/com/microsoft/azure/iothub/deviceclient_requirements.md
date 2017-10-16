# DeviceClient Requirements

## Overview

Allows a single logical or physical device to connect to an IoT Hub.

## References

## Exposed API

```java
public final class DeviceClient
{
    public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException;
    public DeviceClient(String connString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IOException, IllegalArgumentException;

    public void open() throws IOException;
    public void close() throws IOException;
    public void closeNow() throws IOException;

    public void sendEventAsync(Message msg, IotHubEventCallback callback, Object callbackContext);    
    public DeviceClient setMessageCallback(IotHubMessageCallback callback, Object context);
    
    public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object    deviceTwinStatusCallbackContext, PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext) throws IOException;
    public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;
    public void sendReportedProperties(Set<Property> reportedProperties) throws IOException;  

    public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IOException  

    public void uploadToBlobAsync(String destinationBlobName, InputStream inputStream, long streamLength,
                                  IotHubEventCallback callback, Object callbackContext)
            throws IllegalArgumentException, IllegalStateException, IOException;
    
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
}
```

### DeviceClient

```java
public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException;
```

**SRS_DEVICECLIENT_21_001: [**The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.**]**  

**SRS_DEVICECLIENT_21_002: [**The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.**]**  

**SRS_DEVICECLIENT_21_003: [**The constructor shall save the connection configuration using the object DeviceClientConfig.**]**  

**SRS_DEVICECLIENT_21_004: [**If the connection string is null or empty, the function shall throw an IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_21_005: [**If protocol is null, the function shall throw an IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_34_055: [**If the provided connection string contains an expired SAS token, a SecurityException shall be thrown.**]**

```java
public DeviceClient(String connString, IotHubClientProtocol protocol, String publicKeyCertificate, boolean isPathForPublic, String privateKey, boolean isPathForPrivate) throws IOException, IllegalArgumentException;
```

**SRS_DEVICECLIENT_34_058: [**The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', using the object IotHubConnectionString.**]**  

**SRS_DEVICECLIENT_34_059: [**The constructor shall initialize the IoT Hub transport for the protocol specified, creating a instance of the deviceIO.**]**  

**SRS_DEVICECLIENT_34_060: [**The constructor shall save the connection configuration using the object DeviceClientConfig.**]**  

**SRS_DEVICECLIENT_34_061: [**If the connection string is null or empty, the function shall throw an IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_34_062: [**If protocol is null, the function shall throw an IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_34_063: [**This function shall save the provided certificate and key within its config.**]**


### open

```java
public void open() throws IOException;
```

**SRS_DEVICECLIENT_21_006: [**The open shall open the deviceIO connection.**]**  

**SRS_DEVICECLIENT_21_007: [**If the opening a connection via deviceIO is not successful, the open shall throw IOException.**]**  

**SRS_DEVICECLIENT_34_044: [**If the SAS token has expired before this call, throw a Security Exception**]**

### close

```java
public void close() throws IOException;
```

**SRS_DEVICECLIENT_11_040: [**The function shall finish all ongoing tasks.**]**  

**SRS_DEVICECLIENT_11_041: [**The function shall cancel all recurring tasks.**]**  

**SRS_DEVICECLIENT_21_042: [**The close shall close the deviceIO connection.**]**  

**SRS_DEVICECLIENT_21_043: [**If the closing a connection via deviceIO is not successful, the close shall throw IOException.**]**  


### closeNow

```java
public void closeNow();
```

**SRS_DEVICECLIENT_21_008: [**The close shall close the deviceIO connection.**]**  

**SRS_DEVICECLIENT_21_009: [**If the closing a connection via deviceIO is not successful, the close shall throw IOException.**]**  

**SRS_DEVICECLIENT_21_054: [**If the fileUpload is not null, the closeNow shall call closeNow on fileUpload.**]**  


### sendEventAsync

```java
public void sendEventAsync(Message msg, IotHubEventCallback callback, Object callbackContext);
```

**SRS_DEVICECLIENT_21_010: [**The sendEventAsync shall asynchronously send the message using the deviceIO connection.**]**  

**SRS_DEVICECLIENT_21_011: [**If starting to send via deviceIO is not successful, the sendEventAsync shall bypass the threw exception.**]**  

**SRS_DEVICECLIENT_34_045: [**If the SAS token has expired before this call, throw a Security Exception**]**


### setMessageCallback

```java
public DeviceClient setMessageCallback(IotHubMessageCallback callback, Object context);
```

**SRS_DEVICECLIENT_11_013: [**The function shall set the message callback, with its associated context.**]**  

**SRS_DEVICECLIENT_11_014: [**If the callback is null but the context is non-null, the function shall throw an IllegalArgumentException.**]**  


### setOption

```java
public setOption(String optionName, Object value)
```

This method sets the option given by optionName to value.

**SRS_DEVICECLIENT_02_015: [**If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.**]**

Options handled by the client:

**SRS_DEVICECLIENT_02_016: [**"SetMinimumPollingInterval" - time in milliseconds between 2 consecutive polls.**]**

**SRS_DEVICECLIENT_02_017: [**Option "SetMinimumPollingInterval" is available only for HTTP.**]**

**SRS_DEVICECLIENT_02_018: [**"SetMinimumPollingInterval" needs to have value type long**.]**

**SRS_DEVICECLIENT_21_040: [**"SetSendInterval" - time in milliseconds between 2 consecutive message sends.**]**

**SRS_DEVICECLIENT_21_041: [**"SetSendInterval" needs to have value type long**.]**

**SRS_DEVICECLIENT_25_019: [**"SetCertificatePath" - path to the certificate to verify peer .**]**

**SRS_DEVICECLIENT_25_020: [**"SetCertificatePath" is available only for AMQP.**]**

**SRS_DEVICECLIENT_25_021: [**"SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time.**]**

**SRS_DEVICECLIENT_25_022: [**"SetSASTokenExpiryTime" should have value type long.**]**

**SRS_DEVICECLIENT_25_023: [**"SetSASTokenExpiryTime" is available for HTTPS/AMQP/MQTT/AMQPS_WS/MQTT_WS**]**

**SRS_DEVICECLIENT_25_024: [**"SetSASTokenExpiryTime" shall restart the transport
                                    1. If the device currently uses device key and
                                    2. If transport is already open
                               after updating expiry time**.]**

**SRS_DEVICECLIENT_34_065: [**""SetSASTokenExpiryTime" if this option is called when not using sas token authentication, an IllegalStateException shall be thrown.**]**


### startDeviceTwin

```java
public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object    deviceTwinStatusCallbackContext, PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext) throws IOException;
```

**SRS_DEVICECLIENT_25_025: [**The function shall create a new instance of class Device Twin and request all twin properties by calling getDeviceTwin**]**

**SRS_DEVICECLIENT_25_026: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_027: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_028: [**If this method is called twice on the same instance of the client then this method shall throw UnsupportedOperationException.**]**


### subscribeToDesiredProperties

```java
public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;
```

**SRS_DEVICECLIENT_25_029: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_030: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_031: [**This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.**]**


### sendReportedProperties

```java
public void sendReportedProperties(Set<Property> reportedProperties) throws IOException;
```

**SRS_DEVICECLIENT_25_032: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_033: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_034: [**If reportedProperties is null or empty, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_035: [**This method shall send to reported properties by calling updateReportedProperties on the twin object.**]**


### subscribeToDeviceMethod

```java
public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IOException;
```

**SRS_DEVICECLIENT_25_036: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_037: [**If deviceMethodCallback or deviceMethodStatusCallback is null, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_038: [**This method shall subscribe to device methods by calling subscribeToDeviceMethod on DeviceMethod object which it created.**]**

**SRS_DEVICECLIENT_25_039: [**This method shall not create a new instance of deviceMethod if called twice.**]**

### uploadToBlobAsync

```java
public void uploadToBlobAsync(String destinationBlobName, InputStream inputStream, long streamLength,
                              IotHubEventCallback callback, Object callbackContext)
        throws IllegalArgumentException, IllegalStateException, IOException;
```

**SRS_DEVICECLIENT_21_044: [**The uploadToBlobAsync shall asynchronously upload the stream in `inputStream` to the blob in `destinationBlobName`.**]**  

**SRS_DEVICECLIENT_21_045: [**If the `callback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_21_046: [**If the `inputStream` is null, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_21_052: [**If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_21_047: [**If the `destinationBlobName` is null, empty, or not valid, the uploadToBlobAsync shall throw IllegalArgumentException.**]**  

**SRS_DEVICECLIENT_34_066: [**If this function is called when the device client is using x509 authentication, an UnsupportedOperationException shall be thrown.**]**  

**SRS_DEVICECLIENT_21_048: [**If there is no instance of the FileUpload, the uploadToBlobAsync shall create a new instance of the FileUpload.**]**  

**SRS_DEVICECLIENT_21_049: [**If uploadToBlobAsync failed to create a new instance of the FileUpload, it shall bypass the exception.**]**  

**SRS_DEVICECLIENT_21_050: [**The uploadToBlobAsync shall start the stream upload process, by calling uploadToBlobAsync on the FileUpload class.**]**  

**SRS_DEVICECLIENT_21_051: [**If uploadToBlobAsync failed to start the upload using the FileUpload, it shall bypass the exception.**]**  


### registerConnectionStateCallback
```java
public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext);
```

**SRS_DEVICECLIENT_99_001: [**The registerConnectionStateCallback shall register the callback with the Device IO.**]**
**SRS_DEVICECLIENT_99_002: [**The registerConnectionStateCallback shall register the callback even if the client is not open.**]**
**SRS_DEVICECLIENT_99_003: [**If the callback is null the method shall throw an IllegalArgument exception.**]**


### setPrivateKey
```java
public void setPrivateKey(String privateKey, boolean isPath);
```

**SRS_DEVICECLIENT_34_057: [**This method shall save the provided private key in config.**]**

    
### setPublicKeyCertificate
```java
public void setPublicKeyCertificate(String publicKeyCertificate, boolean isPath);
```

**SRS_DEVICECLIENT_34_056: [**This method shall save the provided public key certificate in config.**]**
