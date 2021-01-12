// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.net;

import com.microsoft.azure.sdk.iot.device.net.IotHubMessageUri;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** Unit tests for IotHubMessageUri. */
public class IotHubMessageUriTest
{
    protected static String MESSAGE_PATH = "/messages/devicebound";

    @Mocked IotHubUri mockIotHubUri;

    // Tests_SRS_IOTHUBMESSAGEURI_11_001: [The constructor returns a URI with the format "[iotHubHostname]/devices/[deviceId]/messages/devicebound?api-version=2016-02-03".]
    @Test
    public void constructorConstructsIotHubUriCorrectly()
            throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";

        new IotHubMessageUri(iotHubHostname, deviceId, "");

        new Verifications()
        {
            {
                new IotHubUri(iotHubHostname, deviceId, MESSAGE_PATH, "");
            }
        };
    }

    // Tests_SRS_IOTHUBMESSAGEURI_11_002: [The string representation of the IoT Hub event URI shall be constructed with the format "[iotHubHostname]/devices/[deviceId]/messages/devicebound?api-version=2016-02-03".]
    @Test
    public void toStringIsCorrect() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String uriStr = "test-uri-str";
        new NonStrictExpectations()
        {
            {
                mockIotHubUri.toString();
                result = uriStr;
            }
        };
        IotHubMessageUri messageUri =
                new IotHubMessageUri(iotHubHostname, deviceId, "");

        String testUriStr = messageUri.toString();

        assertThat(testUriStr, is(uriStr));
    }

    // Tests_SRS_IOTHUBMESSAGEURI_11_003: [The function shall return the hostname given in the constructor.] 
    @Test
    public void getHostnameIsCorrect() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String hostname = "test-hostname";
        new NonStrictExpectations()
        {
            {
                mockIotHubUri.getHostname();
                result = hostname;
            }
        };
        IotHubMessageUri messageUri =
                new IotHubMessageUri(iotHubHostname, deviceId, "");

        String testHostname = messageUri.getHostname();

        assertThat(testHostname, is(hostname));
    }

    // Tests_SRS_IOTHUBMESSAGEURI_11_004: [The function shall return a URI with the format '/devices/[deviceId]/messages/devicebound.]
    @Test
    public void getPathIsCorrect() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                mockIotHubUri.getPath();
                result = path;
            }
        };
        IotHubMessageUri messageUri =
                new IotHubMessageUri(iotHubHostname, deviceId, "");

        String testPath = messageUri.getPath();

        assertThat(testPath, is(path));
    }
}
