// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs.scheduled.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.methods.serializers.MethodParser;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import com.microsoft.azure.sdk.iot.service.twin.TwinState;
import lombok.Getter;

import java.util.Date;
import java.util.Map;

/**
 * Representation of a single Jobs response collection with a Json deserializer.
 */
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class JobsResponseParser
{
    // ScheduledJob identifier
    private static final String JOBID_TAG = "jobId";
    @SerializedName(JOBID_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String jobId;

    // Required if type is updateTwin or cloudToDeviceMethod.
    // Condition for device query to get devices to execute the job on
    private static final String QUERYCONDITION_TAG = "queryCondition";
    @SerializedName(QUERYCONDITION_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String queryCondition;

    // Scheduled job start time in UTC.
    private static final String CREATETIME_TAG = "createdTime";
    @SerializedName(CREATETIME_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String createdTime;

    // Scheduled job start time in UTC for Query Response.
    private static final String CREATE_TIME_UTC_TAG = "createdDateTimeUtc";
    @SerializedName(CREATE_TIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String createdTimeUTC;

    @Expose(deserialize = false)
    @Getter
    private Date createdTimeDate;

    // System generated start time in UTC.
    private static final String STARTTIME_TAG = "startTime";
    @SerializedName(STARTTIME_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String startTime;

    // System generated start time in UTC for Query Response.
    private static final String START_TIME_UTC_TAG = "startTimeUtc";
    @SerializedName(START_TIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String startTimeUTC;

    @Expose(deserialize = false)
    @Getter
    private Date startTimeDate;

    // System generated end time in UTC.
    // Represents the time the job stopped processing.
    private static final String ENDTIME_TAG = "endTime";
    @SerializedName(ENDTIME_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String endTime;

    // System generated end time in UTC for Query Response.
    // Represents the time the job stopped processing.
    private static final String END_TIME_UTC_TAG = "endTimeUtc";
    @SerializedName(END_TIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String endTimeUTC;

    @Expose(deserialize = false)
    @Getter
    private Date endTimeDate;

    // System generated end time in UTC for Query Response.
    // Represents the time the job was last updated.
    private static final String LAST_UPDATED_TIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @SerializedName(LAST_UPDATED_TIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String lastUpdatedTime;

    @Expose(serialize = false)
    @Getter
    private Date lastUpdatedTimeDate;

    // Max execution time in seconds (ttl duration)
    private static final String MAXEXECUTIONTIMEINSECONDS_TAG = "maxExecutionTimeInSeconds";
    @SerializedName(MAXEXECUTIONTIMEINSECONDS_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private Long maxExecutionTimeInSeconds;

    // Required.
    // The type of job to execute.
    private static final String TYPE_TAG = "type";
    @SerializedName(TYPE_TAG)
    @Getter
    private String type;

    // Required for Query Response.
    // The type of job to execute.
    private static final String JOB_TYPE_TAG = "jobType";
    @SerializedName(JOB_TYPE_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String jobType;

    // Required.
    // The status of job to execute.
    private static final String STATUS_TAG = "status";
    @SerializedName(STATUS_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String jobsStatus;

    // Required if type is cloudToDeviceMethod.
    // The method type and parameters.
    // Ignored by the json serializer if null.
    private static final String CLOUDTODEVICEMETHOD_TAG = "cloudToDeviceMethod";
    @SerializedName(CLOUDTODEVICEMETHOD_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private MethodParser cloudToDeviceMethod;

    // The outcome for job query if any.
    private static final String OUTCOME_TAG = "outcome";
    @SerializedName(OUTCOME_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private JsonElement outcome;

    // The contents of outcome for job query if any.
    private static final String DEVICE_METHOD_RESPONSE_TAG = "deviceMethodResponse";
    private MethodParser methodResponse;

    // Required if type is updateTwin.
    // The Update Twin tags and desired properties.
    // Ignored by the json serializer if null.
    private static final String UPDATETWIN_TAG = "updateTwin";
    @SerializedName(UPDATETWIN_TAG)
    @Getter
    private TwinState updateTwin = null;

    // System generated failure reason.
    // If status == failure, this represents a string containing the reason.
    private static final String FAILUREREASON_TAG = "failureReason";
    @SerializedName(FAILUREREASON_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String failureReason;

    // System generated status message.
    // Represents a string containing a message with status about the job execution.
    private static final String STATUSMESSAGE_TAG = "statusMessage";
    @SerializedName(STATUSMESSAGE_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String statusMessage;

    // System generated statistics.
    // Different number of devices in the job.
    private static final String DEVICEJOBSSTATISTICS_TAG = "deviceJobStatistics";
    @SerializedName(DEVICEJOBSSTATISTICS_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private JobsStatisticsParser deviceJobStatistics;

    // The deviceId related to this response.
    // It can be null (e.g. in case of a parent orchestration).
    private static final String DEVICEID_TAG = "deviceId";
    @SerializedName(DEVICEID_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String deviceId;

    // The jobId of the parent orchestration, if any.
    private static final String PARENTJOBID_TAG = "parentJobId";
    @SerializedName(PARENTJOBID_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private String parentJobId;

    // The error on the ScheduledJob Response, if any.
    private static final String ERROR_TAG = "error";
    @SerializedName(ERROR_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    @Getter
    private JobQueryResponseError error;

    /**
     * Static constructor to create a instance based on the provided json
     *
     * @param json is the string with the json to parse
     * @return an instance of the JobsResponseParser
     * @throws IllegalArgumentException if the json is {@code null} or empty, or if any date is invalid
     * @throws JsonParseException if the json is not valid
     */
    @SuppressWarnings("rawtypes")
    public static JobsResponseParser createFromJson(String json) throws IllegalArgumentException, JsonParseException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        if ((json == null) || json.isEmpty())
        {
            throw new IllegalArgumentException("Json is null or empty");
        }

        JobsResponseParser jobsResponseParser = gson.fromJson(json, JobsResponseParser.class);

        if ((jobsResponseParser.jobId == null) || jobsResponseParser.jobId.isEmpty())
        {
            throw new IllegalArgumentException("Json do not contains " + JOBID_TAG);
        }

        if (((jobsResponseParser.type == null) || jobsResponseParser.type.isEmpty()) && (jobsResponseParser.jobType == null || jobsResponseParser.jobType.isEmpty()))
        {
            throw new IllegalArgumentException("Json do not contains " + TYPE_TAG);
        }

        if ((jobsResponseParser.type != null) && (jobsResponseParser.jobType != null))
        {
            throw new IllegalArgumentException("Json contains both " + TYPE_TAG + " and " + JOB_TYPE_TAG);
        }

        if ((jobsResponseParser.jobsStatus == null) || jobsResponseParser.jobsStatus.isEmpty())
        {
            throw new IllegalArgumentException("Json do not contains " + STATUS_TAG);
        }

        if (jobsResponseParser.type == null)
        {
            jobsResponseParser.type = jobsResponseParser.jobType;
        }

        Map map = gson.fromJson(json, Map.class);

        if (map.containsKey(UPDATETWIN_TAG))
        {
            jobsResponseParser.updateTwin = new TwinState(gson.toJson(map.get(UPDATETWIN_TAG)));
        }

        if (map.containsKey(CLOUDTODEVICEMETHOD_TAG))
        {
            jobsResponseParser.cloudToDeviceMethod.fromJson(gson.toJson(map.get(CLOUDTODEVICEMETHOD_TAG)));
        }

        if (map.containsKey(OUTCOME_TAG))
        {
            Map responseMap = gson.fromJson(jobsResponseParser.outcome, Map.class);
            if (responseMap.containsKey(DEVICE_METHOD_RESPONSE_TAG))
            {
                MethodParser methodParserResponse = new MethodParser();
                Object value = responseMap.get(DEVICE_METHOD_RESPONSE_TAG);
                if (value == null)
                {
                    jobsResponseParser.methodResponse = null;
                }
                else
                {
                    String outcomeResponse = gson.toJson(value);
                    methodParserResponse.fromJson(outcomeResponse);
                    jobsResponseParser.methodResponse = methodParserResponse;
                }
            }
            else
            {
                MethodParser methodParserResponse = new MethodParser();
                methodParserResponse.fromJson(gson.toJson(map.get(OUTCOME_TAG)));
                jobsResponseParser.methodResponse = methodParserResponse;
            }
        }

        if (jobsResponseParser.createdTime != null && jobsResponseParser.createdTimeUTC != null)
        {
            throw new IllegalArgumentException("Both createdTime and createdTimeUTC cannot be sent at the same time");
        }

        if (jobsResponseParser.createdTime != null)
        {
            try
            {
                jobsResponseParser.createdTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.createdTime);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.createdTimeDate = null;
            }
        }
        else if (jobsResponseParser.createdTimeUTC != null)
        {
            try
            {
                jobsResponseParser.createdTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.createdTimeUTC);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.createdTimeDate = null;
            }
        }

        if (jobsResponseParser.startTime != null && jobsResponseParser.startTimeUTC != null)
        {
            throw new IllegalArgumentException("Both startTime and startTimeUTC cannot be sent at the same time");
        }

        if (jobsResponseParser.startTime != null)
        {
            try
            {
                jobsResponseParser.startTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.startTime);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.startTimeDate = null;
            }
        }
        else if (jobsResponseParser.startTimeUTC != null)
        {
            try
            {
                jobsResponseParser.startTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.startTimeUTC);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.startTimeDate = null;
            }
        }

        if (jobsResponseParser.endTime != null && jobsResponseParser.endTimeUTC != null)
        {
            throw new IllegalArgumentException("Both endTime and endTimeUTC cannot be sent at the same time");
        }

        if (jobsResponseParser.endTime != null)
        {
            try
            {
                jobsResponseParser.endTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.endTime);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.endTimeDate = null;
            }
        }
        else if (jobsResponseParser.endTimeUTC != null)
        {
            try
            {
                jobsResponseParser.endTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.endTimeUTC);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.endTimeDate = null;
            }
        }

        if (jobsResponseParser.lastUpdatedTime != null)
        {
            try
            {
                jobsResponseParser.lastUpdatedTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.lastUpdatedTime);
            }
            catch (IllegalArgumentException e)
            {
                jobsResponseParser.lastUpdatedTimeDate = null;
            }
        }

        return jobsResponseParser;
    }
}
