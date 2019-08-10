package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

/** Structure filled by the device application after processing a command on its interface and returned to the Digital Twin SDK. */
@Builder
@Getter
public class DigitalTwinCommandResponse {
    private final static int DIGITAL_TWIN_COMMAND_RESPONSE_VERSION_1 = 1;
    private final static int DIGITAL_TWIN_ASYNC_STATUS_CODE_PENDING = 202;
    /**
     * The version <b>of the structure</b> that the SDK is passing.  This is <b>NOT</b> related to the server version.
     * Currently {@link #DIGITAL_TWIN_COMMAND_RESPONSE_VERSION_1} but would be incremented if new fields are added.
     */
    @Default
    private int version = DIGITAL_TWIN_COMMAND_RESPONSE_VERSION_1;
    /**
     * Status code to map back to the server.  Roughly maps to HTTP status codes.
     * To indicate that this command has been accepted but that the final response is pending, set this to {@link #DIGITAL_TWIN_ASYNC_STATUS_CODE_PENDING}.
     */
    @NonNull
    private final Integer status;
    /**
     * Response payload to send to server.  This *MUST* be allocated with <c>malloc()</c> by the application.
     * The Digital Twin SDK takes responsibility for calling <c>free()</c> on this value when the structure is returned.
     */
    private byte[] payload;
}
