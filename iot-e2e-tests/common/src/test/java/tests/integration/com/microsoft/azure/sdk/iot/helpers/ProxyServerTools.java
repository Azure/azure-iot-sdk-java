/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.github.monkeywie.proxyee.server.HttpProxyServer;

import java.util.concurrent.TimeUnit;

/**
 * Helpers for the local HTTP proxy servers that the proxy related integration tests run their traffic through.
 */
public class ProxyServerTools
{
    private static final int PROXY_START_TIMEOUT_SECONDS = 30;

    /**
     * Start the provided proxy server on the provided port and block until it is actually listening on that port.
     *
     * {@link HttpProxyServer#startAsync(int)} only initiates the bind, so tests that don't wait on the returned future
     * can start sending traffic to the proxy before it is listening. When that happens, the client under test gets a
     * "Connection refused" instead of a working proxy.
     *
     * @param proxyServer the proxy server to start.
     * @param port the port for the proxy server to listen on.
     * @throws Exception if the proxy server could not be started within {@link #PROXY_START_TIMEOUT_SECONDS} seconds.
     */
    public static void startProxyServer(HttpProxyServer proxyServer, int port) throws Exception
    {
        proxyServer.startAsync(port).toCompletableFuture().get(PROXY_START_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
