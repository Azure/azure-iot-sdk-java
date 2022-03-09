// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.transport.https;

import com.microsoft.azure.sdk.iot.device.transport.TransportUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * A URI for a device to connect to an IoT Hub.
 */
public final class IotHubUri
{
    /**
     * The device ID and specific IoT Hub method path will be interpolated into
     * the path.
     */
    private static final String PATH_FORMAT = "/devices/%s%s";

    private static final String PATH_FORMAT_WITH_MODULEID = "/devices/%s/modules/%s/%s";

    /** The API version will be passed as a param in the URI. */
    public static final String API_VERSION = "api-version=" + TransportUtils.IOTHUB_API_VERSION;

    /** The charset used when URL-encoding the IoT Hub name and device ID. */
    private static final Charset IOTHUB_URL_ENCODING_CHARSET =
            StandardCharsets.UTF_8;

    /**
     * The IoT Hub resource URI is the hostname and path component that is
     * common to all IoT Hub communication methods between the given device and
     * IoT Hub.
     */
    private String hostname;
    private String path;
    private String uri;

    /**
     * Constructor. Creates a URI to an IoT Hub method. The URI does not include
     * a protocol. The function will safely escape the given arguments.
     *
     * @param iotHubHostname the IoT Hub hostname.
     * @param deviceId the device ID.
     * @param iotHubMethodPath the path from the IoT Hub resource to the
     * method.
     * @param queryParams the URL query parameters. Can be null.
     * @param moduleId the module ID. May be null
     */
    public IotHubUri(String iotHubHostname, String deviceId,
            String iotHubMethodPath, Map<String, String> queryParams, String moduleId)
    {
        this.hostname = iotHubHostname;

        String rawPath;
        if (moduleId == null || moduleId.isEmpty())
        {
            rawPath = String.format(PATH_FORMAT, deviceId, iotHubMethodPath);
        }
        else
        {
            rawPath = String.format(PATH_FORMAT_WITH_MODULEID, deviceId, moduleId, iotHubMethodPath);
        }

        this.path = urlEncodePath(rawPath);
        StringBuilder uriBuilder = new StringBuilder(this.hostname);
        uriBuilder.append(this.path);
        uriBuilder.append("?");
        uriBuilder.append(API_VERSION);
        if (queryParams != null)
        {
            for (Map.Entry<String, String> param : queryParams.entrySet())
            {
                uriBuilder.append("&");
                appendQueryParam(uriBuilder, param.getKey(),
                        param.getValue());
            }
        }

        this.uri = uriBuilder.toString();
    }

    /**
     * Constructor. Equivalent to {@code new IotHubUri(iotHubHostname, deviceId,
     * iotHubMethodPath, null)}.
     *
     * @param iotHubHostname the IoT Hub hostname.
     * @param deviceId the device ID.
     * @param iotHubMethodPath the path from the IoT Hub resource to the
     * method.
     * @param moduleId the module ID.
     */
    public IotHubUri(String iotHubHostname, String deviceId, String iotHubMethodPath, String moduleId)
    {
        this(iotHubHostname, deviceId, iotHubMethodPath, null, moduleId);
    }

    /**
     * Returns the string representation of the IoT Hub URI.
     *
     * @return the string representation of the IoT Hub URI.
     */
    @Override
    public String toString()
    {
        return this.uri;
    }

    public String toStringWithoutApiVersion()
    {
        return this.hostname + this.path;
    }

    /**
     * Returns the string representation of the IoT Hub hostname.
     *
     * @return the string representation of the IoT Hub hostname.
     */
    public String getHostname()
    {
        return this.hostname;
    }

    /**
     * Returns the string representation of the IoT Hub path.
     *
     * @return the string representation of the IoT Hub path.
     */
    public String getPath()
    {
        return this.path;
    }

    /**
     * Returns the string representation of the IoT Hub resource URI. The IoT
     * Hub resource URI is the hostname and path component that is common to all
     * IoT Hub communication methods between the given device and IoT Hub.
     * Safely escapes the IoT Hub resource URI.
     *
     * @param iotHubHostname the IoT Hub hostname.
     * @param deviceId the device ID.
     * @param moduleId the module ID.
     * @return the string representation of the IoT Hub resource URI.
     */
    public static String getResourceUri(String iotHubHostname, String deviceId, String moduleId)
    {
        IotHubUri iotHubUri = new IotHubUri(iotHubHostname, deviceId, "", moduleId);
        return iotHubUri.getHostname() + iotHubUri.getPath();
    }

    /**
     * URL-encodes each subdirectory in the path.
     *
     * @param path the path to be safely escaped.
     *
     * @return a path with each subdirectory URL-encoded.
     */
    private static String urlEncodePath(String path)
    {
        String[] pathSubDirs = path.split("/");
        StringBuilder urlEncodedPathBuilder = new StringBuilder();
        try
        {
            for (String subDir : pathSubDirs)
            {
                if (subDir.length() > 0)
                {
                    String urlEncodedSubDir = URLEncoder.encode(
                            subDir, IOTHUB_URL_ENCODING_CHARSET.name());
                    urlEncodedPathBuilder.append("/");
                    urlEncodedPathBuilder.append(urlEncodedSubDir);
                }
            }
        }
        catch (UnsupportedEncodingException e)
        {
            // should never happen.
            throw new IllegalStateException(e);
        }

        return urlEncodedPathBuilder.toString();
    }

    /**
     * URL-encodes the query param {@code name} and {@code value} using charset UTF-8 and
     * appends them to the URI.
     *
     * @param uriBuilder the URI.
     * @param name the query param name.
     * @param value the query param value.
     */
    private static void appendQueryParam(StringBuilder uriBuilder,
                                         String name, String value)
    {
        try
        {
            String urlEncodedName = URLEncoder.encode(name,
                    IOTHUB_URL_ENCODING_CHARSET.name());
            String urlEncodedValue = URLEncoder.encode(value,
                    IOTHUB_URL_ENCODING_CHARSET.name());
            uriBuilder.append(urlEncodedName);
            uriBuilder.append("=");
            uriBuilder.append(urlEncodedValue);
        }
        catch (UnsupportedEncodingException e)
        {
            // should never happen, since the encoding is hard-coded.
            throw new IllegalStateException(e);
        }
    }

    @SuppressWarnings("SameReturnValue")
    public static String getApiVersionString()
    {
        return API_VERSION;
    }

    @SuppressWarnings("unused")
    protected IotHubUri()
    {

    }
}
