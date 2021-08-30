// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.device.*;
import com.microsoft.azure.sdk.iot.device.transport.IotHubConnectionStatus;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;


/**
 * This sample demonstrates how to configure your device client to use a custom SAS token provider instead of
 * directly providing it the device's symmetric key.
 */
public class CustomSasTokenProviderSample
{
    private static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds
    private static final List<String> failedMessageListOnClose = new ArrayList<>(); // List of messages that failed on close

    /**
     * Helper class for turning symmetric keys into SAS tokens. It also provides some helpful functions around
     * if this token should be renewed.
     */
    protected static class SasTokenHelper
    {
        private static final String RAW_SIGNATURE_FORMAT = "%s\n%s";
        private static final String SHARED_ACCESS_SIGNATURE_FORMAT = "SharedAccessSignature %s=%s&%s=%s&%s=%d";
        private static final String SCOPE_FORMAT = "%s/devices/%s";
        private static final Charset SIGNATURE_CHARSET = StandardCharsets.UTF_8;
        private static final String EXPIRY_TIME_FIELD_KEY = "se";
        private static final String SIGNATURE_FIELD_KEY = "sig";
        private static final String RESOURCE_URI_FIELD_KEY = "sr";
        private static final String HMAC_SHA_256 = "HmacSHA256";

        // When deciding whether to renew SAS tokens or not, it is wise to renew proactively to avoid clock skew issues
        // between client and server.
        private final int renewalBufferSeconds;
        private final long expiryTimeSeconds;
        private final char[] sasToken;

        /**
         * Generate a new SAS token from your host name, device Id, and device Key.
         * @param hostName the host name of your IoT Hub (for instance, "my-iot-hub.azure-devices.net").
         * @param deviceId the Id of your device.
         * @param deviceKey the primary or secondary key of your device.
         * @param secondsToLive the number of seconds that the token will live for.
         * @param renewalBufferSeconds the number of seconds before the token expires when this instance will recommend renewal via {{@link #shouldRenewSasToken()}}
         */
        public SasTokenHelper(String hostName, String deviceId, String deviceKey, int secondsToLive, int renewalBufferSeconds)
        {
            this.renewalBufferSeconds = renewalBufferSeconds;

            try
            {
                // expiry time is represented by seconds since the UNIX epoch.
                this.expiryTimeSeconds = (System.currentTimeMillis() / 1000) + secondsToLive;

                String scope = buildScope(hostName, deviceId);

                byte[] signature = String.format(RAW_SIGNATURE_FORMAT, scope, this.expiryTimeSeconds).getBytes(SIGNATURE_CHARSET);
                byte[] decodedDeviceKey = Base64.decodeBase64(deviceKey);

                // HMAC encrypt the signature
                byte[] hmacEncryptedSignature = encryptSignatureHmacSha256(signature, decodedDeviceKey);

                // Base64 encode the HMAC encrypted byte[]
                byte[] base64EncodedHmacEncryptedSignature = Base64.encodeBase64(hmacEncryptedSignature);

                // Convert byte[] of base64 encoded and HMAC encrypted bits to a UTF-8 String
                String utf8Sig = new String(base64EncodedHmacEncryptedSignature, SIGNATURE_CHARSET);

                // URL encode the string
                String urlEncodedSignature = URLEncoder.encode(utf8Sig, SIGNATURE_CHARSET.name());

                this.sasToken = String.format(
                    SHARED_ACCESS_SIGNATURE_FORMAT,
                    RESOURCE_URI_FIELD_KEY,
                    scope,
                    SIGNATURE_FIELD_KEY,
                    urlEncodedSignature,
                    EXPIRY_TIME_FIELD_KEY,
                    this.expiryTimeSeconds).toCharArray();
            }
            catch (UnsupportedEncodingException | InvalidKeyException | NoSuchAlgorithmException e)
            {
                // The exceptions here should never be thrown since the algorithm, encoding, and key are all hardcoded
                throw new IllegalStateException("Failed to generate a new SAS token", e);
            }
        }

        /**
         * Get the SAS token char array.
         * @return The SAS token char array.
         */
        public char[] getValue()
        {
            return this.sasToken;
        }

        /**
         * Returns if this SAS token should be renewed.
         * @return true if this SAS token has expired, or will expire soon (depending on the provided renewal buffer). False, otherwise.
         */
        public boolean shouldRenewSasToken()
        {
            long currentTimeSeconds = (System.currentTimeMillis() / 1000);

            // It will recommend renewing the token if it is expired, or if it will expire in the next few seconds
            return this.expiryTimeSeconds + this.renewalBufferSeconds >= currentTimeSeconds;
        }

        private byte[] encryptSignatureHmacSha256(byte[] signature, byte[] deviceKey) throws NoSuchAlgorithmException, InvalidKeyException
        {
            SecretKeySpec secretKey = new SecretKeySpec(deviceKey, HMAC_SHA_256);

            byte[] encryptedSig;
            Mac hMacSha256 = Mac.getInstance(HMAC_SHA_256);
            hMacSha256.init(secretKey);
            encryptedSig = hMacSha256.doFinal(signature);

            return encryptedSig;
        }

        private String buildScope(String hostName, String deviceId)
        {
            return String.format(SCOPE_FORMAT, hostName, deviceId);
        }
    }

    /**
     * A sample implementation of the {@link SasTokenProvider} interface. It demonstrates how to generate your own SAS
     * tokens from your device key, device Id, and host name. It also demonstrates how to choose how long your SAS tokens
     * will live for.
     *
     * The purpose of the {@link SasTokenProvider} interface is to allow users to generate these tokens in separate
     * processes from the SDK for security purposes, if they wish. This sample does not demonstrate that scenario.
     */
    protected static class SasTokenProviderImpl implements SasTokenProvider
    {
        private final String deviceKey;
        private final String hostName;
        private final String deviceId;
        private final int secondsToLivePerToken;
        private final int renewalBufferSeconds;

        private SasTokenHelper cachedSasToken;

        public SasTokenProviderImpl(String hostName, String deviceId, String deviceKey, int secondsToLivePerToken, int renewalBufferSeconds)
        {
            this.hostName = hostName;
            this.deviceId = deviceId;
            this.deviceKey = deviceKey;
            this.secondsToLivePerToken = secondsToLivePerToken;
            this.renewalBufferSeconds = renewalBufferSeconds;
        }

        @Override
        public char[] getSasToken()
        {
            if (this.cachedSasToken == null || this.cachedSasToken.shouldRenewSasToken())
            {
                // if no SAS token is cached, or if the cached token is expired/about to expire, create a new one
                this.cachedSasToken = new SasTokenHelper(this.hostName, this.deviceId, this.deviceKey, this.secondsToLivePerToken, this.renewalBufferSeconds);
                return this.cachedSasToken.getValue();
            }
            //else if (...)
            //{
                // It is recommended to have some logic in here that checks to make sure that the device key in use itself
                // is still valid. A given device may have it's keys cycled by its owner, and this would be an appropriate
                // time to update the device key if a new one was cycled in.
            //}
            else
            {
                return this.cachedSasToken.getValue();
            }
        }
    }

    protected static class EventCallback implements IotHubEventCallback
    {
        public void execute(IotHubStatusCode status, Object context)
        {
            Message msg = (Message) context;

            System.out.println("IoT Hub responded to message "+ msg.getMessageId()  + " with status " + status.name());

            if (status == IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE)
            {
                failedMessageListOnClose.add(msg.getMessageId());
            }
        }
    }

    protected static class IotHubConnectionStatusChangeCallbackLogger implements IotHubConnectionStatusChangeCallback
    {
        @Override
        public void execute(IotHubConnectionStatus status, IotHubConnectionStatusChangeReason statusChangeReason, Throwable throwable, Object callbackContext)
        {
            System.out.println();
            System.out.println("CONNECTION STATUS UPDATE: " + status);
            System.out.println("CONNECTION STATUS REASON: " + statusChangeReason);
            System.out.println("CONNECTION STATUS THROWABLE: " + (throwable == null ? "null" : throwable.getMessage()));
            System.out.println();

            if (throwable != null)
            {
                throwable.printStackTrace();
            }

            if (status == IotHubConnectionStatus.DISCONNECTED)
            {
                System.out.println("The connection was lost, and is not being re-established." +
                        " Look at provided exception for how to resolve this issue." +
                        " Cannot send messages until this issue is resolved, and you manually re-open the device client");
            }
            else if (status == IotHubConnectionStatus.DISCONNECTED_RETRYING)
            {
                System.out.println("The connection was lost, but is being re-established." +
                        " Can still send messages, but they won't be sent until the connection is re-established");
            }
            else if (status == IotHubConnectionStatus.CONNECTED)
            {
                System.out.println("The connection was successfully established. Can send messages.");
            }
        }
    }

    /**
     * This sample demonstrates how to configure your device client to use a custom SAS token provider instead of
     * directly providing it the device's symmetric key.
     *
     * @param args
     * args[0] = IoT Hub or Edge Hub connection string
     * args[1] = number of messages to send
     * args[2] = protocol (optional, one of 'mqtt' or 'amqps' or 'https' or 'amqps_ws')
     * args[3] = path to certificate to enable one-way authentication over ssl. (Not necessary when connecting directly to Iot Hub, but required if connecting to an Edge device using a non public root CA certificate).
     */
    public static void main(String[] args) throws IOException
    {
        System.out.println("Starting...");
        System.out.println("Beginning setup.");

        if (args.length != 4)
        {
            System.out.format(
                    "Expected 4 arguments but received: %d.\n"
                            + "The program should be called with the following args: \n"
                            + "1. Host Name of your IoT Hub, for instance my-iot-hub.azure-devices.net \n"
                            + "2. Id of your device\n"
                            + "3. The device key of your device\n"
                            + "4. (https | amqps | amqps_ws | mqtt | mqtt_ws)\n",
                    args.length);
            return;
        }

        String hostName = args[0];
        String deviceId = args[1];
        String deviceKey = args[2];

        IotHubClientProtocol protocol;
        String protocolStr = args[3];
        if (protocolStr.equals("https"))
        {
            protocol = IotHubClientProtocol.HTTPS;
        }
        else if (protocolStr.equals("amqps"))
        {
            protocol = IotHubClientProtocol.AMQPS;
        }
        else if (protocolStr.equals("mqtt"))
        {
            protocol = IotHubClientProtocol.MQTT;
        }
        else if (protocolStr.equals("amqps_ws"))
        {
            protocol = IotHubClientProtocol.AMQPS_WS;
        }
        else if (protocolStr.equals("mqtt_ws"))
        {
            protocol = IotHubClientProtocol.MQTT_WS;
        }
        else
        {
            System.out.format(
                    "Expected argument 4 to be one of 'mqtt', 'https', 'amqps' or 'amqps_ws' but received %s\n"
                            + "The program should be called with the following args: \n"
                            + "1. Host Name of your IoT Hub, for instance my-iot-hub.azure-devices.net \n"
                            + "2. Id of your device\n"
                            + "3. The device key of your device\n"
                            + "4. (https | amqps | amqps_ws | mqtt | mqtt_ws)\n",
                    protocolStr);
            return;
        }

        System.out.println("Successfully read input parameters.");
        System.out.format("Using communication protocol %s.\n", protocol.name());

        System.out.println("Constructing SAS token provider");
        int tokenTimeToLiveSeconds = 60 * 60; // 1 hour
        int tokenRenewalBufferSeconds = 60; // token will recommend renewal after it reaches 1 minute before it expires
        SasTokenProvider sasTokenProvider = new SasTokenProviderImpl(hostName, deviceId, deviceKey, tokenTimeToLiveSeconds, tokenRenewalBufferSeconds);

        System.out.println("Constructing Device Client that will use the SAS token provider");
        DeviceClient client = new DeviceClient(hostName, deviceId, sasTokenProvider, protocol);

        System.out.println("Successfully created an IoT Hub client.");

        client.setConnectionStatusChangeCallback(new IotHubConnectionStatusChangeCallbackLogger(), new Object());

        client.open();

        System.out.println("Opened connection to IoT Hub.");
        System.out.println("Sending telemetry...");

        double temperature = 20 + Math.random() * 10;
        double humidity = 30 + Math.random() * 20;
        String msgStr = "{\"temperature\":"+ temperature +",\"humidity\":"+ humidity +"}";

        try
        {
            Message msg = new Message(msgStr);
            msg.setContentType("application/json");
            msg.setProperty("temperatureAlert", temperature > 28 ? "true" : "false");
            msg.setMessageId(java.util.UUID.randomUUID().toString());
            msg.setExpiryTime(D2C_MESSAGE_TIMEOUT);
            System.out.println(msgStr);

            EventCallback callback = new EventCallback();
            client.sendEventAsync(msg, callback, msg);
        }
        catch (Exception e)
        {
            e.printStackTrace(); // Trace the exception
        }

        System.out.println("Wait for " + D2C_MESSAGE_TIMEOUT / 1000 + " second(s) for response from the IoT Hub...");

        // Wait for IoT Hub to respond.
        try
        {
            Thread.sleep(D2C_MESSAGE_TIMEOUT);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        // close the connection
        System.out.println("Closing");
        client.close();

        if (!failedMessageListOnClose.isEmpty())
        {
            System.out.println("List of messages that were cancelled on close:" + failedMessageListOnClose.toString());
        }

        System.out.println("Shutting down...");
    }
}
