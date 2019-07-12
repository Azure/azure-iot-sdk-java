/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.exceptions;

/**
 * 409 Conflict
 * Likely caused by trying to create a resource that already exists
 */
public class IotHubConflictException extends IotHubException
{
    public IotHubConflictException()
    {
        this(null);
    }

    public IotHubConflictException(String message)
    {
        super(message);
    }
}
