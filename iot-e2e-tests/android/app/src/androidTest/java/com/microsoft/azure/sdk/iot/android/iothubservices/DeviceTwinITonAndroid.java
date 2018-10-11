/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothubservices;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.Rerun;
import com.microsoft.azure.sdk.iot.common.iothubservices.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DeviceTwinITonAndroid extends DeviceTwinCommon
{
    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{2} with {3} auth using {4}")
    public static Collection inputsCommons() throws IOException, GeneralSecurityException
    {
        String privateKeyBase64Encoded = BuildConfig.IotHubPrivateKeyBase64;
        String publicKeyCertBase64Encoded = BuildConfig.IotHubPublicCertBase64;
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        x509Thumbprint = BuildConfig.IotHubThumbprint;
        privateKey = new String(Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes()));
        publicKeyCert = new String(Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes()));
        includeModuleClientTest = false;

        return DeviceTwinCommon.inputsCommon();
    }

    public DeviceTwinITonAndroid(String deviceId, String moduleId, IotHubClientProtocol protocol, AuthenticationType authenticationType, String clientType)
    {
        super(deviceId, moduleId, protocol, authenticationType, clientType);
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromTcpConnectionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromTcpConnectionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromTcpConnectionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsConnectionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsConnectionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsConnectionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsSessionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsSessionDrop() throws Exception
    {
    }

    @Test
    public void getDeviceTwinRecoveredFromAmqpsSessionDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSReqLinkrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsCBSReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsCBSRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsD2CDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsD2CLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsC2DLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromC2DDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsC2DLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsTwinReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsTwinRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsMethodReqLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void sendReportedPropertiesRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void subscribeToDesiredPropertiesRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromAmqpsMethodRespLinkDrop() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromGracefulShutdownAmqp() throws Exception
    {
    }

    @Ignore
    @Override
    @Test
    public void getDeviceTwinRecoveredFromGracefulShutdownMqtt() throws Exception
    {
    }
}
