// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Link;
import org.apache.qpid.proton.engine.Receiver;

/**
 * Copy of {@link org.apache.qpid.proton.reactor.FlowController} but with some small edits for logging purposes.
 *
 * Use this FlowController to automatically flow link credit back to the service from this client's receiver links after
 * each delivery on the receiver link.
 */
@Slf4j
public class LoggingFlowController extends BaseHandler
{
    private static final int WINDOW = 1024;
    private final String linkCorrelationId;

    public LoggingFlowController(String linkCorrelationId)
    {
        this.linkCorrelationId = linkCorrelationId;
    }

    private void topup(Receiver link)
    {
        int delta = WINDOW - link.getCredit();
        log.trace("Flowing {} credit(s) back to service on receiver link with correlation id {}", delta, this.linkCorrelationId);
        link.flow(delta);
    }

    @Override
    public void onUnhandled(Event event)
    {
        Link link = event.getLink();

        switch (event.getType())
        {
            case LINK_LOCAL_OPEN:
            case LINK_REMOTE_OPEN:
            case LINK_FLOW:
            case DELIVERY:
                if (link instanceof Receiver)
                {
                    topup((Receiver)link);
                }
                break;
            default:
                break;
        }
    }
}
