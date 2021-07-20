/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.SqlQuery;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.JobType;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/*
    Jobs on IotHub using the JobClient
 */
public class JobClientSample
{
    /*
     * Details for Job client
     */
    public static final String iotHubConnectionString = "[IOT HUB Connection String]";
    public static final String deviceId = "[Device ID]";

    /*
     * Details for scheduling twin
     */
    private static final String jobIdTwin = "DHCMD" + UUID.randomUUID();

    /*
     * Details for scheduling method
     */
    private static final String jobIdMethod = "DHCMD" + UUID.randomUUID();
    private static final String methodName = "reboot";
    private static final Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
    private static final Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);
    private static final Map<String, Object> payload = new HashMap<String, Object>()
    {
        {
            put("arg1", "value1");
            put("arg2", 20);
        }
    };
    private static final Date startTimeUtc = new Date();
    private static final long maxExecutionTimeInSeconds = 100;

    public static void main(String[] args) throws Exception
    {

        System.out.println("Starting sample...");

        // *************************************** Create JobClient ***************************************
        JobClient jobClient = createJobClient();

        // *************************************** Schedule twin job ***************************************
        JobResult jobResultTwin = scheduleUpdateTwin(jobClient);
        monitorJob(jobClient, jobIdTwin);

        // *************************************** Schedule method job ***************************************
        JobResult jobResultMethod = scheduleUpdateMethod(jobClient);
        monitorJob(jobClient, jobIdMethod);

        // *************************************** Query Jobs Response ***************************************
        queryJobsResponse(jobClient);

        // *************************************** Query Device Job ***************************************
        queryDeviceJobs(jobClient);

        System.out.println("Shutting down sample...");
    }


    private static JobClient createJobClient() throws IOException
    {
        System.out.println("Create JobClient from the connectionString...");
        JobClient jobClient = new JobClient(iotHubConnectionString);
        System.out.println("JobClient created with success");
        System.out.println();
        return  jobClient;
    }

    private static void monitorJob(JobClient jobClient, String jobId) throws IOException, IotHubException, InterruptedException
    {
        System.out.println("Monitoring jobClient for job completion...");
        JobResult jobResult = jobClient.getJob(jobId);
        System.out.println("First get response");
        System.out.println(jobResult);
        while(jobResult.getJobStatus() != JobStatus.completed)
        {
            Thread.sleep(1000);
            jobResult = jobClient.getJob(jobId);
        }
        System.out.println("Job ends with status " + jobResult.getJobStatus());
        System.out.println("Last get response");
        System.out.println(jobResult);
    }

    private static void queryDeviceJobs(JobClient jobClient) throws IOException, IotHubException
    {
        System.out.println("Query device job");
        Query deviceJobQuery = jobClient.queryDeviceJob(SqlQuery.createSqlQuery("*", SqlQuery.FromType.JOBS, null, null).getQuery());
        while (jobClient.hasNextJob(deviceJobQuery))
        {
            System.out.println("Query device job response");
            System.out.println(jobClient.getNextJob(deviceJobQuery));
        }
    }

    private static void queryJobsResponse(JobClient jobClient) throws IOException, IotHubException
    {
        System.out.println("Querying job response");
        Query jobResponseQuery = jobClient.queryJobResponse(JobType.scheduleDeviceMethod, JobStatus.completed);
        while (jobClient.hasNextJob(jobResponseQuery))
        {
            System.out.println("job response");
            System.out.println(jobClient.getNextJob(jobResponseQuery));
        }
    }

    private static JobResult scheduleUpdateMethod(JobClient jobClient) throws IOException, IotHubException
    {
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        System.out.println("Schedule method job " + jobIdMethod + " for device " + deviceId + "...");
        JobResult jobResultMethod = jobClient.scheduleDeviceMethod(jobIdMethod, queryCondition, methodName, responseTimeout, connectTimeout, payload, startTimeUtc, maxExecutionTimeInSeconds);
        if(jobResultMethod == null)
        {
            throw new IOException("Schedule method Job returns null");
        }
        System.out.println("Schedule method job response");
        System.out.println(jobResultMethod);
        System.out.println();
        return jobResultMethod;
    }

    private static JobResult scheduleUpdateTwin(JobClient jobClient) throws IOException, IotHubException
    {
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        DeviceTwinDevice updateTwin = new DeviceTwinDevice(deviceId);
        Set<Pair> tags = new HashSet<>();
        tags.add(new Pair("HomeID", UUID.randomUUID()));
        updateTwin.setTags(tags);

        System.out.println("Schedule twin job " + jobIdTwin + " for device " + deviceId + "...");
        JobResult jobResult = jobClient.scheduleUpdateTwin(jobIdTwin, queryCondition, updateTwin, startTimeUtc , maxExecutionTimeInSeconds);
        if(jobResult == null)
        {
            throw new IOException("Schedule Twin Job returns null");
        }
        System.out.println("Schedule twin job response");
        System.out.println(jobResult);
        System.out.println();
        return jobResult;
    }
}
