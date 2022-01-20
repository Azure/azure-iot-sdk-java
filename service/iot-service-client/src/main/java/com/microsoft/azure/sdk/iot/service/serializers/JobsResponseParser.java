// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.devicetwin.TwinState;

import java.util.Date;
import java.util.Map;

/**
 * Representation of a single Jobs response collection with a Json deserializer.
 */
@SuppressWarnings("unused") // A number of private members are unused but may be filled in or used by serialization
public class JobsResponseParser
{
    // Job identifier
    private static final String JOBID_TAG = "jobId";
    @SerializedName(JOBID_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String jobId;

    // Required if type is updateTwin or cloudToDeviceMethod.
    // Condition for device query to get devices to execute the job on
    private static final String QUERYCONDITION_TAG = "queryCondition";
    @SerializedName(QUERYCONDITION_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
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
    private Date endTimeDate;

    // System generated end time in UTC for Query Response.
    // Represents the time the job was last updated.
    private static final String LAST_UPDATED_TIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @SerializedName(LAST_UPDATED_TIME_UTC_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String lastUpdatedTime;

    @Expose(serialize = false)
    private Date lastUpdatedTimeDate;

    // Max execution time in seconds (ttl duration)
    private static final String MAXEXECUTIONTIMEINSECONDS_TAG = "maxExecutionTimeInSeconds";
    @SerializedName(MAXEXECUTIONTIMEINSECONDS_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private Long maxExecutionTimeInSeconds;

    // Required.
    // The type of job to execute.
    private static final String TYPE_TAG = "type";
    @SerializedName(TYPE_TAG)
    private String type;

    // Required for Query Response.
    // The type of job to execute.
    private static final String JOB_TYPE_TAG = "jobType";
    @SerializedName(JOB_TYPE_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String jobType;

    // Required.
    // The status of job to execute.
    private static final String STATUS_TAG = "status";
    @SerializedName(STATUS_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String jobsStatus;

    // Required if type is cloudToDeviceMethod.
    // The method type and parameters.
    // Ignored by the json serializer if null.
    private static final String CLOUDTODEVICEMETHOD_TAG = "cloudToDeviceMethod";
    @SerializedName(CLOUDTODEVICEMETHOD_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private MethodParser cloudToDeviceMethod;

    // The outcome for job query if any.
    private static final String OUTCOME_TAG = "outcome";
    @SerializedName(OUTCOME_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private JsonElement outcome;

    // The contents of outcome for job query if any.
    private static final String DEVICE_METHOD_RESPONSE_TAG = "deviceMethodResponse";
    private MethodParser methodResponse;

    // Required if type is updateTwin.
    // The Update Twin tags and desired properties.
    // Ignored by the json serializer if null.
    private static final String UPDATETWIN_TAG = "updateTwin";
    @SerializedName(UPDATETWIN_TAG)
    private TwinState updateTwin = null;

    // System generated failure reason.
    // If status == failure, this represents a string containing the reason.
    private static final String FAILUREREASON_TAG = "failureReason";
    @SerializedName(FAILUREREASON_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String failureReason;

    // System generated status message.
    // Represents a string containing a message with status about the job execution.
    private static final String STATUSMESSAGE_TAG = "statusMessage";
    @SerializedName(STATUSMESSAGE_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String statusMessage;

    // System generated statistics.
    // Different number of devices in the job.
    private static final String DEVICEJOBSSTATISTICS_TAG = "deviceJobStatistics";
    @SerializedName(DEVICEJOBSSTATISTICS_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private JobsStatisticsParser deviceJobStatistics;

    // The deviceId related to this response.
    // It can be null (e.g. in case of a parent orchestration).
    private static final String DEVICEID_TAG = "deviceId";
    @SerializedName(DEVICEID_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String deviceId;

    // The jobId of the parent orchestration, if any.
    private static final String PARENTJOBID_TAG = "parentJobId";
    @SerializedName(PARENTJOBID_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
    private String parentJobId;

    // The error on the Job Response, if any.
    private static final String ERROR_TAG = "error";
    @SerializedName(ERROR_TAG)
    @SuppressWarnings("unused") // used by reflection during json serialization/deserialization
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
            jobsResponseParser.updateTwin = TwinState.createFromTwinJson(gson.toJson(map.get(UPDATETWIN_TAG)));
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
                String outcomeResponse = gson.toJson(responseMap.get(DEVICE_METHOD_RESPONSE_TAG));
                methodParserResponse.fromJson(outcomeResponse);
                jobsResponseParser.methodResponse = methodParserResponse;
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

    /**
     * Getter for the Job identifier
     *
     * @return Job identifier
     */
    public String getJobId()
    {
        return this.jobId;
    }

    /**
     * Getter for query condition
     *
     * @return the condition for device query to get devices to execute the
     * job on. It can be {@code null}
     */
    public String getQueryCondition()
    {
        return this.queryCondition;
    }

    /**
     * Getter for created time
     *
     * @return the scheduled job start time in UTC. It can be {@code null}
     */
    public Date getCreatedTime()
    {
        return this.createdTimeDate;
    }

    /**
     * Getter for start time UTC
     *
     * @return the system generated start time in UTC. It can be {@code null}
     */
    public Date getStartTime()
    {
        return this.startTimeDate;
    }

    /**
     * Getter for the end time UTC
     * Represents the time the job stopped processing
     *
     * @return the system generated end time in UTC. It can be {@code null}
     */
    public Date getEndTime()
    {
        return this.endTimeDate;
    }

    /**
     * Getter for max execution time in seconds
     *
     * @return the max execution time in seconds (ttl duration). It can be {@code null}
     */
    public Long getMaxExecutionTimeInSeconds()
    {
        return this.maxExecutionTimeInSeconds;
    }

    /**
     * Getter for last updated time in UTC
     * @return System generated last updated time in UTC. Can be {@code null}
     */
    public Date getLastUpdatedTimeDate()
    {
        return lastUpdatedTimeDate;
    }

    /**
     * Getter for the job type
     *
     * @return the type of job to execute
     */
    public String getType()
    {
        return this.type;
    }

    /**
     * Getter for the jobs status
     *
     * @return the status of this job
     */
    public String getJobsStatus()
    {
        return this.jobsStatus;
    }

    /**
     * Getter for cloud to device method json
     *
     * @return the json of cloud to device method. It is {@code null} if type
     * is not scheduleDeviceMethod
     */
    public MethodParser getCloudToDeviceMethod()
    {
        return this.cloudToDeviceMethod;
    }

    /**
     * Returns the error as json
     * @return the json error returned by IotHub
     */
    public JobQueryResponseError getError()
    {
        return error;
    }

    /**
     * Getter for the outcome of device method
     * @return the outcome in json for device method when querying device method job
     */
    public MethodParser getOutcome()
    {
        return this.methodResponse;
    }

    /**
     * Getter for update twin json
     *
     * @return the json of update twin. It is {@code null} if type
     * is not scheduleUpdateTwin
     */
    public TwinState getUpdateTwinState()
    {
        return this.updateTwin;
    }

    /**
     * Getter for failure reason
     *
     * @return If status == failure, this represents a string containing the
     * reason. It can be {@code null}
     */
    public String getFailureReason()
    {
        return this.failureReason;
    }

    /**
     * Getter for the status message
     *
     * @return a string containing a message with status about the job
     * execution. It can be {@code null}
     */
    public String getStatusMessage()
    {
        return this.statusMessage;
    }

    /**
     * Getter for jobs statistics
     *
     * @return a set of counters with the jobs statistics. It can be {@code null}
     */
    public JobsStatisticsParser getJobStatistics()
    {
        return this.deviceJobStatistics;
    }

    /**
     * Getter for the device Id
     *
     * @return the deviceId related to this response. It can be {@code null}
     * (e.g. in case of a parent orchestration)
     */
    public String getDeviceId()
    {
        return this.deviceId;
    }

    /**
     * Getter for the parent jobId
     *
     * @return the jobId of the parent orchestration, if any. It can be {@code null}
     */
    public String getParentJobId()
    {
        return this.parentJobId;
    }
}
