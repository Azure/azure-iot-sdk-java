// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.*;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AmqpDeviceOperations
{
    protected String senderLinkTag;
    protected String receiverLinkTag;

    protected Map<Symbol, Object> amqpProperties = new HashMap<>();

    protected Sender senderLink;
    protected Receiver receiverLink;

    protected String amqpLinkAddress;

    /**
     * Constructor for Amqp Device Operations
     */
    public AmqpDeviceOperations()
    {
        this.senderLinkTag = "provision_sender_link";
        this.receiverLinkTag = "provision_receiver_link";

        this.senderLink = null;
        this.receiverLink = null;
    }

    /**
     * Retrieves a message from a link
     * @param linkName The name of the link to receive
     * @return The Amqp Message from the specified link
     */
    public AmqpMessage receiverMessageFromLink(String linkName)
    {
        AmqpMessage result;
        if (linkName == null || linkName.isEmpty())
        {
            result = null;
        }
        else if (this.receiverLink == null)
        {
            result = null;
        }
        else
        {
            Delivery delivery = this.receiverLink.current();
            if (linkName.equals(this.receiverLinkTag) && (delivery != null) && delivery.isReadable() && !delivery.isPartial())
            {
                int size = delivery.pending();
                byte[] buffer = new byte[size];
                int read = this.receiverLink.recv(buffer, 0, buffer.length);

                this.receiverLink.advance();

                result = new AmqpMessage();
                result.decode(buffer, 0, read);
            }
            else
            {
                result = null;
            }
        }
        return result;
    }

    /**
     * Open the Session links for session
     * @param session the {@link Session} endpoint.
     * @throws IOException if the function failed to open the links for the provided session.
     * @throws IllegalArgumentException if the session is {@code null}.
     */
    public void openLinks(Session session) throws IOException, IllegalArgumentException
    {
        if (session == null)
        {
            throw new IllegalArgumentException("The session cannot be null.");
        }

        try
        {
            this.receiverLink = session.receiver(this.receiverLinkTag);
            this.receiverLink.setProperties(this.amqpProperties);
            this.receiverLink.open();

            this.senderLink = session.sender(this.senderLinkTag);
            this.senderLink.setProperties(this.amqpProperties);
            this.senderLink.open();
        }
        catch (Exception e)
        {
            throw new IOException("Proton exception", e);
        }
    }

    /**
     * Is this name a receiver Link
     * @param name Name of the link
     * @return if this is a receiver link
     */
    public synchronized boolean isReceiverLinkTag(String name)
    {
        if (name != null && this.receiverLinkTag.equals(name))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Initializes the link's other endpoint according to its type
     * @param link The link which shall be initialize.
     * @throws IllegalArgumentException if link argument is null
     * @throws IOException if Proton throws
     */
    public synchronized void initLink(Link link) throws IOException, IllegalArgumentException
    {
        if (link == null)
        {
            throw new IllegalArgumentException("The link cannot be null.");
        }

        try
        {
            if (link.getName().equals(this.senderLinkTag))
            {
                Target target = new Target();
                target.setAddress(this.amqpLinkAddress);
                link.setTarget(target);
                link.setSenderSettleMode(SenderSettleMode.UNSETTLED);
            }
            if (link.getName().equals(this.receiverLinkTag))
            {
                Source source = new Source();
                source.setAddress(this.amqpLinkAddress);
                link.setSource(source);
            }
        }
        catch (Exception e)
        {
            throw new IOException("Proton exception: " + e.getMessage());
        }
    }

    /**
     * Sends a Message on the senderLink
     * @param tag delivery tag
     * @param data Data to be sent on the link
     * @param length Length of the data
     * @param offset Offset of the data to send
     */
    public void sendMessage(byte[] tag, byte[] data, int length, int offset)
    {
        this.senderLink.delivery(tag);
        this.senderLink.send(data, offset, length);
        this.senderLink.advance();
    }

    /**
     * Close the links
     */
    public synchronized void closeLinks()
    {
        if (this.receiverLink != null)
        {
            this.receiverLink.close();
        }
        if (this.senderLink != null)
        {
            this.senderLink.close();
        }
    }

    protected void addAmqpLinkProperty(String key, String value)
    {
        this.amqpProperties.put(Symbol.getSymbol(key), value);
    }

    protected void clearAmqpLinkProperty()
    {
        this.amqpProperties.clear();
    }
}
