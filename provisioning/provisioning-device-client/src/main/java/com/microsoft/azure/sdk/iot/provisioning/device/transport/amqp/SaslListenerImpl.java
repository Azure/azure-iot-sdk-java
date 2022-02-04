package com.microsoft.azure.sdk.iot.provisioning.device.transport.amqp;


import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.SaslListener;
import org.apache.qpid.proton.engine.Transport;

class SaslListenerImpl implements SaslListener
{
    private final SaslHandler saslHandler;
    private Exception savedException;

    private static final String PLAIN = "PLAIN";

    /**
     * Sasl listener implementation that defers mechanism selection, init message payloads, and challenge handling to
     * the provided saslHandler
     * @param saslHandler The object that decides how to choose which mechanism to use and how to build message payloads
     */
    public SaslListenerImpl(SaslHandler saslHandler)
    {
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
            String chosenMechanism;
            try
            {
                chosenMechanism = this.saslHandler.chooseSaslMechanism(mechanisms);
                sasl.setMechanisms(chosenMechanism);

                if (PLAIN.equalsIgnoreCase(chosenMechanism))
                {
                    sasl.plain(this.saslHandler.getPlainUsername(), this.saslHandler.getPlainPassword());
                }

                byte[] initMessage = this.saslHandler.getInitPayload(chosenMechanism);

                if (initMessage != null && initMessage.length > 0)
                {
                    sasl.send(initMessage, 0, initMessage.length);
                }
            }
            catch (Exception e)
            {
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
            byte[] saslChallenge = new byte[sasl.pending()];
            sasl.recv(saslChallenge, 0, saslChallenge.length);

            byte[] challengeResponse;
            try
            {
                challengeResponse = this.saslHandler.handleChallenge(saslChallenge);
                sasl.send(challengeResponse, 0, challengeResponse.length);
            }
            catch (Exception e)
            {
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
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS_TEMP);
                        break;
                    case PN_SASL_PERM:
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS_PERM);
                        break;
                    case PN_SASL_AUTH:
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.AUTH);
                        break;
                    case PN_SASL_OK:
                        this.saslHandler.handleOutcome(SaslHandler.SaslOutcome.OK);
                        break;
                    case PN_SASL_NONE:
                        throw new IllegalStateException("Sasl negotiation did not finish yet");
                    case PN_SASL_SYS:
                    case PN_SASL_SKIPPED:
                    default:
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
