/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.common.helpers;

import org.junit.Before;

import static org.junit.Assume.assumeTrue;

public class IotHubIntegrationTest extends IntegrationTest
{
    @Override
    public boolean isProvisioningTest()
    {
        return false;
    }

    @Override
    public boolean isIotHubTest()
    {
        return true;
    }
}