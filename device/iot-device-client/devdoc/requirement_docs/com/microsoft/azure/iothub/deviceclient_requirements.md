# DeviceClient Requirements

## Overview

Allows a single logical or physical device to connect to an IoT Hub.

## References

## Exposed API

```java
public final class DeviceClient
{
    public static long SEND_PERIOD_MILLIS = 10l;
    public static long RECEIVE_PERIOD_MILLIS = 10l;
    public static long RECEIVE_PERIOD_MILLIS_MQTT = 10l;
    public static long RECEIVE_PERIOD_MILLIS_HTTPS = 25*60*1000; /*25 minutes*/

    public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException;
    public DeviceClient(String iotHubHostname, String deviceId, String deviceKey, IotHubClientProtocol protocol) throws URISyntaxException;

    public void open() throws IOException;
    public void close() throws IOException;

    public void sendEventAsync(Message msg, IotHubEventCallback callback, Object callbackContext);    
    public DeviceClient setMessageCallback(IotHubMessageCallback callback, Object context);
    
    public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object    deviceTwinStatusCallbackContext, PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext) throws IOException;
    public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;
    public void sendReportedProperties(Set<Property> reportedProperties) throws IOException;  

    public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IOException  
}
```


### DeviceClient

```java
public DeviceClient(String connString, IotHubClientProtocol protocol) throws URISyntaxException;
```

**SRS_DEVICECLIENT_11_042: [**The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', with keys and values separated by '='.**]**

**SRS_DEVICECLIENT_11_043: [**The constructor shall save the IoT Hub hostname as the value of 'HostName' in the connection string.**]**

**SRS_DEVICECLIENT_11_044: [**The constructor shall save the device ID as the UTF-8 URL-decoded value of 'DeviceId' in the connection string.**]**

**SRS_DEVICECLIENT_11_045: [**The constructor shall save the device key as the value of 'SharedAccessKey' in the connection string.**]**

**SRS_DEVICECLIENT_25_052: [**The constructor shall save the shared access token as the value of 'sharedAccessToken' in the connection string.**]**

**SRS_DEVICECLIENT_11_046: [**The constructor shall initialize the IoT Hub transport that uses the protocol specified.**]**

**SRS_DEVICECLIENT_11_047: [**If the connection string is null, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_11_048: [**If no value for 'HostName' is found in the connection string, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_11_049: [**If no value for 'DeviceId' is found in the connection string, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_11_050: [**If no argument for 'SharedAccessKey' is found in the connection string, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_053: [**If no argument for 'sharedAccessToken' and 'SharedAccessKey' is found in the connection string, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_054: [**The constructor shall only accept either 'sharedAccessToken' or 'SharedAccessKey' from the connection string and throw an IllegalArgumentException if both are found**]**

**SRS_DEVICECLIENT_11_051: [If protocol is null, the function shall throw an IllegalArgumentException.**]**


### open

```java
public void open();
```

**SRS_DEVICECLIENT_11_035: [**The function shall open the transport to communicate with an IoT Hub.**]**

**SRS_DEVICECLIENT_11_023: [**The function shall schedule send tasks to run every SEND_PERIOD_MILLIS milliseconds.**]**

**SRS_DEVICECLIENT_11_024: [**The function shall schedule receive tasks to run every RECEIVE_PERIOD_MILLIS milliseconds.**]**

**SRS_DEVICECLIENT_11_028: [**If the client is already open, the function shall do nothing.**]**

**SRS_DEVICECLIENT_11_036: [**If an error occurs in opening the transport, the function shall throw an IOException.**]**


### close

```java
public void close();
```

**SRS_DEVICECLIENT_11_010: [**The function shall finish all ongoing tasks.**]**

**SRS_DEVICECLIENT_11_011: [**The function shall cancel all recurring tasks.**]**

**SRS_DEVICECLIENT_11_037: [**The function shall close the transport.**]**

**SRS_DEVICECLIENT_11_031: [**If the client is already closed, the function shall do nothing.**]**


### sendEventAsync

```java
public void sendEventAsync(Message msg, IotHubEventCallback callback, Object callbackContext);**
```

**SRS_DEVICECLIENT_11_006: [**The function shall add the message, with its associated callback and callback context, to the transport.**]**

**SRS_DEVICECLIENT_11_033: [**If the message given is null, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_11_039: [**If the client is closed, the function shall throw an IllegalStateException.**]**


### setMessageCallback

```java
public DeviceClient setMessageCallback(IotHubMessageCallback callback, Object context);
```

**SRS_DEVICECLIENT_11_012: [**The function shall set the message callback, with its associated context.**]**

**SRS_DEVICECLIENT_11_032: [**If the callback is null but the context is non-null, the function shall throw an IllegalArgumentException.**]**


### startDeviceTwin

```java
public void startDeviceTwin(IotHubEventCallback deviceTwinStatusCallback, Object    deviceTwinStatusCallbackContext, PropertyCallBack genericPropertyCallBack, Object genericPropertyCallBackContext) throws IOException;
```

**SRS_DEVICECLIENT_25_011: [**The function shall create a new instance of class Device Twin and request all twin properties by calling getDeviceTwin**]**

**SRS_DEVICECLIENT_25_012: [**If the deviceTwinStatusCallback or genericPropertyCallBack is null, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_013: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_014: [**If this method is called twice on the same instance of the client then this method shall throw UnsupportedOperationException.**]**


### subscribeToDesiredProperties

```java
public void subscribeToDesiredProperties(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;
```

**SRS_DEVICECLIENT_25_015: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_016: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_017: [**This method shall subscribe to desired properties by calling subscribeDesiredPropertiesNotification on the twin object.**]**


### sendReportedProperties

```java
public void sendReportedProperties(Set<Property> reportedProperties) throws IOException;
```

**SRS_DEVICECLIENT_25_018: [**If the client has not started twin before calling this method, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_019: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_020: [**If reportedProperties is null or empty, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_021: [**This method shall send to reported properties by calling updateReportedProperties on the twin object.**]**



### subscribeToDeviceMethod

```java
public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IOException;
```

**SRS_DEVICECLIENT_25_022: [**If the client has not been open, the function shall throw an IOException.**]**

**SRS_DEVICECLIENT_25_023: [**If deviceMethodCallback or deviceMethodStatusCallback is null, the function shall throw an IllegalArgumentException.**]**

**SRS_DEVICECLIENT_25_024: [**This method shall subscribe to device methods by calling subscribeToDeviceMethod on DeviceMethod object which it created.**]**

**SRS_DEVICECLIENT_25_025: [**This method shall update the deviceMethodCallback if called again, but it shall not subscribe twice.**]**


### setOption

```java
public setOption(String optionName, Object value)
```

This method sets the option given by optionName to value.

**SRS_DEVICECLIENT_02_001: [**If optionName is null or not an option handled by the client, then it shall throw IllegalArgumentException.**]**

Options handled by the client:

**SRS_DEVICECLIENT_02_002: [**"SetMinimumPollingInterval" - time in miliseconds between 2 consecutive polls.**]**

**SRS_DEVICECLIENT_02_003: [**Option "SetMinimumPollingInterval" is available only for HTTP.**]**

**SRS_DEVICECLIENT_02_004: [**"SetMinimumPollingInterval" needs to have value type long**.]**

**SRS_DEVICECLIENT_25_005: [**"SetCertificatePath" - path to the certificate to verify peer .**]**

**SRS_DEVICECLIENT_25_006: [**"SetCertificatePath" is available only for AMQP.**]**

**SRS_DEVICECLIENT_25_007: [**"SetSASTokenExpiryTime" - Time in secs to specify SAS Token Expiry time .**]**

**SRS_DEVICECLIENT_25_009: [**"SetSASTokenExpiryTime" should have value type long**.]**

**SRS_DEVICECLIENT_25_008: [**"SetSASTokenExpiryTime" is available for HTTPS/AMQP/MQTT.**]**

**SRS_DEVICECLIENT_25_010: [**"SetSASTokenExpiryTime" shall restart the transport
                                    1. If the device currently uses device key and
                                    2. If transport is already open
                               after updating expiry time**.]**

