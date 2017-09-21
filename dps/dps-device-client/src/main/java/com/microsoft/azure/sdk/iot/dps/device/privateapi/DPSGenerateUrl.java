/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi;

import com.microsoft.azure.sdk.iot.dps.device.DPSTransportProtocol;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DPSGenerateUrl
{
    private String scope;
    private DPSTransportProtocol DPSTransportProtocol;
    private StringBuilder url;

    private static final String SLASH = "/";
    private static final String QUESTION = "?";
    private static final String EQUALS = "=";

    /*
     * DPS specific details
     */
    private static final String URL_HTTPS = "https:" + SLASH + SLASH;
    private static final String REGISTRATIONS = "registrations";
    private static final String REGISTER_ME = "register";
    private static final String OPERATIONS = "operations";
    private static final String API_VERSION_STRING = "api-version";

    public DPSGenerateUrl(String scopeID) throws IllegalArgumentException
    {
        if (scopeID == null || scopeID.length() == 0)
        {
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }
        this.scope = scopeID;
    }

    public DPSGenerateUrl(String hostName, String scopeID, DPSTransportProtocol protocol) throws IllegalArgumentException
    {
        if (hostName == null || hostName.length() == 0)
        {
            throw new IllegalArgumentException("host name cannot be null or empty");
        }

        if (scopeID == null || scopeID.length() == 0)
        {
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("protocol cannot be null");
        }

        this.scope = scopeID;
        this.DPSTransportProtocol = protocol;
        url = new StringBuilder();
        url.append(URL_HTTPS);
        url.append(hostName);
        url.append(SLASH);
        url.append(this.scope);
        url.append(SLASH);
        url.append(REGISTRATIONS);
        url.append(SLASH);
    }

    private String generateRegisterUrlHttp(String registrationId)
    {
        StringBuilder registerUrl = new StringBuilder(url);

        registerUrl.append(registrationId);
        registerUrl.append(SLASH);
        registerUrl.append(REGISTER_ME);
        registerUrl.append(QUESTION);
        registerUrl.append(API_VERSION_STRING);
        registerUrl.append(EQUALS);
        registerUrl.append(SDKUtils.getServiceApiVersion());

        return registerUrl.toString();
    }

    private String generateRequestUrlHttp(String registrationId, String operationsId)
    {
        StringBuilder requestUrl = new StringBuilder(url);

        requestUrl.append(registrationId);
        requestUrl.append(SLASH);
        requestUrl.append(OPERATIONS);
        requestUrl.append(SLASH);
        requestUrl.append(operationsId);

        requestUrl.append(QUESTION);
        requestUrl.append(API_VERSION_STRING);
        requestUrl.append(EQUALS);
        requestUrl.append(SDKUtils.getServiceApiVersion());

        return requestUrl.toString();
    }

    public String generateSasTokenUrl(String registrationId) throws UnsupportedEncodingException
    {
        // idscope/registrations/registrationid/
        StringBuilder sasTokenUrl = new StringBuilder();
        sasTokenUrl.append(scope);
        sasTokenUrl.append(SLASH);
        sasTokenUrl.append(REGISTRATIONS);
        sasTokenUrl.append(SLASH);
        sasTokenUrl.append(registrationId);
        //sasTokenUrl.append(SLASH);
        return URLEncoder.encode(sasTokenUrl.toString(), StandardCharsets.UTF_8.displayName());
    }

    public String generateRegisterUrl(String registrationId) throws IOException
    {
        if (registrationId == null || registrationId.length() == 0)
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }

        switch (DPSTransportProtocol)
        {
            case HTTPS:
                return generateRegisterUrlHttp(registrationId);

            case MQTT:
                return null;

            case AMQPS:
                return null;

            default:
                throw new IOException("Unspecified protocol");
        }
    }

    public String generateRequestUrl(String registrationId, String operationsId) throws IOException
    {
        if (registrationId == null || registrationId.length() == 0)
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }

        if (operationsId == null || operationsId.length() == 0)
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }

        switch (DPSTransportProtocol)
        {
            case HTTPS:
                return generateRequestUrlHttp(registrationId, operationsId);

            case MQTT:
                return null;

            case AMQPS:
                return null;

            default:
                throw new IOException("Unspecified protocol");
        }
    }
}
