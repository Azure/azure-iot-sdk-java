/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices.twin;

import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.twin.DeviceTwinWithVersionTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DeviceTwinWithVersionJVMRunner extends DeviceTwinWithVersionTests
{
    public DeviceTwinWithVersionJVMRunner(IotHubClientProtocol protocol)
    {
        super(protocol);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{0}")
    public static Collection inputs() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        return inputsCommon();
    }
}
