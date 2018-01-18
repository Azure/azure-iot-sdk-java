package com.microsoft.azure.sdk.iot.deps.transport.amqp;


import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.SaslListener;
import org.apache.qpid.proton.engine.Transport;

public class SaslListenerImpl implements SaslListener
{
    private SaslHandler saslHandler;
    private Exception savedException;

    /**
     * Sasl listener implementation that defers mechanism selection, init message payloads, and challenge handling to
     * the provided saslHandler
     * @param saslHandler The object that decides how to choose which mechanism to use and how to build message payloads
     */
    public SaslListenerImpl(SaslHandler saslHandler)
    {
        // Codes_SRS_SASLLISTENERIMPL_34_001: [This constructor shall save the provided handler.]
        this.saslHandler = saslHandler;
    }

    /**
     * This event is fired when the service advertises some sasl authentication mechanisms. This method sends the init message in response.
     * @param sasl the Sasl object
     * @param transport the related transport
     */
    public void onSaslMechanisms(Sasl sasl, Transport transport)
    {
        if (sasl == null)
        {
            this.savedException = new IllegalArgumentException("Sasl cannot be null");
        }
        else
        {
            String[] mechanisms = sasl.getRemoteMechanisms();
            String chosenMechanism = null;
            try
            {
                // Codes_SRS_SASLLISTENERIMPL_34_002: [This function shall retrieve the remote mechanisms and give them to the saved saslHandler object to decide which mechanism to use.]
                chosenMechanism = this.saslHandler.chooseSaslMechanism(mechanisms);
                sasl.setMechanisms(chosenMechanism);

                // Codes_SRS_SASLLISTENERIMPL_34_003: [This function shall ask the saved saslHandler object to create the init payload for the chosen sasl mechanism and then send that payload.]
                byte[] initMessage = this.saslHandler.getInitPayload(chosenMechanism);
                sasl.send(initMessage, 0, initMessage.length);
            }
            catch (Exception e)
            {
                //Codes_SRS_SASLLISTENERIMPL_34_012: [If any exception is thrown while the saslHandler handles the sasl mechanisms, this function shall save that exception and shall not create and send the init payload.]
                //Codes_SRS_SASLLISTENERIMPL_34_013: [If any exception is thrown while the saslHandler builds the init payload, this function shall save that exception and shall not send the returned init payload.]
                this.savedException = e;
            }
        }
    }

    /**
     *
     * @param sasl the Sasl object
     * @param transport the related transport
     */
    public void onSaslChallenge(Sasl sasl, Transport transport)
    {
        if (sasl == null)
        {
            this.savedException = new IllegalArgumentException("Sasl cannot be null");
        }
        else
        {
            // Codes_SRS_SASLLISTENERIMPL_34_004: [This function shall retrieve the sasl challenge from the provided sasl object.]
            byte[] saslChallenge = new byte[sasl.pending()];
            sasl.recv(saslChallenge, 0, saslChallenge.length);

            // Codes_SRS_SASLLISTENERIMPL_34_005: [This function shall give the sasl challenge bytes to the saved saslHandler and send the payload it returns.]
            byte[] challengeResponse;
            try
            {
                challengeResponse = this.saslHandler.handleChallenge(saslChallenge);
                sasl.send(challengeResponse, 0, challengeResponse.length);
            }
            catch (Exception e)
            {
                //Codes_SRS_SASLLISTENERIMPL_34_014: [If any exception is thrown while the saslHandler handles the challenge, this function shall save that exception and shall not send the challenge response.]
                this.savedException = e;
            }
        }
    }

    /**
     * This event is fired when Sasl negotiation finishes. It passes the results to the handler for any further processing
     * @param sasl the Sasl object
     * @param transport the related transport
     */
    public void onSaslOutcome(Sasl sasl, Transport transport)
    {
        if (sasl == null)
        {
            this.savedException = new IllegalArgumentException("Sasl cannot be null");
        }
        else
        {
            try
            {
                switch (sasl.getOutcome())
                {
                    case PN_SASL_TEMP:
                        // Codes_SRS_SASLLISTENERIMPL_34_006: [If the sasl outcome is PN_SASL_TEMP, this function shall tell the saved saslHandler to handleOutcome with SYS_TEMP.]
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS_TEMP);
                        break;
                    case PN_SASL_PERM:
                        // Codes_SRS_SASLLISTENERIMPL_34_007: [If the sasl outcome is PN_SASL_PERM, this function shall tell the saved saslHandler to handleOutcome with SYS_PERM.]
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS_PERM);
                        break;
                    case PN_SASL_AUTH:
                        // Codes_SRS_SASLLISTENERIMPL_34_008: [If the sasl outcome is PN_SASL_AUTH, this function shall tell the saved saslHandler to handleOutcome with AUTH.]
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.AUTH);
                        break;
                    case PN_SASL_OK:
                        // Codes_SRS_SASLLISTENERIMPL_34_009: [If the sasl outcome is PN_SASL_OK, this function shall tell the saved saslHandler to handleOutcome with OK.]
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.OK);
                        break;
                    case PN_SASL_NONE:
                        // Codes_SRS_SASLLISTENERIMPL_34_011: [If the sasl outcome is PN_SASL_NONE, this function shall save an IllegalStateException.]
                        throw new IllegalStateException("Sasl negotiation did not finish yet");
                    case PN_SASL_SYS:
                    case PN_SASL_SKIPPED:
                    default:
                        // Codes_SRS_SASLLISTENERIMPL_34_010: [If the sasl outcome is PN_SASL_SYS or PN_SASL_SKIPPED, this function shall tell the saved saslHandler to handleOutcome with SYS.]
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS);
                        break;
                }
            } catch (Exception e)
            {
                this.savedException = e;
            }
        }
    }

    /**
     * Returns an exception if the sasl negotiation encountered an exception, or null if it has not thrown any exceptions
     * @return the exception the sasl negotiation encountered
     */
    public Exception getSavedException()
    {
        return this.savedException;
    }

    /**
     * Does nothing. This implementation is for clients only, not servers
     */
    public void onSaslResponse(Sasl sasl, Transport transport) {}

    /**
     * Does nothing. This implementation is for clients only, not servers
     */
    public void onSaslInit(Sasl sasl, Transport transport) {}
}
