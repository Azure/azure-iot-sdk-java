/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.util;


import com.microsoft.azure.sdk.iot.deps.util.Tools;
import mockit.Expectations;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Utility function collection
 */
public class ToolsTest
{
    // Tests_SRS_SDK_JAVA_TOOLS_12_001: [The function shall return true if the input is null]      @Test
    @Test
    public void isNullOrEmptyInputNullCalledWithNull()
    {
        // Arrange
        String value = null;
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_002: [The function shall return true if the input string’s length is zero]
    @Test
    public void isNullOrEmptyCalledWithEmpty()
    {
        // Arrange
        String value = "";
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_003: [The function shall return false otherwise]
    @Test
    public void isNullOrEmptyInputNotEmpty()
    {
        // Arrange
        String value = "XXX";
        Boolean expResult = false;
        // Act
        Boolean result = Tools.isNullOrEmpty(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_004: [The function shall return true if the input is null]
    @Test
    public void isNullOrWhiteSpaceInputNull()
    {
        // Arrange
        String value = null;
        Boolean expResult = true;
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_005: [The function shall call the isNullOrEmpty function and return with it’s return value]
    @Test
    public void isNullOrWhiteSpaceInputWhiteSpace()
    {
        // Arrange
        String value = " ";
        Boolean expResult = true;
        new Expectations()
        {
            Tools tools;
            {
                tools.isNullOrEmpty(anyString);
            }
        };
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_005: [The function shall call the isNullOrEmpty function and return with it’s return value]
    @Test
    public void isNullOrWhiteSpaceInputWhiteSpaces()
    {
        // Arrange
        String value = "   ";
        Boolean expResult = true;
        new Expectations()
        {
            Tools tools;
            {
                tools.isNullOrEmpty(anyString);
            }
        };
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_005: [The function shall call the isNullOrEmpty function and return with it’s return value]
    @Test
    public void isNullOrWhiteSpaceNotEmpty()
    {
        // Arrange
        String value = "XXX";
        Boolean expResult = false;
        new Expectations()
        {
            Tools tools;
            {
                tools.isNullOrEmpty(anyString);
            }
        };
        // Act
        Boolean result = Tools.isNullOrWhiteSpace(value);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_006: [The function shall return empty string if any of the input is null]
    @Test
    public void getValueStringByKeyMapNull()
    {
        // Arrange
        Map<String, String> map = null;
        String keyName = "";
        String expResult = "";
        // Act
        String result = Tools.getValueStringByKey(map, keyName);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_006: [The function shall return empty string if any of the input is null]
    @Test
    public void getValueStringByKeyKeyNameNull()
    {
        // Arrange
        Map<String, String> map = new HashMap<String, String>();
        String keyName = null;
        String expResult = "";
        // Act
        String result = Tools.getValueStringByKey(map, keyName);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_007: [The function shall get the value of the given key from the map]
    // Tests_SRS_SDK_JAVA_TOOLS_12_008: [The function shall return with trimmed string if the value of the key is not null]
    @Test
    public void getValueStringByKeyGoodCase()
    {
        // Arrange
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", "value3");
        String keyName = "key2";
        String expResult = "value2";
        // Act
        String result = Tools.getValueStringByKey(map, keyName);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_007: [The function shall get the value of the given key from the map]
    // Tests_SRS_SDK_JAVA_TOOLS_12_008: [The function shall return with trimmed string if the value of the key is not null]
    @Test
    public void getValueStringByKeyGoodCaseTrim()
    {
        // Arrange
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("key2", "    value2 ");
        String keyName = "key2";
        String expResult = "value2";
        // Act
        String result = Tools.getValueStringByKey(map, keyName);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_009: [The function shall return with empty string if the value of the key is null]
    @Test
    public void getValueStringByKeyValueNull()
    {
        // Arrange
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("key2", null);
        String keyName = "key2";
        String expResult = "";
        // Act
        String result = Tools.getValueStringByKey(map, keyName);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_010: [The function shall return empty string if any of the input is null]
    @Test
    public void getValueFromJsonObjectInputObjectNull()
    {
        // Arrange
        String key = "key";
        String expResult = "";
        // Act
        String result = Tools.getValueFromJsonObject(null, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_010: [The function shall return empty string if any of the input is null]
    @Test
    public void getValueFromJsonObjectInputKeyNull()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = null;
        String expResult = "";
        // Act
        String result = Tools.getValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_010: [The function shall return empty string if any of the input is null]
    @Test
    public void getValueFromJsonObjectInputKeyEmpty()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = "";
        String expResult = "";
        // Act
        String result = Tools.getValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_011: [The function shall get the JsonValue of the key]
    // Tests_SRS_SDK_JAVA_TOOLS_12_012: [The function shall get the JsonString from the JsonValue if the JsonValue is not null]
    // Tests_SRS_SDK_JAVA_TOOLS_12_013: [The function shall return the string value from the JsonString calling the getValueFromJsonString function]
    @Test
    public void getValueFromJsonObjectGoodCase()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = "generationId";
        String expResult = "111111111111111111";
        // Act
        String result = Tools.getValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_014: [The function shall return empty string if the JsonValue is null]
    @Test
    public void getValueFromJsonObjectValueNull()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":null,\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = "generationId";
        String expResult = "";
        // Act
        String result = Tools.getValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_015: [The function shall return empty string if the input is null]
    @Test
    public void getValueFromJsonStringInputNull()
    {
        // Arrange
        String expResult = "";
        // Act
        String result = Tools.getValueFromJsonString(null);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_016: [The function shall get the string value from JsonString]
    // Tests_SRS_SDK_JAVA_TOOLS_12_017: [The function shall trim the leading and trailing parenthesis from the string and return with it]
    @Test
    public void getValueFromJsonStringGoodCase()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = "generationId";
        JsonString jsonStringObject = jsonObject.getJsonString(key);
        String expResult = "111111111111111111";
        // Act
        String result = Tools.getValueFromJsonString(jsonStringObject);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_018: [The function shall return zero if any of the input is null]
    @Test
    public void getNumberValueFromJsonObjectInputObjectNull()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        String key = "";
        long expResult = 0;
        // Act
        long result = Tools.getNumberValueFromJsonObject(null, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_018: [The function shall return zero if any of the input is null]
    @Test
    public void getNumberValueFromJsonObjectInputKeyNull()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = null;
        long expResult = 0;
        // Act
        long result = Tools.getNumberValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_018: [The function shall return zero if any of the input is null]
    @Test
    public void getNumberValueFromJsonObjectInputKeyEmpty()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":\"111111111111111111\",\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = "";
        long expResult = 0;
        // Act
        long result = Tools.getNumberValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_019: [The function shall get the JsonValue of the key and return zero if it is null]
    // Tests_SRS_SDK_JAVA_TOOLS_12_020: [The function shall get the JsonNumber from the JsonValue and return zero if it is null]
    // Tests_SRS_SDK_JAVA_TOOLS_12_020: [The function shall get the JsonNumber from the JsonValue and return zero if it is null]
    @Test
    public void getNumberValueFromJsonObjectJsonValueNull()
    {
        // Arrange
        String jsonString = "{\"deviceId\":\"xxx-device\",\"generationId\":null,\"etag\":\"MA==\",\"connectionState\":\"Disconnected\",\"status\":\"Disabled\",\"statusReason\":null,\"connectionStateUpdatedTime\":\"0001-01-01T00:00:00\",\"statusUpdatedTime\":\"0001-01-01T00:00:00\",\"lastActivityTime\":\"0001-01-01T00:00:00\",\"cloudToDeviceMessageCount\":0,\"authentication\":{\"symmetricKey\":{\"primaryKey\":\"AAABBBCCC111222333444000\",\"secondaryKey\":\"111222333444555AAABBBCCC\"}}}";
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = Json.createReader(stringReader);
        JsonObject jsonObject = jsonReader.readObject();
        String key = "generationId";
        long expResult = 0;
        // Act
        long result = Tools.getNumberValueFromJsonObject(jsonObject, key);
        // Assert
        assertEquals(expResult, result);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_022: [The function shall do nothing if the input StringBuilder is null]
    @Test
    public void appendJsonAttributeInputNull()
    {
        Tools.appendJsonAttribute(null, "keyname", "value", false, false);
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseNameNull()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"\":value,";
        // Act
        Tools.appendJsonAttribute(stringBuilder, null, "value", false, false);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseNameEmpty()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"\":value,";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "", "value", false, false);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseValueNull()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"keyname\":null,";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "keyname", null, false, false);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseValueEmpty()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"keyname\":null,";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "keyname", null, false, false);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseNotquotedNotlast()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"keyname\":value,";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "keyname", "value", false, false);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseNotquotedLast()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"keyname\":value";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "keyname", "value", false, true);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseQuotedNotlast()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"keyname\":\"value\",";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "keyname", "value", true, false);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }

    // Tests_SRS_SDK_JAVA_TOOLS_12_023: [The function shall append the input StringBuilder string using the following format: “name”:”value” if isQuoted is false]
    // Tests_SRS_SDK_JAVA_TOOLS_12_024: [The function shall append the input StringBuilder string using the following format: “name”:””value”” if isQuoted is true]
    @Test
    public void appendJsonAttributeGoodCaseQuotedLast()
    {
        // Arrange
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("prefix_");
        String expResult = "prefix_\"keyname\":\"value\"";
        // Act
        Tools.appendJsonAttribute(stringBuilder, "keyname", "value", true, true);
        // Assert
        assertEquals(expResult, stringBuilder.toString());
    }
}
