// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import mockit.Deencapsulation;
import org.junit.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Unit tests for Method serializer
 */
public class MethodTest
{
    /**
     * Test helper, will throw if one of the parameters (name, timeout, or payload) do not fits the ones in the `method`.
     *
     * @param method is the actually method
     * @param expectedName is the expected name in the actually method
     * @param expectedTimeout is the expected timeout in the actually method.
     * @param expectedPayload is a map with all expected parameters in the actually method.
     */
    private static void assertMethod(Method method, String expectedName, Long expectedTimeout, Integer expectedStatus, Object expectedPayload)
    {
        assertNotNull(method);

        String actualName = Deencapsulation.getField(method, "name");
        Long actualTimeout = Deencapsulation.getField(method, "timeout");
        Integer actualStatus = Deencapsulation.getField(method, "status");
        Object actualPayload = Deencapsulation.getField(method, "payload");

        assertEquals(actualName, expectedName);
        assertEquals(actualTimeout, expectedTimeout);
        assertEquals(actualStatus, expectedStatus);
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
        Long timeout;
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
                                        "\"responseTimeoutInSeconds\": " + STANDARD_TIMEOUT.toString() +
                                "}";
                        name = STANDARD_NAME;
                        timeout = STANDARD_TIMEOUT;
                        payload = null;
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
                        timeout = null;
                        payload = STANDARD_PAYLOAD;
                    }},
                    new TestMethod()
                    {{
                        json =  "{" +
                                        "\"methodName\":\"" + STANDARD_NAME + "\"" +
                                "}";
                        name = STANDARD_NAME;
                        timeout = null;
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
                        timeout = STANDARD_TIMEOUT;
                        payload = STANDARD_PAYLOAD;
                    }},
            };
    private static final TestMethod[] failedTestMethod = new TestMethod[]
            {
                    new TestMethod() {{ name = null; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ""; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = BIG_STRING_150CHARS; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_DOT; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_SPACE; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_DOLLAR; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = ILLEGAL_STRING_INJECTION; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ name = STANDARD_NAME; timeout = ILLEGAL_NEGATIVE_TIMEOUT; payload = STANDARD_PAYLOAD; }},
            };
    private static final TestMethod[] successTestResult = new TestMethod[]
            {
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
                        json = "[1, 2, 3]";
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
    @Test
    public void Constructor_succeed()
    {
        // Arrange

        // Act
        Method method = new Method();

        // Assert
        assertMethod(method, null, null, null, null);
    }

    /* Tests_SRS_METHOD_21_001: [The constructor shall create an instance of the method.] */
    /* Tests_SRS_METHOD_21_002: [The constructor shall update the method collection using the provided information.] */
    /* Tests_SRS_METHOD_21_003: [All Strings are case sensitive.] */
    @Test
    public void Constructor_Method_succeed()
    {
        // Arrange

        for (TestMethod testCase:successTestMethod)
        {

            // Act
            Method method = new Method(testCase.name, testCase.timeout, testCase.payload);

            // Assert
            assertMethod(method, testCase.name, testCase.timeout, null, testCase.payload);
        }
    }

    /* Tests_SRS_METHOD_21_004: [If the `name` is null, empty, contains more than 128 chars, or illegal char (`$`, `.`, space), the constructor shall throw IllegalArgumentException.] */
    /* Tests_SRS_METHOD_21_005: [If the timeout is a negative number, the constructor shall throw IllegalArgumentException.] */
    @Test
    public void Constructor_Method_failed()
    {
        // Arrange

        for (TestMethod testCase:failedTestMethod)
        {

            // Act
            try
            {
                Method method = new Method(testCase.name, testCase.timeout, testCase.payload);
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
    @Test
    public void Constructor_map_Payload_success()
    {
        // Arrange

        for (TestMethod testCase:successTestResult)
        {
            // Act
            Method method = new Method(testCase.payload);

            // Assert
            assertMethod(method, null, null, null, testCase.payload);
            String json = method.toJson();
            Helpers.assertJson(testCase.json, json);
        }
    }

    /* Tests_SRS_METHOD_21_006: [The fromJson shall create an instance of the method.] */
    /* Tests_SRS_METHOD_21_007: [The fromJson shall parse the json and fill the status and payload.] */
    /**
     * Tests_SRS_METHOD_21_010: [If the json contains any payload without status identification, the fromJson shall parser only the payload.]
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
            assertMethod(method, null, null, null, testCase.payload);
        }
    }

    /** Tests_SRS_METHOD_21_011: [If the json contains the `status` and `payload` identification, the fromJson shall parser both status and payload.]
     *  Ex:
     *  {
     *      "status": 201,
     *      "payload": {"AnyValidPayload" : "" }
     *  }
     */
    @Test
    public void fromJson_result_succeed()
    {
        // Arrange
        Method method = new Method();

        for (TestMethod testCase:successTestResult)
        {
            // Act
            method.fromJson(testCase.jsonResult);

            // Assert
            assertMethod(method, null, null, testCase.status, testCase.payload);
        }
    }

    /* Tests_SRS_METHOD_21_009: [If the json contains the `methodName` identification, the fromJson shall parser the full method.]
     *  Ex:
     *  {
     *      "methodName": "reboot",
     *      "responseTimeoutInSeconds": 200,
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
            assertMethod(method, testCase.name, testCase.timeout, null, testCase.payload);
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

    /* Tests_SRS_METHOD_21_014: [The toJson shall create a String with the full information in the method collection using json format.] */
    /* Tests_SRS_METHOD_21_015: [The toJson shall include name as `methodName` in the json.] */
    /* Tests_SRS_METHOD_21_016: [The toJson shall include timeout in seconds as `responseTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHOD_21_017: [If the timeout is null, the toJson shall not include the `responseTimeoutInSeconds` in the json.] */
    /* Tests_SRS_METHOD_21_018: [The class toJson include payload as `payload` in the json.] */
    /* Tests_SRS_METHOD_21_019: [If the payload is null, the toJson shall not include `payload` for parameters in the json.] */
    /**
     *  Tests_SRS_METHOD_21_026: [If the method contains a name, the toJson shall include the full method information in the json.]
     *  Ex:
     *  {
     *      "methodName": "reboot",
     *      "responseTimeoutInSeconds": 200,
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
            Method method = new Method(testCase.name, testCase.timeout, testCase.payload);

            // Act
            String json = method.toJson();

            // Assert
            Helpers.assertJson(json, testCase.json);
        }
    }

    /* Tests_SRS_METHOD_21_024: [The class toJson include status as `status` in the json.] */
    /* Tests_SRS_METHOD_21_025: [If the status is null, the toJson shall not include `status` for parameters in the json.] */
    /** Tests_SRS_METHOD_21_027: [If the method contains the status, the toJson shall parser both status and payload.
     *  Ex:
     *  {
     *      "status": 201,
     *      "payload": {"AnyValidPayload" : "" }
     *  }
     */
    @Test
    public void toJson_result_succeed()
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
     * Tests_SRS_METHOD_21_028: [If the method do not contains name or status, the toJson shall parser only the payload.]
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
}
