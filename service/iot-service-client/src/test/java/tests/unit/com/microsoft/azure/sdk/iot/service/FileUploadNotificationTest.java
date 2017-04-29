/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.service;

import com.microsoft.azure.sdk.iot.service.FileUploadNotification;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class FileUploadNotificationTest
{
    //Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_001: [ The getter for device ID ]
    final String mockDeviceId = "anyDeviceID";
    final String mockBlobUri = "anyUri";
    final String mockBlobName = "anyBlobName";
    final Date mockLastUpdatedTimeDate = new Date();
    final Long mockBlobSizeInBytes = 10L;
    final Date mockEnqueuedTimeUtcDate = new Date();

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_001: [** The constructor shall save all the parameters only if they are valid **]
    @Test
    public void constructorSavesParameters() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullDevID() throws IOException
    {
        //arrange
        final String actualDeviceId = null;
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyDevID() throws IOException
    {
        //arrange
        final String actualDeviceId = "";
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullBlobUri() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = null;
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyBlobUri() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = "";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullBlobName() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = null;
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyBlobName() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullDate() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = null;
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullSize() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = null;
        final Date actualEnqueuedTimeUtcDate = new Date();

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_002: [** If any of the parameters are null or empty then this method shall throw IllegalArgumentException.**]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullEnqueuedTime() throws IOException
    {
        //arrange
        final String actualDeviceId = null;
        final String actualBlobUri = "TestblobUri";
        final String actualBlobName = "TestBlobName";
        final Date actualLastUpdatedTimeDate = new Date();
        final Long actualBlobSizeInBytes = 10000L;
        final Date actualEnqueuedTimeUtcDate = null;

        //act
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, actualBlobUri, actualBlobName, actualLastUpdatedTimeDate, actualBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //assert
        assertEquals(testFileUploadNotification.getDeviceId(), actualDeviceId);
        assertEquals(testFileUploadNotification.getBlobName(), actualBlobName);
        assertEquals(testFileUploadNotification.getBlobUri(), actualBlobUri);
        assertEquals(testFileUploadNotification.getLastUpdatedTimeDate(), actualLastUpdatedTimeDate);
        assertEquals(testFileUploadNotification.getBlobSizeInBytes(), actualBlobSizeInBytes);
        assertEquals(testFileUploadNotification.getEnqueuedTimeUtcDate(), actualEnqueuedTimeUtcDate);
    }

    // Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_003: [** The getter for device ID **]
    @Test
    public void getDeviceIdGets() throws IOException
    {
        //arrange
        final String actualDeviceId = "TestDeviceId";
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(actualDeviceId, mockBlobUri, mockBlobName, mockLastUpdatedTimeDate, mockBlobSizeInBytes, mockEnqueuedTimeUtcDate);

        //act
        final String testDeviceId = testFileUploadNotification.getDeviceId();

        //assert
        assertEquals(testDeviceId, actualDeviceId);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_003: [ The getter for Blob Uri ]
    @Test
    public void getBlobUriGets() throws IOException
    {
        //arrange
        final String actualBlobUri = "TestblobUri";
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(mockDeviceId, actualBlobUri, mockBlobName, mockLastUpdatedTimeDate, mockBlobSizeInBytes, mockEnqueuedTimeUtcDate);

        //act
        final String testBlobUri = testFileUploadNotification.getBlobUri();

        //assert
        assertEquals(testBlobUri, actualBlobUri);

    }

    //Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_005: [ The getter for blobName ]
    @Test
    public void getBlobNameGets() throws IOException
    {
        //arrange
        final String actualBlobName = "TestBlobName";
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(mockDeviceId, mockBlobUri, actualBlobName, mockLastUpdatedTimeDate, mockBlobSizeInBytes, mockEnqueuedTimeUtcDate);

        //act
        final String testBlobName = testFileUploadNotification.getBlobName();

        //assert
        assertEquals(testBlobName, actualBlobName);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_007: [ The getter for lastUpdatedTimeDate ]
    @Test
    public void getLastUpdatedTimeDateGets() throws IOException
    {
        //arrange
        final Date actualLastUpdatedTimeDate = new Date();
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(mockDeviceId, mockBlobUri, mockBlobName, actualLastUpdatedTimeDate, mockBlobSizeInBytes, mockEnqueuedTimeUtcDate);

        //act
        Date testLastUpdatedTimeDate = testFileUploadNotification.getLastUpdatedTimeDate();

        //assert
        assertEquals(testLastUpdatedTimeDate, actualLastUpdatedTimeDate);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_009: [ The getter for blobSizeInBytes ]
    @Test
    public void getBlobSizeInBytesGets() throws IOException
    {
        //arrange
        final Long actualBlobSizeInBytes = 10000L;
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(mockDeviceId, mockBlobUri, mockBlobName, mockLastUpdatedTimeDate, actualBlobSizeInBytes, mockEnqueuedTimeUtcDate);

        //act
        Long testBlobSizeInBytes = testFileUploadNotification.getBlobSizeInBytes();

        //assert
        assertEquals(testBlobSizeInBytes, actualBlobSizeInBytes);
    }

    //Tests_SRS_SERVICE_SDK_JAVA_FILEUPLOADNOTIFICATION_25_011: [ The getter for enqueuedTimeUtcDate ]
    @Test
    public void getEnqueuedTimeUtcDateGets() throws IOException
    {
        //arrange
        final Date actualEnqueuedTimeUtcDate = new Date();
        FileUploadNotification testFileUploadNotification = new FileUploadNotification(mockDeviceId, mockBlobUri, mockBlobName, mockLastUpdatedTimeDate, mockBlobSizeInBytes, actualEnqueuedTimeUtcDate);

        //act
        Date testEnqueuedTimeUtcDate = testFileUploadNotification.getEnqueuedTimeUtcDate();

        //assert
        assertEquals(testEnqueuedTimeUtcDate, actualEnqueuedTimeUtcDate);
    }

}
