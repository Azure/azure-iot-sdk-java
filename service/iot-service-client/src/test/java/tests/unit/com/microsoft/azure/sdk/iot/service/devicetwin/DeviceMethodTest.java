// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.Method;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.*;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Unit tests for Device Method
 */
public class DeviceMethodTest
{
    @Mocked
    IotHubConnectionString iotHubConnectionString;

    private static final String STANDARD_HOSTNAME = "testHostName.azure.net";
    private static final String STANDARD_SHAREDACCESSKEYNAME = "testKeyName";
    private static final String STANDARD_SHAREDACCESSKEY = "1234567890ABCDEFGHIJKLMNOPQRESTUVWXYZ=";
    private static final String STANDARD_CONNECTIONSTRING =
            "HostName=" + STANDARD_HOSTNAME +
            ";SharedAccessKeyName=" + STANDARD_SHAREDACCESSKEYNAME +
            ";SharedAccessKey=" + STANDARD_SHAREDACCESSKEY;
    private static final String STANDARD_DEVICEID = "validDeviceId";
    private static final String STANDARD_METHODNAME = "validMethodName";
    private static final Long STANDARD_TIMEOUT_SECONDS = TimeUnit.SECONDS.toSeconds(20);
    private static final Map<String, Object> STANDARD_PAYLOAD_MAP = new HashMap<String, Object>()
    {{
        put("myPar1", "myVal1");
        put("myPar2", "myVal2");
    }};
    private static final String STANDARD_PAYLOAD_STR = "testPayload";
    private static final String STANDARD_JSON =
            "{" +
                "\"methodName\":\"" + STANDARD_METHODNAME + "\"," +
                "\"responseTimeoutInSeconds\":" + STANDARD_TIMEOUT_SECONDS.toString() + "," +
                "\"connectTimeoutInSeconds\":" + STANDARD_TIMEOUT_SECONDS.toString() + "," +
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
        String methodName;
        Long responseTimeoutInSeconds;
        Long connectTimeoutInSeconds;
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
            };


    /* Tests_SRS_DEVICEMETHOD_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
    /* Tests_SRS_DEVICEMETHOD_21_003: [The constructor shall create a new DeviceMethod instance and return it.] */
    @Test
    public void constructor_createMethod_succeed(
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //assert
        assertNotNull(testMethod);
    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_throwOnNullCS_failed() throws Exception
    {
        //arrange
        final String connectionString = null;

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(connectionString);

    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_throwOnEmptyCS_failed() throws Exception
    {
        //arrange
        final String connectionString = "";

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(connectionString);

    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_throwOnImproperCS_failed() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.createConnectionString(STANDARD_CONNECTIONSTRING);
                result = new IllegalArgumentException();
            }
        };

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
    }


    /* Tests_SRS_DEVICEMETHOD_21_004: [The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.] */
    @Test
    public void invoke_IllegalParameters_failed()
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        for (TestMethod testCase: illegalParameter)
        {
            try
            {
                testMethod.invoke(testCase.deviceId, testCase.methodName, testCase.responseTimeoutInSeconds, testCase.connectTimeoutInSeconds, testCase.payload);
                System.out.print(
                        "Negative case> DeviceId=" + testCase.deviceId +
                        " MethodName=" + testCase.methodName +
                        " responseTimeoutInSeconds=" + testCase.responseTimeoutInSeconds +
                        " connectTimeoutInSeconds=" + testCase.connectTimeoutInSeconds +
                        " payload=" + testCase.payload + "\r\n");
                assert true;
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
        }
    }

    /* Tests_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
    /* Tests_SRS_DEVICEMETHOD_21_006: [The invoke shall throw IllegalArgumentException if the provided responseTimeoutInSeconds is negative.] */
    /* Tests_SRS_DEVICEMETHOD_21_007: [The invoke shall throw IllegalArgumentException if the provided connectTimeoutInSeconds is negative.] */
    /* Tests_SRS_DEVICEMETHOD_21_014: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnCreateMethod_failed() throws Exception
    {
        //arrange
        new MockUp<Method>()
        {
            @Mock void $init(String name, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload) throws IllegalArgumentException
            {
                throw new IllegalArgumentException();
            }
        };

        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);

    }

    /* Tests_SRS_DEVICEMETHOD_21_011: [The invoke shall add a HTTP body with Json created by the `serializer.Method`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnToJson_failed(
            @Mocked final Method method)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                method.toJson();
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_012: [If `Method` return a null Json, the invoke shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnNullJson_failed(
            @Mocked final Method method)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                method.toJson();
                result = null;
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_008: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/` by calling getUrlMethod.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnGetUrlMethod_failed(
            @Mocked final Method method,
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                method.toJson();
                result = STANDARD_JSON;
                iotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_009: [The invoke shall send the created request and get the response using the HttpRequester.] */
    /* Tests_SRS_DEVICEMETHOD_21_010: [The invoke shall create a new HttpRequest with http method as `POST`.] */
    @Test (expected = IotHubException.class)
    public void invoke_throwOnHttpRequester_failed(
            @Mocked final Method method,
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                method.toJson();
                result = STANDARD_JSON;
                iotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                result = STANDARD_URL;
            }
        };
        new MockUp<DeviceOperations>()
        {
            @Mock HttpResponse request(
                    IotHubConnectionString iotHubConnectionString,
                    URL url,
                    HttpMethod method,
                    byte[] payload,
                    String requestId)
                    throws IOException, IotHubException, IllegalArgumentException
            {
                throw new IotHubException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_013: [The invoke shall deserialize the payload using the `serializer.Method`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnCreateMethodResponse_failed(
            @Mocked final DeviceOperations request,
            @Mocked final IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                result = STANDARD_URL;
            }
        };
        new MockUp<Method>()
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
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_015: [If the HttpStatus represents success, the invoke shall return the status and payload using the `MethodResult` class.] */
    @Test
    public void invoke_succeed(
            @Mocked final Method method,
            @Mocked final DeviceOperations request,
            @Mocked final IotHubServiceSasToken iotHubServiceSasToken,
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                iotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                result = STANDARD_URL;
                method.toJson();
                result = STANDARD_JSON;
                method.getPayload();
                result = STANDARD_PAYLOAD_STR;
                method.getStatus();
                result = 123;
            }
        };

        //act
        MethodResult result = testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);

        //assert
        assertThat(result.getStatus(), is(123));
        assertThat(result.getPayload().toString(), is(STANDARD_PAYLOAD_STR));
        new Verifications()
        {
            {
                method.toJson();
                times = 1;
                iotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                times = 1;
                method.getPayload();
                times = 1;
                method.getStatus();
                times = 1;
            }
        };
    }

}
