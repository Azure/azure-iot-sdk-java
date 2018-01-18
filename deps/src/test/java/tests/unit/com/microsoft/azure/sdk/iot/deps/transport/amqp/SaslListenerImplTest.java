/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslHandler;
import com.microsoft.azure.sdk.iot.deps.transport.amqp.SaslListenerImpl;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.engine.Sasl;
import org.apache.qpid.proton.engine.SaslListener;
import org.apache.qpid.proton.engine.Transport;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.qpid.proton.engine.Sasl.SaslOutcome.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit tests for SaslListenerImpl.java
 * Coverage : 75% method, 95% line
 * (Two methods currently have no implementation)
 */
@RunWith(JMockit.class)
public class SaslListenerImplTest
{
    @Mocked
    SaslHandler mockedSaslHandler;

    @Mocked
    Transport mockedTransport;

    @Mocked
    Sasl mockedSasl;

    // Tests_SRS_SASLLISTENERIMPL_34_001: [This constructor shall save the provided handler.]
    @Test
    public void constructorSavesProvidedHandler()
    {
        //act
        SaslListenerImpl saslListener = new SaslListenerImpl(mockedSaslHandler);

        //assert
        SaslHandler actualSaslHandler = Deencapsulation.getField(saslListener, "saslHandler");
        assertEquals(mockedSaslHandler, actualSaslHandler);
    }

    // Tests_SRS_SASLLISTENERIMPL_34_002: [This function shall retrieve the remote mechanisms and give them to the saved saslHandler object to decide which mechanism to use.]
    // Tests_SRS_SASLLISTENERIMPL_34_003: [This function shall ask the saved saslHandler object to create the init payload for the chosen sasl mechanism and then send that payload.]
    @Test
    public void onSaslMechanismsTest() throws Exception
    {
        //arrange
        final String[] remoteMechanisms = new String[] {"1", "2"};
        final String chosenMechanism = "1";
        final byte[] initPayload = new byte[] {0, 0, 0};
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getRemoteMechanisms();
                result = remoteMechanisms;

                mockedSaslHandler.chooseSaslMechanism(remoteMechanisms);
                result = chosenMechanism;

                mockedSaslHandler.getInitPayload(chosenMechanism);
                result = initPayload;
            }
        };

        //act
        saslListener.onSaslMechanisms(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.chooseSaslMechanism(remoteMechanisms);
                times = 1;

                mockedSaslHandler.getInitPayload(chosenMechanism);
                times = 1;

                mockedSasl.send(initPayload, 0, initPayload.length);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_004: [This function shall retrieve the sasl challenge from the provided sasl object.]
    // Tests_SRS_SASLLISTENERIMPL_34_005: [This function shall give the sasl challenge bytes to the saved saslHandler and send the payload it returns.]
    @Test
    public void onSaslChallengeTest() throws Exception
    {
        //arrange
        final byte[] challengePayload = new byte[] {0, 0, 0};
        final byte[] challengeResponsePayload = new byte[] {1, 1, 1};
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.pending();
                result = challengePayload.length;

                mockedSasl.recv((byte[]) any, 0, challengePayload.length);

                mockedSaslHandler.handleChallenge((byte[]) any);
                result = challengeResponsePayload;
            }
        };

        //act
        saslListener.onSaslChallenge(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSasl.recv((byte[]) any, 0, challengePayload.length);
                times = 1;

                mockedSaslHandler.handleChallenge((byte[]) any);
                times = 1;

                mockedSasl.send(challengeResponsePayload, 0, challengeResponsePayload.length);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_008: [If the sasl outcome is PN_SASL_AUTH, this function shall tell the saved saslHandler to handleOutcome with AUTH.]
    @Test
    public void onSaslOutcomeAuthTest() throws Exception
    {
        //arrange
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_AUTH;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.handleOutcome(SaslHandler.SaslOutcome.AUTH);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_006: [If the sasl outcome is PN_SASL_TEMP, this function shall tell the saved saslHandler to handleOutcome with SYS_TEMP.]
    @Test
    public void onSaslOutcomeTempTest() throws Exception
    {
        //arrange
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_TEMP;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS_TEMP);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_007: [If the sasl outcome is PN_SASL_PERM, this function shall tell the saved saslHandler to handleOutcome with SYS_PERM.]
    @Test
    public void onSaslOutcomePermTest() throws Exception
    {
        //arrange
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_PERM;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS_PERM);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_010: [If the sasl outcome is PN_SASL_SYS or PN_SASL_SKIPPED, this function shall tell the saved saslHandler to handleOutcome with SYS.]
    @Test
    public void onSaslOutcomeSysTest() throws Exception
    {
        //arrange
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_SYS;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_009: [If the sasl outcome is PN_SASL_OK, this function shall tell the saved saslHandler to handleOutcome with OK.]
    @Test
    public void onSaslOutcomeOkTest() throws Exception
    {
        //arrange
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_OK;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.handleOutcome(SaslHandler.SaslOutcome.OK);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_010: [If the sasl outcome is PN_SASL_SYS or PN_SASL_SKIPPED, this function shall tell the saved saslHandler to handleOutcome with SYS.]
    @Test
    public void onSaslOutcomeSkippedTest() throws Exception
    {
        //arrange
        SaslListener saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_SKIPPED;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        new Verifications()
        {
            {
                mockedSaslHandler.handleOutcome(SaslHandler.SaslOutcome.SYS);
                times = 1;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_011: [If the sasl outcome is PN_SASL_NONE, this function shall save an IllegalStateException.]
    @Test
    public void onSaslOutcomeNoneTest()
    {
        //arrange
        SaslListenerImpl saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getOutcome();
                result = PN_SASL_NONE;
            }
        };

        //act
        saslListener.onSaslOutcome(mockedSasl, mockedTransport);

        //assert
        assertNotNull(saslListener.getSavedException());
        assertEquals(IllegalStateException.class, saslListener.getSavedException().getClass());
    }

    // Tests_SRS_SASLLISTENERIMPL_34_012: [If any exception is thrown while the saslHandler handles the sasl mechanisms, this function shall save that exception and shall not create and send the init payload.]
    @Test
    public void onSaslMechanismHandlerThrowsDoesNotSendInit() throws Exception
    {
        //arrange
        final String[] remoteMechanisms = new String[] {"1", "2"};
        final String chosenMechanism = "1";
        final byte[] initPayload = new byte[] {0, 0, 0};
        SaslListenerImpl saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getRemoteMechanisms();
                result = remoteMechanisms;

                mockedSaslHandler.chooseSaslMechanism(remoteMechanisms);
                result = new Exception();

                mockedSaslHandler.getInitPayload(chosenMechanism);
                result = initPayload;
            }
        };

        //act
        saslListener.onSaslMechanisms(mockedSasl, mockedTransport);

        //assert
        assertNotNull("Expected a saved exception", saslListener.getSavedException());
        new Verifications()
        {
            {
                mockedSaslHandler.chooseSaslMechanism(remoteMechanisms);
                times = 1;

                mockedSaslHandler.getInitPayload(chosenMechanism);
                times = 0;

                mockedSasl.send(initPayload, 0, initPayload.length);
                times = 0;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_013: [If any exception is thrown while the saslHandler builds the init payload, this function shall save that exception and shall not send the returned init payload.]
    @Test
    public void onBuildInitHandlerThrowsDoesNotSendInit() throws Exception
    {
        //arrange
        final String[] remoteMechanisms = new String[] {"1", "2"};
        final String chosenMechanism = "1";
        final byte[] initPayload = new byte[] {0, 0, 0};
        SaslListenerImpl saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.getRemoteMechanisms();
                result = remoteMechanisms;

                mockedSaslHandler.chooseSaslMechanism(remoteMechanisms);
                result = remoteMechanisms;

                mockedSaslHandler.getInitPayload(chosenMechanism);
                result = new SecurityException();
            }
        };

        //act
        saslListener.onSaslMechanisms(mockedSasl, mockedTransport);

        //assert
        assertNotNull("Expected a saved exception", saslListener.getSavedException());
        assertEquals(SecurityException.class, saslListener.getSavedException().getClass());
        new Verifications()
        {
            {
                mockedSaslHandler.chooseSaslMechanism(remoteMechanisms);
                times = 1;

                mockedSaslHandler.getInitPayload(chosenMechanism);
                times = 1;

                mockedSasl.send(initPayload, 0, initPayload.length);
                times = 0;
            }
        };
    }

    // Tests_SRS_SASLLISTENERIMPL_34_014: [If any exception is thrown while the saslHandler handles the challenge, this function shall save that exception and shall not send the challenge response.]
    @Test
    public void onSaslChallengeThrowsDoesNotSendResponseTest() throws Exception
    {
        //arrange
        final byte[] challengePayload = new byte[] {0, 0, 0};
        final byte[] challengeResponsePayload = new byte[] {1, 1, 1};
        SaslListenerImpl saslListener = new SaslListenerImpl(mockedSaslHandler);
        new NonStrictExpectations()
        {
            {
                mockedSasl.pending();
                result = challengePayload.length;

                mockedSasl.recv((byte[]) any, 0, challengePayload.length);

                mockedSaslHandler.handleChallenge((byte[]) any);
                result = new SecurityException();
            }
        };

        //act
        saslListener.onSaslChallenge(mockedSasl, mockedTransport);

        //assert
        assertNotNull(saslListener.getSavedException());
        assertEquals(SecurityException.class, saslListener.getSavedException().getClass());
        new Verifications()
        {
            {
                mockedSasl.recv((byte[]) any, 0, challengePayload.length);
                times = 1;

                mockedSaslHandler.handleChallenge((byte[]) any);
                times = 1;

                mockedSasl.send(challengeResponsePayload, 0, challengeResponsePayload.length);
                times = 0;
            }
        };
    }

}
