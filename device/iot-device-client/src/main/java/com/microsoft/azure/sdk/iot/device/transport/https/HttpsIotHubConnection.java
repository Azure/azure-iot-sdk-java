// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.transport.TransportException;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportConnection;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An HTTPS connection between a device and an IoT Hub. Contains functionality
 * for synchronously connecting to the different IoT Hub resource URIs.
 */
@Slf4j
public class HttpsIotHubConnection implements IotHubTransportConnection
{
    private static final String HTTPS_HEAD_TAG = "https://";
    private static final String HTTPS_PROPERTY_AUTHORIZATION_TAG = "authorization";
    private static final String HTTPS_PROPERTY_IOTHUB_TO_TAG = "iothub-to";
    private static final String HTTPS_PROPERTY_CONTENT_TYPE_TAG = "content-type";
    private static final String HTTPS_PROPERTY_IOTHUB_MESSAGELOCKTIMEOUT_TAG = "iothub-messagelocktimeout";
    private static final String HTTPS_PROPERTY_IF_MATCH_TAG = "if-match";
    private static final String HTTPS_PROPERTY_ETAG_TAG = "etag";

    private IotHubListener listener;

    /** The HTTPS connection lock. */
    private final Object HTTPS_CONNECTION_LOCK = new Object();

    /** The client configuration. */
    private final ClientConfiguration config;

    /**
     * Message e-tag is obtained when the device receives a
     * message and used when sending a message result back to
     * the IoT Hub.
     */
    private final Map<Message, String> messageToETagMap = new HashMap<>();

    /**
     * Constructs an instance from the given {@link ClientConfiguration}
     * object.
     *
     * @param config the client configuration.
     */
    public HttpsIotHubConnection(ClientConfiguration config)
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            this.config = config;
        }
    }

    /**
     * Sends an event message.
     *
     * @param message the event message.
     *
     * @return the IotHubStatusCode from sending the event message.
     *
     * @throws TransportException if the IoT Hub could not be reached.
     */
    public IotHubStatusCode sendMessage(Message message) throws TransportException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            // Here we check if it's a bulk message and serialize it.
            HttpsMessage httpsMessage;

            if (message instanceof BatchMessage)
            {
                try
                {
                    List<HttpsSingleMessage> httpsMessageList = new ArrayList<>();
                    for (Message msg : ((BatchMessage)message).getNestedMessages())
                    {
                        httpsMessageList.add(HttpsSingleMessage.parseHttpsMessage(msg));
                    }
                    httpsMessage = new HttpsBatchMessage(httpsMessageList);
                }
                catch (IllegalArgumentException e)
                {
                    throw new TransportException("Failed to create HTTPS batch message", e);
                }
            }
            else
            {
                httpsMessage = HttpsSingleMessage.parseHttpsMessage(message);
            }

            String iotHubHostname = getHostName();
            String deviceId = this.config.getDeviceId();
            String moduleId = this.config.getModuleId();

            IotHubEventUri iotHubEventUri = new IotHubEventUri(iotHubHostname, deviceId, moduleId);

            URL eventUrl = this.buildUrlFromString(HTTPS_HEAD_TAG + iotHubEventUri.toString());

            HttpsRequest request = new HttpsRequest(eventUrl, HttpsMethod.POST, httpsMessage.getBody(), this.config.getProductInfo().getUserAgentString(), config.getProxySettings());

            for (MessageProperty property : httpsMessage.getProperties())
            {
                request.setHeaderField(property.getName(),
                        property.getValue());
            }

            if (message.getContentEncoding() != null)
            {
                request.setHeaderField(MessageProperty.IOTHUB_CONTENT_ENCODING, message.getContentEncoding());
            }

            if (message.getContentType() != null)
            {
                request.setHeaderField(MessageProperty.IOTHUB_CONTENT_TYPE, message.getContentType());
            }

            if (message.getCreationTimeUTC() != null)
            {
                request.setHeaderField(MessageProperty.IOTHUB_CREATION_TIME_UTC, message.getCreationTimeUTCString());
            }

            if (message.isSecurityMessage())
            {
                request.setHeaderField(MessageProperty.IOTHUB_SECURITY_INTERFACE_ID, MessageProperty.IOTHUB_SECURITY_INTERFACE_ID_VALUE);
            }

            Map<String, String> systemProperties = httpsMessage.getSystemProperties();
            for (String systemProperty : systemProperties.keySet())
            {
                request.setHeaderField(systemProperty, systemProperties.get(systemProperty));
            }

            request.setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, iotHubEventUri.getPath())
                    .setHeaderField(HTTPS_PROPERTY_CONTENT_TYPE_TAG, httpsMessage.getContentType());

            log.trace("Sending message using http request ({})", message);
            HttpsResponse response = this.sendRequest(request);
            IotHubStatusCode status = IotHubStatusCode.getIotHubStatusCode(response.getStatus());
            log.trace("Iot Hub responded to http message for iot hub message ({}) with status code {}", message, status);

            IotHubTransportMessage transportMessage = new IotHubTransportMessage(httpsMessage.getBody(), message.getMessageType(), message.getMessageId(), message.getCorrelationId(), message.getProperties());
            if (status == IotHubStatusCode.OK)
            {
                this.listener.onMessageSent(transportMessage, this.config.getDeviceId(), null);
            }

            // Status codes other than 200 and 204 have their errors handled in the IotHubTransport layer once this method returns,
            // so there is no need to call "this.listener.onMessageSent(transportMessage, someException)" from this layer.

            return status;
        }
    }

    /**
     * Sends an generic https message.
     *
     * @param httpsMessage the message to send.
     * @param httpsMethod the https method (GET, POST, PUT, DELETE).
     * @param httpsPath the path that will be added at the end of the URI with `/`.
     * @param additionalHeaders any extra headers to be included in the http request
     * @return the ResponseMessage including status code and payload from sending message.
     *
     * @throws TransportException if the IoT Hub could not be reached.
     */
    public HttpsResponse sendHttpsMessage(HttpsMessage httpsMessage, HttpsMethod httpsMethod, String httpsPath, Map<String, String> additionalHeaders) throws TransportException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            String iotHubHostname = getHostName();

            URL messageUrl = this.buildUrlFromString(HTTPS_HEAD_TAG + iotHubHostname + httpsPath + "?" + IotHubUri.API_VERSION);

            HttpsRequest request = new HttpsRequest(messageUrl, httpsMethod, httpsMessage.getBody(), this.config.getProductInfo().getUserAgentString(), config.getProxySettings());

            for (MessageProperty property : httpsMessage.getProperties())
            {
                request.setHeaderField(property.getName(), property.getValue());
            }

            request.setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, httpsPath)
                    .setHeaderField(HTTPS_PROPERTY_CONTENT_TYPE_TAG, httpsMessage.getContentType());

            for (String additionalHeaderKey : additionalHeaders.keySet())
            {
                request.setHeaderField(additionalHeaderKey, additionalHeaders.get(additionalHeaderKey));
            }

            HttpsResponse response = this.sendRequest(request);
            byte[] body = response.getBody();
            return new HttpsResponse(response.getStatus(), body, new HashMap<String, List<String>>(), new byte[0]);
        }
    }

    /**
     * Receives an IotHubTransportMessage, if one exists.
     *
     * @return an IotHubTransportMessage, or null if none exists.
     *
     * @throws TransportException if the IoT Hub could not be reached.
     */
    public IotHubTransportMessage receiveMessage() throws TransportException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            IotHubMessageUri messageUri = new IotHubMessageUri(getHostName(), this.config.getDeviceId(), this.config.getModuleId());
            URL messageUrl = this.buildUrlFromString(HTTPS_HEAD_TAG + messageUri.toString());

            HttpsRequest request =
                    new HttpsRequest(messageUrl, HttpsMethod.GET, new byte[0], this.config.getProductInfo().getUserAgentString(), config.getProxySettings()).
                                    setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG,
                                    messageUri.getPath()).
                                    setHeaderField(HTTPS_PROPERTY_IOTHUB_MESSAGELOCKTIMEOUT_TAG,
                                    Integer.toString(this.config.getMessageLockTimeoutSecs()));

            log.trace("Sending http request to check if any messages are ready to be received...");
            HttpsResponse response = this.sendRequest(request);

            IotHubTransportMessage transportMessage = null;
            if (response.getStatus() == 200)
            {
                String messageEtag = sanitizeEtag(response.getHeaderField(HTTPS_PROPERTY_ETAG_TAG));

                HttpsSingleMessage httpsMsg = HttpsSingleMessage.parseHttpsMessage(response);
                Message message = httpsMsg.toMessage();

                //callbacks are always for telemetry as HTTPS does not support Twin or Methods
                transportMessage = new IotHubTransportMessage(message.getBytes(), message.getMessageType(), message.getMessageId(), message.getCorrelationId(), message.getProperties());
                transportMessage.setMessageCallback(this.config.getDeviceTelemetryMessageCallback(message.getInputName()));
                transportMessage.setMessageCallbackContext(this.config.getDeviceTelemetryMessageContext(message.getInputName()));

                log.trace("Received http message with etag {} in transport message ({})", messageEtag, transportMessage);

                this.messageToETagMap.put(transportMessage, messageEtag);
            }

            return transportMessage;
        }
    }

    /**
     * Removes double quotes from the e-tag property.
     *
     * @param dirtyEtag the dirty e-tag value.
     *
     * @return the e-tag value with double quotes removed.
     */
    private static String sanitizeEtag(String dirtyEtag)
    {
        return dirtyEtag.replace("\"", "");
    }

    @Override
    public void open()
    {

    }

    @Override
    public void setListener(IotHubListener listener)
    {
        if (listener == null)
        {
            throw new IllegalArgumentException("Listener cannot be null");
        }

        this.listener = listener;
    }

    @Override
    public void close()
    {
        //Dummy call
    }

    /**
     * Sends the message result for the previously received
     * message.
     *
     * @param message the message that was received from the service to send the result of
     * @param result the message result (one of {@link IotHubMessageResult#COMPLETE},
     *               {@link IotHubMessageResult#ABANDON}, or {@link IotHubMessageResult#REJECT}).
     *
     * @throws TransportException if {@code sendMessageResult} is called before
     * {@link #receiveMessage()} is called.
     * @throws TransportException if the IoT Hub could not be reached.
     */

    @Override
    public boolean sendMessageResult(IotHubTransportMessage message, IotHubMessageResult result) throws TransportException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            log.trace("Checking if http layer can correlate the received iot hub message to a received etag {}", message);
            String messageEtag = this.messageToETagMap.get(message);

            if (messageEtag == null)
            {
                throw new IllegalStateException("Cannot send a message "
                        + "result before a message is received or if the result was already sent");
            }

            log.trace("Http layer correlated the received iot hub message ({}) to etag {}", message, messageEtag);
            log.trace("Sending ACK with result {} for etag {}", result, messageEtag);

            String iotHubHostname = getHostName();
            String deviceId = this.config.getDeviceId();

            String resultUri = HTTPS_HEAD_TAG;
            String resultPath;
            URL resultUrl;
            HttpsRequest request;
            switch (result)
            {
                case COMPLETE:
                    IotHubCompleteUri completeUri =
                            new IotHubCompleteUri(iotHubHostname, deviceId, messageEtag, this.config.getModuleId());
                    resultUri += completeUri.toString();
                    resultPath = completeUri.getPath();
                    resultUrl = this.buildUrlFromString(resultUri);
                    request = new HttpsRequest(resultUrl, HttpsMethod.DELETE, new byte[0], this.config.getProductInfo().getUserAgentString(), config.getProxySettings());
                    break;
                case ABANDON:
                    IotHubAbandonUri abandonUri =
                            new IotHubAbandonUri(iotHubHostname, deviceId, messageEtag, this.config.getModuleId());
                    resultUri += abandonUri.toString();
                    resultPath = abandonUri.getPath();
                    resultUrl = this.buildUrlFromString(resultUri);
                    // The IoT Hub service requires the content-length header to be
                    // set but the Java SE connection omits content-length
                    // if content-length == 0. We include a placeholder body to
                    // make the connection include a content-length.
                    request = new HttpsRequest(resultUrl, HttpsMethod.POST, new byte[1], this.config.getProductInfo().getUserAgentString(), config.getProxySettings());
                    break;
                case REJECT:
                    IotHubRejectUri rejectUri =
                            new IotHubRejectUri(iotHubHostname, deviceId, messageEtag, this.config.getModuleId());
                    resultUri += rejectUri.toString();
                    resultPath = rejectUri.getPath();
                    resultUrl = this.buildUrlFromString(resultUri);
                    request = new HttpsRequest(resultUrl, HttpsMethod.DELETE, new byte[0], this.config.getProductInfo().getUserAgentString(), config.getProxySettings());
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException(
                            "Invalid message result specified.");
            }

            request.setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, resultPath).
                            setHeaderField(HTTPS_PROPERTY_IF_MATCH_TAG, messageEtag);

            HttpsResponse response = this.sendRequest(request);

            IotHubStatusCode resultStatus = IotHubStatusCode.getIotHubStatusCode(response.getStatus());

            if (resultStatus != IotHubStatusCode.OK)
            {
                String errMsg = String.format(
                        "Sending message result failed with status %s.%n",
                        resultStatus.name());

                throw new IotHubServiceException(errMsg);
            }
            else
            {
                log.trace("Successfully sent ack for http message with etag {}. Removing it from saved list of outstanding messages to acknowledge", messageEtag);
                this.messageToETagMap.remove(message);
                return true;
            }
        }
    }

    @Override
    public String getConnectionId()
    {
        return "";
    }

    private HttpsResponse sendRequest(HttpsRequest request) throws TransportException
    {
        request.setReadTimeout(this.config.getHttpsReadTimeout());
        request.setConnectTimeout(this.config.getHttpsConnectTimeout());

        if (this.config.getAuthenticationType() == ClientConfiguration.AuthType.SAS_TOKEN)
        {
            request.setHeaderField(HTTPS_PROPERTY_AUTHORIZATION_TAG, this.getSasToken());
        }

        request.setSSLContext(this.getSSLContext());

        return request.send();
    }

    private URL buildUrlFromString(String url) throws TransportException
    {
        try
        {
            return new URL(url);
        }
        catch (MalformedURLException e)
        {
            throw new TransportException("Could not build HTTP url", e);
        }
    }

    // The warning is for how getSasTokenAuthentication() may return null, the fact that this method is only called
    // when using SAS based authentication is sufficient at confirming that getSasTokenAuthentication() will return a non-null instance
    @SuppressWarnings("ConstantConditions")
    private String getSasToken() throws TransportException
    {
        try
        {
            return String.valueOf(this.config.getSasTokenAuthentication().getSasToken());
        }
        catch (IOException e)
        {
            throw new TransportException(e);
        }
    }

    private SSLContext getSSLContext() throws TransportException
    {
        try
        {
            return this.config.getAuthenticationProvider().getSSLContext();
        }
        catch (IOException e)
        {
            throw new TransportException("Failed to get SSLContext", e);
        }
    }

    private String getHostName()
    {
        String hostname = this.config.getGatewayHostname();
        if (hostname == null || hostname.isEmpty())
        {
            hostname = this.config.getIotHubHostname();
        }

        return hostname;
    }
}
