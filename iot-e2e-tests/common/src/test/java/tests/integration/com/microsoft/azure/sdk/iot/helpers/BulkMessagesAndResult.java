package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

import java.util.List;

public class BulkMessagesAndResult {
    public List<Message> messages;
    public IotHubStatusCode statusCode;

    public BulkMessagesAndResult(List<Message> messages, IotHubStatusCode statusCode) {
        this.statusCode = statusCode;
        this.messages = messages;
    }
}