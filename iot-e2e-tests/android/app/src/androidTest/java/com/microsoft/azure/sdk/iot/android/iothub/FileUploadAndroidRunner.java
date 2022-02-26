/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub;

import com.microsoft.azure.sdk.iot.android.helper.TestGroup10;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;

import tests.integration.com.microsoft.azure.sdk.iot.iothub.FileUploadTests;

@TestGroup10
@RunWith(Parameterized.class)
public class FileUploadAndroidRunner extends FileUploadTests
{
    public FileUploadAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, boolean withProxy) throws IOException {
        super(protocol, authenticationType, withProxy);
    }
}
