// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public final class JsonSerializer {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(NON_NULL);

    private JsonSerializer() {
    }

    public static <T> String serialize(T object) throws IOException {
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    public static <T> T deserialize(byte[] payload, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(payload, clazz);
    }

    public static <T> T deserialize(String payload, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(payload, clazz);
    }

    public static <T> T deserialize(String payload, TypeReference<T> typeReference) throws IOException {
        return OBJECT_MAPPER.readValue(payload, typeReference);
    }

    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean isNotEmpty(String value) {
        return !isNullOrEmpty(value);
    }
}
