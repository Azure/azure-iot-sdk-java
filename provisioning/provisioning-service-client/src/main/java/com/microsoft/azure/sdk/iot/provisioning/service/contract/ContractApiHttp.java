// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.contract;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpRequest;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.ProvisioningSasToken;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientExceptionManager;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * This client handles the Device Provisioning Service HTTP communication.
 *
 * <p> This class implements the HTTPS contract between the Provisioning Service Client and the
 *     Device Provisioning Service. It is called by the Managers that implement the Provisioning
 *     Service Client public APIs. To access the public APIs, please see the
 *     {@link ProvisioningServiceClient}.
 *
 * <p> The follow diagram describe the relation between these 3 layers of the Service Client:
 *
 * <pre>
 * {@code
 *           +-------------------------------------------------------------------+           +------------+
 *           |                        ProvisioningServiceClient                  |           |    Query   |
 *           +-----+----------------------------+--------------------------+-----+           +--+---+-----+
 *                /                             |                           \                   |   |
 *               /                              |                            \                  |   |
 * +------------+----------------+  +-----------+------------+  +-------------+-------------+   |   |
 * | IndividualEnrollmentManager |  | EnrollmentGroupManager |  | RegistrationStatusManager |   |   |
 * +------------+--------+-------+  +-----------+------+-----+  +-------------+-------+-----+   |   |
 *              |         \                     |       \                     |        \        |   |
 *              |          +-----------------------------+------------------------------+-------+   |
 *              |                               |                             |                     |
 * +------------+-------------------------------+-----------------------------+---------------------+---+
 * |                                       ContractApiHttp                                              |
 * +------------------------------------------------+---------------------------------------------------+
 *                                                  |
 *                                                  |
 *            +-------------------------------------+------------------------------------------+
 *            |                 com.microsoft.azure.sdk.iot.deps.transport.http                |
 *            +--------------------------------------------------------------------------------+
 * }
 * </pre>
 *
 */
public class ContractApiHttp
{
    private static final Integer DEFAULT_HTTP_TIMEOUT_MS = 24000;
    private static final String URL_SEPARATOR_0 = "/";
    private static final String URL_SEPARATOR_1 = "?";
    private static final String URL_HTTPS = "https:" + URL_SEPARATOR_0 + URL_SEPARATOR_0;
    private static final String URL_API_VERSION = "api-version=";

    private static final String HEADER_FIELD_NAME_AUTHORIZATION = "authorization";
    private static final String HEADER_FIELD_NAME_USER_AGENT = "User-Agent";
    private static final String HEADER_FIELD_NAME_REQUEST_ID = "Request-Id";
    private static final String HEADER_FIELD_NAME_ACCEPT = "Accept";
    private static final String HEADER_FIELD_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_FIELD_NAME_CONTENT_LENGTH = "Content-Length";
    private static final String HEADER_FIELD_NAME_CHARSET = "charset";

    private static final String HEADER_FIELD_VALUE_REQUEST_ID = "1001";
    private static final String HEADER_FIELD_VALUE_ACCEPT = "application/json";
    private static final String HEADER_FIELD_VALUE_CONTENT_TYPE = "application/json";
    private static final String HEADER_FIELD_VALUE_CHARSET = "utf-8";

    private ProvisioningConnectionString provisioningConnectionString;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private String hostName;

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param provisioningConnectionString is the Device Provisioning Service service connection string.
     * @throws IllegalArgumentException if there is a problem with the provided connection string.
     */
    private ContractApiHttp(ProvisioningConnectionString provisioningConnectionString)
            throws IllegalArgumentException
    {
        if (provisioningConnectionString == null)
        {
            /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_002: [The constructor shall throw IllegalArgumentException if the connection string is null.] */
            throw new IllegalArgumentException("provisioningConnectionString cannot be null");
        }
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_001: [The constructor shall store the provided connection string.] */
        this.provisioningConnectionString = provisioningConnectionString;
        this.hostName = provisioningConnectionString.getHostName();
    }

    /**
     * Create a new instance of the ContractApiHttp.
     *
     * @param provisioningConnectionString is the Device Provisioning Service service connection string.
     * @return an instance of {@code ContractApiHttp}.
     * @throws IllegalArgumentException if there is a problem with the provided connection string.
     */
    public static ContractApiHttp createFromConnectionString(
            ProvisioningConnectionString provisioningConnectionString)
    {
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_003: [The createFromConnectionString shall throw IllegalArgumentException if the input string is null, threw by the constructor.] */
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_004: [The createFromConnectionString shall create a new ContractApiHttp instance and return it.] */
        return new ContractApiHttp(provisioningConnectionString);
    }

    /**
     * Create a new instance of the ContractApiHttp with a custom {@link TokenCredential} to allow for finer grain control
     * of authentication tokens used in the underlying connection.
     *
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public ContractApiHttp(String hostName, TokenCredential credential)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_002: [The constructor shall throw IllegalArgumentException if the host name is null or empty.] */
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        if (credential == null)
        {
            /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_00: [The constructor shall throw IllegalArgumentException if the TokenCredential is null.] */
            throw new IllegalArgumentException("credential cannot be null");
        }

        this.hostName = hostName;
        this.credentialCache = new TokenCredentialCache(credential);
    }

    /**
     * Create a new instance of the ContractApiHttp with the specifed {@link AzureSasCredential}.
     *
     * @param azureSasCredential The SAS token provider that will be used for authentication.
     */
    public ContractApiHttp(String hostName, AzureSasCredential azureSasCredential)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_00: [The constructor shall throw IllegalArgumentException if the host name is null or empty.] */
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        if (azureSasCredential == null)
        {
            /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_00: [The constructor shall throw IllegalArgumentException if the AzureSasCredential is null.] */
            throw new IllegalArgumentException("azureSasCredential cannot be null");
        }

        this.hostName = hostName;
        this.azureSasCredential = azureSasCredential;
    }

    /**
     * This function sends a raw information to the Device Provisioning Service service using http protocol.
     * <p>
     *    The purpose of this function is be the base communication between the controllers and the
     *    Service, and should be used only if you have full understanding of the Device Provisioning Service rest APIs.
     *    We highly recommend that you uses the APis under <b>{@link ProvisioningServiceClient}</b>
     *    instead of directly access the rest API using this class.
     * </p>
     *
     * @param httpMethod is the http verb in the request (GET, POST, PUT, DELETE, PATCH).
     * @param path is the path to the resource in the service that will compose the URL.
     * @param headerParameters is a list of pairs key values that contains optional parameters in the http header.
     * @param payload is the body of the message.
     * @return the {@code HttpResponse} that contains the response of the request.
     * @throws ProvisioningServiceClientTransportException if the Service Client failed to exchange http messages with the Provisioning Service.
     * @throws ProvisioningServiceClientException if the Provisioning Service response contains an error message.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     */
    public synchronized HttpResponse request(
            HttpMethod httpMethod,
            String path,
            Map<String, String> headerParameters,
            String payload)
            throws ProvisioningServiceClientException
    {
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_007: [The request shall create a HTTP URL based on the Device Registration path.*/
        URL url = getUrlForPath(path);

        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_010: [The request shall create a new HttpRequest.*/
        HttpRequest request = createRequest(url, httpMethod, headerParameters, payload.getBytes(StandardCharsets.UTF_8));

        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_014: [The request shall send the request to the Device Provisioning Service service by using the HttpRequest.send().*/
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_015: [If the HttpRequest failed send the message, the request shall throw ProvisioningServiceClientTransportException, threw by the callee.*/
        HttpResponse httpResponse;
        try
        {
            httpResponse = request.send();
        }
        catch (IOException e)
        {
            throw new ProvisioningServiceClientTransportException(e);
        }

        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_016: [If the Device Provisioning Service service respond to the HttpRequest with any error code, the request shall throw the appropriated ProvisioningServiceClientException, by calling ProvisioningServiceClientExceptionManager.responseVerification().*/
        ProvisioningServiceClientExceptionManager.httpResponseVerification(httpResponse.getStatus(), new String(httpResponse.getErrorReason(), StandardCharsets.UTF_8));

        return httpResponse;
    }

    private HttpRequest createRequest(URL url, HttpMethod method, Map<String, String> headerParameters, byte[] payload) throws ProvisioningServiceClientTransportException
    {
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_011: [If the request get problem creating the HttpRequest, it shall throw ProvisioningServiceClientTransportException.*/
        HttpRequest request;
        try
        {
            request = new HttpRequest(url, method, payload);
        }
        catch (IOException e)
        {
            throw new ProvisioningServiceClientTransportException(e);
        }
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_012: [The request shall fill the http header with the standard parameters.] */
        request.setReadTimeoutMillis(DEFAULT_HTTP_TIMEOUT_MS);
        request.setHeaderField(HEADER_FIELD_NAME_AUTHORIZATION, getAuthenticationToken());
        request.setHeaderField(HEADER_FIELD_NAME_USER_AGENT, SDKUtils.getUserAgentString());
        request.setHeaderField(HEADER_FIELD_NAME_REQUEST_ID, HEADER_FIELD_VALUE_REQUEST_ID);
        request.setHeaderField(HEADER_FIELD_NAME_ACCEPT, HEADER_FIELD_VALUE_ACCEPT);
        request.setHeaderField(HEADER_FIELD_NAME_CONTENT_TYPE, HEADER_FIELD_VALUE_CONTENT_TYPE);
        request.setHeaderField(HEADER_FIELD_NAME_CHARSET, HEADER_FIELD_VALUE_CHARSET);
        request.setHeaderField(HEADER_FIELD_NAME_CONTENT_LENGTH, payload != null ? String.valueOf(payload.length) : "0");

        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_013: [The request shall add the headerParameters to the http header, if provided.] */
        if(headerParameters != null)
        {
            for (Map.Entry<String, String> entry: headerParameters.entrySet())
            {
                request.setHeaderField(entry.getKey(), entry.getValue());
            }
        }

        return request;
    }

    private URL getUrlForPath(String path)
    {
        if(Tools.isNullOrEmpty(path))
        {
            /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_008: [If the provided path is null or empty, the request shall throw IllegalArgumentException.*/
            throw new IllegalArgumentException("path cannot be null or empty");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(URL_HTTPS);
        stringBuilder.append(hostName);
        stringBuilder.append(URL_SEPARATOR_0);
        stringBuilder.append(path);
        stringBuilder.append(URL_SEPARATOR_1);
        stringBuilder.append(URL_API_VERSION);
        stringBuilder.append(SDKUtils.getServiceApiVersion());
        /* SRS_HTTP_DEVICE_REGISTRATION_CLIENT_21_009: [If the provided path contains not valid characters, the request shall throw IllegalArgumentException.*/
        URL url;
        try
        {
            url = new URL(stringBuilder.toString());
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException(e);
        }
        return url;
    }

    private String getAuthenticationToken()
    {
        // Three different constructor types for this class, and each type provides either a TokenCredential implementation,
        // an AzureSasCredential instance, or just the connection string. The sas token can be retrieved from the non-null
        // one of the three options.
        if (this.credentialCache != null)
        {
            return this.credentialCache.getTokenString();
        }
        else if (this.azureSasCredential != null)
        {
            return this.azureSasCredential.getSignature();
        }

        return new ProvisioningSasToken(provisioningConnectionString).toString();
    }
}
