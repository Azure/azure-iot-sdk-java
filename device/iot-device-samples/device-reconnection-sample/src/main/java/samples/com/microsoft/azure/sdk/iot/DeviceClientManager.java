package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DeviceClientManager implements IotHubConnectionStatusChangeCallback {
    private enum ConnectionStatus {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private static final Object lock = new Object();
    private static final int SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS = 10;
    private final boolean autoReconnectOnDisconnected;
    private ConnectionStatus connectionStatus;
    @Delegate
    private DeviceClient client;

    DeviceClientManager(DeviceClient deviceClient, boolean autoReconnectOnDisconnected) {
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
        this.client = deviceClient;
        this.client.registerConnectionStatusChangeCallback(this, this);
        this.autoReconnectOnDisconnected = autoReconnectOnDisconnected;
    }

    public void connect() {
        synchronized (lock) {
            if(connectionStatus == ConnectionStatus.DISCONNECTED) {
                connectionStatus = ConnectionStatus.CONNECTING;
            } else {
                return;
            }
        }
        doConnect();
    }

    public void disconnect() {
        synchronized (lock) {
            try {
                log.debug("[disconnect] - Closing the device client instance...");
                client.closeNow();
            }
            catch (IOException e) {
                log.error("[disconnect] - Exception thrown while closing DeviceClient instance: ", e);
            } finally {
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }
        }

    }

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
                handleDisconnection(statusChangeReason);
            }
        }
    }

    private void handleDisconnection(IotHubConnectionStatusChangeReason statusChangeReason) {
        switch (statusChangeReason) {
            case CLIENT_CLOSE:
                log.debug("### The DeviceClient has been closed gracefully. You can reopen by calling open() on this client.");
                break;
            case BAD_CREDENTIAL:
            case EXPIRED_SAS_TOKEN:
                log.warn("### The supplied credentials were invalid. Fix the input and create a new device client instance.");
                break;
            case RETRY_EXPIRED:
                log.warn("### The DeviceClient has been disconnected because the retry policy expired. Can be reopened by closing and then opening the instance.");
                handleRecoverableDisconnection();
                break;
            case COMMUNICATION_ERROR:
                log.warn("### The DeviceClient has been disconnected due to a non-retryable exception. Inspect the throwable for details.");
                log.warn("### The DeviceClient can be reopened by closing and then opening the instance.");
                handleRecoverableDisconnection();
                break;
            default:
                log.error("### [dead code] DeviceClient cannot be disconnected with reason {}", statusChangeReason);
        }
    }

    void handleRecoverableDisconnection() {
        synchronized (lock) {
            if (connectionStatus == ConnectionStatus.CONNECTED && autoReconnectOnDisconnected) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        log.debug("[reconnect] - Attempting reconnect for device client...");
                        synchronized (lock) {
                            if (connectionStatus == ConnectionStatus.CONNECTED) {
                                try {
                                    client.closeNow();
                                } catch (Exception e) {
                                    log.warn("[reconnect] - DeviceClient closeNow failed.", e);
                                } finally {
                                    connectionStatus = ConnectionStatus.CONNECTING;
                                }
                            } else {
                                log.debug("[reconnect] - DeviceClient is currently connecting, or already connected; skipping...");
                                return;
                            }
                        }
                        doConnect();
                    }
                }).start();
            } else {
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }
        }
    }

    void doConnect() {
        // Device client does not have retry on the initial open() call. Will need to be re-opened by the calling application
        while (connectionStatus == ConnectionStatus.CONNECTING) {
            synchronized (lock) {
                if(connectionStatus == ConnectionStatus.CONNECTING) {
                    try {
                        log.debug("[connect] - Opening the device client instance...");
                        client.open();
                        connectionStatus = ConnectionStatus.CONNECTED;
                        break;
                    }
                    catch (Exception ex) {
                        log.error("[connect] - Exception thrown while opening DeviceClient instance: ", ex);
                    }
                }
            }

            try {
                log.debug("[connect] - Sleeping for 10 secs before attempting another open()");
                Thread.sleep(SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS * 1000);
            }
            catch (InterruptedException ex) {
                log.error("[connect] - Exception in thread sleep: ", ex);
            }
        }
    }
}
