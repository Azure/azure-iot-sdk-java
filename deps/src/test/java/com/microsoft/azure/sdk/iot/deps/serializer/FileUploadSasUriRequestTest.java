// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.deps.Helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for File Upload Request serializer
 * 100% methods, 100% lines covered
 */
public class FileUploadSasUriRequestTest
{
    private static final String VALID_BLOB_NAME = "test-device1/image.jpg";
    private static final String INVALID_BLOB_NAME = "\u1234 test-device1/image.jpg";

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the expected assertion param can be passed any value.
    private static void assertFileUploadRequest(FileUploadSasUriRequest fileUploadSasUriRequest, String expectedBlobName)
    {
        assertNotNull(fileUploadSasUriRequest);

        String blobName = Deencapsulation.getField(fileUploadSasUriRequest, "blobName");
        assertEquals(expectedBlobName, blobName);
    }

    @Test
    public void constructor_succeed()
    {
        // act
        FileUploadSasUriRequest fileUploadSasUriRequest = new FileUploadSasUriRequest(VALID_BLOB_NAME);

        // assert
        assertFileUploadRequest(fileUploadSasUriRequest, VALID_BLOB_NAME);
    }

    @Test
    public void toJson_succeed()
    {
        // arrange
        FileUploadSasUriRequest fileUploadSasUriRequest = new FileUploadSasUriRequest(VALID_BLOB_NAME);
        assertFileUploadRequest(fileUploadSasUriRequest, VALID_BLOB_NAME);

        // act
        String json = fileUploadSasUriRequest.toJson();

        // assert
        Helpers.assertJson("{\"blobName\":\"" + VALID_BLOB_NAME + "\"}", json);
    }
}
