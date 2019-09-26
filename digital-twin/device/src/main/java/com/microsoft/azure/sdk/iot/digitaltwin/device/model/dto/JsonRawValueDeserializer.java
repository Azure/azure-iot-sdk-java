// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.model.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class JsonRawValueDeserializer extends StdDeserializer<JsonRawValue> {
    public JsonRawValueDeserializer() {
        super(JsonRawValue.class);
    }

    @Override
    public JsonRawValue deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return new JsonRawValue(parser.getCodec().readTree(parser).toString());
    }
}
