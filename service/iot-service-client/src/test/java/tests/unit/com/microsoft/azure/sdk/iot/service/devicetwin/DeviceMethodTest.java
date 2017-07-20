// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.service.devicetwin.Job;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Device Method
 * 100% methods, 100% lines covered
 */
public class DeviceMethodTest
{
    @Mocked
    IotHubConnectionString mockedIotHubConnectionString;

    @Mocked
    IotHubConnectionStringBuilder mockedConnectionStringBuilder;

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


    /* Tests_SRS_DEVICEMETHOD_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
    /* Tests_SRS_DEVICEMETHOD_21_003: [The constructor shall create a new DeviceMethod instance and return it.] */
    @Test
    public void constructorCreateMethodSucceed() throws Exception
    {
        //arrange

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

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
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(connectionString);

    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowOnEmptyCSFailed() throws Exception
    {
        //arrange
        final String connectionString = "";

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(connectionString);

    }

    /* Tests_SRS_DEVICEMETHOD_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowOnImproperCSFailed() throws Exception
    {
        //arrange
        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.createConnectionString(STANDARD_CONNECTIONSTRING);
                result = new IllegalArgumentException();
            }
        };

        //act
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
    }


    /* Tests_SRS_DEVICEMETHOD_21_004: [The invoke shall throw IllegalArgumentException if the provided deviceId is null or empty.] */
    /* Tests_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
    @Test
    public void invokeIllegalParametersFailed()
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

        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);

    }

    /* Tests_SRS_DEVICEMETHOD_21_011: [The invoke shall add a HTTP body with Json created by the `serializer.MethodParser`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnToJsonFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_012: [If `MethodParser` return a null Json, the invoke shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnNullJsonFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = null;
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_008: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/` by calling getUrlMethod.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnGetUrlMethodFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = STANDARD_JSON;
                mockedIotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_009: [The invoke shall send the created request and get the response using the HttpRequester.] */
    /* Tests_SRS_DEVICEMETHOD_21_010: [The invoke shall create a new HttpRequest with http method as `POST`.] */
    @Test (expected = IotHubException.class)
    public void invokeThrowOnHttpRequesterFailed(
            @Mocked final MethodParser methodParser)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                methodParser.toJson();
                result = STANDARD_JSON;
                mockedIotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                result = STANDARD_URL;
            }
        };
        new MockUp<DeviceOperations>()
        {
            @Mock HttpResponse request(
                    IotHubConnectionString mockedIotHubConnectionString,
                    URL url,
                    HttpMethod method,
                    byte[] payload,
                    String requestId,
                    long timeoutInMs)
                    throws IOException, IotHubException, IllegalArgumentException
            {
                throw new IotHubException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_013: [The invoke shall deserialize the payload using the `serializer.MethodParser`.] */
    @Test (expected = IllegalArgumentException.class)
    public void invokeThrowOnCreateMethodResponseFailed(
            @Mocked final DeviceOperations request,
            @Mocked final IotHubServiceSasToken iotHubServiceSasToken)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
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
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP);
    }

    /* Tests_SRS_DEVICEMETHOD_21_015: [If the HttpStatus represents success, the invoke shall return the status and payload using the `MethodResult` class.] */
    @Test
    public void invokeSucceed(
            @Mocked final MethodParser methodParser,
            @Mocked final DeviceOperations request,
            @Mocked final IotHubServiceSasToken iotHubServiceSasToken)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
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
        MethodResult result = testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, null, null, STANDARD_PAYLOAD_MAP);

        //assert
        assertThat(result.getStatus(), is(123));
        assertThat(result.getPayload().toString(), is(STANDARD_PAYLOAD_STR));
        new Verifications()
        {
            {
                methodParser.toJson();
                times = 1;
                mockedIotHubConnectionString.getUrlMethod(STANDARD_DEVICEID);
                times = 1;
                methodParser.getPayload();
                times = 1;
                methodParser.getStatus();
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICEMETHOD_21_016: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnMethodNameNull() throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = null;
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.scheduleDeviceMethod(queryCondition, methodName, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_DEVICEMETHOD_21_016: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnEmptyMethodName() throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final String methodName = "";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.scheduleDeviceMethod(queryCondition, methodName, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_DEVICEMETHOD_21_017: [If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnStartTimeUtcNull() throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final Date now = null;
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.scheduleDeviceMethod(queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void scheduleDeviceMethodThrowOnInvalidMaxExecutionTimeInSeconds() throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = -100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        //act
        testMethod.scheduleDeviceMethod(queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_DEVICEMETHOD_21_019: [The scheduleDeviceMethod shall create a new instance of the Job class.] */
    /* Tests_SRS_DEVICEMETHOD_21_023: [The scheduleDeviceMethod shall return the created instance of the Job class.] */
    @Test
    public void scheduleDeviceMethodCreateJobSucceed(@Mocked Job mockedJob) throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.toString();
                result = STANDARD_CONNECTIONSTRING;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, STANDARD_CONNECTIONSTRING);
                result = mockedJob;
                times = 1;
            }
        };

        //act
        Job job = testMethod.scheduleDeviceMethod(queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);

        //assert
        assertNotNull(job);
    }

    /* Tests_SRS_DEVICEMETHOD_21_020: [If the scheduleDeviceMethod failed to create a new instance of the Job class, it shall throws IOException. Threw by the Jobs constructor.] */
    @Test (expected = IOException.class)
    public void scheduleDeviceMethodCreateJobThrow(@Mocked Job mockedJob) throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.toString();
                result = STANDARD_CONNECTIONSTRING;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, STANDARD_CONNECTIONSTRING);
                result = new IOException();
                times = 1;
            }
        };

        //act
        testMethod.scheduleDeviceMethod(queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
    }

    /* Tests_SRS_DEVICEMETHOD_21_021: [The scheduleDeviceMethod shall invoke the scheduleDeviceMethod in the Job class with the received parameters.] */
    @Test
    public void scheduleDeviceMethodScheduleDeviceMethodSucceed(@Mocked Job mockedJob) throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.toString();
                result = STANDARD_CONNECTIONSTRING;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, STANDARD_CONNECTIONSTRING);
                result = mockedJob;
            }
        };

        //act
        Job job = testMethod.scheduleDeviceMethod(queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedJob, "scheduleDeviceMethod", queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
                times = 1;
            }
        };
    }

    /* Tests_SRS_DEVICEMETHOD_21_022: [If scheduleDeviceMethod failed, the scheduleDeviceMethod shall throws IotHubException. Threw by the scheduleUpdateTwin.] */
    @Test (expected = IotHubException.class)
    public void scheduleDeviceMethodScheduleDeviceMethodThrow(@Mocked Job mockedJob) throws IOException, IotHubException
    {
        //arrange
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations()
        {
            {
                mockedIotHubConnectionString.toString();
                result = STANDARD_CONNECTIONSTRING;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, STANDARD_CONNECTIONSTRING);
                result = mockedJob;
                Deencapsulation.invoke(mockedJob, "scheduleDeviceMethod", queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
                result = new IotHubException();
                times = 1;
            }
        };

        //act
        testMethod.scheduleDeviceMethod(queryCondition, STANDARD_METHODNAME, STANDARD_TIMEOUT_SECONDS, STANDARD_TIMEOUT_SECONDS, STANDARD_PAYLOAD_MAP, now, maxExecutionTimeInSeconds);
    }

}
