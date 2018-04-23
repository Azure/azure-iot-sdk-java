Connectivity for IoT devices is generally a hard problem to solve, because there are many layers where an error can happen. Physical network, protocols, hardware, device constraints, a lot of things can go wrong.

The following document describes the approach that the Azure IoT Device SDK for Java takes to deal with the failures it detects, and how you can alter the default behavior to better suit your needs.

# Types of errors and how to detect them

From an SDK perspective there are only a few types of failures we can detect, mostly related to network and protocols:
- Network errors such as a disconnected socket, name resolution errors, etc
- Protocol-level errors for our HTTP, AMQP and MQTT transports (links detached, session expired...)
- Application-level errors that result from either local mistakes (invalid credentials) or service behavior (Quota exceeded, throttling...)

One class of error we do not deal with is hardware and OS-related error such as memory exhaustion or faulty drivers.

## Protocol errors

IoT Protocols usually make use of OSI layer 5-7 objects to manage session, presentation etc. These can fail even when the network connectivity itself is working perfectly. Links can be detached, sessions can be unmapped, etc. These errors are caught by our transport layer, translated into "protocol-agnostic errors" and then fed to our retry logic that can decide to retry the operation that failed, based on the type of error that is emitted.

To have the most fine grain control in your retry policy, you will need to check the type of the last exception given. To see if an exception occurred when sending a message (perhaps it was throttled), check if it was an IotHubServiceException. To check if it was due to a disconnection or some other protocol level problem, check if it is a ProtocolException.

You can also look deeper for protocol specific exceptions. For example, if you wanted to know what MQTT connect code you received during reconnection/disconnection, check if the exception is a MqttBadUsernameOrPasswordException (connect code 0x04).

## Application-level errors

"Application-level" in this case relates to an error that can happen server-side because either the service is misconfigured (for example, not enough units compared to the number of messages sent by devices) and that can be sent back to devices in hope to alter their behavior (in case it's being throttled for example).

Depending on the type of error the SDK may try a less aggressive retry policy, or not retry at all and let the user decide what to do.

# How does the SDK deals with these errors?

Depending on the error type and the retry policy that has been configured, the SDK may or may not retry operations that could not be completed because of an error. The following sections describe the constructs used in the SDK to make this decision, the default behavior, and how to alter it.

## What is a retry policy?

In our SDK, a retry policy is a combination of 2 things:
- an error filter
- an algorithm to calculate when to retry

### Error filters

**The error filter defines which exceptions can be retried and which cannot be retried**.

Currently, the default error filter cannot be altered. 

### Retry algorithms

When an error occurs and the retry policy kicks in, it calculates a delay to wait before retrying. The idea is that if an error happens very quickly, you don't want to retry immediately and keep hammering the network, or your IoT Hub, and make the problem worse (especially if the error is a `ThrottlingError` for example!).

The delay between retries is always strictly a function of how many retries have already been executed

The math formula used to calculate the delay varies depending on the policy that is chosen, and can generally result in a few different things:
- the time between retries can be constant or increasing
- a measure of randomness (also called jitter) can be added to avoid the [thundering herd problem](https://en.wikipedia.org/wiki/Thundering_herd_problem)

The reasons for not retrying anymore could be:
- A different error that should not be retried has been received
- The total time to retry has been is would be exceeded.

## What is the default retry policy?

In the SDK, the default retry policy is called "Exponential Backoff with Jitter". It's a fairly common standard math formula that is aggressive at the start, then slows down exponentially.

The formula is the following, `x` being the current retry count:

```
F(x) = min(Cmin+ (2^(x-1)-1) * rand(C*(1 – Jd), C*(1-Ju)), Cmax)
```

There are a few constants in this formula, here's their role and default values:
- `C`: Initial retry interval, 100ms
- `Cmin`: Lower bound for the delay between retries, 100ms
- `Cmax`: Upper bound for the delay between retries, 10,000ms
- `Jd`: Lower bound for the jitter factor, 0.5
- `Ju`: Upper bound for the iitter factor, 0.25


# How to change the retry logic

The device client has a specific method to change the retry policy, called `public void setRetryPolicy(RetryPolicy retryPolicy)` that accepts a `RetryPolicy` object, which will be consulted to compute whether or not to retry, and if so, after what time.

## Built-in retry policy objects

The SDK comes with 2 built-in `RetryPolicy` classes:
- The [ExponentialBackoffWithJitter](https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-client/src/main/java/com/microsoft/azure/sdk/iot/device/transport/ExponentialBackoffWithJitter.java) class that implements the default retry policy discussed in the previous paragraph.
- The [NoRetry](https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-client/src/main/java/com/microsoft/azure/sdk/iot/device/transport/NoRetry.java) class that simply disables the retry logic and doesn't take any parameters.

## Creating a custom retry policy

The [RetryPolicy](https://github.com/Azure/azure-iot-sdk-java/blob/master/device/iot-device-client/src/main/java/com/microsoft/azure/sdk/iot/device/transport/RetryPolicy.java) interface is public and it is possible for the SDK user to implement it and inject it in the SDK:

```java
public interface RetryPolicy
{
    /**
     * Determines whether the operation should be retried and the interval until the next retry.
     *
     * @param currentRetryCount the number of retries for the given operation
     * @param lastException the latest exception explaining why the retry is happening. This exception is guaranteed to
     *                      be retryable. In the event of a terminal exception occurring, this API will not be called.
     *                      Looking at this exception allows you to prevent retry on certain retryable exceptions, but
     *                      does not allow you to retry exceptions that are terminal.
     * @return the retry decision.
     */
    RetryDecision getRetryDecision(int currentRetryCount, TransportException lastException);
}
```

The `getRetryDecision` method is called when retrying and is passed the error that caused the retry in the first place. It should return a decision with `true` if the retry policy should kick in or `false` to disable the retry and fail the operation. The RetryDecision object also declares how long to wait before retrying.

As outlined earlier, the ```lastException``` will help you understand the type of operation that is being retried. If the exception is of type ```ProtocolException```, the retry policy is being consulted about reconnecting after losing connection. If the exception is of type ```IotHubServiceException```, the retry policy is being consulted about re-sending a message.

## Pitfalls and potential issues when retrying

There are a few of things that can go wrong when implementing retries:

- Not retrying conservatively: hammering the server with retries when throttling is in progress is going to make things worse. [Here is a good article](https://azure.microsoft.com/en-us/blog/iot-hub-throttling-and-you/) to learn more about throttling and what can trigger it. For example you can get throttled for sending too many messages at once, but also for trying to connect to many devices at once. That's why the jitter factor in the retry policy is important.

- The SDK can receive an unknown error from the underlying socket, protocol library or the service. In that case, the error filter will most likely not know whether to retry or not and will default to not retrying (again, conservative approach is best).

# Conclusion

hopefully you've learned a bit more about how the SDK works and how retries are implemented. If you want to learn even more, you can look directly at the source code in [the SDK repository](https://github.com/Azure/azure-iot-sdk-java) and if you have questions, ask them [in the issues section of the repository](https://github.com/Azure/azure-iot-sdk-java/issues).
