/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup13;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;

import tests.integration.com.microsoft.azure.sdk.iot.iothub.twin.TwinWithVersionTests;

@TestGroup13
@RunWith(Parameterized.class)
public class TwinWithVersionAndroidRunner extends TwinWithVersionTests
{
    public TwinWithVersionAndroidRunner(IotHubClientProtocol protocol) throws IOException
    {
        super(protocol);
    }
}
