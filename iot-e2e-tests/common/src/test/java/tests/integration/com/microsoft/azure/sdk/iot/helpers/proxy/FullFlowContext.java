package tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.ClientToProxyConnection;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy.impl.ProxyToServerConnection;

/**
 * Extension of {@link FlowContext} that provides additional information (which
 * we know after actually processing the request from the client).
 */
public class FullFlowContext extends FlowContext
{
    private final String serverHostAndPort;
    private final ChainedProxy chainedProxy;

    public FullFlowContext(ClientToProxyConnection clientConnection,
                           ProxyToServerConnection serverConnection) {
        super(clientConnection);
        this.serverHostAndPort = serverConnection.getServerHostAndPort();
        this.chainedProxy = serverConnection.getChainedProxy();
    }

    /**
     * The host and port for the server (i.e. the ultimate endpoint).
     * 
     * @return
     */
    public String getServerHostAndPort() {
        return serverHostAndPort;
    }

    /**
     * The chained proxy (if proxy chaining).
     * 
     * @return
     */
    public ChainedProxy getChainedProxy() {
        return chainedProxy;
    }

}
