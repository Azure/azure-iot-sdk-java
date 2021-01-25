Below is the behavior of the SDK on receiving a `TransportException`. If the exception is marked as retry-able, the SDK will implement the default retry-policy and attempt to reconnect. For exceptions not marked as retryable, it is advised to inspect the exception details and perform the necessary action as indicated below.

`DeviceClient` provides an interface `IotHubConnectionStatusChangeCallback` which you can implement for monitoring the connection status change, and inspecting the exception details in case of an exception.

Example:
```java
protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
{
    @Override
    public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
    {
        System.out.println();
        System.out.println("CONNECTION STATUS UPDATE: " + status);
        System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
        System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
        System.out.println();

        if (throwable != null)
        {
            throwable.printStackTrace();
        }

        if (status == IotHubConnectionStatus.DISCONNECTED)
        {
            //connection was lost, and is not being re-established. Look at provided exception for how to resolve this issue. 
        }
        else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
        {
            //connection was lost, but is being re-established. 
        }
        else if (status == IotHubConnectionStatus.CONNECTED)
        {
            //Connection was successfully re-established. 
        }
    }
}

DeviceClient client = new DeviceClient(<YOUR_DEVICE_CONNECTION_STRING_HERE>, IotHubClientProtocol.AMQPS);
client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), client);
```


|Exception Name |Error code (if available) |isRetryable  |Action                 |
|------|------|------|------|
| TransportException | | No | Unrecognized transport exceptions are not retried. Inspect the throwable for additional details|
| ProtocolException | | No | Unrecognized protocol exceptions are not retried. Inspect the throwable for additional details|
| IotHubServiceException | | No | Thrown when the Service returns an Unknown Status Code; contact CSS with logs and Exception details |
| BadFormatException | | No | Thrown when service receives a Bad Request; inspect the request being sent |
| HubOrDeviceIdNotFoundException | | No | Inspect the exception details and verify that device/ IoT Hub being used exists |
| InternalServerErrorException | | No | Inspect the logs, and contact service with exception details |
| PreconditionFailedException | | No | Thrown when some IoT Hub precondition is not met; collect logs and contact service |
| RequestEntityTooLargeException | | No | Inspect the logs, and contact service with exception details |
| ServerBusyException | | Yes | SDK will retry |
| ServiceUnknownException | | No | Inspect the logs, and contact service with exception details |
| ThrottledException | | Yes | SDK will retry, with backoff |
| TooManyDevicesException | | No | Too many devices on your hub instance, clean up unused devices or scale up the hub instance |
| UnauthorizedException | | No | Verify your credentials and make sure they are up-to-date. |
| | | | 
| AMQP | | | 
| | | | 
| AmqpConnectionForcedException | amqp:connection:forced error | Yes | SDK will retry |
| AmqpConnectionFramingErrorException | amqp:connection:framing-error | Yes | SDK will retry |
| AmqpConnectionRedirectException | amqp:connection:redirect | Yes | SDK will retry |
| AmqpConnectionThrottledException | com.microsoft:device-container-throttled | Yes | SDK will retry, with backoff |
| AmqpDecodeErrorException | amqp:decode-error | No | Mis-match between AMQP message sent by client and received by service; collect logs and contact service |
| AmqpFrameSizeTooSmallException | amqp:frame-size-too-small | No | The AMQP message is not being formed correctly by the SDK, collect logs and contact SDK team |
| AmqpIllegalStateException | amqp:illegal-state | No | Inspect the exception details, collect logs and contact service |
| AmqpInternalErrorException | amqp:internal-error | Yes | SDK will retry |
| AmqpInvalidFieldException | amqp:invalid-field | No | Inspect the exception details, collect logs and contact service |
| AmqpLinkDetachForcedException | amqp:link :detach-forced | Yes | SDK will retry |
| AmqpLinkMessageSizeExceededException | amqp:link :message-size-exceeded | No | The AMQP message size exceeded the value supported by the link, collect logs and contact service |
| AmqpLinkRedirectException	| amqp:link :redirect | Yes | SDK will retry | 
| AmqpLinkStolenException | amqp:link stolen | Yes | SDK will retry |
| AmqpLinkTransferLimitExceededException | amqp:link :transfer-limit-exceeded | Yes | SDK will retry |
| AmqpNotAllowedException	| amqp:not-allowed | No | Inspect the exception details, collect logs and contact service |
| AmqpNotFoundException | amqp:not-found | No | Inspect the exception details, collect logs and contact service |
| AmqpNotImplementedException | amqp:not-implemented | No | Inspect the exception details, collect logs and contact service |
| AmqpPreconditionFailedException | amqp:precondition-failed | No | Inspect the exception details, collect logs and contact service |
| AmqpResourceDeletedException | amqp:resource-deleted | No | Inspect the exception details, collect logs and contact service |
| AmqpResourceLimitExceededException | amqp:resource-limit-exceeded | No | Inspect the exception details, collect logs and contact service |
| AmqpResourceLockedException | amqp:resource-locked | Yes | SDK will retry |
| AmqpSessionErrantLinkException | amqp:session:errant-link | Yes | SDK will retry |
| AmqpSessionHandleInUseException | amqp:session:handle-in-use | Yes | SDK will retry |
| AmqpSessionUnattachedHandleException | amqp:session:unattached-handle | Yes | SDK will retry |
| AmqpSessionWindowViolationException | amqp:session:window-violation | Yes | SDK will retry |
| AmqpUnauthorizedAccessException | amqp:unauthorized-access | No | SDK will throw `UnauthorizedException` with Connection status reason `BAD_CREDENTIAL` |
| | | | 
| MQTT | | | 
| | | | 
| MqttBadUsernameOrPasswordException | Paho: REASON_CODE_FAILED_AUTHENTICATION | No | Verify your credentials and make sure they are up-to-date. |
| MqttIdentifierRejectedException | Paho: REASON_CODE_INVALID_CLIENT_ID | No | Verify that the device ID/ module ID being used for connection exists on your hub instance |
| MqttRejectedProtocolVersionException | Paho: REASON_CODE_INVALID_PROTOCOL_VERSION | No | The MQTT version being specified by the SDK is not supported by the service; collect logs and contact SDK team |
| MqttServerUnavailableException | | Yes | SDK will retry | 
| MqttUnauthorizedException | Paho: REASON_CODE_NOT_AUTHORIZED | No | Verify your credentials and make sure they are up-to-date. |
| MqttUnexpectedErrorException | | No | Inspect the exception details, collect logs and contact service |