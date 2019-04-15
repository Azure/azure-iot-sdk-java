/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.microsoft.azure.sdk.iot.common.helpers.CorrelationDetailsLoggingAssert.buildExceptionMessage;
import static org.junit.Assert.*;

/**
 * Test class containing all tests to be run on JVM and android pertaining to Queries. Class needs to be extended
 * in order to run these tests as that extended class handles setting connection strings and certificate generation
 */
public class QueryTwinTests extends DeviceTwinCommon
{
    public QueryTwinTests(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    @Test
    public void testRawQueryTwin() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        final double actualNumOfDevices = MAX_DEVICES;

        setDesiredProperties(queryProperty, queryPropertyValue, MAX_DEVICES);

        Thread.sleep(DESIRED_PROPERTIES_PROPAGATION_TIME_MILLIS);

        // Raw Query for multiple devices having same property
        final String select = "properties.desired." + queryProperty + " AS " + queryProperty + "," + " COUNT() AS numberOfDevices";
        final String groupBy = "properties.desired." + queryProperty;
        final SqlQuery sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy);
        Query rawTwinQuery = scRawTwinQueryClient.query(sqlQuery.getQuery(), PAGE_SIZE);

        while (scRawTwinQueryClient.hasNext(rawTwinQuery))
        {
            String result = scRawTwinQueryClient.next(rawTwinQuery);
            assertNotNull(result);
            Map map = gson.fromJson(result, Map.class);
            if (map.containsKey("numberOfDevices") && map.containsKey(queryProperty))
            {
                double value = (double) map.get("numberOfDevices");
                assertEquals(value, actualNumOfDevices, 0);
            }
        }

        removeMultipleDevices(MAX_DEVICES);
    }

    @Test
    public void testRawQueryMultipleInParallelTwin() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);
        final Gson gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().create();
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        final double actualNumOfDevices = MAX_DEVICES;

        final String queryPropertyEven = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValueEven = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        int noOfEvenDevices = 0;

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            if (i % 2 == 0)
            {
                desiredProperties.add(new Pair(queryPropertyEven, queryPropertyValueEven));
                noOfEvenDevices++;
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Raw Query for multiple devices having same property
                    final String select = "properties.desired." + queryProperty + " AS " + queryProperty + "," + " COUNT() AS numberOfDevices";
                    final String groupBy = "properties.desired." + queryProperty;
                    final SqlQuery sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy);
                    Query rawTwinQuery = scRawTwinQueryClient.query(sqlQuery.getQuery(), PAGE_SIZE);

                    while (scRawTwinQueryClient.hasNext(rawTwinQuery))
                    {
                        String result = scRawTwinQueryClient.next(rawTwinQuery);
                        assertNotNull(result);
                        Map map = gson.fromJson(result, Map.class);
                        if (map.containsKey("numberOfDevices") && map.containsKey(queryProperty))
                        {
                            double value = (double) map.get("numberOfDevices");
                            assertEquals(value, actualNumOfDevices, 0);
                        }
                    }
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }

            }
        });

        final double actualNumOfDevicesEven = noOfEvenDevices;
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    // Raw Query for multiple devices having same property
                    final String select = "properties.desired." + queryPropertyEven + " AS " + queryPropertyEven + "," + " COUNT() AS numberOfDevices";
                    final String groupBy = "properties.desired." + queryPropertyEven;
                    final SqlQuery sqlQuery = SqlQuery.createSqlQuery(select, SqlQuery.FromType.DEVICES, null, groupBy);
                    Query rawTwinQuery = scRawTwinQueryClient.query(sqlQuery.getQuery(), PAGE_SIZE);

                    while (scRawTwinQueryClient.hasNext(rawTwinQuery))
                    {
                        String result = scRawTwinQueryClient.next(rawTwinQuery);
                        assertNotNull(result);
                        Map map = gson.fromJson(result, Map.class);
                        if (map.containsKey("numberOfDevices") && map.containsKey(queryPropertyEven))
                        {
                            double value = (double) map.get("numberOfDevices");
                            assertEquals(value, actualNumOfDevicesEven, 0);
                        }
                    }
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }
            }
        });

        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }

        removeMultipleDevices(MAX_DEVICES);
    }

    @Test
    public void testQueryTwin() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();

        setDesiredProperties(queryProperty, queryPropertyValue, MAX_DEVICES);

        // Query multiple devices having same property
        final String where = "is_defined(properties.desired." + queryProperty + ")";
        SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, where, null);
        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
        Query twinQuery = sCDeviceTwin.queryTwin(sqlQuery.getQuery(), PAGE_SIZE);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            if (sCDeviceTwin.hasNextDeviceTwin(twinQuery))
            {
                DeviceTwinDevice d = sCDeviceTwin.getNextDeviceTwin(twinQuery);
                assertNotNull(d.getVersion());

                for (Pair dp : d.getDesiredProperties())
                {
                    assertEquals(buildExceptionMessage("Unexpected desired property key, expected " + queryProperty + " but was " + dp.getKey(), internalClient), queryProperty, dp.getKey());
                    assertEquals(buildExceptionMessage("Unexpected desired property value, expected " + queryPropertyValue + " but was " + dp.getValue(), internalClient), queryPropertyValue, dp.getValue());
                }
            }
        }
        assertFalse(sCDeviceTwin.hasNextDeviceTwin(twinQuery));
        removeMultipleDevices(MAX_DEVICES);
    }

    @Test
    public void testQueryTwinWithContinuationToken() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(PAGE_SIZE + 1);

        // Add same desired on multiple devices so that they can be queried
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        setDesiredProperties(queryProperty, queryPropertyValue, PAGE_SIZE + 1);

        // Query multiple devices having same property
        final String where = "is_defined(properties.desired." + queryProperty + ")";

        SqlQuery sqlQuery;
        if (this.testInstance.moduleId != null)
        {
            sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.MODULES, where, null);
        }
        else
        {
            sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, where, null);
        }

        Thread.sleep(MAXIMUM_TIME_FOR_IOTHUB_PROPAGATION_BETWEEN_DEVICE_SERVICE_CLIENTS);
        QueryCollection twinQueryCollection = sCDeviceTwin.queryTwinCollection(sqlQuery.getQuery(), PAGE_SIZE);

        // Run a query and save the continuation token for the second page of results
        QueryCollectionResponse<DeviceTwinDevice> queryCollectionResponse = sCDeviceTwin.next(twinQueryCollection);
        Collection<DeviceTwinDevice> queriedDeviceTwinDeviceCollection = queryCollectionResponse.getCollection();
        String continuationToken = queryCollectionResponse.getContinuationToken();

        // Re-run the same query using the saved continuation token. The results can be predicted since this test caused them
        QueryOptions options = new QueryOptions();
        options.setContinuationToken(continuationToken);
        options.setPageSize(PAGE_SIZE);
        QueryCollection twinQueryToReRun = sCDeviceTwin.queryTwinCollection(sqlQuery.getQuery());
        Collection<DeviceTwinDevice> continuedDeviceTwinDeviceQuery = sCDeviceTwin.next(twinQueryToReRun, options).getCollection();

        // Cleanup
        removeMultipleDevices(PAGE_SIZE + 1);

        // Assert
        assertEquals((long) PAGE_SIZE, queriedDeviceTwinDeviceCollection.size());
        assertEquals(1, continuedDeviceTwinDeviceQuery.size());

        // since order is not guaranteed, we cannot check that the third updated deviceTwinDevice is the third queried.
        // Instead, all we can check is that each updated device twin identity is in either the initial query or the continued query.
        ArrayList<String> expectedDeviceIds = new ArrayList<>();
        for (int deviceTwinDeviceIndex = 0; deviceTwinDeviceIndex < PAGE_SIZE + 1; deviceTwinDeviceIndex++)
        {
            expectedDeviceIds.add(devicesUnderTest[deviceTwinDeviceIndex].sCDeviceForTwin.getDeviceId());
        }

        Collection<DeviceTwinDevice> allQueriedDeviceTwinDevices = new ArrayList<>();
        allQueriedDeviceTwinDevices.addAll(continuedDeviceTwinDeviceQuery);
        continuedDeviceTwinDeviceQuery.addAll(queriedDeviceTwinDeviceCollection);

        for (DeviceTwinDevice deviceTwinDevice : allQueriedDeviceTwinDevices)
        {
            if (!expectedDeviceIds.contains(deviceTwinDevice.getDeviceId()))
            {
                fail("Missing deviceTwinDevice: continuation token did not continue query where expected");
            }
        }
    }

    @Test
    public void queryCollectionCanReturnEmptyQueryResults() {
        try
        {
            String fullQuery = "select * from devices where deviceId='nonexistantdevice'";
            DeviceTwin twinClient = DeviceTwin.createFromConnectionString(iotHubConnectionString);
            QueryCollection twinQuery = twinClient.queryTwinCollection(fullQuery);
            QueryOptions options = new QueryOptions();
            QueryCollectionResponse<DeviceTwinDevice> response = twinClient.next(twinQuery, options);

            assertNull(response.getContinuationToken());
            assertTrue(response.getCollection().isEmpty());
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testMultipleQueryTwinInParallel() throws IOException, InterruptedException, IotHubException, NoSuchAlgorithmException, URISyntaxException, ModuleClientException
    {
        addMultipleDevices(MAX_DEVICES);

        // Add same desired on multiple devices
        final String queryProperty = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyEven = PROPERTY_KEY_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValue = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        final String queryPropertyValueEven = PROPERTY_VALUE_QUERY + UUID.randomUUID().toString();
        int noOfEvenDevices = 0;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < MAX_DEVICES; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            if (i % 2 == 0)
            {
                desiredProperties.add(new Pair(queryPropertyEven, queryPropertyValueEven));
                noOfEvenDevices++;
            }
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }

        // Query multiple devices having same property

        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final String where = "is_defined(properties.desired." + queryProperty + ")";
                    SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, where, null);
                    final Query twinQuery = sCDeviceTwin.queryTwin(sqlQuery.getQuery(), PAGE_SIZE);

                    for (int i = 0; i < MAX_DEVICES; i++)
                    {
                        try
                        {
                            if (sCDeviceTwin.hasNextDeviceTwin(twinQuery))
                            {
                                DeviceTwinDevice d = sCDeviceTwin.getNextDeviceTwin(twinQuery);

                                assertNotNull(d.getVersion());
                                for (Pair dp : d.getDesiredProperties())
                                {
                                    assertEquals(dp.getKey(), queryProperty);
                                    assertEquals(dp.getValue(), queryPropertyValue);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            fail(e.getMessage());
                        }

                        assertFalse(sCDeviceTwin.hasNextDeviceTwin(twinQuery));
                    }
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }
            }
        });

        final int maximumEvenDevices = noOfEvenDevices;
        executor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    final String whereEvenDevices = "is_defined(properties.desired." + queryPropertyEven + ")";
                    SqlQuery sqlQueryEvenDevices = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, whereEvenDevices, null);
                    final Query twinQueryEven = sCDeviceTwin.queryTwin(sqlQueryEvenDevices.getQuery(), PAGE_SIZE);

                    for (int i = 0; i < maximumEvenDevices; i++)
                    {
                        try
                        {
                            if (sCDeviceTwin.hasNextDeviceTwin(twinQueryEven))
                            {
                                DeviceTwinDevice d = sCDeviceTwin.getNextDeviceTwin(twinQueryEven);

                                assertNotNull(d.getVersion());
                                for (Pair dp : d.getDesiredProperties())
                                {
                                    assertEquals(dp.getKey(), queryPropertyEven);
                                    assertEquals(dp.getValue(), queryPropertyValueEven);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            fail(e.getMessage());
                        }

                        assertFalse(sCDeviceTwin.hasNextDeviceTwin(twinQueryEven));
                    }
                }
                catch (Exception e)
                {
                    fail(e.getMessage());
                }
            }
        });

        executor.shutdown();
        if (!executor.awaitTermination(MULTITHREADED_WAIT_TIMEOUT_MS, TimeUnit.MILLISECONDS))
        {
            executor.shutdownNow();
        }
        removeMultipleDevices(MAX_DEVICES);
    }

    public void setDesiredProperties(String queryProperty, String queryPropertyValue, int numberOfDevices) throws IOException, IotHubException
    {
        for (int i = 0; i < numberOfDevices; i++)
        {
            Set<Pair> desiredProperties = new HashSet<>();
            desiredProperties.add(new Pair(queryProperty, queryPropertyValue));
            devicesUnderTest[i].sCDeviceForTwin.setDesiredProperties(desiredProperties);

            sCDeviceTwin.updateTwin(devicesUnderTest[i].sCDeviceForTwin);
            devicesUnderTest[i].sCDeviceForTwin.clearTwin();
        }
    }
}
