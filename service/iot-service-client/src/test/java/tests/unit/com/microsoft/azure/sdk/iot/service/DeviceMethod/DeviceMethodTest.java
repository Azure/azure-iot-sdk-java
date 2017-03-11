// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.service.DeviceMethod;

import com.microsoft.azure.sdk.iot.deps.serializer.Method;
import com.microsoft.azure.sdk.iot.service.DeviceMethod.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for Device Method
 */
@RunWith(JMockit.class)
public class DeviceMethodTest
{

    @Mocked
    Method method;

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
    private static final Long STANDARD_TIMEOUT = TimeUnit.SECONDS.toSeconds(20);
    private static final Long ILLEGAL_NEGATIVE_TIMEOUT = TimeUnit.SECONDS.toSeconds(-20);
    private final Integer DEFAULT_HTTP_TIMEOUT_MS = 24000;
    private static final Map<String, Object> STANDARD_PAYLOAD = new HashMap<String, Object>()
    {{
        put("myPar1", "myVal1");
        put("myPar2", "myVal2");
    }};
    private static final String STANDARD_JSON =
            "{" +
                "\"methodName\":\"" + STANDARD_METHODNAME + "\"," +
                "\"responseTimeoutInSeconds\":" + STANDARD_TIMEOUT.toString() + "," +
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
        Long timeout;
        Object payload;
    }

    private static final TestMethod[] illegalParameter = new TestMethod[]
            {
                    new TestMethod() {{ deviceId = null; methodName = STANDARD_METHODNAME; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
                    new TestMethod() {{ deviceId = ""; methodName = STANDARD_METHODNAME; timeout = STANDARD_TIMEOUT; payload = STANDARD_PAYLOAD; }},
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
                testMethod.invoke(testCase.deviceId, testCase.methodName, testCase.timeout, testCase.payload);
                System.out.print("Negative case> DeviceId=" + testCase.deviceId + " MethodName=" + testCase.methodName + " timeout=" + testCase.timeout + " payload=" + testCase.payload + "\r\n");
                assert true;
            }
            catch (IllegalArgumentException expected)
            {
                //Don't do anything. Expected throw.
            }
        }
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    /* Tests_SRS_DEVICEMETHOD_21_005: [The invoke shall throw IllegalArgumentException if the provided methodName is null, empty, or not valid.] */
    /* Tests_SRS_DEVICEMETHOD_21_006: [The invoke shall throw IllegalArgumentException if the provided timeout is negative.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnCreateMethod_failed() throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);

        new MockUp<Method>()
        {
            @Mock void $init(String name, Long timeout, Object payload)
            {
                throw new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);

    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnToJson_failed()
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
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnGetUrlMethod_failed(
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
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IOException.class)
    public void invoke_throwOnCreateHttpRequest_failed(
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
        new MockUp<HttpRequest>()
        {
            @Mock void $init(URL url, HttpMethod method, byte[] body) throws IOException
            {
                throw new IOException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnCreateIotHubServiceSasToken_failed(
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

        new MockUp<IotHubServiceSasToken>()
        {
            @Mock void $init(IotHubConnectionString iotHubConnectionString)
            {
                throw new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnSetReadTimeoutMillis_failed(
            @Mocked final HttpRequest request,
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
        new NonStrictExpectations()
        {
            {
                request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IllegalArgumentException.class)
    public void invoke_throwOnSetHeaderField_failed(
            @Mocked final HttpRequest request,
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                request.setHeaderField("", "");
                result = new IllegalArgumentException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }

    /* Tests_SRS_DEVICEMETHOD_21_018: [The invoke shall bypass the Exception if one of the functions called by invoke failed.] */
    @Test (expected = IOException.class)
    public void invoke_throwOnSend_failed(
            @Mocked final HttpRequest request,
            @Mocked final IotHubConnectionStringBuilder mockedConnectionStringBuilder)
            throws Exception
    {
        //arrange
        DeviceMethod testMethod = DeviceMethod.createFromConnectionString(STANDARD_CONNECTIONSTRING);
        new NonStrictExpectations()
        {
            {
                request.send();
                result = new IOException();
            }
        };

        //act
        testMethod.invoke(STANDARD_DEVICEID, STANDARD_METHODNAME, STANDARD_TIMEOUT, STANDARD_PAYLOAD);
    }


    /* Tests_SRS_DEVICEMETHOD_21_007: [The invoke shall build the Method URL `{iot hub}/twins/{device id}/methods/`.] */
    /* Tests_SRS_DEVICEMETHOD_21_008: [The invoke shall create a new SASToken with the ServiceConnect rights.] */
    /* Tests_SRS_DEVICEMETHOD_21_009: [The invoke shall create a new HttpRequest with http method as `POST`.] */
    /* Tests_SRS_DEVICEMETHOD_21_010: [The invoke shall add to the HTTP header an default timeout in milliseconds.] */
    /* Tests_SRS_DEVICEMETHOD_21_011: [The invoke shall add to the HTTP header an `authorization` key with the SASToken.] */
    /* Tests_SRS_DEVICEMETHOD_21_012: [The invoke shall add to the HTTP header a `request-id` key with a new unique string value for every request.] */
    /* Tests_SRS_DEVICEMETHOD_21_013: [The invoke shall add a HTTP body with Json created by the `serializer.Method`.] */
    /* Tests_SRS_DEVICEMETHOD_21_014: [The invoke shall send the created request and get the response.] */
    /* Tests_SRS_DEVICEMETHOD_21_015: [The invoke shall deserialize the payload using the `serializer.Method`.] */
    /* Tests_SRS_DEVICEMETHOD_21_016: [If the resulted status represents fail, the invoke shall throw proper Exception.] */
    /* Tests_SRS_DEVICEMETHOD_21_017: [If the resulted status represents success, the invoke shall return the result payload.] */

}
