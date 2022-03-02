// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for Response Message class.
 */
public class ResponseMessageTest
{
    /* Tests_SRS_RESPONSEMESSAGE_21_001: [The constructor shall save the message body by calling super with the body as parameter.] */
    /* Tests_SRS_RESPONSEMESSAGE_21_003: [The constructor shall save the status.] */
    @Test
    public void constructorSetsRequiredPrivateMembers()
    {
        // arrange
        final byte[] body = {'A', 'B', 'C', '\0'};
        final IotHubStatusCode status = IotHubStatusCode.OK;

        // act
        ResponseMessage msg = new ResponseMessage(body, status);

        // assert
        assertEquals(status, Deencapsulation.getField(msg, "status"));
        assertEquals(body, Deencapsulation.getField(msg, "body"));
    }

    /* Tests_SRS_RESPONSEMESSAGE_21_002: [If the message body is null, the constructor shall throw an IllegalArgumentException thrown by base constructor.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullBodyThrows()
    {
        // arrange
        final byte[] body = null;
        final IotHubStatusCode status = IotHubStatusCode.OK;

        // act
        ResponseMessage msg = new ResponseMessage(body, status);
    }

    /* Tests_SRS_RESPONSEMESSAGE_21_004: [If the message status is null, the constructor shall throw an IllegalArgumentException.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorNullStatusThrows()
    {
        // arrange
        final byte[] body = {'A', 'B', 'C', '\0'};
        final IotHubStatusCode status = null;

        // act
        ResponseMessage msg = new ResponseMessage(body, status);
    }

    /* Tests_SRS_RESPONSEMESSAGE_21_005: [The getStatus shall return the stored status.] */
    @Test
    public void getStatusReturnStatus()
    {
        // arrange
        final byte[] body = {'A', 'B', 'C', '\0'};
        final IotHubStatusCode status = IotHubStatusCode.OK;

        // act
        ResponseMessage msg = new ResponseMessage(body, status);

        // assert
        assertEquals(status, Deencapsulation.invoke(msg, "getStatus"));
    }

}
