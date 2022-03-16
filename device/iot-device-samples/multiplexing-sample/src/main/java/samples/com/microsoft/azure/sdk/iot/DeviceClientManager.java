package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubConnectionStatusChangeCallback;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * This class is in charge of handling reconnection logic and registering callbacks for connection status changes.
 * It will delegate all other calls other than `Open`, `Close` and setConnectionStatusChangeCallback to the inner client (DeviceClient)
 */
@Slf4j
public class DeviceClientManager extends ClientManagerBase
{
    /**
     * Define method calls that will not be delegated to the inner client.
     */
    private interface DeviceClientNonDelegatedFunction
    {
        void open();
        void close();
        void setConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
    }

    /**
     * The methods defined in the interface DeviceClientNonDelegatedFunction will be called on DeviceClientManager, and not on DeviceClient.
     */
    @Delegate(excludes = DeviceClientNonDelegatedFunction.class)
    private final DeviceClient deviceClient;

    /**
     * Creates an instance of DeviceClientManager
     * @param deviceClient the DeviceClient to manage
     * @param dependencyConnectionStatusTracker the dependency connection status tracker (it may be the MultiplexingClientManager object)
     */
    DeviceClientManager(DeviceClient deviceClient, ConnectionStatusTracker dependencyConnectionStatusTracker)
    {
        this.dependencyConnectionStatusTracker = dependencyConnectionStatusTracker;
        this.deviceClient = deviceClient;
        this.deviceClient.setConnectionStatusChangeCallback(this, this);
    }

    /**
     * All classes that extend ClientManagerBase should implement how their inner client can be opened.
     */
    @Override
    protected void openClient() throws IOException
    {
        deviceClient.open(true);
    }

    /**
     * All classes that extend ClientManagerBase should implement how their inner client can be closed.
     */
    @Override
    protected void closeClient() throws IOException
    {
        deviceClient.close();
    }

    /**
     * All classes that extend ClientManagerBase should implement how their inner client can be identified for logging purposes.
     */
    @Override
    public String getClientId()
    {
        return  deviceClient.getConfig().getDeviceId();
    }
}
