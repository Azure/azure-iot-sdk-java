package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public final class JsonSerializer {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(NON_NULL);

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
    public static <T> T nodeToValue(JsonNode node, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.treeToValue(node, clazz);
    }
    public static ObjectNode createJsonObject() {
        return OBJECT_MAPPER.createObjectNode();
    }
    public static <T> JsonNode valueToNode(T instance) {
        return OBJECT_MAPPER.valueToTree(instance);
    }
    public static JsonNode deserialize(byte[] content) throws IOException {
        return OBJECT_MAPPER.readTree(content);
    }
    public static JsonNode deserialize(String content) throws IOException {
        return OBJECT_MAPPER.readTree(content);
    }
    public static String getAttributeAsString(JsonNode node, String name) {
        JsonNode attribute = node.get(name);
        if (attribute == null || !attribute.isTextual()) {
            return null;
        } else {
            return attribute.asText();
        }
    }
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }
    public static boolean isNotEmpty(String value) {
        return !isNullOrEmpty(value);
    }
}
