/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.androidthings.iothubservices.errorinjection.twin;

import com.microsoft.azure.sdk.iot.androidthings.BuildConfig;
import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.errorinjection.ReportedPropertiesErrInjTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.bouncycastle.operator.OperatorCreationException;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ReportedPropertiesErrInjDeviceThingsRunner extends ReportedPropertiesErrInjTests
{
    static Collection<BaseDevice> identities;

    @Rule
    public Rerun count = new Rerun(3);

    public ReportedPropertiesErrInjDeviceThingsRunner(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{2}_{3}_{4}")
    public static Collection inputsCommons() throws Exception
    {
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        Collection inputs = inputsCommon(ClientType.DEVICE_CLIENT);
        identities = getIdentities(inputs);
        return inputs;
    }

    @AfterClass
    public static void cleanUpResources()
    {
        tearDown(identities);
    }
}
