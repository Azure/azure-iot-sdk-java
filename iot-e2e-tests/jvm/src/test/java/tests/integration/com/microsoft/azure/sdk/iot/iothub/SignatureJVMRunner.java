/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;

import com.microsoft.azure.sdk.iot.common.helpers.IotHubIntegrationTest;
import com.microsoft.azure.sdk.iot.deps.util.Base64;
import com.microsoft.azure.sdk.iot.device.auth.Signature;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Integration tests for Signature. */
public class SignatureJVMRunner extends IotHubIntegrationTest
{
    @Test
    public void signatureIsCorrect()
    {
        String resourceUri =
                "sdktesthub.private.azure-devices-int.net/devices/test8";
        String deviceKey = Base64.encodeBase64StringLocal("someKey".getBytes());
        long expiryTime = 1462333672;

        String testSig =
                new Signature(resourceUri, expiryTime, deviceKey).toString();

        String sigEncoded = new String(Base64.encodeBase64Local(testSig.getBytes()));

        assertEquals("bXVROU55OUJZZXJ0b1VZJTJCdHlpc0lHMTZiNjdwOFFIckRlZlhBNFYxcUxNJTNE", sigEncoded);
    }
}
