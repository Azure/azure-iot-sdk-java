/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup16;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.FlakeyTest;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.MultiplexingClientTests;

@TestGroup16
@RunWith(Parameterized.class)
public class MultiplexingClientAndroidRunner extends MultiplexingClientTests
{
    public MultiplexingClientAndroidRunner(IotHubClientProtocol protocol)
    {
        super(protocol);
    }

    // This test is a bit too heavy for android to reliably pass, even for nightly builds
    @Ignore
    @Override
    public void sendMessagesMaxDevicesAllowed() {

    }

    // This test is a bit too heavy for android to reliably pass, but it can still be run during nightly builds
    @FlakeyTest
    @Override
    public void multiplexedConnectionRecoversFromDeviceSessionDropsParallel() {

    }

    // This test is a bit too heavy for android to reliably pass, but it can still be run during nightly builds
    @FlakeyTest
    @Override
    public void multiplexedConnectionRecoversFromDeviceSessionDropsSequential() {

    }

    // This test is a bit too heavy for android to reliably pass, but it can still be run during nightly builds
    @FlakeyTest
    @Override
    public void multiplexedConnectionRecoversFromTcpConnectionDrop() {

    }
}
