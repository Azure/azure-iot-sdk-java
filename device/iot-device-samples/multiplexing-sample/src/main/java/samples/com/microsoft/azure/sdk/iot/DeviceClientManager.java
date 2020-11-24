package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * This class is in charge of handling reconnection logic and registering callbacks for connection status changes.
 * It will delegate all other calls other than `Open`, `Close` and registerConnectionStatusChangeCallbaack to the inner client (DeviceClient)
 */
@Slf4j
public class DeviceClientManager extends ClientManagerBase
{
    // Define method calls that will not be delegated to the inner client.
    private interface DeviceClientNonDelegatedFunction
    {
        void open();
        void closeNow();
        void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
    }

    // The methods defined in the interface DeviceClientNonDelegatedFunction will be called on DeviceClientManager, and not on DeviceClient.
    @Delegate(excludes = DeviceClientNonDelegatedFunction.class)
    private final DeviceClient client;

    /**
     * Creates an instance of DeviceClientManager
     * @param deviceClient the DeviceClient to manage
     * @param dependencyConnectionStatusTracker the dependency connection status tracker (it may be the MultiplexClientManager object)
     */
    DeviceClientManager(DeviceClient deviceClient, ConnectionStatusTracker dependencyConnectionStatusTracker)
    {
        this.dependencyConnectionStatusTracker = dependencyConnectionStatusTracker;
        lastKnownConnectionStatus = ConnectionStatus.DISCONNECTED;
        client = deviceClient;
        client.registerConnectionStatusChangeCallback(this, this);
    }

    @Override
    public void openClient() throws IOException
    {
        client.open();
    }

    @Override
    public void closeClient() throws IOException
    {
        client.closeNow();
    }

    @Override
    public String getClientId()
    {
        return  client.getConfig().getDeviceId();
    }
}
