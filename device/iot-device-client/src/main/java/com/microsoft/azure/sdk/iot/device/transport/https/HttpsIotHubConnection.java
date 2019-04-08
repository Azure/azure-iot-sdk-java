// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.exceptions.IotHubServiceException;
import com.microsoft.azure.sdk.iot.device.exceptions.TransportException;
import com.microsoft.azure.sdk.iot.device.net.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubListener;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportConnection;
import com.microsoft.azure.sdk.iot.device.transport.IotHubTransportMessage;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An HTTPS connection between a device and an IoT Hub. Contains functionality
 * for synchronously connecting to the different IoT Hub resource URIs.
 */
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
    private final DeviceClientConfig config;

    /**
     * Message e-tag is obtained when the device receives a
     * message and used when sending a message result back to
     * the IoT Hub.
     */
    private Map<Message, String> messageToETagMap = new HashMap<>();

    /**
     * Constructs an instance from the given {@link DeviceClientConfig}
     * object.
     *
     * @param config the client configuration.
     */
    public HttpsIotHubConnection(DeviceClientConfig config)
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_001: [The constructor shall save the client configuration.]
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
            HttpsMessage httpsMessage = HttpsSingleMessage.parseHttpsMessage(message);

            String iotHubHostname = getHostName();
            String deviceId = this.config.getDeviceId();
            String moduleId = this.config.getModuleId();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_002: [The function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/events?api-version=2016-02-03'.]
            IotHubEventUri iotHubEventUri = new IotHubEventUri(iotHubHostname, deviceId, moduleId);

            URL eventUrl = this.buildUrlFromString(HTTPS_HEAD_TAG + iotHubEventUri.toString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_003: [The function shall send a POST request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_004: [The function shall set the request body to the message body.]
            HttpsRequest request = new HttpsRequest(eventUrl, HttpsMethod.POST, httpsMessage.getBody(), this.config.getProductInfo().getUserAgentString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_005: [The function shall write each message property as a request header.]
            for (MessageProperty property : httpsMessage.getProperties())
            {
                request.setHeaderField(property.getName(),
                        property.getValue());
            }

            if (message.getContentEncoding() != null)
            {
                // Codes_SRS_HTTPSIOTHUBCONNECTION_34_073: [If the provided message has a content encoding, this function shall set the request header to include that value with the key "iothub-contentencoding".]
                request.setHeaderField(MessageProperty.IOTHUB_CONTENT_ENCODING, message.getContentEncoding());
            }

            if (message.getContentType() != null)
            {
                // Codes_SRS_HTTPSIOTHUBCONNECTION_34_074: [If the provided message has a content type, this function shall set the request header to include that value with the key "iothub-contenttype".]
                request.setHeaderField(MessageProperty.IOTHUB_CONTENT_TYPE, message.getContentType());
            }

            if (message.getCreationTimeUTC() != null)
            {
                // Codes_SRS_HTTPSIOTHUBCONNECTION_34_075: [If the provided message has a creation time utc, this function shall set the request header to include that value with the key "iothub-contenttype".]
                request.setHeaderField(MessageProperty.IOTHUB_CREATION_TIME_UTC, message.getCreationTimeUTCString());
            }

            Map<String, String> systemProperties = httpsMessage.getSystemProperties();
            for (String systemProperty : systemProperties.keySet())
            {
                request.setHeaderField(systemProperty, systemProperties.get(systemProperty));
            }

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_008: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/events'.]
            request.setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, iotHubEventUri.getPath())
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_009: [The function shall set the header field 'content-type' to be the message content type.]
                    .setHeaderField(HTTPS_PROPERTY_CONTENT_TYPE_TAG, httpsMessage.getContentType());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_25_040: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_007: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_34_059: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_006: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            HttpsResponse response = this.sendRequest(request);

            IotHubStatusCode status = IotHubStatusCode.getIotHubStatusCode(response.getStatus());

            IotHubTransportMessage transportMessage = new IotHubTransportMessage(message.getBytes(), message.getMessageType(), message.getMessageId(), message.getCorrelationId(), message.getProperties());
            if (status == IotHubStatusCode.OK || status == IotHubStatusCode.OK_EMPTY)
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_067: [If the response from the service is OK or OK_EMPTY, this function shall notify its listener that a message was sent with no exception.]
                this.listener.onMessageSent(transportMessage, null);
            }
            else
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_068: [If the response from the service not OK or OK_EMPTY, this function shall notify its listener that a message was with the mapped IotHubServiceException.]
                this.listener.onMessageSent(transportMessage, IotHubStatusCode.getConnectionStatusException(status, ""));
            }

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
    public ResponseMessage sendHttpsMessage(HttpsMessage httpsMessage, HttpsMethod httpsMethod, String httpsPath, Map<String, String> additionalHeaders) throws TransportException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            String iotHubHostname = getHostName();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_041: [The function shall send a request to the URL https://[iotHubHostname]/[httpsPath]?api-version=2016-02-03.]
            URL messageUrl = this.buildUrlFromString(HTTPS_HEAD_TAG + iotHubHostname + httpsPath + "?" + IotHubUri.API_VERSION);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_042: [The function shall send a `httpsMethod` request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_043: [The function shall set the request body to the message body.]
            HttpsRequest request = new HttpsRequest(messageUrl, httpsMethod, httpsMessage.getBody(), this.config.getProductInfo().getUserAgentString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_044: [The function shall write each message property as a request header.]
            for (MessageProperty property : httpsMessage.getProperties())
            {
                request.setHeaderField(property.getName(), property.getValue());
            }

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_048: [The function shall set the header field 'iothub-to' to be '[https path]'.]
            request.setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, httpsPath)
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_21_049: [The function shall set the header field 'content-type' to be the message content type.]
                    .setHeaderField(HTTPS_PROPERTY_CONTENT_TYPE_TAG, httpsMessage.getContentType());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_34_072: [The function shall set the additional header fields provided.]
            for (String additionalHeaderKey : additionalHeaders.keySet())
            {
                request.setHeaderField(additionalHeaderKey, additionalHeaders.get(additionalHeaderKey));
            }

            // Codes_SRS_HTTPSIOTHUBCONNECTION_34_056: [This function shall retrieve a sas token from its config to use in the https request header.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_047: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_34_060: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_046: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_045: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_051: [If the IoT Hub could not be reached, the function shall throw a ProtocolException.]
            HttpsResponse response = this.sendRequest(request);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_050: [The function shall return a ResponseMessage with the status and payload.]
            IotHubStatusCode status = IotHubStatusCode.getIotHubStatusCode(response.getStatus());
            byte[] body = response.getBody();

            return new ResponseMessage(body, status);
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
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_013: [The function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound?api-version=2016-02-03'.]
            IotHubMessageUri messageUri = new IotHubMessageUri(getHostName(), this.config.getDeviceId(), this.config.getModuleId());
            URL messageUrl = this.buildUrlFromString(HTTPS_HEAD_TAG + messageUri.toString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_014: [The function shall send a GET request.]
            HttpsRequest request =
                    new HttpsRequest(messageUrl, HttpsMethod.GET, new byte[0], this.config.getProductInfo().getUserAgentString()).
                            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_017: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound'.]
                                    setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG,
                                    messageUri.getPath()).
                            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_018: [The function shall set the header field 'iothub-messagelocktimeout' to be the configuration parameter messageLockTimeoutSecs.]
                                    setHeaderField(HTTPS_PROPERTY_IOTHUB_MESSAGELOCKTIMEOUT_TAG,
                                    Integer.toString(this.config.getMessageLockTimeoutSecs()));

            // Codes_SRS_HTTPSIOTHUBCONNECTION_34_057: [This function shall retrieve a sas token from its config to use in the https request header.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_016: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_34_061: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_25_041: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_015: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_023: [If the IoT Hub could not be reached, the function shall throw a TransportException.]
            HttpsResponse response = this.sendRequest(request);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_021: [If a response with IoT Hub status code OK is not received, the function shall return null.]
            IotHubTransportMessage transportMessage = null;
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_019: [If a response with IoT Hub status code OK is received, the function shall return the IoT Hub message included in the response.]
            IotHubStatusCode messageStatus = IotHubStatusCode.getIotHubStatusCode(response.getStatus());
            if (messageStatus == IotHubStatusCode.OK)
            {
                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_020: [If a response with IoT Hub status code OK is received, the function shall save the response header field 'etag'.]
                String messageEtag = sanitizeEtag(response.getHeaderField(HTTPS_PROPERTY_ETAG_TAG));

                HttpsSingleMessage httpsMsg = HttpsSingleMessage.parseHttpsMessage(response);
                Message message = httpsMsg.toMessage();

                //callbacks are always for telemetry as HTTPS does not support Twin or Methods
                transportMessage = new IotHubTransportMessage(message.getBytes(), message.getMessageType(), message.getMessageId(), message.getCorrelationId(), message.getProperties());
                transportMessage.setMessageCallback(this.config.getDeviceTelemetryMessageCallback(message.getInputName()));
                transportMessage.setMessageCallbackContext(this.config.getDeviceTelemetryMessageContext(message.getInputName()));

                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_070: [If the message status was OK this function shall save the received message and its eTag into its map.]
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
    public void open(Queue<DeviceClientConfig> deviceClientConfigs, ScheduledExecutorService scheduledExecutorService)
    {

    }

    @Override
    public void setListener(IotHubListener listener)
    {
        if (listener == null)
        {
            //Codes_SRS_HTTPSIOTHUBCONNECTION_34_065: [If the provided listener object is null, this function shall throw an IllegalArgumentException.]
            throw new IllegalArgumentException("Listener cannot be null");
        }

        //Codes_SRS_HTTPSIOTHUBCONNECTION_34_066: [This function shall save the provided listener object.]
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
    public boolean sendMessageResult(Message message, IotHubMessageResult result) throws TransportException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            String messageEtag = this.messageToETagMap.get(message);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_039: [If the function is called before receiveMessage() returns a message, the function shall throw an IllegalStateException.]
            if (messageEtag == null)
            {
                throw new IllegalStateException("Cannot send a message "
                        + "result before a message is received or if the result was already sent");
            }

            String iotHubHostname = getHostName();
            String deviceId = this.config.getDeviceId();

            String resultUri = HTTPS_HEAD_TAG;
            String resultPath;
            URL resultUrl;
            HttpsRequest request;
            switch (result)
            {
                case COMPLETE:
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_024: [If the result is COMPLETE, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]?api-version=2016-02-03'.]
                    IotHubCompleteUri completeUri =
                            new IotHubCompleteUri(iotHubHostname, deviceId, messageEtag, this.config.getModuleId());
                    resultUri += completeUri.toString();
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_026: [If the result is COMPLETE, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.]
                    resultPath = completeUri.getPath();
                    resultUrl = this.buildUrlFromString(resultUri);
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_025: [If the result is COMPLETE, the function shall send a DELETE request.]
                    request = new HttpsRequest(resultUrl, HttpsMethod.DELETE, new byte[0], this.config.getProductInfo().getUserAgentString());
                    break;
                case ABANDON:
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_027: [If the result is ABANDON, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]/abandon?api-version=2016-02-03'.]
                    IotHubAbandonUri abandonUri =
                            new IotHubAbandonUri(iotHubHostname, deviceId, messageEtag, this.config.getModuleId());
                    resultUri += abandonUri.toString();
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_029: [If the result is ABANDON, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]/abandon'.]
                    resultPath = abandonUri.getPath();
                    resultUrl = this.buildUrlFromString(resultUri);
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_028: [If the result is ABANDON, the function shall send a POST request.]
                    // The IoT Hub service requires the content-length header to be
                    // set but the Java SE connection omits content-length
                    // if content-length == 0. We include a placeholder body to
                    // make the connection include a content-length.
                    request = new HttpsRequest(resultUrl, HttpsMethod.POST, new byte[1], this.config.getProductInfo().getUserAgentString());
                    break;
                case REJECT:
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_030: [If the result is REJECT, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]??reject=true&api-version=2016-02-03' (the query parameters can be in any order).]
                    IotHubRejectUri rejectUri =
                            new IotHubRejectUri(iotHubHostname, deviceId, messageEtag, this.config.getModuleId());
                    resultUri += rejectUri.toString();
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_032: [If the result is REJECT, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.]
                    resultPath = rejectUri.getPath();
                    resultUrl = this.buildUrlFromString(resultUri);
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_031: [If the result is REJECT, the function shall send a DELETE request.]
                    request = new HttpsRequest(resultUrl, HttpsMethod.DELETE, new byte[0], this.config.getProductInfo().getUserAgentString());
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException(
                            "Invalid message result specified.");
            }

            request.setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, resultPath).
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_035: [The function shall set the header field 'if-match' to be the e-tag saved when receiveMessage() was previously called.]
                            setHeaderField(HTTPS_PROPERTY_IF_MATCH_TAG, messageEtag);

            //Codes_SRS_HTTPSIOTHUBCONNECTION_34_062: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
            //Codes_SRS_HTTPSIOTHUBCONNECTION_25_042: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_034: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_033: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_037: [If the IoT Hub could not be reached, the function shall throw a ProtocolException.]
            HttpsResponse response = this.sendRequest(request);

            IotHubStatusCode resultStatus = IotHubStatusCode.getIotHubStatusCode(response.getStatus());

            if (resultStatus != IotHubStatusCode.OK_EMPTY && resultStatus != IotHubStatusCode.OK)
            {
                String errMsg = String.format(
                        "Sending message result failed with status %s.%n",
                        resultStatus.name());

                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_038: [If the IoT Hub status code in the response is not OK_EMPTY, the function shall throw an IotHubServiceException.]
                throw new IotHubServiceException(errMsg);
            }
            else
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_069: [If the IoT Hub status code in the response is OK_EMPTY or OK, the function shall remove the sent eTag from its map and return true.]
                this.messageToETagMap.remove(message);
                return true;
            }
        }
    }

    @Override
    public String getConnectionId()
    {
        //Codes_SRS_HTTPSIOTHUBCONNECTION_34_071: [This function shall return the empty string.]
        return "";
    }

    private HttpsResponse sendRequest(HttpsRequest request) throws TransportException
    {
        request.setReadTimeoutMillis(this.config.getReadTimeoutMillis());

        if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
        {
            request.setHeaderField(HTTPS_PROPERTY_AUTHORIZATION_TAG, this.getSasToken());
        }

        request.setSSLContext(this.getSSLContext());

        HttpsResponse response = request.send();
        return response;
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

    private String getSasToken() throws TransportException
    {
        try
        {
            return this.config.getSasTokenAuthentication().getRenewedSasToken(false, false);
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
            throw new TransportException(e);
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
