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
public class MultiplexClientManager extends ClientManagerBase
{
    /**
     * Define method calls that will not be delegated to the inner client.
     */
    private interface DeviceClientNonDelegatedFunction
    {
        void open();
        void close();
        void registerConnectionStatusChangeCallback(IotHubConnectionStatusChangeCallback callback, Object callbackContext);
    }

    /**
     * The methods defined in the interface DeviceClientNonDelegatedFunction will be called on MultiplexingClientManager, and not on MultiplexingClient.
     */
    @Delegate(excludes = DeviceClientNonDelegatedFunction.class)
    private final MultiplexingClient multiplexingClient;
    private final String multiplexClientId;

    /**
     * Creates an instance of the MultiplexClientManager
     *
     * @param multiplexingClient the multiplexing client
     * @param multiplexClientId user defined Id for the multiplexing client.
     */
    MultiplexClientManager(MultiplexingClient multiplexingClient, String multiplexClientId)
    {
        this.multiplexingClient = multiplexingClient;
        this.multiplexClientId = multiplexClientId;
        this.multiplexingClient.registerConnectionStatusChangeCallback(this, this);
    }

    /**
     * All classes that extend ClientManagerBase should implement how their inner client can be opened.
     */
    @Override
    protected void openClient() throws IOException
    {
        this.multiplexingClient.open();
    }

    /**
     * All classes that extend ClientManagerBase should implement how their inner client can be closed.
     */
    @Override
    protected void closeClient() throws IOException
    {
        this.multiplexingClient.close();
    }

    /**
     * All classes that extend ClientManagerBase should implement how their inner client can be identified for logging purposes.
     */
    @Override
    public String getClientId()
    {
        return multiplexClientId;
    }
}
