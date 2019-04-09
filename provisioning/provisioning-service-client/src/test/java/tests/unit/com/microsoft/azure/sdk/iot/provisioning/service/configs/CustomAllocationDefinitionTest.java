// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.microsoft.azure.sdk.iot.provisioning.service.configs.CustomAllocationDefinition;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

/**
 * Unit tests for CustomAllocationDefinition
 * 100% methods, 100% lines covered
 */
public class CustomAllocationDefinitionTest
{
    private static final String expectedWebhookUrl = "https://www.microsoft.com";
    private static final String expectedApiVersion = "2019-03-31";

    //Tests_SRS_CUSTOM_ALLOCATION_DEFINITION_34_001: [This function shall return the saved webhook uri.]
    @Test
    public void getWebhookUrlGets()
    {
        //arrange
        CustomAllocationDefinition customAllocationDefinition = new CustomAllocationDefinition();
        Deencapsulation.setField(customAllocationDefinition, "webhookUrl", expectedWebhookUrl);

        //act
        String actualWebHookUrl = customAllocationDefinition.getWebhookUrl();

        //assert
        assertEquals(expectedWebhookUrl, actualWebHookUrl);
    }

    //Tests_SRS_CUSTOM_ALLOCATION_DEFINITION_34_002: [This function shall save the provided webhook uri.]
    @Test
    public void setWebhookUrlSets()
    {
        //arrange
        CustomAllocationDefinition customAllocationDefinition = new CustomAllocationDefinition();

        //act
        customAllocationDefinition.setWebhookUrl(expectedWebhookUrl);

        //assert
        assertEquals(expectedWebhookUrl, Deencapsulation.getField(customAllocationDefinition, "webhookUrl"));
    }

    //Tests_SRS_CUSTOM_ALLOCATION_DEFINITION_34_003: [This function shall return the saved api version.]
    @Test
    public void getApiVersionGets()
    {
        //arrange
        CustomAllocationDefinition customAllocationDefinition = new CustomAllocationDefinition();
        Deencapsulation.setField(customAllocationDefinition, "apiVersion", expectedApiVersion);

        //act
        String actualApiVersion = customAllocationDefinition.getApiVersion();

        //assert
        assertEquals(expectedApiVersion, actualApiVersion);
    }

    //Tests_SRS_CUSTOM_ALLOCATION_DEFINITION_34_004: [This function shall save the provided api version.]
    @Test
    public void setApiVersionSets()
    {
        //arrange
        CustomAllocationDefinition customAllocationDefinition = new CustomAllocationDefinition();

        //act
        customAllocationDefinition.setApiVersion(expectedApiVersion);

        //assert
        assertEquals(expectedApiVersion, Deencapsulation.getField(customAllocationDefinition, "apiVersion"));
    }
}
