/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothubservices.methods;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.TestGroup5;
import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.DeviceTestManager;
import com.microsoft.azure.sdk.iot.common.helpers.Rerun;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.methods.DeviceMethodTests;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;

@TestGroup5
@RunWith(Parameterized.class)
public class DeviceMethodDeviceAndroidRunner extends DeviceMethodTests
{
    static Collection<BaseDevice> identities;
    static ArrayList<DeviceTestManager> testManagers;

    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    public DeviceMethodDeviceAndroidRunner(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceTestManager, protocol, authenticationType, clientType, identity, publicKeyCert, privateKey, x509Thumbprint);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1}_{2}_{3}")
    public static Collection inputsCommons() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, InterruptedException, ModuleClientException
    {
        String privateKeyBase64Encoded = BuildConfig.IotHubPrivateKeyBase64;
        String publicKeyCertBase64Encoded = BuildConfig.IotHubPublicCertBase64;
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        String x509Thumbprint = BuildConfig.IotHubThumbprint;
        String privateKey = new String(Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes()));
        String publicKeyCert = new String(Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes()));

        Collection inputs = inputsCommon(ClientType.DEVICE_CLIENT, publicKeyCert, privateKey, x509Thumbprint);
        Object[] inputsArray = inputs.toArray();

        testManagers = new ArrayList<>();
        for (int i = 0; i < inputsArray.length; i++)
        {
            Object[] inputCollection = (Object[])inputsArray[i];
            testManagers.add((DeviceTestManager) inputCollection[0]);
        }

        identities = getIdentities(inputs);

        return inputs;
    }

    @AfterClass
    public static void cleanUpResources()
    {
        tearDown(identities, testManagers);
    }

    @After
    public void labelSnapshot()
    {
        reportHelper.label("Stopping App");
    }
}
