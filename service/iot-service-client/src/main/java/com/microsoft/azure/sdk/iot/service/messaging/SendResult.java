// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Consumer;

/**
 * The result of sending a cloud to device or cloud to module message.
 */
@AllArgsConstructor
public class SendResult
{
    private final boolean sentSuccessfully;

    /**
     * The correlationId of the message that this send result is for.
     */
    @Getter
    private final String correlationId;

    /**
     * The user-defined context provided when sending this message using {@link MessagingClient#sendAsync(String, Message, Consumer, Object)}.
     */
    @Getter
    private final Object context;

    /**
     * The exception encountered while sending the message. If no exception was encountered, then this will be null. If
     * {@link #wasSentSuccessfully()} returns true, then this value will be null.
     */
    @Getter
    private final IotHubException exception;

    /**
     * @return true if the message was sent to the service successfully, and false otherwise.
     */
    public boolean wasSentSuccessfully()
    {
        // opting not to use lombok getter so that the naming can be improved
        return this.sentSuccessfully;
    }
}
