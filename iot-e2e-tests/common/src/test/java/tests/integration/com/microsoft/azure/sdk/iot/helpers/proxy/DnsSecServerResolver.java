package tests.integration.com.microsoft.azure.sdk.iot.helpers.proxy;


import org.littleshoot.dnssec4j.VerifiedAddressFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public class DnsSecServerResolver implements HostResolver
{
    @Override
    public InetSocketAddress resolve(String host, int port)
            throws UnknownHostException {
        return VerifiedAddressFactory.newInetSocketAddress(host, port, true);
    }
}
