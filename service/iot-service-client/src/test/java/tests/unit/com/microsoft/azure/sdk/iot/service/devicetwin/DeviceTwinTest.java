/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/*
    Unit tests for Device Twin
    Coverage : 90% method, 86% line
 */
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
    TwinParser mockedTwinParser;

    @Mocked
    Query mockedQuery;

    static String VALID_SQL_QUERY = null;

    @Before
    public void setUp() throws IOException
    {
        VALID_SQL_QUERY = SqlQuery.createSqlQuery("tags.Floor, AVG(properties.reported.temperature) AS AvgTemperature",
                                                  SqlQuery.FromType.DEVICES, "tags.building = '43'", null).getQuery();
    }

    private void assetEqualSetAndMap(Set<Pair> pairSet, Map<String, String> map)
    {
        assertEquals(pairSet.size(), map.size());
        for(Pair p : pairSet)
        {
            String val = map.get(p.getKey());
            assertNotNull(val);
            assertEquals(p.getValue(), val);
        }
    }

    /*
    **Tests_SRS_DEVICETWIN_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**
    **Tests_SRS_DEVICETWIN_25_003: [** The constructor shall create a new DeviceTwin instance and return it **]**
     */
    @Test
    public void constructorCreatesTwin(@Mocked IotHubConnectionStringBuilder mockedConnectionStringBuilder,
                                       @Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void getTwinSucceeds(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                result = mockedTwinParser;
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
                mockedTwinParser.updateTwin(anyString);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                times = 4;
                mockedTwinParser.getTagsMap();
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
    public void getTwinThrowsOnNullDevice(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.getTwin(null);
    }

    @Test (expected =  IllegalArgumentException.class)
    public void getTwinThrowsOnEmptyDeviceID(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void getTwinThrowsOnNullDeviceID(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void getTwinThrowsVerificationFailure(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void replaceDesiredPropertiesThrowsIfDeviceIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    { //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.replaceDesiredProperties(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void replaceDesiredPropertiesThrowsIfDeviceIDIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void replaceDesiredPropertiesThrowsIfDeviceIDIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void replaceDesiredPropertiesThrowsIfJsonIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                result = mockedTwinParser;
                mockedTwinParser.resetDesiredProperty((Map<String, Object>)any);
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
                mockedTwinParser.resetDesiredProperty((Map<String, Object>)any);
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void replaceDesiredPropertiesThrowsIfJsonIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                result = mockedTwinParser;
                mockedTwinParser.resetDesiredProperty((Map<String, Object>)any);
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
                mockedTwinParser.resetDesiredProperty((Map<String, Object>)any);
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
    public void replaceTagsThrowsIfDeviceIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.replaceTags(null);

    }

    @Test (expected = IllegalArgumentException.class)
    public void replaceTagsThrowsIfDeviceIDIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void replaceTagsThrowsIfDeviceIDIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void replaceTagsThrowsJsonIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinParser.resetTags((Map<String, Object>)any);
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
                mockedTwinParser.resetTags((Map<String, Object>)any);
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void replaceTagsThrowsJsonIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedTwinParser.resetTags((Map<String, Object>)any);
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
                mockedTwinParser.resetTags((Map<String, Object>)any);
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
    public void updateTwinSucceeds(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
    public void updateTwinThrowsIfDeviceIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    { //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.updateTwin(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfDeviceIDIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void updateTwinThrowsIfDeviceIDIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void updateTwinThrowsIfBothDesiredAndTagsIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
    public void updateTwinDoesNotThrowsIfOnlyDesiredHasValue(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
    public void updateTwinDoesNotThrowsIfOnlyTagsHasValue(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
    public void updateTwinThrowsIfJsonIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                result = mockedTwinParser;
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;
            }
        };
    }

    @Test (expected = IOException.class)
    public void updateTwinThrowsIfJsonIsEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                result = mockedTwinParser;
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
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
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                times = 1;
            }
        };
    }

    @Test (expected = IotHubException.class)
    public void updateTwinThrowsVerificationThrows(@Mocked DeviceTwinDevice mockedDevice) throws Exception
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
                Deencapsulation.invoke(mockedDevice, "getTwinParser");
                result = mockedTwinParser;
                mockedTwinParser.updateTwin((Map<String, Object>)any, null, (Map<String, Object>)any);
                result = "SomeJsonString";
                IotHubExceptionManager.httpResponseVerification(mockedHttpResponse);
                result = new IotHubException();
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);
    }

    //Tests_SRS_DEVICETWIN_25_049: [ The method shall build the URL for this operation by calling getUrlTwinQuery ]
    //Tests_SRS_DEVICETWIN_25_050: [ The method shall create a new Query Object of Type TWIN. ]
    //Tests_SRS_DEVICETWIN_25_051: [ The method shall send a Query Request to IotHub as HTTP Method Post on the query Object by calling sendQueryRequest.]
    //Tests_SRS_DEVICETWIN_25_052: [ If the pagesize if not provided then a default pagesize of 100 is used for the query.]
    @Test
    public void queryTwinSucceeds(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
            }
        };

        //act
        testTwin.queryTwin(VALID_SQL_QUERY);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                times = 1;
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICETWIN_25_047: [ The method shall throw IllegalArgumentException if the query is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnNullQuery(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.queryTwin(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnEmptyQuery(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.queryTwin("");
    }

    //Tests_SRS_DEVICETWIN_25_048: [ The method shall throw IllegalArgumentException if the page size is zero or negative.]
    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnNegativePageSize(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.queryTwin(VALID_SQL_QUERY, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnZeroPageSize(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.queryTwin(VALID_SQL_QUERY, 0);
    }

    @Test (expected = IotHubException.class)
    public void twinQueryThrowsOnNewQueryThrows(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                result = new IotHubException();
            }
        };

        //act
        testTwin.queryTwin(VALID_SQL_QUERY);
    }

    //Tests_SRS_DEVICETWIN_25_055: [ If a queryResponse is available, this method shall return true as is to the user. ]
    //Tests_SRS_DEVICETWIN_25_054: [ The method shall check if a response to query is avaliable by calling hasNext on the query object.]
    @Test
    public void hasNextSucceeds(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        boolean result = testTwin.hasNextDeviceTwin(testQuery);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };

        assertTrue(result);
    }

    //Tests_SRS_DEVICETWIN_25_053: [ The method shall throw IllegalArgumentException if query is null ]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextThrowsOnNullQuery(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        boolean result = testTwin.hasNextDeviceTwin(null);
    }

    @Test (expected = IotHubException.class)
    public void hasNextThrowsIfHasNextOnQueryThrows(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = new IotHubException();
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        boolean result = testTwin.hasNextDeviceTwin(testQuery);
    }

    //Tests_SRS_DEVICETWIN_25_059: [ The method shall parse the next element from the query response as Twin Document using TwinParser and provide the response on DeviceTwinDevice.]
    @Test
    public void nextRetrievesCorrectly() throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        final String expectedString = "testJsonAsNext";
        Map tags = new HashMap();
        tags.put("tagsKey", "tagsValue");
        Map rp = new HashMap();
        rp.put("rpKey", "rpValue");
        Map dp = new HashMap();
        dp.put("dpKey", "dpValue");

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
                Deencapsulation.invoke(mockedQuery, "next");
                result = expectedString;
                mockedTwinParser.getDeviceId();
                result = "testDeviceID";
                mockedTwinParser.getTagsMap();
                result = tags;
                mockedTwinParser.getDesiredPropertyMap();
                result = dp;
                mockedTwinParser.getReportedPropertyMap();
                result = rp;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        DeviceTwinDevice result = testTwin.getNextDeviceTwin(testQuery);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };

        assertNotNull(result.getTags());
        assertNotNull(result.getReportedProperties());
        assertNotNull(result.getDesiredProperties());

        assetEqualSetAndMap(result.getTags(), tags);
        assetEqualSetAndMap(result.getDesiredProperties(), dp);
        assetEqualSetAndMap(result.getReportedProperties(), rp);
    }

    @Test (expected = IllegalArgumentException.class)
    public void nextThrowsOnNullQuery(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        testTwin.getNextDeviceTwin(null);
    }

    @Test (expected = IotHubException.class)
    public void nextThrowsOnQueryNextThrows(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "next");
                result = new IotHubException();
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        testTwin.getNextDeviceTwin(testQuery);
    }

    //Tests_SRS_DEVICETWIN_25_058: [ The method shall check if hasNext returns true and throw NoSuchElementException otherwise ]
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsIfNoNewElements(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "next");
                result = new NoSuchElementException();
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        DeviceTwinDevice result = testTwin.getNextDeviceTwin(testQuery);
    }

    //Tests_SRS_DEVICETWIN_25_060: [ If the next element from the query response is an object other than String, then this method shall throw IOException ]
    @Test (expected = IOException.class)
    public void nextThrowsIfNonStringRetrieved(@Mocked DeviceTwinDevice mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "hasNext");
                result = true;
                Deencapsulation.invoke(mockedQuery, "next");
                result = 5;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        DeviceTwinDevice result = testTwin.getNextDeviceTwin(testQuery);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedQuery, "continueQuery", new Class[] {String.class}, anyString);
                times = 0;
                Deencapsulation.invoke(mockedQuery, "sendQueryRequest", new Class[] {IotHubConnectionString.class, URL.class, HttpMethod.class, Long.class}, any, any, HttpMethod.POST, any);
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICETWIN_21_061: [If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException ]
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinFailedOnUpdateTwinNull(@Mocked Job mockedJob) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.scheduleUpdateTwin(queryCondition, null, now, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_062: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException ]
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinFailedOnStartTimeUtcNull(@Mocked Job mockedJob, @Mocked DeviceTwinDevice mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final long maxExecutionTimeInSeconds = 100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, null, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_063: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException ]
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinFailedOnInvalidMaxExecutionTimeInSeconds(@Mocked Job mockedJob, @Mocked DeviceTwinDevice mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = -100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_064: [The scheduleUpdateTwin shall create a new instance of the Job class ]
    // Tests_SRS_DEVICETWIN_21_068: [The scheduleUpdateTwin shall return the created instance of the Job class ]
    @Test
    public void scheduleUpdateTwinCreateJobSucceed(@Mocked Job mockedJob, @Mocked DeviceTwinDevice mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
                result = mockedJob;
                times = 1;
            }
        };

        //act
        Job job = testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);

        //assert
        assertNotNull(job);
    }

    // Tests_SRS_DEVICETWIN_21_065: [If the scheduleUpdateTwin failed to create a new instance of the Job class, it shall throws IOException. Threw by the Jobs constructor ]
    @Test (expected = IOException.class)
    public void scheduleUpdateTwinCreateJobFailed(@Mocked Job mockedJob, @Mocked DeviceTwinDevice mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
                result = new IOException();
            }
        };

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_066: [The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the Job class with the received parameters ]
    @Test
    public void schedule(@Mocked Job mockedJob, @Mocked DeviceTwinDevice mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
                result = mockedJob;
            }
        };

        //act
        Job job = testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedJob, "scheduleUpdateTwin", queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
                times = 1;
            }
        };
    }

    // Tests_SRS_DEVICETWIN_21_067: [If scheduleUpdateTwin failed, the scheduleUpdateTwin shall throws IotHubException. Threw by the scheduleUpdateTwin ]
    @Test (expected = IotHubException.class)
    public void scheduleUpdateTwinScheduleUpdateTwinFailed(@Mocked Job mockedJob, @Mocked DeviceTwinDevice mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, connectionString);
                result = mockedJob;
                Deencapsulation.invoke(mockedJob, "scheduleUpdateTwin", queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
                result = new IotHubException();
                times = 1;
            }
        };

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
    }

}
