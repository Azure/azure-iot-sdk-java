/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.parser;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.ProvisioningErrorParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class ProvisioningErrorParserTest
{
    //Tests_SRS_PROVISIONING_ERROR_PARSER_34_001: [This function shall create a ProvisioningErrorParser instance from the provided json]
    @Test
    public void createFromJsonWorks()
    {
        //arrange
        final int statusCode = 200;
        final String trackingIdValue = "some tracking id";
        final String messageValue = "This is an error message!";
        final String provisioningErrorParserJson = "{\"errorCode\" : " + statusCode + ", \"trackingId\" : \"" + trackingIdValue +"\", \"message\" : \"" + messageValue + "\"}";

        //act
        ProvisioningErrorParser errorParser = ProvisioningErrorParser.createFromJson(provisioningErrorParserJson);

        //assert
        assertEquals(statusCode, (int) Deencapsulation.getField(errorParser, "errorCode"));
        assertEquals(trackingIdValue, Deencapsulation.getField(errorParser, "trackingId"));
        assertEquals(messageValue, Deencapsulation.getField(errorParser, "message"));
    }

    //Tests_SRS_PROVISIONING_ERROR_PARSER_34_002: [This function shall return a string containing the saved error code, message, and tracking id]
    @Test
    public void getExceptionMessageContainsErrorCodeTrackingIdAndMessage()
    {
        //arrange
        final int statusCode = 200;
        final String trackingIdValue = "some tracking id";
        final String messageValue = "This is an error message!";
        final String provisioningErrorParserJson = "{\"errorCode\" : " + statusCode + ", \"trackingId\" : \"" + trackingIdValue +"\", \"message\" : \"" + messageValue + "\"}";

        ProvisioningErrorParser errorParser = ProvisioningErrorParser.createFromJson(provisioningErrorParserJson);

        //act
        String errorMessage = errorParser.getExceptionMessage();

        //assert
        assertTrue(errorMessage.contains(String.valueOf(statusCode)));
        assertTrue(errorMessage.contains(messageValue));
        assertTrue(errorMessage.contains(trackingIdValue));
    }
}
