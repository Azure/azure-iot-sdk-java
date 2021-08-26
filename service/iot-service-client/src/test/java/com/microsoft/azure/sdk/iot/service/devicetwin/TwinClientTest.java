/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationInfo;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinConnectionState;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.StrictExpectations;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
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
    Query mockedQuery;

    @Mocked
    QueryCollection mockQueryCollection;

    @Mocked
    QueryCollectionResponse<Twin> mockQueryCollectionResponse;

    @Mocked
    QueryOptions mockQueryOptions;

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
        VALID_SQL_QUERY = SqlQuery.createSqlQuery("tags.Floor, AVG(properties.reported.temperature) AS AvgTemperature",
                SqlQuery.FromType.DEVICES, "tags.building = '43'", null).getQuery();
    }

    private void assertEqualSetAndMap(Set<Pair> pairSet, Map<String, String> map)
    {
        assertEquals(pairSet.size(), map.size());
        for(Pair p : pairSet)
        {
            String val = map.get(p.getKey());
            assertNotNull(val);
            assertEquals(p.getValue(), val);
        }
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
                                                2. Key as request id with a new string value for every request
                                                3. Key as User-Agent with value specified by the clientIdentifier and its version
                                                4. Key as Accept with value as application/json
                                                5. Key as Content-Type and value as application/json
                                                6. Key as charset and value as utf-8
                                                7. Key as If-Match and value as '*'  **]**
     **Tests_SRS_DEVICETWIN_25_009: [** The function shall send the created request and get the response **]**
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
        TwinCollection testMap = new TwinCollection();
        String expectedConnectionState = TwinConnectionState.CONNECTED.toString();
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                TwinState.createFromTwinJson((String)any);
                result = mockedTwinState;
                mockedTwinState.getCapabilities();
                result = mockCapabilities;
                mockedTwinState.getConfigurations();
                result = mockConfigurations;
                mockedTwinState.getConnectionState();
                result = expectedConnectionState;
            }
        };

        //act
        Deencapsulation.invoke(testTwin,"getTwinOperation", new Class[]{URL.class, Twin.class},
                mockUrl, mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 6;
                mockedHttpRequest.send();
                times = 1;
                TwinState.createFromTwinJson((String)any);
                times = 1;
                mockedTwinState.getETag();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setETag", anyString);
                times = 1;
                mockedTwinState.getTags();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setTags", testMap);
                times = 1;
                mockedTwinState.getDesiredProperty();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setDesiredProperties", testMap);
                times = 1;
                mockedTwinState.getReportedProperty();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setReportedProperties", testMap );
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setCapabilities", mockCapabilities);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setConfigurations", mockConfigurations);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setConnectionState", expectedConnectionState);
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
        TwinCollection testMap = new TwinCollection();
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "SomeDevID";
                mockedDevice.getModuleId();
                result = "SomeModuleID";
                TwinState.createFromTwinJson((String)any);
                result = mockedTwinState;
                mockedTwinState.getCapabilities();
                result = mockCapabilities;
                mockedTwinState.getConfigurations();
                result = mockConfigurations;
            }
        };

        //act
        testTwin.getTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 6;
                mockedHttpRequest.send();
                times = 1;
                TwinState.createFromTwinJson((String)any);
                times = 1;
                mockedTwinState.getETag();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setETag", anyString);
                times = 1;
                mockedTwinState.getTags();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setTags", testMap);
                times = 1;
                mockedTwinState.getDesiredProperty();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setDesiredProperties", testMap);
                times = 1;
                mockedTwinState.getReportedProperty();
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setReportedProperties", testMap );
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setCapabilities", mockCapabilities);
                times = 1;
                Deencapsulation.invoke(mockedDevice, "setConfigurations", mockConfigurations);
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

    /*
     **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
    @Test (expected = IllegalArgumentException.class)
    public void getTwinThrowsOnNullDeviceID(@Mocked Twin mockedDevice) throws Exception
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
        testTwin.getTwin(mockedDevice);
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
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "somedeviceId";
                mockedDevice.getModuleId();
                result = "";
            }
        };

        //act
        testTwin.getTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlTwin(anyString, anyString);
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
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "somedeviceId";
                mockedDevice.getModuleId();
                result = null;
            }
        };

        //act
        testTwin.getTwin(mockedDevice);

        //assert
        new Verifications()
        {
            {
                IotHubConnectionString.getUrlTwin(anyString, anyString);
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
        new NonStrictExpectations()
        {
            {
                mockedDevice.getDeviceId();
                result = "somedeviceId";
                mockedDevice.getModuleId();
                result = "somemoduleId";
            }
        };

        //act
        testTwin.getTwin(mockedDevice);

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
    **Tests_SRS_DEVICETWIN_25_010: [** The function shall verify the response status and throw proper Exception **]**
     */
    @Test (expected = IotHubException.class)
    public void getTwinThrowsVerificationFailure(@Mocked Twin mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);
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
    **Tests_SRS_DEVICETWIN_25_016: [** The function shall create a new SAS token **]**

    **Tests_SRS_DEVICETWIN_25_017: [** The function shall create a new HttpRequest with http method as Patch **]**

    **Tests_SRS_DEVICETWIN_25_018: [** The function shall set the following HTTP headers specified in the IotHub TwinClient doc.
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
                times = 6;
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
                times = 6;
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
                times = 6;
                mockedHttpRequest.send();
                times = 1;
            }
        };
    }

    @Test (expected = IotHubException.class)
    public void updateTwinThrowsVerificationThrows(@Mocked Twin mockedDevice) throws Exception
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
    public void queryTwinSucceeds(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        final int connectTimeout = 1234;
        final int readTimeout = 5678;
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString, TwinClientOptions.builder().httpConnectTimeout(connectTimeout).httpReadTimeout(readTimeout).build());


        new Expectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
            }
        };

        //act
        testTwin.queryTwin(VALID_SQL_QUERY);
    }

    //Tests_SRS_DEVICETWIN_25_047: [ The method shall throw IllegalArgumentException if the query is null or empty.]
    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnNullQuery(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.queryTwin(null);
    }

    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnEmptyQuery(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.queryTwin("");
    }

    //Tests_SRS_DEVICETWIN_25_048: [ The method shall throw IllegalArgumentException if the page size is zero or negative.]
    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnNegativePageSize(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.queryTwin(VALID_SQL_QUERY, -1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void twinQueryThrowsOnZeroPageSize(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.queryTwin(VALID_SQL_QUERY, 0);
    }

    //Tests_SRS_DEVICETWIN_25_055: [ If a queryResponse is available, this method shall return true as is to the user. ]
    //Tests_SRS_DEVICETWIN_25_054: [ The method shall check if a response to query is avaliable by calling hasNext on the query object.]
    @Test
    public void hasNextSucceeds(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        final int connectTimeout = 1234;
        final int readTimeout = 5678;
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString, TwinClientOptions.builder().httpConnectTimeout(connectTimeout).httpReadTimeout(readTimeout).build());

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
        boolean result = testTwin.hasNextTwin(testQuery);
        assertTrue(result);
    }

    //Tests_SRS_DEVICETWIN_25_053: [ The method shall throw IllegalArgumentException if query is null ]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextThrowsOnNullQuery(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        boolean result = testTwin.hasNextTwin(null);
    }

    @Test (expected = IotHubException.class)
    public void hasNextThrowsIfHasNextOnQueryThrows(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

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
        boolean result = testTwin.hasNextTwin(testQuery);
    }

    //Tests_SRS_DEVICETWIN_25_059: [ The method shall parse the next element from the query response as Twin Document using TwinState and provide the response on Twin.]
    @Test
    public void nextRetrievesCorrectlyWithoutModuleId() throws IotHubException, IOException
    {
        //arrange
        final Integer version = 15;
        final String etag = "validEtag";
        final String connectionString = "testString";
        final int connectTimeout = 1234;
        final int readTimeout = 5678;
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString, TwinClientOptions.builder().httpConnectTimeout(connectTimeout).httpReadTimeout(readTimeout).build());
        final String expectedString = "testJsonAsNext";
        final String modelId = "testModelId";
        TwinCollection tags = new TwinCollection();
        tags.put("tagsKey", "tagsValue");
        TwinCollection rp = new TwinCollection();
        rp.put("rpKey", "rpValue");
        TwinCollection dp = new TwinCollection();
        dp.put("dpKey", "dpValue");

        new Expectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "next");
                result = expectedString;
                mockedTwinState.getDeviceId();
                result = "testDeviceID";
                mockedTwinState.getModuleId();
                result = null;
                mockedTwinState.getVersion();
                result = version;
                mockedTwinState.getETag();
                result = etag;
                mockedTwinState.getModelId();
                result = modelId;
                mockedTwinState.getTags();
                result = tags;
                mockedTwinState.getDesiredProperty();
                result = dp;
                mockedTwinState.getReportedProperty();
                result = rp;
                mockCapabilities.isIotEdge();
                result = Boolean.TRUE;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        Twin result = testTwin.getNextTwin(testQuery);

        //assert
        assertNotNull(result.getTags());
        assertNotNull(result.getReportedProperties());
        assertNotNull(result.getDesiredProperties());

        assertEquals(version, result.getVersion());
        assertEquals(etag, result.getETag());
        assertEqualSetAndMap(result.getTags(), (Map)tags);
        assertEqualSetAndMap(result.getDesiredProperties(), (Map)dp);
        assertEqualSetAndMap(result.getReportedProperties(), (Map)rp);
        assertTrue(result.getCapabilities().isIotEdge());
        assertNull(result.getModuleId());
        assertEquals(result.getModelId(), result.getModelId());
    }

    @Test
    public void nextRetrievesCorrectlyWithoutModelId() throws IotHubException, IOException
    {
        //arrange
        final Integer version = 15;
        final String etag = "validEtag";
        final int connectTimeout = 1234;
        final int readTimeout = 5678;
        final String connectionString = "someConnectionString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString, TwinClientOptions.builder().httpConnectTimeout(connectTimeout).httpReadTimeout(readTimeout).build());
        final String expectedString = "testJsonAsNext";
        final String moduleId = "testModuleId";
        TwinCollection tags = new TwinCollection();
        tags.put("tagsKey", "tagsValue");
        TwinCollection rp = new TwinCollection();
        rp.put("rpKey", "rpValue");
        TwinCollection dp = new TwinCollection();
        dp.put("dpKey", "dpValue");

        new Expectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
                Deencapsulation.invoke(mockedQuery, "next");
                result = expectedString;
                mockedTwinState.getDeviceId();
                result = "testDeviceID";
                mockedTwinState.getModuleId();
                result = moduleId;
                mockedTwinState.getVersion();
                result = version;
                mockedTwinState.getETag();
                result = etag;
                mockedTwinState.getModelId();
                result = null;
                mockedTwinState.getTags();
                result = tags;
                mockedTwinState.getDesiredProperty();
                result = dp;
                mockedTwinState.getReportedProperty();
                result = rp;
                mockCapabilities.isIotEdge();
                result = Boolean.TRUE;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        Twin result = testTwin.getNextTwin(testQuery);

        assertNotNull(result.getTags());
        assertNotNull(result.getReportedProperties());
        assertNotNull(result.getDesiredProperties());

        assertEquals(version, result.getVersion());
        assertEquals(etag, result.getETag());
        assertEqualSetAndMap(result.getTags(), (Map)tags);
        assertEqualSetAndMap(result.getDesiredProperties(), (Map)dp);
        assertEqualSetAndMap(result.getReportedProperties(), (Map)rp);
        assertTrue(result.getCapabilities().isIotEdge());
        assertNull(result.getModelId());
        assertEquals(result.getModuleId(), result.getModuleId());
    }

    @Test (expected = IllegalArgumentException.class)
    public void nextThrowsOnNullQuery(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        TwinClient testTwin = new TwinClient(connectionString);

        new NonStrictExpectations()
        {
            {
                Deencapsulation.newInstance(Query.class, new Class[] {String.class, Integer.class, QueryType.class}, anyString, anyInt, QueryType.TWIN);
                result = mockedQuery;
            }
        };

        Query testQuery = testTwin.queryTwin(VALID_SQL_QUERY);

        //act
        testTwin.getNextTwin(null);
    }

    @Test (expected = IotHubException.class)
    public void nextThrowsOnQueryNextThrows(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

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
        testTwin.getNextTwin(testQuery);
    }

    //Tests_SRS_DEVICETWIN_25_058: [ The method shall check if hasNext returns true and throw NoSuchElementException otherwise ]
    @Test (expected = NoSuchElementException.class)
    public void nextThrowsIfNoNewElements(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

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
        Twin result = testTwin.getNextTwin(testQuery);
    }

    //Tests_SRS_DEVICETWIN_25_060: [ If the next element from the query response is an object other than String, then this method shall throw IOException ]
    @Test (expected = IOException.class)
    public void nextThrowsIfNonStringRetrieved(@Mocked Twin mockedDevice) throws IotHubException, IOException
    {
        //arrange
        final String connectionString = "testString";
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

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
        Twin result = testTwin.getNextTwin(testQuery);

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
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.scheduleUpdateTwin(queryCondition, null, now, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_062: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException ]
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinFailedOnStartTimeUtcNull(@Mocked Job mockedJob, @Mocked Twin mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final long maxExecutionTimeInSeconds = 100;
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, null, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_063: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException ]
    @Test (expected = IllegalArgumentException.class)
    public void scheduleUpdateTwinFailedOnInvalidMaxExecutionTimeInSeconds(@Mocked Job mockedJob, @Mocked Twin mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = -100;
        TwinClient testTwin = new TwinClient(connectionString);

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_064: [The scheduleUpdateTwin shall create a new instance of the Job class ]
    // Tests_SRS_DEVICETWIN_21_068: [The scheduleUpdateTwin shall return the created instance of the Job class ]
    @Test
    public void scheduleUpdateTwinCreateJobSucceed(@Mocked Job mockedJob, @Mocked Twin mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, anyString);
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
    public void scheduleUpdateTwinCreateJobFailed(@Mocked Job mockedJob, @Mocked Twin mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, anyString);
                result = new IOException();
            }
        };

        //act
        testTwin.scheduleUpdateTwin(queryCondition, mockedDevice, now, maxExecutionTimeInSeconds);
    }

    // Tests_SRS_DEVICETWIN_21_066: [The scheduleUpdateTwin shall invoke the scheduleUpdateTwin in the Job class with the received parameters ]
    @Test
    public void schedule(@Mocked Job mockedJob, @Mocked Twin mockedDevice) throws IOException, IotHubException
    {
        //arrange
        final String connectionString = "testString";
        final String queryCondition = "validQueryCondition";
        final Date now = new Date();
        final long maxExecutionTimeInSeconds = 100;
        constructorExpectations(connectionString);
        TwinClient testTwin = new TwinClient(connectionString);

        new NonStrictExpectations()
        {
            {
                mockedConnectionString.toString();
                result = connectionString;
                Deencapsulation.newInstance(Job.class, new Class[]{String.class}, anyString);
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

    //Tests_SRS_DEVICETWIN_34_069: [This function shall return the results of calling queryTwinCollection(sqlQuery, DEFAULT_PAGE_SIZE).]
    @Test
    public void queryTwinCollectionWithoutPageSizeUsesDefaultPageSize() throws IOException, IotHubException
    {
        //arrange
        TwinClient twinClient = new TwinClient(STANDARD_CONNECTIONSTRING);
        String expectedQuery = "someQuery";
        Integer expectedPageSize = Deencapsulation.getField(twinClient, "DEFAULT_PAGE_SIZE");
        new StrictExpectations(twinClient)
        {
            {
                //assert
                twinClient.queryTwinCollection(expectedQuery, expectedPageSize);
                result = null;
            }
        };

        //act
        twinClient.queryTwinCollection(expectedQuery);
    }

    //Tests_SRS_DEVICETWIN_34_076: [If the provided deviceTwinQueryCollection is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void getNextDeviceTwinCollectionWithOptionsThrowsForNullQueryCollection() throws IOException, IotHubException
    {
        //arrange
        TwinClient twinClient = new TwinClient(STANDARD_CONNECTIONSTRING);

        //act
        twinClient.next(null, new QueryOptions());
    }

    //Tests_SRS_DEVICETWIN_34_077: [If the provided deviceTwinQueryCollection has no next set to give, this function shall return null.]
    @Test
    public void getNextDeviceTwinCollectionWithOptionsReturnsNullIfDoesNotHaveNext() throws IOException, IotHubException
    {
        //arrange
        TwinClient twinClient = new TwinClient(STANDARD_CONNECTIONSTRING);

        new NonStrictExpectations(twinClient)
        {
            {
                twinClient.hasNext(mockQueryCollection);
                result = false;
            }
        };

        //act
        QueryCollectionResponse actualResponse = twinClient.next(mockQueryCollection, mockQueryOptions);

        //assert
        assertNull(actualResponse);
    }

    //Tests_SRS_DEVICETWIN_34_078: [If the provided deviceTwinQueryCollection has a next set to give, this function shall retrieve that set from deviceTwinQueryCollection, cast its contents to Twin objects, and return it in a QueryCollectionResponse object.]
    //Tests_SRS_DEVICETWIN_34_079: [The returned QueryCollectionResponse object shall contain the continuation token needed to retrieve the next set with.]
    @Test
    public void getNextDeviceTwinCollectionWithOptionsSuccess() throws IOException, IotHubException
    {
        //arrange
        TwinClient twinClient = new TwinClient(STANDARD_CONNECTIONSTRING);
        String expectedContinuationToken = "some continuation token";

        String expectedJsonString = "some json string";

        new MockUp<TwinClient>()
        {
            @Mock boolean hasNext(QueryCollection twinQueryCollection)
            {
                return true;
            }

            @Mock
            Twin jsonToTwin(String json) throws IOException
            {
                return new Twin();
            }
        };

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockQueryCollection, "next", new Class[] {QueryOptions.class}, mockQueryOptions);
                result = mockQueryCollectionResponse;

                mockQueryCollectionResponse.getCollection();
                result = mockCollection;

                mockCollection.iterator();
                result = mockIterator;

                mockIterator.hasNext();
                result = true;

                mockIterator.next();
                result = expectedJsonString;

                mockIterator.hasNext();
                result = true;

                mockIterator.next();
                result = expectedJsonString;

                mockIterator.hasNext();
                result = false;

                mockQueryCollectionResponse.getContinuationToken();
                result = expectedContinuationToken;

                Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, (Collection) any, expectedContinuationToken);
                result = mockQueryCollectionResponse;
            }
        };

        //act
        twinClient.next(mockQueryCollection, mockQueryOptions);
    }

    //Tests_SRS_DEVICETWIN_34_071: [This function shall return if the provided deviceTwinQueryCollection has next.]
    @Test
    public void hasNextDeviceTwinQueryCollectionSuccess() throws IOException, IotHubException
    {
        //arrange
        TwinClient twinClient = new TwinClient(STANDARD_CONNECTIONSTRING);

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockQueryCollection, "hasNext");
                result = true;
            }
        };

        //act
        boolean actualHasNext = twinClient.hasNext(mockQueryCollection);

        //assert
        assertTrue(actualHasNext);
    }

    //Tests_SRS_DEVICETWIN_34_080: [If the provided deviceTwinQueryCollection is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextDeviceTwinQueryCollectionThrowsForNullQuery() throws IOException, IotHubException
    {
        //arrange
        TwinClient twinClient = new TwinClient(STANDARD_CONNECTIONSTRING);

        //act
        twinClient.hasNext(null);
    }
}
