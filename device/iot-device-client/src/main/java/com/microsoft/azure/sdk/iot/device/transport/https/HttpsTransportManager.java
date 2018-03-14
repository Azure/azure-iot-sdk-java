// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.DeviceClientConfig;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.device.ResponseMessage;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportManager;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;

import java.io.IOException;

/**
 * Implementation of the transport manager for https.
 */
public class HttpsTransportManager implements IotHubTransportManager
{
    DeviceClientConfig config;
    HttpsIotHubConnection httpsIotHubConnection;

    /**
     * Constructor
     *
     * @param config is the set of device configurations.
     * @throws IllegalArgumentException is the config is null.
     */
    public HttpsTransportManager(DeviceClientConfig config) throws IllegalArgumentException
    {
        if(config == null)
        {
            //Codes_SRS_HTTPSTRANSPORTMANAGER_21_002: [If the provided `config` is null, the constructor shall throws IllegalArgumentException.]
            throw new IllegalArgumentException("config is null");
        }

        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_001: [The constructor shall store the device client configuration `config`.]
        this.config = config;
    }

    /**
     * Opens the connection by creating a new instance of the HttpsIotHubConnection.
     */
    public void open()
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_003: [The open shall create and store a new transport connection `HttpsIotHubConnection`.]
        this.httpsIotHubConnection = new HttpsIotHubConnection(config);
    }

    /**
     * Opens the connection by creating a new instance of the HttpsIotHubConnection.
     * The provided topics have no effect for HTTPS protocol, and it is ignored.
     *
     * @param topics is a list of topics to signed in.
     */
    public void open(String[] topics)
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_004: [The open shall create and store a new transport connection `HttpsIotHubConnection`.]
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_005: [The open shall ignore the parameter `topics`.]
        this.httpsIotHubConnection = new HttpsIotHubConnection(config);
    }

    /**
     * Close the connection destroying the HttpsIotHubConnection instance.
     */
    public void close()
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_006: [The close shall destroy the transport connection `HttpsIotHubConnection`.]
        this.httpsIotHubConnection = null;
    }

    /**
     * This is a blocking send message. It send the provide message, wait for the IotHub answer, and return is
     * in the ResponseMessage, which contains the status and the payload.
     *
     * @param message is the message to send.
     * @return the IotHub response with the status and payload.
     * @throws IOException if the IotHub communication failed.
     * @throws IllegalArgumentException if the provided message is null, or invalid.
     */
    public ResponseMessage send(IotHubTransportMessage message) throws IOException, IllegalArgumentException
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_007: [The send shall create a new instance of the `HttpMessage`, by parsing the Message with `parseHttpsJsonMessage` from `HttpsSingleMessage`.]
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_008: [If send failed to parse the message, it shall bypass the exception.]
        HttpsMessage httpsMessage = HttpsSingleMessage.parseHttpsJsonMessage(message);

        if((message.getIotHubMethod() == null) || (message.getUriPath() == null))
        {
            throw new IllegalArgumentException("method or path is null");
        }

        HttpsMethod httpsMethod;
        switch (message.getIotHubMethod())
        {
            case GET:
                //Codes_SRS_HTTPSTRANSPORTMANAGER_21_009: [If the IotHubMethod is `GET`, the send shall set the httpsMethod as `GET`.]
                httpsMethod = HttpsMethod.GET;
                break;
            case POST:
                //Codes_SRS_HTTPSTRANSPORTMANAGER_21_010: [If the IotHubMethod is `POST`, the send shall set the httpsMethod as `POST`.]
                httpsMethod = HttpsMethod.POST;
                break;
            default:
                //Codes_SRS_HTTPSTRANSPORTMANAGER_21_011: [If the IotHubMethod is not `GET` or `POST`, the send shall throws IllegalArgumentException.]
                throw new IllegalArgumentException("Unknown IoT Hub type " + message.getIotHubMethod().toString());
        }

        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_012: [The send shall set the httpsPath with the uriPath in the message.]
        String httpsPath = message.getUriPath();

        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_013: [The send shall call `sendHttpsMessage` from `HttpsIotHubConnection` to send the message.]
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_014: [If `sendHttpsMessage` failed, the send shall bypass the exception.]
        try
        {
            return this.httpsIotHubConnection.sendHttpsMessage(httpsMessage, httpsMethod, httpsPath);
        }
        catch (TransportException e)
        {
            //Wrapping this as IOException to avoid breaking changes.
            throw new IOException(e);
        }
    }

    /**
     * Pull the IotHub looking for new message.
     * @return New message from the IotHub. It can be {@code null} is there is no new message to read.
     * @throws IOException if the IotHub communication failed.
     */
    public Message receive() throws IOException
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_015: [The receive shall receive and bypass message from `HttpsIotHubConnection`, by calling `receiveMessage`.]
        //Codes_SRS_HTTPSTRANSPORTMANAGER_21_016: [If `receiveMessage` failed, the receive shall bypass the exception.]
        try
        {
            return this.httpsIotHubConnection.receiveMessage();
        }
        catch (TransportException e)
        {
            //Wrapping this as IOException to avoid breaking changes.
            throw new IOException(e);
        }
    }
}
