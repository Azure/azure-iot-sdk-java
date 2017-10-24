/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract;

import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.SDKUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class UrlPathBuilder
{
    private String scope;
    private ProvisioningDeviceClientTransportProtocol provisioningDeviceClientTransportProtocol;
    private StringBuilder url;

    private static final String SLASH = "/";
    private static final String QUESTION = "?";
    private static final String EQUALS = "=";

    /*
     * Service Contract specific strings
     */
    private static final String URL_HTTPS = "https:" + SLASH + SLASH;
    private static final String REGISTRATIONS = "registrations";
    private static final String REGISTER = "register";
    private static final String OPERATIONS = "operations";
    private static final String API_VERSION_STRING = "api-version";

    /**
     * Constructor for Url Path builder
     * @param scopeId scope id for Provisioning service which cannot be {@code null} or empty
     * @throws IllegalArgumentException is thrown when invalid parameters are given
     */
    public UrlPathBuilder(String scopeId) throws IllegalArgumentException
    {
        //SRS_UrlPathBuilder_25_002: [ Constructor throw IllegalArgumentException if scope id is null or empty.]
        if (scopeId == null || scopeId.isEmpty())
        {
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }
        //SRS_UrlPathBuilder_25_001: [ Constructor shall save scope id.]
        this.scope = scopeId;
    }

    /**
     * Constructor for Url Path builder
     * @param hostName HostName for Provisioning service which cannot be {@code null} or empty
     * @param scopeId scope id for Provisioning service which cannot be {@code null} or empty
     * @param protocol One of the valid protocols. Cannot be {@code null}
     * @throws IllegalArgumentException is thrown when invalid parameters are given
     */
    public UrlPathBuilder(String hostName, String scopeId, ProvisioningDeviceClientTransportProtocol protocol) throws IllegalArgumentException
    {
        //SRS_UrlPathBuilder_25_003: [ The constructor shall throw IllegalArgumentException if the scope id or hostName string is empty or null or if protocol is null.]
        if (hostName == null || hostName.isEmpty())
        {
            throw new IllegalArgumentException("host name cannot be null or empty");
        }

        if (scopeId == null || scopeId.isEmpty())
        {
            throw new IllegalArgumentException("scope id cannot be null or empty");
        }

        if (protocol == null)
        {
            throw new IllegalArgumentException("protocol cannot be null");
        }

        //SRS_UrlPathBuilder_25_004: [ The constructor shall save the scope id or hostName string and protocol. ]
        this.scope = scopeId;
        this.provisioningDeviceClientTransportProtocol = protocol;
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
        registerUrl.append(REGISTER);
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

    /**
     * Generates URL Encoded SAS Token
     * @param registrationId Id for the registration. Cannot be {@code null} or empty
     * @return A string of format
     * @throws UnsupportedEncodingException if the string could not be encoded.
     */
    public String generateSasTokenUrl(String registrationId) throws UnsupportedEncodingException
    {
        //SRS_UrlPathBuilder_25_005: [ This method shall throw IllegalArgumentException if the registration id is null or empty. ]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }
        //SRS_UrlPathBuilder_25_006: [ This method shall create a String using the following format after Url Encoding: <scopeid>/registrations/<registrationId> ]
        StringBuilder sasTokenUrl = new StringBuilder();
        sasTokenUrl.append(scope);
        sasTokenUrl.append(SLASH);
        sasTokenUrl.append(REGISTRATIONS);
        sasTokenUrl.append(SLASH);
        sasTokenUrl.append(registrationId);
        return URLEncoder.encode(sasTokenUrl.toString(), StandardCharsets.UTF_8.displayName());
    }

    /**
     * Generates Register URL/Path for the specified protocol
     * @param registrationId Id for the registration. Cannot be {@code null} or empty
     * @return A string of format for the specified protocol
     * @throws IOException If string could not be generated
     */
    public String generateRegisterUrl(String registrationId) throws IOException
    {
        //SRS_UrlPathBuilder_25_007: [ This method shall throw IllegalArgumentException if the registration id is null or empty. ]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }

        //SRS_UrlPathBuilder_25_008: [ This method shall create a String using the following format: HTTP - https://<HostName>/<Scope>/registrations/<Registration ID>/register?api-version=<Service API Version> MQTT - TBD AMQP - TBD ]
        switch (provisioningDeviceClientTransportProtocol)
        {
            case HTTPS:
                return generateRegisterUrlHttp(registrationId);

            case MQTT:
            case MQTT_WS:
                return null;

            case AMQPS:
            case AMQPS_WS:
                return null;

            default:
                throw new IOException("Unspecified protocol");
        }
    }

    /**
     * Generates Request URL/Path for the specififed protocol
     * @param registrationId Id for the registration. Cannot be {@code null} or empty
     * @param operationsId Id for the Operation. Cannot be {@code null} or empty
     * @return A string of format for the specified protocol
     * @throws IOException If string could not be generated
     */
    public String generateRequestUrl(String registrationId, String operationsId) throws IOException
    {
        //SRS_UrlPathBuilder_25_009: [ This method shall throw IllegalArgumentException if the registration id or operation id is null or empty. ]
        if (registrationId == null || registrationId.isEmpty())
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }

        if (operationsId == null || operationsId.length() == 0)
        {
            throw new IllegalArgumentException("registration id cannot be null or empty");
        }

        //SRS_UrlPathBuilder_25_010: [ This method shall create a String using the following format: HTTP - https://<HostName>/<Scope>/registrations/<Registration ID>/operations/<operationId>?api-version=<Service API Version> MQTT - TBD AMQP - TBD ]
        switch (provisioningDeviceClientTransportProtocol)
        {
            case HTTPS:
                return generateRequestUrlHttp(registrationId, operationsId);

            case MQTT:
            case MQTT_WS:
                return null;

            case AMQPS:
            case AMQPS_WS:
                return null;

            default:
                throw new IOException("Unspecified protocol");
        }
    }
}
