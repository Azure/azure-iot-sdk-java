/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub;

import com.microsoft.azure.sdk.iot.device.net.IotHubEventUri;
import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** Integration tests for IotHubEventUri. */
@IotHubTest
public class IotHubEventUriTests extends IntegrationTest
{
    @Test
    public void eventUriIsCorrect() throws URISyntaxException
    {
        String iotHubName = "test.iothub";
        String deviceId = "test-deviceid";
        IotHubEventUri uri = new IotHubEventUri(iotHubName, deviceId, "");

        String testUriStr = uri.toString();

        String expectedUriStr = "test.iothub/devices/test-deviceid/messages/events?api-version=" + TransportUtils.IOTHUB_API_VERSION;
        assertThat(testUriStr, is(expectedUriStr));
    }
}
