package com.microsoft.azure.sdk.iot.digitaltwin.device.model;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;

/**
 * Structure filled in by the Digital Twin SDK on invoking an interface's command callback routine with information about the request.
 */
@Builder
@Getter
public class DigitalTwinCommandRequest {
    private final static int DIGITAL_TWIN_COMMAND_REQUEST_VERSION_1 = 1;
    /**
     * The version <b>of the structure</b> that the SDK is passing.  This is <b>NOT</b> related to the server version.
     * Currently {@link #DIGITAL_TWIN_COMMAND_REQUEST_VERSION_1} but would be incremented if new fields are added.
     */
    @Default
    private int version = DIGITAL_TWIN_COMMAND_REQUEST_VERSION_1;
    /** Name of the command to execute on this interface. */
    @NonNull
    private final String commandName;
    /**
     * A server generated string passed as part of the command.
     * This is used when sending responses to asynchronous commands to act as a correlation Id and/or for diagnostics purposes.
     */
    @NonNull
    private final String requestId;
    /** Raw payload of the request. */
    private byte[] payload;
}
