# DeviceTwin Requirements

## Overview

Allows a single logical or physical device to connect to an IoT Hub.

## References

## Exposed API

```java
public final class DeviceTwin
{
    public DeviceTwin(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext, PropertyCallBack genericPropertyCallback, Object genericPropertyCallbackContext) throws IOException;

    public void getDeviceTwin();
    public void updateReportedProperties(HashSet<Property> reportedProperties) throws IOException;   
    public void subscribeDesiredPropertiesNotification(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;
}
```


### DeviceTwin

```java
public DeviceTwin(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceTwinCallback, Object deviceTwinCallbackContext, PropertyCallBack genericPropertyCallback, Object genericPropertyCallbackContext) throws IOException;
```

**SRS_DEVICETWIN_25_001: [**The constructor shall throw IllegalArgument Exception if any of the parameters i.e deviceIO, config, deviceTwinCallback, genericPropertyCallback are null. **]**

**SRS_DEVICETWIN_25_002: [**The constructor shall save the device twin message callback, by calling setDeviceTwinMessageCallback, where any further messages for device twin shall be delivered.**]**

**SRS_DEVICETWIN_25_003: [**The constructor shall save all the parameters specified i.e deviceIO, config, deviceTwinCallback, genericPropertyCallback.**]**

**SRS_DEVICETWIN_25_004: [**The constructor shall create a new twin object which will hence forth be used as a storage for all the properties provided by user.**]**


### getDeviceTwin

```java
public void getDeviceTwin();
```

**SRS_DEVICETWIN_25_005: [**The method shall create a device twin message with empty payload to be sent to the IotHub.**]**  

**SRS_DEVICETWIN_25_006: [**This method shall set the message type as DEVICE_TWIN_OPERATION_GET_REQUEST by calling setDeviceTwinOperationType.**]**

**SRS_DEVICETWIN_25_007: [**This method shall set the request id for the message by calling setRequestId .**]**

**SRS_DEVICETWIN_25_008: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**


### updateReportedProperties

```java
public void updateReportedProperties(HashSet<Property> reportedProperties) throws IOException;
```

**SRS_DEVICETWIN_25_009: [**The method shall throw IllegalArgument Exception if reportedProperties is null.**]**

**SRS_DEVICETWIN_25_010: [**The method shall throw IOException if twin object has not yet been created.**]**

**SRS_DEVICETWIN_25_011: [**The method shall send the property set to Twin Serializer by calling updateReportedProperty.**]**

**SRS_DEVICETWIN_25_012: [**The method shall create a device twin message with the serialized payload only if payload is not null.**]**

**SRS_DEVICETWIN_25_013: [**This method shall set the message type as DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_REQUEST by calling setDeviceTwinOperationType.**]**

**SRS_DEVICETWIN_25_014: [**This method shall set the request id for the message by calling setRequestId .**]**

**SRS_DEVICETWIN_25_015: [**This method shall send the message to the lower transport layers by calling sendEventAsync.**]**


### subscribeDesiredPropertiesNotification

```java
public void subscribeDesiredPropertiesNotification(Map<Property, Pair<PropertyCallBack<String, Object>, Object>> onDesiredPropertyChange) throws IOException;
```

**SRS_DEVICETWIN_25_017: [**The method shall create a treemap to store callbacks for desired property notifications specified in onDesiredPropertyChange.**]**

**SRS_DEVICETWIN_25_018: [**If not already subscribed then this method shall create a device twin message with empty payload and set its type as DEVICE_TWIN_OPERATION_SUBSCRIBE_DESIRED_PROPERTIES_REQUEST.**]**

**SRS_DEVICETWIN_25_019: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**


### OnDesiredPropertyChange

```java
private final class OnDesiredPropertyChange implements TwinPropertiesChangeCallback
{
    @Override
    public void execute(HashMap<String, String> desiredPropertyMap);
}
```

**SRS_DEVICETWIN_25_020: [**OnDesiredPropertyChange callback is registered with the serializer to be triggered when desired propery changes.**]**

**SRS_DEVICETWIN_25_021: [**On deserialization of desired properties, OnDesiredPropertyChange callback is triggered by the serializer**]**

**SRS_DEVICETWIN_25_022: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed provided in desiredPropertyMap and call the user providing the desired property change key and value pair**]**

**SRS_DEVICETWIN_25_023: [**OnDesiredPropertyChange callback shall look for the user registered call back on the property that changed and if no callback is registered or is null then OnDesiredPropertyChange shall call the user on generic callback providing with the desired property change key and value pair**]**

### deviceTwinResponseMessageCallback

```java
private final class deviceTwinResponseMessageCallback implements MessageCallback
{
    @Override
    public IotHubMessageResult execute(Message message, Object callbackContext);
}
```

**SRS_DEVICETWIN_25_025: [**On receiving a message from IOTHub with desired property changes, the callback deviceTwinResponseMessageCallback is triggered.**]**

**SRS_DEVICETWIN_25_026: [**If the message is of type DeviceTwin and DEVICE_TWIN_OPERATION_SUBSCRIBE_DESIRED_PROPERTIES_RESPONSE then the payload shall be deserialized by calling updateDesiredProperty.**]**

**SRS_DEVICETWIN_25_027: [**If the message is of type DeviceTwin and DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_RESPONSE then the user call with a valid status shall be triggered.**]**

**SRS_DEVICETWIN_25_028: [**If the message is of type DeviceTwin and DEVICE_TWIN_OPERATION_UPDATE_REPORTED_PROPERTIES_RESPONSE and if the status is null then the user shall be notified on the status callback registered by the user as ERROR.**]**

**SRS_DEVICETWIN_25_029: [**If the message is of type DeviceTwin and DEVICE_TWIN_OPERATION_GET_RESPONSE then the user call with a valid status shall be triggered.**]**

**SRS_DEVICETWIN_25_030: [**If the message is of type DeviceTwin and DEVICE_TWIN_OPERATION_GET_RESPONSE then the payload shall be deserialized by calling updateTwin only if the status is ok.**]**

**SRS_DEVICETWIN_25_031: [**If the message is of type DeviceTwin and DEVICE_TWIN_OPERATION_GET_RESPONSE and if the status is null then the user shall be notified on the status callback registered by the user as ERROR.**]**

