// Copyright (c) Microsoft. All rights reserved.Licensed under the MIT license.
// See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IotHubConnectionStatusChangeCallbackHandler implements IotHubConnectionStatusChangeCallback {
    @Override
    public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
        log.debug("### Connection status change reported: status={}, reason={}", status, statusChangeReason);

        if (throwable != null) {
            log.warn("Connection status callback executed with a throwable");
            log.warn("Throwable: {}", throwable);
        }

        final DeviceClientManager clientManager = (DeviceClientManager) callbackContext;

        switch (status) {
            case CONNECTED:
                switch (statusChangeReason) {
                    case CONNECTION_OK:
                        log.debug("### The DeviceClient is CONNECTED; all operations will be carried out as normal");
                        return;
                    case NO_NETWORK:
                    case BAD_CREDENTIAL:
                    case EXPIRED_SAS_TOKEN:
                    case COMMUNICATION_ERROR:
                    case RETRY_EXPIRED:
                    case CLIENT_CLOSE:
                    default:
                        throw new IllegalStateException(String.format("### DeviceClient cannot be %s with reason %s", status, statusChangeReason));
                }
            case DISCONNECTED_RETRYING:
                switch (statusChangeReason) {
                    case NO_NETWORK:
                    case COMMUNICATION_ERROR:
                        log.debug("### The DeviceClient is retrying based on the retry policy. Do NOT close or open the DeviceClient instance");
                        log.debug("### The DeviceClient can still queue messages and report properties, but they won't be sent until the connection is established.");
                        return;
                    case CONNECTION_OK:
                    case BAD_CREDENTIAL:
                    case EXPIRED_SAS_TOKEN:
                    case RETRY_EXPIRED:
                    case CLIENT_CLOSE:
                    default:
                        throw new IllegalStateException(String.format("### DeviceClient cannot be %s with reason %s", status, statusChangeReason));
                }
            case DISCONNECTED:
                switch (statusChangeReason) {
                    case CLIENT_CLOSE:
                        log.debug("### The DeviceClient has been closed gracefully. You can reopen by calling open() on this client.");
                        return;
                    case BAD_CREDENTIAL:
                    case EXPIRED_SAS_TOKEN:
                        log.warn("### The supplied credentials were invalid. Fix the input and create a new device client instance.");
                        return;
                    case RETRY_EXPIRED:
                        log.warn("### The DeviceClient has been disconnected because the retry policy expired. Can be reopened by closing and then opening the instance.");
                        if (Application.reconnectIndefinitely) {
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    clientManager.reconnect();
                                }
                            }).start();
                        }
                        return;
                    case COMMUNICATION_ERROR:
                        log.warn("### The DeviceClient has been disconnected due to a non-retryable exception. Inspect the throwable for details.");
                        log.warn("### The DeviceClient can be reopened by closing and then opening the instance.");
                        if (Application.reconnectIndefinitely) {
                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    clientManager.reconnect();
                                }
                            }).start();
                        }
                        return;
                    case CONNECTION_OK:
                    case NO_NETWORK:
                    default:
                        throw new IllegalStateException(String.format("### DeviceClient cannot be %s with reason %s", status, statusChangeReason));
                }
        }
    }
}
