package com.microsoft.azure.sdk.iot.digitaltwin.device;

import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandResponse;

import java.util.List;

/**
 * Callback that is invoked by the Digital Twin SDK when a command is invoked from the service.
 * When a command arrives from the service for a particular interface, {@link DigitalTwinCommandRequest} specifies its callback signature.
*/
public interface DigitalTwinCommandExecuteCallback {
    /**
     * Callback that is invoked by the Digital Twin SDK when a command is invoked from the service.
     * @param digitalTwinCommandRequest {@link DigitalTwinCommandRequest} structure filled in by the SDK with information about the command.
     * @param context Context pointer that was specified in the parameter context during the device application's {@link DigitalTwinDeviceClient#registerInterfacesAsync(String, List, DigitalTwinCallback, Object)} call.
     * @return {@link DigitalTwinCommandResponse} to be filled in by the application with the response code and payload to be returned to the service.
     */
    DigitalTwinCommandResponse onCommandReceived(DigitalTwinCommandRequest digitalTwinCommandRequest, Object context);
}
