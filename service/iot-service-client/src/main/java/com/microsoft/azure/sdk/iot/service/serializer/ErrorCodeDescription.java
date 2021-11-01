package com.microsoft.azure.sdk.iot.service.serializer;

/**
 * The descriptions for all the possible 6 digit and 3 digit error codes returned by IoT hub for service calls.
 * See https://docs.microsoft.com/en-us/rest/api/iothub/common-error-codes for additional details
 */
public enum ErrorCodeDescription
{
    // These descriptions belong to fully qualified status codes, such as 404001
    /**
     * This library could not classify the received error code into a known description.
     */
    UnclassifiedErrorCode,

    /**
     * The API version used by the SDK is not supported by the IoT hub endpoint used in this connection.
     * Usually this would mean that the region of the hub doesn't yet support the API version. One should
     * consider downgrading to a previous version of the SDK that uses an older API version, or use a hub
     * in a region that supports it.
     */
    InvalidProtocolVersion,

    /**
     * The client has requested an operation that the hub recognizes as invalid. Check the error message
     * for more information about what is invalid.
     */
    InvalidOperation,

    /**
     * Something in the request payload is invalid. Check the error message for more information about what is invalid.
     */
    ArgumentInvalid,

    /**
     * Something in the payload is unexpectedly null. Check the error message for more information about what is invalid.
     */
    ArgumentNull,

    /**
     * Returned by the service if a JSON object provided by this library cannot be parsed, for instance, if the
     * JSON provided for updating a twin is malformed.
     */
    IotHubFormatError,

    /**
     * A devices with the same Id was present multiple times in the input request for bulk device registry operations.
     * @see <a href="https://docs.microsoft.com/rest/api/iothub/service/bulk-registry/update-registry">Bulk registry operations</a>
     */
    DeviceDefinedMultipleTimes,

    /**
     * An error was encountered processing bulk registry operations.
     */
    BulkRegistryOperationFailure,

    /**
     * The SAS token has expired or IoT hub couldn't authenticate the authentication header, rule, or key.
     * @see <a href="https://docs.microsoft.com/azure/iot-hub/iot-hub-troubleshoot-error-401003-iothubunauthorized">More details</a>
     */
    IotHubUnauthorizedAccess,

    /**
     * Total number of messages on the hub exceeded the allocated quota. To resolve this, increase units for this hub
     * to increase the quota.
     * @see <a href="https://aka.ms/iothubthrottling">More details on throttling limits.</a>
     */
    IotHubQuotaExceeded,

    /**
     * The underlying cause is that the number of cloud-to-device messages enqueued for the device exceeds the queue limit.
     * To resolve this issue, you will need to make sure your device is actively completing or abandoning the currently queued
     * messages, or you will need to purge the cloud to device message queue.
     * @see <a href="https://docs.microsoft.com/azure/iot-hub/iot-hub-devguide-messages-c2d">More details on cloud to device messaging</a>
     */
    DeviceMaximumQueueDepthExceeded,

    /**
     * The operation failed because the device cannot be found by IoT hub. The device is either not registered or
     * disabled. May be thrown by operations such as getting a device by id.
     */
    DeviceNotFound,

    /**
     * The operation failed because the module cannot be found by IoT hub. The module is either not registered or
     * disabled. May be thrown by operations such as getting a module by id.
     */
    ModuleNotFound,

    /**
     * There's already a device with the same device Id in the IoT hub. This can be returned on by the service when adding
     * a device to your IoT hub with an Id that is already in use.
     */
    DeviceAlreadyExists,

    /**
     * The operation failed because it attempted to add a module to a device when that device already has a module
     * registered to it with the same Id. This issue can be fixed by removing the existing module from the device first.
     */
    ModuleAlreadyExistsOnDevice,

    /**
     * When the message that you attempted to send is too large for IoT hub you will receive this error.
     * @see <a href="https://aka.ms/iothubthrottling#other-limits">More details on message size limits</a>
     */
    MessageTooLarge,

    /**
     * Too many devices were included in the bulk operation.
     * @see <a href="https://docs.microsoft.com/rest/api/iothub/service/bulk-registry/update-registry">More details on device count limits</a>
     */
    TooManyDevices,

    /**
     * IoT hub throttling limits have been exceeded for the requested operation.
     * @see <a href="https://aka.ms/iothubthrottling">More details on throttling limits.</a>
     */
    ThrottleBacklogLimitExceeded,

    /**
     * IoT hub encountered a service-side issue. There can be a number of causes for a 500xxx error response.
     * In all cases, the issue is most likely transient. IoT hub nodes can occasionally experience transient faults.
     * When your application tries to connect to a node that is having issues, you receive this error.
     * To mitigate 500xxx errors, issue a retry from your application.
     */
    ServerError,

    /**
     * IoT hub failed to invoke the direct method because the target device was not connected at the time. To resolve
     * this issue, you will need to make sure that your device is connected.
     */
    DeviceNotOnline,

    // These descriptions belong to 3 digit http response codes such as 404.
    /**
     * The general error for malformed service requests.
     */
    BadFormat,

    /**
     * The general error for unauthorized service requests.
     */
    Unauthorized,

    /**
     * The general error for forbidden service requests.
     */
    Forbidden,

    /**
     * The general error for service requests that failed because a resource could not be found.
     */
    NotFound,

    /**
     * The general error for service requests that failed because the resource already exists.
     */
    Conflict,

    /**
     * The general error for when a service request fails because it provided an out of date ETag such as when updating
     * a twin.
     */
    PreconditionFailed,

    /**
     * The general error for service requests that failed because a service request contained a resource that was larger
     * than the service allows.
     */
    RequestEntityTooLarge,

    /**
     * IoT hub throttling limits have been exceeded for the requested operation.
     * @see <a href="https://aka.ms/iothubthrottling">More details on throttling limits.</a>
     */
    ThrottlingException,

    /**
     * The general error for service requests that failed because the service encountered an error.
     */
    InternalServerError,

    /**
     * The general error for service requests that failed because the service isn't available at the moment.
     */
    ServiceUnavailable;

    /**
     * Get the ErrorCodeDescription tied to the provided errorCode
     * @param errorCode the service error code, such as 404, or 429001
     * @return the corresponding ErrorCodeDescription
     */
    public static ErrorCodeDescription Parse(int errorCode)
    {
        switch (errorCode)
        {
            case(400001):
                return InvalidProtocolVersion;
            case(400003):
                return InvalidOperation;
            case(400004):
                return ArgumentInvalid;
            case(400005):
                return ArgumentNull;
            case(400006):
                return IotHubFormatError;
            case(400011):
                return DeviceDefinedMultipleTimes;
            case(400013):
                return BulkRegistryOperationFailure;
            case(401002):
                return IotHubUnauthorizedAccess;
            case(403002):
                return IotHubQuotaExceeded;
            case(403004):
                return DeviceMaximumQueueDepthExceeded;
            case(404001):
                return DeviceNotFound;
            case(404010):
                return ModuleNotFound;
            case (404103):
                return DeviceNotOnline;
            case(409001):
                return DeviceAlreadyExists;
            case(409301):
                return ModuleAlreadyExistsOnDevice;
            case(412001):
            case (412):
                return PreconditionFailed;
            case(413001):
                return MessageTooLarge;
            case(413002):
                return TooManyDevices;
            case(429001):
            case (429):
                return ThrottlingException;
            case(429002):
                return ThrottleBacklogLimitExceeded;
            case(500001):
                return ServerError;
            case(503001):
                // intended fall through
            case (503):
                return ServiceUnavailable;
            case (400):
                return BadFormat;
            case (401):
                return Unauthorized;
            case (403):
                return Forbidden;
            case (404):
                return NotFound;
            case (409):
                return Conflict;
            case (413):
                return RequestEntityTooLarge;
            case (500):
                return InternalServerError;
            default:
                return UnclassifiedErrorCode;
        }
    }
}
