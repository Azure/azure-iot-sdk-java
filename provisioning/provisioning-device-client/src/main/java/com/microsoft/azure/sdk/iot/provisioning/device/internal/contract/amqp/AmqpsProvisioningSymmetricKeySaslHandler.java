/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.ResponseCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ContractState;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.task.ResponseData;

/**
 * Implementation of a SaslHandler that is designed to handle Sasl negotiation using TPM authentication against the Device Provisioning Service
 */
public class AmqpsProvisioningSymmetricKeySaslHandler implements SaslHandler
{
    private final static String PLAIN_MECHANISM = "PLAIN";
    private final static String USERNAME_FORMAT = "%s/registrations/%s";
    private String idScope;
    private String registrationId;
    private String sasToken;

    /**
     * SaslHandler implementation that handles the TPM flow for Provisioning
     *
     * @param idScope idScope of the provisioning service
     * @param registrationId registration id of this provisioning
     * @param sasToken sas token for authentication
     */
    AmqpsProvisioningSymmetricKeySaslHandler(String idScope, String registrationId, String sasToken)
    {
        if (idScope == null || idScope.isEmpty())
        {
            // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("IdScope cannot be null or empty");
        }

        if (registrationId == null || registrationId.isEmpty())
        {
            // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty other than the autorizationCallbackContext, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("RegistrationId cannot be null or empty");
        }

        if (sasToken == null || sasToken.length() == 0)
        {
            // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_002: [If any of the arguments are null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("sasToken cannot be null or empty");
        }

        // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_001: [This constructor shall save the provided idScope, registrationId, and sas token.]
        this.idScope = idScope;
        this.registrationId = registrationId;
        this.sasToken = sasToken;
    }

    /**
     * Checks to ensure that TPM is an available mechanism and chooses it
     * @param mechanisms A list of available Sasl Mechanisms offered by the service
     * @return "TPM" if offered by the service
     */
    public String chooseSaslMechanism(String[] mechanisms) throws ProvisioningDeviceSecurityException
    {
        boolean plainMechanismOfferedByService = false;
        for (String mechanism : mechanisms)
        {
            plainMechanismOfferedByService |= mechanism.equals(PLAIN_MECHANISM);
        }

        if (!plainMechanismOfferedByService)
        {
            // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_004: [If the provided mechanisms array does not contain "PLAIN" then this function shall throw a ProvisioningDeviceSecurityException.]
            throw new ProvisioningDeviceSecurityException("Service endpoint does not support TPM authentication");
        }

        // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_005: [This function shall return "PLAIN".]
        return PLAIN_MECHANISM;
    }

    /**
     * Builds the init payload out of the saved idScope, registrationId, and endorsementKey
     * @param chosenMechanism The sasl mechanism chosen to be used when doing Sasl negotiation with the service
     * @return the payload of the init message to be sent to the service
     */
    public byte[] getInitPayload(String chosenMechanism)
    {
        // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_007: [This function shall return an empty byte array".]
        return new byte[0];
    }

    /**
     * Handles the three expected challenges from the service that happen in Sasl negotiation
     * @param saslChallenge The bytes from the Sasl challenge received from the service
     * @return the payload of the challenge response to the given challenge
     */
    public byte[] handleChallenge(byte[] saslChallenge) throws ProvisioningDeviceClientException
    {
        // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_009: [This function shall return an empty byte array.]
        return new byte[0];
    }

    /**
     * Handles the outcome of the Sasl negotiation
     * @param outcome The outcome of the sasl negotiation
     */
    public void handleOutcome(SaslOutcome outcome) throws ProvisioningDeviceSecurityException
    {
        switch (outcome)
        {
            case OK:
                //auth successful
                break;

            case AUTH:
                //bad credentials
                // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a SecurityException.]
                throw new ProvisioningDeviceSecurityException("Sas token was rejected by the service");

            case SYS_TEMP:
                // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a SecurityException.]
                throw new ProvisioningDeviceSecurityException("Sasl negotiation failed due to transient system error");
                
            case SYS:
            case SYS_PERM:
            default:
                //some other kind of failure
                // Codes_SRS_AMQPSPROVISIONINGSASLHANDLER_34_022: [If the sasl outcome is not OK, this function shall throw a SecurityException.]
                throw new ProvisioningDeviceSecurityException("Sasl negotiation with service failed");
        }
    }

    @Override
    public String plainUsername()
    {
        // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_023: [This function shall return <idScope>/registrations/<registrationId>.]
        return String.format(USERNAME_FORMAT, this.idScope, registrationId);
    }

    @Override
    public String plainPassword()
    {
        // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_023: [This function shall return the saved sas token.]
        return this.sasToken;
    }

    @Override
    public void setSasToken(String sasToken)
    {
        // Codes_SRS_AMQPSPROVISIONINGSYMMETRICKEYSASLHANDLER_34_024: [This function shall save the provided sas token.]
        this.sasToken = sasToken;
    }
}
