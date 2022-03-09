// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.exceptions.*;

/**
 * An IoT Hub status code. Included in a message from an IoT Hub to a device.
 */
public enum IotHubStatusCode
{
    OK,
    BAD_FORMAT,
    UNAUTHORIZED,
    TOO_MANY_DEVICES,
    HUB_OR_DEVICE_ID_NOT_FOUND,
    PRECONDITION_FAILED,
    REQUEST_ENTITY_TOO_LARGE,
    THROTTLED,
    INTERNAL_SERVER_ERROR,
    SERVER_BUSY,
    ERROR,
    MESSAGE_EXPIRED,
    MESSAGE_CANCELLED_ONCLOSE;

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
            case TOO_MANY_DEVICES:
                transportException = new TooManyDevicesException(statusDescription);
                break;
            case HUB_OR_DEVICE_ID_NOT_FOUND:
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
                iotHubStatus = TOO_MANY_DEVICES;
                break;
            case 404:
                iotHubStatus = HUB_OR_DEVICE_ID_NOT_FOUND;
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
}
