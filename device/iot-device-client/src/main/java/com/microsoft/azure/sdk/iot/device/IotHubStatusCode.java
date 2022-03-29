// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.transport.https.exceptions.*;

/**
 * An IoT Hub status code. Included in a message from an IoT Hub to a device.
 */
public enum IotHubStatusCode
{
    /**
     * The request completed without exception
     */
    OK,

    /**
     * The request failed because it had one or more format issues.
     */
    BAD_FORMAT,

    /**
     * The request failed because the provided credentials are out of date or incorrect.
     */
    UNAUTHORIZED,

    /**
     * The request failed because the quota for such operations has been exceeded. For file upload operations, this
     * signifies that the maximum number of concurrent file upload operations are already happening. For telemetry operations,
     * this signifies that the IoT hub has reached its daily quota for the number of messages ingested.
     */
    QUOTA_EXCEEDED,

    /**
     * The request failed because the resource the request targeted does not exist.
     */
    NOT_FOUND,

    /**
     * The request failed because the request provided an out of date ETag or version number.
     */
    PRECONDITION_FAILED,

    /**
     * The request failed because the request payload exceeded IoT Hub's size limits.
     */
    REQUEST_ENTITY_TOO_LARGE,

    /**
     * The request was rejected by the service because the service is handling too many requests right now.
     */
    THROTTLED,

    /**
     * The service encountered an error while handling the request.
     */
    INTERNAL_SERVER_ERROR,

    /**
     * The request was rejected by the service because it is too busy to handle it right now.
     */
    SERVER_BUSY,

    /**
     * The request failed for an unknown reason.
     */
    ERROR,

    /**
     * The request failed to be sent to the service and/or acknowledged by the service before it expired.
     */
    MESSAGE_EXPIRED,

    /**
     * The request failed to be sent to the service and/or acknowledged by the service before the client was closed.
     */
    MESSAGE_CANCELLED_ONCLOSE,

    /**
     * The request failed because of network level issues.
     */
    IO_ERROR,

    /**
     * The request failed because it took longer than the device operation timeout as defined in {@link DeviceClient#setOperationTimeout(long)}.
     */
    DEVICE_OPERATION_TIMED_OUT;

    public static IotHubServiceException getConnectionStatusException(IotHubStatusCode statusCode, String statusDescription)
    {
        IotHubServiceException transportException;
        switch (statusCode)
        {
            case OK:
            case MESSAGE_CANCELLED_ONCLOSE:
            case MESSAGE_EXPIRED:
                transportException = null;
                break;
            case BAD_FORMAT:
                transportException = new BadFormatException(statusDescription);
                break;
            case UNAUTHORIZED:
                transportException = new UnauthorizedException(statusDescription);
                break;
            case QUOTA_EXCEEDED:
                transportException = new TooManyDevicesException(statusDescription);
                break;
            case NOT_FOUND:
                transportException = new HubOrDeviceIdNotFoundException(statusDescription);
                break;
            case PRECONDITION_FAILED:
                transportException = new PreconditionFailedException(statusDescription);
                break;
            case REQUEST_ENTITY_TOO_LARGE:
                transportException = new RequestEntityTooLargeException(statusDescription);
                break;
            case THROTTLED:
                transportException = new ThrottledException(statusDescription);
                break;
            case INTERNAL_SERVER_ERROR:
                transportException = new InternalServerErrorException(statusDescription);
                break;
            case SERVER_BUSY:
                transportException = new ServerBusyException(statusDescription);
                break;
            case ERROR:
                transportException = new ServiceUnknownException("Service gave unknown status code: " + statusCode);
                break;
            default:
                transportException = new IotHubServiceException("Service gave unknown status code: " + statusCode);
        }

        return transportException;
    }

    /**
     * Returns the IoT Hub status code referenced by the HTTPS status code.
     *
     * @param httpsStatus the HTTPS status code.
     *
     * @return the corresponding IoT Hub status code.
     *
     * @throws IllegalArgumentException if the HTTPS status code does not map to
     * an IoT Hub status code.
     */
    public static IotHubStatusCode getIotHubStatusCode(int httpsStatus)
    {
        IotHubStatusCode iotHubStatus;
        switch (httpsStatus)
        {
            case 200:
            case 204:
                iotHubStatus = OK;
                break;
            case 400:
                iotHubStatus = BAD_FORMAT;
                break;
            case 401:
                iotHubStatus = UNAUTHORIZED;
                break;
            case 403:
                iotHubStatus = QUOTA_EXCEEDED;
                break;
            case 404:
                iotHubStatus = NOT_FOUND;
                break;
            case 412:
                iotHubStatus = PRECONDITION_FAILED;
                break;
            case 413:
                iotHubStatus = REQUEST_ENTITY_TOO_LARGE;
                break;
            case 429:
                iotHubStatus = THROTTLED;
                break;
            case 500:
                iotHubStatus = INTERNAL_SERVER_ERROR;
                break;
            case 503:
                iotHubStatus = SERVER_BUSY;
                break;
            default:
                iotHubStatus = ERROR;
        }

        return iotHubStatus;
    }

    /**
     * Returns true if this event callback signals that the asynchronous action was unsuccessful, but could be retried.
     * Returns false if it was successful, or it was unsuccessful but should not be retried.
     * @param statusCode The status code.
     * @return true if this event callback signals that the asynchronous action was unsuccessful, but could be retried, and false otherwise.
     */
    public static boolean isRetryable(IotHubStatusCode statusCode)
    {
        switch (statusCode)
        {
            case ERROR:
            case MESSAGE_CANCELLED_ONCLOSE:
            case MESSAGE_EXPIRED:
            case THROTTLED:
            case INTERNAL_SERVER_ERROR:
            case SERVER_BUSY:
                return true;
            default:
                return false; // even for OK case, return false here since it wouldn't need to be retried.
        }
    }

    /**
     * Returns true if this event callback signals that the asynchronous action was successful, and false otherwise.
     * @param statusCode The status code.
     * @return true if this event callback signals that the asynchronous action was successful, and false otherwise.
     */
    public static boolean isSuccessful(IotHubStatusCode statusCode)
    {
        return statusCode == OK;
    }

    public static IotHubClientException toException(IotHubStatusCode statusCode)
    {
        if (!IotHubStatusCode.isSuccessful(statusCode))
        {
            return new IotHubClientException(statusCode);
        }

        return null;
    }
}
