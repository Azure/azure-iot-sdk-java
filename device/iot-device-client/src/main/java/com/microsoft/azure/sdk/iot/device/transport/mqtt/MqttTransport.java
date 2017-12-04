// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.mqtt;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubCallbackPacket;
import com.microsoft.azure.sdk.iot.device.transport.IotHubOutboundPacket;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransport;
import com.microsoft.azure.sdk.iot.device.transport.State;

import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * <p>
 * An MQTT transport. Contains functionality for adding messages and sending
 * batched messages to an IoT Hub. Buffers unsent messages until they are
 * received by an IoT Hub. A transport is bound at construction to the following
 * parameters: IoT Hub name, device ID, device key, and message valid seconds.
 * </p>
 * <p>
 * The transport can also poll an IoT Hub for messages and invoke a
 * user-defined message callback if a message and callback is found.
 * </p>
 */

public final class MqttTransport implements IotHubTransport
{
    /** The MQTT connection lock. */
    protected final Object sendMessagesLock = new Object();
    protected final Object handleMessageLock = new Object();

    private State state;

    /** The MQTT connection.*/
    private MqttIotHubConnection mqttIotHubConnection;

    /** Messages waiting to be sent. */
    private final Queue<IotHubOutboundPacket> waitingList;

    /** Messages whose callbacks that are waiting to be invoked. */
    private final Queue<IotHubCallbackPacket> callbackList;

    private IotHubConnectionStateCallback stateCallback;
    private Object stateCallbackContext;

    private final DeviceClientConfig config;

    /**
     * Constructs an instance from the given {@link DeviceClientConfig}
     * object.
     *
     * @param config configuration parameters for the connection.
     */
    public MqttTransport(DeviceClientConfig config)
    {
        // Codes_SRS_MQTTTRANSPORT_15_001: [The constructor shall initialize an empty transport queue
        // for adding messages to be sent as a batch.]
        this.waitingList = new LinkedBlockingDeque<>();
        // Codes_SRS_MQTTTRANSPORT_15_002: [The constructor shall initialize an empty queue
        // for completed messages whose callbacks are waiting to be invoked.]
        this.callbackList = new LinkedBlockingDeque<>();
        this.config = config;
        this.state = State.CLOSED;
    }

    /**
     * Establishes a communication channel with an IoT Hub. If a channel is
     * already open, the function shall do nothing.
     *
     * @throws IOException if a communication channel cannot be established.
     */
    public void open() throws IOException
    {
        // Codes_SRS_MQTTTRANSPORT_15_004: [If the MQTT connection is already open, the function shall do nothing.]
        if(this.state == State.OPEN)
        {
            return;
        }

        // Codes_SRS_MQTTTRANSPORT_15_003: [The function shall establish an MQTT connection
        // with the IoT Hub given in the configuration.]
        this.mqttIotHubConnection = new MqttIotHubConnection(this.config);
        this.mqttIotHubConnection.open();

        this.state = State.OPEN;
    }

    @Override
    public void multiplexOpen(List<DeviceClient> deviceClientList)
    {
        return;
    }

    /**
     * Closes all resources used to communicate with an IoT Hub. Once {@code close()} is
     * called, the transport is no longer usable. If the transport is already
     * closed, the function shall do nothing.
     */
    public void close() throws IOException
    {
        // Codes_SRS_MQTTTRANSPORT_15_006: [If the MQTT connection is closed, the function shall do nothing.]
        if (this.state == State.CLOSED)
        {
            return;
        }
        
        // Codes_SRS_MQTTTRANSPORT_99_020: [The method will remove all the messages which are in progress or waiting to be sent and add them to the callback list.]
        while (!this.waitingList.isEmpty())
        {
            IotHubOutboundPacket packet = this.waitingList.remove();

            IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE, packet.getCallback(), packet.getContext());
            this.callbackList.add(callbackPacket);
        }
       
        // Codes_SRS_MQTTTRANSPORT_99_021: [The method will invoke the callback list]
        invokeCallbacks(); 

        // Codes_SRS_MQTTTRANSPORT_15_005: [The function shall close the MQTT connection
        // with the IoT Hub given in the configuration.]
        this.mqttIotHubConnection.close();
        this.state = State.CLOSED;
    }

    /**
     * Adds a message to the transport queue.
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * invoked.
     *
     * @throws IllegalStateException if the transport has not been opened or is closed
     */
    public void addMessage(
            Message message,
            IotHubEventCallback callback,
            Object callbackContext) throws IllegalStateException
    {
        synchronized (sendMessagesLock)
        {
            // Codes_SRS_MQTTTRANSPORT_15_008: [If the transport is closed,
            // the function shall throw an IllegalStateException.]
            if (this.state == State.CLOSED)
            {
                throw new IllegalStateException("Cannot add a message to an MQTT transport that is closed.");
            }

            //Codes_SRS_MQTTTRANSPORT_15_007: [The function shall add a packet containing the message, callback,
            // and callback context to the transport queue.]
            IotHubOutboundPacket packet = new IotHubOutboundPacket(message, callback, callbackContext);
            this.waitingList.add(packet);
        }
    }

    /**
     * Adds a message to the transport queue.
     * !!! This functionality is not supported yet, please use `addMessage` with `IotHubEventCallback`!!!
     *
     * @param message the message to be sent.
     * @param callback the callback to be invoked when a response for the
     * message is received.
     * @param callbackContext the context to be passed in when the callback is
     * invoked.
     *
     * @throws UnsupportedOperationException always.
     */
    public void addMessage(
            Message message,
            IotHubResponseCallback callback,
            Object callbackContext) throws UnsupportedOperationException
    {
        // Codes_SRS_MQTTTRANSPORT_21_022: [The function shall throws `UnsupportedOperationException`.]
        throw new UnsupportedOperationException("MQTT do not support callback with message response");
    }

    /**
     * <p>
     * Sends all messages on the transport queue, one at a time. If a previous
     * send attempt had failed, the function will attempt to resend the messages
     * in the previous attempt.
     * </p>
     * If one has not already been created, the function will initialize an
     * MQTT connection with the IoT Hub specified in the configuration.
     *
     * @throws IllegalStateException if the transport has not been opened or is closed.
     */
    public void sendMessages() throws IllegalStateException
    {
        synchronized (sendMessagesLock)
        {
            // Codes_SRS_MQTTTRANSPORT_15_012: [If the MQTT connection is closed,
            // the function shall throw an IllegalStateException.]
            if (this.state == State.CLOSED)
            {
                throw new IllegalStateException("MQTT transport is closed.");
            }

            if (this.waitingList.size() <= 0)
            {
                return;
            }

            // Codes_SRS_MQTTTRANSPORT_15_009: [The function shall attempt to send every message
            // on its waiting list, one at a time.]
            while (!this.waitingList.isEmpty())
            {
                IotHubOutboundPacket packet = this.waitingList.remove();

                if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN && this.config.getSasTokenAuthentication().isRenewalNecessary())
                {
                    //Codes_SRS_MQTTTRANSPORT_34_023: [If the config is using sas token auth and its token has expired, the message shall not be sent, but shall be added to the callback list with IotHubStatusCode UNAUTHORIZED.]
                    IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.UNAUTHORIZED, packet.getCallback(), packet.getContext());
                    this.callbackList.add(callbackPacket);

                    //Codes_SRS_MQTTTRANSPORT_34_024: [If the config is using sas token auth, its token has expired, and the connection status callback is not null, the connection status callback will be fired with SAS_TOKEN_EXPIRED.]
                    if (this.stateCallback != null)
                    {
                        this.stateCallback.execute(IotHubConnectionState.SAS_TOKEN_EXPIRED, this.stateCallbackContext);
                    }
                }
                else if (packet.getMessage().isExpired())
                {
                    //Codes_SRS_MQTTTRANSPORT_34_027: [If the packet to be sent contains a message that has expired, the message shall not be sent, but shall be added to the callback list with IotHubStatusCode MESSAGE_EXPIRED.]
                    IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(IotHubStatusCode.MESSAGE_EXPIRED, packet.getCallback(), packet.getContext());
                    this.callbackList.add(callbackPacket);
                }
                else
                {
                    try
                    {
                        IotHubStatusCode status = this.mqttIotHubConnection.sendEvent(packet.getMessage());

                        // Codes_SRS_MQTTTRANSPORT_15_010: [For each message being sent, the function shall add
                        // the IoT Hub status code along with the callback and context to the callback list.]
                        IotHubCallbackPacket callbackPacket = new IotHubCallbackPacket(status, packet.getCallback(), packet.getContext());
                        this.callbackList.add(callbackPacket);
                    }
                    // Codes_SRS_MQTTTRANSPORT_15_011: [If the IoT Hub could not be reached, the message
                    // shall be buffered to be sent again next time.]
                    catch (IllegalStateException e)
                    {
                        this.waitingList.add(packet);
                    }
                }
            }
        }
    }

    /**
     * Invokes the callbacks for all completed requests.
     *
     * @throws IllegalStateException if the transport has not been opened or is closed.
     */
    public void invokeCallbacks() throws IllegalStateException
    {
        synchronized (sendMessagesLock)
        {
            // Codes_SRS_MQTTTRANSPORT_15_014: [If the transport is closed,
            // the function shall throw an IllegalStateException.]
            if (this.state == State.CLOSED)
            {
                throw new IllegalStateException("MQTT transport is closed.");
            }

            // Codes_SRS_MQTTTRANSPORT_15_013: [The function shall invoke all callbacks on the callback queue.]
            while (!this.callbackList.isEmpty())
            {
                // Codes_SRS_MQTTTRANSPORT_15_015: [If an exception is thrown during the callback,
                // the function shall drop the callback from the queue.]
                IotHubCallbackPacket packet = this.callbackList.remove();

                IotHubStatusCode status = packet.getStatus();
                IotHubEventCallback callback = packet.getCallback();
                Object context = packet.getContext();

                callback.execute(status, context);
            }
        }
    }

    /**
     * <p>
     * Invokes the message callback every time a message arrives from the server.
     * responds to the IoT Hub on how the processed message should be
     * handled by the IoT Hub.
     * </p>
     * If no message callback is set, the function will do nothing.
     *
     * @throws IllegalStateException if the transport has not been opened or is closed.
     */
    public void handleMessage() throws IllegalStateException, IOException
    {
        synchronized (handleMessageLock)
        {
            // Codes_SRS_MQTTTRANSPORT_15_018: [If the MQTT connection is closed,
            // the function shall throw an IllegalStateException.]
            if (this.state == State.CLOSED)
            {
                throw new IllegalStateException("MQTT transport is closed.");
            }

            MessageCallback callback = this.config.getDeviceTelemetryMessageCallback();
            Object context = this.config.getDeviceTelemetryMessageContext();

            MessageCallback deviceTwinMessageCallback = this.config.getDeviceTwinMessageCallback();
            Object deviceTwinContext = this.config.getDeviceTwinMessageContext();

            MessageCallback deviceMethodMessageCallback = this.config.getDeviceMethodsMessageCallback();
            Object deviceMethodContext = this.config.getDeviceMethodsMessageContext();

            if (callback == null && deviceTwinMessageCallback == null && deviceMethodMessageCallback == null)
            {
                return;
            }

            // Codes_SRS_MQTTTRANSPORT_15_016: [The function shall attempt to consume a message from the IoT Hub.]
            Message message = this.mqttIotHubConnection.receiveMessage();

            // Codes_SRS_MQTTTRANSPORT_15_017: [If a message is found and a message callback is registered,
            // the function shall invoke the callback on the message.]
            if (message != null)
            {
                if (message.getMessageType() == MessageType.DEVICE_METHODS)
                {
                    if (deviceMethodMessageCallback != null)
                    {
                        deviceMethodMessageCallback.execute(message, deviceMethodContext);
                    }
                }
                else if (message.getMessageType() == MessageType.DEVICE_TWIN)
                {
                    if (deviceTwinMessageCallback != null)
                    {
                        deviceTwinMessageCallback.execute(message, deviceTwinContext);
                    }
                }
                else
                {
                    if (callback != null)
                    {
                        callback.execute(message, context);
                    }
                }
            }
        }
    }

    /**
     * Returns {@code true} if the transport has no more messages to handle,
     * and {@code false} otherwise.
     *
     * @return {@code true} if the transport has no more messages to handle,
     * and {@code false} otherwise.
     */
    public boolean isEmpty()
    {
        synchronized (sendMessagesLock)
        {
            // Codes_SRS_MQTTTRANSPORT_15_019: [The function shall return true if the waiting list
            // and callback list are all empty, and false otherwise.]
            if (this.waitingList.isEmpty() && this.callbackList.isEmpty())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Registers a callback to be executed whenever the mqtt connection is lost or established.
     * 
     * @param callback the callback to be called.
     * @param callbackContext a context to be passed to the callback. Can be
     * {@code null} if no callback is provided.
     * @throws IllegalArgumentException if callback is null
     */
    public void registerConnectionStateCallback(IotHubConnectionStateCallback callback, Object callbackContext)
    {
        if (callback == null)
        {
            //Codes_SRS_MQTTTRANSPORT_34_025: [If the provided callback is null, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("Callback cannot be null");
        }

        //Codes_SRS_MQTTTRANSPORT_34_026: [This function shall register the connection state callback.]
        this.stateCallback = callback;
        this.stateCallbackContext = callbackContext;
    }
}