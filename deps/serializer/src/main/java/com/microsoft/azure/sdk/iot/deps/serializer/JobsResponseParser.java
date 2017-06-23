// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

/**
 * Representation of a single Jobs response collection with a Json deserializer.
 */
public class JobsResponseParser
{
    @Expose(deserialize = false)
    private static final String DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSS'Z'";
    @Expose(deserialize = false)
    private static final String TIMEZONE = "UTC";

    // Job identifier
    private static final String JOBID_TAG = "jobId";
    @SerializedName(JOBID_TAG)
    private String jobId;

    // Required if jobType is updateTwin or cloudToDeviceMethod.
    // Condition for device query to get devices to execute the job on
    private static final String QUERYCONDITION_TAG = "queryCondition";
    @SerializedName(QUERYCONDITION_TAG)
    private String queryCondition;

    // Scheduled job start time in UTC.
    private static final String CREATETIME_TAG = "createdTime";
    @SerializedName(CREATETIME_TAG)
    private String createdTime;
    @Expose(deserialize = false)
    private Date createdTimeDate;

    // System generated start time in UTC.
    private static final String STARTTIME_TAG = "startTime";
    @SerializedName(STARTTIME_TAG)
    private String startTime;
    @Expose(deserialize = false)
    private Date startTimeDate;

    // System generated end time in UTC.
    // Represents the time the job stopped processing.
    private static final String ENDTIME_TAG = "endTime";
    @SerializedName(ENDTIME_TAG)
    private String endTime;
    @Expose(deserialize = false)
    private Date endTimeDate;

    // Max execution time in seconds (ttl duration)
    private static final String MAXEXECUTIONTIMEINSECONDS_TAG = "maxExecutionTimeInSeconds";
    @SerializedName(MAXEXECUTIONTIMEINSECONDS_TAG)
    private Long maxExecutionTimeInSeconds;

    // Required.
    // The type of job to execute.
    private static final String TYPE_TAG = "type";
    @SerializedName(TYPE_TAG)
    private String jobType;

    // Required.
    // The status of job to execute.
    private static final String STATUS_TAG = "status";
    @SerializedName(STATUS_TAG)
    private String jobsStatus;

    // Required if jobType is cloudToDeviceMethod.
    // The method type and parameters.
    // Ignored by the json serializer if null.
    private static final String CLOUDTODEVICEMETHOD_TAG = "cloudToDeviceMethod";
    @SerializedName(CLOUDTODEVICEMETHOD_TAG)
    private MethodParser cloudToDeviceMethod = null;

    // Required if jobType is updateTwin.
    // The Update Twin tags and desired properties.
    // Ignored by the json serializer if null.
    private static final String UPDATETWIN_TAG = "updateTwin";
    @SerializedName(UPDATETWIN_TAG)
    private TwinParser updateTwin = null;

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
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
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
        if((jobsResponseParser.jobType == null) || jobsResponseParser.jobType.isEmpty())
        {
            throw new IllegalArgumentException("Json do not contains " + TYPE_TAG);
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_010: [If the json do not contains `status`, or the `status` is invalid, the createFromJson shall throws IllegalArgumentException.] */
        if((jobsResponseParser.jobsStatus == null) || jobsResponseParser.jobsStatus.isEmpty())
        {
            throw new IllegalArgumentException("Json do not contains " + STATUS_TAG);
        }

        Map map = gson.fromJson(json, Map.class);

        /* Codes_SRS_JOBSRESPONSEPARSER_21_003: [If the json contains `updateTwin`, the createFromJson shall parse the content of it for TwinParser class.] */
        if(map.containsKey(UPDATETWIN_TAG))
        {
            jobsResponseParser.updateTwin.updateTwin(gson.toJson(map.get(UPDATETWIN_TAG)));
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_004: [If the json contains `cloudToDeviceMethod`, the createFromJson shall parse the content of it for MethodParser class.] */
        if(map.containsKey(CLOUDTODEVICEMETHOD_TAG))
        {
            jobsResponseParser.cloudToDeviceMethod.fromJson(gson.toJson(map.get(CLOUDTODEVICEMETHOD_TAG)));
        }

        /* Codes_SRS_JOBSRESPONSEPARSER_21_011: [If the json contains any of the dates `createdTime`, `startTime`, or `endTime`, the createFromJson shall parser it as ISO_8601.] */
        if(jobsResponseParser.createdTime != null)
        {
            try
            {
                jobsResponseParser.createdTimeDate = dateFormat.parse(jobsResponseParser.createdTime);
            }
            catch (ParseException e)
            {
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
                jobsResponseParser.createdTimeDate = null;
            }
        }

        if(jobsResponseParser.startTime != null)
        {
            try
            {
                jobsResponseParser.startTimeDate = dateFormat.parse(jobsResponseParser.startTime);
            }
            catch (ParseException e)
            {
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
                jobsResponseParser.startTimeDate = null;
            }
        }

        if(jobsResponseParser.endTime != null)
        {
            try
            {
                jobsResponseParser.endTimeDate = dateFormat.parse(jobsResponseParser.endTime);
            }
            catch (ParseException e)
            {
                /* Codes_SRS_JOBSRESPONSEPARSER_21_012: [If the createFromJson cannot properly parse the date in json, it shall ignore this value.] */
                jobsResponseParser.endTimeDate = null;
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
     * Getter for the job type
     *
     * @return the type of job to execute
     */
    public String getJobType()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_019: [The getJobType shall return a String with the job type value.] */
        return this.jobType;
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
     * @return the json of cloud to device method. It is {@code null} if jobType
     * is not scheduleDeviceMethod
     */
    public MethodParser getCloudToDeviceMethod()
    {
        /* Codes_SRS_JOBSRESPONSEPARSER_21_021: [The getCloudToDeviceMethod shall return the cloudToDeviceMethod value.] */
        return this.cloudToDeviceMethod;
    }

    /**
     * Getter for update twin json
     *
     * @return the json of update twin. It is {@code null} if jobType
     * is not scheduleUpdateTwin
     */
    public TwinParser getUpdateTwin()
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
