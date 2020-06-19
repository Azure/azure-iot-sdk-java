package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.engine.BaseHandler;

/**
 * Status callbacks to be executed to notify the session level when something happened to one of its links. Should
 * only be implemented by session handler objects.
 */
public interface AmqpsLinkStateCallback
{
    /**
     * Executed when a link has finished opening
     *
     * @param linkHandler the handler of the link that opened successfully
     */
    void onLinkOpened(BaseHandler linkHandler);

    /**
     * Executed when a message sent by a link in this session was acknowledged by the service
     *
     * @param message     the message that was sent
     * @param deliveryTag the integer that identifies which delivery this acknowledgement was tied to
     */
    void onMessageAcknowledged(Message message, int deliveryTag);

    /**
     * Executed when a message is received by a link in this session. This message should be acknowledged later.
     *
     * @param message the message that was received.
     */
    void onMessageReceived(IotHubTransportMessage message);

    /**
     * Executed if a link closes remotely unexpectedly
     *
     * @param errorCondition the condition of the link that caused the close
     */
    void onLinkClosedUnexpectedly(ErrorCondition errorCondition);
}
