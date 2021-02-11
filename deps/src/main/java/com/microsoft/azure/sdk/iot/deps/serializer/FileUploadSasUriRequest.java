// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The request parameters when getting a file upload sas uri from IoT Hub.
 */
public class FileUploadSasUriRequest
{
    private static final String BLOB_NAME_TAG = "blobName";
    @Expose
    @SerializedName(BLOB_NAME_TAG)
    private String blobName;

    /**
     * Create an instance of the FileUploadSasUriRequest based on the provided blobName.
     *
     * @param blobName is the name of the blob (file name in the blob) that IoT Hub will make Azure Storage
     *                 create for this file upload. This field is mandatory
     */
    public FileUploadSasUriRequest(String blobName)
    {
        this.blobName = blobName;
    }

    /**
     * @return the blob name that will be created in Azure Storage to upload to.
     */
    public String getBlobName()
    {
        return blobName;
    }

    /**
     * Set the blob name that will be created in Azure Storage to upload to.
     */
    public void setBlobName(String blobName)
    {
        this.blobName = blobName;
    }

    /**
     * Convert this class to json.
     *
     * @return json that represents the content of this class.
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

        return gson.toJson(this);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    FileUploadSasUriRequest()
    {
    }
}
