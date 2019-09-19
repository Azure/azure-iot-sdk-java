package com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class JsonRawValueSerializer extends StdSerializer<JsonRawValue> {
    public JsonRawValueSerializer() {
        super(JsonRawValue.class);
    }

    @Override
    public void serialize(JsonRawValue value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeRawValue(value.getRawJson());
    }
}
