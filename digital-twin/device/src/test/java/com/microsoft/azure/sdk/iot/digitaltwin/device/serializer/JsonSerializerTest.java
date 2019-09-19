package com.microsoft.azure.sdk.iot.digitaltwin.device.serializer;

import lombok.Data;
import org.junit.Test;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.deserialize;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.isNotEmpty;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.isNullOrEmpty;
import static com.microsoft.azure.sdk.iot.digitaltwin.device.serializer.JsonSerializer.serialize;
import static java.lang.Boolean.TRUE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class JsonSerializerTest {
    private static final int NUMBER = 123456;
    private static final String STRING = "abc";
    private static final String JSON_STRING = String.format("\"%s\"", STRING);
    private static final String TEST_PAYLOAD = "{\"number\":123456,\"str\":\"abc\",\"bool\":true}";

    @Test
    public void serializeBooleanTest() throws IOException {
        String booleanJson = serialize(true);
        assertThat(booleanJson).isEqualTo(TRUE.toString());
    }

    @Test
    public void serializeStringTest() throws IOException {
        String stringJson = serialize(STRING);
        assertThat(stringJson).isEqualTo(JSON_STRING);
    }

    @Test
    public void serializeObjectTest() throws IOException {
        TestObject testObject = new TestObject();
        testObject.setBool(true);
        testObject.setNumber(NUMBER);
        testObject.setStr(STRING);
        String objectJson = serialize(testObject);
        assertThat(objectJson).isEqualTo(TEST_PAYLOAD);
    }

    @Test
    public void deserializeBooleanFromByteArrayTest() throws IOException {
        boolean booleanValue = deserialize(TRUE.toString().getBytes(UTF_8), Boolean.class);
        assertThat(booleanValue).isTrue();
    }

    @Test
    public void deserializeStringFromByteArrayTest() throws IOException {
        String stringValue = deserialize(JSON_STRING.getBytes(UTF_8), String.class);
        assertThat(stringValue).isEqualTo(STRING);
    }

    @Test
    public void deserializeNumberFromByteArrayTest() throws IOException {
        int intValue = deserialize(String.valueOf(NUMBER).getBytes(UTF_8), Integer.class);
        assertThat(intValue).isEqualTo(NUMBER);
    }

    @Test
    public void deserializeObjectFromByteArrayTest() throws IOException {
        TestObject testObject = deserialize(TEST_PAYLOAD.getBytes(UTF_8), TestObject.class);
        assertThat(testObject.getNumber()).isEqualTo(NUMBER);
        assertThat(testObject.isBool()).isTrue();
        assertThat(testObject.getStr()).isEqualTo(STRING);
    }

    @Test
    public void deserializeBooleanFromStringTest() throws IOException {
        boolean booleanValue = deserialize(TRUE.toString(), Boolean.class);
        assertThat(booleanValue).isTrue();
    }

    @Test
    public void deserializeStringFromStringTest() throws IOException {
        String stringValue = deserialize(JSON_STRING, String.class);
        assertThat(stringValue).isEqualTo(STRING);
    }

    @Test
    public void deserializeNumberFromStringTest() throws IOException {
        int intValue = deserialize(String.valueOf(NUMBER), Integer.class);
        assertThat(intValue).isEqualTo(NUMBER);
    }

    @Test
    public void deserializeObjectFromStringTest() throws IOException {
        TestObject testObject = deserialize(TEST_PAYLOAD, TestObject.class);
        assertThat(testObject.getNumber()).isEqualTo(NUMBER);
        assertThat(testObject.isBool()).isTrue();
        assertThat(testObject.getStr()).isEqualTo(STRING);
    }

    @Test
    public void isNullOrEmptyTest() {
        assertThat(isNullOrEmpty(STRING)).isFalse();
        assertThat(isNullOrEmpty(null)).isTrue();
        assertThat(isNullOrEmpty("")).isTrue();
    }

    @Test
    public void isNotEmptyTest() {
        assertThat(isNotEmpty(STRING)).isTrue();
        assertThat(isNotEmpty(null)).isFalse();
        assertThat(isNotEmpty("")).isFalse();
    }

    @Data
    public static class TestObject {
        private int number;
        private String str;
        private boolean bool;
    }
}
