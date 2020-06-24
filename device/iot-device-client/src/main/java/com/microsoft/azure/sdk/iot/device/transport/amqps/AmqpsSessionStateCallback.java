package com.microsoft.azure.sdk.iot.device.transport.amqps;

import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

/**
 * Status callbacks to be executed to notify the connection level when something happened to one of its sessions. Should
 * only be implemented by connection handler objects.
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
     */
    void onMessageAcknowledged(Message message);

    /**
     * Executed when a message was received by a session that this connection owns. This message should be acknowledged later.
     *
     * @param message the message that was received.
     */
    void onMessageReceived(IotHubTransportMessage message);

    /**
     * Executed when SAS based authentication fails for a device in this connection
     *
     * @param transportException the cause of that failure.
     */
    void onAuthenticationFailed(TransportException transportException);

    /**
     * Executed if a session closes unexpectedly. May be because one of its links closed unexpectedly, or if the session
     * closed unexpectedly.
     *
     * @param errorCondition the condition of the session that caused the close if the session closed remotely, or the condition
     *                       of the link that closed unexpectedly
     */
    void onSessionClosedUnexpectedly(ErrorCondition errorCondition);
}
