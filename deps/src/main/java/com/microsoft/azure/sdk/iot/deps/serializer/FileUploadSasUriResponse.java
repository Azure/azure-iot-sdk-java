// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

/**
 * The information provided from IoT Hub that can be used with the Azure Storage SDK to upload a file from your device, including authentication.
 */
public class FileUploadSasUriResponse
{
    private static final String HTTPS_URL_STRING = "https://";
    private static final String UTF_8_STRING = "UTF-8";

    private static final String CORRELATION_ID_TAG = "correlationId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CORRELATION_ID_TAG)
    private String correlationId = null;

    private static final String HOST_NAME_TAG = "hostName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(HOST_NAME_TAG)
    private String hostName = null;

    private static final String CONTAINER_NAME_TAG = "containerName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(CONTAINER_NAME_TAG)
    private String containerName = null;

    private static final String BLOB_NAME_TAG = "blobName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(BLOB_NAME_TAG)
    private String blobName = null;

    private static final String SAS_TOKEN_TAG = "sasToken";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(SAS_TOKEN_TAG)
    private String sasToken = null;

    /**
     * Create an instance of the FileUploadSasUriResponse using the information in the provided json.
     *
     * @param json is the string that contains a valid json with the FileUpload response.
     * @throws IllegalArgumentException if the json is null, empty, or not valid.
     */
    public FileUploadSasUriResponse(String json) throws IllegalArgumentException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        FileUploadSasUriResponse newFileUploadSasUriResponse;

        ParserUtility.validateStringUTF8(json);
        try
        {
            newFileUploadSasUriResponse = gson.fromJson(json, FileUploadSasUriResponse.class);
        }
        catch (JsonSyntaxException malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.hostName = newFileUploadSasUriResponse.hostName;
        this.containerName = newFileUploadSasUriResponse.containerName;
        this.correlationId = newFileUploadSasUriResponse.correlationId;
        this.blobName = newFileUploadSasUriResponse.blobName;
        this.sasToken = newFileUploadSasUriResponse.sasToken;
    }

    /**
     * Getter for the Azure storage correlation identification. This correlation id should be used when notifying IoT Hub
     * that this file upload has completed.
     *
     * @return The correlation identification.
     */
    public String getCorrelationId()
    {
        return this.correlationId;
    }

    /**
     * Getter for the Azure storage host name that this file upload SAS URI allows uploading to.
     *
     * @return The host name.
     */
    public String getHostName()
    {
        return this.hostName;
    }

    /**
     * Getter for the container name in Azure storage that this file upload SAS URI allows uploading to.
     *
     * @return The container name.
     */
    public String getContainerName()
    {
        return this.containerName;
    }

    /**
     * Getter for the blob name (aka file name) in Azure storage that this file upload SAS URI allows uploading to.
     *
     * @return The blob name.
     */
    public String getBlobName()
    {
        return this.blobName;
    }

    /**
     * Getter for the file sasToken for authenticating with Azure storage while uploading.
     *
     * @return String with the file sasToken.
     */
    public String getSasToken()
    {
        return this.sasToken;
    }

    /**
     * Get the full Azure Storage blob uri to upload a file to. This uri includes authentication information
     * @return The full Azure Storage blob uri to upload a file to.
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported on this device.
     * @throws URISyntaxException if building the URI fails for any reason.
     */
    public URI getBlobUri() throws UnsupportedEncodingException, URISyntaxException
    {
        String putString = HTTPS_URL_STRING +
                hostName + "/" +
                containerName + "/" +
                URLEncoder.encode(blobName, UTF_8_STRING) + // Pass URL encoded blob name to support special characters
                sasToken;

        return new URI(putString);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    FileUploadSasUriResponse()
    {
    }
}
