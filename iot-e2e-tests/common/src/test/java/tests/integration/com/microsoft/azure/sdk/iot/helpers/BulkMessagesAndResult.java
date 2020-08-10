package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;

import java.util.Set;

public class BulkMessagesAndResult {
    public Set<Message> messages;
    public IotHubStatusCode statusCode;

    public BulkMessagesAndResult(Set<Message> messages, IotHubStatusCode statusCode) {
        this.statusCode = statusCode;
        this.messages = messages;
    }
}