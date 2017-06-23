// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.deps.serializer.JobsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceOperations;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * JobClient enables service client to schedule and cancel jobs for a group of devices using the IoTHub.
 */
public class JobClient
{
    final static long USE_DEFAULT_TIMEOUT = 0L;

    private IotHubConnectionString iotHubConnectionString = null;
    private Integer requestId = 0;

    /**
     * Static constructor to create instance from connection string
     *
     * @param connectionString The iot hub connection string
     * @return The instance of JobClient
     * @throws IOException if the object creation failed
     * @throws IllegalArgumentException if the provided connectionString is {@code null} or empty
     */
    public static JobClient createFromConnectionString(String connectionString) throws IOException, IllegalArgumentException
    {
        if (connectionString == null || connectionString.length() == 0)
        {
            /* Codes_SRS_JOBCLIENT_21_001: [The constructor shall throw IllegalArgumentException if the input string is null or empty.] */
            throw new IllegalArgumentException("Connection string cannot be null or empty");
        }

        /* Codes_SRS_JOBCLIENT_21_003: [The constructor shall create a new DeviceMethod instance and return it.] */
        JobClient jobClient = new JobClient();

        /* Codes_SRS_JOBCLIENT_21_002: [The constructor shall create an IotHubConnectionStringBuilder object from the given connection string.] */
        jobClient.iotHubConnectionString = IotHubConnectionStringBuilder.createConnectionString(connectionString);

        return jobClient;
    }

    /**
     * Creates a new Job to update twin tags and desired properties on one or multiple devices
     *
     * @param jobId Unique Job Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param updateTwin Twin object to use for the update
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a jobResult object
     * @throws IllegalArgumentException if one of the provided parameters is invalid
     * @throws IOException if the function cannot create a URL for the job
     * @throws IotHubException if the http request failed
     */
    public synchronized JobResult scheduleUpdateTwin(
            String jobId,
            String queryCondition,
            DeviceTwinDevice updateTwin,
            Date startTimeUtc,
            long maxExecutionTimeInSeconds)
            throws IllegalArgumentException, IOException, IotHubException
    {
        URL url = null;

        /* Codes_SRS_JOBCLIENT_21_005: [If the JobId is null, empty, or invalid, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if((jobId == null) || jobId.isEmpty())
        {
            throw new IllegalArgumentException("null jobId");
        }

        /* Codes_SRS_JOBCLIENT_21_006: [If the updateTwin is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if(updateTwin == null)
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        /* Codes_SRS_JOBCLIENT_21_007: [If the startTimeUtc is null, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if(startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        /* Codes_SRS_JOBCLIENT_21_008: [If the maxExecutionTimeInSeconds is negative, the scheduleUpdateTwin shall throws IllegalArgumentException.] */
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        /* Codes_SRS_JOBCLIENT_21_004: [The scheduleUpdateTwin shall create a json String that represent the twin job using the JobsParser class.] */
        JobsParser jobsParser = new JobsParser(jobId, getParserFromDevice(updateTwin), queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
        String json = jobsParser.toJson();

        /* Codes_SRS_JOBCLIENT_21_009: [The scheduleUpdateTwin shall create a URL for Jobs using the iotHubConnectionString.] */
        try
        {
            url = iotHubConnectionString.getUrlJobs(jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        /* Codes_SRS_JOBCLIENT_21_010: [The scheduleUpdateTwin shall send a PUT request to the iothub using the created uri and json.] */
        /* Codes_SRS_JOBCLIENT_21_011: [If the scheduleUpdateTwin failed to send a PUT request, it shall throw IOException.] */
        /* Codes_SRS_JOBCLIENT_21_012: [If the scheduleUpdateTwin failed to verify the iothub response, it shall throw IotHubException.] */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), String.valueOf(requestId++), USE_DEFAULT_TIMEOUT);

        /* Codes_SRS_JOBCLIENT_21_013: [The scheduleUpdateTwin shall parse the iothub response and return it as JobResult.] */
        return new JobResult(response.getBody());
    }


    /**
     * Creates a new Job to invoke method on one or multiple devices
     *
     * @param jobId Unique Job Id for this job
     * @param queryCondition Query condition to evaluate which devices to run the job on. It can be {@code null} or empty
     * @param methodName Method name to be invoked
     * @param responseTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for answer. It can be {@code null}.
     * @param connectTimeoutInSeconds Maximum interval of time, in seconds, that the Direct Method will wait for the connection. It can be {@code null}.
     * @param payload Object that contains the payload defined by the user. It can be {@code null}.
     * @param startTimeUtc Date time in Utc to start the job
     * @param maxExecutionTimeInSeconds Max execution time in seconds, i.e., ttl duration the job can run
     * @return a jobResult object
     * @throws IllegalArgumentException if one of the provided parameters is invalid
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public synchronized JobResult scheduleDeviceMethod(
            String jobId,
            String queryCondition,
            String methodName, Long responseTimeoutInSeconds, Long connectTimeoutInSeconds, Object payload,
            Date startTimeUtc,
            long maxExecutionTimeInSeconds)
            throws IllegalArgumentException, IOException, IotHubException
    {
        URL url = null;

        /* Codes_SRS_JOBCLIENT_21_014: [If the JobId is null, empty, or invalid, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if((jobId == null) || jobId.isEmpty())
        {
            throw new IllegalArgumentException("null jobId");
        }

        /* Codes_SRS_JOBCLIENT_21_015: [If the methodName is null or empty, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if((methodName == null) || methodName.isEmpty())
        {
            throw new IllegalArgumentException("null updateTwin");
        }

        /* Codes_SRS_JOBCLIENT_21_016: [If the startTimeUtc is null, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if(startTimeUtc == null)
        {
            throw new IllegalArgumentException("null startTimeUtc");
        }

        /* Codes_SRS_JOBCLIENT_21_017: [If the maxExecutionTimeInSeconds is negative, the scheduleDeviceMethod shall throws IllegalArgumentException.] */
        if(maxExecutionTimeInSeconds < 0)
        {
            throw new IllegalArgumentException("negative maxExecutionTimeInSeconds");
        }

        /* Codes_SRS_JOBCLIENT_21_018: [The scheduleDeviceMethod shall create a json String that represent the invoke method job using the JobsParser class.] */
        MethodParser cloudToDeviceMethod = new MethodParser(methodName, responseTimeoutInSeconds, connectTimeoutInSeconds, payload);
        JobsParser jobsParser = new JobsParser(jobId, cloudToDeviceMethod, queryCondition, startTimeUtc, maxExecutionTimeInSeconds);
        String json = jobsParser.toJson();

        /* Codes_SRS_JOBCLIENT_21_019: [The scheduleDeviceMethod shall create a URL for Jobs using the iotHubConnectionString.] */
        try
        {
            url = iotHubConnectionString.getUrlJobs(jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        /* Codes_SRS_JOBCLIENT_21_020: [The scheduleDeviceMethod shall send a PUT request to the iothub using the created url and json.] */
        /* Codes_SRS_JOBCLIENT_21_021: [If the scheduleDeviceMethod failed to send a PUT request, it shall throw IOException.] */
        /* Codes_SRS_JOBCLIENT_21_022: [If the scheduleDeviceMethod failed to verify the iothub response, it shall throw IotHubException.] */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), String.valueOf(requestId++), USE_DEFAULT_TIMEOUT);

        /* Codes_SRS_JOBCLIENT_21_023: [The scheduleDeviceMethod shall parse the iothub response and return it as JobResult.] */
        return new JobResult(response.getBody());
    }


    /**
     * Get the current job on the iotHub.
     *
     * @param jobId Unique Job Id for this job
     * @return a jobResult object
     * @throws IllegalArgumentException if the jobId is invalid
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public synchronized JobResult getJob(String jobId)
            throws IllegalArgumentException, IOException, IotHubException
    {
        URL url;

        /* Codes_SRS_JOBCLIENT_21_024: [If the JobId is null, empty, or invalid, the getJob shall throws IllegalArgumentException.] */
        if((jobId == null) || jobId.isEmpty())
        {
            throw new IllegalArgumentException("null jobId");
        }

        /* Codes_SRS_JOBCLIENT_21_025: [The getJob shall create a URL for Jobs using the iotHubConnectionString.] */
        try
        {
            url = iotHubConnectionString.getUrlJobs(jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        /* Codes_SRS_JOBCLIENT_21_026: [The getJob shall send a GET request to the iothub using the created url.] */
        /* Codes_SRS_JOBCLIENT_21_027: [If the getJob failed to send a GET request, it shall throw IOException.] */
        /* Codes_SRS_JOBCLIENT_21_028: [If the getJob failed to verify the iothub response, it shall throw IotHubException.] */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.GET, new byte[]{}, String.valueOf(requestId++), USE_DEFAULT_TIMEOUT);

        /* Codes_SRS_JOBCLIENT_21_029: [The getJob shall parse the iothub response and return it as JobResult.] */
        return new JobResult(response.getBody());
    }

    /**
     * Cancel a current jod on the IoTHub
     *
     * @param jobId Unique Job Id for this job
     * @return a jobResult object
     * @throws IllegalArgumentException if the jobId is invalid
     * @throws IOException if the function cannot create a URL for the job, or the IO failed on request
     * @throws IotHubException if the http request failed
     */
    public synchronized JobResult cancelJob(String jobId)
            throws IllegalArgumentException, IOException, IotHubException
    {
        URL url;
        /* Codes_SRS_JOBCLIENT_21_030: [If the JobId is null, empty, or invalid, the cancelJob shall throws IllegalArgumentException.] */
        if((jobId == null) || jobId.isEmpty())
        {
            throw new IllegalArgumentException("null jobId");
        }

        /* Codes_SRS_JOBCLIENT_21_031: [The cancelJob shall create a cancel URL for Jobs using the iotHubConnectionString.] */
        try
        {
            url = iotHubConnectionString.getUrlJobsCancel(jobId);
        }
        catch (MalformedURLException e)
        {
            throw new IllegalArgumentException("Invalid JobId to create url");
        }

        /* Codes_SRS_JOBCLIENT_21_032: [The cancelJob shall send a POST request to the iothub using the created url.] */
        /* Codes_SRS_JOBCLIENT_21_033: [If the cancelJob failed to send a POST request, it shall throw IOException.] */
        /* Codes_SRS_JOBCLIENT_21_034: [If the cancelJob failed to verify the iothub response, it shall throw IotHubException.] */
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.POST, new byte[]{}, String.valueOf(requestId++), USE_DEFAULT_TIMEOUT);

        /* Codes_SRS_JOBCLIENT_21_035: [The cancelJob shall parse the iothub response and return it as JobResult.] */
        return new JobResult(response.getBody());
    }

    private TwinParser getParserFromDevice(DeviceTwinDevice device) throws IOException
    {
        TwinParser twinParser = new TwinParser();
        twinParser.enableTags();

        if(device.getDeviceId() != null)
        {
            twinParser.setDeviceId(device.getDeviceId());
        }

        if(device.getETag() == null)
        {
            twinParser.setETag("*");
        }
        else
        {
            twinParser.setETag(device.getETag());
        }

        if(device.getTags() != null)
        {
            twinParser.resetTags(setToMap(device.getTags()));
        }

        if(device.getDesiredProperties() != null)
        {
            twinParser.resetDesiredProperty(setToMap(device.getDesiredProperties()));
        }

        if(device.getReportedProperties() != null)
        {
            twinParser.resetReportedProperty(setToMap(device.getReportedProperties()));
        }

        return twinParser;
    }

    private Map<String, Object> setToMap(Set<Pair> set)
    {
        Map<String, Object> map = new HashMap<>();

        if (set != null)
        {
            for (Pair p : set)
            {
                map.put(p.getKey(), p.getValue());
            }
        }

        return map;
    }

    @SuppressWarnings("unused")
    protected JobClient()
    {
    }

}
