// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package tests.integration.com.microsoft.azure.sdk.iot.longhaul.helpers;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The possible parameters for parameterized longhaul test to use.
 */
@AllArgsConstructor
public class TestParameters
{
    @Getter
    final private IotHubClientProtocol protocol;

    @Getter
    final private AuthenticationType authenticationType;

    // This override makes it so that the test name of each parameterized test looks like "someLonghaulTest[<protocol> <authenticationType>]"
    @Override
    public String toString()
    {
        return protocol + " " + authenticationType;
    }
}
