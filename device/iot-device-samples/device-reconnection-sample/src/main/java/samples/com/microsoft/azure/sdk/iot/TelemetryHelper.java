// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import lombok.extern.slf4j.Slf4j;

import static samples.com.microsoft.azure.sdk.iot.Application.failedMessageListOnClose;

@Slf4j
public class TelemetryHelper {
    protected static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {
            Message msg = (Message) context;
            log.debug(">> IoT Hub responded to message {} with status {}", msg.getMessageId(), status.name());

            if (status == IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE) {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

    static Message composeMessage(int counter) {
        double temperature = 0.0;
        double humidity = 0.0;

        temperature = 20 + Math.random() * 10;
        humidity = 30 + Math.random() * 20;
        String messageId = java.util.UUID.randomUUID().toString();

        String msgStr = ">> {\"count\":" + counter + ",\"messageId\":" + messageId + ",\"temperature\":" + temperature + ",\"humidity\":" + humidity + "}";
        Message msg = new Message(msgStr);
        msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
        msg.setMessageId(messageId);
        log.debug(msgStr);

        return msg;
    }
}
