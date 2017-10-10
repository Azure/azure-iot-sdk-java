/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package tests.integration.com.microsoft.azure.sdk.iot.helpers;

import com.microsoft.azure.sdk.iot.deps.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class SasTokenGenerator
{
    private static final String ENCODING_CHARSET = "utf-8";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int EPOCH_YEAR = 1970;

    private static final String URI_SEPARATOR = "/";
    private static final String DEVICE_PATH = "devices";

    /**
     * Creates HMAC_SHA256 SAS token for the specified device using its device key
     * @param hostname The host name for the IoT Hub
     * @param deviceId The id of the device to create the token for
     * @param devicePrimaryKey The primary key for the device
     * @param secondsUntilExpiration the number of seconds until the created token expires
     * @return The full SAS token string for accessing your device
     * @throws InvalidKeyException if the provided device key cannot be used to authenticate
     */
    public static String generateSasTokenForIotDevice(String hostname, String deviceId, String devicePrimaryKey, long secondsUntilExpiration)
            throws InvalidKeyException
    {
        String deviceUri = hostname + URI_SEPARATOR + DEVICE_PATH + URI_SEPARATOR + deviceId;

        //Codes_SRS_SAS_TOKEN_GENERATOR_34_003: [This created Sas Token will expire after the provided number of seconds.]
        Date now = new Date();
        Date previousDate = new Date(EPOCH_YEAR);
        long tokenExpirationTime = ((now.getTime() - previousDate.getTime()) / 1000) + secondsUntilExpiration;

        //Codes_SRS_SAS_TOKEN_GENERATOR_34_002: [This method shall create a Sas token from the provided device key and deviceId.]
        String signature = getSignature(deviceUri, tokenExpirationTime, devicePrimaryKey);

        //Codes_SRS_SAS_TOKEN_GENERATOR_34_001: [This method shall return the created Sas token in the format "HostName=<hostname>;DeviceId=<deviceId>;SharedAccessSignature=SharedAccessSignature sr=<deviceUri>&sig=<signature>&se=<tokenExpirationTime>".]
        String token = String.format("HostName=%s;DeviceId=%s;SharedAccessSignature=SharedAccessSignature sig=%s&se=%s&sr=%s", hostname, deviceId, signature,
                String.valueOf(tokenExpirationTime), deviceUri);

        return token;
    }

    /**
     * Creates and returns the sas token signature
     * @param resourceUri the uri for the resource the sas token is created for
     * @param expiryTime the number of seconds for the sas token to be valid for
     * @param devicePrimaryKey the key for the primary device
     * @return The created sas token signature
     * @throws InvalidKeyException if the provided device key is invalid
     */
    private static String getSignature(String resourceUri, long expiryTime, String devicePrimaryKey)
            throws InvalidKeyException
    {
        try
        {
            byte[] textToSign = new String(resourceUri + "\n" + expiryTime).getBytes();
            byte[] decodedDeviceKey = Base64.decodeBase64Local(devicePrimaryKey.getBytes());
            byte[] signature = encryptHmacSha256(textToSign, decodedDeviceKey);
            byte[] encryptedSignature = Base64.encodeBase64Local(signature);
            String encryptedSignatureUtf8 = new String(encryptedSignature, StandardCharsets.UTF_8);
            return URLEncoder.encode(encryptedSignatureUtf8, ENCODING_CHARSET);
        }
        catch (NoSuchAlgorithmException | UnsupportedEncodingException e)
        {
            //algorithm and encoding are hardcoded, this should never catch
        }

        return "";
    }

    /**
     * Encrypts HMAC_SHA256 Hash-based Message Authentication Code using SHA256 hash function
     * @param textToSign the text to encrypt
     * @param key the key used for authentication
     * @return the encrypted text
     * @throws NoSuchAlgorithmException if HMAC_SHA256 isn't a valid algorithm for encryption
     * @throws InvalidKeyException if the provided key is not valid for authentication
     */
    private static byte[] encryptHmacSha256(byte[] textToSign, byte[] key)
            throws NoSuchAlgorithmException, InvalidKeyException
    {
        SecretKeySpec secretKey = new SecretKeySpec(key, HMAC_SHA256);
        Mac hMacSha256 = Mac.getInstance(HMAC_SHA256);
        hMacSha256.init(secretKey);
        return hMacSha256.doFinal(textToSign);
    }
}
