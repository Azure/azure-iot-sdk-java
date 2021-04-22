package tests.unit.com.microsoft.azure.sdk.iot.device.fileupload;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadTask;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
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
    private FileUploadSasUriRequest mockFileUploadSasUriRequest;

    @Mocked
    private FileUploadSasUriResponse mockFileUploadSasUriResponse;

    @Mocked
    private FileUploadCompletionNotification mockFileUploadStatusParser;

    @Mocked
    private IotHubTransportMessage mockMessageRequest;

    @Mocked
    private IotHubTransportMessage mockMessageNotification;

    @Mocked
    private ResponseMessage mockResponseMessage;

    @Mocked
    private BlobClient mockCloudBlockBlob;

    @Mocked
    private BlobClientBuilder mockCloudBlockBlobBuilder;

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
                new FileUploadSasUriRequest(blobName);
                result = mockFileUploadSasUriRequest;
                mockFileUploadSasUriRequest.toJson();
                result = requestJson;
                new IotHubTransportMessage(requestJson);
                result = mockMessageRequest;
                mockHttpsTransportManager.getFileUploadSasUri(mockMessageRequest);
                result = mockResponseMessage;
                new FileUploadSasUriResponse(anyString);
                result = mockFileUploadSasUriResponse;
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
                new FileUploadSasUriResponse(responseJson);
                result = mockFileUploadSasUriResponse;
            }
        };
    }

    private void responseParserExpectations(final String blobName, final String correlationId, final String hostName, final String containerName, final String sasToken)
    {
        new NonStrictExpectations()
        {
            {
                mockFileUploadSasUriResponse.getBlobName();
                result = blobName;
                mockFileUploadSasUriResponse.getCorrelationId();
                result = correlationId;
                mockFileUploadSasUriResponse.getHostName();
                result = hostName;
                mockFileUploadSasUriResponse.getContainerName();
                result = containerName;
                mockFileUploadSasUriResponse.getSasToken();
                result = sasToken;
            }
        };
    }

    private void blobClientBuilderExpectations() throws IOException, IllegalArgumentException, URISyntaxException
    {
        new NonStrictExpectations()
        {
            {
                new BlobClientBuilder();
                result = mockCloudBlockBlobBuilder;

                mockCloudBlockBlobBuilder.buildClient();
                result = mockCloudBlockBlob;
            }
        };
    }

    private void notificationExpectations(final String correlationId, final String notificationJson) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new FileUploadCompletionNotification(correlationId, true, 0, (String)any);
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

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private void failedNotificationExpectations(final String correlationId, final String notificationJson) throws IOException
    {
        new NonStrictExpectations()
        {
            {
                new FileUploadCompletionNotification(correlationId, false, -1, (String)any);
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

    @SuppressWarnings("SameParameterValue") // Since this is a helper method, the params can be passed any value.
    private void expectSuccess(
            final String blobName, final String correlationId, final String hostName, final String containerName, final String sasToken,
            final String requestJson, final String responseJson, final String notificationJson)
            throws IOException, IllegalArgumentException, URISyntaxException
    {
        requestExpectations(blobName, requestJson);
        responseExpectations(responseJson);
        responseParserExpectations(blobName, correlationId, hostName, containerName, sasToken);
        blobClientBuilderExpectations();
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

    /* Tests_SRS_FILEUPLOADTASK_21_007: [The run shall create a FileUpload request message, by using the FileUploadSasUriRequest.] */
    @Test
    public void runCreateRequest() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runSetPOSTForRequest() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runOpenConnectionToIothubForRequestAndNotification() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runSendRequestToIothub() throws IOException, IllegalArgumentException, URISyntaxException
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
                mockHttpsTransportManager.getFileUploadSasUri(mockMessageRequest);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_012: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
    /* Tests_SRS_FILEUPLOADTASK_21_028: [The run shall close the connection with the iothub, using the httpsTransportManager.] */
    @Test
    public void runCloseConnectionToIothubForRequestAndNotification() throws IOException, IllegalArgumentException, URISyntaxException
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

    /* Tests_SRS_FILEUPLOADTASK_21_019: [The run shall create a `CloudBlockBlob` using the `blobUri`.] */
    @Test
    public void runCreateCloudBlockBlob() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runUploadStreamToCloudBlockBlob() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runCreateNotificationSucceed() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runCreateNotification() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runSetPOSTForNotification() throws IOException, IllegalArgumentException, URISyntaxException
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
    public void runSendNotificationToIothub() throws IOException, IllegalArgumentException, URISyntaxException
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

    /* Tests_SRS_FILEUPLOADTASK_21_031: [If run failed to send the request, it shall call the userCallback with the status `ERROR`, and abort the upload.] */
    @Test
    public void runFileUploadRequestParserThrows() throws IOException, IllegalArgumentException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new FileUploadSasUriRequest(VALID_BLOB_NAME);
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
    public void runCreateRequestMessageThrows() throws IOException, IllegalArgumentException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new FileUploadSasUriRequest(VALID_BLOB_NAME);
                result = mockFileUploadSasUriRequest;
                mockFileUploadSasUriRequest.toJson();
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
    public void runSendRequestThrows() throws IOException, IllegalArgumentException, URISyntaxException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new FileUploadSasUriRequest(VALID_BLOB_NAME);
                result = mockFileUploadSasUriRequest;
                mockFileUploadSasUriRequest.toJson();
                result = VALID_REQUEST_JSON;
                new IotHubTransportMessage(VALID_REQUEST_JSON);
                result = mockMessageRequest;
                mockHttpsTransportManager.getFileUploadSasUri(mockMessageRequest);
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
                mockHttpsTransportManager.getFileUploadSasUri(mockMessageRequest);
                times = 1;
                mockIotHubEventCallback.execute(IotHubStatusCode.ERROR, VALID_CALLBACK_CONTEXT);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOADTASK_21_016: [If the `responseMessage` is null, empty, do not contains a valid json, or if the information in json is not correct, the run shall call the `userCallback` reporting the error, and abort the upload.] */
    @Test
    public void runFileUploadResponseParserThrows() throws IOException, IllegalArgumentException, URISyntaxException
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
                new FileUploadSasUriResponse(VALID_RESPONSE_JSON);
                result = new IllegalArgumentException();
            }
        };

        FileUploadTask fileUploadTask = Deencapsulation.newInstance(FileUploadTask.class,
                new Class[] {String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                VALID_BLOB_NAME, mockInputStream, VALID_STREAM_LENGTH, mockHttpsTransportManager, mockIotHubEventCallback, VALID_CALLBACK_CONTEXT);

        // act
        Deencapsulation.invoke(fileUploadTask, "run");
    }

    /* Tests_SRS_FILEUPLOADTASK_21_033: [If run failed to send the notification, it shall call the userCallback with the stratus `ERROR`, and abort the upload.] */
    @Test
    public void runFileUploadStatusParserThrows() throws IOException, IllegalArgumentException, URISyntaxException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        blobClientBuilderExpectations();
        new NonStrictExpectations()
        {
            {
                new FileUploadCompletionNotification(VALID_CORRELATION_ID, true, 0, (String)any);
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
    public void runCreateNotificationMessageThrows() throws IOException, IllegalArgumentException, URISyntaxException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        blobClientBuilderExpectations();
        new NonStrictExpectations()
        {
            {
                new FileUploadCompletionNotification(VALID_CORRELATION_ID, true, 0, (String)any);
                result = mockFileUploadStatusParser;
                mockFileUploadStatusParser.toJson();
                result = VALID_NOTIFICATION_JSON;
                new IotHubTransportMessage(VALID_NOTIFICATION_JSON);
                result = new IOException();
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
    public void runSendNotificationThrows() throws IOException, IllegalArgumentException, URISyntaxException
    {
        // arrange
        requestExpectations(VALID_BLOB_NAME, VALID_REQUEST_JSON);
        responseExpectations(VALID_RESPONSE_JSON);
        responseParserExpectations(VALID_BLOB_NAME, VALID_CORRELATION_ID, VALID_HOST_NAME, VALID_CONTAINER_NAME, VALID_SAS_TOKEN);
        blobClientBuilderExpectations();
        new NonStrictExpectations()
        {
            {
                new FileUploadCompletionNotification(VALID_CORRELATION_ID, true, 0, (String)any);
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
