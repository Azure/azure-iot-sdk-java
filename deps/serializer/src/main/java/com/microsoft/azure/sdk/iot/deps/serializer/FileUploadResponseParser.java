// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Representation of a single container for the File Upload response with a Json deserializer.
 * Ex of JSON format:
 *  {
 *      "correlationId": "somecorrelationid",
 *      "hostname": "contoso.azure-devices.net",
 *      "containerName": "testcontainer",
 *      "blobName": "test-device1/image.jpg",
 *      "sasToken": "1234asdfSAStoken"
 *  }
 */
public class FileUploadResponseParser
{
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
     * CONSTRUCTOR
     * Create an instance of the FileUploadResponseParser using the information in the provided json.
     *
     * @param json is the string that contains a valid json with the FileUpload response.
     * @throws IllegalArgumentException if the json is null, empty, or not valid.
     */
    public FileUploadResponseParser(String json) throws IllegalArgumentException
    {
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_001: [The constructor shall create an instance of the FileUploadResponseParser.] */
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        FileUploadResponseParser newFileUploadResponseParser;

        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_003: [If the provided json is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
        ParserUtility.validateStringUTF8(json);
        try
        {
            newFileUploadResponseParser = gson.fromJson(json, FileUploadResponseParser.class);
        }
        catch (Exception malformed)
        {
            throw new IllegalArgumentException("Malformed json:" + malformed);
        }

        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_004: [If the provided json do not contains a valid `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken`, the constructor shall throws IllegalArgumentException.] */
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_005: [If the provided json do not contains one of the keys `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken`, the constructor shall throws IllegalArgumentException.] */
        ParserUtility.validateStringUTF8(newFileUploadResponseParser.hostName);
        ParserUtility.validateStringUTF8(newFileUploadResponseParser.containerName);
        ParserUtility.validateStringUTF8(newFileUploadResponseParser.correlationId);
        ParserUtility.validateBlobName(newFileUploadResponseParser.blobName);
        ParserUtility.validateStringUTF8(newFileUploadResponseParser.sasToken);

        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_002: [The constructor shall parse the provided json and initialize `correlationId`, `hostName`, `containerName`, `blobName`, and `sasToken` using the information in the json.] */
        this.hostName = newFileUploadResponseParser.hostName;
        this.containerName = newFileUploadResponseParser.containerName;
        this.correlationId = newFileUploadResponseParser.correlationId;
        this.blobName = newFileUploadResponseParser.blobName;
        this.sasToken = newFileUploadResponseParser.sasToken;
    }

    /**
     * Getter for the Azure storage correlation identification.
     *
     * @return string with the correlation identification.
     */
    public String getCorrelationId()
    {
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_005: [The getCorrelationId shall return the string stored in `correlationId`.] */
        return this.correlationId;
    }

    /**
     * Getter for the Azure storage host name.
     *
     * @return string with the host name.
     */
    public String getHostName()
    {
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_006: [The getHostName shall return the string stored in `hostName`.] */
        return this.hostName;
    }

    /**
     * Getter for the container name in the Azure storage.
     *
     * @return string with the container name.
     */
    public String getContainerName()
    {
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_007: [The getContainerName shall return the string stored in `containerName`.] */
        return this.containerName;
    }

    /**
     * Getter for the file name (blob name).
     *
     * @return string with the file name.
     */
    public String getBlobName()
    {
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_008: [The getBlobName shall return the string stored in `blobName`.] */
        return this.blobName;
    }

    /**
     * Getter for the file sasToken.
     *
     * @return String with the file sasToken.
     */
    public String getSasToken()
    {
        /* Codes_SRS_FILE_UPLOAD_RESPONSE_21_009: [The getSasToken shall return the string stored in `sasToken`.] */
        return this.sasToken;
    }
}
