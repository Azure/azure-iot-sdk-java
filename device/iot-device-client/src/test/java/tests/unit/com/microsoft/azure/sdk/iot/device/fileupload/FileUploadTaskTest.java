package tests.unit.com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadRequestParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadResponseParser;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadStatusParser;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.StorageExtendedErrorInformation;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for file upload task class.
 * 100% methods, 100% lines covered
 */
public class FileUploadTaskTest
{
    @Mocked
    private InputStream mockInputStream;

    @Mocked
    private IotHubEventCallback mockIotHubEventCallback;

    @Mocked
    private HttpsTransportManager mockHttpsTransportManager;

    @Mocked
    private FileUploadRequestParser mockFileUploadRequestParser;

    @Mocked
    private FileUploadResponseParser mockFileUploadResponseParser;

    @Mocked
    private FileUploadStatusParser mockFileUploadStatusParser;

    @Mocked
    private IotHubTransportMessage mockMessageRequest;

    @Mocked
    private IotHubTransportMessage mockMessageNotification;

    @Mocked
    private ResponseMessage mockResponseMessage;

    @Mocked
    private CloudBlockBlob mockCloudBlockBlob;

    private static final String VALID_BLOB_NAME = "test-device1/image.jpg";
    private static final String VALID_BLOB_NAME_URI = "test-device1%2Fimage.jpg";
    private static final String VALID_CORRELATION_ID = "somecorrelationid";
    private static final String VALID_HOST_NAME = "contoso.azure-devices.net";
    private static final String VALID_CONTAINER_NAME = "testcontainer";
    private static final String VALID_SAS_TOKEN = "1234asdfSAStoken";
    private static final String VALID_REQUEST_JSON = "{\"blobName\":\"" + VALID_BLOB_NAME + "\"}";
    private static final String VALID_RESPONSE_JSON =
            "{\n" +
                "\"correlationId\":\"" + VALID_CORRELATION_ID + "\",\n" +
                "\"hostname\":\"" + VALID_HOST_NAME + "\",\n" +
                "\"containerName\":\"" + VALID_CONTAINER_NAME + "\",\n" +
                "\"blobName\":\"" + VALID_BLOB_NAME + "\",\n" +
                "\"sasToken\":\"" + VALID_SAS_TOKEN + "\"\n" +
            "}";
    private static final String VALID_NOTIFICATION_JSON =
            "{\n" +
                "\"correlationId\":\"" + VALID_CORRELATION_ID + "\",\n" +
                "\"isSuccess\":true,\n" +
                "\"statusCode\":1234,\n" +
                "\"statusDescription\":\"Description of the status\"\n" +
            "}";
    private static final String VALID_URI_STRING = "https://" + VALID_HOST_NAME + "/" + VALID_CONTAINER_NAME + "/" + VALID_BLOB_NAME_URI + VALID_SAS_TOKEN;
    private static final long VALID_STREAM_LENGTH = 100;
    private static final Map<String, Object> VALID_CALLBACK_CONTEXT = new HashMap<>();




    private void requestExpectations(final String blobName, final String requestJson) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new FileUploadRequestParser(blobName);
                result = mockFileUploadRequestParser;
                mockFileUploadRequestParser.toJson();
                result = requestJson;
                new IotHubTransportMessage(requestJson);
                result = mockMessageRequest;
                mockHttpsTransportManager.send(mockMessageRequest, (Map) any);
                result = mockResponseMessage;
            }
        };
    }

    private void responseExpectations(final String responseJson)
    {
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK;
                mockResponseMessage.getBytes();
                result = responseJson.getBytes();
                new FileUploadResponseParser(responseJson);
                result = mockFileUploadResponseParser;
            }
        };
    }

    private void responseParserExpectations(final String blobName, final String correlationId, final String hostName, final String containerName, final String sasToken)
    {
        new NonStrictExpectations()
        {
            {
                mockFileUploadResponseParser.getBlobName();
                result = blobName;
                mockFileUploadResponseParser.getCorrelationId();
                result = correlationId;
                mockFileUploadResponseParser.getHostName();
                result = hostName;
                mockFileUploadResponseParser.getContainerName();
                result = containerName;
                mockFileUploadResponseParser.getSasToken();
                result = sasToken;
            }
        };
    }

    private void cloudExpectations() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        new NonStrictExpectations()
        {
            {
                new CloudBlockBlob((URI) any);
                result = mockCloudBlockBlob;
            }
        };
    }

    private void notificationExpectations(final String correlationId, final String notificationJson) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new FileUploadStatusParser(correlationId, true, 0, (String)any);
                result = mockFileUploadStatusParser;
                mockFileUploadStatusParser.toJson();
                result = notificationJson;
                new IotHubTransportMessage(notificationJson);
                result = mockMessageNotification;
                mockHttpsTransportManager.send(mockMessageNotification, (Map) any);
                result = mockResponseMessage;
            }
        };
    }

    private void failedNotificationExpectations(final String correlationId, final String notificationJson) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new FileUploadStatusParser(correlationId, false, -1, (String)any);
                result = mockFileUploadStatusParser;
                mockFileUploadStatusParser.toJson();
                result = notificationJson;
                new IotHubTransportMessage(notificationJson);
                result = mockMessageNotification;
                mockHttpsTransportManager.send(mockMessageNotification, (Map) any);
                result = mockResponseMessage;
            }
        };
    }

    private void expectSuccess(
            final String blobName, final String correlationId, final String hostName, final String containerName, final String sasToken,
            final String requestJson, final String responseJson, final String notificationJson)
            throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        requestExpectations(blobName, requestJson);
        responseExpectations(responseJson);
        responseParserExpectations(blobName, correlationId, hostName, containerName, sasToken);
        cloudExpectations();
        notificationExpectations(correlationId, notificationJson);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_001: [If the `blobName` is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullBlobNameThrows()
    {
        // arrange
        final String blobName = null;

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_001: [If the `blobName` is null or empty, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorEmptyBlobNameThrows()
    {
        // arrange
        final String blobName = "";

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                blobName, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_002: [If the `inputStream` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullInputStreamThrows()
    {
        // arrange

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, null, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_003: [If the `streamLength` is negative, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNegativeStreamLengthThrows()
    {
        // arrange
        final long streamLength = -100;

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, streamLength, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_004: [If the `httpsTransportManager` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullHttpsTransportManagerThrows()
    {
        // arrange

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, null, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_005: [If the `userCallback` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullUserCallbackThrows()
    {
        // arrange

        // act
        Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, null, VALID_CALLBACK_CONTEXT);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_006: [The constructor shall store all the provided parameters.] */
    @Test
    public void constructorStoreParametersSucceed()
    {
        // arrange

        // act
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // assert
        assertEquals(Deencapsulation.getField(fileUploadTask, "blobName"), VALID_BLOB_NAME);
        assertEquals(Deencapsulation.getField(fileUploadTask, "inputStream"), mockInputStream);
        assertEquals(Deencapsulation.getField(fileUploadTask, "streamLength"), VALID_STREAM_LENGTH);
        assertEquals(Deencapsulation.getField(fileUploadTask, "userCallback"), mockIotHubEventCallback);
        assertEquals(Deencapsulation.getField(fileUploadTask, "userCallbackContext"), VALID_CALLBACK_CONTEXT);
        assertEquals(Deencapsulation.getField(fileUploadTask, "httpsTransportManager"), mockHttpsTransportManager);
    }

    /* Tests_SRS_FILEUPLOADTASK_21_007: [The run shall create a FileUpload request message, by using the FileUploadRequestParser.] */
    @Test
    public void runCreateRequest() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_008: [The run shall set the message method as `POST`.] */
    @Test
    public void runSetPOSTForRequest() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMessageRequest, "setIotHubMethod", IotHubMethod.POST);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_010: [The run shall open the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_026: [The run shall open the connection with the iothub, using the httpsTransportManager.] */
    @Test
    public void runOpenConnectionToIothubForRequestAndNotification() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockHttpsTransportManager, "open");
                times = 2;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_011: [The run shall send the blob request message to the iothub, using the httpsTransportManager.] */
    @Test
    public void runSendRequestToIothub() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockHttpsTransportManager.sendFileUploadMessage(mockMessageRequest);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_012: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_028: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
    @Test
    public void runCloseConnectionToIothubForRequestAndNotification() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockHttpsTransportManager, "close");
                times = 2;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_015: [If the iothub accepts the request, it shall provide a `responseMessage` with the blob information with a correlationId.] */
    /* Tests_SRS_FILEUPLOADTASK_21_017: [The run shall parse and store the blobName and correlationId in the response, by use the FileUploadResponseParser.] */
    @Test
    public void runSetBlobNameAndCorrelationId() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockFileUploadResponseParser, "getCorrelationId");
                times = 1;
                Deencapsulation.invoke(mockFileUploadResponseParser, "getBlobName");
                times = 1;
            }
        };
        assertEquals(VALID_CORRELATION_ID, Deencapsulation.getField(fileUploadTask, "correlationId"));
        assertEquals(VALID_BLOB_NAME, Deencapsulation.getField(fileUploadTask, "blobName"));
    }

    /* Tests_SRS_FILEUPLOADTASK_21_015: [If the iothub accepts the request, it shall provide a `responseMessage` with the blob information with a correlationId.] */
    /* Tests_SRS_FILEUPLOADTASK_21_018: [The run shall create a blob URI `blobUri` with the format `https://[hostName]/[containerName]/[blobName,UTF-8][sasToken]`.] */
    @Test
    public void runCreatesBlobURI() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockFileUploadResponseParser, "getHostName");
                times = 1;
                Deencapsulation.invoke(mockFileUploadResponseParser, "getContainerName");
                times = 1;
                Deencapsulation.invoke(mockFileUploadResponseParser, "getSasToken");
                times = 1;
            }
        };
        assertEquals(VALID_URI_STRING, ((URI)Deencapsulation.getField(fileUploadTask, "blobURI")).toString());
    }

    /* Tests_SRS_FILEUPLOADTASK_21_019: [The run shall create a `CloudBlockBlob` using the `blobUri`.] */
    @Test
    public void runCreateCloudBlockBlob() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_020: [The run shall upload the `inputStream` with the `streamLength` to the created `CloudBlockBlob`.] */
    @Test
    public void runUploadStreamToCloudBlockBlob() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockCloudBlockBlob.upload(mockInputStream, VALID_STREAM_LENGTH);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_021: [If the upload to blob succeed, the run shall create a notification the IoT Hub with `isSuccess` equals true, `statusCode` equals 0.] */
    @Test
    public void runCreateNotificationSucceed() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_023: [The run shall create a FileUpload status notification message, by using the FileUploadStatusParser.] */
    @Test
    public void runCreateNotification() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_024: [The run shall set the message method as `POST`.] */
    @Test
    public void runSetPOSTForNotification() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockMessageNotification, "setIotHubMethod", IotHubMethod.POST);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_027: [The run shall send the blob request message to the iothub, using the httpsTransportManager.] */
    @Test
    public void runSendNotificationToIothub() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockHttpsTransportManager.sendFileUploadNotification(mockMessageNotification);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_029: [The run shall call the `userCallback` with the final response status.] */
    @Test
    public void runCallUserCallback() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        expectSuccess(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN,
                VALID_REQUEST_JSON, VALID_RESPONSE_JSON, VALID_NOTIFICATION_JSON);
        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.OK, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_031: [If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.] */
    @Test
    public void runFileUploadRequestParserThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new FileUploadRequestParser(VALID_BLOB_NAME);
                result = new IllegalArgumentException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_031: [If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.] */
    @Test
    public void runCreateRequestMessageThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new FileUploadRequestParser(VALID_BLOB_NAME);
                result = mockFileUploadRequestParser;
                mockFileUploadRequestParser.toJson();
                result = VALID_REQUEST_JSON;
                new IotHubTransportMessage(VALID_REQUEST_JSON);
                result = new IllegalArgumentException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_031: [If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.] */
    @Test
    public void runSendRequestThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new FileUploadRequestParser(VALID_BLOB_NAME);
                result = mockFileUploadRequestParser;
                mockFileUploadRequestParser.toJson();
                result = VALID_REQUEST_JSON;
                new IotHubTransportMessage(VALID_REQUEST_JSON);
                result = mockMessageRequest;
                mockHttpsTransportManager.sendFileUploadMessage(mockMessageRequest);
                result = new IOException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockHttpsTransportManager.sendFileUploadMessage(mockMessageRequest);
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_013: [If result status for the blob request is not `OK`, or `OK_EMPTY`, the run shall call the userCallback bypassing the received status, and abort the upload.] */
    @Test
    public void runRequestReceivedInternalServerError() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.INTERNAL_SERVER_ERROR;
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockResponseMessage.getStatus();
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.INTERNAL_SERVER_ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_014: [If result status for the blob request is `OK_EMPTY`, the run shall call the userCallback with the stratus `ERROR`, and abort the upload.] */
    @Test
    public void runIoTHubResponseDoNotContainsMessage() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK_EMPTY;
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockResponseMessage.getStatus();
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    @Test
    public void runIoTHubResponseNullBytesInMessage() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK;
                mockResponseMessage.getBytes();
                result = null;
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockResponseMessage.getBytes();
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    @Test
    public void runIoTHubResponseEmptyMessage() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK;
                mockResponseMessage.getBytes();
                result = new byte[]{};
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockResponseMessage.getBytes();
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    @Test
    public void runIoTHubResponseInvalidMessage() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK;
                mockResponseMessage.getBytes();
                result = new byte[]{'1','2','3'};
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockResponseMessage.getBytes();
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    @Test
    public void runFileUploadResponseParserThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        new NonStrictExpectations()
        {
            {
                mockResponseMessage.getStatus();
                result = IotHubStatusCode.OK;
                mockResponseMessage.getBytes();
                result = VALID_RESPONSE_JSON.getBytes();
                new FileUploadResponseParser(VALID_RESPONSE_JSON);
                result = new IllegalArgumentException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_032: [If create the blob URI failed, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    @Test
    public void runMakeURIThrows(@Mocked final URI mockURI) throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        new NonStrictExpectations()
        {
            {
                new URI((String)any);
                result = new URISyntaxException("", "");
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_022: [If the upload to blob failed, the run shall create a notification the IoT Hub with `isSuccess` equals false, `statusCode` equals -1.] */
    /* Tests_SRS_FILEUPLOADTASK_21_030: [If the upload to blob failed, the run shall call the `userCallback` reporting an error status `ERROR`.] */
    @Test
    public void runCloudBlockBlobThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        new NonStrictExpectations()
        {
            {
                new CloudBlockBlob((URI) any);
                result = new StorageException("", "", 0, new StorageExtendedErrorInformation(), new Exception());
            }
        };
        failedNotificationExpectations(VALID_CORRELATION_ID, VALID_NOTIFICATION_JSON);

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_022: [If the upload to blob failed, the run shall create a notification the IoT Hub with `isSuccess` equals false, `statusCode` equals -1.] */
    /* Tests_SRS_FILEUPLOADTASK_21_030: [If the upload to blob failed, the run shall call the `userCallback` reporting an error status `ERROR`.] */
    @Test
    public void runCloudBlockBlobUploadThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        new NonStrictExpectations()
        {
            {
                new CloudBlockBlob((URI) any);
                result = mockCloudBlockBlob;
                mockCloudBlockBlob.upload(mockInputStream, VALID_STREAM_LENGTH);
                result = new StorageException("", "", 0, new StorageExtendedErrorInformation(), new Exception());
            }
        };
        failedNotificationExpectations(VALID_CORRELATION_ID, VALID_NOTIFICATION_JSON);

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockCloudBlockBlob.upload(mockInputStream, VALID_STREAM_LENGTH);
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_033: [If run failed to send the notification, it shall call the userCallback with the stratus `ERROR`, and abort the upload.] */
    @Test
    public void runFileUploadStatusParserThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        cloudExpectations();
        new NonStrictExpectations()
        {
            {
                new FileUploadStatusParser(VALID_CORRELATION_ID, true, 0, (String)any);
                result = new IllegalArgumentException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_033: [If run failed to send the notification, it shall call the userCallback with the stratus `ERROR`, and abort the upload.] */
    @Test
    public void runCreateNotificationMessageThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        cloudExpectations();
        new NonStrictExpectations()
        {
            {
                new FileUploadStatusParser(VALID_CORRELATION_ID, true, 0, (String)any);
                result = mockFileUploadStatusParser;
                mockFileUploadStatusParser.toJson();
                result = VALID_NOTIFICATION_JSON;
                new IotHubTransportMessage(VALID_NOTIFICATION_JSON);
                result = new IllegalArgumentException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_031: [If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.] */
    @Test
    public void runSendNotificationThrows() throws IOException, IllegalArgumentException, URISyntaxException, StorageException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        cloudExpectations();
        new NonStrictExpectations()
        {
            {
                new FileUploadStatusParser(VALID_CORRELATION_ID, true, 0, (String)any);
                result = mockFileUploadStatusParser;
                mockFileUploadStatusParser.toJson();
                result = VALID_NOTIFICATION_JSON;
                new IotHubTransportMessage(VALID_NOTIFICATION_JSON);
                result = mockMessageNotification;
                mockHttpsTransportManager.sendFileUploadNotification(mockMessageNotification);
                result = new IOException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");

        // assert
        new Verifications()
        {
            {
                mockHttpsTransportManager.sendFileUploadNotification(mockMessageNotification);
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

}
