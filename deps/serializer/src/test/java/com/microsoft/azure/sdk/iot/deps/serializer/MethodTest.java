// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import mockit.Deencapsulation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.*;

/**
 * Unit tests for Method serializer
 */
public class MethodTest
{
    /**
     * Test helper, will throw if one of the parameters (name, responseTimeout, connectTimeout, or payload) do not fits the ones in the `method`.
     *
     * @param method is the actually method
     * @param expectedName is the expected name in the actually method
     * @param expectedResponseTimeout is the expected responseTimeout in the actually method.
     * @param expectedConnectTimeout is the expected connectTimeout in the actually method.
     * @param expectedPayload is an object with all expected parameters in the actually method.
     * @param expectedOperation is the expected operation type.
     */
    private static void assertMethod(
            Method method,
            String expectedName,
            Long expectedResponseTimeout,
            Long expectedConnectTimeout,
            Integer expectedStatus,
            Object expectedPayload,
            Method.Operation expectedOperation)
    {
        assertNotNull(method);

        String actualName = Deencapsulation.getField(method, "name");
        Long actualResponseTimeout = Deencapsulation.getField(method, "responseTimeout");
        Long actualConnectTimeout = Deencapsulation.getField(method, "connectTimeout");
        Integer actualStatus = Deencapsulation.getField(method, "status");
        Object actualPayload = Deencapsulation.getField(method, "payload");
        Method.Operation actualOperation = Deencapsulation.getField(method, "operation");

        assertEquals(actualName, expectedName);
        assertEquals(actualResponseTimeout, expectedResponseTimeout);
        assertEquals(actualConnectTimeout, expectedConnectTimeout);
        assertEquals(actualStatus, expectedStatus);
        assertEquals(actualOperation, expectedOperation);
        if((expectedPayload instanceof Integer) && (actualPayload instanceof Double))
        {
            assertEquals(actualPayload, (double)(int)expectedPayload);
        }
        else if(actualPayload instanceof ArrayList)
        {
            assertEquals((ArrayList<Double>)actualPayload, Arrays.asList((Double[]) expectedPayload));
        }
        else
        {
            assertEquals(actualPayload, expectedPayload);
        }
    }

    private static final String BIG_STRING_150CHARS =
            "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789" +
                    "01234567890123456789012345678901234567890123456789";
    private static final String ILLEGAL_STRING_DOT = "illegal.key";
    private static final String ILLEGAL_STRING_SPACE = "illegal key";
    private static final String ILLEGAL_STRING_DOLLAR = "illegal$key";
    private static final String ILLEGAL_STRING_INJECTION = "illegal\",key";
    private static final String STANDARD_NAME = "validName";
    private static final Long STANDARD_TIMEOUT = TimeUnit.SECONDS.toSeconds(20);
    private static final Long ILLEGAL_NEGATIVE_TIMEOUT = TimeUnit.SECONDS.toSeconds(-20);
    private static final Map<String, Object> STANDARD_PAYLOAD = new HashMap<String, Object>()
    {{
        put("myPar1", "myVal1");
        put("myPar2", "myVal2");
    }};

    private static class TestMethod
    {
        String name;
        Long responseTimeout;
        Long connectTimeout;
        Object payload;

        String json;
        String jsonResult;
        Integer status;
    }


    private static final TestMethod[] successTestMethod = new TestMethod[]
            {
                    new TestMethod()
                    {{
                        json =  "{" +
                                        "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                        "\"responseTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() + "," +
                                        "\"connectTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() + "," +
                                        "\"payload\":null" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = STANDARD_TIMEOUT;
                        connectTimeout = STANDARD_TIMEOUT;
                        payload = null;
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                "\"responseTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() + "," +
                                "\"connectTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() + "," +
                                "\"payload\":\"null\"" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = STANDARD_TIMEOUT;
                        connectTimeout = STANDARD_TIMEOUT;
                        payload = "null";
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                "\"responseTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() + "," +
                                "\"connectTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() + "," +
                                "\"payload\":\"\"" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = STANDARD_TIMEOUT;
                        connectTimeout = STANDARD_TIMEOUT;
                        payload = "";
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                        "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                        "\"payload\":" +
                                        "{" +
                                            "\"myPar1\": \"myVal1\"," +
                                            "\"myPar2\": \"myVal2\"" +
                                        "}" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = null;
                        connectTimeout = null;
                        payload = STANDARD_PAYLOAD;
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                        "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                        "\"payload\":null" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = null;
                        connectTimeout = null;
                        payload = null;
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                        "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                        "\"responseTimeoutInSeconds\":" + STANDARD_TIMEOUT.toString() + "," +
                                        "\"payload\":" +
                                        "{" +
                                            "\"myPar1\": \"myVal1\"," +
                                            "\"myPar2\": \"myVal2\"" +
                                        "}" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = STANDARD_TIMEOUT;
                        connectTimeout = null;
                        payload = STANDARD_PAYLOAD;
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                    "\"methodName\":\"" + STANDARD_NAME + "\"," +
                                    "\"connectTimeoutInSeconds\":" + STANDARD_TIMEOUT.toString() + "," +
                                    "\"payload\":" +
                                    "{" +
                                        "\"myPar1\": \"myVal1\"," +
                                        "\"myPar2\": \"myVal2\"" +
                                    "}" +
                                "}";
                        name = STANDARD_NAME;
                        responseTimeout = null;
                        connectTimeout = STANDARD_TIMEOUT;
                        payload = STANDARD_PAYLOAD;
                    }},
            };

    private static final TestMethod[] failedTestMethod = new TestMethod[]
            {
                    new TestMethod() {{ name = null; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ""; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = BIG_STRING_150CHARS; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_DOT; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_SPACE; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_DOLLAR; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_INJECTION; responseTimeout = STANDARD_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = STANDARD_NAME; responseTimeout = ILLEGAL_NEGATIVE_TIMEOUT; connectTimeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = STANDARD_NAME; responseTimeout = STANDARD_TIMEOUT; connectTimeout = ILLEGAL_NEGATIVE_TIMEOUT; payload = STANDARD_PAYLOAD; }},
            };

    private static final TestMethod[] successTestResult = new TestMethod[]
            {
                    new TestMethod()
                    {{
                        json = "null";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = null;
                    }},
                    new TestMethod()
                    {{
                        json = "\"null\"";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = "null";
                    }},
                    new TestMethod()
                    {{
                        json = "\"\"";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = "";
                    }},
                    new TestMethod()
                    {{
                        json = "\"Hi, this is a payload\"";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = "Hi, this is a payload";
                    }},
                    new TestMethod()
                    {{
                        json = "10";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = 10;
                    }},
                    new TestMethod()
                    {{
                        json = "true";
                        jsonResult = "{\"status\":200,\"payload\":" + json + "}";
                        status = 200;
                        payload = true;
                    }},
                    new TestMethod()
                    {{
                        json = "[1.0, 2.0, 3.0]";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = new Double[]{1.0, 2.0, 3.0};
                    }},
                    new TestMethod()
                    {{
                        json="{\"input1\":\"someInput\",\"input2\":\"anotherInput\"}";
                        jsonResult = "{\"status\":201,\"payload\":" + json + "}";
                        status = 201;
                        payload = new HashMap<String, String>()
                        {{
                            put("input1", "someInput");
                            put("input2", "anotherInput");
                        }};
                    }},
                    new TestMethod()
                    {{
                        json="{\"input1\":100,\"input2\":true}";
                        jsonResult = "{\"status\":401,\"payload\":" + json + "}";
                        status = 401;
                        payload = new HashMap<String, Object>()
                        {{
                            put("input1", 100.0);
                            put("input2", true);
                        }};
                    }},
                    new TestMethod()
                    {{
                        json="{\"input1\":100,\"input2\":true}";
                        jsonResult = "{\"status\":null,\"payload\":" + json + "}";
                        status = null;
                        payload = new HashMap<String, Object>()
                        {{
                            put("input1", 100.0);
                            put("input2", true);
                        }};
                    }},
            };

    private static final TestMethod[] failedTestPayload = new TestMethod[]
            {
                    new TestMethod() {{ json = null; }},
                    new TestMethod() {{ json = ""; }},
                    new TestMethod() {{ json = "{\"key\":}"; }},
                    new TestMethod() {{ json = "{\"methodName\":\"" + STANDARD_NAME + "\",\"status\":201}"; }},
                    new TestMethod() {{ json = "{\"methodName\":\"\"}"; }},
                    new TestMethod() {{ json = "{\"methodName\":}"; }},
            };


    /* Tests_SRS_METHOD_21_029: [The constructor shall create an instance of the method.] */
    /* Tests_SRS_METHOD_21_030: [The constructor shall initialize all data in the collection as null.] */
    /* Tests_SRS_METHOD_21_022: [The constructor shall initialize the method operation as `none`.] */
    @Test
    public void Constructor_succeed()
    {
        // Arrange

        // Act
        Method method = new Method();

        // Assert
        assertMethod(method, null, null, null, null, null, Method.Operation.none);
    }

    /* Tests_SRS_METHOD_21_001: [The constructor shall create an instance of the method.] */
    /* Tests_SRS_METHOD_21_002: [The constructor shall update the method collection using the provided information.] */
    /* Tests_SRS_METHOD_21_003: [All Strings are case sensitive.] */
    /* Tests_SRS_METHOD_21_023: [The constructor shall initialize the method operation as `invoke`.] */
    @Test
    public void Constructor_Method_succeed()
    {
        // Arrange

        for (TestMethod testCase:successTestMethod)
        {

            // Act
            Method method = new Method(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);

            // Assert
            assertMethod(method, testCase.name, testCase.responseTimeout, testCase.connectTimeout, null, testCase.payload,Method.Operation.invoke);
        }
    }

    /* Tests_SRS_METHOD_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_METHOD_21_005: [If the responseTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_METHOD_21_033: [If the connectTimeout is a negative number, the constructor shall throw IllegalArgumentException.] */
    @Test
    public void Constructor_Method_failed()
    {
        // Arrange

        for (TestMethod testCase:failedTestMethod)
        {

            // Act
            try
            {
                Method method = new Method(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);
                assert true;
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
            // Assert
        }
    }

    /* Tests_SRS_METHOD_21_020: [The constructor shall create an instance of the method.] */
    /* Tests_SRS_METHOD_21_021: [The constructor shall update the method collection using the provided information.] */
    /* Tests_SRS_METHOD_21_034: [The constructor shall set the method operation as `payload`.] */
    @Test
    public void Constructor_map_Payload_success()
    {
        // Arrange

        for (TestMethod testCase:successTestResult)
        {
            // Act
            Method method = new Method(testCase.payload);

            // Assert
            assertMethod(method, null, null, null, null, testCase.payload, Method.Operation.payload);
            String json = method.toJson();
            Helpers.assertJson(testCase.json, json);
        }
    }

    /* Tests_SRS_METHOD_21_006: [The fromJson shall parse the json and fill the method collection.] */
    /* Tests_SRS_METHOD_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */
    /**
     * Tests_SRS_METHOD_21_010: [If the json contains any payload without `methodName` or `status` identification, the fromJson shall parse only the payload, and set the operation as `payload`]
     *  Ex:
     *  {
     *      "input1": "someInput",
     *      "input2": "anotherInput"
     *  }
     */
    @Test
    public void fromJson_payload_succeed()
    {

        for (TestMethod testCase:successTestResult)
        {
            // Arrange
            Method method = new Method();

            // Act
            method.fromJson(testCase.json);

            // Assert
            assertMethod(method, null, null, null, null, testCase.payload, Method.Operation.payload);
        }
    }

    /* Tests_SRS_METHOD_21_006: [The fromJson shall parse the json and fill the method collection.] */
    /* Tests_SRS_METHOD_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */
    /**
     * Tests_SRS_METHOD_21_011: [If the json contains the `status` identification, the fromJson shall parse both status and payload, and set the operation as `response`.]
     *  Ex:
     *  {
     *      "status": 201,
     *      "payload": {"AnyValidPayload" : "" }
     *  }
     */
    @Test
    public void fromJson_response_succeed()
    {
        // Arrange
        Method method = new Method();

        for (TestMethod testCase:successTestResult)
        {
            // Act
            method.fromJson(testCase.jsonResult);

            // Assert
            assertMethod(method, null, null, null, testCase.status, testCase.payload, Method.Operation.response);
        }
    }

    /* Tests_SRS_METHOD_21_006: [The fromJson shall parse the json and fill the method collection.] */
    /* Tests_SRS_METHOD_21_007: [The json can contain values `null`, `"null"`, and `""`, which represents null, the string null, and empty string respectively.] */
    /**
     * Tests_SRS_METHOD_21_009: [If the json contains the `methodName` identification, the fromJson shall parse the full method, and set the operation as `invoke`.]
     *  Ex:
     *  {
     *      "methodName": "reboot",
     *      "responseTimeoutInSeconds": 200,
     *      "connectTimeoutInSeconds": 5,
     *      "payload":
     *      {
     *          "input1": "someInput",
     *          "input2": "anotherInput"
     *      }
     *  }
     */
    @Test
    public void fromJson_method_succeed()
    {
        // Arrange
        Method method = new Method();

        for (TestMethod testCase:successTestMethod)
        {
            // Act
            method.fromJson(testCase.json);

            // Assert
            assertMethod(method, testCase.name, testCase.responseTimeout, testCase.connectTimeout, null, testCase.payload, Method.Operation.invoke);
        }
    }

    /* Tests_SRS_METHOD_21_008: [If the provided json is null, empty, or not valid, the fromJson shall throws IllegalArgumentException.] */
    @Test
    public void fromJson_failed()
    {
        // Arrange
        Method method = new Method();

        for (TestMethod testCase:failedTestPayload)
        {
            // Act
            try
            {
                method.fromJson(testCase.json);
                assert true;
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
            // Assert
        }
    }

    /* Tests_SRS_METHOD_21_012: [The getStatus shall return an Integer with the status in the parsed json.] */
    /* Tests_SRS_METHOD_21_013: [The getPayload shall return an Object with the Payload in the parsed json.] */
    @Test
    public void getResults_succeed()
    {
        // Arrange
        Method method = new Method();
        method.fromJson("{\"status\":201,\"payload\":{\"myPar1\":\"myVal1\",\"myPar2\":\"myVal2\"}}");

        // Act
        // Assert
        assertEquals(method.getStatus(), (Integer) 201);
        assertEquals(method.getPayload(), STANDARD_PAYLOAD);
    }

    /* Tests_SRS_METHOD_21_035: [If the operation is not `response`, the getStatus shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void getResults_failed()
    {
        // Arrange
        Method method = new Method();

        // Act
        method.getStatus();
    }

    /* Tests_SRS_METHOD_21_014: [The toJson shall create a String with the full information in the method collection using json format.] */
    /* Tests_SRS_METHOD_21_015: [The toJson shall include name as `methodName` in the json.] */
    /* Tests_SRS_METHOD_21_016: [The toJson shall include responseTimeout in seconds as `responseTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHOD_21_017: [If the responseTimeout is null, the toJson shall not include the `responseTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHOD_21_031: [The toJson shall include connectTimeout in seconds as `connectTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHOD_21_032: [If the connectTimeout is null, the toJson shall not include the `connectTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHOD_21_018: [The class toJson include payload as `payload` in the json.] */
    /* Tests_SRS_METHOD_21_019: [If the payload is null, the toJson shall include `payload` with value `null`.] */
    /**
     *  Tests_SRS_METHOD_21_026: [If the method operation is `invoke`, the toJson shall include the full method information in the json.]
     *  Ex:
     *  {
     *      "methodName": "reboot",
     *      "responseTimeoutInSeconds": 200,
     *      "connectTimeoutInSeconds": 5,
     *      "payload":
     *      {
     *          "input1": "someInput",
     *          "input2": "anotherInput"
     *      }
     *  }
     */
    @Test
    public void toJson_Method_succeed()
    {
        for (TestMethod testCase:successTestMethod)
        {
            // Arrange
            Method method = new Method(testCase.name, testCase.responseTimeout, testCase.connectTimeout, testCase.payload);

            // Act
            String json = method.toJson();

            // Assert
            Helpers.assertJson(json, testCase.json);
        }
    }

    /* Tests_SRS_METHOD_21_024: [The class toJson include status as `status` in the json.] */
    /* Tests_SRS_METHOD_21_025: [If the status is null, the toJson shall include `status` as `null`.] */
    /**
     * Tests_SRS_METHOD_21_027: [If the method operation is `response`, the toJson shall parse both status and payload.]
     *  Ex:
     *  {
     *      "status": 201,
     *      "payload": {"AnyValidPayload" : "" }
     *  }
     */
    @Test
    public void toJson_response_succeed()
    {
        // Arrange
        Method method = new Method();

        for (TestMethod testCase:successTestResult)
        {
            // Arrange
            method.fromJson(testCase.jsonResult);

            // Act
            String json = method.toJson();

            // Assert
            Helpers.assertJson(json, testCase.jsonResult);
        }
    }

    /**
     * Tests_SRS_METHOD_21_028: [If the method operation is `payload`, the toJson shall parse only the payload.]
     *  Ex:
     *  {
     *      "input1": "someInput",
     *      "input2": "anotherInput"
     *  }
     */
    @Test
    public void toJson_payload_succeed()
    {
        for (TestMethod testCase:successTestResult)
        {
            // Arrange
            Method method = new Method(testCase.payload);

            // Act
            String json = method.toJson();

            // Assert
            Helpers.assertJson(json, testCase.json);
        }
    }

    /* Tests_SRS_METHOD_21_036: [If the method operation is `none`, the toJson shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void toJson_failed()
    {
        // Arrange
        Method method = new Method();

        // Act
        method.toJson();
    }

}
