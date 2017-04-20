/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.Twin;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class DeviceTwinTest
{
    @Mocked
    IotHubConnectionStringBuilder mockedConnectionStringBuilder;

    @Mocked
    IotHubConnectionString mockedConnectionString;

    @Mocked
    IotHubServiceSasToken mockedSasToken;

    @Mocked
    HttpRequest mockedHttpRequest;

    @Mocked
    HttpResponse mockedHttpResponse;

    @Mocked
    IotHubExceptionManager mockedExceptionManager;

    @Mocked
    Twin mockedTwinObject;

    @Mocked
    DeviceTwinDevice mockedDevice;

    /*
    **Tests_SRS_DEVICETWIN_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**
    **Tests_SRS_DEVICETWIN_25_003: [** The constructor shall create a new DeviceTwin instance and return it **]**
     */
    @Test
    public void constructorCreatesTwin(@Mocked IotHubConnectionStringBuilder mockedConnectionStringBuilder) throws Exception
    {
        //arrange
        final String connectionString = "testString";

        //act
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //assert
        assertNotNull(testTwin);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_001: [** The constructor shall throw IllegalArgumentException if the input string is null or empty **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullCS() throws Exception
    {
        //arrange
        final String connectionString = null;

        //act
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyCS() throws Exception
    {
        //arrange
        final String connectionString = "";

        //act
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnImproperCS() throws Exception
    {
        //arrange
        final String connectionString = "ImproperCSFormat";

        new NonStrictExpectations()
        {
            {
                IotHubConnectionStringBuilder.createConnectionString(connectionString);
                result = new IllegalArgumentException();
            }
        };

        //act
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
    **Tests_SRS_DEVICETWIN_25_006: [** The function shall create a new SAS token **]**
    **Tests_SRS_DEVICETWIN_25_007: [** The function shall create a new HttpRequest with http method as Get **]**
    **Tests_SRS_DEVICETWIN_25_008: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**
     **Tests_SRS_DEVICETWIN_25_009: [** The function shall send the created request and get the response **]**
     **Tests_SRS_DEVICETWIN_25_011: [** The function shall deserialize the payload by calling updateTwin Api on the twin object **]**
     **Tests_SRS_DEVICETWIN_25_012: [** The function shall set tags, desired property map, reported property map on the user device **]**
     */
    @Test
    public void getTwinSucceeds() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                result = mockedTwinObject;
            }
        };

        //act
        testTwin.getTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 5;
                mockedHttpRequest.send();
                times = 1;
                mockedTwinObject.updateTwin(anyString);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                times = 4;
                mockedTwinObject.getTagsMap();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setTags", testMap);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setDesiredProperties", testMap);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setReportedProperties", testMap );
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected =  IllegalArgumentException.class)
    public void getTwinThrowsOnNullDevice() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.getTwin(null);
    }

    @Test (expected =  IllegalArgumentException.class)
    public void getTwinThrowsOnEmptyDeviceID() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "";
            }
        };

        //act
        testTwin.getTwin(mockedDevice);

    }

    @Test (expected = IllegalArgumentException.class)
    public void getTwinThrowsOnNullDeviceID() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = null;
            }
        };

        //act
        testTwin.getTwin(mockedDevice);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_010: [** The function shall verify the response status and throw proper Exception **]**
     */
    @Test (expected = IotHubException.class)
    public void getTwinThrowsVerificationFailure() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                IotHubExceptionManager.httpResponseVerification(mockedHttpResponse);
                result = new IotHubException();
            }
        };

        //act
        testTwin.getTwin(mockedDevice);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_030: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
    **Tests_SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twin object for the device provided by the user**]**
    **Tests_SRS_DEVICETWIN_25_032: [** The function shall create a new SAS token **]**

    **Tests_SRS_DEVICETWIN_25_033: [** The function shall create a new HttpRequest with http method as PUT **]**

    **Tests_SRS_DEVICETWIN_25_034: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

    **Tests_SRS_DEVICETWIN_25_035: [** The function shall send the created request and get the response **]**

    **Tests_SRS_DEVICETWIN_25_036: [** The function shall verify the response status and throw proper Exception **]**

     */
    /*
    @Test
    public void replaceDesiredPropertiesSucceeds() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.replaceDesiredProperties(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinDesired(anyString);
                times = 1;
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 7;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }
*/
    /*
    **Tests_SRS_DEVICETWIN_25_029: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void replaceDesiredPropertiesThrowsIfDeviceIsNull() throws Exception
    { //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.replaceDesiredProperties(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void replaceDesiredPropertiesThrowsIfDeviceIDIsNull() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = null;
            }
        };

        //act
        testTwin.replaceDesiredProperties(mockedDevice);
    }

    @Test (expected = IllegalArgumentException.class)
    public void replaceDesiredPropertiesThrowsIfDeviceIDIsEmpty() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "";
            }
        };

        //act
        testTwin.replaceDesiredProperties(mockedDevice);
    }

    /*
     **Tests_SRS_DEVICETWIN_25_045: [** If resetDesiredProperty call returns null or empty string then this method shall throw IOException**]**
     */
    @Test (expected = IOException.class)
    public void replaceDesiredPropertiesThrowsIfJsonIsNull() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                result = mockedTwinObject;
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                result = null;

            }
        };

        //act
        testTwin.replaceDesiredProperties(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinDesired(anyString);
                times = 1;
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                times = 1;

            }
        };
    }

    @Test (expected = IOException.class)
    public void replaceDesiredPropertiesThrowsIfJsonIsEmpty() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                result = mockedTwinObject;
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                result = "";

            }
        };

        //act
        testTwin.replaceDesiredProperties(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinDesired(anyString);
                times = 1;
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                times = 1;

            }
        };
    }
/*
    @Test (expected = IotHubException.class)
    public void replaceDesiredPropertiesThrowsVerificationThrows() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                result = "SomeJsonString";
                IotHubExceptionManager.httpResponseVerification(mockedHttpResponse);
                result = new IotHubException();
            }
        };

        //act
        testTwin.replaceDesiredProperties(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinDesired(anyString);
                times = 1;
                mockedTwinObject.resetDesiredProperty((Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 7;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }
*/
    /*
    **Tests_SRS_DEVICETWIN_25_038: [** The function shall build the URL for this operation by calling getUrlTwinTags **]**
    **Tests_SRS_DEVICETWIN_25_039: [** The function shall serialize the tags map by calling resetTags Api on the twin object for the device provided by the user**]**
    **Tests_SRS_DEVICETWIN_25_040: [** The function shall create a new SAS token **]**

    **Tests_SRS_DEVICETWIN_25_041: [** The function shall create a new HttpRequest with http method as PUT **]**

    **Tests_SRS_DEVICETWIN_25_042: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

    **Tests_SRS_DEVICETWIN_25_043: [** The function shall send the created request and get the response **]**

    **Tests_SRS_DEVICETWIN_25_044: [** The function shall verify the response status and throw proper Exception **]**

     */
/*
    @Test
    public void replaceTagsSucceeds() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinObject.resetTags((Map<String, Object>)any);
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.replaceTags(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinTags(anyString);
                times = 1;
                mockedTwinObject.resetTags((Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 7;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }
*/
    /*
    Tests_SRS_DEVICETWIN_25_037: [ The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty ]
     */
    @Test (expected = IllegalArgumentException.class)
    public void replaceTagsThrowsIfDeviceIsNull() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.replaceTags(null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void replaceTagsThrowsIfDeviceIDIsNull() throws Exception
    {        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = null;

            }
        };

        //act
        testTwin.replaceTags(mockedDevice);

    }

    @Test (expected = IllegalArgumentException.class)
    public void replaceTagsThrowsIfDeviceIDIsEmpty() throws Exception
    {
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "";

            }
        };

        //act
        testTwin.replaceTags(mockedDevice);
    }

    /*
    @Test (expected = IotHubException.class)
    public void replaceTagsThrowsVerificationThrows() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinObject.resetTags((Map<String, Object>)any);
                result = "SomeJsonString";
                IotHubExceptionManager.httpResponseVerification(mockedHttpResponse);
                result = new IotHubException();
            }
        };

        //act
        testTwin.replaceTags(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinTags(anyString);
                times = 1;
                mockedTwinObject.resetTags((Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 7;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }
*/
    /*
    **Tests_SRS_DEVICETWIN_25_046: [** If resetTags call returns null or empty string then this method shall throw IOException**]**
     */
    @Test (expected = IOException.class)
    public void replaceTagsThrowsJsonIsNull() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinObject.resetTags((Map<String, Object>)any);
                result = null;
            }
        };

        //act
        testTwin.replaceTags(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinTags(anyString);
                times = 1;
                mockedTwinObject.resetTags((Map<String, Object>)any);
                times = 1;

            }
        };
    }

    @Test (expected = IOException.class)
    public void replaceTagsThrowsJsonIsEmpty() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinObject.resetTags((Map<String, Object>)any);
                result = "";
            }
        };

        //act
        testTwin.replaceTags(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinTags(anyString);
                times = 1;
                mockedTwinObject.resetTags((Map<String, Object>)any);
                times = 1;

            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_030: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
    **Tests_SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twin object for the device provided by the user**]**
    **Tests_SRS_DEVICETWIN_25_016: [** The function shall create a new SAS token **]**

    **Tests_SRS_DEVICETWIN_25_017: [** The function shall create a new HttpRequest with http method as Patch **]**

    **Tests_SRS_DEVICETWIN_25_018: [** The function shall set the following HTTP headers specified in the IotHub DeviceTwin doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

    **Tests_SRS_DEVICETWIN_25_019: [** The function shall send the created request and get the response **]**

    **Tests_SRS_DEVICETWIN_25_020: [** The function shall verify the response status and throw proper Exception **]**

    *Tests_SRS_DEVICETWIN_25_036: [** The function shall verify the response status and throw proper Exception **]**

     */
    @Test
    public void updateTwinSucceeds() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = testMap;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 5;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_013: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfDeviceIsNull() throws Exception
    { //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.updateTwin(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfDeviceIDIsNull() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = null;
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);
    }

    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfDeviceIDIsEmpty() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);
    }

    /*
    **Tests_SRS_DEVICETWIN_25_045: [** The function shall throw IllegalArgumentException if the both desired and tags maps are either empty or null **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfBothDesiredAndTagsIsEmpty() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        //testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = testMap;
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);
    }


    @Test
    public void updateTwinDoesNotThrowsIfOnlyDesiredHasValue() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = null;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 5;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }


    @Test
    public void updateTwinDoesNotThrowsIfOnlyTagsHasValue() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = null;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = testMap;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 5;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_046: [** The function shall throw IOException if updateTwin Api call returned an empty or null json**]**
     */
    @Test (expected = IOException.class)
    public void updateTwinThrowsIfJsonIsNull() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                result = mockedTwinObject;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = null;

            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;

            }
        };
    }

    @Test (expected = IOException.class)
    public void updateTwinThrowsIfJsonIsEmpty() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                result = mockedTwinObject;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = "";

            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedConnectionString.getUrlTwinDesired(anyString);
                times = 1;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;

            }
        };
    }

    @Test (expected = IotHubException.class)
    public void updateTwinThrowsVerificationThrows() throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        Map<String, Object> testMap = new HashMap<>();
        testMap.put("TestKey", "TestValue");
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTwinObject");
                result = mockedTwinObject;
                mockedTwinObject.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = "SomeJsonString";
                IotHubExceptionManager.httpResponseVerification(mockedHttpResponse);
                result = new IotHubException();
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

    }
}
