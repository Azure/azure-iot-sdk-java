/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothubservices.methods;

import com.microsoft.azure.sdk.iot.common.helpers.ClientType;
import com.microsoft.azure.sdk.iot.common.helpers.DeviceTestManager;
import com.microsoft.azure.sdk.iot.common.helpers.TestConstants;
import com.microsoft.azure.sdk.iot.common.helpers.Tools;
import com.microsoft.azure.sdk.iot.common.tests.iothubservices.methods.DeviceMethodTests;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.BaseDevice;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@RunWith(Parameterized.class)
public class DeviceMethodModuleJVMRunner extends DeviceMethodTests
{
    static Collection<BaseDevice> identities;
    static ArrayList<DeviceTestManager> testManagers;

    public DeviceMethodModuleJVMRunner(DeviceTestManager deviceTestManager, IotHubClientProtocol protocol, AuthenticationType authenticationType, ClientType clientType, BaseDevice identity, String publicKeyCert, String privateKey, String x509Thumbprint)
    {
        super(deviceTestManager, protocol, authenticationType, clientType, identity, publicKeyCert, privateKey, x509Thumbprint);
    }

    //This function is run before even the @BeforeClass annotation, so it is used as the @BeforeClass method
    @Parameterized.Parameters(name = "{1} with {2} auth using {3}")
    public static Collection inputs() throws Exception
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        if (!isBasicTierHub)
        {
            Collection inputs = inputsCommon(ClientType.MODULE_CLIENT);
            Object[] inputsArray = inputs.toArray();

            testManagers = new ArrayList<>();
            for (int i = 0; i < inputsArray.length; i++)
            {
                Object[] inputCollection = (Object[]) inputsArray[i];
                testManagers.add((DeviceTestManager) inputCollection[0]);
            }

            identities = getIdentities(inputs);

            return inputs;
        }
        else
        {
            return Collections.EMPTY_LIST;
        }
    }

    @AfterClass
    public static void cleanUpResources()
    {
        tearDown(identities, testManagers);
    }
}
