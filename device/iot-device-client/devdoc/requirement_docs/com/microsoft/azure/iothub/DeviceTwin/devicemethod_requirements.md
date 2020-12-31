# DeviceMethod Requirements

## Overview

DeviceMethod defines device method interaction between IotHub and user.

## References

## Exposed API

```java
public final class DeviceMethod
{
    public DeviceMethod(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IllegalArgumentException;

    public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext) throws IllegalArgumentException;    
}
```

### DeviceMethod

```java
public DeviceMethod(DeviceIO deviceIO, DeviceClientConfig config, IotHubEventCallback deviceMethodStatusCallback, Object deviceMethodStatusCallbackContext) throws IllegalArgumentException;
```

**SRS_DEVICEMETHOD_25_001: [**The constructor shall throw IllegalArgumentException if any of the parameters i.e deviceIO, config, deviceMethodStatusCallback are null. **]**

**SRS_DEVICEMETHOD_25_002: [**The constructor shall create a new instance of the deviceMethodResponseCallback, and set it as desired callback in config by calling setDeviceMethodMessageCallback, where any further messages for device method shall be delivered.**]**

**SRS_DEVICEMETHOD_25_003: [**The constructor shall save all the parameters specified i.e deviceIO, config, deviceMethodStatusCallback, deviceMethodStatusCallbackContext.**]**


### subscribeToDeviceMethod

```java
public void subscribeToDeviceMethod(DeviceMethodCallback deviceMethodCallback, Object deviceMethodCallbackContext) throws IllegalArgumentException;
```

**SRS_DEVICEMETHOD_25_004: [**If deviceMethodCallback parameter is null then this method shall throw IllegalArgumentException**]**

**SRS_DEVICEMETHOD_25_005: [**If not already subscribed then this method shall create a device method message with empty payload and set its type as DEVICE_OPERATION_METHOD_SUBSCRIBE_REQUEST.**]**

**SRS_DEVICEMETHOD_25_006: [**If not already subscribed then this method shall send the message using sendEventAsync.**]**

**SRS_DEVICEMETHOD_25_006: [**If already subscribed then this method shall save the callbacks and exit.**]**


### deviceMethodResponseCallback

```java
private final class deviceMethodResponseCallback implements MessageCallback
{
    @Override
    public IotHubMessageResult execute(Message message, Object callbackContext);
}
```

**SRS_DEVICEMETHOD_25_007: [**On receiving a message from IOTHub for method invoke, the callback DeviceMethodResponseMessageCallback shall be triggered.**]**

**SRS_DEVICEMETHOD_25_008: [**If the message is of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user registered device method callback shall be invoked providing the user with method name and payload along with the user context. **]**

**SRS_DEVICEMETHOD_25_009: [**If the received message is not of type DeviceMethod and DEVICE_OPERATION_METHOD_RECEIVE_REQUEST then user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Abandon **]**

**SRS_DEVICEMETHOD_25_010: [**User shall provide response message and status upon invoking the device method callback.**]**

**SRS_DEVICEMETHOD_25_015: [**User can provide null response message upon invoking the device method callback which will be serialized as is, before sending it to IotHub.**]**

**SRS_DEVICEMETHOD_25_011: [**If the user callback is successful and user has successfully provided the response message and status, then this method shall build a device method message of type DEVICE_OPERATION_METHOD_SEND_RESPONSE, serialize the user data by invoking MethodParser from serializer and save the user data as payload in the message before sending it to IotHub by calling sendEventAsync and thereby marking the result as complete**]**

**SRS_DEVICEMETHOD_25_012: [**The device method message sent to IotHub shall have the same request id as the invoking message.**]**

**SRS_DEVICEMETHOD_25_013: [**The device method message sent to IotHub shall have the status provided by the user as the message status.**]**

**SRS_DEVICEMETHOD_25_014: [**If the user invoked callback failed for any reason then the user shall be notified on the status callback registered by the user as ERROR before marking the status of the sent message as Rejected.**]**

