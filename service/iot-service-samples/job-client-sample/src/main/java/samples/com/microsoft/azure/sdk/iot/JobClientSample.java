/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.jobs.DirectMethodsJobOptions;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobStatus;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobsClient;
import com.microsoft.azure.sdk.iot.service.query.SqlQueryBuilder;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.query.JobQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJobType;

import java.io.IOException;
import java.util.*;

/*
    Jobs on IotHub using the ScheduledJobsClient
 */
public class JobClientSample
{
    /*
     * Details for ScheduledJob client
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
    private static final int responseTimeout = 200;
    private static final int connectTimeout = 5;
    private static final Map<String, Object> payload = new HashMap<String, Object>()
    {
        {
            put("arg1", "value1");
            put("arg2", 20);
        }
    };
    private static final Date startTimeUtc = new Date();
    private static final int maxExecutionTimeInSeconds = 100;

    public static void main(String[] args) throws Exception
    {

        System.out.println("Starting sample...");

        // *************************************** Create ScheduledJobsClient ***************************************
        ScheduledJobsClient jobClient = createJobClient();

        // *************************************** Schedule twin job ***************************************
        ScheduledJob jobTwin = scheduleUpdateTwin(jobClient);
        monitorJob(jobClient, jobIdTwin);

        // *************************************** Schedule method job ***************************************
        ScheduledJob jobMethod = scheduleUpdateMethod(jobClient);
        monitorJob(jobClient, jobIdMethod);

        // *************************************** Query Jobs Response ***************************************
        QueryClient queryClient = new QueryClient(iotHubConnectionString);
        queryJobsResponse(queryClient);

        // *************************************** Query Device ScheduledJob ***************************************
        queryDeviceJobs(queryClient);

        System.out.println("Shutting down sample...");
    }


    private static ScheduledJobsClient createJobClient()
    {
        System.out.println("Create ScheduledJobsClient from the connectionString...");
        ScheduledJobsClient jobClient = new ScheduledJobsClient(iotHubConnectionString);
        System.out.println("ScheduledJobsClient created with success");
        System.out.println();
        return  jobClient;
    }

    private static void monitorJob(ScheduledJobsClient jobClient, String jobId) throws IOException, IotHubException, InterruptedException
    {
        System.out.println("Monitoring jobClient for job completion...");
        ScheduledJob job = jobClient.get(jobId);
        System.out.println("First get response");
        System.out.println(job);
        while(job.getJobStatus() != ScheduledJobStatus.completed)
        {
            Thread.sleep(1000);
            job = jobClient.get(jobId);
        }
        System.out.println("ScheduledJob ends with status " + job.getJobStatus());
        System.out.println("Last get response");
        System.out.println(job);
    }

    private static void queryDeviceJobs(QueryClient queryClient) throws IOException, IotHubException
    {
        System.out.println("Query device job");
        String jobsQueryString = SqlQueryBuilder.createSqlQuery("*", SqlQueryBuilder.FromType.JOBS, null, null);
        JobQueryResponse jobQueryResponse = queryClient.queryJobs(jobsQueryString);
        while (jobQueryResponse.hasNext())
        {
            System.out.println("Query device job response");
            System.out.println(jobQueryResponse.next());
        }
    }

    private static void queryJobsResponse(QueryClient queryClient) throws IOException, IotHubException
    {
        System.out.println("Querying job response");
        JobQueryResponse jobResponseQuery = queryClient.queryJobs(ScheduledJobType.scheduleDeviceMethod, ScheduledJobStatus.completed);
        while (jobResponseQuery.hasNext())
        {
            System.out.println("job response");
            System.out.println(jobResponseQuery.next());
        }
    }

    private static ScheduledJob scheduleUpdateMethod(ScheduledJobsClient jobClient) throws IOException, IotHubException
    {
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        System.out.println("Schedule method job " + jobIdMethod + " for device " + deviceId + "...");

        DirectMethodsJobOptions options =
            DirectMethodsJobOptions.builder()
                .payload(payload)
                .methodConnectTimeoutSeconds(connectTimeout)
                .methodResponseTimeoutSeconds(responseTimeout)
                .maxExecutionTimeSeconds(maxExecutionTimeInSeconds)
                .build();

        ScheduledJob jobMethod =
            jobClient.scheduleDirectMethod(
                jobIdMethod,
                queryCondition,
                methodName,
                startTimeUtc,
                options);

        if(jobMethod == null)
        {
            throw new IOException("Schedule method ScheduledJob returns null");
        }
        System.out.println("Schedule method job response");
        System.out.println(jobMethod);
        System.out.println();
        return jobMethod;
    }

    private static ScheduledJob scheduleUpdateTwin(ScheduledJobsClient jobClient) throws IOException, IotHubException
    {
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";
        Twin updateTwin = new Twin(deviceId);
        updateTwin.getTags().put("HomeID", UUID.randomUUID());

        System.out.println("Schedule twin job " + jobIdTwin + " for device " + deviceId + "...");
        ScheduledJob job = jobClient.scheduleUpdateTwin(jobIdTwin, queryCondition, updateTwin, startTimeUtc , maxExecutionTimeInSeconds);
        System.out.println("Schedule twin job response");
        System.out.println(job);
        System.out.println();
        return job;
    }
}
