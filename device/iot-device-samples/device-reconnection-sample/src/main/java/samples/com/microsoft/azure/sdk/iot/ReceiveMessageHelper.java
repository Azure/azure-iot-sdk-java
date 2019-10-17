package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubMessageResult;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.MessageProperty;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReceiveMessageHelper {
    protected static class MessageCallback
            implements com.microsoft.azure.sdk.iot.device.MessageCallback {
        public IotHubMessageResult execute(Message msg,
                                           Object context) {
            log.debug("Received message with content: {}", new String(msg.getBytes(), Message.DEFAULT_IOTHUB_MESSAGE_CHARSET));
            for (MessageProperty messageProperty : msg.getProperties()) {
                log.debug("{}: {}", messageProperty.getName(), messageProperty.getValue());
            }

            IotHubMessageResult res = IotHubMessageResult.COMPLETE;

            log.debug("Responding to message with {}", res.name());

            return res;
        }
    }
}
