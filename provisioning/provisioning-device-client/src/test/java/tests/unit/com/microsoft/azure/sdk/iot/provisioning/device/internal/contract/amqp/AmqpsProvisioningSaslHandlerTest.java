/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp.AmqpsProvisioningSaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Unit tests for AmqpsProvisioningSaslHandler.java
 * Coverage : 100% method, 90% line
 */
@RunWith(JMockit.class)
public class AmqpsProvisioningSaslHandlerTest
{
    private static final String idScope = "5";
    private static final String registrationId = "3";
    private static final byte[] endorsementKey = new byte[]{2, 2};
    private static final byte[] storageRootKey = new byte[]{1, 1};
    private static final byte[] expectedInitPayload = new byte[] {0, 53, 0, 51, 0, 2, 2};
    private static final byte[] validFirstChallenge = new byte[]{0};
    private static final byte[] validSecondChallenge = new byte[431];
    private static final byte[] validThirdChallenge = new byte[415];
    private static final byte[] expectedFullNonce = new byte[844];
    private static final String sasToken = "6";
    private static final byte[] expectedFirstChallengeResponsePayload = new byte[] {0, 1, 1};
    private static final byte[] expectedSecondChallengeResponsePayload = new byte[] {0};
    private static final byte[] expectedFinalChallengeResponsePayload = new byte[] {0, 54};

    @Mocked
    ResponseCallback mockedResponseCallback;

    @Mocked
    ResponseData mockedResponseData;

    @BeforeClass
    public static void beforeClass()
    {
        validSecondChallenge[0] = Deencapsulation.getField(AmqpsProvisioningSaslHandler.class, "INTERMEDIATE_SEGMENT_CONTROL_BYTE");
        validThirdChallenge[0] = Deencapsulation.getField(AmqpsProvisioningSaslHandler.class, "FINAL_SEGMENT_CONTROL_BYTE");
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_001: [This constructor shall save the provided idScope, registrationId, endorsementKey, storageRootKey, responseCallback and autorizationCallbackContext .]
    @Test
    public void constructorSavesArgumentValues()
    {
        //act
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //assert
        String actualIdScope = Deencapsulation.getField(handler, "idScope");
        String actualRegistrationId = Deencapsulation.getField(handler, "registrationId");
        byte[] actualEndorsementKey = Deencapsulation.getField(handler, "endorsementKey");
        byte[] actualStorageRootKey = Deencapsulation.getField(handler, "storageRootKey");
        ResponseCallback actualNonceCallback = Deencapsulation.getField(handler, "responseCallback");
        String actualSasToken = Deencapsulation.getField(handler, "sasToken");

        assertEquals(idScope, actualIdScope);
        assertEquals(registrationId, actualRegistrationId);
        assertEquals(endorsementKey, actualEndorsementKey);
        assertEquals(storageRootKey, actualStorageRootKey);
        assertEquals(mockedResponseCallback, actualNonceCallback);
        assertNull(actualSasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullIdScope()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, null, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyIdScope()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, "", registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullRegistrationId()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, idScope, null, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForEmptyRegistrationId()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, idScope, "", endorsementKey, storageRootKey, mockedResponseCallback, new Object());
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullEndorsementKey()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, idScope, registrationId, null, storageRootKey, mockedResponseCallback, new Object());
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullStorageRootKey()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, idScope, registrationId, endorsementKey, null, mockedResponseCallback, new Object());
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsForNullNonceCallback()
    {
        //act
        Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, new Class[]{String.class, String.class, byte[].class, byte[].class, ResponseCallback.class, Object.class}, idScope, registrationId, endorsementKey, storageRootKey, null);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_004: [If the provided mechanisms array does not contain "TPM" then this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void choseSaslMechanismThrowsIfTPMNotPresent() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //act
        handler.chooseSaslMechanism(new String[]{"NotTPM"});
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_005: [This function shall return "TPM".]
    @Test
    public void choseSaslMechanismReturnsTPMIfPresent() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //act
        String chosenMechanism = handler.chooseSaslMechanism(new String[]{"NotTPM", "TPM"});

        //assert
        String expectedMechanism = Deencapsulation.getField(handler, "TPM_MECHANISM");
        assertEquals(expectedMechanism, chosenMechanism);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_003: [If this handler is not in the state where it is expecting to choose a sasl mechanism, this function shall throw in IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void choseSaslMechanismThrowsIfNotWaitingToChooseMechanism() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"NotTPM", "TPM"});

        //act
        handler.chooseSaslMechanism(new String[]{"NotTPM", "TPM"});
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_007: [This function shall return the init payload bytes in the format "control byte + scopeId + null byte + registration id + null byte + base64 decoded endorsement key".]
    @Test
    public void buildInitSuccess() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});

        //act
        byte[] actualInitPayload = handler.getInitPayload("TPM");

        //assert
        assertArraysEqual(expectedInitPayload, actualInitPayload);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_006: [If this handler is not in the state where it is expecting to build the init payload, this function shall throw in IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void buildInitThrowsIfNotWaitingToBuildInit()
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //act
        handler.getInitPayload("TPM");
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_011: [If this object is waiting for the first challenge, this function shall return a payload in the format "control byte + base64 decoded storage root key".]
    @Test
    public void handleChallengeFirstChallengeSuccess() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");

        //act
        byte[] actualFirstChallengeResponsePayload = handler.handleChallenge(validFirstChallenge);

        //assert
        assertArraysEqual(expectedFirstChallengeResponsePayload, actualFirstChallengeResponsePayload);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_014: [If this object is waiting for the second challenge, this function shall return a payload of one null byte.]
    @Test
    public void handleChallengeSecondChallengeSuccess() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);

        //act
        byte[] actualSecondChallengeResponsePayload = handler.handleChallenge(validSecondChallenge);

        //assert
        assertArraysEqual(expectedSecondChallengeResponsePayload, actualSecondChallengeResponsePayload);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_013: [If this object is waiting for the second challenge, this function shall read the challenge in the format "control byte + nonce (first half)" and save the nonce portion.]
    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_016: [If this object is waiting for the third challenge, this function shall read the challenge in the format "control byte + nonce (second half)" and save the nonce portion.]
    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_017: [If this object is waiting for the third challenge, this function shall put together the full nonce byte array and run the saved responseCallback with the nonce and DPS_REGISTRATION_RECEIVED.]
    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_018: [If this object is waiting for the third challenge, after running the saved responseCallback, this function shall wait for the sas token to be set before returning a payload in the format "control byte + sas token".]
    @Test
    public void handleChallengeThirdChallengeSuccess() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);
        new NonStrictExpectations()
        {
            {
                new ResponseData(expectedFullNonce, ContractState.DPS_REGISTRATION_RECEIVED, 0);
                result = mockedResponseData;
            }
        };


        //act
        byte[] actualFinalChallengeResponsePayload = handler.handleChallenge(validThirdChallenge);

        //assert
        assertArraysEqual(expectedFinalChallengeResponsePayload, actualFinalChallengeResponsePayload);
        new Verifications()
        {
            {
                mockedResponseCallback.run(mockedResponseData, any);
                times = 1;
            }
        };
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_009: [If the provided saslChallenge is null, this function shall throw an IllegalArgumentException.]
    @Test (expected = IllegalArgumentException.class)
    public void handleChallengeThrowsForNullChallengeData() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");

        //act
        handler.handleChallenge(null);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_010: [If this object is waiting for the first challenge, this function shall validate that this challenge payload contains only a null byte and shall throw an IllegalStateException if it is not.]
    @Test (expected = IllegalStateException.class)
    public void handleFirstChallengeThrowsForChallengeDataWithIncorrectControlByte() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");

        //act
        handler.handleChallenge(validSecondChallenge);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_012: [If this object is waiting for the second challenge, this function shall validate that this challenge payload contains a control byte with the mask 0x80 and shall throw an IllegalStateException if it is not.]
    @Test (expected = IllegalStateException.class)
    public void handleSecondChallengeThrowsForChallengeDataWithIncorrectControlByte() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);

        //act
        handler.handleChallenge(validFirstChallenge);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_015: [If this object is waiting for the third challenge, this function shall validate that this challenge payload contains a control byte with the mask 0xC1 and shall throw an IllegalStateException if it is not.]
    @Test (expected = IllegalStateException.class)
    public void handleThirdChallengeThrowsForChallengeDataWithIncorrectControlByte() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);

        //act
        handler.handleChallenge(validFirstChallenge);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_020: [If this object is not waiting for a first, second or third challenge, this function shall throw an IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void handleChallengeThrowsIfWaitingToChooseMechanism() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //act
        handler.handleChallenge(validThirdChallenge);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_020: [If this object is not waiting for a first, second or third challenge, this function shall throw an IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void handleChallengeThrowsIfWaitingToBuildInitPayload() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});

        //act
        handler.handleChallenge(validThirdChallenge);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_020: [If this object is not waiting for a first, second or third challenge, this function shall throw an IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void handleChallengeThrowsIfWaitingForSaslOutcome() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);
        handler.handleChallenge(validThirdChallenge);

        //act
        handler.handleChallenge(validThirdChallenge);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeAuthThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);
        handler.handleChallenge(validThirdChallenge);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.AUTH);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeSysTempThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);
        handler.handleChallenge(validThirdChallenge);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS_TEMP);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeSysThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);
        handler.handleChallenge(validThirdChallenge);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a ProvisioningDeviceSecurityException.]
    @Test (expected = ProvisioningDeviceSecurityException.class)
    public void handleOutcomeSysPermThrowsProvisioningDeviceSecurityException() throws ProvisioningDeviceClientException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());
        handler.chooseSaslMechanism(new String[]{"TPM", "notTPM"});
        handler.getInitPayload("TPM");
        handler.handleChallenge(validFirstChallenge);
        handler.handleChallenge(validSecondChallenge);
        Deencapsulation.setField(handler, "sasToken", sasToken);
        handler.handleChallenge(validThirdChallenge);

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS_PERM);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_021: [If this object is not waiting for the sasl outcome, this function shall throw an IllegalStateException.]
    @Test (expected = IllegalStateException.class)
    public void handleOutcomeThrowsIfNotWaitingOnOutcome() throws ProvisioningDeviceSecurityException
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //act
        handler.handleOutcome(SaslHandler.SaslOutcome.SYS_PERM);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_008: [This function shall save the provided sas token.]
    @Test
    public void setSasTokenSets()
    {
        //arrange
        AmqpsProvisioningSaslHandler handler = Deencapsulation.newInstance(AmqpsProvisioningSaslHandler.class, idScope, registrationId, endorsementKey, storageRootKey, mockedResponseCallback, new Object());

        //act
        Deencapsulation.invoke(handler, "setSasToken", sasToken);

        //assert
        String actualSasToken = Deencapsulation.getField(handler, "sasToken");
        assertEquals(sasToken, actualSasToken);
    }

    // Tests_SRS_AMQPSPROVISIONINGSASLHANDLER_34_019: [If this object is waiting for the third challenge, and if the sas token is not provided within 3 minutes of waiting, this function shall throw a ProvisioningDeviceSecurityException.]
    //No test because it would take 3 minutes to run. This is an E2E scenario, not a unit test

    private static void assertArraysEqual(byte[] expected, byte[] actual)
    {
        assertEquals("actual byte array has different length than expected", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
        {
            assertEquals("actual byte array has different contents than expected", expected[i], actual[i]);
        }
    }
}
