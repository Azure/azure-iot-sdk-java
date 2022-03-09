/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

import lombok.Getter;

/**
 * Super class for IotHub exceptions
 */
public class IotHubException extends Exception
{
    /**
     * Instantiate a new IotHubException with no details.
     */
    public IotHubException()
    {
        this(null);
    }

    /**
     * Instantiate a new IotHubException.
     * @param message the human readable description for why this exception was thrown.
     */
    public IotHubException(String message)
    {
        this(message, ErrorMessageParser.getDefaultErrorCode(), ErrorCodeDescription.UnclassifiedErrorCode);
    }

    IotHubException(String message, int errorCode, ErrorCodeDescription errorCodeDescription)
    {
        super(message);
        this.errorCodeDescription = errorCodeDescription;
        this.errorCode = errorCode;
    }

    /**
     * Instantiate a new IotHubException.
     * @param errorCode the Http status code (200, 401, 404, etc.)
     * @param message the human readable description for why this exception was thrown.
     */
    public IotHubException(int errorCode, String message)
    {
        super(message);
        this.errorCodeDescription = ErrorCodeDescription.Parse(errorCode);
        this.errorCode = errorCode;
    }

    /**
     * <p>Provides the HTTP error code, if applicable.</p>
     *
     * <p>This value will be a 6 digital error code such as 404001 if the service provided one in response message to the HTTP request.
     * Otherwise it will be a 3 digit status code such as 404.</p>
     *
     * <p>For AMQP operations such as sending cloud to device messages,
     * receiving message feedback, and getting file upload notifications, this field will not be populated.</p>
     */
    @Getter
    protected int errorCode;

    /**
     * <p>Provides the HTTP error code description, if applicable.</p>
     *
     * <p>For AMQP operations such as sending cloud to device messages,
     * receiving message feedback, and getting file upload notifications, this field will not be populated.</p>
     */
    @Getter
    protected ErrorCodeDescription errorCodeDescription;
}
