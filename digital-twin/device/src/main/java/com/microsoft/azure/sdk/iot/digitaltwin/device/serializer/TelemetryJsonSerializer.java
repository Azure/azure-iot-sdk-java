package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.NonNull;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.createJsonObject;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;

public final class TelemetryJsonSerializer {
    private TelemetryJsonSerializer() {
    }

    public static String serializeTelemetry(
            @NonNull final String telemetryName,
            @NonNull final String payload
    ) throws IOException {
        ObjectNode telemetryNode = createJsonObject();
        telemetryNode.set(telemetryName, deserialize(payload));
        return telemetryNode.toString();
    }

}
