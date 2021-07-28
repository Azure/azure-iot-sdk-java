package tests.unit.com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadCompletionNotification;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriRequest;
import com.microsoft.azure.sdk.iot.deps.serializer.FileUploadSasUriResponse;
import com.microsoft.azure.sdk.iot.device.FileUpload;
import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.TestCase.assertEquals;

public class FileUploadTest
{
    @Mocked
    HttpsTransportManager mockHttpsTransportManager;

    @Mocked
    FileUploadSasUriRequest mockFileUploadSasUriRequest;

    @Mocked
    IotHubTransportMessage mockIotHubTransportMessage;

    @Mocked
    ResponseMessage mockResponseMessage;

    @Mocked
    FileUploadSasUriResponse mockFileUploadSasUriResponse;

    @Mocked
    FileUploadCompletionNotification mockFileUploadCompletionNotification;

    @Test
    public void constructor()
    {
        new Expectations()
        {
            {
                mockHttpsTransportManager.open();
                times = 1;
            }
        };

        Deencapsulation.newInstance(FileUpload.class, mockHttpsTransportManager);
    }

    @Test
    public void getFileUploadSasUri() throws IOException
    {
        final String mockJson = "";
        final String mockResponsePayloadString = "some response payload";
        final byte[] mockResponsePayload = mockResponsePayloadString.getBytes();
        new Expectations()
        {
            {
                mockHttpsTransportManager.open();
                times = 1;

                mockFileUploadSasUriRequest.toJson();
                result = mockJson;

                new IotHubTransportMessage(mockJson);
                result = mockIotHubTransportMessage;

                mockIotHubTransportMessage.setIotHubMethod(IotHubMethod.POST);

                mockHttpsTransportManager.getFileUploadSasUri(mockIotHubTransportMessage);
                result = mockResponseMessage;

                mockResponseMessage.getBytes();
                result = mockResponsePayload;

                new FileUploadSasUriResponse(mockResponsePayloadString);
                result = mockFileUploadSasUriResponse;
            }
        };

        FileUpload fileUpload = Deencapsulation.newInstance(FileUpload.class, mockHttpsTransportManager);

        Deencapsulation.invoke(fileUpload, "getFileUploadSasUri", mockFileUploadSasUriRequest);
    }

    @Test
    public void sendNotification() throws IOException
    {
        final String mockJson = "";
        final String mockResponsePayloadString = "some response payload";
        final byte[] mockResponsePayload = mockResponsePayloadString.getBytes();
        new Expectations()
        {
            {
                mockHttpsTransportManager.open();
                times = 1;

                mockFileUploadCompletionNotification.toJson();
                result = mockJson;

                new IotHubTransportMessage(mockJson);
                result = mockIotHubTransportMessage;

                mockIotHubTransportMessage.setIotHubMethod(IotHubMethod.POST);

                mockHttpsTransportManager.sendFileUploadNotification(mockIotHubTransportMessage);
                result = mockResponseMessage;

                mockResponseMessage.getBytes();
                result = mockResponsePayload;
            }
        };

        FileUpload fileUpload = Deencapsulation.newInstance(FileUpload.class, mockHttpsTransportManager);

        Deencapsulation.invoke(fileUpload, "sendNotification", mockFileUploadCompletionNotification);
    }

}
