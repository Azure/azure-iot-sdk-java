/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.unit.com.microsoft.azure.sdk.iot.deps.transport.amqp;

import com.microsoft.azure.sdk.iot.deps.transport.amqp.*;
import com.microsoft.azure.sdk.iot.deps.util.ObjectLock;
import mockit.Deencapsulation;
import org.apache.qpid.proton.Proton;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.integration.junit4.JMockit;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.reactor.*;
import org.apache.qpid.proton.reactor.ReactorOptions;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.BufferOverflowException;
import java.util.concurrent.*;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;

/** Unit tests for AmqpConnection.
 * Coverage : 95% method, 100% line */
@RunWith(JMockit.class)
public class AmqpConnectionTest
{
    private static final String TEST_HOST_NAME = "testHostName";
    private static final String TEST_LINK_NAME = "testLinkName";

    @Mocked
    private Proton mockedProton;

    @Mocked
    private Reactor mockedReactor;

    @Mocked
    private AmqpReactor mockedAmqpReactor;

    @Mocked
    private ExecutorService mockedExecutorService;

    @Mocked
    private AmqpDeviceOperations mockedProvisionOperations;

    @Mocked
    private Transport mockedTransport;

    @Mocked
    private AmqpListener mockedAmqpListener;

    @Mocked
    private AmqpMessage mockedMessage;

    @Mocked
    private ObjectLock mockedObjectLock;

    @Mocked
    private Session mockedSession;

    @Mocked
    private Connection mockedConnection;

    @Mocked
    private Event mockedEvent;

    @Mocked
    private SslDomain domain;

    @Mocked
    private Link mockedLink;

    @Mocked
    private Delivery mockedDelivery;

    @Mocked
    private DeliveryState mockDeliveryState;

    @Test (expected = IllegalArgumentException.class)
    public void AmqpsConnectionThrowsOnHostNameNull() throws IOException
    {
        new AmqpsConnection(null, mockedProvisionOperations, null, null, false);
    }

    @Test
    public void AmqpsConnectionConstructorSucceeds() throws IOException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                mockedProton.reactor((ReactorOptions)any, (Handler)any);
                result = mockedReactor;
            }
        };

        // Act
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        //assert
        assertEquals(TEST_HOST_NAME, Deencapsulation.getField(amqpsConnection, "hostName"));
    }

    @Test (expected = IOException.class)
    public void AmqpsConnectionConstructorThrowsOnReactor() throws IOException
    {
        // Arrange
        new NonStrictExpectations()
        {
            {
                mockedProton.reactor((ReactorOptions)any, (Handler)any);
                result = new IOException();
            }
        };

        // Act
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);
    }

    @Test (expected = IllegalArgumentException.class)
    public void SetListenerNullSucceeds() throws IOException
    {
        // Arrange
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        amqpsConnection.setListener(null);

        //assert
    }

    @Test
    public void SetListenerSucceeds() throws IOException
    {
        // Arrange
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        amqpsConnection.setListener(mockedAmqpListener);

        //assert
    }

    @Test (expected = Exception.class)
    public void OpenThrowExceptionReactor() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                new AmqpReactor((Reactor)any);
                result = new Exception();
            }
        };

        // Act
        amqpsConnection.open();

        //assert
    }

    @Test (expected = IOException.class)
    public void OpenThrowsOnWaitLock() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                new AmqpReactor((Reactor)any);
                result = mockedAmqpReactor;

                mockedObjectLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        // Act
        amqpsConnection.open();

        //assert
    }

    @Test
    public void OpenSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                new AmqpReactor((Reactor)any);
                result = mockedAmqpReactor;

                mockedObjectLock.waitLock(anyLong);
            }
        };

        // Act
        amqpsConnection.open();

        //assert
    }

    @Test (expected = IOException.class)
    public void closeThrowsOnWaitLock() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "isOpen", true);
        Deencapsulation.setField(amqpsConnection, "session", mockedSession);
        Deencapsulation.setField(amqpsConnection, "connection", mockedConnection);
        Deencapsulation.setField(amqpsConnection, "reactor", mockedReactor);

        new NonStrictExpectations()
        {
            {
                mockedObjectLock.waitLock(anyLong);
                result = new InterruptedException();
            }
        };

        // Act
        amqpsConnection.close();

        //assert
    }

    @Test
    public void closeThrowOnShutdown() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "isOpen", true);
        Deencapsulation.setField(amqpsConnection, "session", mockedSession);
        Deencapsulation.setField(amqpsConnection, "connection", mockedConnection);
        Deencapsulation.setField(amqpsConnection, "reactor", mockedReactor);
        Deencapsulation.setField(amqpsConnection, "executorService", mockedExecutorService);

        new NonStrictExpectations()
        {
            {
                mockedObjectLock.waitLock(anyLong);

                mockedExecutorService.shutdown();

                mockedExecutorService.awaitTermination(anyLong, (TimeUnit) any);
                result = new InterruptedException();

                mockedExecutorService.shutdownNow();
            }
        };

        // Act
        amqpsConnection.close();

        //assert
    }

    @Test
    public void closeSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "isOpen", true);
        Deencapsulation.setField(amqpsConnection, "session", mockedSession);
        Deencapsulation.setField(amqpsConnection, "connection", mockedConnection);
        Deencapsulation.setField(amqpsConnection, "reactor", mockedReactor);
        Deencapsulation.setField(amqpsConnection, "executorService", mockedExecutorService);

        new NonStrictExpectations()
        {
            {
                mockedObjectLock.waitLock(anyLong);
            }
        };

        // Act
        amqpsConnection.close();

        //assert
    }

    @Test
    public void onReactorInitSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getReactor();
                result = mockedReactor;
            }
        };

        // Act
        amqpsConnection.onReactorInit(mockedEvent);

        //assert
    }

    @Test
    public void onReactorFinalSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        amqpsConnection.onReactorFinal(mockedEvent);

        //assert
    }

    @Test
    public void onConnectionInitThrowOnOpenLinks() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getConnection();
                result = mockedConnection;

                mockedConnection.session();
                result = mockedSession;

                mockedConnection.open();
                mockedSession.open();

                mockedProvisionOperations.openLinks(mockedSession);
                result = new Exception();
            }
        };

        // Act
        amqpsConnection.onConnectionInit(mockedEvent);

        //assert
    }

    @Test
    public void onConnectionInitSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getConnection();
                result = mockedConnection;

                mockedConnection.session();
                result = mockedSession;

                mockedConnection.open();
                mockedSession.open();

                mockedProvisionOperations.openLinks(mockedSession);
            }
        };

        // Act
        amqpsConnection.onConnectionInit(mockedEvent);

        //assert
    }

    @Test
    public void onConnectionBoundSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getConnection();
                result = mockedConnection;

                mockedConnection.getTransport();
                result = mockedTransport;

                mockedProton.sslDomain();
                result = domain;

                mockedTransport.ssl(domain);
            }
        };

        // Act
        amqpsConnection.onConnectionBound(mockedEvent);

        //assert
    }

    @Test
    public void onConnectionBoundThrowOnSslDomain() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getConnection();
                result = mockedConnection;

                mockedConnection.getTransport();
                result = mockedTransport;

                mockedProton.sslDomain();
                result = new IOException();
            }
        };

        // Act
        amqpsConnection.onConnectionBound(mockedEvent);

        //assert
    }

    @Test
    public void onConnectionUnboundSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        amqpsConnection.onConnectionUnbound(mockedEvent);

        //assert
        assertEquals(false, Deencapsulation.getField(amqpsConnection, "isOpen"));
    }

    @Test
    public void onLinkInitSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getLink();
                result = mockedLink;

                mockedProvisionOperations.initLink(mockedLink);
            }
        };

        // Act
        amqpsConnection.onLinkInit(mockedEvent);

        //assert
    }

    @Test
    public void onLinkInitThrowsOnGetLink() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getLink();
                result = new Exception();
            }
        };

        // Act
        amqpsConnection.onLinkInit(mockedEvent);

        //assert
    }

    @Test
    public void onLinkRemoteOpenSucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "msgListener", mockedAmqpListener);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getLink();
                result = mockedLink;
                mockedLink.getName();
                result = TEST_LINK_NAME;

                mockedProvisionOperations.isReceiverLinkTag(anyString);
                result = true;
            }
        };

        // Act
        amqpsConnection.onLinkRemoteOpen(mockedEvent);

        //assert
    }

    @Test
    public void sendAmqpMessageNotConnected() throws Exception
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        boolean result = amqpsConnection.sendAmqpMessage(mockedMessage);

        //assert
        assertEquals(false, result);
    }

    @Test
    public void sendAmqpMessageEncodeThrowsBufferOverflowSuccess() throws Exception
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "isOpen", true);
        Deencapsulation.setField(amqpsConnection, "amqpDeviceOperations", mockedProvisionOperations);

        new NonStrictExpectations()
        {
            {
                mockedMessage.encode((byte[])any, 0);
                result = new BufferOverflowException();
                mockedMessage.encode((byte[])any, 0);
                result = 10;
            }
        };

        // Act
        boolean result = amqpsConnection.sendAmqpMessage(mockedMessage);

        //assert
        assertEquals(true, result);
    }

    @Test
    public void sendAmqpMessageLengthZero() throws Exception
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "isOpen", true);
        Deencapsulation.setField(amqpsConnection, "amqpDeviceOperations", mockedProvisionOperations);

        new NonStrictExpectations()
        {
            {
                mockedMessage.encode((byte[])any, 0);
                result = 0;
            }
        };

        // Act
        boolean result = amqpsConnection.sendAmqpMessage(mockedMessage);

        //assert
        assertEquals(false, result);
    }

    @Test
    public void sendAmqpMessageSucceeds() throws Exception
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "isOpen", true);
        Deencapsulation.setField(amqpsConnection, "amqpDeviceOperations", mockedProvisionOperations);

        new NonStrictExpectations()
        {
            {
                mockedMessage.encode((byte[])any, 0);
                result = 10;
            }
        };

        // Act
        boolean result = amqpsConnection.sendAmqpMessage(mockedMessage);

        //assert
        assertEquals(true, result);
    }

    @Test
    public void onDeliverySucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        Deencapsulation.setField(amqpsConnection, "msgListener", mockedAmqpListener);

        new NonStrictExpectations()
        {
            {
                mockedProvisionOperations.receiverMessageFromLink(anyString);
                result = mockedMessage;

                mockedEvent.getType();
                result = Event.Type.TRANSPORT;

                mockedAmqpListener.messageReceived(mockedMessage);
            }
        };

        // Act
        amqpsConnection.onDelivery(mockedEvent);

        //assert
    }

    @Test
    public void onDeliveryTypeDeliverySucceeds() throws IOException, InterruptedException
    {
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedProvisionOperations.receiverMessageFromLink(anyString);
                result = null;

                mockedEvent.getType();
                result = Event.Type.DELIVERY;

                mockedEvent.getDelivery();
                result = mockedDelivery;

                mockedDelivery.getRemoteState();
                result = mockDeliveryState;

                mockDeliveryState.equals(any);

                mockedDelivery.free();
            }
        };

        // Act
        amqpsConnection.onDelivery(mockedEvent);

        //assert
    }

    @Test
    public void onLinkFlowSucceeds() throws IOException
    {
        // Arrange
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        new NonStrictExpectations()
        {
            {
                mockedEvent.getLink();
                result = mockedLink;

                mockedLink.getCredit();
                result = 100;
            }
        };

        // Act
        amqpsConnection.onLinkFlow(mockedEvent);

        //assert
    }

    @Test
    public void onLinkRemoteCloseSucceeds() throws IOException
    {
        // Arrange
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        amqpsConnection.onLinkRemoteClose(mockedEvent);

        //assert
    }

    @Test
    public void onTransportErrorSucceeds() throws IOException
    {
        // Arrange
        AmqpsConnection amqpsConnection = new AmqpsConnection(TEST_HOST_NAME, mockedProvisionOperations, null, null,  false);

        // Act
        amqpsConnection.onTransportError(mockedEvent);

        //assert
        assertEquals(false, Deencapsulation.getField(amqpsConnection, "isOpen"));
    }
}

