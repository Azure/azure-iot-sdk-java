// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class JobPropertiesParser
{
    private transient static Gson gson = new Gson();

    private static final String JOB_ID_NAME = "jobId";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(JOB_ID_NAME)
    private String jobId;

    private static final String START_TIME_UTC_NAME = "startTimeUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(START_TIME_UTC_NAME)
    private String startTimeUtcString;
    private transient Date startTimeUtc;

    private static final String END_TIME_UTC_NAME = "endTimeUtc";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(END_TIME_UTC_NAME)
    private String endTimeUtcString;
    private transient Date endTimeUtc;

    private static final String TYPE_NAME = "type";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(TYPE_NAME)
    private String type;

    private static final String STATUS_NAME = "status";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(STATUS_NAME)
    private String status;

    private static final String PROGRESS_NAME = "progress";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(PROGRESS_NAME)
    private int progress;

    private static final String INPUT_BLOB_CONTAINER_URI_NAME = "inputBlobContainerUri";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(INPUT_BLOB_CONTAINER_URI_NAME)
    private String inputBlobContainerUri;

    private static final String OUTPUT_BLOB_CONTAINER_URI_NAME = "outputBlobContainerUri";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(OUTPUT_BLOB_CONTAINER_URI_NAME)
    private String outputBlobContainerUri;

    private static final String EXCLUDE_KEYS_IN_EXPORT_NAME = "excludeKeysInExport";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(EXCLUDE_KEYS_IN_EXPORT_NAME)
    private boolean excludeKeysInExport;

    private static final String FAILURE_REASON_NAME = "failureReason";
    @Expose(serialize = true, deserialize = true)
    @SerializedName(FAILURE_REASON_NAME)
    private String failureReason;

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
     * Getter for type
     *
     * @return The value of type
     */
    public String getType()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_012: [This method shall return the value of this object's type.]
        return type;
    }

    /**
     * Getter for inputBlobContainerUri
     *
     * @return The value of inputBlobContainerUri
     */
    public String getInputBlobContainerUri()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_014: [This method shall return the value of this object's inputBlobContainerUri.]
        return inputBlobContainerUri;
    }

    /**
     * Getter for outputBlobContainerUri
     *
     * @return The value of outputBlobContainerUri
     */
    public String getOutputBlobContainerUri()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_016: [This method shall return the value of this object's outputBlobContainerUri.]
        return outputBlobContainerUri;
    }

    /**
     * Getter for jobId
     *
     * @return The value of jobId
     */
    public String getJobId()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_018: [This method shall return the value of this object's jobId.]
        return jobId;
    }

    /**
     * Setter for jobId
     * @param jobId the value to set jobId to
     * @throws IllegalArgumentException if the provided jobId is null
     */
    public void setJobId(String jobId) throws IllegalArgumentException
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_005: [If the provided jobId is null, an IllegalArgumentException shall be thrown.]
        if (jobId == null)
        {
            throw new IllegalArgumentException("jobId cannot be null");
        }

        //Codes_SRS_JOB_PROPERTIES_PARSER_34_010: [This method shall set the value of this object's JobId equal to the provided value.]
        this.jobId = jobId;
    }

    /**
     * Getter for startTimeUtc
     *
     * @return The value of startTimeUtc
     */
    public Date getStartTimeUtc()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_020: [This method shall return the value of this object's startTimeUtc.]
        return startTimeUtc;
    }

    /**
     * Getter for endTimeUtc
     *
     * @return The value of endTimeUtc
     */
    public Date getEndTimeUtc()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_022: [This method shall return the value of this object's endTimeUtc.]
        return endTimeUtc;
    }

    /**
     * Getter for status
     *
     * @return The value of status
     */
    public String getStatus()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_024: [This method shall return the value of this object's status.]
        return status;
    }

    /**
     * Getter for progress
     *
     * @return The value of progress
     */
    public int getProgress()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_026: [This method shall return the value of this object's progress.]
        return progress;
    }

    /**
     * Getter for excludeKeysInExport
     *
     * @return The value of excludeKeysInExport
     */
    public boolean isExcludeKeysInExport()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_028: [This method shall return the value of this object's excludeKeysInExport.]
        return excludeKeysInExport;
    }

    /**
     * Getter for failureReason
     *
     * @return The value of failureReason
     */
    public String getFailureReason()
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_030: [This method shall return the value of this object's failureReason.]
        return failureReason;
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

    /**
     * Setter for Type
     *
     * @param type the value to set Type to
     */
    public void setType(String type)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_011: [This method shall set the value of this object's type equal to the provided value.]
        this.type = type;
    }

    /**
     * Setter for Status
     *
     * @param status the value to set Status to
     */
    public void setStatus(String status)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_023: [This method shall set the value of this object's status equal to the provided value.]
        this.status = status;
    }

    /**
     * Setter for Progress
     *
     * @param progress the value to set Progress to
     */
    public void setProgress(int progress)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_025: [This method shall set the value of this object's progress equal to the provided value.]
        this.progress = progress;
    }

    /**
     * Setter for InputBlobContainerUri
     *
     * @param inputBlobContainerUri the value to set InputBlobContainerUri to
     */
    public void setInputBlobContainerUri(String inputBlobContainerUri)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_013: [This method shall set the value of this object's inputBlobContainerUri equal to the provided value.]
        this.inputBlobContainerUri = inputBlobContainerUri;
    }

    /**
     * Setter for OutputBlobContainerUri
     *
     * @param outputBlobContainerUri the value to set OutputBlobContainerUri to
     */
    public void setOutputBlobContainerUri(String outputBlobContainerUri)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_015: [This method shall set the value of this object's outputBlobContainerUri equal to the provided value.]
        this.outputBlobContainerUri = outputBlobContainerUri;
    }

    /**
     * Setter for ExcludeKeysInExport
     *
     * @param excludeKeysInExport the value to set ExcludeKeysInExport to
     */
    public void setExcludeKeysInExport(boolean excludeKeysInExport)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_027: [This method shall set the value of this object's excludeKeysInExport equal to the provided value.]
        this.excludeKeysInExport = excludeKeysInExport;
    }

    /**
     * Setter for FailureReason
     *
     * @param failureReason the value to set FailureReason to
     */
    public void setFailureReason(String failureReason)
    {
        //Codes_SRS_JOB_PROPERTIES_PARSER_34_029: [This method shall set the value of this object's failureReason equal to the provided value.]
        this.failureReason = failureReason;
    }
}
