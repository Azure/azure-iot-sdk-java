package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

/**
 * Status callbacks to be executed by an AMQP session handler to notify its connection handler when something happened
 * to one of its sessions. Should only be implemented by connection handler objects.
 */
interface AmqpsSessionStateCallback
{
    /**
     * Executed when a device's amqp session has successfully been opened. For multiplexing scenarios, this callback will
     * be fired once per device.
     *
     * @param deviceId the id of the device that had its session opened successfully
     */
    void onDeviceSessionOpened(String deviceId);

    /**
     * Executed when the authentication session has successfully been opened. Never called for x509 auth.
     */
    void onAuthenticationSessionOpened();

    /**
     * Executed when a message sent in this connection was acknowledged by the service.
     *
     * @param message the message that was acknowledged.
     * @param deliveryState state information that describes if the message was accepted by the receiver or not.
     * @param deviceId the Id of the device whose message was acknowledged.
     */
    void onMessageAcknowledged(Message message, DeliveryState deliveryState, String deviceId);

    /**
     * Executed when a message was received by a session that this connection owns. This message should be acknowledged later.
     *
     * @param message the message that was received.
     */
    void onMessageReceived(IotHubTransportMessage message);

    /**
     * Executed when SAS based authentication fails for a device in this connection
     *
     * @param deviceId the Id of the device for which the authentication failed.
     * @param transportException the cause of that failure.
     */
    void onAuthenticationFailed(String deviceId, TransportException transportException);

    /**
     * Executed if a session closes unexpectedly. May be because one of its links closed unexpectedly, or if the session
     * closed unexpectedly.
     *
     * @param errorCondition the condition of the session that caused the close if the session closed remotely, or the condition
     *                       of the link that closed unexpectedly.
     * @param deviceId the device that the session belonged to.
     */
    void onSessionClosedUnexpectedly(ErrorCondition errorCondition, String deviceId);

    /**
     * Executed if a session closes, but it was expected. Likely due to user calling close on the connection, or
     * unregistering a device from an active multiplexed connection
     *
     * @param deviceId the device whose connection closed.
     */
    void onSessionClosedAsExpected(String deviceId);

    /**
     * Executed if the CBS session closes unexpectedly. May be because one of its links closed unexpectedly, or if the
     * session closed unexpectedly.
     *
     * @param errorCondition the condition of the session that caused the close if the session closed remotely, or the condition
     *                       of the link that closed unexpectedly.
     */
    void onCBSSessionClosedUnexpectedly(ErrorCondition errorCondition);
}
