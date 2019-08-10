package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled in by the device application when it is updating an asynchronous command's status. */
@Builder
@Getter
public class DigitalTwinAsyncCommandUpdate {
    private final static int DIGITAL_TWIN_ASYNC_COMMAND_UPDATE_VERSION_1 = 1;
    /** The version of this structure (not the server version). Currently must be {@link #DIGITAL_TWIN_ASYNC_COMMAND_UPDATE_VERSION_1}. */
    @Default
    private int version = DIGITAL_TWIN_ASYNC_COMMAND_UPDATE_VERSION_1;
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
    private byte[] payload;
}
