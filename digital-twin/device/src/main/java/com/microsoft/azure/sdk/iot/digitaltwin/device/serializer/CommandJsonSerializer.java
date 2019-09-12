package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.microsoft.azure.sdk.iot.digitaltwin.device.model.DigitalTwinCommandRequest.DigitalTwinCommandRequestBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.getAttributeAsString;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.isNullOrEmpty;

@Slf4j
public final class CommandJsonSerializer {
    private static final String ATTRIBUTE_COMMAND_REQUEST = "commandRequest";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ATTRIBUTE_REQUEST_ID = "requestId";
;
    private CommandJsonSerializer() {
    }

    public static void deserializeCommandRequest(DigitalTwinCommandRequestBuilder builder, byte[] payload) throws IOException {
        JsonNode root = deserialize(payload);
        if (root == null) {
            throw new IOException("Empty command payload.");
        }

        JsonNode command = root.get(ATTRIBUTE_COMMAND_REQUEST);
        if (command == null) {
            throw new IOException("Attribute['commandRequest'] not found.");
        }

        String requestId = getAttributeAsString(command, ATTRIBUTE_REQUEST_ID);
        if (isNullOrEmpty(requestId)) {
            throw new IOException("Attribute['requestId'] not found.");
        }

        builder.requestId(requestId);

        JsonNode value = command.get(ATTRIBUTE_VALUE);
        if (value != null) {
            builder.payload(value.toString());
        }
    }

}
