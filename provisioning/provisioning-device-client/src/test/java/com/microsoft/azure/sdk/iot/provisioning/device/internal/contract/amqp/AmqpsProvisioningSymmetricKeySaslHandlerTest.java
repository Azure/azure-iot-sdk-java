/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.AmqpsProvisioningSymmetricKeySaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import mockit.Deencapsulation;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for AmqpsProvisioningSymmetricKeySaslHandler.java
 * Method: 100%
 * Lines: 93%
 */
@RunWith(JMockit.class)
public class AmqpsProvisioningSymmetricKeySaslHandlerTest
{
    private static final String idScope = "5";
    private static final String registrationId = "3";
    private static final String sasToken = "6";

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_001: [This constructor shall save the provided idScope, registrationId, and sas token.]
    @Test
    public void constructorSavesArgumentValues()
    {
        //act
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //assert
        String actualIdScope = Deencapsulation.getField(handler, "idScope");
        String actualRegistrationId = Deencapsulation.getField(handler, "registrationId");
        String actualSasToken = Deencapsulation.getField(handler, "sasToken");

        assertEquals(idScope, actualIdScope);
        assertEquals(registrationId, actualRegistrationId);
        assertEquals(sasToken, actualSasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullIdScope()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, null, registrationId, sasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyIdScope()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, "", registrationId, sasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullRegistrationId()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, null, sasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyRegistrationId()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, "", sasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullSasToken()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, null);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptySasToken()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, "");
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_004: [If the provided mechanisms array does not contain "PLAIN" then this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void choseSaslMechanismThrowsIfPlainNotPresent() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        handler.chooseSaslMechanism(new String[]{"NotPLAIN"});
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_005: [This function shall return "PLAIN".]
    @Test
    public void choseSaslMechanismReturnsPLAINIfPresent() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        String chosenMechanism = handler.chooseSaslMechanism(new String[]{"NotTPM", "PLAIN"});

        //assert
        String expectedMechanism = Deencapsulation.getField(handler, "PLAIN_MECHANISM");
        assertEquals(expectedMechanism, chosenMechanism);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_007: [This function shall return an empty byte array.]
    @Test
    public void buildInitSuccess() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);
        handler.chooseSaslMechanism(new String[]{"TPM", "PLAIN"});

        //act
        byte[] actualInitPayload = handler.getInitPayload("PLAIN");

        //assert
        assertEquals(0, actualInitPayload.length);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_009: [This function shall return an empty byte array.]
    @Test
    public void challengeResponseSuccess() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);
        handler.chooseSaslMechanism(new String[]{"TPM", "PLAIN"});

        //act
        byte[] challengeResponseBytes = handler.handleChallenge(new byte[0]);

        //assert
        assertEquals(0, challengeResponseBytes.length);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeAuthThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.AUTH);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeSysTempThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS_TEMP);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeSysThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeSysPermThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS_PERM);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_008: [This function shall save the provided sas token.]
    @Test
    public void setSasTokenSets()
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        handler.setSasToken(sasToken);

        //assert
        String actualSasToken = Deencapsulation.getField(handler, "sasToken");
        assertEquals(sasToken, actualSasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_023: [This function shall return <idScope>/registrations/<registrationId>.]
    @Test
    public void plainUsernameSuccess()
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);
        String expectedUsername = idScope + "/registrations/" + registrationId;

        //act
        String actualUsername = handler.getPlainUsername();

        //assert
        assertEquals(expectedUsername, actualUsername);
    }

    // Tests_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_023: [This function shall return the saved sas token.]
    @Test
    public void plainPasswordSuccess()
    {
        //arrange
        AmqpsProvisioningSymmetricKeySaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSymmetricKeySaslHandler.class, new Class[]{String.class, String.class, String.class}, idScope, registrationId, sasToken);

        //act
        String actualPassword = handler.getPlainPassword();

        //assert
        assertEquals(sasToken, actualPassword);
    }

}
