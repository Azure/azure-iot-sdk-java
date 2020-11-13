package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import com.microsoft.azure.sdk.iot.device.MultiplexingClient;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class MultiplexClientManager extends ClientManagerBase {

    private interface DeviceClientNonDelegatedFunction {
        void open();
        void close();
        void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
    }

    // The methods defined in the interface DeviceClientNonDelegatedFunction will be called on DeviceClientManager, and not on DeviceClient.
    @Delegate(excludes = DeviceClientNonDelegatedFunction.class)
    private final MultiplexingClient client;
    private final String multiplexClientId;

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
