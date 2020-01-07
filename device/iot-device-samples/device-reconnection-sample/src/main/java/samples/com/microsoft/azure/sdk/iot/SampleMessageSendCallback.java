package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import lombok.extern.slf4j.Slf4j;

import static samples.com.microsoft.azure.sdk.iot.DeviceClientManagerSample.failedMessageListOnClose;

@Slf4j
public class SampleMessageSendCallback implements IotHubEventCallback {
    @Override
    public void execute(IotHubStatusCode status, Object context) {
        Message msg = (Message) context;
        log.debug(">> IoT Hub responded to message {} with status {}", msg.getMessageId(), status.name());

        if (status == IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE) {
            failedMessageListOnClose.add(msg.getMessageId());
        }
    }
}
