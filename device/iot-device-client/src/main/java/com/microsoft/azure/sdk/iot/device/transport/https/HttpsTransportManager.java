// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.edge.MethodRequest;
import com.microsoft.azure.sdk.iot.device.edge.MethodResult;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.net.IotHubUri;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportManager;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the transport manager for https.
 */
public class HttpsTransportManager implements IotHubTransportManager
{
    DeviceClientConfig config;
    HttpsIotHubConnection httpsIotHubConnection;

    private static final String MODULE_ID = "x-ms-edge-moduleId";
    private final static String ModuleMethodUriFormat = "/twins/%s/modules/%s/methods";
    private final static String DeviceMethodUriFormat = "/twins/%s/methods";

    private static final String PATH_NOTIFICATIONS_STRING = "/files/notifications";
    private static final String PATH_FILES_STRING = "/files";

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

    public ResponseMessage sendFileUploadMessage(IotHubTransportMessage message) throws IOException
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_028 [This function shall set the uri path of the provided message to the
        // format devices/<deviceid>/modules/<moduleid>/files if a moduleId is present or
        // devices/<deviceid>/modules/<moduleid>/files otherwise, and then send it.]
        String uri = new IotHubUri("", this.config.getDeviceId(), PATH_FILES_STRING, this.config.getModuleId()).toStringWithoutApiVersion();
        message.setUriPath(uri);
        return this.send(message, new HashMap<String, String>());
    }

    public ResponseMessage sendFileUploadNotification(IotHubTransportMessage message) throws IOException
    {
        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_029 [This function shall set the uri path of the provided message to the
        // format devices/<deviceid>/modules/<moduleid>/files/notifications if a moduleId is present or
        // devices/<deviceid>/modules/<moduleid>/files/notifications otherwise, and then send it.]
        String uri = new IotHubUri("", this.config.getDeviceId(), PATH_NOTIFICATIONS_STRING, this.config.getModuleId()).toStringWithoutApiVersion();
        message.setUriPath(uri);
        return this.send(message, new HashMap<String, String>());
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
    public ResponseMessage send(IotHubTransportMessage message, Map<String, String> additionalHeaders) throws IOException, IllegalArgumentException
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
            return this.httpsIotHubConnection.sendHttpsMessage(httpsMessage, httpsMethod, httpsPath, additionalHeaders);
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

    /**
     * Invoke a direct method to the provided uri
     * @param methodRequest the method request to make
     * @param deviceId the device id of the device the moduleId belongs to
     * @param moduleId the module id of the module to invoke the method on
     * @return the result of that request
     * @throws IOException if the IotHub cannot be reached
     */
    public MethodResult invokeMethod(MethodRequest methodRequest, String deviceId, String moduleId) throws IOException, URISyntaxException, TransportException
    {
        URI uri;
        if (moduleId == null || moduleId.isEmpty())
        {
            //Codes_SRS_HTTPSTRANSPORTMANAGER_34_017: [If a moduleId is not provided, this function shall call invokeMethod with the provided request and
            // a uri in the format twins/<device id>/methods?api-version=<api_version>.]
            uri = getDeviceMethodUri(deviceId);
        }
        else
        {
            //Codes_SRS_HTTPSTRANSPORTMANAGER_34_018: [If a moduleId is provided, this function shall call invokeMethod with the provided request and
            // a uri in the format twins/<device id>/modules/<module id>/methods?api-version=<api_version>.]
            uri = getModuleMethodUri(deviceId, moduleId);
        }

        return this.invokeMethod(methodRequest, uri);
    }

    /**
     * Invoke a direct method to the provided uri
     * @param methodRequest the method request to make
     * @param uri the path to send the request to
     * @return the result of that request
     * @throws IOException if the IotHub cannot be reached
     */
    private MethodResult invokeMethod(MethodRequest methodRequest, URI uri) throws IOException, TransportException
    {
        if (methodRequest == null)
        {
            //Codes_SRS_HTTPSTRANSPORTMANAGER_34_019: [If the provided method request is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("direct method request cannot be null");
        }

        if (uri == null || uri.toString().isEmpty())
        {
            //Codes_SRS_HTTPSTRANSPORTMANAGER_34_020: [If the provided uri is null or empty, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("uri cannot be null or be an empty path");
        }

        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_021: [This function shall set the methodrequest json as the body of the http message.]
        IotHubTransportMessage message = new IotHubTransportMessage(methodRequest.toJson());

        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_022: [This function shall set the http method to POST.]
        message.setIotHubMethod(IotHubMethod.POST);

        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_023: [This function shall set the http message's uri path to the provided uri path.]
        message.setUriPath(uri.toString());

        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_024 [This function shall set a custom property of 'x-ms-edge-moduleId' to the value of <device id>/<module id> of the sending module/device.]
        Map<String, String> additionalHeaders = new HashMap<>();
        additionalHeaders.put(MODULE_ID, this.config.getDeviceId() + "/" + this.config.getModuleId());

        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_025 [This function shall send the built message.]
        ResponseMessage responseMessage = this.send(message, additionalHeaders);

        if (responseMessage.getStatus() != IotHubStatusCode.OK && responseMessage.getStatus() != IotHubStatusCode.OK_EMPTY)
        {
            //Codes_SRS_HTTPSTRANSPORTMANAGER_34_026 [If the http response contains an error code, this function shall throw the associated exception.]
            IotHubServiceException exception = IotHubStatusCode.getConnectionStatusException(responseMessage.getStatus(), new String(responseMessage.getBytes()));
            throw exception;
        }

        //Codes_SRS_HTTPSTRANSPORTMANAGER_34_027 [If the http response doesn't contain an error code, this function return a method result with the response message body as the method result body.]
        String resultJson = new String(responseMessage.getBytes());
        return new MethodResult(resultJson);
    }

    private static URI getDeviceMethodUri(String deviceId) throws UnsupportedEncodingException, URISyntaxException
    {
        deviceId = URLEncoder.encode(deviceId, "UTF-8");
        return new URI(String.format(DeviceMethodUriFormat, deviceId));
    }

    private static URI getModuleMethodUri(String deviceId, String moduleId) throws UnsupportedEncodingException, URISyntaxException
    {
        deviceId = URLEncoder.encode(deviceId, "UTF-8");
        moduleId = URLEncoder.encode(moduleId, "UTF-8");
        return new URI(String.format(ModuleMethodUriFormat, deviceId, moduleId));
    }
}
