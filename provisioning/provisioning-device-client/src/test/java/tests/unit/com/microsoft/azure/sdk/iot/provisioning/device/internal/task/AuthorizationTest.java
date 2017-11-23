/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.task;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.Authorization;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.net.ssl.SSLContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/*
    Unit Tests for Authorization
    Coverage : 100% lines, 100% method
 */
@RunWith(JMockit.class)
public class AuthorizationTest
{
    //SRS_Authorization_25_001: [ Constructor shall create null SasToken and null SSL Context ]
    @Test
    public void constructorSucceeds() throws Exception
    {
        //act
        Authorization testAuth = Deencapsulation.newInstance(Authorization.class);

        //assert
        assertNull(Deencapsulation.invoke(testAuth, "getSasToken"));
        assertNull(Deencapsulation.invoke(testAuth, "getSslContext"));
    }

    //SRS_Authorization_25_002: [ This method shall save the value of SSLContext. ]
    //SRS_Authorization_25_003: [ This method shall return the saved value of SSLContext. ]
    @Test
    public void setAndGetSSL(@Mocked SSLContext mockedSslContext) throws Exception
    {
        //arrange
        Authorization testAuth = Deencapsulation.newInstance(Authorization.class);

        //act
        Deencapsulation.invoke(testAuth, "setSslContext", mockedSslContext);
        //assert
        assertEquals(mockedSslContext, Deencapsulation.invoke(testAuth, "getSslContext"));
    }

    //SRS_Authorization_25_004: [ This method shall save the value of sasToken. ]
    //SRS_Authorization_25_005: [ This method shall return the saved value of sasToken. ]
    @Test
    public void setAndGetSasToken() throws Exception
    {
        //arrange
        Authorization testAuth = Deencapsulation.newInstance(Authorization.class);

        //act
        Deencapsulation.invoke(testAuth, "setSasToken", "testSasToken");
        //assert
        assertEquals("testSasToken", Deencapsulation.invoke(testAuth, "getSasToken"));
    }
}
