/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package tests.integration.com.microsoft.azure.sdk.iot.provisioning;

import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.X509Cert;

public class ProvisioningClientIT
{
    IotHubClientProtocol [] iotHubClientProtocols = {IotHubClientProtocol.MQTT, IotHubClientProtocol.MQTT_WS, IotHubClientProtocol.AMQPS, IotHubClientProtocol.AMQPS_WS, IotHubClientProtocol.HTTPS};

    @Before
    public void setUp() throws Exception
    {
        
        /*X509Cert x509Cert = new X509Cert(1, false);

        System.out.println("getPrivateKeyLeafPem \n " +  x509Cert.getPrivateKeyLeafPem());
        System.out.println("getPublicCertLeafPem\n" + x509Cert.getPublicCertLeafPem());
        System.out.println("getIntermediatesPem \n" + x509Cert.getIntermediatesPem());
        System.out.println("getPublicCertRootPem \n" + x509Cert.getPublicCertRootPem());*/
    }

    @After
    public void tearDown() throws Exception
    {

    }

    @Test
    public void individualEnrollmentTPMHardware() throws Exception
    {

    }

    @Test
    public void individualEnrollmentTPMSimulator() throws Exception
    {

    }

    @Test
    public void individualEnrollmentX509() throws Exception
    {

    }

    @Test
    public void groupEnrollmentX509WithZeroIntermediate() throws Exception
    {

    }

    @Test
    public void groupEnrollmentX509WithOneIntermediate() throws Exception
    {

    }

    @Test
    public void groupEnrollmentX509WithTwoIntermediate() throws Exception
    {

    }

    @Test
    public void groupEnrollmentDiceWithOneIntermediateEmulator() throws Exception
    {

    }

    @Test
    public void individualEnrollmentDiceEmulator() throws Exception
    {

    }


    @Test
    public void groupEnrollmentDiceWithOneIntermediate() throws Exception
    {

    }

    @Test
    public void individualEnrollmentDice() throws Exception
    {

    }
}
