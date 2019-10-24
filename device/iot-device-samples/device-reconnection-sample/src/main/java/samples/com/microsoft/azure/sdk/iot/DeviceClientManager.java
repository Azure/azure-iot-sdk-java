package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class DeviceClientManager {
    private static final Object lock = new Object();
    private static boolean connecting;
    @Delegate
    private DeviceClient client;

    DeviceClientManager(DeviceClient deviceClient) {
        this.client = deviceClient;
        this.client.registerConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackHandler(), this);
    }

    void connect() {
        // Device client does not have retry on the initial open() call. Will need to be re-opened by the calling application
        while (true) {
            try {
                log.debug("[connect] - Opening the device client instance...");
                client.open();
                break;
            }
            catch (Exception ex) {
                log.error("[connect] - Exception thrown while opening DeviceClient instance.");
                ex.printStackTrace();
            }
            try {
                log.debug("[connect] - Sleeping for 10 secs before attempting another open()");
                Thread.sleep(10 * 1000);
            } catch (InterruptedException ex) {
                log.error("[connect] - Exception in thread sleep");
                ex.printStackTrace();
            }
        }

    }

    void reconnect() {
        log.debug("[reconnect] - Attempting reconnect for device client...");
        synchronized (lock) {
            if (connecting) {
                log.debug("[reconnect] - DeviceClient is currently connecting, or already connected; skipping...");
                return;
            }
            disconnect();
            log.debug("[reconnect] - DeviceClient Disconnected");
            connecting = true;
        }

        connect();
        synchronized(lock) {
            connecting = false;
        }
    }

    void disconnect() {
        try {
            log.debug("[disconnect] - Closing the device client instance...");
            client.closeNow();
        }
        catch (IOException e) {
            log.error("[disconnect] - Exception thrown while closing DeviceClient instance.");
            e.printStackTrace();
        }
    }
}
