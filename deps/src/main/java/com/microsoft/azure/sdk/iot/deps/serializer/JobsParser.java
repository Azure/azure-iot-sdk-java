// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Representation of a single Jobs collection with a Json serializer.
 */
public class JobsParser
{
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String TIMEZONE = "UTC";
    private static final String SCHEDULE_DEVICE_METHOD = "scheduleDeviceMethod";
    private static final String SCHEDULE_UPDATE_TWIN = "scheduleUpdateTwin";

    // Job identifier
    private static final String JOBID_TAG = "jobId";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(JOBID_TAG)
    private String jobId;

    // Required.
    // The type of job to execute.
    private static final String TYPE_TAG = "type";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(TYPE_TAG)
    private String jobType;

    // Required if jobType is cloudToDeviceMethod.
    // The method type and parameters.
    // Ignored by the json serializer if null.
    private static final String CLOUDTODEVICEMETHOD_TAG = "cloudToDeviceMethod";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(CLOUDTODEVICEMETHOD_TAG)
    private JsonElement cloudToDeviceMethod = null;

    // Required if jobType is updateTwin.
    // The Update Twin tags and desired properties.
    // Ignored by the json serializer if null.
    private static final String UPDATETWIN_TAG = "updateTwin";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(UPDATETWIN_TAG)
    private JsonElement updateTwin = null;

    // Required if jobType is updateTwin or cloudToDeviceMethod.
    // Condition for device query to get devices to execute the job on
    private static final String QUERYCONDITION_TAG = "queryCondition";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(QUERYCONDITION_TAG)
    private String queryCondition;

    // ISO 8601 date time to start the job
    private static final String STARTTIME_TAG = "startTime";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(STARTTIME_TAG)
    private String startTime;

    // Max execution time in seconds (ttl duration)
    private static final String MAXEXECUTIONTIMEINSECONDS_TAG = "maxExecutionTimeInSeconds";
    @Expose(serialize = true, deserialize = false)
    @SerializedName(MAXEXECUTIONTIMEINSECONDS_TAG)
    private long maxExecutionTimeInSeconds;

    /**
     * CONSTRUCTOR
     *
     * @param jobId is a string with the job identification. Cannot be {@code null} or empty.
     * @param cloudToDeviceMethod is the class that contains the json for the cloud to Device Method. Cannot be {@code null}.
     * @param queryCondition is a string with the deviceId or an <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">IoT Hub Query Condition</a>. Cannot be {@code null} or empty.
     * @param startTime is the date and time to start the job. Cannot be {@code null}.
     * @param maxExecutionTimeInSeconds is the maximum time that the device can expend to execute the job. Cannot be negative
     * @throws IllegalArgumentException if one of the parameter is not valid.
     */
    public JobsParser(
            String jobId, MethodParser cloudToDeviceMethod,
            String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException
    {
        /* Codes_SRS_JOBSPARSER_21_004: [If the cloudToDeviceMethod is null, the constructor shall throws IllegalArgumentException.] */
        if (cloudToDeviceMethod == null)
        {
            throw new IllegalArgumentException("Null cloudToDeviceMethod parameter");
        }

        /* Codes_SRS_JOBSPARSER_21_001: [The constructor shall evaluate and store the commons parameters using the internal function validateCommonFields.] */
        /* Codes_SRS_JOBSPARSER_21_002: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
        validateCommonFields(jobId, queryCondition, startTime, maxExecutionTimeInSeconds);

        /* Codes_SRS_JOBSPARSER_21_003: [The constructor shall store the JsonElement for the cloudToDeviceMethod.] */
        this.cloudToDeviceMethod = cloudToDeviceMethod.toJsonElement();

        /* Codes_SRS_JOBSPARSER_21_005: [The constructor shall set the jobType as scheduleDeviceMethod.] */
        this.jobType = SCHEDULE_DEVICE_METHOD;

        /* Codes_SRS_JOBSPARSER_21_006: [The constructor shall set the updateTwin as null.] */
        this.updateTwin = null;
    }


    /**
     * CONSTRUCTOR
     *
     * @param jobId is a string with the job identification. Cannot be {@code null} or empty.
     * @param updateTwin is the class that contains the json for the update twin properties. Cannot be {@code null}.
     * @param queryCondition is a string with the deviceId or an <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">IoT Hub Query Condition</a>. Cannot be {@code null} or empty.
     * @param startTime is the date and time to start the job. Cannot be {@code null}.
     * @param maxExecutionTimeInSeconds is the maximum time that the device can expend to execute the job. Cannot be negative
     * @throws IllegalArgumentException if one of the parameter is not valid.
     * @deprecated As of release 0.4.0, replaced by {@link #JobsParser(String, TwinState, String, Date, long)}
     */
    @Deprecated
    public JobsParser(
            String jobId, TwinParser updateTwin,
            String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException
    {
        /* Codes_SRS_JOBSPARSER_21_010: [If the updateTwin is null, the constructor shall throws IllegalArgumentException.] */
        if (updateTwin == null)
        {
            throw new IllegalArgumentException("Null TwinParser parameter");
        }

        /* Codes_SRS_JOBSPARSER_21_007: [The constructor shall evaluate and store the commons parameters using the internal function validateCommonFields.] */
        /* Codes_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
        validateCommonFields(jobId, queryCondition, startTime, maxExecutionTimeInSeconds);

        /* Codes_SRS_JOBSPARSER_21_009: [The constructor shall store the JsonElement for the updateTwin.] */
        this.updateTwin = updateTwin.toJsonElement();

        /* Codes_SRS_JOBSPARSER_21_011: [The constructor shall set the jobType as scheduleUpdateTwin.] */
        this.jobType = SCHEDULE_UPDATE_TWIN;

        /* Codes_SRS_JOBSPARSER_21_012: [The constructor shall set the cloudToDeviceMethod as null.] */
        this.cloudToDeviceMethod = null;
    }

    /**
     * CONSTRUCTOR
     *
     * @param jobId is a string with the job identification. Cannot be {@code null} or empty.
     * @param updateTwin is the class that contains the json for the update twin properties. Cannot be {@code null}.
     * @param queryCondition is a string with the deviceId or an <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">IoT Hub Query Condition</a>. Cannot be {@code null} or empty.
     * @param startTime is the date and time to start the job. Cannot be {@code null}.
     * @param maxExecutionTimeInSeconds is the maximum time that the device can expend to execute the job. Cannot be negative
     * @throws IllegalArgumentException if one of the parameter is not valid.
     */
    public JobsParser(
            String jobId, TwinState updateTwin,
            String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException
    {
        /* Codes_SRS_JOBSPARSER_21_010: [If the updateTwin is null, the constructor shall throws IllegalArgumentException.] */
        if (updateTwin == null)
        {
            throw new IllegalArgumentException("Null twinState parameter");
        }

        /* Codes_SRS_JOBSPARSER_21_007: [The constructor shall evaluate and store the commons parameters using the internal function validateCommonFields.] */
        /* Codes_SRS_JOBSPARSER_21_008: [If any common parameter is invalid, the constructor shall throws IllegalArgumentException.] */
        validateCommonFields(jobId, queryCondition, startTime, maxExecutionTimeInSeconds);

        /* Codes_SRS_JOBSPARSER_21_009: [The constructor shall store the JsonElement for the updateTwin.] */
        this.updateTwin = updateTwin.toJsonElement();

        /* Codes_SRS_JOBSPARSER_21_011: [The constructor shall set the jobType as scheduleUpdateTwin.] */
        this.jobType = SCHEDULE_UPDATE_TWIN;

        /* Codes_SRS_JOBSPARSER_21_012: [The constructor shall set the cloudToDeviceMethod as null.] */
        this.cloudToDeviceMethod = null;
    }


    /**
     * Getter for the string with the json.
     *
     * @return a String with the json the represents the content of this class.
     */
    public String toJson()
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        /* Codes_SRS_JOBSPARSER_21_013: [The toJson shall return a String with a json that represents the content of this class.] */
        return gson.toJson(this);
    }


    /**
     * Evaluate and store the common fields.
     *
     * @param jobId is a string with the job identification. Cannot be {@code null} or empty.
     * @param queryCondition is a string with the deviceId or an <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-query-language">IoT Hub Query Condition</a>. Cannot be {@code null} or empty.
     * @param startTime is the date and time to start the job. Cannot be {@code null}.
     * @param maxExecutionTimeInSeconds is the maximum time that the device can expend to execute the job. Cannot be negative
     * @throws IllegalArgumentException if one of the parameter is not valid.
     */
    private void validateCommonFields(
            String jobId, String queryCondition, Date startTime, long maxExecutionTimeInSeconds)
            throws IllegalArgumentException
    {
        /* Codes_SRS_JOBSPARSER_21_015: [If the jobId is null, empty, or invalid, the validateCommonFields shall throws IllegalArgumentException.] */
        ParserUtility.validateStringUTF8(jobId);

        /* Codes_SRS_JOBSPARSER_21_019: [If the startTime is null, the validateCommonFields shall throws IllegalArgumentException.] */
        if (startTime == null)
        {
            throw new IllegalArgumentException("Null start time");
        }

        /* Codes_SRS_JOBSPARSER_21_017: [If the maxExecutionTimeInSeconds is negative, the validateCommonFields shall throws IllegalArgumentException.] */
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("Negative max execution time in seconds");
        }

        /* Codes_SRS_JOBSPARSER_21_014: [The validateCommonFields shall store the jobId, queryCondition, and maxExecutionTimeInSeconds.] */
        this.jobId = jobId;
        this.queryCondition = queryCondition;
        this.maxExecutionTimeInSeconds = maxExecutionTimeInSeconds;

        /* Codes_SRS_JOBSPARSER_21_018: [The validateCommonFields shall format startTime as a String and store it.] */
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        this.startTime = dateFormat.format(startTime);
    }

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    protected JobsParser()
    {
    }
}
