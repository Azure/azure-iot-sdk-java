// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.serializers.FileUploadNotificationParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.amqp.messaging.Released;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.Event;
import org.apache.qpid.proton.engine.Receiver;

import java.util.UUID;
import java.util.function.Function;

@Slf4j
public class FileUploadNotificationReceiverLinkHandler extends ReceiverLinkHandler
{
    private final Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback;

    public FileUploadNotificationReceiverLinkHandler(
        Receiver link,
        LinkStateCallback linkStateCallback,
        Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationReceivedCallback)
    {
        super(link, UUID.randomUUID().toString(), linkStateCallback);

        this.fileUploadNotificationReceivedCallback = fileUploadNotificationReceivedCallback;
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
                String fileUploadNotificationJson = ((Data) msg.getBody()).getValue().toString();
                AcknowledgementType messageResult = AcknowledgementType.ABANDON;

                try
                {
                    FileUploadNotificationParser notificationParser = new FileUploadNotificationParser(fileUploadNotificationJson);

                    FileUploadNotification fileUploadNotification = new FileUploadNotification(notificationParser.getDeviceId(),
                        notificationParser.getBlobUri(), notificationParser.getBlobName(), notificationParser.getLastUpdatedTime(),
                        notificationParser.getBlobSizeInBytesTag(), notificationParser.getEnqueuedTimeUtc());

                    messageResult = fileUploadNotificationReceivedCallback.apply(fileUploadNotification);
                }
                catch (Exception e)
                {
                    // this should never happen. However if it does, proton can't handle it. So guard against throwing it at proton.
                    log.warn("Encountered an exception while handling file upload notification", e);
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
        return "fileUploadNotificationReceiver";
    }
}
