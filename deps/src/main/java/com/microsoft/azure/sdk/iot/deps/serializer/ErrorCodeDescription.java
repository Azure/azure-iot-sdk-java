package com.microsoft.azure.sdk.iot.deps.serializer;

/**
 * See https://docs.microsoft.com/en-us/rest/api/iothub/common-error-codes for additional details
 */
public enum ErrorCodeDescription
{
    // These descriptions belong to fully qualified status codes, such as 404001
    UnclassifiedErrorCode,
    InvalidProtocolVersion,
    DeviceInvalidResultCount,
    InvalidOperation,
    ArgumentInvalid,
    ArgumentNull,
    IotHubFormatError,
    DeviceStorageEntitySerializationError,
    BlobContainerValidationError,
    ImportWarningExistsError,
    InvalidSchemaVersion,
    DeviceDefinedMultipleTimes,
    DeserializationError,
    BulkRegistryOperationFailure,
    CannotRegisterModuleToModule,
    IotHubNotFound,
    IotHubUnauthorizedAccess,
    IotHubUnauthorized,
    IotHubSuspended,
    IotHubQuotaExceeded,
    JobQuotaExceeded,
    DeviceMaximumQueueDepthExceeded,
    IotHubMaxCbsTokenExceeded,
    DeviceNotFound,
    JobNotFound,
    PartitionNotFound,
    ModuleNotFound,
    DeviceAlreadyExists,
    ModuleAlreadyExistsOnDevice,
    DeviceMessageLockLost,
    MessageTooLarge,
    TooManyDevices,
    TooManyModulesOnDevice,
    ThrottleBacklogLimitExceeded,
    InvalidThrottleParameter,
    ServerError,
    JobCancelled,
    ConnectionForcefullyClosedOnNewConnection,
    DeviceNotOnline,
    DeviceConnectionClosedRemotely,

    // These descriptions belong to 3 digit http response codes such as 404.
    BadFormat,
    Unauthorized,
    Forbidden,
    NotFound,
    Conflict,

    /**
     * Represents status code for 429 and for status code 429001
     */
    PreconditionFailed,

    RequestEntityTooLarge,

    /**
     * Represents status code for 429 and 429001
     */
    ThrottlingException,
    InternalServerError,

    /**
     * Defined for status code 503 and 503001
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
            case(400002):
                return DeviceInvalidResultCount;
            case(400003):
                return InvalidOperation;
            case(400004):
                return ArgumentInvalid;
            case(400005):
                return ArgumentNull;
            case(400006):
                return IotHubFormatError;
            case(400007):
                return DeviceStorageEntitySerializationError;
            case(400008):
                return BlobContainerValidationError;
            case(400009):
                return ImportWarningExistsError;
            case(400010):
                return InvalidSchemaVersion;
            case(400011):
                return DeviceDefinedMultipleTimes;
            case(400012):
                return DeserializationError;
            case(400013):
                return BulkRegistryOperationFailure;
            case (400027):
                return ConnectionForcefullyClosedOnNewConnection;
            case(400301):
                return CannotRegisterModuleToModule;
            case(401001):
                return IotHubNotFound;
            case(401002):
                return IotHubUnauthorizedAccess;
            case(401003):
                return IotHubUnauthorized;
            case(403001):
                return IotHubSuspended;
            case(403002):
                return IotHubQuotaExceeded;
            case(403003):
                return JobQuotaExceeded;
            case(403004):
                return DeviceMaximumQueueDepthExceeded;
            case(403005):
                return IotHubMaxCbsTokenExceeded;
            case(404001):
                return DeviceNotFound;
            case(404002):
                return JobNotFound;
            case(404003):
                return PartitionNotFound;
            case(404010):
                return ModuleNotFound;
            case (404103):
                return DeviceNotOnline;
            case (404104):
                return DeviceConnectionClosedRemotely;
            case(409001):
                return DeviceAlreadyExists;
            case(409301):
                return ModuleAlreadyExistsOnDevice;
            case(412001):
            case (412):
                return PreconditionFailed;
            case(412002):
                return DeviceMessageLockLost;
            case(413001):
                return MessageTooLarge;
            case(413002):
                return TooManyDevices;
            case(413003):
                return TooManyModulesOnDevice;
            case(429001):
            case (429):
                return ThrottlingException;
            case(429002):
                return ThrottleBacklogLimitExceeded;
            case(429003):
                return InvalidThrottleParameter;
            case(500001):
                return ServerError;
            case(500002):
                return JobCancelled;
            case(503001):
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
