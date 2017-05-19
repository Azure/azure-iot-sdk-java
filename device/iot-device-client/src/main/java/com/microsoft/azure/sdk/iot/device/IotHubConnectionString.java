// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Parser for the Iothub connection string.
 */
public class IotHubConnectionString
{
    /** The hostName attribute name in a connection string. */
    private static final String HOSTNAME_ATTRIBUTE = "HostName=";
    /** The device ID attribute name in a connection string. */
    private static final String DEVICE_ID_ATTRIBUTE = "DeviceId=";
    /** The shared access key attribute name in a connection string. */
    private static final String SHARED_ACCESS_KEY_ATTRIBUTE = "SharedAccessKey=";
    /** The shared access signature attribute name in a connection string. */
    private static final String SHARED_ACCESS_TOKEN_ATTRIBUTE = "SharedAccessSignature=";

    /**
     * The charset used for URL-encoding the device ID in the connection
     * string.
     */
    private static final Charset CONNECTION_STRING_CHARSET = StandardCharsets.UTF_8;

    private String hostName = null;
    private String hubName = null;
    private String deviceId = null;
    private String sharedAccessKey = null;
    private String sharedAccessToken = null;
    private CustomLogger logger = null;
    
    
    /**
     * CONSTRUCTOR.
     *
     * @param connectionString is the iothub connection string to parse.
     * @throws IllegalArgumentException if the provided connectionString is {@code null}, empty, or not valid.
     * @throws URISyntaxException if the hostName in the connection string is not a valid URI.
     */
    public IotHubConnectionString(String connectionString) throws URISyntaxException, IllegalArgumentException
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_016: [If the connection string is null or empty, the constructor shall throw an IllegalArgumentException.] */
        if ((connectionString == null) || connectionString.isEmpty())
        {
            throw new IllegalArgumentException("The connection string cannot be null or empty.");
        }

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_010: [The constructor shall interpret the connection string as a set of key-value pairs delimited by ';', with keys and values separated by '='.] */
        String[] connStringAttrs = connectionString.split(";");
        for (String attr : connStringAttrs)
        {
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_011: [The constructor shall save the IoT Hub hostName as the value of 'hostName' in the connection string.] */
            if (attr.startsWith(HOSTNAME_ATTRIBUTE))
            {
                this.hostName = attr.substring(HOSTNAME_ATTRIBUTE.length());
            }
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_013: [The constructor shall save the device ID as the UTF-8 URL-decoded value of 'deviceId' in the connection string.] */
            else if (attr.startsWith(DEVICE_ID_ATTRIBUTE))
            {
                String urlEncodedDeviceId = attr.substring(DEVICE_ID_ATTRIBUTE.length());
                try
                {
                    this.deviceId = URLDecoder.decode(urlEncodedDeviceId, CONNECTION_STRING_CHARSET.name());
                } catch (UnsupportedEncodingException e) {
                    // should never happen, since the encoding is hard-coded.
                    throw new IllegalStateException(e);
                }
            }
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_014: [The constructor shall save the device key as the value of 'sharedAccessKey' in the connection string.] */
            else if (attr.startsWith(SHARED_ACCESS_KEY_ATTRIBUTE))
            {
                this.sharedAccessKey = attr.substring(SHARED_ACCESS_KEY_ATTRIBUTE.length());
            }
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_015: [The constructor shall save the shared access token as the value of 'sharedAccessToken' in the connection string.] */
            else if (attr.startsWith(SHARED_ACCESS_TOKEN_ATTRIBUTE))
            {
                this.sharedAccessToken = attr.substring(SHARED_ACCESS_TOKEN_ATTRIBUTE.length());
            }
        }

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_017: [If the connection string is not valid, the constructor shall throw an IllegalArgumentException.] */
        validateTerms(this.hostName, this.deviceId, this.sharedAccessKey, this.sharedAccessToken);

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_012: [The constructor shall save the first part of the IoT Hub hostname as the value of `hubName`, hostname split by `.`.] */
        this.hubName = parseHubName(this.hostName);

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("IotHubConnectionString object is created successfully for %s, method name is %s ", this.hostName, logger.getMethodName());
    }

    /**
     * Constructor.
     *
     * @param hostName the IoT Hub hostname.
     * @param deviceId the device ID.
     * @param sharedAccessKey the device key.
     * @param sharedAccessToken the shared access token.
     * @throws URISyntaxException if the IoT Hub hostname does not conform to RFC 3986.
     * @throws IllegalArgumentException if the IoT Hub hostname does not contain
     * a valid IoT Hub name as its prefix.
     */
    public IotHubConnectionString(String hostName, String deviceId,
                                  String sharedAccessKey, String sharedAccessToken)
            throws URISyntaxException, IllegalArgumentException
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_025: [If the parameters for the connection string is not valid, the constructor shall throw an IllegalArgumentException.] */
        validateTerms(hostName, deviceId, sharedAccessKey, sharedAccessToken);

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_020: [The constructor shall save the IoT Hub hostname as the value of `hostName` in the connection string.] */
        this.hostName = hostName;

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_021: [The constructor shall save the first part of the IoT Hub hostname as the value of `hubName`, hostname split by `.`.] */
        this.hubName = parseHubName(this.hostName);

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_022: [The constructor shall save the device ID as the UTF-8 URL-decoded value of `deviceId` in the connection string.] */
        this.deviceId = deviceId;

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_023: [The constructor shall save the device key as the value of `sharedAccessKey` in the connection string.] */
        this.sharedAccessKey = sharedAccessKey;

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_024: [The constructor shall save the shared access token as the value of `sharedAccessToken` in the connection string.] */
        this.sharedAccessToken = sharedAccessToken;

        this.logger = new CustomLogger(this.getClass());
        logger.LogInfo("IotHubConnectionString object is created successfully for %s, method name is %s ", this.hostName, logger.getMethodName());
    }

    /**
     * Getter for the hostName.
     * @return string with the hostName in the connectionString
     */
    public String getHostName()
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_030: [The getHostName shall return the stored host name.] */
        return this.hostName;
    }

    /**
     * Getter for the hubName.
     * @return string with the hubName in the connectionString
     */
    public String getHubName()
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_031: [The getHubName shall return the stored hub name, which is the first part of the hostName.] */
        return this.hubName;
    }

    /**
     * Getter for the deviceId.
     * @return string with the deviceId in the connectionString
     */
    public String getDeviceId()
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_032: [The getDeviceId shall return the stored device id.] */
        return this.deviceId;
    }

    /**
     * Getter for the sharedAccessKey.
     * @return string with the sharedAccessKey in the connectionString. It can be {@code null}.
     */
    public String getSharedAccessKey()
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_033: [The getSharedAccessKey shall return the stored shared access key.] */
        return this.sharedAccessKey;
    }

    /**
     * Getter for the sharedAccessToken.
     * @return string with the sharedAccessToken in the connectionString. It can be {@code null}.
     */
    public String getSharedAccessToken()
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_034: [The getSharedAccessToken shall return the stored shared access token.] */
        return this.sharedAccessToken;
    }


    private static void validateTerms(String hostName, String deviceId,
                               String sharedAccessKey, String sharedAccessToken)
            throws URISyntaxException, IllegalArgumentException
    {
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_001: [A valid `hostName` shall not be null or empty.] */
        if ((hostName == null) || hostName.isEmpty())
        {
            throw new IllegalArgumentException("IoT Hub hostName cannot be null.");
        }

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_002: [A valid `hostName` shall be a valid URI.] */
        new URI(hostName);

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_003: [A valid `hostName` shall contain at least one `.`.] */
        parseHubName(hostName);

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_004: [A valid `deviceId` shall not be null or empty.] */
        if ((deviceId == null) || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device ID cannot be null.");
        }

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_008: [A valid connectionString shall not contain both `sharedAccessToken` and `sharedAccessKey` at the same time.] */
        if((sharedAccessKey != null) && (sharedAccessToken != null))
        {
            throw new IllegalArgumentException("Either of device key or Shared Access Signature should be provided.");
        }

        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_005: [A valid connectionString shall contain a `sharedAccessToken` or a `sharedAccessKey`.] */
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_006: [If provided, the `sharedAccessToken` shall not be null or empty.] */
        /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_007: [If provided, the `sharedAccessKey` shall not be null or empty.] */
        if (((sharedAccessKey == null) || sharedAccessKey.isEmpty()) &&
                ((sharedAccessToken == null) || sharedAccessToken.isEmpty()))
        {
            throw new IllegalArgumentException("Device key and Shared Access Signature both cannot be null.");
        }
    }

    private static String parseHubName(String hostName) throws IllegalArgumentException
    {
        int iotHubNameEndIdx = hostName.indexOf(".");
        if (iotHubNameEndIdx == -1)
        {
            String errStr = String.format(
                    "%s did not include a valid IoT Hub name as its prefix. "
                            + "An IoT Hub hostname has the following format: "
                            + "[iotHubName].[valid URI chars]",
                    hostName);
            throw new IllegalArgumentException(errStr);
        }
        return hostName.substring(0, iotHubNameEndIdx);
    }
}
