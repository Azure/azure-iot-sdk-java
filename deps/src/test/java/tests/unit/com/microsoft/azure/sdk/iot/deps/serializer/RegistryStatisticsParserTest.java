// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.RegistryStatisticsParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Code coverage:
 * Methods: 100%
 * Lines: 100%
 */
public class RegistryStatisticsParserTest
{
    //Tests_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_001: [This method shall return a json representation of this.]
    //Tests_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_002: [This constructor shall create and return an instance of a RegistryStatisticsParser object based off the provided json.]
    @Test
    public void testBasicFunctionality()
    {
        //arrange
        RegistryStatisticsParser parser = Deencapsulation.newInstance(RegistryStatisticsParser.class);
        parser.setTotalDeviceCount(2);
        parser.setEnabledDeviceCount(2);
        parser.setDisabledDeviceCount(0);

        //act
        RegistryStatisticsParser processedParser = new RegistryStatisticsParser(parser.toJson());

        //assert
        assertEquals(parser.getTotalDeviceCount(), processedParser.getTotalDeviceCount());
        assertEquals(parser.getEnabledDeviceCount(), processedParser.getEnabledDeviceCount());
        assertEquals(parser.getDisabledDeviceCount(), processedParser.getDisabledDeviceCount());
    }

    //Tests_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_003: [If the provided json is null, empty, or cannot be parsed into a RegistryStatisticsParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void nullJsonForConstructorThrows()
    {
        //act
        new RegistryStatisticsParser(null);
    }

    //Tests_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_003: [If the provided json is null, empty, or cannot be parsed into a RegistryStatisticsParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void emptyJsonForConstructorThrows()
    {
        //act
        new RegistryStatisticsParser("");
    }

    //Tests_SRS_REGISTRY_STATISTICS_PROPERTIES_PARSER_34_003: [If the provided json is null, empty, or cannot be parsed into a RegistryStatisticsParser object, an IllegalArgumentException shall be thrown.]
    @Test (expected = IllegalArgumentException.class)
    public void invalidJsonForConstructorThrows()
    {
        //act
        new RegistryStatisticsParser("}");
    }

    //Tests_SRS_JOB_PROPERTIES_PARSER_34_004: [This method shall set the value of this object's totalDeviceCount equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_005: [This method shall return the value of this object's totalDeviceCount.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_006: [This method shall set the value of this object's enabledDeviceCount equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_007: [This method shall return the value of this object's enabledDeviceCount.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_008: [This method shall set the value of this object's disabledDeviceCount equal to the provided value.]
    //Tests_SRS_JOB_PROPERTIES_PARSER_34_009: [This method shall return the value of this object's disabledDeviceCount.]
    @Test
    public void testGettersAndSetters()
    {
        //arrange
        RegistryStatisticsParser parser = new RegistryStatisticsParser();
        int expectedTotalDevices = 23;
        int expectedEnabledDevices = 11;
        int expectedDisabledDevices = 12;

        //act
        parser.setTotalDeviceCount(expectedTotalDevices);
        parser.setEnabledDeviceCount(expectedEnabledDevices);
        parser.setDisabledDeviceCount(expectedDisabledDevices);

        //assert
        assertEquals(expectedTotalDevices, parser.getTotalDeviceCount());
        assertEquals(expectedEnabledDevices, parser.getEnabledDeviceCount());
        assertEquals(expectedDisabledDevices, parser.getDisabledDeviceCount());
    }
}
