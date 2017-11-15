/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.provisioning.service.exceptions;

/**
 * Super class for the Device Provisioning Service exceptions on the Service Client.
 *
 * <pre>
 * {@code
 * ProvisioningServiceClientException
 *     |
 *     +-->ProvisioningServiceClientTransportException [any transport layer exception]
 *     |
 *     +-->ProvisioningServiceClientServiceException [any exception reported in the http response]
 *             |
 *             |
 *             +-->ProvisioningServiceClientBadUsageException [any http response 4xx]
 *             |        |
 *             |        +-->ProvisioningServiceClientBadFormatException [400]
 *             |        +-->ProvisioningServiceClientUnathorizedException [401]
 *             |        +-->ProvisioningServiceClientNotFoundException [404]
 *             |        +-->ProvisioningServiceClientPreconditionFailedException [412]
 *             |        +-->ProvisioningServiceClientTooManyRequestsException [429]
 *             |
 *             +-->ProvisioningServiceClientTransientException [any http response 5xx]
 *             |        |
 *             |        +-->ProvisioningServiceClientInternalServerErrorException [500]
 *             |
 *             +-->ProvisioningServiceClientUnknownException [any other http response >300, but not 4xx or 5xx]
 * }
 * </pre>
 */
public class ProvisioningServiceClientException extends Exception
{
    public ProvisioningServiceClientException()
    {
        super();
    }

    public ProvisioningServiceClientException(String message)
    {
        super(((message == null) || message.isEmpty()) ? "General Device Provisioning Service exception!" : message);
    }

    public ProvisioningServiceClientException(String message, Throwable cause)
    {
        super((((message == null) || message.isEmpty()) ? "General Device Provisioning Service exception!" : message), cause);
    }

    public ProvisioningServiceClientException(Throwable cause)
    {
        super(cause);
    }
}
