// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatchMessage;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.serializers.FileUploadNotificationParser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class MessageFeedbackReceiverLinkHandler extends ReceiverLinkHandler
{
    private final Function<FeedbackBatch, AcknowledgementType> feedbackBatchReceivedCallback;

    public MessageFeedbackReceiverLinkHandler(
        Receiver link,
        LinkStateCallback linkStateCallback,
        Function<FeedbackBatch, AcknowledgementType> feedbackBatchReceivedCallback)
    {
        super(link, UUID.randomUUID().toString(), linkStateCallback);

        this.feedbackBatchReceivedCallback = feedbackBatchReceivedCallback;
    }

    /**
     * Event handler for the on delivery event
     * @param event The proton event object
     */
    @Override
    public void onDelivery(Event event)
    {
        Receiver recv = (Receiver)event.getLink();
        Delivery delivery = recv.current();

        if (delivery.isReadable() && !delivery.isPartial())
        {
            int size = delivery.pending();
            byte[] buffer = new byte[size];
            int read = recv.recv(buffer, 0, buffer.length);
            recv.advance();

            org.apache.qpid.proton.message.Message msg = Proton.message();
            msg.decode(buffer, 0, read);

            if (msg.getBody() instanceof Data)
            {
                AcknowledgementType messageResult = AcknowledgementType.ABANDON;
                try
                {
                    String feedbackJson = ((Data) msg.getBody()).getValue().toString();
                    FeedbackBatch feedbackBatch = FeedbackBatchMessage.parse(feedbackJson);

                    messageResult = feedbackBatchReceivedCallback.apply(feedbackBatch);
                }
                catch (Exception e)
                {
                    log.warn("Encountered an exception while handling feedback batch message", e);
                }

                DeliveryState deliveryState = Accepted.getInstance();
                if (messageResult == AcknowledgementType.ABANDON)
                {
                    deliveryState = Released.getInstance();
                }
                else if (messageResult == AcknowledgementType.COMPLETE)
                {
                    deliveryState = Accepted.getInstance();
                }

                delivery.disposition(deliveryState);
                delivery.settle();
                recv.flow(1); // flow back the credit so the service can send another message now
            }
        }
    }

    @Override
    String getLinkInstanceType()
    {
        return "messageFeedbackReceiver";
    }
}
