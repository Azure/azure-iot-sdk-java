package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.MessageSentCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubClientException;
import lombok.extern.slf4j.Slf4j;

import static samples.com.microsoft.azure.sdk.iot.DeviceClientManagerSample.failedMessageListOnClose;

@Slf4j
public class SampleMessageSendCallback implements MessageSentCallback {
    @Override
    public void onMessageSent(Message sentMessage, IotHubClientException exception, Object context) {
        IotHubStatusCode status = exception == null ? IotHubStatusCode.OK : exception.getStatusCode();
        log.debug(">> IoT Hub responded to message {} with status {}", sentMessage.getMessageId(), status.name());

        if (status == IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE) {
            failedMessageListOnClose.add(sentMessage.getMessageId());
        }
    }
}
