/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;

import com.microsoft.azure.sdk.iot.device.auth.Signature;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;

/** Integration tests for Signature. */
@IotHubTest
public class SignatureTests extends IntegrationTest
{
    @Test
    public void signatureIsCorrect()
    {
        String resourceUri =
                "sdktesthub.private.azure-devices-int.net/devices/test8";
        String deviceKey = encodeBase64String("someKey".getBytes());
        long expiryTime = 1462333672;

        String testSig =
                new Signature(resourceUri, expiryTime, deviceKey).toString();

        String sigEncoded = new String(encodeBase64(testSig.getBytes()));

        assertEquals("bXVROU55OUJZZXJ0b1VZJTJCdHlpc0lHMTZiNjdwOFFIckRlZlhBNFYxcUxNJTNE", sigEncoded);
    }
}
