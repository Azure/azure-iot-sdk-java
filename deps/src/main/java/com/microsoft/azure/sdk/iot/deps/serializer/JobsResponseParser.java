// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.twin.TwinState;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Representation of a single Jobs response collection with a Json deserializer.
 */
public class JobsResponseParser
{
    @Expose(deserialize = false)
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    @Expose(deserialize = false)
    private static final String TIMEZONE = "UTC";

    // Job identifier
    private static final String JOBID_TAG = "jobId";
    @SerializedName(JOBID_TAG)
    private String jobId;

    // Required if type is updateTwin or cloudToDeviceMethod.
    // Condition for device query to get devices to execute the job on
    private static final String QUERYCONDITION_TAG = "queryCondition";
    @SerializedName(QUERYCONDITION_TAG)
    private String queryCondition;

    // Scheduled job start time in UTC.
    private static final String CREATETIME_TAG = "createdTime";
    @SerializedName(CREATETIME_TAG)
    private String createdTime;

    // Scheduled job start time in UTC for Query Response.
    private static final String CREATE_TIME_UTC_TAG = "createdDateTimeUtc";
    @SerializedName(CREATE_TIME_UTC_TAG)
    private String createdTimeUTC;

    @Expose(deserialize = false)
    private Date createdTimeDate;

    // System generated start time in UTC.
    private static final String STARTTIME_TAG = "startTime";
    @SerializedName(STARTTIME_TAG)
    private String startTime;

    // System generated start time in UTC for Query Response.
    private static final String START_TIME_UTC_TAG = "startTimeUtc";
    @SerializedName(START_TIME_UTC_TAG)
    private String startTimeUTC;

    @Expose(deserialize = false)
    private Date startTimeDate;

    // System generated end time in UTC.
    // Represents the time the job stopped processing.
    private static final String ENDTIME_TAG = "endTime";
    @SerializedName(ENDTIME_TAG)
    private String endTime;

    // System generated end time in UTC for Query Response.
    // Represents the time the job stopped processing.
    private static final String END_TIME_UTC_TAG = "endTimeUtc";
    @SerializedName(END_TIME_UTC_TAG)
    private String endTimeUTC;

    @Expose(deserialize = false)
    private Date endTimeDate;

    // System generated end time in UTC for Query Response.
    // Represents the time the job was last updated.
    private static final String LAST_UPDATED_TIME_UTC_TAG = "lastUpdatedDateTimeUtc";
    @SerializedName(LAST_UPDATED_TIME_UTC_TAG)
    private String lastUpdatedTime;

    @Expose(serialize = false, deserialize = true)
    private Date lastUpdatedTimeDate;

    // Max execution time in seconds (ttl duration)
    private static final String MAXEXECUTIONTIMEINSECONDS_TAG = "maxExecutionTimeInSeconds";
    @SerializedName(MAXEXECUTIONTIMEINSECONDS_TAG)
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
    private String jobType;

    // Required.
    // The status of job to execute.
    private static final String STATUS_TAG = "status";
    @SerializedName(STATUS_TAG)
    private String jobsStatus;

    // Required if type is cloudToDeviceMethod.
    // The method type and parameters.
    // Ignored by the json serializer if null.
    private static final String CLOUDTODEVICEMETHOD_TAG = "cloudToDeviceMethod";
    @SerializedName(CLOUDTODEVICEMETHOD_TAG)
    private MethodParser cloudToDeviceMethod = null;

    // The outcome for job query if any.
    private static final String OUTCOME_TAG = "outcome";
    @SerializedName(OUTCOME_TAG)
    private JsonElement outcome = null;

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
    private String failureReason = null;

    // System generated status message.
    // Represents a string containing a message with status about the job execution.
    private static final String STATUSMESSAGE_TAG = "statusMessage";
    @SerializedName(STATUSMESSAGE_TAG)
    private String statusMessage = null;

    // System generated statistics.
    // Different number of devices in the job.
    private static final String DEVICEJOBSSTATISTICS_TAG = "deviceJobStatistics";
    @SerializedName(DEVICEJOBSSTATISTICS_TAG)
    private JobsStatisticsParser deviceJobStatistics = null;

    // The deviceId related to this response.
    // It can be null (e.g. in case of a parent orchestration).
    private static final String DEVICEID_TAG = "deviceId";
    @SerializedName(DEVICEID_TAG)
    private String deviceId = null;

    // The jobId of the parent orchestration, if any.
    private static final String PARENTJOBID_TAG = "parentJobId";
    @SerializedName(PARENTJOBID_TAG)
    private String parentJobId = null;

    // The error on the Job Response, if any.
    private static final String ERROR_TAG = "error";
    @SerializedName(ERROR_TAG)
    private JobQueryResponseError error = null;

    /**
     * Static constructor to create a instance based on the provided json
     *
     * @param json is the string with the json to parse
     * @return an instance of the JobsResponseParser
     * @throws IllegalArgumentException if the json is {@code null} or empty, or if any date is invalid
     * @throws JsonParseException if the json is not valid
     */
    public static JobsResponseParser createFromJson(String json) throws IllegalArgumentException, JsonParseException
    {
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();

        /* Codes_SRS_JOBSRESPONSEPARSER_21_006: [If the json is null or empty, the createFromJson shall throws IllegalArgumentException.] */
        if((json == null) || json.isEmpty())
        {
            throw new IllegalArgumentException("Json is null or empty");
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_002: [The createFromJson shall parse the provided string for JobsResponseParser class.] */
        /* Codes_SRS_JOBSRESPONSEPARSER_21_001: [The createFromJson shall create a new instance of JobsResponseParser class.] */
        /* Codes_SRS_JOBSRESPONSEPARSER_21_005: [If the json contains `deviceJobStatistics`, the createFromJson shall parse the content of it for JobsStatisticsParser class.] */
        /* Codes_SRS_JOBSRESPONSEPARSER_21_007: [If the json is not valid, the createFromJson shall throws JsonParseException.] */
        JobsResponseParser jobsResponseParser = gson.fromJson(json, JobsResponseParser.class);

        /* Codes_SRS_JOBSRESPONSEPARSER_21_008: [If the json do not contains `jobId`, the createFromJson shall throws IllegalArgumentException.] */
        if((jobsResponseParser.jobId == null) || jobsResponseParser.jobId.isEmpty())
        {
            throw new IllegalArgumentException("Json do not contains " + JOBID_TAG);
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_009: [If the json do not contains `type`, or the `type` is invalid, the createFromJson shall throws IllegalArgumentException.] */
        if(((jobsResponseParser.type == null) || jobsResponseParser.type.isEmpty()) && (jobsResponseParser.jobType == null || jobsResponseParser.jobType.isEmpty()))
        {
            throw new IllegalArgumentException("Json do not contains " + TYPE_TAG);
        }

        if((jobsResponseParser.type != null) && (jobsResponseParser.jobType != null))
        {
            throw new IllegalArgumentException("Json contains both " + TYPE_TAG + " and " + JOB_TYPE_TAG);
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_010: [If the json do not contains `status`, or the `status` is invalid, the createFromJson shall throws IllegalArgumentException.] */
        if((jobsResponseParser.jobsStatus == null) || jobsResponseParser.jobsStatus.isEmpty())
        {
            throw new IllegalArgumentException("Json do not contains " + STATUS_TAG);
        }

        if (jobsResponseParser.type == null)
        {
            jobsResponseParser.type = jobsResponseParser.jobType;
        }

        Map map = gson.fromJson(json, Map.class);

        /* Codes_SRS_JOBSRESPONSEPARSER_21_003: [If the json contains `updateTwin`, the createFromJson shall parse the content of it for TwinState class.] */
        if(map.containsKey(UPDATETWIN_TAG))
        {
            jobsResponseParser.updateTwin = TwinState.createFromTwinJson(gson.toJson(map.get(UPDATETWIN_TAG)));
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_004: [If the json contains `cloudToDeviceMethod`, the createFromJson shall parse the content of it for MethodParser class.] */
        if(map.containsKey(CLOUDTODEVICEMETHOD_TAG))
        {
            jobsResponseParser.cloudToDeviceMethod.fromJson(gson.toJson(map.get(CLOUDTODEVICEMETHOD_TAG)));
        }

        if (map.containsKey(OUTCOME_TAG))
        {
            Map responseMap = gson.fromJson(jobsResponseParser.outcome, Map.class);
            if (responseMap.containsKey(DEVICE_METHOD_RESPONSE_TAG))
            {
                //Codes_SRS_JOBSRESPONSEPARSER_25_028: [If the json contains outcome, the createFromJson shall parse the value of the key deviceMethodResponse for MethodParser class.]
                MethodParser methodParserResponse = new MethodParser();
                String outcomeResponse = gson.toJson(responseMap.get(DEVICE_METHOD_RESPONSE_TAG));
                methodParserResponse.fromJson(outcomeResponse);
                jobsResponseParser.methodResponse = methodParserResponse;
            }
            else
            {
                //Codes_SRS_JOBSRESPONSEPARSER_25_029: [If the json contains outcome, and the key deviceMethodResponse does not exist then this method shall create empty method parser for MethodParser class.]
                /*As out come has a value only for method response, in all other cases it should not be looked for values and Exception should be throw
                * As this is already done by method parser, Initialise the outcome to empty method parser.*/
                MethodParser methodParserResponse = new MethodParser();
                methodParserResponse.fromJson(gson.toJson(map.get(OUTCOME_TAG)));
                jobsResponseParser.methodResponse = methodParserResponse;
            }
        }

        // Codes_SRS_JOBSRESPONSEPARSER_25_034: [If the json contains both of the dates createdTime and createdDateTimeUtc or startTime and startTimeUtc or endTime and endTimeUtc, the createFromJson shall throw IllegalArgumentException.]
        /* Codes_SRS_JOBSRESPONSEPARSER_21_011: [If the json contains any of the dates `createdTime`, `startTime`, or `endTime`, the createFromJson shall parser it as ISO_8601.] */
        if (jobsResponseParser.createdTime != null && jobsResponseParser.createdTimeUTC != null)
        {
            throw new IllegalArgumentException("Both createdTime and createdTimeUTC cannot be sent at the same time");
        }

        if(jobsResponseParser.createdTime != null)
        {
            try
            {
                jobsResponseParser.createdTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.createdTime);
            }
            catch (IllegalArgumentException e)
            {
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
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
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
                jobsResponseParser.createdTimeDate = null;
            }
        }

        if (jobsResponseParser.startTime != null && jobsResponseParser.startTimeUTC != null)
        {
            throw new IllegalArgumentException("Both startTime and startTimeUTC cannot be sent at the same time");
        }

        if(jobsResponseParser.startTime != null)
        {
            try
            {
                jobsResponseParser.startTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.startTime);
            }
            catch (IllegalArgumentException e)
            {
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
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
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
                jobsResponseParser.startTimeDate = null;
            }
        }

        if (jobsResponseParser.endTime != null && jobsResponseParser.endTimeUTC != null)
        {
            throw new IllegalArgumentException("Both endTime and endTimeUTC cannot be sent at the same time");
        }

        if(jobsResponseParser.endTime != null)
        {
            try
            {
                jobsResponseParser.endTimeDate = ParserUtility.getDateTimeUtc(jobsResponseParser.endTime);
            }
            catch (IllegalArgumentException e)
            {
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
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
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
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
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_013: [The getJobId shall return the jobId value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_014: [The getQueryCondition shall return the queryCondition value.] */
        return this.queryCondition;
    }

    /**
     * Getter for created time
     *
     * @return the scheduled job start time in UTC. It can be {@code null}
     */
    public Date getCreatedTime()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_015: [The getCreateTime shall return the createTime value.] */
        return this.createdTimeDate;
    }

    /**
     * Getter for start time UTC
     *
     * @return the system generated start time in UTC. It can be {@code null}
     */
    public Date getStartTime()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_016: [The getStartTime shall return the startTime value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_017: [The getEndTime shall return the endTime value.] */
        return this.endTimeDate;
    }

    /**
     * Getter for max execution time in seconds
     *
     * @return the max execution time in seconds (ttl duration). It can be {@code null}
     */
    public Long getMaxExecutionTimeInSeconds()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_018: [The getMaxExecutionTimeInSeconds shall return the maxExecutionTimeInSeconds value.] */
        return this.maxExecutionTimeInSeconds;
    }

    /**
     * Getter for last updated time in UTC
     * @return System generated last updated time in UTC. Can be {@code null}
     */
    public Date getLastUpdatedTimeDate()
    {
        //Codes_SRS_JOBSRESPONSEPARSER_25_031: [The getLastUpdatedTimeDate shall return the LastUpdatedTimeUTCDate value.]
        return lastUpdatedTimeDate;
    }

    /**
     * Getter for the job type
     *
     * @return the type of job to execute
     */
    public String getType()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_019: [The getType shall return a String with the job type value.] */
        return this.type;
    }

    /**
     * Getter for the jobs status
     *
     * @return the status of this job
     */
    public String getJobsStatus()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_020: [The getJobsStatus shall return a String with the job status value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_021: [The getCloudToDeviceMethod shall return the cloudToDeviceMethod value.] */
        return this.cloudToDeviceMethod;
    }

    /**
     * Returns the error as json
     * @return the json error returned by IotHub
     */
    public JobQueryResponseError getError()
    {
        //Codes_SRS_JOBSRESPONSEPARSER_25_032: [The getError shall return the error value.]
        return error;
    }

    /**
     * Getter for the outcome of device method
     * @return the outcome in json for device method when querying device method job
     */
    public MethodParser getOutcome()
    {
        //Codes_SRS_JOBSRESPONSEPARSER_25_033: [The getOutcome shall return the outcome value.]
        return this.methodResponse;
    }

    /**
     * Getter for update twin json
     *
     * @return the json of update twin. It is {@code null} if type
     * is not scheduleUpdateTwin
     * @deprecated As of release 0.4.0, replaced by {@link #getUpdateTwinState()}
     */
    @Deprecated
    public TwinParser getUpdateTwin()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_022: [The getUpdateTwin shall return the updateTwin value.] */
        TwinParser twinParser = new TwinParser();
        try
        {
            twinParser.updateTwin(this.updateTwin.getDesiredProperty(), this.updateTwin.getReportedProperty(), this.updateTwin.getTags());
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
        return twinParser;
    }

    /**
     * Getter for update twin json
     *
     * @return the json of update twin. It is {@code null} if type
     * is not scheduleUpdateTwin
     */
    public TwinState getUpdateTwinState()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_022: [The getUpdateTwin shall return the updateTwin value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_023: [The getFailureReason shall return the failureReason value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_024: [The getStatusMessage shall return the statusMessage value.] */
        return this.statusMessage;
    }

    /**
     * Getter for jobs statistics
     *
     * @return a set of counters with the jobs statistics. It can be {@code null}
     */
    public JobsStatisticsParser getJobStatistics()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_025: [The getJobStatistics shall return the jobStatistics value.] */
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
        /* Codes_SRS_JOBSRESPONSEPARSER_21_026: [The getDeviceId shall return the deviceId value.] */
        return this.deviceId;
    }

    /**
     * Getter for the parent jobId
     *
     * @return the jobId of the parent orchestration, if any. It can be {@code null}
     */
    public String getParentJobId()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_027: [The getParentJobId shall return the parentJobId value.] */
        return this.parentJobId;
    }

}
