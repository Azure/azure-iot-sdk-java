/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service.devicetwin;

import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.deps.twin.ConfigurationInfo;
import com.microsoft.azure.sdk.iot.deps.twin.DeviceCapabilities;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;
import com.microsoft.azure.sdk.iot.service.Configuration;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubExceptionManager;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;
import mockit.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.junit.Assert.*;

/*
    Unit tests for Device Twin
    Coverage : 94% method, 97% line
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
    TwinState mockedTwinState;

    @Mocked
    Query mockedQuery;

    @Mocked
    QueryCollection mockQueryCollection;

    @Mocked
    QueryCollectionResponse<DeviceTwinDevice> mockQueryCollectionResponse;

    @Mocked
    QueryOptions mockQueryOptions;

    @Mocked
    URL mockUrl;

    @Mocked
    Collection mockCollection;

    @Mocked
    Iterator<String> mockIterator;

    @Mocked
    DeviceCapabilities mockCapabilities;

    @Mocked
    HashMap<String, ConfigurationInfo> mockConfigurations;

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
     **Tests_SRS_DEVICETWIN_25_012: [** The function shall set eTag, tags, desired property map, reported property map on the user device **]**
     */
    @Test
    public void getTwinOperationSucceeds(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        TwinCollection testMap = new TwinCollection();
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
            }
        };

        //act
        Deencapsulation.invoke(testTwin,"getTwinOperation", new Class[]{URL.class, DeviceTwinDevice.class},
                mockUrl, mockedDevice);

        //assert
        new Verifications()
        {
            {
                mockedHttpRequest.setReadTimeoutMillis(anyInt);
                times = 1;
                mockedHttpRequest.setHeaderField(anyString, anyString);
                times = 5;
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

    @Test
    public void getTwinSucceeds(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
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
                times = 5;
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
    public void getTwinThrowsOnNullDevice(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.getTwin(null);
    }

    /*
     **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
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

    /*
     **Tests_SRS_DEVICETWIN_25_004: [** The function shall throw IllegalArgumentException if the input device is null or if deviceId is null or empty **]**
     */
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
     **Tests_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
     */
    @Test
    public void getTwinInvokeGetUrlTwinModuleIdEmpty(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
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
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
            }
        };
    }

     /*
     **Tests_SRS_DEVICETWIN_25_005: [** The function shall build the URL for this operation by calling getUrlTwin **]**
     */
    @Test
    public void getTwinInvokeGetUrlTwinModuleIdNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
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
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
            }
        };
    }

    /*
     **Tests_SRS_DEVICETWIN_28_001: [** The function shall build the URL for this operation by calling getUrlModuleTwin if moduleId is not null **]**
     */
    @Test
    public void getTwinInvokeGetUrlModuleTwin(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
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
                mockedConnectionString.getUrlModuleTwin(anyString, anyString);
                times = 1;
            }
        };
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

    @Test (expected = UnsupportedOperationException.class)
    public void replaceDesiredPropertiesThrows(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.replaceDesiredProperties(mockedDevice);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void replaceTagsThrowsIfDeviceIsNull(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);

        //act
        testTwin.replaceTags(mockedDevice);
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
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                new TwinState((TwinCollection)any, (TwinCollection)any, null);
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
    public void updateTwinDoesNotThrowsIfOnlyDesiredHasValue(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
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
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                new TwinState(null, (TwinCollection)any, null);
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
                mockedConnectionString.getUrlTwin(anyString);
                times = 1;
                new TwinState((TwinCollection)any, null, null);
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

    @Test (expected = IotHubException.class)
    public void updateTwinThrowsVerificationThrows(@Mocked DeviceTwinDevice mockedDevice) throws Exception
    {
        //arrange
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
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

    //Tests_SRS_DEVICETWIN_25_059: [ The method shall parse the next element from the query response as Twin Document using TwinState and provide the response on DeviceTwinDevice.]
    @Test
    public void nextRetrievesCorrectlyWithoutModuleId() throws IotHubException, IOException
    {
        //arrange
        final Integer version = 15;
        final String etag = "validEtag";
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        final String expectedString = "testJsonAsNext";
        TwinCollection tags = new TwinCollection();
        tags.put("tagsKey", "tagsValue");
        TwinCollection rp = new TwinCollection();
        rp.put("rpKey", "rpValue");
        TwinCollection dp = new TwinCollection();
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
                mockedTwinState.getDeviceId();
                result = "testDeviceID";
                mockedTwinState.getModuleId();
                result = null;
                mockedTwinState.getVersion();
                result = version;
                mockedTwinState.getETag();
                result = etag;
                mockedTwinState.getTags();
                result = tags;
                mockedTwinState.getDesiredProperty();
                result = dp;
                mockedTwinState.getReportedProperty();
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

        assertEquals(version, result.getVersion());
        assertEquals(etag, result.getETag());
        assetEqualSetAndMap(result.getTags(), (Map)tags);
        assetEqualSetAndMap(result.getDesiredProperties(), (Map)dp);
        assetEqualSetAndMap(result.getReportedProperties(), (Map)rp);
        assertNull(result.getModuleId());
    }

    //Tests_SRS_DEVICETWIN_25_059: [ The method shall parse the next element from the query response as Twin Document using TwinState and provide the response on DeviceTwinDevice.]
    @Test
    public void nextRetrievesCorrectlyWithModuleId() throws IotHubException, IOException
    {
        //arrange
        final String expectedModuleId = "someModuleId";
        final Integer version = 15;
        final String etag = "validEtag";
        final String connectionString = "testString";
        DeviceTwin testTwin = DeviceTwin.createFromConnectionString(connectionString);
        final String expectedString = "testJsonAsNext";
        TwinCollection tags = new TwinCollection();
        tags.put("tagsKey", "tagsValue");
        TwinCollection rp = new TwinCollection();
        rp.put("rpKey", "rpValue");
        TwinCollection dp = new TwinCollection();
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
                mockedTwinState.getDeviceId();
                result = "testDeviceID";
                mockedTwinState.getModuleId();
                result = expectedModuleId;
                mockedTwinState.getVersion();
                result = version;
                mockedTwinState.getETag();
                result = etag;
                mockedTwinState.getTags();
                result = tags;
                mockedTwinState.getDesiredProperty();
                result = dp;
                mockedTwinState.getReportedProperty();
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

        assertEquals(version, result.getVersion());
        assertEquals(etag, result.getETag());
        assetEqualSetAndMap(result.getTags(), (Map)tags);
        assetEqualSetAndMap(result.getDesiredProperties(), (Map)dp);
        assetEqualSetAndMap(result.getReportedProperties(), (Map)rp);
        assertEquals(expectedModuleId, result.getModuleId());
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

    //Tests_SRS_DEVICETWIN_34_069: [This function shall return the results of calling queryTwinCollection(sqlQuery, DEFAULT_PAGE_SIZE).]
    @Test
    public void queryTwinCollectionWithoutPageSizeUsesDefaultPageSize() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();
        String expectedQuery = "someQuery";
        Integer expectedPageSize = Deencapsulation.getField(deviceTwin, "DEFAULT_PAGE_SIZE");
        new StrictExpectations(deviceTwin)
        {
            {
                //assert
                deviceTwin.queryTwinCollection(expectedQuery, expectedPageSize);
                result = null;
            }
        };

        //act
        deviceTwin.queryTwinCollection(expectedQuery);
    }

    //Tests_SRS_DEVICETWIN_34_070: [This function shall return a new QueryCollection object of type TWIN with the provided sql query and page size.]
    @Test
    public void queryTwinCollectionWithPageSizeSuccess() throws IOException, IotHubException
    {
        //arrange
        String expectedSqlQuery = "some query";
        int expectedPageSize = 23;

        new StrictExpectations()
        {
            {
                //returning mock URL seems to break this test for some reason
                mockedConnectionString.getUrlTwinQuery();
                result = null;
                Deencapsulation.newInstance(QueryCollection.class, new Class[] {String.class, int.class, QueryType.class, IotHubConnectionString.class, URL.class, HttpMethod.class, long.class}, expectedSqlQuery, expectedPageSize, QueryType.TWIN, mockedConnectionString, null, HttpMethod.POST, 0);
                result = mockQueryCollection;
            }
        };

        DeviceTwin deviceTwin = new DeviceTwin();
        Deencapsulation.setField(deviceTwin, "iotHubConnectionString", mockedConnectionString);

        //act
        deviceTwin.queryTwinCollection(expectedSqlQuery, expectedPageSize);
    }

    //Tests_SRS_DEVICETWIN_34_075: [This function shall call next(deviceTwinQueryCollection, queryOptions) where queryOptions has the deviceTwinQueryCollection's current page size.]
    @Test
    public void getNextDeviceTwinCollectionWithoutOptionsCallsGetNextDeviceTwinCollectionWithOptions() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();
        Integer expectedPageSize = 33;

        new NonStrictExpectations(deviceTwin)
        {
            {
                new QueryOptions();
                result = mockQueryOptions;
                mockQueryOptions.getPageSize();
                result = expectedPageSize;
                deviceTwin.next(mockQueryCollection, mockQueryOptions);
                result = mockQueryCollectionResponse;
            }
        };

        //act
        deviceTwin.next(mockQueryCollection);

        //assert
        new Verifications()
        {
            {
                mockQueryOptions.setPageSize(anyInt);
                times = 1;
                deviceTwin.next(mockQueryCollection, mockQueryOptions);
                times = 1;
            }
        };
    }

    //Tests_SRS_DEVICETWIN_34_076: [If the provided deviceTwinQueryCollection is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void getNextDeviceTwinCollectionWithOptionsThrowsForNullQueryCollection() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();

        //act
        deviceTwin.next(null, new QueryOptions());
    }

    //Tests_SRS_DEVICETWIN_34_077: [If the provided deviceTwinQueryCollection has no next set to give, this function shall return null.]
    @Test
    public void getNextDeviceTwinCollectionWithOptionsReturnsNullIfDoesNotHaveNext() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();

        new NonStrictExpectations(deviceTwin)
        {
            {
                deviceTwin.hasNext(mockQueryCollection);
                result = false;
            }
        };

        //act
        QueryCollectionResponse actualResponse = deviceTwin.next(mockQueryCollection, mockQueryOptions);

        //assert
        assertNull(actualResponse);
    }

    //Tests_SRS_DEVICETWIN_34_078: [If the provided deviceTwinQueryCollection has a next set to give, this function shall retrieve that set from deviceTwinQueryCollection, cast its contents to DeviceTwinDevice objects, and return it in a QueryCollectionResponse object.]
    //Tests_SRS_DEVICETWIN_34_079: [The returned QueryCollectionResponse object shall contain the continuation token needed to retrieve the next set with.]
    @Test
    public void getNextDeviceTwinCollectionWithOptionsSuccess() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();
        String expectedContinuationToken = "some continuation token";

        String expectedJsonString = "some json string";

        new StrictExpectations(deviceTwin)
        {
            {
                deviceTwin.hasNext(mockQueryCollection);
                result = true;

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

                Deencapsulation.invoke(deviceTwin, "jsonToDeviceTwinDevice", new Class[] {String.class}, expectedJsonString);
                result = new DeviceTwinDevice();

                mockIterator.hasNext();
                result = true;

                mockIterator.next();
                result = expectedJsonString;

                Deencapsulation.invoke(deviceTwin, "jsonToDeviceTwinDevice", new Class[] {String.class}, expectedJsonString);
                result = new DeviceTwinDevice();

                mockIterator.hasNext();
                result = false;

                mockQueryCollectionResponse.getContinuationToken();
                result = expectedContinuationToken;

                Deencapsulation.newInstance(QueryCollectionResponse.class, new Class[] {Collection.class, String.class}, (Collection) any, expectedContinuationToken);
                result = mockQueryCollectionResponse;
            }
        };

        //act
        deviceTwin.next(mockQueryCollection, mockQueryOptions);
    }

    //Tests_SRS_DEVICETWIN_34_071: [This function shall return if the provided deviceTwinQueryCollection has next.]
    @Test
    public void hasNextDeviceTwinQueryCollectionSuccess() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();

        new StrictExpectations()
        {
            {
                Deencapsulation.invoke(mockQueryCollection, "hasNext");
                result = true;
            }
        };

        //act
        boolean actualHasNext = deviceTwin.hasNext(mockQueryCollection);

        //assert
        assertTrue(actualHasNext);
    }

    //Tests_SRS_DEVICETWIN_34_080: [If the provided deviceTwinQueryCollection is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void hasNextDeviceTwinQueryCollectionThrowsForNullQuery() throws IOException, IotHubException
    {
        //arrange
        DeviceTwin deviceTwin = new DeviceTwin();

        //act
        deviceTwin.hasNext(null);
    }
}
