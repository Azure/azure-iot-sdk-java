/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Create unauthorized exception
 *
 * <p> The authorization token cannot be validated; for example, it is expired or does not apply to the
 *     request’s URI. This error code is also returned to devices as part of the TPM attestation flow.
 * <p> HTTP status code 401
 */
public class ProvisioningServiceClientUnathorizedException extends ProvisioningServiceClientBadUsageException
{
    public ProvisioningServiceClientUnathorizedException()
    {
        super();
    }

    public ProvisioningServiceClientUnathorizedException(String message)
    {
        super("Unauthorized!" + (((message == null) || message.isEmpty()) ? "" : " " + message));
    }
}
