/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract.amqp;

import com.microsoft.azure.sdk.iot.provisioning.device.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceSecurityException;

/**
 * Implementation of a SaslHandler that is designed to handle Sasl negotiation using TPM authentication against the Device Provisioning Service
 */
class AmqpsProvisioningSymmetricKeySaslHandler implements SaslHandler
{
    private final static String PLAIN_MECHANISM = "PLAIN";
    private final static String USERNAME_FORMAT = "%s/registrations/%s";
    private final static byte[] EMPTY_BYTE_ARRAY = new byte[0];

    private final String idScope;
    private final String registrationId;
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
            throw new IllegalArgumentException("IdScope cannot be null or empty");
        }

        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("RegistrationId cannot be null or empty");
        }

        if (sasToken == null || sasToken.isEmpty())
        {
            throw new IllegalArgumentException("sasToken cannot be null or empty");
        }

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
        for (String mechanism : mechanisms)
        {
            if (PLAIN_MECHANISM.equals(mechanism))
            {
                return PLAIN_MECHANISM;
            }
        }

        throw new ProvisioningDeviceSecurityException("Service endpoint does not support TPM authentication");
    }

    /**
     * Builds the init payload out of the saved idScope, registrationId, and endorsementKey
     * @param chosenMechanism The sasl mechanism chosen to be used when doing Sasl negotiation with the service
     * @return the payload of the init message to be sent to the service
     */
    public byte[] getInitPayload(String chosenMechanism)
    {
        return EMPTY_BYTE_ARRAY;
    }

    /**
     * Handles the three expected challenges from the service that happen in Sasl negotiation
     * @param saslChallenge The bytes from the Sasl challenge received from the service
     * @return the payload of the challenge response to the given challenge
     */
    public byte[] handleChallenge(byte[] saslChallenge)
    {
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
        return String.format(USERNAME_FORMAT, this.idScope, registrationId);
    }

    @Override
    public String getPlainPassword()
    {
        return this.sasToken;
    }

    @Override
    public void setSasToken(String sasToken)
    {
        this.sasToken = sasToken;
    }
}
