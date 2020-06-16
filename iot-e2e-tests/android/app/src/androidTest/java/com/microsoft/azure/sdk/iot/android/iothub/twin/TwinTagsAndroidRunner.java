/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.twin;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup16;
import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothub.twin.TwinTagsTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

@TestGroup16
@RunWith(Parameterized.class)
public class TwinTagsAndroidRunner extends TwinTagsTests
{
    @Rule
    public Rerun count = new Rerun(3);

    public TwinTagsAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method	
    @Parameterized.Parameters(name = "{0}_{1}_{2}")
    public static Collection inputsCommons() throws IOException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);
        return inputsCommon();
    }
}