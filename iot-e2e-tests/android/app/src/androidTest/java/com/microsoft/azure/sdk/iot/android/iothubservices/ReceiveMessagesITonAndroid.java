package com.microsoft.azure.sdk.iot.android.iothubservices;

import com.microsoft.appcenter.espresso.Factory;
import com.microsoft.appcenter.espresso.ReportHelper;
import com.microsoft.azure.sdk.iot.android.BuildConfig;
import com.microsoft.azure.sdk.iot.common.iothubservices.ReceiveMessagesCommon;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.InternalClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.exceptions.ModuleClientException;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.Module;
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

@RunWith(Parameterized.class)
public class ReceiveMessagesITonAndroid extends ReceiveMessagesCommon
{
    @Rule
    public ReportHelper reportHelper = Factory.getReportHelper();

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {4} auth using {5}")
    public static Collection inputs() throws IOException, IotHubException, GeneralSecurityException, URISyntaxException, ModuleClientException
    {
        String privateKeyBase64Encoded = BuildConfig.IotHubPrivateKeyBase64;
        String publicKeyCertBase64Encoded = BuildConfig.IotHubPublicCertBase64;
        iotHubConnectionString = BuildConfig.IotHubConnectionString;
        x509Thumbprint = BuildConfig.IotHubThumbprint;
        privateKey = new String(Base64.decodeBase64Local(privateKeyBase64Encoded.getBytes()));
        publicKeyCert = new String(Base64.decodeBase64Local(publicKeyCertBase64Encoded.getBytes()));
        includeModuleClientTest = false;

        return ReceiveMessagesCommon.inputsCommon();
    }

    public ReceiveMessagesITonAndroid(InternalClient client, IotHubClientProtocol protocol, Device device, Module module, AuthenticationType authenticationType, String clientType)
    {
        super(client, protocol, device, module, authenticationType, clientType);
    }

    @Ignore
    @Override
    @Test
    public void receiveMessagesWithTCPConnectionDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsConnectionDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsSessionDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsCBSReqLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsCBSRespLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsD2CLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsC2DLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsMethodReqLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsMethodRespLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsTwinReqLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithAmqpsTwinRespLinkDrop() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithGracefulShutdownAmqp() throws IOException, IotHubException, InterruptedException
    {
    }

    @Ignore
    @Override
    @Test 
    public void receiveMessagesWithGracefulShutdownMqtt() throws IOException, IotHubException, InterruptedException
    {
    }
}
