// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.methods;

import com.google.gson.*;
import com.microsoft.azure.sdk.iot.service.Helpers;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
import mockit.Deencapsulation;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * Unit tests for Method serializer
 * 100% methods, 97% lines covered
 */
public class MethodParserTest
{
    private static final String BIG_STRING_150CHARS =
            "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789";
    private static final String ILLEGAL_STRING_DOT = "illegal.key";
    private static final String ILLEGAL_STRING_SPACE = "illegal key";
    private static final String ILLEGAL_STRING_DOLLAR = "illegal$key";
    private static final String ILLEGAL_STRING_INJECTION = "illegal\",key";
    private static final String STANDARD_NAME = "validName";
    private static final int STANDARD_TIMEOUT = 20;
    private static final int ILLEGAL_NEGATIVE_TIMEOUT = -20;

    // One-liner map may cause incorrect serializing by Gson.toJson(). This seems a known issue mentioned here: https://github.com/google/gson/issues/1080.
    private static final Map<String, Object> PAYLOAD_MAP = mapGenerator();
    private static final String PAYLOAD_MAP_JSON_STRING = "{\"boolean\":true,\"string\":\"STRING\",\"double\":1.0E10,\"map\":{\"innerKey\":\"innerVal\"},\"int\":123,\"long\":12345678901}";
    private static final String METHOD_REQUEST_PATTERN_WITH_TIMEOUT = "{\"methodName\":\"%s\",\"responseTimeoutInSeconds\":%d,\"connectTimeoutInSeconds\":%d,\"payload\":%s}";
    private static final String METHOD_REQUEST_PATTERN_WITHOUT_TIMEOUT = "{\"methodName\":\"%s\",\"payload\":%s}";
    private static final String METHOD_REQUEST_PATTERN_WITH_RESPONSE_TIMEOUT = "{\"methodName\":\"%s\",\"responseTimeoutInSeconds\":%d,\"payload\":%s}";
    private static final String METHOD_REQUEST_PATTERN_WITH_CONNECT_TIMEOUT = "{\"methodName\":\"%s\",\"connectTimeoutInSeconds\":%d,\"payload\":%s}";
    private static final String METHOD_RESPONSE_PATTERN = "{\"status\":%s,\"payload\":%s}";

    private static final List<TestMethod> VALID_METHOD_REQUESTS = asList(
            createMethodRequestWithTimeout(STANDARD_NAME, STANDARD_TIMEOUT, STANDARD_TIMEOUT, "null", false, null),
            createMethodRequestWithTimeout(STANDARD_NAME, STANDARD_TIMEOUT, STANDARD_TIMEOUT, "null", true, new JsonParser().parse(new Gson().toJson("null"))),
            createMethodRequestWithTimeout(STANDARD_NAME, STANDARD_TIMEOUT, STANDARD_TIMEOUT, "", true, new JsonParser().parse(new Gson().toJson(""))),
            createMethodRequestWithTimeout(STANDARD_NAME, STANDARD_TIMEOUT, STANDARD_TIMEOUT, "10", false, new JsonParser().parse(new Gson().toJson(10))),
            createMethodRequestWithTimeout(STANDARD_NAME, STANDARD_TIMEOUT, STANDARD_TIMEOUT, PAYLOAD_MAP_JSON_STRING, false, new Gson().toJson(PAYLOAD_MAP)),
            createMethodRequestWithOutTimeout(STANDARD_NAME, PAYLOAD_MAP_JSON_STRING, false, new Gson().toJson(PAYLOAD_MAP)),
            createMethodRequestWithResponseTimeout(STANDARD_NAME, STANDARD_TIMEOUT, PAYLOAD_MAP_JSON_STRING, false, new Gson().toJson(PAYLOAD_MAP)),
            createMethodRequestWithConnectTimeout(STANDARD_NAME, STANDARD_TIMEOUT, PAYLOAD_MAP_JSON_STRING, false, new Gson().toJson(PAYLOAD_MAP))
    );

    private static final List<TestMethod> INVALID_METHOD_REQUESTS = asList(
            createMethodRequestWithOutTimeout(null, null, false, null),
            createMethodRequestWithOutTimeout("", null, false, null),
            createMethodRequestWithOutTimeout(BIG_STRING_150CHARS, null, false, null),
            createMethodRequestWithOutTimeout(ILLEGAL_STRING_DOT, null, false, null),
            createMethodRequestWithOutTimeout(ILLEGAL_STRING_SPACE, null, false, null),
            createMethodRequestWithOutTimeout(ILLEGAL_STRING_DOLLAR, null, false, null),
            createMethodRequestWithOutTimeout(ILLEGAL_STRING_INJECTION, null, false, null),
            createMethodRequestWithResponseTimeout(STANDARD_NAME, ILLEGAL_NEGATIVE_TIMEOUT, "", false, ""),
            createMethodRequestWithConnectTimeout(STANDARD_NAME, ILLEGAL_NEGATIVE_TIMEOUT, "", false, "")
    );

    private static final List<TestMethod> VALID_METHOD_RESPONSES = asList(
            createMethodResponse(201, "null", true, new JsonParser().parse(new Gson().toJson("null"))),
            createMethodResponse(201, "", true, new JsonParser().parse(new Gson().toJson(""))),
            createMethodResponse(201, "Hi, this is a payload", true, new JsonParser().parse(new Gson().toJson("Hi, this is a payload"))),
            createMethodResponse(201, "10", false, new JsonParser().parse(new Gson().toJson(10))),
            createMethodResponse(201, "true", false, new JsonParser().parse(new Gson().toJson(true))),
            createMethodResponse(201, "[1.0,2.0,3.0]", false, new JsonParser().parse(new Gson().toJson((asList(1.0, 2.0, 3.0))))),
            createMethodResponse(201, PAYLOAD_MAP_JSON_STRING, false, new Gson().toJson(PAYLOAD_MAP)),
            createMethodResponse(null, PAYLOAD_MAP_JSON_STRING, false, new Gson().toJson(PAYLOAD_MAP))
    );

    private static final List<TestMethod> INVALID_METHOD_RESPONSES = asList(
            createMethodResponse(null),
            createMethodResponse("null"),
            createMethodResponse(""),
            createMethodResponse("{\"key\":}"),
            createMethodResponse("{\"methodName\":\"" + STANDARD_NAME + "\",\"status\":201}"),
            createMethodResponse("{\"methodName\":\"\"}"),
            createMethodResponse("{\"methodName\":}")
    );

	private static class TestMethod
    {

        String name;
        int responseTimeout;
        int connectTimeout;
        Object payload;

        String json;
        String jsonResult;
        Integer status;
    }

    /**
     * Test helper, will throw if one of the parameters (name, responseTimeout, connectTimeout, or payload) do not fits the ones in the `method`.
     *
     * @param methodParser            is the actually method
     * @param expectedName            is the expected name in the actually method
     * @param expectedResponseTimeout is the expected responseTimeout in the actually method.
     * @param expectedConnectTimeout  is the expected connectTimeout in the actually method.
     * @param expectedPayload         is an object with all expected parameters in the actually method.
     * @param expectedOperation       is the expected operation type.
     */
    private static void assertMethod(
            MethodParser methodParser,
            String expectedName,
            int expectedResponseTimeout,
            int expectedConnectTimeout,
            Integer expectedStatus,
            Object expectedPayload,
            String expectedOperation
    )
    {
        assertNotNull(methodParser);

        String actualName = Deencapsulation.getField(methodParser, "name");
        int actualResponseTimeout = Deencapsulation.getField(methodParser, "responseTimeout");
        int actualConnectTimeout = Deencapsulation.getField(methodParser, "connectTimeout");
        Integer actualStatus = Deencapsulation.getField(methodParser, "status");
        Object actualPayload = Deencapsulation.getField(methodParser, "payload");
        Object actualOperation = Deencapsulation.getField(methodParser, "operation");

        assertEquals(actualName, expectedName);
        assertEquals(actualResponseTimeout, expectedResponseTimeout);
        assertEquals(actualConnectTimeout, expectedConnectTimeout);
        assertEquals(actualStatus, expectedStatus);
        assertEquals(actualOperation.toString(), expectedOperation);
        if (expectedPayload instanceof Number)
        {
            assertEquals(((Number) expectedPayload).doubleValue(), ((Number) actualPayload).doubleValue(), 1e-10);
        }
        else if (expectedPayload instanceof List)
        {
            Helpers.assertListEquals((List) expectedPayload, (List) actualPayload);
        }
        else if (expectedPayload instanceof String && actualPayload instanceof JsonObject)
        {
            assertEquals(expectedPayload, String.valueOf(actualPayload));
        }
        else if (actualPayload instanceof Map)
        {
            Helpers.assertMap((Map) expectedPayload, (Map) actualPayload);
        }
        else
        {
            assertEquals(expectedPayload, actualPayload);
        }
    }

    /**
     * Test helper, will throw if one of the parameters (name, responseTimeout, connectTimeout, or payload) do not fits the ones in the `method`.
     *
     * @param methodParser            is the actually method
     * @param expectedName            is the expected name in the actually method
     * @param expectedPayload         is an object with all expected parameters in the actually method.
     * @param expectedOperation       is the expected operation type.
     */
    private static void assertMethod(
        MethodParser methodParser,
        String expectedName,
        Integer expectedStatus,
        Object expectedPayload,
        String expectedOperation
    )
    {
        assertNotNull(methodParser);

        String actualName = Deencapsulation.getField(methodParser, "name");
        Integer actualStatus = Deencapsulation.getField(methodParser, "status");
        Object actualPayload = Deencapsulation.getField(methodParser, "payload");
        Object actualOperation = Deencapsulation.getField(methodParser, "operation");

        assertEquals(actualName, expectedName);
        assertEquals(actualStatus, expectedStatus);
        assertEquals(actualOperation.toString(), expectedOperation);
        if (expectedPayload instanceof Number)
        {
            assertEquals(((Number) expectedPayload).doubleValue(), ((Number) actualPayload).doubleValue(), 1e-10);
        }
        else if (expectedPayload instanceof List)
        {
            Helpers.assertListEquals((List) expectedPayload, (List) actualPayload);
        }
        else if (expectedPayload instanceof String && actualPayload instanceof JsonObject)
        {
            assertEquals(expectedPayload, String.valueOf(actualPayload));
        }
        else if (actualPayload instanceof Map)
        {
            if (expectedPayload instanceof String)
            {
                assertEquals(expectedPayload, new Gson().toJson(actualPayload));
            }else
            {
                Helpers.assertMap((Map) expectedPayload, (Map) actualPayload);
            }
        }
        else
        {
            assertEquals(expectedPayload, actualPayload);
        }
    }

    /* Tests_SRS_METHODPARSER_21_029: [The constructor shall create an instance of the methodParser.] */
    /* Tests_SRS_METHODPARSER_21_030: [The constructor shall initialize all data in the collection as null.] */
    /* Tests_SRS_METHODPARSER_21_022: [The constructor shall initialize the method operation as `none`.] */
    @Test
    public void ConstructorSucceed()
    {
        // Arrange

        // Act
        MethodParser methodParser = new MethodParser();

        // Assert
        assertMethod(methodParser, null, null, null, "none"
        );
    }

    /* Tests_SRS_METHODPARSER_21_001: [The constructor shall create an instance of the methodParser.] */
    /* Tests_SRS_METHODPARSER_21_002: [The constructor shall update the method collection using the provided information.] */
    /* Tests_SRS_METHODPARSER_21_003: [All Strings are case sensitive.] */
    /* Tests_SRS_METHODPARSER_21_023: [The constructor shall initialize the method operation as `invoke`.] */
    @Test
    public void ConstructorMethodSucceed()
    {
        // Arrange

        for (TestMethod testCase : VALID_METHOD_REQUESTS)
        {

            // Act
            MethodParser methodParser = new MethodParser(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);

            // Assert
            assertMethod(methodParser, testCase.name, testCase.responseTimeout, testCase.connectTimeout,
                         null, testCase.payload, "invoke"
            );
        }
    }

    /* Tests_SRS_METHODPARSER_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_METHODPARSER_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_METHODPARSER_21_033: [If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
    @Test
    public void ConstructorMethodFailed()
    {
        // Arrange

        for (TestMethod testCase : INVALID_METHOD_REQUESTS)
        {

            // Act
            try
            {
                new MethodParser(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);
                assert true;
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
            // Assert
        }
    }

    /* Tests_SRS_METHODPARSER_21_020: [The constructor shall create an instance of the methodParser.] */
    /* Tests_SRS_METHODPARSER_21_021: [The constructor shall update the method collection using the provided information.] */
    /* Tests_SRS_METHODPARSER_21_034: [The constructor shall set the method operation as `payload`.] */
    @Test
    public void ConstructorMapPayloadSuccess()
    {
        // Arrange

        for (TestMethod testCase : VALID_METHOD_RESPONSES)
        {
            // Act
            MethodParser methodParser = new MethodParser(testCase.payload);

            // Assert
            assertMethod(methodParser, null, null, testCase.payload, "payload"
            );
            String json = methodParser.toJson();
            Helpers.assertJson(testCase.json, json);
        }
    }

    /* Tests_SRS_METHODPARSER_21_006: [The fromJson shall parse the json and fill the method collection.] */
    /* Tests_SRS_METHODPARSER_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */

    /**
     * Tests_SRS_METHODPARSER_21_010: [If the json contains any payload without `methodName` or `status` identification, the fromJson shall parse only the payload, and set the operation as `payload`]
     * Ex:
     * {
     * "input1": "someInput",
     * "input2": "anotherInput"
     * }
     */
    @Test
    public void fromJsonPayloadSucceed()
    {

        for (TestMethod testCase : VALID_METHOD_RESPONSES)
        {
            // Arrange
            MethodParser methodParser = new MethodParser();

            // Act
            methodParser.fromJson(testCase.json);

            // Assert
            assertMethod(methodParser, null, null, testCase.payload, "payload");
        }
    }

    /* Tests_SRS_METHODPARSER_21_006: [The fromJson shall parse the json and fill the method collection.] */
    /* Tests_SRS_METHODPARSER_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */

    /**
     * Tests_SRS_METHODPARSER_21_011: [If the json contains the `status` identification, the fromJson shall parse both status and payload, and set the operation as `response`.]
     * Ex:
     * {
     * "status": 201,
     * "payload": {"AnyValidPayload" : "" }
     * }
     */
    @Test
    public void fromJsonResponseSucceed()
    {
        for (TestMethod testCase : VALID_METHOD_RESPONSES)
        {
            // Arrange
            MethodParser methodParser = new MethodParser();
            // Act
            methodParser.fromJson(testCase.jsonResult);

            // Assert
            assertMethod(methodParser, null, testCase.status, testCase.payload, "response");
        }
    }

    /* Tests_SRS_METHODPARSER_21_006: [The fromJson shall parse the json and fill the method collection.] */
    /* Tests_SRS_METHODPARSER_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */

    /**
     * Tests_SRS_METHODPARSER_21_009: [If the json contains the `methodName` identification, the fromJson shall parse the full method, and set the operation as `invoke`.]
     * Ex:
     * {
     * "methodName": "reboot",
     * "responseTimeoutInSeconds": 200,
     * "connectTimeoutInSeconds": 5,
     * "payload":
     * {
     * "input1": "someInput",
     * "input2": "anotherInput"
     * }
     * }
     */
    @Test
    public void fromJsonMethodSucceed()
    {
        for (TestMethod testCase : VALID_METHOD_REQUESTS)
        {
            // Arrange
            MethodParser methodParser = new MethodParser();
            // Act
            methodParser.fromJson(testCase.json);

            // Assert
            assertMethod(methodParser, testCase.name, testCase.responseTimeout, testCase.connectTimeout,
                         null, testCase.payload, "invoke"
            );
        }
    }

    /* Tests_SRS_METHODPARSER_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
    @Test
    public void fromJsonFailed()
    {

        for (TestMethod testCase : INVALID_METHOD_RESPONSES)
        {
            // Arrange
            MethodParser methodParser = new MethodParser();
            // Act
            try
            {
                methodParser.fromJson(testCase.json);
                assert true;
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
            // Assert
        }
    }

    /* Tests_SRS_METHODPARSER_21_014: [The toJsonElement shall create a String with the full information in the method collection using json format, by using the toJsonElement.] */
    /* Tests_SRS_METHODPARSER_21_015: [The toJsonElement shall include name as `methodName` in the json.] */
    /* Tests_SRS_METHODPARSER_21_016: [The toJsonElement shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHODPARSER_21_017: [If the responseTimeout is null, the toJsonElement shall not include the `responseTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHODPARSER_21_031: [The toJsonElement shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHODPARSER_21_032: [If the connectTimeout is null, the toJsonElement shall not include the `connectTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHODPARSER_21_018: [The class toJsonElement include payload as `payload` in the json.] */
    /* Tests_SRS_METHODPARSER_21_019: [If the payload is null, the toJsonElement shall include `payload` with value `null`.] */

    /**
     * Tests_SRS_METHODPARSER_21_026: [If the method operation is `invoke`, the toJsonElement shall include the full method information in the json.]
     * Ex:
     * {
     * "methodName": "reboot",
     * "responseTimeoutInSeconds": 200,
     * "connectTimeoutInSeconds": 5,
     * "payload":
     * {
     * "input1": "someInput",
     * "input2": "anotherInput"
     * }
     * }
     */
    @Test
    public void toJsonElementMethodSucceed()
    {
        for (TestMethod testCase : VALID_METHOD_REQUESTS)
        {
            // Arrange
            MethodParser methodParser = new MethodParser(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);

            // Act
            String json = methodParser.toJsonElement().toString();

            // Assert
            Helpers.assertJson(json, testCase.json);
        }
    }

    /* Tests_SRS_METHODPARSER_21_014: [The toJsonElement shall create a String with the full information in the method collection using json format, by using the toJsonElement.] */
    @Test
    public void toJsonMethodSucceed()
    {
        for (TestMethod testCase : VALID_METHOD_REQUESTS)
        {
            // Arrange
            MethodParser methodParser = new MethodParser(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);

            // Act
            String json = methodParser.toJson();

            // Assert
            Helpers.assertJson(json, testCase.json);
        }
    }

    /* Tests_SRS_METHODPARSER_21_024: [The class toJsonElement include status as `status` in the json.] */
    /* Tests_SRS_METHODPARSER_21_025: [If the status is null, the toJsonElement shall include `status` as `null`.] */

    /**
     * Tests_SRS_METHODPARSER_21_027: [If the method operation is `response`, the toJsonElement shall parse both status and payload.]
     * Ex:
     * {
     * "status": 201,
     * "payload": {"AnyValidPayload" : "" }
     * }
     */
    @Test
    public void toJsonElementResponseSucceed()
    {

        for (TestMethod testCase : VALID_METHOD_RESPONSES)
        {
            // Arrange
            MethodParser methodParser = new MethodParser();
            // Arrange
            methodParser.fromJson(testCase.jsonResult);

            // Act
            String json = methodParser.toJsonElement().toString();

            // Assert
            Helpers.assertJson(json, testCase.jsonResult);
        }
    }

    /**
     * Tests_SRS_METHODPARSER_21_028: [If the method operation is `payload`, the toJsonElement shall parse only the payload.]
     * Ex:
     * {
     * "input1": "someInput",
     * "input2": "anotherInput"
     * }
     */
    @Test
    public void toJsonElementPayloadSucceed()
    {
        for (TestMethod testCase : VALID_METHOD_RESPONSES)
        {
            // Arrange
            MethodParser methodParser = new MethodParser(testCase.payload);

            // Act
            JsonElement jsonElement = methodParser.toJsonElement();
            String json = jsonElement.toString();

            // Assert
            Helpers.assertJson(testCase.json, json);
        }
    }

    /* Tests_SRS_METHODPARSER_21_036: [If the method operation is `none`, the toJsonElement shall throw IllegalArgumentException.] */
    @Test(expected = IllegalArgumentException.class)
    public void toJsonElementFailed()
    {
        // Arrange
        MethodParser methodParser = new MethodParser();

        // Act
        methodParser.toJsonElement();
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static TestMethod createMethodRequestWithTimeout(
            String methodName,
            int responseTimeOut,
            int connectTimeout,
            String value,
            boolean wrapValue,
            Object expected
    )
    {
        TestMethod testMethod = new TestMethod();
        testMethod.name = methodName;
        testMethod.responseTimeout = responseTimeOut;
        testMethod.connectTimeout = connectTimeout;
        testMethod.payload = expected;
        testMethod.json = String.format(
                METHOD_REQUEST_PATTERN_WITH_TIMEOUT,
                methodName,
                responseTimeOut,
                connectTimeout,
                wrapValue ? String.format("\"%s\"", value) : value
        );
        return testMethod;
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static TestMethod createMethodRequestWithOutTimeout(String methodName, String payload, boolean wrapPayloadAsString, Object expectedPayload)
    {
        TestMethod testMethod = new TestMethod();
        testMethod.name = methodName;
        testMethod.payload = expectedPayload;
        testMethod.json = String.format(
                METHOD_REQUEST_PATTERN_WITHOUT_TIMEOUT,
                methodName,
                wrapPayloadAsString ? String.format("\"%s\"", payload) : payload
        );
        return testMethod;
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static TestMethod createMethodRequestWithResponseTimeout(
            String methodName,
            int responseTimeOut,
            String payload,
            boolean wrapPayloadAsString,
            Object expectedPayload
    )
    {
        TestMethod testMethod = new TestMethod();
        testMethod.name = methodName;
        testMethod.responseTimeout = responseTimeOut;
        testMethod.payload = expectedPayload;
        testMethod.json = String.format(
                METHOD_REQUEST_PATTERN_WITH_RESPONSE_TIMEOUT,
                methodName,
                responseTimeOut,
                wrapPayloadAsString ? String.format("\"%s\"", payload) : payload
        );
        return testMethod;
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static TestMethod createMethodRequestWithConnectTimeout(
            String methodName,
            int connectTimeout,
            String payload,
            boolean wrapPayloadAsString,
            Object expectedPayload
    )
    {
        TestMethod testMethod = new TestMethod();
        testMethod.name = methodName;
        testMethod.connectTimeout = connectTimeout;
        testMethod.payload = expectedPayload;
        testMethod.json = String.format(
                METHOD_REQUEST_PATTERN_WITH_CONNECT_TIMEOUT,
                methodName,
                connectTimeout,
                wrapPayloadAsString ? String.format("\"%s\"", payload) : payload
        );
        return testMethod;
    }

    private static TestMethod createMethodResponse(Integer status, String payload, boolean wrapPayloadAsString, Object expectedPayload)
    {
        TestMethod testMethod = new TestMethod();
        testMethod.status = status;
        testMethod.payload = expectedPayload;
        testMethod.json = wrapPayloadAsString ? String.format("\"%s\"", payload) : payload;
        testMethod.jsonResult = String.format(
                METHOD_RESPONSE_PATTERN,
                status,
                testMethod.json
        );
        return testMethod;
    }

    private static TestMethod createMethodResponse(String json)
    {
        TestMethod testMethod = new TestMethod();
        testMethod.json = json;
        return testMethod;
    }

    private static Map mapGenerator()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("string", "STRING");
        map.put("int", 123);
        map.put("double", 1E10);
        map.put("long", 12345678901L);
        map.put("boolean", true);
        Map<String, Object> innerMap = new HashMap<>();
        innerMap.put("innerKey", "innerVal");
        map.put("map", innerMap);

        return map;
    }
}
