package com.microsoft.azure.sdk.iot.device.certificatesigning;

public enum IotHubCertificateSigningErrorCode
{
    /**
     * This error code should never happen since this SDK hardcodes the protocol version to a valid version
     */
    InvalidProtocolVersion,

    OperationNotAvailableInCurrentTier,
    PreconditionFailed,
    CredentialManagementPreconditionFailed,
    ThrottleBacklogLimitExceeded,
    ThrottlingBacklogTimeout,
    CredentialOperationPending,
    CredentialOperationActive,
    CredentialOperationFailed,
    DeviceNotFound,
    DeviceUnavailable,
    ServerError,
    ServiceUnavailable,
    Unknown;

    public static IotHubCertificateSigningErrorCode GetValue(int code)
    {
        switch (code)
        {
            case 400001:
                return InvalidProtocolVersion;
            case 400040:
                return CredentialOperationFailed;
            case 403010:
                return OperationNotAvailableInCurrentTier;
            case 404001:
                return DeviceNotFound;
            case 409004:
                return CredentialOperationPending;
            case 409005:
                return CredentialOperationActive;
            case 412001:
                return PreconditionFailed;
            case 412005:
                return CredentialManagementPreconditionFailed;
            case 429002:
                return ThrottleBacklogLimitExceeded;
            case 429003:
                return ThrottlingBacklogTimeout;
            case 503102:
                return DeviceUnavailable;
            case 500001:
                return ServerError;
            case 503001:
                return ServiceUnavailable;
            default:
                return Unknown;
        }
    }
}
