// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Representation of a single File Upload request with a Json serializer.
 * Ex of JSON format:
 *  {
 *      "blobName": "{name of the file for which a SAS URI will be generated}"
 *  }
 */
public class FileUploadRequestParser
{
    private static final String BLOB_NAME_TAG = "blobName";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(BLOB_NAME_TAG)
    private String blobName;

    /**
     * CONSTRUCTOR
     * Create an instance of the FileUploadRequestParser based on the provided blobName.
     *
     * @param blobName is the name of the blob (file name in the blob)
     * @throws IllegalArgumentException if the blobName is null, empty, or not valid.
     */
    public FileUploadRequestParser(String blobName) throws IllegalArgumentException
    {
        /* Codes_SRS_FILE_UPLOAD_REQUEST_21_001: [The constructor shall create an instance of the FileUploadRequestParser.] */
        /* Codes_SRS_FILE_UPLOAD_REQUEST_21_003: [If the provided blob name is null, empty, or not valid, the constructor shall throws IllegalArgumentException.] */
        ParserUtility.validateBlobName(blobName);

        /* Codes_SRS_FILE_UPLOAD_REQUEST_21_002: [The constructor shall set the `blobName` in the new class with the provided blob name.] */
        this.blobName = blobName;
    }

    /**
     * Convert this class in a valid json.
     *
     * @return a valid json that represents the content of this class.
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

        /* Codes_SRS_FILE_UPLOAD_REQUEST_21_004: [The toJson shall return a string with a json that represents the contend of the FileUploadResponseParser.] */
        return gson.toJson(this);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    protected FileUploadRequestParser()
    {
    }
}
