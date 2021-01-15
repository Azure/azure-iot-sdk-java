// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

/**
 * Extend Message to support status response
 */
public final class ResponseMessage extends Message
{
    private final IotHubStatusCode status;

    /**
     * CONSTRUCTOR
     *
     * @param body is the byte array with the response message payload
     * @param status is the response status
     */
    public ResponseMessage(byte[] body, IotHubStatusCode status)
    {
        /* Codes_SRS_RESPONSEMESSAGE_21_001: [The constructor shall save the message body by calling super with the body as parameter.] */
        /* Codes_SRS_RESPONSEMESSAGE_21_002: [If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.] */
        super(body);

        if(status == null)
        {
            /* Codes_SRS_RESPONSEMESSAGE_21_004: [If the message status is null, the constructor shall throw an IllegalArgumentException.] */
            throw new IllegalArgumentException("Null status");
        }

        /* Codes_SRS_RESPONSEMESSAGE_21_003: [The constructor shall save the status.] */
        this.status = status;
    }

    /**
     * Getter for the status
     *
     * @return the status code
     */
    public IotHubStatusCode getStatus()
    {
        /* Codes_SRS_RESPONSEMESSAGE_21_005: [The getStatus shall return the stored status.] */
        return this.status;
    }
}
