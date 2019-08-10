package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public final class JsonSerializer implements Serializer {
    private final static Serializer INSTANCE = new JsonSerializer();
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(NON_NULL);

    private JsonSerializer() {
    }

    public static Serializer getInstance() {
        return INSTANCE;
    }

    public byte[] serialize(Object object) throws IOException {
        return OBJECT_MAPPER.writeValueAsBytes(object);
    }
    public <T> T deserialize(byte[] payload, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(payload, clazz);
    }
    public <T> T deserialize(String payload, Class<T> clazz) throws IOException {
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
}
