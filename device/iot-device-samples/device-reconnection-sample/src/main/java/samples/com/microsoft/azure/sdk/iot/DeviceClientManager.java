package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceOperationTimeoutException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.NO_NETWORK;
import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.RETRY_EXPIRED;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.DISCONNECTED;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.DISCONNECTED_RETRYING;

@Slf4j
public class DeviceClientManager implements IotHubConnectionStatusChangeCallback {
    private enum ConnectionStatus {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    private static final Object lock = new Object();
    private static final int SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS = 10;
    private ConnectionStatus connectionStatus;
    private Pair<IotHubConnectionStatusChangeCallback, Object> suppliedConnectionStatusChangeCallback;

    private interface DeviceClientNonDelegatedFunction {
        void open();
        void closeNow();
        void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
    }

    // The methods defined in the interface DeviceClientNonDelegatedFunction will be called on DeviceClientManager, and not on DeviceClient.
    @Delegate(excludes = DeviceClientNonDelegatedFunction.class)
    private final DeviceClient client;

    DeviceClientManager(DeviceClient deviceClient) {
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
        this.client = deviceClient;
        this.client.registerConnectionStatusChangeCallback(this, this);
    }

    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext) {
        this.suppliedConnectionStatusChangeCallback = new Pair<>(callback, callbackContext);
    }

    public void open() {
        synchronized (lock) {
            if(connectionStatus == ConnectionStatus.DISCONNECTED) {
                connectionStatus = ConnectionStatus.CONNECTING;
            } else {
                return;
            }
        }
        doConnect();
    }

    private void doConnect() {
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

    public void closeNow() {
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
        Pair<IotHubConnectionStatusChangeCallback, Object> suppliedCallbackPair = this.suppliedConnectionStatusChangeCallback;
        IotHubConnectionStatusChangeCallback suppliedCallback = suppliedCallbackPair.getKey();
        Object suppliedCallbackContext = suppliedCallbackPair.getValue();

        if (shouldDeviceReconnect(status, statusChangeReason, throwable)) {
            if (suppliedCallback != null) {
                suppliedCallback.execute(DISCONNECTED_RETRYING, NO_NETWORK, throwable, suppliedCallbackContext);
            }

            handleRecoverableDisconnection();
        } else if (suppliedCallback != null) {
            suppliedCallback.execute(status, statusChangeReason, throwable, suppliedCallbackContext);
        }
    }

    // This handles the state where the DeviceClient reports that OperationTimeout has expired, and stops retrying; even though the applied RetryPolicy is still valid.
    private boolean shouldDeviceReconnect(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable) {
        return (status == DISCONNECTED && statusChangeReason == RETRY_EXPIRED && throwable instanceof DeviceOperationTimeoutException);
    }

    private void handleRecoverableDisconnection() {
        synchronized (lock) {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
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
}
