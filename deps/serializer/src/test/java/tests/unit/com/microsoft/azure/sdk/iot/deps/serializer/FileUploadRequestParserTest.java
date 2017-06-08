// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadRequestParser;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for File Upload Request serializer
 * 100% methods, 100% lines covered
 */
public class FileUploadRequestParserTest
{
    private static final String VALID_BLOB_NAME = "test-device1/image.jpg";
    private static final String INVALID_BLOB_NAME = "\u1234 test-device1/image.jpg";

    private static void assertFileUploadRequest(FileUploadRequestParser fileUploadRequestParser, String expectedBlobName)
    {
        assertNotNull(fileUploadRequestParser);

        String blobName = Deencapsulation.getField(fileUploadRequestParser, "blobName");
        assertEquals(expectedBlobName, blobName);
    }

    /* Tests_SRS_FILE_UPLOAD_REQUEST_21_001: [The constructor shall create an instance of the FileUploadRequestParser.] */
    /* Tests_SRS_FILE_UPLOAD_REQUEST_21_002: [The constructor shall set the `blobName` in the new class with the provided blob name.] */
    @Test
    public void constructor_succeed()
    {
        // act
        FileUploadRequestParser fileUploadRequestParser = new FileUploadRequestParser(VALID_BLOB_NAME);

        // assert
        assertFileUploadRequest(fileUploadRequestParser, VALID_BLOB_NAME);
    }

    /* Tests_SRS_FILE_UPLOAD_REQUEST_21_003: [If the provided blob name is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_null_name_failed()
    {
        // act
        new FileUploadRequestParser(null);
    }

    /* Tests_SRS_FILE_UPLOAD_REQUEST_21_003: [If the provided blob name is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_empty_name_failed()
    {
        // act
        new FileUploadRequestParser("");
    }

    /* Tests_SRS_FILE_UPLOAD_REQUEST_21_003: [If the provided blob name is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_invalid_name_failed()
    {
        // act
        new FileUploadRequestParser(INVALID_BLOB_NAME);
    }

    /* Tests_SRS_FILE_UPLOAD_REQUEST_21_004: [The toJson shall return a string with a json that represents the contend of the FileUploadResponseParser.] */
    @Test
    public void toJson_succeed()
    {
        // arrange
        FileUploadRequestParser fileUploadRequestParser = new FileUploadRequestParser(VALID_BLOB_NAME);
        assertFileUploadRequest(fileUploadRequestParser, VALID_BLOB_NAME);

        // act
        String json = fileUploadRequestParser.toJson();

        // assert
        Helpers.assertJson("{\"blobName\":\"" + VALID_BLOB_NAME + "\"}", json);
    }
}
