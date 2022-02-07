/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.twin;

import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.auth.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.query.SqlQueryBuilder;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/*
    Unit tests for Device Twin
    Coverage : 94% method, 97% line
 */
public class TwinClientTest
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
    TwinState mockedTwinState;

    @Mocked
    Collection mockCollection;

    @Mocked
    Iterator<String> mockIterator;

    @Mocked
    DeviceCapabilities mockCapabilities;

    @Mocked
    HashMap<String, ConfigurationInfo> mockConfigurations;

    static String VALID_SQL_QUERY = null;

    private static final String STANDARD_HOSTNAME = "testHostName.azure.net";
    private static final String STANDARD_SHAREDACCESSKEYNAME = "testKeyName";
    private static final String STANDARD_SHAREDACCESSKEY = "1234567890ABCDEFGHIJKLMNOPQRESTUVWXYZ=";
    private static final String STANDARD_CONNECTIONSTRING =
            "HostName=" + STANDARD_HOSTNAME +
                    ";SharedAccessKeyName=" + STANDARD_SHAREDACCESSKEYNAME +
                    ";SharedAccessKey=" + STANDARD_SHAREDACCESSKEY;

    @Before
    public void setUp() throws IOException
    {
        VALID_SQL_QUERY = SqlQueryBuilder.createSqlQuery("tags.Floor, AVG(properties.reported.temperature) AS AvgTemperature",
                SqlQueryBuilder.FromType.DEVICES, "tags.building = '43'", null);
    }

    @Test
    public void testOptionsDefaults()
    {
        TwinClientOptions options = TwinClientOptions.builder().build();
        assertEquals((int) Deencapsulation.getField(TwinClientOptions.class, "DEFAULT_HTTP_READ_TIMEOUT_MS"), options.getHttpReadTimeout());
        assertEquals((int) Deencapsulation.getField(TwinClientOptions.class, "DEFAULT_HTTP_CONNECT_TIMEOUT_MS"), options.getHttpConnectTimeout());
    }

    /*
    **Tests_SRS_DEVICETWIN_25_002: [** The constructor shall create an IotHubConnectionStringBuilder object from the given connection string **]**
    **Tests_SRS_DEVICETWIN_25_003: [** The constructor shall create a new TwinClient instance and return it **]**
     */
    @Test
    public void constructorCreatesTwin(@Mocked IotHubConnectionStringBuilder mockedConnectionStringBuilder,
                                       @Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";

        //act
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

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
        TwinClient testTwin = new TwinClient(connectionString);

    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyCS() throws Exception
    {
        //arrange
        final String connectionString = "";

        //act
        TwinClient testTwin = new TwinClient(connectionString);

    }

    /*
    **Tests_SRS_DEVICETWIN_25_006: [** The function shall create a new SAS token **]**
    **Tests_SRS_DEVICETWIN_25_007: [** The function shall create a new HttpRequest with http method as Get **]**
    **Tests_SRS_DEVICETWIN_25_008: [** The function shall set the following HTTP headers specified in the IotHub TwinClient doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as sendHttpRequest id with a new string value for every sendHttpRequest
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**
     **Tests_SRS_DEVICETWIN_25_009: [** The function shall send the created sendHttpRequest and get the response **]**
     **Tests_SRS_DEVICETWIN_25_011: [** The function shall deserialize the payload by calling updateTwin Api on the twin object **]**
     **Tests_SRS_DEVICETWIN_25_012: [** The function shall set eTag, tags, desired property map, reported property map on the user device **]**
     */
    @Test
    public void getTwinOperationSucceeds(@Mocked Twin mockedDevice, @Mocked final URL mockUrl) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        Deencapsulation.invoke(testTwin,"getTwin", new Class[]{URL.class}, mockUrl);

        //assert
        new Verifications()
        {
            {
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 1;
                mockedHttpRequest.send();
                times = 1;
                new Twin(anyString);
                times = 1;
            }
        };
    }

    @Test
    public void getTwinSucceeds(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin("SomeDevID", "SomeModuleID");

        //assert
        new Verifications()
        {
            {
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 1;
                mockedHttpRequest.send();
                times = 1;
                new Twin(anyString);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected =  IllegalArgumentException.class)
    public void getTwinThrowsOnNullDevice(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin(null);
    }

    /*
     **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected =  IllegalArgumentException.class)
    public void getTwinThrowsOnEmptyDeviceID(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin("");
    }

    /*
     **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getTwinThrowsOnNullDeviceID(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin(null);
    }

    /*
     **Tests_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
     */
    @Test
    public void getTwinInvokeGetUrlTwinModuleIdEmpty(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin("somedeviceId", "");

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlModuleTwin(anyString, anyString, anyString);
                times = 1;
            }
        };
    }

     /*
     **Tests_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
     */
    @Test
    public void getTwinInvokeGetUrlTwinModuleIdNull(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin("somedeviceId", null);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlModuleTwin(anyString, anyString, anyString);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_28_001: [** The function shall build the URL for this operation by calling getUrlModuleTwin if moduleId is not null **]**
     */
    @Test
    public void getTwinInvokeGetUrlModuleTwin(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.getTwin("somedeviceId", "somemoduleId");

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlModuleTwin(anyString, anyString, anyString);
                times = 1;
            }
        };
    }

    /*
    **Tests_SRS_DEVICETWIN_25_030: [** The function shall build the URL for this operation by calling getUrlTwinDesired **]**
    **Tests_SRS_DEVICETWIN_25_031: [** The function shall serialize the desired properties map by calling resetDesiredProperty Api on the twin object for the device provided by the user**]**
    **Tests_SRS_DEVICETWIN_25_016: [** The function shall create a new SAS token **]**

    **Tests_SRS_DEVICETWIN_25_017: [** The function shall create a new HttpRequest with http method as Patch **]**

    **Tests_SRS_DEVICETWIN_25_018: [** The function shall set the following HTTP headers specified in the IotHub TwinClient doc.
                                                1. Key as authorization with value as sastoken
                                                2. Key as sendHttpRequest id with a new string value for every sendHttpRequest
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**

    **Tests_SRS_DEVICETWIN_25_019: [** The function shall send the created sendHttpRequest and get the response **]**

    **Tests_SRS_DEVICETWIN_25_020: [** The function shall verify the response status and throw proper Exception **]**

    *Tests_SRS_DEVICETWIN_25_036: [** The function shall verify the response status and throw proper Exception **]**

     */
    @Test
    public void updateTwinSucceeds(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);
        TwinCollection testMap = new TwinCollection();
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
                new TwinState((TwinCollection)any, (TwinCollection)any, null);
                result = mockedTwinState;
                mockedTwinState.toJsonElement().toString();
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlTwin(anyString, anyString);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 1;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_25_013: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfDeviceIsNull(@Mocked Twin mockedDevice) throws Exception
    { //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.updateTwin(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void updateTwinThrowsIfDeviceIDIsNull(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);
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
    public void updateTwinThrowsIfDeviceIDIsEmpty(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);
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
    public void updateTwinThrowsIfBothDesiredAndTagsIsEmpty(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);
        TwinCollection testMap = new TwinCollection();
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
    public void updateTwinDoesNotThrowsIfOnlyDesiredHasValue(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);
        TwinCollection testMap = new TwinCollection();
        testMap.put("TestKey", "TestValue");
        new Expectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                Deencapsulation.invoke(mockedDevice, "getDesiredMap");
                result = testMap;
                Deencapsulation.invoke(mockedDevice, "getTagsMap");
                result = null;
                new TwinState(null, (TwinCollection)any, null);
                result = mockedTwinState;
                mockedTwinState.toJsonElement().toString();
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlTwin(anyString, anyString);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 1;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }


    @Test
    public void updateTwinDoesNotThrowsIfOnlyTagsHasValue(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);
        TwinCollection testMap = new TwinCollection();
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
                new TwinState((TwinCollection)any, null, null);
                result = mockedTwinState;
                mockedTwinState.toJsonElement().toString();
                result = "SomeJsonString";
            }
        };

        //act
        testTwin.updateTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlTwin(anyString, anyString);
                times = 1;
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 1;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }

    private void constructorExpectations(String connectionString)
    {
        new Expectations()
        {
            {
                IotHubConnectionString.createIotHubConnectionString(connectionString);
                result = mockedConnectionString;
                mockedConnectionString.getHostName();
                result = "someHostName";
            }
        };
    }
}
