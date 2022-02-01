// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.twin;

import com.microsoft.azure.sdk.iot.service.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * Unit tests for Device Method
 * 100% methods, 100% lines covered
 */
public class DirectMethodsClientTest
{
    @Mocked
    IotHubConnectionString mockedIotHubConnectionString;

    @Mocked
    IotHubConnectionStringBuilder mockedConnectionStringBuilder;

    @Mocked
    IotHubServiceSasToken mockedIotHubServiceSasToken;

    @Mocked
    HttpRequest mockHttpRequest;

    private static final String STANDARD_HOSTNAME = "testHostName.azure.net";
    private static final String STANDARD_SHAREDACCESSKEYNAME = "testKeyName";
    private static final String STANDARD_SHAREDACCESSKEY = "1234567890ABCDEFGHIJKLMNOPQRESTUVWXYZ=";
    private static final String STANDARD_CONNECTIONSTRING =
            "HostName=" + STANDARD_HOSTNAME +
            ";SharedAccessKeyName=" + STANDARD_SHAREDACCESSKEYNAME +
            ";SharedAccessKey=" + STANDARD_SHAREDACCESSKEY;
    private static final String STANDARD_DEVICEID = "validDeviceId";
    private static final String STANDARD_MODULEID = "validModuleId";
    private static final String STANDARD_METHODNAME = "validMethodName";
    private static final int STANDARD_TIMEOUT_SECONDS = 20;
    private static final Map<String, Object> STANDARD_PAYLOAD_MAP = new HashMap<String, Object>()
    {{
        put("myPar1", "myVal1");
        put("myPar2", "myVal2");
    }};
    private static final String STANDARD_PAYLOAD_STR = "testPayload";
    private static final String STANDARD_JSON =
            "{" +
                "\"methodName\":\"" + STANDARD_METHODNAME + "\"," +
                "\"responseTimeoutInSeconds\":" + STANDARD_TIMEOUT_SECONDS + "," +
                "\"connectTimeoutInSeconds\":" + STANDARD_TIMEOUT_SECONDS + "," +
                "\"payload\":" +
                "{" +
                    "\"myPar1\": \"myVal1\"," +
                    "\"myPar2\": \"myVal2\"" +
                "}" +
            "}";
    private static final String STANDARD_URL = "https://" + STANDARD_HOSTNAME + "/twins/" + STANDARD_DEVICEID + "/methods/" ;
    private static class TestMethod
    {
        String deviceId;
        String moduleId;
        String methodName;
        int responseTimeoutInSeconds;
        int connectTimeoutInSeconds;
        Object payload;
    }

    private static final TestMethod[] illegalParameter = new TestMethod[]
            {
                    new TestMethod()
                    {{
                        deviceId = null;
                        methodName = STANDARD_METHODNAME;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = "";
                        methodName = STANDARD_METHODNAME;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = STANDARD_DEVICEID;
                        methodName = null;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = STANDARD_DEVICEID;
                        methodName = "";
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
            };

    private static final TestMethod[] illegalParameterModule = new TestMethod[]
            {
                    new TestMethod()
                    {{
                        deviceId = null;
                        moduleId = STANDARD_MODULEID;
                        methodName = STANDARD_METHODNAME;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = "";
                        moduleId = STANDARD_MODULEID;
                        methodName = STANDARD_METHODNAME;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = STANDARD_DEVICEID;
                        moduleId = null;
                        methodName = STANDARD_METHODNAME;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = STANDARD_DEVICEID;
                        moduleId = "";
                        methodName = STANDARD_METHODNAME;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = STANDARD_DEVICEID;
                        moduleId = STANDARD_MODULEID;
                        methodName = null;
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
                    new TestMethod()
                    {{
                        deviceId = STANDARD_DEVICEID;
                        moduleId = STANDARD_MODULEID;
                        methodName = "";
                        responseTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        connectTimeoutInSeconds = STANDARD_TIMEOUT_SECONDS;
                        payload = STANDARD_PAYLOAD_MAP;
                    }},
            };


    @Test
    public void testOptionsDefaults()
    {
        DirectMethodsClientOptions options = DirectMethodsClientOptions.builder().build();
        assertEquals((int) Deencapsulation.getField(DirectMethodsClientOptions.class, "DEFAULT_HTTP_READ_TIMEOUT_MS"), options.getHttpReadTimeout());
        assertEquals((int) Deencapsulation.getField(DirectMethodsClientOptions.class, "DEFAULT_HTTP_CONNECT_TIMEOUT_MS"), options.getHttpConnectTimeout());
    }

    /* Tests_SRS_DEVICEMETHOD_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
    /* Tests_SRS_DEVICEMETHOD_21_003: [The constructor shall create a new DirectMethodsClient instance and return it.] */
    @Test
    public void constructorCreateMethodSucceed() throws Exception
    {
        //arrange

        //act
        constructorExpectations();
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);

        //assert
        assertNotNull(testMethod);
    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowOnNullCSFailed() throws Exception
    {
        //arrange
        final String connectionString = null;

        //act
        DirectMethodsClient testMethod = new DirectMethodsClient(connectionString);

    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowOnEmptyCSFailed() throws Exception
    {
        //arrange
        final String connectionString = "";

        //act
        DirectMethodsClient testMethod = new DirectMethodsClient(connectionString);

    }

    /* Tests_SRS_DEVICEMETHOD_21_004: [The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.] */
    /* Tests_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
    @Test
    public void invokeIllegalParametersFailed()
            throws Exception
    {
        //arrange
        constructorExpectations();
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);

        //act
        for (TestMethod testCase: illegalParameter)
        {
            try
            {
                DirectMethodRequestOptions options =
                    DirectMethodRequestOptions.builder()
                        .payload(testCase.payload)
                        .methodConnectTimeout(testCase.connectTimeoutInSeconds)
                        .methodResponseTimeout(testCase.responseTimeoutInSeconds)
                        .build();

                testMethod.invoke(testCase.deviceId, testCase.methodName, options);
                assertTrue(
                        "Negative case> DeviceId=" + testCase.deviceId +
                        " MethodName=" + testCase.methodName +
                        " responseTimeoutInSeconds=" + testCase.responseTimeoutInSeconds +
                        " connectTimeoutInSeconds=" + testCase.connectTimeoutInSeconds +
                        " payload=" + testCase.payload, true);
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
        }
    }

    /* Tests_SRS_DEVICEMETHOD_28_001: [The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.] */
    /* Tests_SRS_DEVICEMETHOD_28_002: [The invoke shall throw IllegalArgumentException if the provided moduleId is null or empty.] */
    /* Tests_SRS_DEVICEMETHOD_28_003: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
    @Test
    public void invokeModuleIllegalParametersFailed()
            throws Exception
    {
        //arrange
        constructorExpectations();
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);

        //act
        for (TestMethod testCase: illegalParameterModule)
        {
            try
            {
                DirectMethodRequestOptions options =
                    DirectMethodRequestOptions.builder()
                        .payload(testCase.payload)
                        .methodConnectTimeout(testCase.connectTimeoutInSeconds)
                        .methodResponseTimeout(testCase.responseTimeoutInSeconds)
                        .build();

                testMethod.invoke(testCase.deviceId, testCase.moduleId, testCase.methodName, options);
                assertTrue(
                        "Negative case> DeviceId=" + testCase.deviceId +
                                " ModuleName=" + testCase.moduleId +
                                " MethodName=" + testCase.methodName +
                                " responseTimeoutInSeconds=" + testCase.responseTimeoutInSeconds +
                                " connectTimeoutInSeconds=" + testCase.connectTimeoutInSeconds +
                                " payload=" + testCase.payload, true);
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
        }
    }

    /* Codes_SRS_DEVICEMETHOD_28_004: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/modules/{module id}/methods/` by calling getUrlModuleMethod.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeModuleThrowOnGetUrlMethodFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = STANDARD_JSON;
                IotHubConnectionString.getUrlModuleMethod(anyString, STANDARD_DEVICEID, STANDARD_MODULEID);
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_MODULEID, STANDARD_METHODNAME);
    }

    /* Tests_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
    /* Tests_SRS_DEVICEMETHOD_21_006: [The invoke shall throw IllegalArgumentException if the provided responseTimeoutInSeconds is negative.] */
    /* Tests_SRS_DEVICEMETHOD_21_007: [The invoke shall throw IllegalArgumentException if the provided connectTimeoutInSeconds is negative.] */
    /* Tests_SRS_DEVICEMETHOD_21_014: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnCreateMethodFailed() throws Exception
    {
        //arrange
        new MockUp<MethodParser>()
        {
            @Mock void $init(String name, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) throws IllegalArgumentException
            {
                throw new IllegalArgumentException();
            }
        };

        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME);

    }

    /* Tests_SRS_DEVICEMETHOD_21_011: [The invoke shall add a HTTP body with Json created by the `serializer.MethodParser`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnToJsonFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME);
    }

    /* Tests_SRS_DEVICEMETHOD_21_012: [If `MethodParser` return a null Json, the invoke shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnNullJsonFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = null;
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME);
    }

    /* Tests_SRS_DEVICEMETHOD_21_008: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/` by calling getUrlMethod.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnGetUrlMethodFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = STANDARD_JSON;
                IotHubConnectionString.getUrlMethod(anyString, STANDARD_DEVICEID);
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME);
    }

    /* Tests_SRS_DEVICEMETHOD_21_013: [The invoke shall deserialize the payload using the `serializer.MethodParser`.] */
    @SuppressWarnings("EmptyMethod")
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnCreateMethodResponseFailed(
            @Mocked final IotHubServiceSasToken iotHubServiceSasToken)
            throws Exception
    {
        //arrange
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                IotHubConnectionString.getUrlMethod(anyString, STANDARD_DEVICEID);
                result = STANDARD_URL;
            }
        };
        new MockUp<MethodParser>()
        {
            @Mock void $init(String name, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) throws IllegalArgumentException
            {
            }

            @Mock void $init()
            {
            }

            @Mock String toJson()
            {
                return STANDARD_JSON;
            }

            @Mock void fromJson(String json)
            {
                throw new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME);
    }

    private void constructorExpectations()
    {
        new Expectations()
        {
            {
                IotHubConnectionString.createIotHubConnectionString(STANDARD_CONNECTIONSTRING);
                result = mockedIotHubConnectionString;
                mockedIotHubConnectionString.getHostName();
                result = "someHostName";
            }
        };
    }

    /* Tests_SRS_DEVICEMETHOD_21_015: [If the HttpStatus represents success, the invoke shall return the status and payload using the `MethodResult` class.] */
    @Test
    public void invokeSucceed(
            @Mocked final MethodParser methodParser,
            @Mocked final IotHubServiceSasToken iotHubServiceSasToken)
            throws Exception
    {
        //arrange
        constructorExpectations();
        DirectMethodsClient testMethod = new DirectMethodsClient(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                IotHubConnectionString.getUrlMethod(anyString, STANDARD_DEVICEID);
                result = STANDARD_URL;
                methodParser.toJson();
                result = STANDARD_JSON;
                methodParser.getPayload();
                result = STANDARD_PAYLOAD_STR;
                methodParser.getStatus();
                result = 123;
            }
        };

        //act
        MethodResult result = testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME);

        //assert
        assertThat(result.getStatus(), is(123));
        assertThat(result.getPayload().toString(), is(STANDARD_PAYLOAD_STR));
        new Verifications()
        {
            {
                methodParser.toJson();
                times = 1;
                IotHubConnectionString.getUrlMethod(anyString, STANDARD_DEVICEID);
                times = 1;
                methodParser.getPayload();
                times = 1;
                methodParser.getStatus();
                times = 1;
            }
        };
    }
}
