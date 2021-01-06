/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.ErrorLoggingBaseHandlerWithCleanup;
import mockit.Expectations;
import mockit.Mocked;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.Reactor;
import org.junit.Test;

public class ErrorLoggingBaseHandlerWithCleanupTest
{
    @Mocked Event mockEvent;
    @Mocked
    Reactor mockReactor;
    @Mocked
    Connection mockConnection;
    @Mocked
    Session mockSession;
    @Mocked
    Link mockLink;

    @Test
    public void onConnectionLocalCloseStopsReactorIfConnectionClosedRemotely()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getRemoteState();
                result = EndpointState.CLOSED;
                mockEvent.getReactor();
                result = mockReactor;
                mockReactor.stop();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onConnectionLocalClose(mockEvent);
    }

    @Test
    public void onConnectionLocalCloseStopsReactorIfConnectionOpenRemotely()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getRemoteState();
                result = EndpointState.ACTIVE;
                mockEvent.getReactor();
                result = mockReactor;
                times = 0;

                mockReactor.stop();
                times = 0;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onConnectionLocalClose(mockEvent);
    }

    @Test
    public void onConnectionRemoteCloseStopsReactorIfConnectionLocallyClosed()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getLocalState();
                result = EndpointState.CLOSED;
                mockEvent.getReactor();
                result = mockReactor;
                mockReactor.stop();
                times = 1;
                mockConnection.close();
                times = 0;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onConnectionRemoteClose(mockEvent);
    }

    //Expect the handler to close the connection locally, but not the reactor yet
    @Test
    public void onConnectionRemoteCloseIfConnectionLocallyOpen()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;
                mockConnection.getLocalState();
                result = EndpointState.ACTIVE;
                mockEvent.getReactor();
                result = mockReactor;
                times = 0;

                mockReactor.stop();
                times = 0;

                //shouldn't close reactor, but should locally close the connection
                mockConnection.close();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onConnectionRemoteClose(mockEvent);
    }

    @Test
    public void onLinkLocalCloseClosesLocalSession()
    {
        new Expectations()
        {
            {
                mockEvent.getSession();
                result = mockSession;

                mockSession.close();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onLinkLocalClose(mockEvent);
    }

    @Test
    public void onSessionLocalCloseClosesLocalConnection()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;

                mockConnection.close();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onSessionLocalClose(mockEvent);
    }

    @Test
    public void onLinkRemoteCloseClosesLocalLink()
    {
        new Expectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;

                mockLink.getLocalState();
                result = EndpointState.ACTIVE;

                mockLink.close();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onLinkRemoteClose(mockEvent);
    }

    @Test
    public void onLinkRemoteCloseDoesNotCloseLocalLinkIfNotLocallyOpen()
    {
        new Expectations()
        {
            {
                mockEvent.getLink();
                result = mockLink;

                mockLink.getLocalState();
                result = EndpointState.CLOSED;

                mockLink.close();
                times = 0;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onLinkRemoteClose(mockEvent);
    }

    @Test
    public void onSessionRemoteCloseClosesLocalSession()
    {
        new Expectations()
        {
            {
                mockEvent.getSession();
                result = mockSession;

                mockSession.getLocalState();
                result = EndpointState.ACTIVE;

                mockSession.close();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onSessionRemoteClose(mockEvent);
    }

    @Test
    public void onSessionRemoteCloseDoesNotCloseLocalSessionIfNotLocallyOpen()
    {
        new Expectations()
        {
            {
                mockEvent.getSession();
                result = mockSession;

                mockSession.getLocalState();
                result = EndpointState.CLOSED;

                mockSession.close();
                times = 0;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onSessionRemoteClose(mockEvent);
    }

    @Test
    public void onTransportErrorClosesConnection()
    {
        new Expectations()
        {
            {
                mockEvent.getConnection();
                result = mockConnection;

                mockConnection.close();
                times = 1;
            }
        };

        ErrorLoggingBaseHandlerWithCleanup handler = new ErrorLoggingBaseHandlerWithCleanup();
        handler.onTransportError(mockEvent);
    }
}
