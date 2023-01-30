package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsMethod;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsResponse;
import com.microsoft.azure.sdk.iot.device.transport.https.HttpsTransportManager;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import java.io.IOException;

public class FileUploadTest
{
    @Mocked
    HttpsTransportManager mockHttpsTransportManager;

    @Mocked
    FileUploadSasUriRequest mockFileUploadSasUriRequest;

    @Mocked
    IotHubTransportMessage mockIotHubTransportMessage;

    @Mocked
    HttpsResponse mockResponseMessage;

    @Mocked
    FileUploadSasUriResponse mockFileUploadSasUriResponse;

    @Mocked
    FileUploadCompletionNotification mockFileUploadCompletionNotification;

    @Test
    public void getFileUploadSasUri() throws IOException
    {
        final String mockJson = "";
        final String mockResponsePayloadString = "some response payload";
        final byte[] mockResponsePayload = mockResponsePayloadString.getBytes();
        new Expectations()
        {
            {
                mockFileUploadSasUriRequest.toJson();
                result = mockJson;

                new IotHubTransportMessage(mockJson);
                result = mockIotHubTransportMessage;

                mockIotHubTransportMessage.setIotHubMethod(HttpsMethod.POST);

                mockHttpsTransportManager.getFileUploadSasUri(mockIotHubTransportMessage);
                result = mockResponseMessage;

                mockResponseMessage.getBody();
                result = mockResponsePayload;

                new FileUploadSasUriResponse(mockResponsePayloadString);
                result = mockFileUploadSasUriResponse;

                mockResponseMessage.getStatus();
                result = 200;
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
                mockFileUploadCompletionNotification.toJson();
                result = mockJson;

                new IotHubTransportMessage(mockJson);
                result = mockIotHubTransportMessage;

                mockIotHubTransportMessage.setIotHubMethod(HttpsMethod.POST);

                mockHttpsTransportManager.sendFileUploadNotification(mockIotHubTransportMessage);
                result = mockResponseMessage;

                mockResponseMessage.getBody();
                result = mockResponsePayload;

                mockResponseMessage.getStatus();
                result = 200;
            }
        };

        FileUpload fileUpload = Deencapsulation.newInstance(FileUpload.class, mockHttpsTransportManager);

        Deencapsulation.invoke(fileUpload, "sendNotification", mockFileUploadCompletionNotification);
    }

}
