// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Structure filled in by the Digital Twin SDK on invoking an interface's command callback routine with information about the request.
 */
@Builder
@Getter
public class DigitalTwinCommandRequest {
    /**
     * Name of the command to execute on this interface.
     */
    @NonNull
    private final String commandName;
    /**
     * A server generated string passed as part of the command.
     * This is used when sending responses to asynchronous commands to act as a correlation Id and/or for diagnostics purposes.
     */
    @NonNull
    private final String requestId;
    /**
     * Raw payload of the request.
     */
    private final String payload;
}
