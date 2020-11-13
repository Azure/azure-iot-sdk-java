/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup16;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;

import tests.integration.com.microsoft.azure.sdk.iot.iothub.MultiplexingClientTests;
import tests.integration.com.microsoft.azure.sdk.iot.iothub.TransportClientTests;

@TestGroup16
@RunWith(Parameterized.class)
public class MultiplexingClientAndroidRunner extends MultiplexingClientTests
{
    public MultiplexingClientAndroidRunner(IotHubClientProtocol protocol)
    {
        super(protocol);
    }

    // This test is a bit too heavy for android to reliably pass
    @Ignore
    @Override
    public void sendMessagesMaxDevicesAllowed() throws Exception
    {

    }
}
