// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class SendResult
{
    private boolean sentSuccessfully;

    @Getter
    private Object context;

    @Getter
    private IotHubException exception;

    // opting not to use lombok getter so that the naming is better
    public boolean wasSentSuccessfully()
    {
        return this.sentSuccessfully;
    }
}
