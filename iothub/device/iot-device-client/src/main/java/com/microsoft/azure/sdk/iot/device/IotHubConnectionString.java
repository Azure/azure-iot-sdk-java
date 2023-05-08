// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Parser for the Iothub connection string.
 */
@Slf4j
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

    /** The module ID attribute name in a connection string. */
    private static final String MODULE_ID_ATTRIBUTE = "ModuleId=";

    /**
     * IP address or internet name of the host machine working as a device or protocol gateway.
     * Used when communicating with Azure Edge devices.
     * */
    private static final String GATEWAY_HOST_NAME_ATTRIBUTE = "GatewayHostName=";

    /** Used for E4K. */
    private static final String MQTT_GATEWAY_HOST_NAME_ATTRIBUTE = "MqttGatewayHostName=";

    /** Specify when using X.509 certificate to authenticate */
    private static final String X509_ENABLED_ATTRIBUTE = "x509=true";

    /**
     * The charset used for URL-encoding the device ID in the connection
     * string.
     */
    private static final Charset CONNECTION_STRING_CHARSET = StandardCharsets.UTF_8;

    private String hostName;
    private final String hubName;
    private String deviceId;
    private String sharedAccessKey;
    private String sharedAccessToken;
    private String moduleId;
    private final boolean isUsingX509;
    private String gatewayHostName;
    private String mqttGatewayHostName;

    /**
     * CONSTRUCTOR.
     *
     * @param connectionString is the iothub connection string to parse.
     * @throws IllegalArgumentException if the provided connectionString is {@code null}, empty, or not valid or if the hostName in the connection string is not a valid URI.
     */
    public IotHubConnectionString(String connectionString) throws IllegalArgumentException
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
            if (attr.toLowerCase().startsWith(HOSTNAME_ATTRIBUTE.toLowerCase()))
            {
                this.hostName = attr.substring(HOSTNAME_ATTRIBUTE.length());
            }
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_013: [The constructor shall save the device ID as the UTF-8 URL-decoded value of 'deviceId' in the connection string.] */
            else if (attr.toLowerCase().startsWith(DEVICE_ID_ATTRIBUTE.toLowerCase()))
            {
                String urlEncodedDeviceId = attr.substring(DEVICE_ID_ATTRIBUTE.length());
                try
                {
                    this.deviceId = URLDecoder.decode(urlEncodedDeviceId, CONNECTION_STRING_CHARSET.name());
                }
                catch (UnsupportedEncodingException e)
                {
                    // should never happen, since the encoding is hard-coded.
                    throw new IllegalStateException(e);
                }
            }
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_014: [The constructor shall save the device key as the value of 'sharedAccessKey' in the connection string.] */
            else if (attr.toLowerCase().startsWith(SHARED_ACCESS_KEY_ATTRIBUTE.toLowerCase()))
            {
                this.sharedAccessKey = attr.substring(SHARED_ACCESS_KEY_ATTRIBUTE.length());
            }
            /* Codes_SRS_IOTHUB_CONNECTIONSTRING_21_015: [The constructor shall save the shared access token as the value of 'sharedAccessToken' in the connection string.] */
            else if (attr.toLowerCase().startsWith(SHARED_ACCESS_TOKEN_ATTRIBUTE.toLowerCase()))
            {
                this.sharedAccessToken = attr.substring(SHARED_ACCESS_TOKEN_ATTRIBUTE.length());
            }
            else if (attr.toLowerCase().startsWith(MODULE_ID_ATTRIBUTE.toLowerCase()))
            {
                this.moduleId = attr.substring(MODULE_ID_ATTRIBUTE.length());
            }
            else if (attr.toLowerCase().startsWith(GATEWAY_HOST_NAME_ATTRIBUTE.toLowerCase()))
            {
                this.gatewayHostName = attr.substring(GATEWAY_HOST_NAME_ATTRIBUTE.length());
            }
            else if (attr.toLowerCase().startsWith(MQTT_GATEWAY_HOST_NAME_ATTRIBUTE.toLowerCase()))
            {
                this.mqttGatewayHostName = attr.substring(MQTT_GATEWAY_HOST_NAME_ATTRIBUTE.length());
            }
        }

        this.isUsingX509 = connectionString.contains(X509_ENABLED_ATTRIBUTE);
        validateTerms(this.hostName, this.deviceId, this.sharedAccessKey, this.sharedAccessToken, this.isUsingX509);

        this.hubName = parseHubName(this.hostName);
    }

    /**
     * Constructor.
     *
     * @param hostName the IoT Hub hostname.
     * @param deviceId the device ID.
     * @param sharedAccessKey the device key.
     * @param sharedAccessToken the shared access token.
     * @throws IllegalArgumentException if the IoT Hub hostname does not contain
     * a valid IoT Hub name as its prefix or if the IoT Hub hostname does not conform to RFC 3986.
     */
    public IotHubConnectionString(
        String hostName,
        String deviceId,
        String sharedAccessKey,
        String sharedAccessToken)
            throws IllegalArgumentException
    {
        this(hostName, deviceId, sharedAccessKey, sharedAccessToken, null, null);
    }

    private IotHubConnectionString(
        String hostName,
        String deviceId,
        String sharedAccessKey,
        String sharedAccessToken,
        String gatewayHostName,
        String mqttGatewayHostName)
            throws IllegalArgumentException
    {
        this.isUsingX509 = (sharedAccessKey == null && sharedAccessToken == null);

        validateTerms(hostName, deviceId, sharedAccessKey, sharedAccessToken, this.isUsingX509);

        this.hostName = hostName;
        this.hubName = parseHubName(this.hostName);
        this.deviceId = deviceId;
        this.sharedAccessKey = sharedAccessKey;
        this.sharedAccessToken = sharedAccessToken;

        this.gatewayHostName = gatewayHostName;
        if (this.gatewayHostName != null && !this.gatewayHostName.isEmpty())
        {
            this.hostName = gatewayHostName;
        }

        this.mqttGatewayHostName = mqttGatewayHostName;
        if (this.mqttGatewayHostName != null && !this.mqttGatewayHostName.isEmpty())
        {
            this.hostName = mqttGatewayHostName;
        }
    }

    /**
     * Getter for the hostName.
     * @return string with the hostName in the connectionString
     */
    public String getHostName()
    {
        return this.hostName;
    }

    public String getGatewayHostName()
    {
        return this.gatewayHostName;
    }

    public String getMqttGatewayHostName() { return this.mqttGatewayHostName; }

    /**
     * Getter for the hubName.
     * @return string with the hubName in the connectionString
     */
    public String getHubName()
    {
        return this.hubName;
    }

    /**
     * Getter for the deviceId.
     * @return string with the deviceId in the connectionString
     */
    public String getDeviceId()
    {
        return this.deviceId;
    }

    /**
     * Getter for the sharedAccessKey.
     * @return string with the sharedAccessKey in the connectionString. It can be {@code null}.
     */
    public String getSharedAccessKey()
    {
        return this.sharedAccessKey;
    }

    /**
     * Getter for the sharedAccessToken.
     * @return string with the sharedAccessToken in the connectionString. It can be {@code null}.
     */
    public String getSharedAccessToken()
    {
        return this.sharedAccessToken;
    }

    public String getModuleId()
    {
        return this.moduleId;
    }

    /**
     * Getter for UsingX509
     *
     * @return The value of UsingX509
     */
    public boolean isUsingX509()
    {
        return this.isUsingX509;
    }

    /**
     * Setter for the shared access token
     * @param sharedAccessToken the new token value to set
     * @throws IllegalArgumentException if the provided value is null or empty
     */
    public void setSharedAccessToken(String sharedAccessToken) throws IllegalArgumentException
    {
        if (sharedAccessToken == null || sharedAccessToken.isEmpty())
        {
            throw new IllegalArgumentException("Shared access token cannot be set to null or empty");
        }

        this.sharedAccessToken = sharedAccessToken;
    }


    private static void validateTerms(String hostName, String deviceId,
                                      String sharedAccessKey, String sharedAccessToken, boolean usingX509)
            throws IllegalArgumentException
    {
        if ((hostName == null) || hostName.isEmpty())
        {
            throw new IllegalArgumentException("IoT Hub hostName cannot be null.");
        }

        try
        {
            new URI(hostName);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalArgumentException("Host name did not contain a valid URI", e);
        }

        parseHubName(hostName);

        if ((deviceId == null) || deviceId.isEmpty())
        {
            throw new IllegalArgumentException("Device ID cannot be null.");
        }

        if ((sharedAccessKey != null) && (sharedAccessToken != null))
        {
            throw new IllegalArgumentException("Either of device key or Shared Access Signature should be provided, but not both.");
        }

        if (!usingX509
                && ((sharedAccessKey == null) || sharedAccessKey.isEmpty())
                && ((sharedAccessToken == null) || sharedAccessToken.isEmpty()))
        {
            throw new IllegalArgumentException("Device key and Shared Access Signature both cannot be null unless using x509 authentication.");
        }
    }

    static String parseHubName(String hostName) throws IllegalArgumentException
    {
        int iotHubNameEndIdx = hostName.indexOf(".");
        if (iotHubNameEndIdx == -1)
        {
            String errStr = "Provided hostname did not include a valid IoT Hub name as its prefix. "
                    + "An IoT Hub hostname has the following format: "
                    + "[iotHubName].[valid URI chars]";
            throw new IllegalArgumentException(errStr);
        }
        return hostName.substring(0, iotHubNameEndIdx);
    }
}