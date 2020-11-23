package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.MultiplexingClient;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * This class is in charge of handling reconnection logic and registering callbacks for connection status changes.
 * It will delegate all other calls other than `Open`, `Close` and registerConnectionStatusChangeCallbaack to the inner client (MultiplexingClient)
 */
@Slf4j
public class MultiplexClientManager extends ClientManagerBase {

    // Define method calls that will not be delegated to the inner client.
    private interface DeviceClientNonDelegatedFunction {
        void open();
        void close();
        void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
    }

    // The methods defined in the interface DeviceClientNonDelegatedFunction will be called on MultiplexingClientManager, and not on MultiplexingClient.
    @Delegate(excludes = DeviceClientNonDelegatedFunction.class)
    private final MultiplexingClient client;
    private final String multiplexClientId;

    /**
     * Creates an instance of the MultiplexClientManager
     *
     * @param multiplexingClient the multiplexing client
     * @param multiplexClientId user defined Id for the multiplexing client.
     */
    MultiplexClientManager(MultiplexingClient multiplexingClient, String multiplexClientId) {
        this.connectionStatus = ConnectionStatus.DISCONNECTED;
        this.client = multiplexingClient;
        this.multiplexClientId = multiplexClientId;
        this.client.registerConnectionStatusChangeCallback(this, this);
    }

    @Override
    public void openClient() throws IOException {
        this.client.open();
    }

    @Override
    public void closeClient() throws IOException {
        this.client.close();
    }

    @Override
    public String getClientId() {
        return  multiplexClientId;
    }

    public MultiplexingClient getMultiplexClient(){
        return client;
    }
}
