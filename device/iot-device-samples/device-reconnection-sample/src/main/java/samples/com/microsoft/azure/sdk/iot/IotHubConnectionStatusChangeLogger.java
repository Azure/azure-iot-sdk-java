package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.extern.slf4j.Slf4j;

import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.*;

@Slf4j
public class IotHubConnectionStatusChangeLogger implements IotHubConnectionStatusChangeCallback {
    @Override
    public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
        log.debug("### Connection status change reported: status={}, reason={}, throwable={}", status, statusChangeReason, throwable);

        switch (status) {
            case CONNECTED: {
                log.debug("### The DeviceClient is CONNECTED; all operations will be carried out as normal.");
                break;
            }
            case DISCONNECTED_RETRYING: {
                log.debug("### The DeviceClient is retrying based on the retry policy. Do NOT close or open the DeviceClient instance");
                log.debug("### The DeviceClient can still queue messages and report properties, but they won't be sent until the connection is established.");
                break;
            }
            case DISCONNECTED: {
                if (statusChangeReason == CLIENT_CLOSE) {
                    log.debug("### The DeviceClient has been closed gracefully. You can reopen by calling open() on this client.");
                } else if (statusChangeReason == BAD_CREDENTIAL || statusChangeReason == EXPIRED_SAS_TOKEN) {
                    log.warn("### The supplied credentials were invalid. Fix the input and create a new device client instance.");
                } else if (statusChangeReason == RETRY_EXPIRED) {
                    log.warn("### The DeviceClient has been disconnected because the retry policy expired. Can be reopened by closing and then opening the instance.");
                } else if (statusChangeReason == COMMUNICATION_ERROR) {
                    log.warn("### The DeviceClient has been disconnected due to a non-retry-able exception. Inspect the throwable for details.");
                    log.warn("### The DeviceClient can be reopened by closing and then opening the instance.");
                } else {
                    log.error("### [dead code] DeviceClient cannot be disconnected with reason {}", statusChangeReason);
                }
            }
        }
    }
}
