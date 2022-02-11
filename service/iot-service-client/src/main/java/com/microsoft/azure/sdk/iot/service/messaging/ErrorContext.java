// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

import com.azure.core.annotation.Immutable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

@Immutable
public class ErrorContext
{
    @NonNull
    @Setter(AccessLevel.PACKAGE)
    @Getter
    Exception exception;

    public ErrorContext(Exception exception)
    {
        this.exception = exception;
    }
}
