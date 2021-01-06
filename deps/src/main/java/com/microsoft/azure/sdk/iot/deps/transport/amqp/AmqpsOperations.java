package com.microsoft.azure.sdk.iot.deps.transport.amqp;

import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Session;

import java.io.IOException;

public abstract class AmqpsOperations
{
    public abstract void openLinks(Session session) throws IOException, IllegalArgumentException;
    public abstract void initLink(Link link) throws IOException, IllegalArgumentException;
    public abstract void closeLinks();
}
