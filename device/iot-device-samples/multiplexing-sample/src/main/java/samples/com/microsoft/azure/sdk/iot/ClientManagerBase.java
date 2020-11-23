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

/**
 * This is the base class for DeviceClientManager and MultiplexingClientManager
 * The shared logic for reconnection and handling status change call back exists here.
 */
@Slf4j
public abstract class ClientManagerBase implements IotHubConnectionStatusChangeCallback {

    protected enum ConnectionStatus {
        DISCONNECTED, CONNECTING, CONNECTED
    }
    
    protected static final Object lock = new Object();
    protected static final int SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS = 2;
    protected ConnectionStatus connectionStatus;
    protected Pair<IotHubConnectionStatusChangeCallback, Object> suppliedConnectionStatusChangeCallback;

    public abstract void openClient() throws IOException;
    public abstract void closeClient() throws IOException;
    public abstract String getClientId();

    public void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext) {
        if (callback != null) {
            this.suppliedConnectionStatusChangeCallback = new Pair<>(callback, callbackContext);
        } else {
            this.suppliedConnectionStatusChangeCallback = null;
        }
    }

    public void doConnect() throws IOException {
        // Device client does not have retry on the initial open() call. Will need to be re-opened by the calling application
        while (connectionStatus == ConnectionStatus.CONNECTING) {
            synchronized (lock) {
                if(connectionStatus == ConnectionStatus.CONNECTING) {
                    try {
                        log.debug("Opening the device client instance " + getClientId() + " ...");
                        openClient();
                        connectionStatus = ConnectionStatus.CONNECTED;
                        break;
                    }
                    catch (Exception ex) {
                        if (ex.getCause() instanceof TransportException && ((TransportException) ex.getCause()).isRetryable()) {
                            log.warn("Transport exception thrown while opening DeviceClient instance " + getClientId() + ", retrying: ", ex);
                        } else {
                            log.error("Non-retryable exception thrown while opening DeviceClient instance " + getClientId() + ": ", ex);
                            connectionStatus = ConnectionStatus.DISCONNECTED;
                            throw ex;
                        }
                    }
                }
            }

            try {
                log.debug("Sleeping for " + SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS + " secs before attempting another open()");
                Thread.sleep(SLEEP_TIME_BEFORE_RECONNECTING_IN_SECONDS * 1000);
            }
            catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted while waiting between attempting to open the client: ", ex);
            }
        }
    }

    public void closeNow() {
        synchronized (lock) {
            try {
                log.debug("Closing the client instance " + getClientId() + " ...");
                closeClient();
            }
            catch (IOException e) {
                log.error("Exception thrown while closing client instance " + getClientId() + " : ", e);
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
            log.info("CONNECTION STATUS UPDATE FOR CLIENT: " + getClientId() + " - " + status + ", " + statusChangeReason);
        }
        else
        {
            log.info("CONNECTION STATUS UPDATE FOR CLIENT: " + getClientId() + " - " + status + ", " + statusChangeReason + ", " + throwable.getMessage());
            throwable.printStackTrace();
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
                        log.debug("Attempting reconnect for client: " + getClientId() + " ...");
                        synchronized (lock) {
                            if (connectionStatus == ConnectionStatus.CONNECTED) {
                                try {
                                    closeClient();
                                } catch (Exception e) {
                                    log.warn("Client " + getClientId() + " closeNow failed.", e);
                                } finally {
                                    connectionStatus = ConnectionStatus.CONNECTING;
                                }
                            } else {
                                log.debug("Client `" + getClientId() + "` is currently connecting, or already connected; skipping...");
                                return;
                            }
                        }
                        try {
                            doConnect();
                        } catch (IOException e) {
                            log.error("Exception thrown while opening client instance: " + getClientId(), e);
                        }
                    }
                }).start();
            } else {
                connectionStatus = ConnectionStatus.DISCONNECTED;
            }
        }
    }
}
