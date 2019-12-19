// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * Structure filled in by the device application when it is updating an asynchronous command's status.
 */
@Builder
@Getter
public class DigitalTwinAsyncCommandUpdate {
    /**
     * Status code to map back to the server. Roughly maps to HTTP status codes.
     */
    @NonNull
    private final Integer statusCode;
    /**
     * The command from the server that initiated the request that we are updating.
     * This comes from the structure {@link DigitalTwinCommandRequest#getCommandName()} passed to the device application's command callback handler.
     */
    @NonNull
    private final String commandName;
    /**
     * The requestId from the server that initiated the request that we are updating.
     * This comes from the structure {@link DigitalTwinCommandRequest#getRequestId()} ()} passed to the device application's command callback handler.
     */
    @NonNull
    private final String requestId;
    /**
     * Payload that the device should send to the service.
     */
    private final String payload;
}
