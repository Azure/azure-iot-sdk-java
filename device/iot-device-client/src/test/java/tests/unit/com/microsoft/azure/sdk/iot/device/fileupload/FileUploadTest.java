// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.fileupload;

import com.microsoft.azure.sdk.iot.device.CustomLogger;
import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUpload;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadInProgress;
import com.microsoft.azure.sdk.iot.device.fileupload.FileUploadTask;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.*;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for file upload class.
 * 100% methods, 100% lines covered
 */
public class FileUploadTest
{
    @Mocked
    private DeviceClientConfig mockConfig;

    @Mocked
    private HttpsTransportManager mockHttpsTransportManager;

    @Mocked
    private InputStream mockInputStream;

    @Mocked
    private IotHubEventCallback mockIotHubEventCallback;

    @Mocked
    private FileUploadTask mockFileUploadTask;

    @Mocked
    private Executors mockExecutors;

    @Mocked
    private ScheduledExecutorService mockScheduler;

    @Mocked
    private FileUploadInProgress mockFileUploadInProgress;

    private void constructorExpectations()
    {
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;
                Executors.newScheduledThreadPool(10);
                result = mockScheduler;
            }
        };
    }

    /* Tests_SRS_FILEUPLOAD_21_001: [If the provided `config` is null, the constructor shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullConfigThrows() throws IOException
    {
        // act
        FileUpload fileUpload = new FileUpload(null);
    }

    /* Tests_SRS_FILEUPLOAD_21_002: [The constructor shall create a new instance of `HttpsTransportManager` with the provided `config`.] */
    /* Tests_SRS_FILEUPLOAD_21_012: [The constructor shall create an pool of 10 threads to execute the uploads in parallel.] */
    /* Tests_SRS_FILEUPLOAD_21_013: [The constructor shall create a list `fileUploadInProgressesSet` to control the pending uploads.] */
    @Test
    public void constructorSuccess(@Mocked final LinkedBlockingDeque<?> mockFileUploadInProgressQueue) throws IOException
    {
        // arrange
        new Expectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;
                Executors.newScheduledThreadPool(10);
                result = mockScheduler;
            }
        };

        // act
        final FileUpload fileUpload = new FileUpload(mockConfig);

        // assert
        new Verifications()
        {
            {
                new LinkedBlockingDeque<>();
                times = 1;
            }
        };
        assertNotNull(fileUpload);
    }

    /* Tests_SRS_FILEUPLOAD_21_003: [If the constructor fail to create the new instance of the `HttpsTransportManager`, it shall throw IllegalArgumentException, threw by the HttpsTransportManager constructor.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorHttpsTransportManagerThrows() throws IOException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        FileUpload fileUpload = new FileUpload(mockConfig);
    }

    /* Tests_SRS_FILEUPLOAD_21_015: [If create the executor failed, the constructor shall throws IOException.] */
    @Test (expected = IOException.class)
    public void constructorExecutorThrows() throws IOException
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;
                Executors.newScheduledThreadPool(10);
                result = new IllegalArgumentException();
                times = 1;
            }
        };

        // act
        FileUpload fileUpload = new FileUpload(mockConfig);
    }

    /* Tests_SRS_FILEUPLOAD_21_004: [The uploadToBlobAsync shall asynchronously upload the InputStream `inputStream` to the blob in `blobName`.] */
    /* Tests_SRS_FILEUPLOAD_21_009: [The uploadToBlobAsync shall create a `FileUploadTask` to control this file upload.] */
    /* Tests_SRS_FILEUPLOAD_21_010: [The uploadToBlobAsync shall schedule the task `FileUploadTask` to immediately start.] */
    /* Tests_SRS_FILEUPLOAD_21_016: [The uploadToBlobAsync shall create a `FileUploadInProgress` to store the fileUpload context.] */
    @Test
    public void uploadToBlobAsyncSuccess() throws IOException
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // assert
        new NonStrictExpectations()
        {
            {
                mockInputStream.available();
                result = streamLength;
                Deencapsulation.newInstance(FileUploadInProgress.class,
                        new Class[] {IotHubEventCallback.class, Object.class},
                        mockIotHubEventCallback, context);
                result = mockFileUploadInProgress;
                times = 1;
                Deencapsulation.newInstance(FileUploadTask.class,
                        new Class[] { String.class, InputStream.class, long.class, HttpsTransportManager.class, IotHubEventCallback.class, Object.class},
                        blobName, mockInputStream, streamLength, mockHttpsTransportManager, (IotHubEventCallback)any, mockFileUploadInProgress);
                result = mockFileUploadTask;
                times = 1;
                mockScheduler.submit(mockFileUploadTask);
                times = 1;
            }
        };

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_005: [If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNullBlobNameThrows() throws IOException
    {
        // arrange
        final String blobName = null;
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_005: [If the `blobName` is null or empty, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncEmptyBlobNameThrows() throws IOException
    {
        // arrange
        final String blobName = "";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_006: [If the `inputStream` is null or not available, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNullInputStreamThrows() throws IOException
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockInputStream.available();
                result = streamLength;
            }
        };
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, null, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_007: [If the `streamLength` is negative, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNegativeStreamLenghtThrows() throws IOException
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = -100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockInputStream.available();
                result = streamLength;
            }
        };
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, mockIotHubEventCallback, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_008: [If the `userCallback` is null, the uploadToBlobAsync shall throw IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void uploadToBlobAsyncNullUserCallbackThrows() throws IOException
    {
        // arrange
        final String blobName = "validBlobName";
        final long streamLength = 100;
        final Map<String, Object> context = new HashMap<>();

        constructorExpectations();
        new NonStrictExpectations()
        {
            {
                mockInputStream.available();
                result = streamLength;
            }
        };
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.uploadToBlobAsync(blobName, mockInputStream, streamLength, null, context);
    }

    /* Tests_SRS_FILEUPLOAD_21_017: [The closeNow shall shutdown the thread pool by calling `shutdownNow`.] */
    @Test
    public void closeNowSuccess() throws IOException
    {
        // arrange
        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);

        // act
        fileUpload.closeNow();

        // assert
        new Verifications()
        {
            {
                mockScheduler.shutdownNow();
                times = 1;
            }
        };

    }

    /* Tests_SRS_FILEUPLOAD_21_018: [If there is pending file uploads, the closeNow shall cancel the upload, and call the `statusCallback` reporting ERROR.] */
    @Test
    public void closeNowWithPendingUploadSuccess(@Mocked final Future mockFuture) throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        final Queue<FileUploadInProgress> fileUploadInProgressSet = new LinkedBlockingDeque<FileUploadInProgress>()
        {
            {
                add(mockFileUploadInProgress);
            }
        };

        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;
                Executors.newScheduledThreadPool(10);
                result = mockScheduler;
                Deencapsulation.invoke(mockFileUploadInProgress, "isCancelled");
                result = true;
            }
        };
        FileUpload fileUpload = new FileUpload(mockConfig);
        Deencapsulation.setField(fileUpload, "fileUploadInProgressesSet", fileUploadInProgressSet);

        // act
        fileUpload.closeNow();

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockFileUploadInProgress, "triggerCallback" , new Class[] {IotHubStatusCode.class}, IotHubStatusCode.ERROR);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOAD_21_014: [The constructor shall create an Event callback `fileUploadStatusCallBack` to receive the upload status.] */
    /* Tests_SRS_FILEUPLOAD_21_019: [The FileUploadStatusCallBack shall implements the `IotHubEventCallback` as result of the FileUploadTask.] */
    /* Tests_SRS_FILEUPLOAD_21_020: [The FileUploadStatusCallBack shall call the `statusCallback` reporting the received status.] */
    @Test
    public void callbackBypassStatus() throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);
        IotHubEventCallback testFileUploadStatusCallBack = Deencapsulation.newInnerInstance("FileUploadStatusCallBack", fileUpload);

        //act
        testFileUploadStatusCallBack.execute(IotHubStatusCode.OK_EMPTY, mockFileUploadInProgress);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockFileUploadInProgress, "triggerCallback" , new Class[] {IotHubStatusCode.class}, IotHubStatusCode.OK_EMPTY);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOAD_21_021: [The FileUploadStatusCallBack shall delete the `FileUploadInProgress` that store this file upload context.] */
    @Test
    public void callbackDeleteFileUploadInProgress(@Mocked final LinkedBlockingDeque<?> mockFileUploadInProgressQueue) throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        new NonStrictExpectations()
        {
            {
                new HttpsTransportManager(mockConfig);
                result = mockHttpsTransportManager;

                Executors.newScheduledThreadPool(10);
                result = mockScheduler;

                new LinkedBlockingDeque<>();
                result = mockFileUploadInProgressQueue;
            }
        };
        FileUpload fileUpload = new FileUpload(mockConfig);
        IotHubEventCallback testFileUploadStatusCallBack = Deencapsulation.newInnerInstance("FileUploadStatusCallBack", fileUpload);

        //act
        testFileUploadStatusCallBack.execute(IotHubStatusCode.OK_EMPTY, mockFileUploadInProgress);

        //assert
        new Verifications()
        {
            {
                mockFileUploadInProgressQueue.remove(mockFileUploadInProgress);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOAD_21_022: [If the received context is not type of `FileUploadInProgress`, the FileUploadStatusCallBack shall log a error and ignore the message.] */
    @Test
    public void callbackContextIsNotFileUploadInProgress(@Mocked final CustomLogger mockCustomLogger) throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        constructorExpectations();
        FileUpload fileUpload = new FileUpload(mockConfig);
        IotHubEventCallback testFileUploadStatusCallBack = Deencapsulation.newInnerInstance("FileUploadStatusCallBack", fileUpload);

        //act
        testFileUploadStatusCallBack.execute(IotHubStatusCode.OK_EMPTY, context);

        //assert
        new Verifications()
        {
            {
                mockCustomLogger.LogError((String)any);
                times = 1;
            }
        };
    }

    /* Tests_SRS_FILEUPLOAD_21_023: [If the FileUploadStatusCallBack failed to delete the `FileUploadInProgress`, it shall log a error.] */
    @Test
    public void callbackDeleteFileUploadInProgressThrows(@Mocked final LinkedBlockingDeque<?> mockFileUploadInProgressQueue) throws IOException
    {
        // arrange
        final Map<String, Object> context = new HashMap<>();
        constructorExpectations();
        new NonStrictExpectations()
        {
            {
                new LinkedBlockingDeque<>();
                result = mockFileUploadInProgressQueue;
                mockFileUploadInProgressQueue.remove(mockFileUploadInProgress);
                result = new UnsupportedOperationException();
            }
        };
        FileUpload fileUpload = new FileUpload(mockConfig);
        IotHubEventCallback testFileUploadStatusCallBack = Deencapsulation.newInnerInstance("FileUploadStatusCallBack", fileUpload);

        //act
        testFileUploadStatusCallBack.execute(IotHubStatusCode.OK_EMPTY, mockFileUploadInProgress);

        //assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockFileUploadInProgress, "triggerCallback" , new Class[] {IotHubStatusCode.class}, IotHubStatusCode.OK_EMPTY);
                times = 1;
            }
        };
    }


}
