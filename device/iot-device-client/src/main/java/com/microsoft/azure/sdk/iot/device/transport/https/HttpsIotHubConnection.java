// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.net.*;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * An HTTPS connection between a device and an IoT Hub. Contains functionality
 * for synchronously connecting to the different IoT Hub resource URIs.
 */
public class HttpsIotHubConnection
{
    private static final String HTTPS_HEAD_TAG = "https://";
    private static final String HTTPS_PROPERTY_AUTHORIZATION_TAG = "authorization";
    private static final String HTTPS_PROPERTY_IOTHUB_TO_TAG = "iothub-to";
    private static final String HTTPS_PROPERTY_CONTENT_TYPE_TAG = "content-type";
    private static final String HTTPS_PROPERTY_IOTHUB_MESSAGELOCKTIMEOUT_TAG = "iothub-messagelocktimeout";
    private static final String HTTPS_PROPERTY_IF_MATCH_TAG = "if-match";
    private static final String HTTPS_PROPERTY_ETAG_TAG = "etag";

    /** The HTTPS connection lock. */
    private final Object HTTPS_CONNECTION_LOCK = new Object();

    /** The client configuration. */
    private final DeviceClientConfig config;
    /**
     * The message e-tag. Obtained when the device receives a
     * message and used when sending a message result back to
     * the IoT Hub.
     */
    private String messageEtag;

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
     * @param msg the event message.
     *
     * @return the ResponseMessage including status code and payload from sending the event message.
     *
     * @throws IOException if the IoT Hub could not be reached.
     */
    public ResponseMessage sendEvent(HttpsMessage msg) throws IOException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            String iotHubHostname = this.config.getIotHubHostname();
            String deviceId = this.config.getDeviceId();
            int readTimeoutMillis = this.config.getReadTimeoutMillis();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_002: [The function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/events?api-version=2016-02-03'.]
            IotHubEventUri iotHubEventUri = new IotHubEventUri(iotHubHostname, deviceId);
            URL eventUrl = new URL(HTTPS_HEAD_TAG + iotHubEventUri.toString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_003: [The function shall send a POST request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_004: [The function shall set the request body to the message body.]
            HttpsRequest request =
                    new HttpsRequest(eventUrl, HttpsMethod.POST, msg.getBody());
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_005: [The function shall write each message property as a request header.]
            for (MessageProperty property : msg.getProperties())
            {
                request.setHeaderField(property.getName(),
                        property.getValue());
            }

            Map<String, String> systemProperties = msg.getSystemProperties();
            for (String systemProperty : systemProperties.keySet())
            {
                request.setHeaderField(systemProperty, systemProperties.get(systemProperty));
            }

            SSLContext sslContext = null;
            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                sslContext = this.config.getSasTokenAuthentication().getSSLContext();

                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_055: [This function shall retrieve a sas token from its config to use in the https request header.]
                if (this.config.getSasTokenAuthentication().isRenewalNecessary())
                {
                    //Codes_SRS_HTTPSIOTHUBCONNECTION_34_052: [If the SAS token used by this has expired, the function shall return a ResponseMessage object with the IotHubStatusCode UNAUTHORIZED.]
                    return new ResponseMessage("Your sas token has expired".getBytes(), IotHubStatusCode.UNAUTHORIZED);
                }

                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_007: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
                request.setHeaderField(HTTPS_PROPERTY_AUTHORIZATION_TAG, this.config.getSasTokenAuthentication().getRenewedSasToken());
            }
            else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_059: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
                sslContext = this.config.getX509Authentication().getSSLContext();
            }

            //Codes_SRS_HTTPSIOTHUBCONNECTION_25_040: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            request.setSSLContext(sslContext);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_006: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            request.setReadTimeoutMillis(readTimeoutMillis).
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_008: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/events'.]
                            setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, iotHubEventUri.getPath()).
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_009: [The function shall set the header field 'content-type' to be the message content type.]
                            setHeaderField(HTTPS_PROPERTY_CONTENT_TYPE_TAG, msg.getContentType());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_012: [If the IoT Hub could not be reached, the function shall throw an IOException.]
            HttpsResponse response = request.send();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_010: [The function shall return a ResponseMessage with the status and payload.]
            IotHubStatusCode status = IotHubStatusCode.getIotHubStatusCode(response.getStatus());
            byte[] body = response.getBody();

            return new ResponseMessage(body, status);
        }
    }

    /**
     * Sends an generic https message.
     *
     * @param httpsMessage the message to send.
     * @param httpsMethod the https method (GET, POST, PUT, DELETE).
     * @param httpsPath the path that will be added at the end of the URI with `/`.
     *
     * @return the ResponseMessage including status code and payload from sending message.
     *
     * @throws IOException if the IoT Hub could not be reached.
     */
    public ResponseMessage sendHttpsMessage(HttpsMessage httpsMessage, HttpsMethod httpsMethod, String httpsPath) throws IOException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            String iotHubHostname = this.config.getIotHubHostname();
            String deviceId = this.config.getDeviceId();
            int readTimeoutMillis = this.config.getReadTimeoutMillis();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_041: [The function shall send a request to the URL https://[iotHubHostname]/devices/[deviceId]/[path]?api-version=2016-02-03.]
            IotHubUri iotHubUri = new IotHubUri(iotHubHostname, deviceId, httpsPath);
            URL messageUrl = new URL(HTTPS_HEAD_TAG + iotHubUri.toString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_042: [The function shall send a `httpsMethod` request.]
            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_043: [The function shall set the request body to the message body.]
            HttpsRequest request = new HttpsRequest(messageUrl, httpsMethod, httpsMessage.getBody());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_044: [The function shall write each message property as a request header.]
            for (MessageProperty property : httpsMessage.getProperties())
            {
                request.setHeaderField(property.getName(),
                        property.getValue());
            }

            SSLContext sslContext = null;
            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                sslContext = this.config.getSasTokenAuthentication().getSSLContext();

                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_056: [This function shall retrieve a sas token from its config to use in the https request header.]
                if (this.config.getSasTokenAuthentication().isRenewalNecessary())
                {
                    //Codes_SRS_HTTPSIOTHUBCONNECTION_34_053: [If the SAS token used by this has expired, the function shall return a ResponseMessage object with the IotHubStatusCode UNAUTHORIZED.]
                    return new ResponseMessage("Your sas token has expired".getBytes(), IotHubStatusCode.UNAUTHORIZED);
                }

                // Codes_SRS_HTTPSIOTHUBCONNECTION_21_047: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
                request.setHeaderField(HTTPS_PROPERTY_AUTHORIZATION_TAG, this.config.getSasTokenAuthentication().getRenewedSasToken());
            }
            else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_060: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
                sslContext = this.config.getX509Authentication().getSSLContext();
            }

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_045: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            request.setReadTimeoutMillis(readTimeoutMillis).
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_21_048: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/[path]'.]
                            setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, iotHubUri.getPath()).
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_21_049: [The function shall set the header field 'content-type' to be the message content type.]
                            setHeaderField(HTTPS_PROPERTY_CONTENT_TYPE_TAG, httpsMessage.getContentType());
            //Codes_SRS_HTTPSIOTHUBCONNECTION_21_046: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            request.setSSLContext(sslContext);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_051: [If the IoT Hub could not be reached, the function shall throw an IOException.]
            HttpsResponse response = request.send();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_21_050: [The function shall return a ResponseMessage with the status and payload.]
            IotHubStatusCode status = IotHubStatusCode.getIotHubStatusCode(response.getStatus());
            byte[] body = response.getBody();

            return new ResponseMessage(body, status);
        }
    }

    /**
     * Receives a message, if one exists.
     *
     * @return a message, or null if none exists.
     *
     * @throws IOException if the IoT Hub could not be reached.
     */
    public Message receiveMessage() throws IOException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            String iotHubHostname = this.config.getIotHubHostname();
            String deviceId = this.config.getDeviceId();
            int readTimeoutMillis = this.config.getReadTimeoutMillis();
            int messageLockTimeoutSecs = this.config.getMessageLockTimeoutSecs();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_013: [The function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound?api-version=2016-02-03'.]
            IotHubMessageUri messageUri = new IotHubMessageUri(iotHubHostname, deviceId);
            URL messageUrl = new URL(HTTPS_HEAD_TAG + messageUri.toString());

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_014: [The function shall send a GET request.]
            HttpsRequest request =
                    new HttpsRequest(messageUrl, HttpsMethod.GET, new byte[0]).
                            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_015: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
                                    setReadTimeoutMillis(readTimeoutMillis).
                            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_017: [The function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound'.]
                                    setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG,
                                    messageUri.getPath()).
                            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_018: [The function shall set the header field 'iothub-messagelocktimeout' to be the configuration parameter messageLockTimeoutSecs.]
                                    setHeaderField(HTTPS_PROPERTY_IOTHUB_MESSAGELOCKTIMEOUT_TAG,
                                    Integer.toString(messageLockTimeoutSecs));

            SSLContext sslContext = null;
            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                sslContext = this.config.getSasTokenAuthentication().getSSLContext();

                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_057: [This function shall retrieve a sas token from its config to use in the https request header.]
                if (this.config.getSasTokenAuthentication().isRenewalNecessary())
                {
                    //Codes_SRS_HTTPSIOTHUBCONNECTION_34_054: [If the SAS token used by this has expired, the function shall return a Message object with a body of "Your sas token has expired".]
                    return new ResponseMessage("Your sas token has expired".getBytes(), IotHubStatusCode.UNAUTHORIZED);
                }

                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_016: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
                request.setHeaderField(HTTPS_PROPERTY_AUTHORIZATION_TAG, this.config.getSasTokenAuthentication().getRenewedSasToken());
            }
            else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_061: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
                sslContext = this.config.getX509Authentication().getSSLContext();
            }

            //Codes_SRS_HTTPSIOTHUBCONNECTION_25_041: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            request.setSSLContext(sslContext);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_023: [If the IoT Hub could not be reached, the function shall throw an IOException.]
            HttpsResponse response = request.send();

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_021: [If a response with IoT Hub status code OK is not received, the function shall return null.]
            Message msg = null;
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_019: [If a response with IoT Hub status code OK is received, the function shall return the IoT Hub message included in the response.]
            IotHubStatusCode messageStatus = IotHubStatusCode.getIotHubStatusCode(response.getStatus());
            if (messageStatus == IotHubStatusCode.OK)
            {
                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_020: [If a response with IoT Hub status code OK is received, the function shall save the response header field 'etag'.]
                this.messageEtag = sanitizeEtag(response.getHeaderField(HTTPS_PROPERTY_ETAG_TAG));

                HttpsSingleMessage httpsMsg = HttpsSingleMessage.parseHttpsMessage(response);
                msg = httpsMsg.toMessage();
            }

            return msg;
        }
    }

    /**
     * Sends the message result for the previously received
     * message.
     *
     * @param result the message result (one of {@link IotHubMessageResult#COMPLETE},
     *               {@link IotHubMessageResult#ABANDON}, or {@link IotHubMessageResult#REJECT}).
     *
     * @throws IllegalStateException if {@code sendMessageResult} is called before
     * {@link #receiveMessage()} is called.
     * @throws IOException if the IoT Hub could not be reached.
     * @throws SecurityException if the sas token saved in config has expired and cannot be renewed
     */
    public void sendMessageResult(IotHubMessageResult result)
            throws IOException, SecurityException
    {
        synchronized (HTTPS_CONNECTION_LOCK)
        {
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_039: [If the function is called before receiveMessage() returns a message, the function shall throw an IllegalStateException.]
            if (this.messageEtag == null)
            {
                throw new IllegalStateException("Cannot send a message "
                        + "result before a message is received.");
            }

            String iotHubHostname = this.config.getIotHubHostname();
            String deviceId = this.config.getDeviceId();
            int readTimeoutMillis = this.config.getReadTimeoutMillis();

            String resultUri = HTTPS_HEAD_TAG;
            String resultPath;
            URL resultUrl;
            HttpsRequest request;
            switch (result)
            {
                case COMPLETE:
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_024: [If the result is COMPLETE, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]?api-version=2016-02-03'.]
                    IotHubCompleteUri completeUri =
                            new IotHubCompleteUri(iotHubHostname, deviceId,
                                    this.messageEtag);
                    resultUri += completeUri.toString();
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_026: [If the result is COMPLETE, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.]
                    resultPath = completeUri.getPath();
                    resultUrl = new URL(resultUri);
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_025: [If the result is COMPLETE, the function shall send a DELETE request.]
                    request = new HttpsRequest(resultUrl, HttpsMethod.DELETE,
                            new byte[0]);
                    break;
                case ABANDON:
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_027: [If the result is ABANDON, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]/abandon?api-version=2016-02-03'.]
                    IotHubAbandonUri abandonUri =
                            new IotHubAbandonUri(iotHubHostname, deviceId,
                                    this.messageEtag);
                    resultUri += abandonUri.toString();
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_029: [If the result is ABANDON, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]/abandon'.]
                    resultPath = abandonUri.getPath();
                    resultUrl = new URL(resultUri);
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_028: [If the result is ABANDON, the function shall send a POST request.]
                    // The IoT Hub service requires the content-length header to be
                    // set but the Java SE connection omits content-length
                    // if content-length == 0. We include a placeholder body to
                    // make the connection include a content-length.
                    request = new HttpsRequest(resultUrl, HttpsMethod.POST,
                            new byte[1]);
                    break;
                case REJECT:
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_030: [If the result is REJECT, the function shall send a request to the URL 'https://[iotHubHostname]/devices/[deviceId]/messages/devicebound/[eTag]??reject=true&api-version=2016-02-03' (the query parameters can be in any order).]
                    IotHubRejectUri rejectUri =
                            new IotHubRejectUri(iotHubHostname, deviceId,
                                    this.messageEtag);
                    resultUri += rejectUri.toString();
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_032: [If the result is REJECT, the function shall set the header field 'iothub-to' to be '/devices/[deviceId]/messages/devicebound/[eTag]'.]
                    resultPath = rejectUri.getPath();
                    resultUrl = new URL(resultUri);
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_031: [If the result is REJECT, the function shall send a DELETE request.]
                    request = new HttpsRequest(resultUrl, HttpsMethod.DELETE,
                            new byte[0]);
                    break;
                default:
                    // should never happen.
                    throw new IllegalStateException(
                            "Invalid message result specified.");
            }

            SSLContext sslContext = null;
            if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.SAS_TOKEN)
            {
                sslContext = this.config.getSasTokenAuthentication().getSSLContext();

                if (this.config.getSasTokenAuthentication().isRenewalNecessary())
                {
                    //Codes_SRS_HTTPSIOTHUBCONNECTION_34_058: [If the saved SAS token for this connection has expired and cannot be renewed, this function shall throw a SecurityException.]
                    throw new SecurityException("Your SAS token has expired");
                }

                // Codes_SRS_HTTPSIOTHUBCONNECTION_11_034: [The function shall set the header field 'authorization' to be a valid SAS token generated from the configuration parameters.]
                request.setHeaderField(HTTPS_PROPERTY_AUTHORIZATION_TAG, this.config.getSasTokenAuthentication().getRenewedSasToken());
            }
            else if (this.config.getAuthenticationType() == DeviceClientConfig.AuthType.X509_CERTIFICATE)
            {
                //Codes_SRS_HTTPSIOTHUBCONNECTION_34_062: [If this config is using x509 authentication, this function shall retrieve its sslcontext from its x509 Authentication object.]
                sslContext = this.config.getX509Authentication().getSSLContext();
            }

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_033: [The function shall set the request read timeout to be the configuration parameter readTimeoutMillis.]
            request.setReadTimeoutMillis(readTimeoutMillis).
                    setHeaderField(HTTPS_PROPERTY_IOTHUB_TO_TAG, resultPath).
                    // Codes_SRS_HTTPSIOTHUBCONNECTION_11_035: [The function shall set the header field 'if-match' to be the e-tag saved when receiveMessage() was previously called.]
                            setHeaderField(HTTPS_PROPERTY_IF_MATCH_TAG, this.messageEtag);

            //Codes_SRS_HTTPSIOTHUBCONNECTION_25_042: [The function shall set the IotHub SSL context by calling setSSLContext on the request.]
            request.setSSLContext(sslContext);

            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_037: [If the IoT Hub could not be reached, the function shall throw an IOException.]
            HttpsResponse response = request.send();
            // Codes_SRS_HTTPSIOTHUBCONNECTION_11_038: [If the IoT Hub status code in the response is not OK_EMPTY, the function shall throw an IOException.]
            IotHubStatusCode resultStatus =
                    IotHubStatusCode.getIotHubStatusCode(
                            response.getStatus());
            if (resultStatus != IotHubStatusCode.OK_EMPTY)
            {
                String errMsg = String.format(
                        "Sending message result failed with status %s.%n",
                        resultStatus.name());
                throw new IOException(errMsg);
            }
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
}
