// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.device.transport;

import com.microsoft.azure.sdk.iot.device.IotHubMethod;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for IotHubTransportMessage
 * 100% methods, 100% lines covered
 */
public class IotHubTransportMessageTest
{
    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_001: [The constructor shall call the supper class with the body. This function do not evaluates this parameter.] */
    @Test
    public void constructorSuccess()
    {
        // arrange
        String body = "This is a valid body";

        // act
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage(body);

        // assert
        assertEquals(body, new String(iotHubTransportMessage.getBytes()));
    }

    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_002: [The setIotHubMethod shall store the iotHubMethod. This function do not evaluates this parameter.] */
    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_004: [The getIotHubMethod shall return the stored iotHubMethod.] */
    @Test
    public void setGetIotHubMethodSuccess()
    {
        // arrange
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage("This is a valid body");

        // act
        iotHubTransportMessage.setIotHubMethod(IotHubMethod.POST);

        // assert
        assertEquals(IotHubMethod.POST, iotHubTransportMessage.getIotHubMethod());
    }

    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_003: [The setUriPath shall store the uriPath. This function do not evaluates this parameter.] */
    /* Tests_SRS_IOTHUBTRANSPORTMESSAGE_21_005: [The getUriPath shall return the stored uriPath.] */
    @Test
    public void setGetUriPathSuccess()
    {
        // arrange
        String uriPath = "valid/uri";
        IotHubTransportMessage iotHubTransportMessage = new IotHubTransportMessage("This is a valid body");

        // act
        iotHubTransportMessage.setUriPath(uriPath);

        // assert
        assertEquals(uriPath, iotHubTransportMessage.getUriPath());
    }
}
