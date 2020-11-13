package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceTwin.Pair;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason;
import com.microsoft.azure.sdk.iot.device.exceptions.DeviceOperationTimeoutException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.NO_NETWORK;
import static com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeReason.RETRY_EXPIRED;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.DISCONNECTED;
import static com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus.DISCONNECTED_RETRYING;

@Slf4j
public abstract class ClientManagerBase implements IotHubConnectionStatusChangeCallback {

    protected enum ConnectionStatus {
        DISCONNECTED, CONNECTING, CONNECTED
    }

    protected static final Object lock = new Object();
    protected static final int SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS = 10;
    protected ConnectionStatus connectionStatus;
    protected Pair<IotHubConnectionStatusChangeCallback, Object> suppliedConnectionStatusChangeCallback;

    public void openClient() throws IOException{};
    public void closeClient() throws IOException{};

    public void doConnect() throws IOException {
        // Device client does not have retry on the initial open() call. Will need to be re-opened by the calling application
        while (connectionStatus == ConnectionStatus.CONNECTING) {
            synchronized (lock) {
                if(connectionStatus == ConnectionStatus.CONNECTING) {
                    try {
                        log.debug("Opening the device client instance...");
                        openClient();
                        connectionStatus = ConnectionStatus.CONNECTED;
                        break;
                    }
                    catch (Exception ex) {
                        if (ex.getCause() instanceof TransportException && ((TransportException) ex.getCause()).isRetryable()) {
                            log.warn("Transport exception thrown while opening DeviceClient instance, retrying: ", ex);
                        } else {
                            log.error("Non-retryable exception thrown while opening DeviceClient instance: ", ex);
                            connectionStatus = ConnectionStatus.DISCONNECTED;
                            throw ex;
                        }
                    }
                }
            }

            try {
                log.debug("Sleeping for 10 secs before attempting another open()");
                Thread.sleep(SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS * 1000);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException("InterruptedException in thread sleep: ", ex);
            }
        }
    }

    public void closeNow() {
        synchronized (lock) {
            try {
                log.debug("Closing the device client instance...");
                closeClient();
            }
            catch (IOException e) {
                log.error("Exception thrown while closing DeviceClient instance: ", e);
            } finally {
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }
        }
    }

    public void open() throws IOException {
        synchronized (lock) {
            if(connectionStatus == ConnectionStatus.DISCONNECTED) {
                connectionStatus = ConnectionStatus.CONNECTING;
            } else {
                return;
            }
        }
        doConnect();
    }

    @Override
    public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext) {
        Pair<IotHubConnectionStatusChangeCallback, Object> suppliedCallbackPair = this.suppliedConnectionStatusChangeCallback;
        if (throwable == null)
        {
            System.out.println("CONNECTION STATUS UPDATE FOR MULTIPLEXING CLIENT " + " - " + status + ", " + statusChangeReason);
        }
        else
        {
            System.out.println("CONNECTION STATUS UPDATE FOR DEVICE " + " - " + status + ", " + statusChangeReason + ", " + throwable.getMessage());
        }

        if (shouldDeviceReconnect(status, statusChangeReason, throwable)) {
            if (suppliedCallbackPair != null) {
                suppliedCallbackPair.getKey().execute(DISCONNECTED_RETRYING, NO_NETWORK, throwable, suppliedCallbackPair.getValue());
            }

            handleRecoverableDisconnection();
        } else if (suppliedCallbackPair != null) {
            suppliedCallbackPair.getKey().execute(status, statusChangeReason, throwable, suppliedCallbackPair.getValue());
        }
    }

    // This handles the state where the DeviceClient reports that OperationTimeout has expired, and stops retrying; even though the applied RetryPolicy is still valid.
    protected boolean shouldDeviceReconnect(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable) {
        return (status == DISCONNECTED && statusChangeReason == RETRY_EXPIRED && throwable instanceof DeviceOperationTimeoutException);
    }

    public void handleRecoverableDisconnection() {
        synchronized (lock) {
            if (connectionStatus == ConnectionStatus.CONNECTED) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        log.debug("Attempting reconnect for device client...");
                        synchronized (lock) {
                            if (connectionStatus == ConnectionStatus.CONNECTED) {
                                try {
                                    closeClient();
                                } catch (Exception e) {
                                    log.warn("DeviceClient closeNow failed.", e);
                                } finally {
                                    connectionStatus = ConnectionStatus.CONNECTING;
                                }
                            } else {
                                log.debug("DeviceClient is currently connecting, or already connected; skipping...");
                                return;
                            }
                        }
                        try {
                            doConnect();
                        } catch (IOException e) {
                            log.error("Exception thrown while opening DeviceClient instance: ", e);
                        }
                    }
                }).start();
            } else {
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }
        }
    }
}
