/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.provisioning.device.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;

import java.nio.charset.StandardCharsets;

/**
 * Implementation of a SaslHandler that is designed to handle Sasl negotiation using TPM authentication against the Device Provisioning Service
 */
class AmqpsProvisioningSaslHandler implements SaslHandler
{
    private final static String TPM_MECHANISM = "TPM";
    private final static byte NULL_BYTE = (byte) '\0';
    private final static byte INIT_SEGMENT_CONTROL_BYTE = (byte) 0;
    private final static byte INTERMEDIATE_SEGMENT_CONTROL_BYTE = (byte) 0x80;
    private final static byte FINAL_SEGMENT_CONTROL_BYTE = (byte) 0xC1;
    private final static long MAX_MILLISECONDS_TIMEOUT_FOR_SAS_TOKEN_WAIT = 60*1000; // 1 minute
    private final static long WAIT_INTERVALS = 4*1000; // 4 second wait intervals when waiting and checking for sas token

    private final String idScope;
    private final String registrationId;
    private final byte[] endorsementKey;
    private final byte[] storageRootKey;
    private byte[] challengeKey;
    private ChallengeState challengeState;
    private final ResponseCallback responseCallback;
    private final Object authorizationCallbackContext;
    private String sasToken;

    /**
     * Tracks the state of the TPM Provisioning sasl negotiation
     */
    private enum ChallengeState
    {
        WAITING_FOR_MECHANISMS,
        WAITING_TO_BUILD_INIT,
        WAITING_FOR_FIRST_CHALLENGE,
        WAITING_FOR_SECOND_CHALLENGE,
        WAITING_FOR_THIRD_CHALLENGE,
        WAITING_TO_SEND_SAS_TOKEN,
        WAITING_FOR_FINAL_OUTCOME
    }

    /**
     * SaslHandler implementation that handles the TPM flow for Provisioning
     *
     * @param idScope idScope of the provisioning service
     * @param registrationId registration id of this provisioning
     * @param endorsementKey Endorsement key of the provisioned device
     * @param storageRootKey Storage root key of the provisioned device
     * @param responseCallback The callback to be fired upon receiving the full nonce
     * @param authorizationCallbackContext the context to pass along in the response callback
     */
    AmqpsProvisioningSaslHandler(String idScope, String registrationId, byte[] endorsementKey, byte[] storageRootKey, ResponseCallback responseCallback, Object authorizationCallbackContext)
    {
        if (idScope == null || idScope.isEmpty())
        {
            throw new IllegalArgumentException("IdScope cannot be null or empty");
        }

        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("RegistrationId cannot be null or empty");
        }

        if (endorsementKey == null || endorsementKey.length == 0)
        {
            throw new IllegalArgumentException("Endorsement Key cannot be null or empty");
        }

        if (storageRootKey == null || storageRootKey.length == 0)
        {
            throw new IllegalArgumentException("Storage root key cannot be null or empty");
        }

        if (responseCallback == null)
        {
            throw new IllegalArgumentException("responseCallback cannot be null");
        }

        this.idScope = idScope;
        this.registrationId = registrationId;
        this.endorsementKey = endorsementKey;
        this.storageRootKey = storageRootKey;
        this.responseCallback = responseCallback;
        this.authorizationCallbackContext = authorizationCallbackContext;
        this.challengeState = ChallengeState.WAITING_FOR_MECHANISMS;
        this.sasToken = null;
    }

    /**
     * Checks to ensure that TPM is an available mechanism and chooses it
     * @param mechanisms A list of available Sasl Mechanisms offered by the service
     * @return "TPM" if offered by the service
     */
    public String chooseSaslMechanism(String[] mechanisms) throws ProvisioningDeviceSecurityException
    {
        if (this.challengeState != ChallengeState.WAITING_FOR_MECHANISMS)
        {
            throw new IllegalStateException("Handler is not in a state to handle choosing a mechanism");
        }

        boolean tpmMechanismOfferedByService = false;
        for (String mechanism : mechanisms)
        {
            tpmMechanismOfferedByService |= mechanism.equals(TPM_MECHANISM);
        }

        if (!tpmMechanismOfferedByService)
        {
            throw new ProvisioningDeviceSecurityException("Service endpoint does not support TPM authentication");
        }

        this.challengeState = ChallengeState.WAITING_TO_BUILD_INIT;

        return TPM_MECHANISM;
    }

    /**
     * Builds the init payload out of the saved idScope, registrationId, and endorsementKey
     * @param chosenMechanism The sasl mechanism chosen to be used when doing Sasl negotiation with the service
     * @return the payload of the init message to be sent to the service
     */
    public byte[] getInitPayload(String chosenMechanism)
    {
        if (this.challengeState != ChallengeState.WAITING_TO_BUILD_INIT)
        {
            throw new IllegalStateException("Handler is not in a state to build the init payload");
        }

        byte[] saslInitBytes = buildSaslInitPayload(this.idScope, this.registrationId, this.endorsementKey);
        this.challengeState = ChallengeState.WAITING_FOR_FIRST_CHALLENGE;
        return saslInitBytes;
    }

    /**
     * Handles the three expected challenges from the service that happen in Sasl negotiation
     * @param saslChallenge The bytes from the Sasl challenge received from the service
     * @return the payload of the challenge response to the given challenge
     */
    public byte[] handleChallenge(byte[] saslChallenge) throws ProvisioningDeviceClientException
    {
        if (saslChallenge == null)
        {
            throw new IllegalArgumentException("Challenge data cannot be null");
        }

        switch (this.challengeState)
        {
            case WAITING_FOR_FIRST_CHALLENGE:
                this.challengeState = ChallengeState.WAITING_FOR_SECOND_CHALLENGE;
                return handleFirstChallenge(saslChallenge);

            case WAITING_FOR_SECOND_CHALLENGE:
                return handleSecondChallenge(saslChallenge);

            case WAITING_FOR_THIRD_CHALLENGE:
                return handleThirdChallenge(saslChallenge);

            case WAITING_FOR_MECHANISMS:
                throw new IllegalStateException("Unexpected challenge received when expecting to choose sasl mechanism");

            case WAITING_TO_BUILD_INIT:
                throw new IllegalStateException("Unexpected challenge received when expecting to build sasl init payload");

            case WAITING_TO_SEND_SAS_TOKEN:
                throw new IllegalStateException("Unexpected challenge received when expecting to send sas token");

            case WAITING_FOR_FINAL_OUTCOME:
                throw new IllegalStateException("Unexpected challenge received when expecting Sasl outcome");

            default:
                throw new IllegalStateException("Unexpected challenge received");
        }
    }

    /**
     * Handles the outcome of the Sasl negotiation
     * @param outcome The outcome of the sasl negotiation
     */
    public void handleOutcome(SaslOutcome outcome) throws ProvisioningDeviceSecurityException
    {
        if (this.challengeState != ChallengeState.WAITING_FOR_FINAL_OUTCOME)
        {
            throw new IllegalStateException("This handler is not ready to handle the sasl outcome");
        }

        switch (outcome)
        {
            case OK:
                //auth successful
                break;

            case AUTH:
                //bad credentials
                throw new ProvisioningDeviceSecurityException("Sas token was rejected by the service");

            case SYS_TEMP:
                throw new ProvisioningDeviceSecurityException("Sasl negotiation failed due to transient system error");
                
            case SYS:
            case SYS_PERM:
            default:
                //some other kind of failure
                throw new ProvisioningDeviceSecurityException("Sasl negotiation with service failed");
        }
    }

    @Override
    public String getPlainUsername()
    {
        throw new UnsupportedOperationException("TPM sasl does not use plain mechanism for authentication");
    }

    @Override
    public String getPlainPassword()
    {
        throw new UnsupportedOperationException("TPM sasl does not use plain mechanism for authentication");
    }

    /**
     * Sets the value of this object's saved sas token. Should only be called when that sas token was generated using the
     * nonce retrieved from the Device Provisioning service.
     * @param sasToken The SAS token to be used when finishing Sasl negotiation.
     */
    public void setSasToken(String sasToken)
    {
        this.sasToken = sasToken;
    }

    private byte[] handleFirstChallenge(byte[] challengeData)
    {
        //validate challenge
        if (challengeData.length != 1 || challengeData[0] != NULL_BYTE)
        {
            throw new IllegalStateException("Unexpected challenge data");
        }

        return buildFirstSaslChallengeResponsePayload(this.storageRootKey);
    }

    private byte[] handleSecondChallenge(byte[] challengeData)
    {
        //validate challenge
        if (challengeData.length < 1 || challengeData[0] != INTERMEDIATE_SEGMENT_CONTROL_BYTE)
        {
            throw new IllegalStateException("Unexpected challenge data");
        }

        this.challengeState = ChallengeState.WAITING_FOR_THIRD_CHALLENGE;

        this.challengeKey = new byte[challengeData.length-1];
        System.arraycopy(challengeData, 1, this.challengeKey, 0, challengeData.length-1);

        return new byte[]{NULL_BYTE};
    }

    private byte[] handleThirdChallenge(byte[] challengeData) throws ProvisioningDeviceClientException
    {
        //validate challenge
        if (challengeData.length < 1 || challengeData[0] != FINAL_SEGMENT_CONTROL_BYTE)
        {
            throw new IllegalStateException("Unexpected challenge data");
        }

        this.challengeKey = buildNonceFromThirdChallenge(challengeData);

        this.responseCallback.run(new ResponseData(this.challengeKey, ContractState.DPS_REGISTRATION_RECEIVED, 0), this.authorizationCallbackContext);
        
        this.challengeState = ChallengeState.WAITING_TO_SEND_SAS_TOKEN;

        long millisecondsElapsed = 0;
        long waitTimeStart = System.currentTimeMillis();
        while (this.sasToken == null && millisecondsElapsed < MAX_MILLISECONDS_TIMEOUT_FOR_SAS_TOKEN_WAIT)
        {
            try
            {
                //noinspection BusyWait
                Thread.sleep(WAIT_INTERVALS);
            }
            catch (InterruptedException e)
            {
                throw new ProvisioningDeviceClientException(e);
            }

            millisecondsElapsed = System.currentTimeMillis() - waitTimeStart;
        }

        if (millisecondsElapsed >= MAX_MILLISECONDS_TIMEOUT_FOR_SAS_TOKEN_WAIT)
        {
            throw new ProvisioningDeviceSecurityException("Sasl negotiation failed: Sas token was never supplied to finish negotiation");
        }

        this.challengeState = ChallengeState.WAITING_FOR_FINAL_OUTCOME;
        return prependByteArrayWithControlByte(INIT_SEGMENT_CONTROL_BYTE, sasToken.getBytes(StandardCharsets.UTF_8));
    }

    private byte[] buildNonceFromThirdChallenge(byte[] challengeData)
    {
        byte[] completeChallengeKey = new byte[this.challengeKey.length + challengeData.length - 1];
        System.arraycopy(this.challengeKey, 0, completeChallengeKey, 0, this.challengeKey.length);
        System.arraycopy(challengeData, 1, completeChallengeKey, this.challengeKey.length, challengeData.length - 1);
        return completeChallengeKey;
    }

    private static byte[] buildSaslInitPayload(String idScope, String registrationId, byte[] endorsementKey)
    {
        byte[] bytes = concatBytesWithNullDelimiter(idScope.getBytes(StandardCharsets.UTF_8), registrationId.getBytes(StandardCharsets.UTF_8), endorsementKey);
        return prependByteArrayWithControlByte(INIT_SEGMENT_CONTROL_BYTE, bytes);
    }

    private static byte[] buildFirstSaslChallengeResponsePayload(byte[] srk)
    {
        return prependByteArrayWithControlByte(INIT_SEGMENT_CONTROL_BYTE, srk);
    }

    private static byte[] concatBytesWithNullDelimiter(byte[]...arrays)
    {
        // Determine the length of the result array
        int totalLength = 0;
        for (byte[] array : arrays)
        {
            totalLength += array.length;
        }

        //for X arrays, there will be X-1 delimiters
        totalLength += arrays.length - 1;

        // create the result array
        byte[] result = new byte[totalLength];

        // copy the source arrays into the result array
        int currentIndex = 0;
        for (int i = 0; i < arrays.length-1; i++)
        {
            //copy the source array into the single new array
            System.arraycopy(arrays[i], 0, result, currentIndex, arrays[i].length);

            //add the UTF8NUL delimiter
            result[currentIndex + arrays[i].length] = NULL_BYTE;

            currentIndex += arrays[i].length + 1;
        }

        //copy the final value into the array without adding a delimiter at the end
        System.arraycopy(arrays[arrays.length-1], 0, result, currentIndex, arrays[arrays.length-1].length);

        return result;
    }

    @SuppressWarnings("SameParameterValue") // The "controlByte" is currently always an initial segment control byte, but this method can be used generically as well.
    private static byte[] prependByteArrayWithControlByte(byte controlByte, byte[] bytes)
    {
        byte[] newByteArray = new byte[bytes.length + 1];
        newByteArray[0] = controlByte;
        System.arraycopy(bytes, 0, newByteArray, 1, bytes.length);
        return newByteArray;
    }
}
