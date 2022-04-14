# Device Reconnection Sample

This sample shows the best practices for handling a device (or module) client using a stateful protocol (AMQPS or MQTT) 
over the course of its lifetime. This sample is intended to be a starting point for a user developing a production application 
wherein their device client must handle unexpected connection loss events and transient errors such as throttling, internal server errors, and timeouts. 

Included in this sample is the recommended patterns for sending reported properties and receiving desired properties, for
sending telemetry, for receiving cloud to device messages, and for receiving direct methods.

## Sample Structure

The sample uses two threads to accomplish these best practices. The threads, and their purpose is as follows:
- "Iot-Hub-Connection-Manager-Thread"
  - This thread is responsible for ensuring that the device client is either connected to the service or is attempting to reconnect.
  - When the device client is connected or when the device client's internal retry logic is running, this thread is dormant.
  - Once a terminal disconnection event happens, this thread is woken up to attempt to reconnect indefinitely. 
- "Iot-Hub-Worker-Thread"
  - This thread queues outgoing work such as sending device to cloud telemetry and updating reported properties.
  - This thread is active when the client is connected or the device client's internal retry logic is running.
  - This thread ends when the client reaches a terminal disconnection state and the Iot-Hub-Connection-Manager-Thread is working to restore the connection.
  - This thread starts again once the connection has been restored.

## Modifying this sample to fit your application

This sample is written such that users can copy this code and modify it to fit their needs by removing the parts they 
don't need all while still getting the best practices for managing the client's connection.

For instance, if your client will never receive direct methods, you can safely delete this code snippet from the sample:

```java
// region direct methods setup
// This region can be removed if no direct methods will be invoked on this client
this.deviceClient.subscribeToMethods(this, null);
// endregion
```

and you can delete this code snippet as well:

```java
// region direct methods
// callback for when a direct method is invoked on this device
@Override
public DirectMethodResponse onMethodInvoked(String methodName, DirectMethodPayload payload, Object context)
{
    // Typically there would be some method handling that differs based on the name of the method and/or the payload
    // provided, but this sample's method handling is simplified for brevity. There are other samples in this repo
    // that demonstrate handling methods in more depth.
    log.debug("Method {} invoked on device.", methodName);
    return new DirectMethodResponse(200, null);
}
// endregion
```

Similarly, if your device client will only ever be receiving cloud to device messages and direct methods, you can remove the
entire "Iot-Hub-Worker-Thread" code and you will still retain the best practices for receving cloud to device messages, direct methods,
and for handling disconnection events.

## Boundary cases

There are a few places in this sample where ```System.exit(-1);``` is executed. These cases indicate that something 
fundamentally wrong has happened and that the client's connection cannot continue. For instance, if you provide incorrect
or badly formatted credentials, this sample will exit like this. These cases should not happen in production applications
since the credentials should be correct, so these cases can be removed when writing your application.