/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothub.errorinjection.messaging;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup16;
import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothub.errorinjection.SendMessagesErrInjTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.After;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;

@TestGroup16
@RunWith(Parameterized.class)
public class SendMessagesErrInjModuleAndroidRunner extends SendMessagesErrInjTests
{
    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    public SendMessagesErrInjModuleAndroidRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint, boolean useHttpProxy) throws Exception
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint, useHttpProxy);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}_{1}_{2}_{6}")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException, InterruptedException
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        isBasicTierHub = Boolean.parseBoolean(BuildConfig.IsBasicTierHub);

        if (!isBasicTierHub)
        {
            return inputsCommon(ClientType.MODULE_CLIENT);
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}

