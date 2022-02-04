// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.registry;

import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.registry.serializers.RegistryStatisticsParser;
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

    @Test
    public void constructorSucceeds(@Mocked RegistryClient mockedRegistryClient) throws IOException, IotHubException
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryClient registryClient = new RegistryClient(connectionString);

        // act
        RegistryStatistics statistics = registryClient.getStatistics();
    }

    @Test
    public void gettersSucceeds(@Mocked RegistryClient mockedRegistryClient) throws IOException, IotHubException
    {
        //arrange
        String connectionString = "HostName=aaa.bbb.ccc;SharedAccessKeyName=XXX;SharedAccessKey=YYY";
        RegistryClient registryClient = new RegistryClient(connectionString);
        RegistryStatistics statistics = registryClient.getStatistics();

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
