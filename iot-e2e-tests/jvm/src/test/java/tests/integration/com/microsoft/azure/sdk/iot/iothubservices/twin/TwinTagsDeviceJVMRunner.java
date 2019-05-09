/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices.twin;

import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.setup.DeviceTwinCommon;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin.TwinTagsTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

@RunWith(Parameterized.class)
public class TwinTagsDeviceJVMRunner extends TwinTagsTests
{
    public TwinTagsDeviceJVMRunner(IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(protocol, authenticationType, clientType, publicKeyCert, privateKey, x509Thumbprint);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0} with {1} auth using {2}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        return DeviceTwinCommon.inputsCommon(ClientType.DEVICE_CLIENT);
    }
}
