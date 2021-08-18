// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The request payload to send to IoT Hub to notify it when a file upload is completed, whether successful or not.
 * Must set {@link #setIsSuccess(Boolean)} and {@link #setCorrelationId(String)}, but all other fields are optional.
 */
public class FileUploadCompletionNotification
{
    private static final String CORRELATION_ID_TAG = "correlationId";
    @Expose
    @SerializedName(CORRELATION_ID_TAG)
    private String correlationId = null;

    private static final String IS_SUCCESS_TAG = "isSuccess";
    @Expose
    @SerializedName(IS_SUCCESS_TAG)
    private Boolean isSuccess = null;

    private static final String STATUS_CODE_TAG = "statusCode";
    @Expose
    @SerializedName(STATUS_CODE_TAG)
    private Integer statusCode = null;

    private static final String STATUS_DESCRIPTION_TAG = "statusDescription";
    @Expose
    @SerializedName(STATUS_DESCRIPTION_TAG)
    private String statusDescription = null;

    /**
     * Create an instance of the FileUploadCompletionNotification for a single file upload operation using Azure Storage.
     *
     * @param correlationId the correlationId that correlates this FileUploadCompletionNotification to the earlier request to get the SAS URI
     *                      for this upload from IoT Hub. This field is mandatory. Must equal {@link FileUploadSasUriResponse#getCorrelationId()}.
     * @param isSuccess whether the file was uploaded successfully. This field is mandatory.
     */
    public FileUploadCompletionNotification(String correlationId, Boolean isSuccess)
    {
        setCorrelationId(correlationId);
        updateStatus(isSuccess, statusCode, statusDescription);
    }

    /**
     * Create an instance of the FileUploadCompletionNotification for a single file upload operation using Azure Storage.
     *
     * @param correlationId the correlationId that correlates this FileUploadCompletionNotification to the earlier request to get the SAS URI
     *                      for this upload from IoT Hub. This field is mandatory. Must equal {@link FileUploadSasUriResponse#getCorrelationId()}.
     * @param isSuccess whether the file was uploaded successfully. This field is mandatory.
     * @param statusCode is the status for the upload of the file to storage.
     * @param statusDescription is the description of the status code.
     */
    public FileUploadCompletionNotification(String correlationId, Boolean isSuccess, Integer statusCode, String statusDescription)
    {
        setCorrelationId(correlationId);
        updateStatus(isSuccess, statusCode, statusDescription);
    }

    /**
     * Construct this notification with json
     * @param json the json to parse.
     */
    private FileUploadCompletionNotification(String json)
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
        FileUploadCompletionNotification fileUploadCompletionNotification;

        try
        {
            fileUploadCompletionNotification = gson.fromJson(json, FileUploadCompletionNotification.class);
        }
        catch (Exception malformed)
        {
            throw new IllegalArgumentException("Malformed json", malformed);
        }

        this.correlationId = fileUploadCompletionNotification.getCorrelationId();
        this.isSuccess = fileUploadCompletionNotification.isSuccess();
        this.statusCode = fileUploadCompletionNotification.getStatusCode();
        this.statusDescription = fileUploadCompletionNotification.getStatusDescription();
    }

    private void updateStatus(Boolean isSuccess, Integer statusCode, String statusDescription) throws IllegalArgumentException
    {
        this.isSuccess = isSuccess;
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
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
     * Set the correlationId that correlates this FileUploadCompletionNotification to the earlier request to get the SAS URI
     * for this upload from IoT Hub. Must equal {@link FileUploadSasUriResponse#getCorrelationId()}.
     * @param correlationId the unique identifier that correlates this file upload status to a SAS URI that IoT Hub retreived from Azure Storage earlier.
     */
    public void setCorrelationId(String correlationId)
    {
        this.correlationId = correlationId;
    }

    /**
     * @return the correlationId that correlates this FileUploadCompletionNotification to the earlier request to get the SAS URI
     */
    public String getCorrelationId()
    {
        return this.correlationId;
    }

    /**
     * @return Get if the file upload was successful
     */
    public Boolean isSuccess()
    {
        return this.isSuccess;
    }

    /**
     * Set if the file upload was a success
     * @param success true if the file upload was a success. False otherwise.
     */
    public void setSuccess(Boolean success)
    {
        this.isSuccess = success;
    }

    /**
     * @return get the status code associated with this file upload.
     */
    public Integer getStatusCode()
    {
        return this.statusCode;
    }

    /**
     * Set the status code associated with this file upload request
     * @param statusCode The status code associated with this file upload request
     */
    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }

    /**
     * @return get the status description associated with this file upload.
     */
    public String getStatusDescription()
    {
        return this.statusDescription;
    }

    /**
     * Set the status description associated with this file upload request
     * @param statusDescription The status description associated with this file upload request
     */
    public void setStatusDescription(String statusDescription)
    {
        this.statusDescription = statusDescription;
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    public FileUploadCompletionNotification()
    {
    }
}
