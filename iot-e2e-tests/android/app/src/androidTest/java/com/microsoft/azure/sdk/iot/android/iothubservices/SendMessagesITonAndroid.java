/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.android.iothubservices;

import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.android.helper.Rerun;
import com.microsoft.azure.sdk.iot.common.iothubservices.SendMessagesCommon;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;

@RunWith(Parameterized.class)
public class SendMessagesITonAndroid extends SendMessagesCommon
{
    @Rule
    public Rerun count = new Rerun(3);

    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1}_{3}_{4}")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        String privateKeyBase64Encoded = BuildConfig.IotHubPrivateKeyBase64;
        String publicKeyCertBase64Encoded = BuildConfig.IotHubPublicCertBase64;
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        x509Thumbprint = BuildConfig.IotHubThumbprint;
        privateKey = new String(Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes()));
        publicKeyCert = new String(Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes()));
        includeModuleClientTest = false;

        return SendMessagesCommon.inputsCommon();
    }

    public SendMessagesITonAndroid(InternalClient client, IotHubClientProtocol protocol, Device device, AuthenticationType authenticationType, String clientType)
    {
        super(client, protocol, device, authenticationType, clientType);
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesWithTcpConnectionDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithConnectionDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithSessionDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithCbsRequestLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithCbsResponseLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithD2CLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithC2DLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithMethodReqLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithMethodRespLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithTwinReqLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithTwinRespLinkDrop() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesWithThrottling() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesWithThrottlingNoRetry() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesWithAuthenticationError() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesWithQuotaExceeded() throws URISyntaxException, IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverAmqpWithGracefulShutdown() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesOverMqttWithGracefulShutdown() throws IOException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test
    public void sendMessagesWithTcpConnectionDropNotifiesUserIfRetryExpires() throws IOException, InterruptedException
    {
    }
}