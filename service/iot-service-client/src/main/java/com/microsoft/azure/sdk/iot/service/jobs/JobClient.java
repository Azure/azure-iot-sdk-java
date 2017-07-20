// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.jobs;

import com.microsoft.azure.sdk.iot.deps.serializer.JobsParser;
import com.microsoft.azure.sdk.iot.deps.serializer.MethodParser;
import com.microsoft.azure.sdk.iot.deps.serializer.TwinParser;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionString;
import com.microsoft.azure.sdk.iot.service.IotHubConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.service.devicetwin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.service.transport.http.HttpResponse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * JobClient enables service client to schedule and cancel jobs for a group of devices using the IoTHub.
 */
public class JobClient
{
    private final static long USE_DEFAULT_TIMEOUT = 0L;
    private final static long MAX_TIMEOUT = Integer.MAX_VALUE - 24000;
    private final static Integer DEFAULT_PAGE_SIZE = 100;

    private final static byte[] EMPTY_JSON = "{}".getBytes();

    private IotHubConnectionString iotHubConnectionString = null;

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
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), null, USE_DEFAULT_TIMEOUT);

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
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.PUT, json.getBytes(StandardCharsets.UTF_8), null, USE_DEFAULT_TIMEOUT);

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
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.GET, new byte[]{}, null, USE_DEFAULT_TIMEOUT);

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
        HttpResponse response = DeviceOperations.request(this.iotHubConnectionString, url, HttpMethod.POST, EMPTY_JSON, null, USE_DEFAULT_TIMEOUT);

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

    /**
     * Query for device Job
     * @param sqlQuery sql style query over device.jobs
     * @param pageSize the value per which to limit the size of query response by.
     * @return Query object for this query
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException When any of the parameters are incorrect
     */
    public synchronized Query queryDeviceJob(String sqlQuery, Integer pageSize) throws IotHubException, IOException
    {
        if (sqlQuery == null || sqlQuery.length() == 0)
        {
            //CodesSRS_JOBCLIENT_25_036: [If the sqlQuery is null, the queryDeviceJob shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("Query cannot be null or empty");
        }

        if (pageSize <= 0)
        {
            //Codes_SRS_DEVICETWIN_25_048: [ The method shall throw IllegalArgumentException if the page size is zero or negative.]
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        //Codes_SRS_JOBCLIENT_25_039: [The queryDeviceJob shall create a query object for the type DEVICE_JOB.]
        Query deviceJobQuery = new Query(sqlQuery, pageSize, QueryType.DEVICE_JOB);

        //Codes_SRS_JOBCLIENT_25_040: [The queryDeviceJob shall send a query request on the query object using Query URL, HTTP POST method and wait for the response by calling sendQueryRequest.]
        deviceJobQuery.sendQueryRequest(iotHubConnectionString, iotHubConnectionString.getUrlTwinQuery(), HttpMethod.POST, MAX_TIMEOUT);
        return deviceJobQuery;
    }

    /**
     * Query for device Job limited by default page size of 100 for response
     * @param sqlQuery sql style query over device.jobs
     * @return Query object for this query
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException When any of the parameters are incorrect
     */
    public synchronized Query queryDeviceJob(String sqlQuery) throws IotHubException, IOException
    {
        //Codes_SRS_JOBCLIENT_25_038: [If the pageSize is not specified, default pageSize of 100 shall be used .]
        return queryDeviceJob(sqlQuery, DEFAULT_PAGE_SIZE);
    }

    /**
     * returns the availability of next job result in response. Query's further if page size has been met
     * @param query Query for which to look for next job response by
     * @return true if next job result is available , false otherwise
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException if any of the input parameters are incorrect
     */
    public synchronized boolean hasNextJob(Query query) throws IotHubException, IOException
    {
        if (query == null)
        {
            //Codes_SRS_JOBCLIENT_25_046: [If the input query is null, the hasNextJob shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("Query cannot be null");
        }
        // Codes_SRS_JOBCLIENT_25_047: [hasNextJob shall return true if the next job exist, false other wise.]
        return query.hasNext();
    }

    /**
     * returns the next job result in response. Query's further if page size has been met and has next is not called
     * @param query Query for which to look for next job response by
     * @return next job result if available
     * @throws IotHubException When IotHub fails to respond
     * @throws IOException if any of the input parameters are incorrect
     * @throws NoSuchElementException if called when no further responses are left
     */
    public synchronized JobResult getNextJob(Query query) throws IOException, IotHubException, NoSuchElementException
    {
        if (query == null)
        {
            //Codes_SRS_JOBCLIENT_25_048: [If the input query is null, the getNextJob shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("Query cannot be null");
        }
        //Codes_SRS_JOBCLIENT_25_049: [getNextJob shall return next Job Result if the exist, and throw NoSuchElementException other wise.]
        Object nextObject = query.next();

        if (nextObject instanceof String)
        {
            //Codes_SRS_JOBCLIENT_25_051: [getNextJob method shall parse the next job element from the query response provide the response as JobResult object.]
            String deviceJobJson = (String) nextObject;
            return new JobResult(deviceJobJson.getBytes());
        }
        else
        {
            //Codes_SRS_JOBCLIENT_25_050: [getNextJob shall throw IOException if next Job Result exist and is not a string.]
            throw new IOException("Received a response that could not be parsed");
        }
    }

    /**
     * Query the iot hub for a jobs response. Query response are limited by page size per attempt
     * @param jobType The type of job to query for
     * @param jobStatus The status of the job to query for
     * @param pageSize The value to which to limit the job response size by
     * @return A query object on which to look for responses by
     * @throws IOException If any of the input parameters are incorrect
     * @throws IotHubException If IotHub failed to respond
     */
    public synchronized Query queryJobResponse(JobType jobType, JobStatus jobStatus, Integer pageSize) throws IOException, IotHubException
    {
        if (jobType == null || jobStatus == null)
        {
            //Codes_SRS_JOBCLIENT_25_041: [If the input parameters are null, the queryJobResponse shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("jobType and jobStatus cannot be null");
        }

        if (pageSize <= 0)
        {
            //Codes_SRS_JOBCLIENT_25_042: [If the pageSize is null, zero or negative, the queryJobResponse shall throw IllegalArgumentException.]
            throw new IllegalArgumentException("pagesize cannot be negative or zero");
        }

        //Codes_SRS_JOBCLIENT_25_043: [If the pageSize is not specified, default pageSize of 100 shall be used.] SRS_JOBCLIENT_25_044: [The queryDeviceJob shall create a query object for the type JOB_RESPONSE.]
        Query jobResponseQuery = new Query(pageSize, QueryType.JOB_RESPONSE);

        //Codes_SRS_JOBCLIENT_25_045: [The queryDeviceJob shall send a query request on the query object using Query URL, HTTP GET method and wait for the response by calling sendQueryRequest.]
        jobResponseQuery.sendQueryRequest(iotHubConnectionString, iotHubConnectionString.getUrlQuery(jobType.toString(), jobStatus.toString()), HttpMethod.GET, MAX_TIMEOUT);
        return jobResponseQuery;
    }
    /**
     * Query the iot hub for a jobs response. Query response are limited by default page size per attempt
     * @param jobType The type of job to query for
     * @param jobStatus The status of the job to query for
     * @return A query object on which to look for responses by
     * @throws IOException If any of the input parameters are incorrect
     * @throws IotHubException If IotHub failed to respond
     */
    public synchronized Query queryJobResponse(JobType jobType, JobStatus jobStatus) throws IotHubException, IOException
    {
        //Codes_SRS_JOBCLIENT_25_043: [If the pageSize is not specified, default pageSize of 100 shall be used.]
        return queryJobResponse(jobType, jobStatus, DEFAULT_PAGE_SIZE);
    }

    @SuppressWarnings("unused")
    protected JobClient()
    {
    }
}
