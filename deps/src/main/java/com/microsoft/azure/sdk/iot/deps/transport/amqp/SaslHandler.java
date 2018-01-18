/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

/**
 * Interface definition for what client events are expected to be handled during Sasl negotiation
 * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-security-v1.0-os.html#doc-idp51040">AMQPS 1.0 Sasl Negotiation Documentation</a>
 */
public interface SaslHandler
{
    /**
     * Possible outcomes of Sasl negotiation as per AMQP 1.0
     * @see <a href="http://docs.oasis-open.org/amqp/core/v1.0/os/amqp-core-security-v1.0-os.html#type-sasl-code">
     *     AMPQ</a>
     */
    enum SaslOutcome
    {
        /** authentication succeeded */
        OK,

        /** failed due to bad credentials */
        AUTH,

        /** failed due to a system error */
        SYS,

        /** failed due to unrecoverable error */
        SYS_PERM,

        /** failed due to transient error */
        SYS_TEMP
    }

    /**
     * Handle the provided mechanisms and return the mechanism to be used from a provided list
     *
     * @param mechanisms A list of available Sasl Mechanisms offered by the service
     * @return the mechanism to use
     */
    String chooseSaslMechanism(String[] mechanisms) throws Exception;

    /**
     * Based on the chosen mechanism, builds and returns the bytes to be sent in the payload for the Sasl init message
     * @return the bytes to be used as the paylaod for the Sasl init message
     */
    byte[] getInitPayload(String chosenMechanism) throws Exception;

    /**
     * Handles a given challenge and returns the bytes to be sent in the payload of the Sasl response message
     * @param saslChallenge The bytes from the Sasl challenge received from the service
     * @return the bytes to be sent in the payload of the Sasl response to the provided challenge
     */
    byte[] handleChallenge(byte[] saslChallenge) throws Exception;

    /**
     * Handles what to do upon the Sasl negotiation finishing.
     * @param outcome The outcome of the sasl negotiation
     */
    void handleOutcome(SaslOutcome outcome) throws Exception;
}
