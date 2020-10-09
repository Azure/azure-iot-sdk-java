// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.digitaltwin.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Configures the {@link StdSerializer} with custom behavior needed to work with Digital Twins REST API.
 */
public final class DigitalTwinStringSerializer extends StdSerializer<String> {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper;

    public DigitalTwinStringSerializer(Class<String> t, ObjectMapper mapper) {
        super(t);
        this.mapper = mapper;
    }

    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (isValidJson(s)) {
            jsonGenerator.writeRawValue(s);
        } else {
            jsonGenerator.writeString(s);
        }
    }

    private boolean isValidJson(String jsonInString ) {
        try {
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
