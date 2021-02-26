/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.devicetwin;

import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.Tools;
import com.microsoft.azure.sdk.iot.service.auth.IotHubServiceSasToken;
import com.microsoft.azure.sdk.iot.service.auth.TokenCredentialCache;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;

public class RawTwinQuery
{
    private static final int DEFAULT_PAGE_SIZE = 100;

    private static final Integer DEFAULT_HTTP_READ_TIMEOUT_MS = 24000; // 24 seconds
    private static final Integer DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 24000; // 24 seconds

    private String hostName;
    private TokenCredentialCache credentialCache;
    private AzureSasCredential azureSasCredential;
    private IotHubConnectionString iotHubConnectionString;

    private RawTwinQuery()
    {

    }

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of RawTwinQuery
     * @throws IOException This exception is never thrown.
     * @deprecated because this method declares a thrown IOException even though it never throws an IOException. Users
     * are recommended to use {@link #RawTwinQuery(String)} instead
     * since it does not declare this exception even though it constructs the same RawTwinQuery.
     */
    @Deprecated
    public static RawTwinQuery createFromConnectionString(String connectionString) throws IOException
    {
        return new RawTwinQuery(connectionString);
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of RawTwinQuery
     */
    public RawTwinQuery(String connectionString)
    {
        if (Tools.isNullOrEmpty(connectionString))
        {
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        this.iotHubConnectionString = IotHubConnectionStringBuilder.createIotHubConnectionString(connectionString);
        this.hostName = this.iotHubConnectionString.getHostName();
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param credential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed. The provided tokens must be Json Web Tokens.
     */
    public RawTwinQuery(String hostName, TokenCredential credential)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(credential);

        this.hostName = hostName;
        this.credentialCache = new TokenCredentialCache(credential);
    }

    /**
     * Constructor to create instance from connection string
     *
     * @param hostName The hostname of your IoT Hub instance (For instance, "your-iot-hub.azure-devices.net")
     * @param azureSasCredential The custom {@link TokenCredential} that will provide authentication tokens to
     *                                    this library when they are needed.
     */
    public RawTwinQuery(String hostName, AzureSasCredential azureSasCredential)
    {
        if (Tools.isNullOrEmpty(hostName))
        {
            throw new IllegalArgumentException("hostName cannot be null or empty");
        }

        Objects.requireNonNull(azureSasCredential);

        this.hostName = hostName;
        this.azureSasCredential = azureSasCredential;
    }

    /**
     * Creates a query object for this query
     * @param sqlQuery Sql style query for Raw data over twin
     * @param pageSize Size to restrict response of query by
     * @return Object for the query
     * @throws IotHubException If IotHub did not respond successfully to the query
     * @throws IOException If any of the input parameters are incorrect
     */
    public synchronized Query query(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        if (pageSize <= 0)
        {
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        Query rawQuery = new Query(sqlQuery, pageSize, QueryType.RAW);

        rawQuery.sendQueryRequest(
                this.credentialCache,
                this.azureSasCredential,
                this.iotHubConnectionString,
                IotHubConnectionString.getUrlTwinQuery(this.hostName),
                HttpMethod.POST,
                DEFAULT_HTTP_CONNECT_TIMEOUT_MS,
                DEFAULT_HTTP_READ_TIMEOUT_MS,
                null);

        return rawQuery;
    }

    /**
     * Creates a query object for this query using default page size
     * @param sqlQuery Sql style query for Raw data over twin
     * @return Object for the query
     * @throws IotHubException If IotHub did not respond successfully to the query
     * @throws IOException If any of the input parameters are incorrect
     */
    public synchronized Query query(String sqlQuery) throws IotHubException, IOException
    {
        return this.query(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * Returns the availability of next element in response. Sends the request again (if possible)
     * to retrieve response until no response is found.
     * @param query Object corresponding to the query
     * @return True if available and false otherwise
     * @throws IotHubException If IotHub could not respond successfully to the query request
     * @throws IOException If any of the input parameters are incorrect
     */
    public synchronized boolean hasNext(Query query) throws IotHubException, IOException
    {
        if (query == null)
        {
            throw new IllegalArgumentException("Query cannot be null");
        }

        return query.hasNext();
    }

    /**
     * Returns the next json element available in response
     * @param query Object corresponding for this query
     * @return Next json element as a response to this query
     * @throws IOException If any of input parameters are incorrect
     * @throws IotHubException If IotHub could not respond successfully to the query request
     * @throws NoSuchElementException If no other element is found
     */
    public synchronized String next(Query query) throws IOException, IotHubException, NoSuchElementException
    {
        if (query == null)
        {
            throw new IllegalArgumentException();
        }

        Object nextObject = query.next();

        if (nextObject instanceof String)
        {
            return (String) nextObject;
        }
        else
        {
            throw new IOException("Received a response that could not be parsed");
        }
    }

    private String getAuthenticationToken()
    {
        // Three different constructor types for this class, and each type provides either a TokenCredential implementation,
        // an AzureSasCredential instance, or just the connection string. The sas token can be retrieved from the non-null
        // one of the three options.
        if (this.credentialCache != null)
        {
            return this.credentialCache.getAccessToken().getToken();
        }
        else if (this.azureSasCredential != null)
        {
            return this.azureSasCredential.getSignature();
        }

        return new IotHubServiceSasToken(iotHubConnectionString).toString();
    }
}
