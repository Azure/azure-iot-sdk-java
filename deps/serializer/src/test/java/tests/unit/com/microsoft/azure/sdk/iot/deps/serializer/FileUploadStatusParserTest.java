// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadStatusParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for File Upload Status serializer
 */
public class FileUploadStatusParserTest
{
    private static final String VALID_CORRELATION_ID = "somecorrelationid";
    private static final String INVALID_CORRELATION_ID = "some\u1234correlationid";
    private static final String VALID_STATUS_DESCRIPTION = "Description of status";
    private static final String INVALID_STATUS_DESCRIPTION = "some\u1234 description of status";
    private static final Boolean VALID_IS_SUCCESS = true;
    private static final Integer VALID_STATUS_CODE = 200;

    private static void assertFileUploadStatus(FileUploadStatusParser fileUploadStatusParser, String expectedCorrelationId, Boolean expectedIsSuccess, Integer expectedStatusCode, String expectedStatusDescription)
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
    
    /* Tests_SRS_FILE_UPLOAD_STATUS_21_001: [The constructor shall create an instance of the FileUploadStatusParser.] */
    /* Tests_SRS_FILE_UPLOAD_STATUS_21_002: [The constructor shall set the `correlationId`, `isSuccess`, `statusCode`, and `statusDescription` in the new class with the provided parameters.] */
    @Test
    public void constructor_succeed()
    {
        // act
        FileUploadStatusParser fileUploadStatusParser = new FileUploadStatusParser(VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);

        // assert
        assertFileUploadStatus(fileUploadStatusParser, VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);
    }

    /* Tests_SRS_FILE_UPLOAD_STATUS_21_003: [If one of the provided parameters is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test
    public void constructor_failed()
    {
        for (TestParameters test:tests)
        {
            // act
            try
            {
                new FileUploadStatusParser(test.correlationId, test.isSuccess, test.statusCode, test.statusDescription);
                System.out.println("Test failed: correlationId=" + test.correlationId + ", isSuccess=" + test.isSuccess +
                        ", statusCode=" + test.statusCode + ", statusDescription=" + test.statusDescription);
                assert false;
            }
            catch (IllegalArgumentException expected)
            {
                // Don't do anything, expected throw.
            }
        }
    }

    /* Tests_SRS_FILE_UPLOAD_STATUS_21_004: [The toJson shall return a string with a json that represents the contend of the FileUploadStatusParser.] */
    @Test
    public void toJson_succeed()
    {
        // arrange
        FileUploadStatusParser fileUploadStatusParser = new FileUploadStatusParser(VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);
        String expectedJson = createJson(VALID_CORRELATION_ID, VALID_IS_SUCCESS, VALID_STATUS_CODE, VALID_STATUS_DESCRIPTION);

        // act
        String json = fileUploadStatusParser.toJson();

        // assert
        Helpers.assertJson(json, expectedJson);
    }
}
