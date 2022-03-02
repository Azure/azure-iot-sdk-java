/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.ProxySettings;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.Proxy;

import static junit.framework.TestCase.assertEquals;

public class ProxySettingsTest
{
    @Mocked
    Proxy mockProxy;

    @Mocked
    InetSocketAddress mockInetSocketAddress;

    @Test
    public void constructorSavesProxy()
    {
        //act
        ProxySettings proxySettings = new ProxySettings(mockProxy);

        //assert
        Proxy actualProxy = Deencapsulation.getField(proxySettings, "proxy");
        assertEquals(mockProxy, actualProxy);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForDirectProxy()
    {
        //arrange
        new Expectations()
        {
            {
                mockProxy.type();
                result = Proxy.Type.DIRECT;
            }
        };

        //act
        new ProxySettings(mockProxy);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullProxy()
    {
        //act
        new ProxySettings(null);
    }

    @Test
    public void getHostnameGetsHostnameFromAddress()
    {
        //arrange
        final String expectedHostname = "127.0.0.1";
        new Expectations()
        {
            {
                mockProxy.address();
                result = mockInetSocketAddress;

                mockInetSocketAddress.getHostName();
                result = expectedHostname;
            }
        };

        ProxySettings proxySettings = new ProxySettings(mockProxy);

        //act
        String actualHostname = proxySettings.getHostname();

        //assert
        assertEquals(expectedHostname, actualHostname);
    }

    @Test
    public void getPortGetsPortFromAddress()
    {
        //arrange
        final int expectedPort = 1234;
        new Expectations()
        {
            {
                mockProxy.address();
                result = mockInetSocketAddress;

                mockInetSocketAddress.getPort();
                result = expectedPort;
            }
        };

        ProxySettings proxySettings = new ProxySettings(mockProxy);

        //act
        int actualPort = proxySettings.getPort();

        //assert
        assertEquals(expectedPort, actualPort);
    }
}
