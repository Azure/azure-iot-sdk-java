// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SendResult
{
    private final boolean sentSuccessfully; //TODO test out case where this is false

    @Getter
    private final String correlationId;

    @Getter
    private final Object context;

    @Getter
    private final IotHubException exception;

    // opting not to use lombok getter so that the naming can be improved
    public boolean wasSentSuccessfully()
    {
        return this.sentSuccessfully;
    }
}
