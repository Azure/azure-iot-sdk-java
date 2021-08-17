// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class JobPropertiesParser
{
    private final transient static Gson gson = new Gson();

    private static final String JOB_ID_NAME = "jobId";
    @Expose
    @SerializedName(JOB_ID_NAME)
    @Setter
    @Getter
    private String jobId;

    private static final String START_TIME_UTC_NAME = "startTimeUtc";
    @Expose
    @SerializedName(START_TIME_UTC_NAME)
    @Setter
    @Getter
    private String startTimeUtcString;
    private transient Date startTimeUtc;

    private static final String END_TIME_UTC_NAME = "endTimeUtc";
    @Expose
    @SerializedName(END_TIME_UTC_NAME)
    @Setter
    private String endTimeUtcString;
    private transient Date endTimeUtc;

    private static final String TYPE_NAME = "type";
    @Expose
    @SerializedName(TYPE_NAME)
    @Setter
    @Getter
    private String type;

    private static final String STATUS_NAME = "status";
    @Expose
    @SerializedName(STATUS_NAME)
    @Setter
    @Getter
    private String status;

    private static final String PROGRESS_NAME = "progress";
    @Expose
    @SerializedName(PROGRESS_NAME)
    @Setter
    @Getter
    private int progress;

    private static final String INPUT_BLOB_CONTAINER_URI_NAME = "inputBlobContainerUri";
    @Expose
    @SerializedName(INPUT_BLOB_CONTAINER_URI_NAME)
    @Setter
    @Getter
    private String inputBlobContainerUri;

    private static final String OUTPUT_BLOB_CONTAINER_URI_NAME = "outputBlobContainerUri";
    @Expose
    @SerializedName(OUTPUT_BLOB_CONTAINER_URI_NAME)
    @Setter
    @Getter
    private String outputBlobContainerUri;

    private static final String EXCLUDE_KEYS_IN_EXPORT_NAME = "excludeKeysInExport";
    @Expose
    @SerializedName(EXCLUDE_KEYS_IN_EXPORT_NAME)
    @Setter
    @Getter
    private boolean excludeKeysInExport;

    private static final String FAILURE_REASON_NAME = "failureReason";
    @Expose
    @SerializedName(FAILURE_REASON_NAME)
    @Setter
    @Getter
    private String failureReason;

    private static final String STORAGE_AUTHENTICATION_TYPE = "storageAuthenticationType";
    @Expose
    @SerializedName(STORAGE_AUTHENTICATION_TYPE)
    @Setter
    @Getter
    private StorageAuthenticationType storageAuthenticationType;

    private static final String IDENTITY = "identity";
    @Expose
    @SerializedName(IDENTITY)
    @Setter
    @Getter
    private ManagedIdentity identity;

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    public JobPropertiesParser()
    {
    }

    /**
     * Constructor for a JobPropertiesParser object that is built from the provided Json
     * @param json the json to build the JobPropertiesParser from
     * @throws IllegalArgumentException if the provided Json is null, empty, cannot be parsed,
     * or if the provided Json is missing any of the type, inputBlobContainerUri or outputBlobContainerUri fields
     */
    public JobPropertiesParser(String json) throws IllegalArgumentException
    {
        if (json == null || json.isEmpty())
        {
            //Codes_SRS_JOB_PROPERTIES_PARSER_34_007: [If the provided json is null or empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json cannot be null or empty");
        }

        JobPropertiesParser parser;
        try
        {
            parser = gson.fromJson(json, JobPropertiesParser.class);
        }
        catch (JsonSyntaxException e)
        {
            //Codes_SRS_JOB_PROPERTIES_PARSER_34_008: [If the provided json cannot be parsed into a JobPropertiesParser object, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json could not be parsed");
        }

        if (parser.getJobId() == null || parser.getJobId().isEmpty())
        {
            //Codes_SRS_JOB_PROPERTIES_PARSER_34_009: [If the provided json is missing the field for jobId, or if its value is null or empty, an IllegalArgumentException shall be thrown.]
            throw new IllegalArgumentException("The provided json is missing the jobId field");
        }

        this.inputBlobContainerUri = parser.inputBlobContainerUri;
        this.type = parser.type;
        this.status = parser.status;
        this.jobId = parser.jobId;
        this.excludeKeysInExport = parser.excludeKeysInExport;
        this.progress = parser.progress;
        this.outputBlobContainerUri = parser.outputBlobContainerUri;
        this.failureReason = parser.failureReason;
        this.storageAuthenticationType = parser.storageAuthenticationType;
        this.identity  = parser.identity;

        if (parser.endTimeUtcString != null)
        {
            this.endTimeUtcString = parser.endTimeUtcString;
            this.endTimeUtc = ParserUtility.getDateTimeUtc(parser.endTimeUtcString);
        }

        if (parser.startTimeUtcString != null)
        {
            this.startTimeUtcString = parser.startTimeUtcString;
            this.startTimeUtc = ParserUtility.getDateTimeUtc(parser.startTimeUtcString);
        }
    }

    /**
     * Converts this into json and returns it
     * @return the json representation of this
     */
    public String toJson()
    {
        if (this.startTimeUtc != null)
        {
            this.startTimeUtcString = ParserUtility.getDateStringFromDate(this.startTimeUtc);
        }

        if (this.endTimeUtc != null)
        {
            this.endTimeUtcString = ParserUtility.getDateStringFromDate(this.endTimeUtc);
        }

        return gson.toJson(this);
    }

    /**
     * Setter for StartTimeUtc
     *
     * @param startTimeUtc the value to set StartTimeUtc to
     */
    public void setStartTimeUtc(Date startTimeUtc)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_019: [This method shall set the value of this object's startTimeUtc equal to the provided value.]
        this.startTimeUtc = startTimeUtc;

        if (startTimeUtc == null)
        {
            this.startTimeUtcString = null;
        }
        else
        {
            this.startTimeUtcString = ParserUtility.getDateStringFromDate(startTimeUtc);
        }
    }

    /**
     * Setter for EndTimeUtc
     *
     * @param endTimeUtc the value to set EndTimeUtc to
     */
    public void setEndTimeUtc(Date endTimeUtc)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_021: [This method shall set the value of this object's endTimeUtc equal to the provided value.]
        this.endTimeUtc = endTimeUtc;

        if (endTimeUtc == null)
        {
            this.endTimeUtcString = null;
        }
        else
        {
            this.endTimeUtcString = ParserUtility.getDateStringFromDate(endTimeUtc);
        }
    }
}
