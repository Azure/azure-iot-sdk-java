// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.serializer;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadNotificationParser;
import mockit.Deencapsulation;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for File Upload Notification deserializer
 */
public class FileUploadNotificationParserTest
{
    private static final String VALID_DEVICEID = "test-device1";
    private static final String INVALID_DEVICEID = "\u1234test-device1";
    private static final String VALID_BLOB_UTI = "https://storageaccount.blob.core.windows.net/containername/test-device1/image.jpg";
    private static final String INVALID_BLOB_URI = "https://\u1234storageaccount.blob.core.windows.net/containername/test-device1/image.jpg";
    private static final String VALID_BLOB_NAME = "test-device1/image.jpg";
    private static final String INVALID_BLOB_NAME = "\u1234test-device1/image.jpg";
    private static final String VALID_LAST_UPDATE_TIME = "2016-06-01T21:22:41+00:00";
    private static final long VALID_LAST_UPDATE_TIME_IN_MILLISECONDS = 1464816161000L;
    private static final String INVALID_LAST_UPDATE_TIME = "\u12342016-06-01T21:22:41+00:00";
    private static final String INVALID_DATETIME_OFFSET = "2016-06-40T21:22:41 00:00";
    private static final String VALID_ENQUEUED_TIME_UTC = "2016-06-01T21:22:43.7996883Z";
    private static final long VALID_ENQUEUED_TIME_UTC_IN_MILLISECONDS = 1464824159883L;
    private static final String INVALID_ENQUEUED_TIME_UTC = "\u12342016-06-01T21:22:43.7996883Z";
    private static final String INVALID_DATETIME_UTC = "2016-6-1T4:22:43.7996883";
    private static final Long VALID_BLOB_SIZE_IN_BYTES = 1234L;

    private static class TestParameters
    {
        String deviceId;
        String blobUri;
        String blobName;
        String lastUpdatedTime;
        String enqueuedTimeUtc;
        Long blobSizeInBytes;
    }
    private static final TestParameters[] tests = new TestParameters[]
    {
            new TestParameters(){{ deviceId = null; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = ""; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = INVALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},

            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = null; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = ""; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = INVALID_BLOB_URI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},

            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = null; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = ""; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = INVALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},

            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = null; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = ""; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = INVALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = INVALID_DATETIME_OFFSET; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},

            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = null; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = ""; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = INVALID_ENQUEUED_TIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},
            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = INVALID_DATETIME_UTC; blobSizeInBytes=VALID_BLOB_SIZE_IN_BYTES; }},

            new TestParameters(){{ deviceId = VALID_DEVICEID; blobUri = VALID_BLOB_UTI; blobName = VALID_BLOB_NAME; lastUpdatedTime = VALID_LAST_UPDATE_TIME; enqueuedTimeUtc = VALID_ENQUEUED_TIME_UTC; blobSizeInBytes=null; }},
    };

    private static void assertFileUploadNotification(FileUploadNotificationParser fileUploadNotificationParser,
                                                     String expectedDeviceId, String expectedBlobUri, String expectedBlobName,
                                                     String expectedLastUpdatedTime, String expectedEnqueuedTimeUtc, long expectedBlobSizeInBytes)
    {
        assertNotNull(fileUploadNotificationParser);

        String deviceId = Deencapsulation.getField(fileUploadNotificationParser, "deviceId");
        String blobUri = Deencapsulation.getField(fileUploadNotificationParser, "blobUri");
        String blobName = Deencapsulation.getField(fileUploadNotificationParser, "blobName");
        String lastUpdatedTime = Deencapsulation.getField(fileUploadNotificationParser, "lastUpdatedTime");
        String enqueuedTimeUtc = Deencapsulation.getField(fileUploadNotificationParser, "enqueuedTimeUtc");
        long blobSizeInBytes = Deencapsulation.getField(fileUploadNotificationParser, "blobSizeInBytes");

        assertEquals(expectedDeviceId, deviceId);
        assertEquals(expectedBlobUri, blobUri);
        assertEquals(expectedBlobName, blobName);
        assertEquals(expectedLastUpdatedTime, lastUpdatedTime);
        assertEquals(expectedEnqueuedTimeUtc, enqueuedTimeUtc);
        assertEquals(expectedBlobSizeInBytes, blobSizeInBytes);
    }

    private static String createJson(String deviceId, String blobUri, String blobName, String lastUpdatedTime, String enqueuedTimeUtc, Long blobSizeInBytes)
    {
        return "{\n" +
                "    \"deviceId\": " + (deviceId == null ? "null" : "\"" + deviceId + "\"") + ",\n" +
                "    \"blobUri\": " + (blobUri == null ? "null" : "\"" + blobUri + "\"") + ",\n" +
                "    \"blobName\": " + (blobName == null ? "null" : "\"" + blobName + "\"") + ",\n" +
                "    \"lastUpdatedTime\": " + (lastUpdatedTime == null ? "null" : "\"" + lastUpdatedTime + "\"") + ",\n" +
                "    \"blobSizeInBytes\": " + blobSizeInBytes + ",\n" +
                "    \"enqueuedTimeUtc\": " + (enqueuedTimeUtc == null ? "null" : "\"" + enqueuedTimeUtc + "\"") + "\n" +
                "}";
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_001: [The constructor shall create an instance of the FileUploadNotification.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_002: [The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.] */
    @Test
    public void constructor_json_succeed()
    {
        // arrange
        String validJson = createJson(VALID_DEVICEID, VALID_BLOB_UTI, VALID_BLOB_NAME, VALID_LAST_UPDATE_TIME, VALID_ENQUEUED_TIME_UTC, VALID_BLOB_SIZE_IN_BYTES);

        // act
        FileUploadNotificationParser fileUploadNotificationParser = new FileUploadNotificationParser(validJson);

        // assert
        assertFileUploadNotification(fileUploadNotificationParser, VALID_DEVICEID, VALID_BLOB_UTI, VALID_BLOB_NAME, VALID_LAST_UPDATE_TIME, VALID_ENQUEUED_TIME_UTC, VALID_BLOB_SIZE_IN_BYTES);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_001: [The constructor shall create an instance of the FileUploadNotification.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_002: [The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.] */
    @Test
    public void constructor_json_realCase_succeed()
    {
        // arrange
        String validJson = "{" +
                "\"deviceId\":\"test-device1\"," +
                "\"blobUri\":\"https://storageaccount.blob.core.windows.net/storage-accout-storageaccount/storageaccount-test/hello_world.txt\"," +
                "\"blobName\":\"storageaccount-test/hello_world.txt\"," +
                "\"lastUpdatedTime\":\"2017-05-01T23:29:11+00:00\"," +
                "\"blobSizeInBytes\":45," +
                "\"enqueuedTimeUtc\":\"2017-05-01T23:29:13.5700695Z\"}";

        // act
        FileUploadNotificationParser fileUploadNotificationParser = new FileUploadNotificationParser(validJson);

        // assert
        assertFileUploadNotification(fileUploadNotificationParser,
                "test-device1",
                "https://storageaccount.blob.core.windows.net/storage-accout-storageaccount/storageaccount-test/hello_world.txt",
                "storageaccount-test/hello_world.txt",
                "2017-05-01T23:29:11+00:00",
                "2017-05-01T23:29:13.5700695Z",
                45);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_002: [The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.] */
    @Test
    public void constructor_specialCase_string_null_succeed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"deviceId\": \"null\",\n" +
                "    \"blobUri\": \"" + VALID_BLOB_UTI + "\",\n" +
                "    \"blobName\": \"" + VALID_BLOB_NAME + "\",\n" +
                "    \"lastUpdatedTime\": \"" + VALID_LAST_UPDATE_TIME + "\",\n" +
                "    \"blobSizeInBytes\": " + VALID_BLOB_SIZE_IN_BYTES + ",\n" +
                "    \"enqueuedTimeUtc\": \"" + VALID_ENQUEUED_TIME_UTC + "\"\n" +
                "}";

        // act
        FileUploadNotificationParser fileUploadNotificationParser = new FileUploadNotificationParser(validJson);

        // assert
        assertFileUploadNotification(fileUploadNotificationParser, "null", VALID_BLOB_UTI, VALID_BLOB_NAME, VALID_LAST_UPDATE_TIME, VALID_ENQUEUED_TIME_UTC, VALID_BLOB_SIZE_IN_BYTES);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_null_json_failed()
    {
        // act
        new FileUploadNotificationParser(null);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_empty_json_failed()
    {
        // act
        new FileUploadNotificationParser("");
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_invalid_json_failed()
    {
        // act
        new FileUploadNotificationParser("{&*");
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_004: [If the provided json do not contains a valid `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test
    public void constructor_json_failed()
    {
        int counter = -1;
        for (TestParameters test:tests)
        {
            // arrange
            counter++;
            String invalidJson = createJson(test.deviceId, test.blobUri, test.blobName, test.lastUpdatedTime, test.enqueuedTimeUtc, test.blobSizeInBytes);

            // act
            try
            {
                new FileUploadNotificationParser(invalidJson);
                System.out.println("Test " + counter + " failed:");
                System.out.println(invalidJson);
                assert false;
            }
            catch (IllegalArgumentException expected)
            {
                // Don't do anything, expected throw.
            }
        }
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_json_missing_deviceId_failed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"blobUri\": \"" + VALID_BLOB_UTI + "\",\n" +
                "    \"blobName\": \"" + VALID_BLOB_NAME + "\",\n" +
                "    \"lastUpdatedTime\": \"" + VALID_LAST_UPDATE_TIME + "\",\n" +
                "    \"blobSizeInBytes\": " + VALID_BLOB_SIZE_IN_BYTES + ",\n" +
                "    \"enqueuedTimeUtc\": \"" + VALID_ENQUEUED_TIME_UTC + "\"\n" +
                "}";

        // act
        new FileUploadNotificationParser(validJson);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_json_missing_BlobUri_failed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"deviceId\": \"" + VALID_DEVICEID + "\",\n" +
                "    \"blobName\": \"" + VALID_BLOB_NAME + "\",\n" +
                "    \"lastUpdatedTime\": \"" + VALID_LAST_UPDATE_TIME + "\",\n" +
                "    \"blobSizeInBytes\": " + VALID_BLOB_SIZE_IN_BYTES + ",\n" +
                "    \"enqueuedTimeUtc\": \"" + VALID_ENQUEUED_TIME_UTC + "\"\n" +
                "}";

        // act
        new FileUploadNotificationParser(validJson);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_json_missing_BlobName_failed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"deviceId\": \"" + VALID_DEVICEID + "\",\n" +
                "    \"blobUri\": \"" + VALID_BLOB_UTI + "\",\n" +
                "    \"lastUpdatedTime\": \"" + VALID_LAST_UPDATE_TIME + "\",\n" +
                "    \"blobSizeInBytes\": " + VALID_BLOB_SIZE_IN_BYTES + ",\n" +
                "    \"enqueuedTimeUtc\": \"" + VALID_ENQUEUED_TIME_UTC + "\"\n" +
                "}";

        // act
        new FileUploadNotificationParser(validJson);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_json_missing_lastUpdateTime_failed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"deviceId\": \"" + VALID_DEVICEID + "\",\n" +
                "    \"blobUri\": \"" + VALID_BLOB_UTI + "\",\n" +
                "    \"blobName\": \"" + VALID_BLOB_NAME + "\",\n" +
                "    \"blobSizeInBytes\": " + VALID_BLOB_SIZE_IN_BYTES + ",\n" +
                "    \"enqueuedTimeUtc\": \"" + VALID_ENQUEUED_TIME_UTC + "\"\n" +
                "}";

        // act
        new FileUploadNotificationParser(validJson);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_json_missing_blobSize_failed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"deviceId\": \"" + VALID_DEVICEID + "\",\n" +
                "    \"blobUri\": \"" + VALID_BLOB_UTI + "\",\n" +
                "    \"blobName\": \"" + VALID_BLOB_NAME + "\",\n" +
                "    \"lastUpdatedTime\": \"" + VALID_LAST_UPDATE_TIME + "\",\n" +
                "    \"enqueuedTimeUtc\": \"" + VALID_ENQUEUED_TIME_UTC + "\"\n" +
                "}";

        // act
        new FileUploadNotificationParser(validJson);
    }

    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_005: [If the provided json do not contains one of the keys `deviceId`, `blobUri`, `blobName`, `lastUpdatedTime`, `enqueuedTimeUtc`, and `blobSizeInBytes`, the constructor shall throws IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructor_json_missing_enqueuedTimeUtc_failed()
    {
        // arrange
        String validJson = "{\n" +
                "    \"deviceId\": \"" + VALID_DEVICEID + "\",\n" +
                "    \"blobUri\": \"" + VALID_BLOB_UTI + "\",\n" +
                "    \"blobName\": \"" + VALID_BLOB_NAME + "\",\n" +
                "    \"lastUpdatedTime\": \"" + VALID_LAST_UPDATE_TIME + "\",\n" +
                "    \"blobSizeInBytes\": " + VALID_BLOB_SIZE_IN_BYTES + "\n" +
                "}";

        // act
        new FileUploadNotificationParser(validJson);
    }


    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_006: [The getDeviceId shall return the string stored in `deviceId`.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_007: [The getBlobUri shall return the string stored in `blobUri`.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_008: [The getBlobName shall return the string stored in `blobName`.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_009: [The getLastUpdateTime shall return the string stored in `lastUpdateTime`.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_010: [The getEnqueuedTimeUtc shall return the string stored in `enqueuedTimeUtc`.] */
    /* Tests_SRS_FILE_UPLOAD_NOTIFICATION_21_011: [The getBlobSizeInBytesTag shall return the integer stored in `blobSizeInBytes`.] */
    @Test
    public void getters_succeed()
    {
        // arrange
        String validJson = createJson(VALID_DEVICEID, VALID_BLOB_UTI, VALID_BLOB_NAME, VALID_LAST_UPDATE_TIME, VALID_ENQUEUED_TIME_UTC, VALID_BLOB_SIZE_IN_BYTES);
        FileUploadNotificationParser fileUploadNotificationParser = new FileUploadNotificationParser(validJson);
        Date expectedLastUpdatedTime = new Date(VALID_LAST_UPDATE_TIME_IN_MILLISECONDS);
        Date expectedEnqueuedTimeUtc = new Date(VALID_ENQUEUED_TIME_UTC_IN_MILLISECONDS);

        // act
        // assert
        assertEquals(VALID_DEVICEID, fileUploadNotificationParser.getDeviceId());
        assertEquals(VALID_BLOB_UTI, fileUploadNotificationParser.getBlobUri());
        assertEquals(VALID_BLOB_NAME, fileUploadNotificationParser.getBlobName());
        assertEquals(expectedLastUpdatedTime, fileUploadNotificationParser.getLastUpdatedTime());
        assertEquals(expectedEnqueuedTimeUtc, fileUploadNotificationParser.getEnqueuedTimeUtc());
        assertEquals(VALID_BLOB_SIZE_IN_BYTES, fileUploadNotificationParser.getBlobSizeInBytesTag());
    }

}
