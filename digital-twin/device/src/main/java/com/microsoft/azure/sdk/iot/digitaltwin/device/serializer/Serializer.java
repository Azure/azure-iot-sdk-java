package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import java.io.IOException;

public interface Serializer {
    byte[] serialize(Object object) throws IOException;
    <T> T deserialize(byte[] payload, Class<T> clazz) throws IOException;
    <T> T deserialize(String payload, Class<T> clazz) throws IOException;
}
