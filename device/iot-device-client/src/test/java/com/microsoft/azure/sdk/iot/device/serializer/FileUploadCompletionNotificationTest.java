// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.serializer;

import com.microsoft.azure.sdk.iot.device.Helpers;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for File Upload Status serializer
 * 100% methods, 100% lines covered
 */
public class FileUploadCompletionNotificationTest
{
    private static final String VALID_CORRELATION_ID = "somecorrelationid";
    private static final String INVALID_CORRELATION_ID = "some\u1234correlationid";
    private static final String VALID_STATUS_DESCRIPTION = "Description of status";
    private static final String INVALID_STATUS_DESCRIPTION = "some\u1234 description of status";
    private static final Boolean VALID_IS_SUCCESS = true;
    private static final Integer VALID_STATUS_CODE = 200;

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the expected assertion param can be passed any value.
    private static void assertFileUploadStatus(FileUploadCompletionNotification fileUploadStatusParser, String expectedCorrelationId, Boolean expectedIsSuccess, Integer expectedStatusCode, String expectedStatusDescription)
    {
        assertNotNull(fileUploadStatusParser);

        String correlationId = Deencapsulation.getField(fileUploadStatusParser, "correlationId");
        Boolean isSuccess = Deencapsulation.getField(fileUploadStatusParser, "isSuccess");
        Integer statusCode = Deencapsulation.getField(fileUploadStatusParser, "statusCode");
        String statusDescription = Deencapsulation.getField(fileUploadStatusParser, "statusDescription");

        assertEquals(expectedCorrelationId, correlationId);
        assertEquals(expectedIsSuccess, isSuccess);
        assertEquals(expectedStatusCode, statusCode);
        assertEquals(expectedStatusDescription, statusDescription);
    }

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private static String createJson(String correlationId, Boolean isSuccess, Integer statusCode, String statusDescription)
    {
        return "{\n" +
                "    \"correlationId\": " + (correlationId == null ? "null" : "\"" + correlationId + "\"") + ",\n" +
                "    \"isSuccess\": " + isSuccess + ",\n" +
                "    \"statusCode\": " + statusCode + ",\n" +
                "    \"statusDescription\": " + (statusDescription == null ? "null" : "\"" + statusDescription + "\"") + "\n" +
                "}";
    }

    private static class TestParameters
    {
        String correlationId;
        Boolean isSuccess;
        Integer statusCode;
        String statusDescription;
    }
    private static final TestParameters[] tests = new TestParameters[]
    {
            new TestParameters(){{ correlationId = null; isSuccess = true; statusCode = 200;  statusDescription = VALID_STATUS_DESCRIPTION; }},
            new TestParameters(){{ correlationId = ""; isSuccess = true; statusCode = 200;  statusDescription = VALID_STATUS_DESCRIPTION; }},
            new TestParameters(){{ correlationId = INVALID_CORRELATION_ID; isSuccess = true; statusCode = 200; statusDescription = VALID_STATUS_DESCRIPTION; }},

            new TestParameters(){{ correlationId = VALID_CORRELATION_ID; isSuccess = null; statusCode = 200; statusDescription = VALID_STATUS_DESCRIPTION; }},

            new TestParameters(){{ correlationId = VALID_CORRELATION_ID; isSuccess = true; statusCode = null; statusDescription = VALID_STATUS_DESCRIPTION; }},

            new TestParameters(){{ correlationId = VALID_CORRELATION_ID; isSuccess = true; statusCode = 200; statusDescription = null; }},
            new TestParameters(){{ correlationId = VALID_CORRELATION_ID; isSuccess = true; statusCode = 200; statusDescription = ""; }},
            new TestParameters(){{ correlationId = VALID_CORRELATION_ID; isSuccess = true; statusCode = 200; statusDescription = INVALID_STATUS_DESCRIPTION; }},
    };
    
    @Test
    public void constructor_succeed()
    {
        // act
        FileUploadCompletionNotification fileUploadStatusParser = new FileUploadCompletionNotification(VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);

        // assert
        assertFileUploadStatus(fileUploadStatusParser, VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);
    }

    @Test
    public void toJson_succeed()
    {
        // arrange
        FileUploadCompletionNotification fileUploadStatusParser = new FileUploadCompletionNotification(VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);
        String expectedJson = createJson(VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);

        // act
        String json = fileUploadStatusParser.toJson();

        // assert
        Helpers.assertJson(json, expectedJson);
    }
}
