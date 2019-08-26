package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled in by the device application when it is updating an asynchronous command's status. */
@Builder
@Getter
public class DigitalTwinAsyncCommandUpdate {
    /** Status code to map back to the server. Roughly maps to HTTP status codes.*/
    @NonNull
    private Integer statusCode;
    /**
     * The command from the server that initiated the request that we are updating.
     * This comes from the structure {@link DigitalTwinCommandRequest#getCommandName()} passed to the device application's command callback handler.
     */
    @NonNull
    private String commandName;
    /**
     * The requestId from the server that initiated the request that we are updating.
     * This comes from the structure {@link DigitalTwinCommandRequest#getRequestId()} ()} passed to the device application's command callback handler.
     */
    @NonNull
    private String requestId;
    /** Payload that the device should send to the service. */
    private String payload;
}
