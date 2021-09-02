// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.deps.serializer.RegistryStatisticsParser;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryStatistics;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import mockit.Deencapsulation;
import mockit.Mocked;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

/**
 * Code Coverage
 * Methods: 100%
 * Lines: 100%
 */
public class RegistryStatisticsTest
{
    //Tests_SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_001: [This method shall convert the provided parser into a RegistryStatistics object and return it]
    @Test
    public void fromRegistryStatisticsParser()
    {
        // arrange
        RegistryStatisticsParser parser = new RegistryStatisticsParser();
        parser.setTotalDeviceCount(20);
        parser.setEnabledDeviceCount(15);
        parser.setDisabledDeviceCount(5);

        // act
        RegistryStatistics registryStatistics = Deencapsulation.newInstance(RegistryStatistics.class, new Class[] {RegistryStatisticsParser.class}, parser);

        // assert
        assertEquals(parser.getTotalDeviceCount(), registryStatistics.getTotalDeviceCount());
        assertEquals(parser.getEnabledDeviceCount(), registryStatistics.getEnabledDeviceCount());
        assertEquals(parser.getDisabledDeviceCount(), registryStatistics.getDisabledDeviceCount());
    }

    //Tests_SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_002: [This method shall convert this into a RegistryStatisticsParser object and return it.]
    @Test
    public void testToRegistryStatisticsParser()
    {
        // arrange
        RegistryStatisticsParser expectedParser = new RegistryStatisticsParser();
        expectedParser.setTotalDeviceCount(10);
        expectedParser.setEnabledDeviceCount(7);
        expectedParser.setDisabledDeviceCount(3);

        RegistryStatistics registryStatistics = Deencapsulation.newInstance(RegistryStatistics.class, new Class[] {});
        Deencapsulation.setField(registryStatistics, "totalDeviceCount", 10);
        Deencapsulation.setField(registryStatistics, "enabledDeviceCount", 7);
        Deencapsulation.setField(registryStatistics, "disabledDeviceCount", 3);

        // act
        RegistryStatisticsParser actualParser = Deencapsulation.invoke(registryStatistics, "toRegistryStatisticsParser", new Class[]{});

        // assert
        assertEquals(expectedParser.getTotalDeviceCount(), actualParser.getTotalDeviceCount());
        assertEquals(expectedParser.getEnabledDeviceCount(), actualParser.getEnabledDeviceCount());
        assertEquals(expectedParser.getDisabledDeviceCount(), actualParser.getDisabledDeviceCount());
    }

    @Test
    public void constructorSucceeds(@Mocked RegistryManager mockedRegistryManager) throws IOException, IotHubException
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);

        // act
        RegistryStatistics statistics = registryManager.getStatistics();
    }

    @Test
    public void gettersSucceeds(@Mocked RegistryManager mockedRegistryManager) throws IOException, IotHubException
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryManager registryManager = RegistryManager.createFromConnectionString(connectionString);
        RegistryStatistics statistics = registryManager.getStatistics();

        //act
        final long disabledDeviceCount = statistics.getDisabledDeviceCount();
        final long enabledDeviceCount = statistics.getEnabledDeviceCount();
        final long totalDeviceCount = statistics.getTotalDeviceCount();

        //assert
        Assert.assertEquals(disabledDeviceCount, 0);
        Assert.assertEquals(enabledDeviceCount, 0);
        Assert.assertEquals(totalDeviceCount, 0);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_REGISTRY_STATISTICS_34_003: [If the provided RegistryStatisticsParser object is null, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorRejectsNullParser()
    {
        //act
        Deencapsulation.newInstance(RegistryStatistics.class, new Class[] {RegistryStatisticsParser.class}, null);
    }
}
