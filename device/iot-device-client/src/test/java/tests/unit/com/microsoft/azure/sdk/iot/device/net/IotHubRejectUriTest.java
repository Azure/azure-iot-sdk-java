// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.net;

import com.microsoft.azure.sdk.iot.device.net.IotHubRejectUri;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/** Unit tests for IotHubRejectUri. */
public class IotHubRejectUriTest
{
    /** The e-tag will be interpolated where the '%s' is placed. */
    protected static String REJECT_PATH_FORMAT = "/messages/devicebound/%s";

    @Mocked IotHubUri mockIotHubUri;

    // Tests_SRS_IOTHUBREJECTURI_11_001: [The constructor returns a URI with the format "[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]??reject=true&api-version=2016-02-03" (the query parameters can be in any order).]
    @Test
    public void constructorConstructsIotHubUriCorrectly()
            throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String eTag = "test-etag";
        final String rejectPath = String.format(REJECT_PATH_FORMAT, eTag);

        new IotHubRejectUri(iotHubHostname, deviceId, eTag, null);

        new Verifications()
        {
            {
                new IotHubUri(iotHubHostname, deviceId, rejectPath,
                        (Map<String, String>) any, null);
            }
        };
    }

    // Tests_SRS_IOTHUBREJECTURI_11_002: [The string representation of the IoT Hub event URI shall be constructed with the format "[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]??reject=true&api-version=2016-02-03" (the query parameters can be in any order).]
    @Test
    public void toStringIsCorrect() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String eTag = "test-etag";
        final String uriStr = "test-uri-str";
        new NonStrictExpectations()
        {
            {
                mockIotHubUri.toString();
                result = uriStr;
            }
        };
        IotHubRejectUri rejectUri =
                new IotHubRejectUri(iotHubHostname, deviceId, eTag, null);

        String testUriStr = rejectUri.toString();

        assertThat(testUriStr, is(uriStr));
    }

    // Tests_SRS_IOTHUBREJECTURI_11_003: [The function shall return the hostname given in the constructor.] 
    @Test
    public void getHostnameIsCorrect() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String eTag = "test-etag";
        final String hostname = "test-hostname";
        new NonStrictExpectations()
        {
            {
                mockIotHubUri.getHostname();
                result = hostname;
            }
        };
        IotHubRejectUri rejectUri =
                new IotHubRejectUri(iotHubHostname, deviceId, eTag, null);

        String testHostname = rejectUri.getHostname();

        assertThat(testHostname, is(hostname));
    }

    // Tests_SRS_IOTHUBREJECTURI_11_004: [The function shall return a URI with the format '/devices/[deviceId]/messages/devicebound/[eTag].]
    @Test
    public void getPathIsCorrect() throws URISyntaxException
    {
        final String iotHubHostname = "test.iothub";
        final String deviceId = "test-deviceid";
        final String eTag = "test-etag";
        final String path = "test-path";
        new NonStrictExpectations()
        {
            {
                mockIotHubUri.getPath();
                result = path;
            }
        };
        IotHubRejectUri rejectUri =
                new IotHubRejectUri(iotHubHostname, deviceId, eTag, null);

        String testPath = rejectUri.getPath();

        assertThat(testPath, is(path));
    }
}
