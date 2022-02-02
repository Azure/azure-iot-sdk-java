/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.jobs.Job;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.query.SqlQuery;
import com.microsoft.azure.sdk.iot.service.query.TwinQueryResponse;
import com.microsoft.azure.sdk.iot.service.twin.*;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;

import java.io.IOException;
import java.util.*;

/** Manages device twin operations on IotHub */
public class DeviceTwinSample
{
    private static final String iotHubConnectionString  = System.getenv("IOTHUB_CONNECTION_STRING");
    private static final String deviceId = System.getenv("IOTHUB_DEVICE_ID");

    private static final int TEMPERATURE_RANGE = 100;
    private static final int HUMIDITY_RANGE = 100;
    private static final long WAIT_1_SECOND_TO_CANCEL_IN_MILLISECONDS = 1000L;
    private static final long GIVE_100_MILLISECONDS_TO_IOTHUB = 100L;
    private static final long ADD_10_SECONDS_IN_MILLISECONDS = 10000L;
    private static final long ADD_10_MINUTES_IN_MILLISECONDS = 600000L;
    private static final long MAX_EXECUTION_TIME_IN_SECONDS = 100L;

    /**
     * Manages device twin operations on IotHub
     *
     * @param args not used.
     * @throws Exception Throws Exception if sample fails
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        System.out.println("Creating the Device Twin");
        TwinClient twinClient = new TwinClient(iotHubConnectionString);
        JobClient jobClient = new JobClient(iotHubConnectionString);
        QueryClient queryClient = new QueryClient(iotHubConnectionString);

        try
        {
            // Manage complete twin
            // ============================== get initial twin properties =============================
            Twin device = getInitialState(twinClient, deviceId);

            // ================================ patch desired property ===============================
            patchDesiredProperties(twinClient, device);

            // ================================ replace desired property ===============================
            replaceDesiredProperties(twinClient, device);

            // ============================ schedule update desired property ==========================
            scheduleUpdateDesiredProperties(twinClient, device, jobClient);

            // =================================== cancel job scheduled ===============================
            cancelJob(twinClient, device, jobClient);

            // ================================= remove desired property ==============================
            removeDesiredProperties(twinClient, device);

            // ======================================= query twin =====================================
            queryTwin(queryClient);
        }
        catch (IotHubException | IOException e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println("Shutting down sample...");
    }

    private static Twin getInitialState(TwinClient twinClient, String deviceId) throws IOException, IotHubException
    {
        System.out.println("Getting the Device twin");
        Twin device = twinClient.getTwin(deviceId);
        System.out.println(device);

        //Update Twin Tags and Desired Properties
        Set<Pair> tags = new HashSet<>();
        tags.add(new Pair("HomeID", UUID.randomUUID()));
        device.setTags(tags);
        return device;
    }

    private static void patchDesiredProperties(TwinClient twinClient, Twin device) throws IOException, IotHubException
    {
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("temp", new Random().nextInt(TEMPERATURE_RANGE)));
        desiredProperties.add(new Pair("hum", new Random().nextInt(HUMIDITY_RANGE)));
        device.setDesiredProperties(desiredProperties);

        System.out.println("Updating Device twin (new temp, hum)");
        twinClient.updateTwin(device);

        System.out.println("Getting the updated Device twin");
        device = twinClient.getTwin(device.getDeviceId());
        System.out.println(device);
    }

    private static void replaceDesiredProperties(TwinClient twinClient, Twin device) throws IOException, IotHubException
    {
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("temp", new Random().nextInt(TEMPERATURE_RANGE)));
        device.setDesiredProperties(desiredProperties);

        // By replacing the twin rather than patching it, any desired properties that existed on the twin prior to this call
        // that aren't present on the new set of desired properties will be deleted.
        System.out.println("Replacing Device twin");
        device = twinClient.replaceTwin(device);

        System.out.println("Getting the updated Device twin");
        device = twinClient.getTwin(device.getDeviceId());
        System.out.println(device);
    }

    private static void removeDesiredProperties(TwinClient twinClient, Twin device) throws IOException, IotHubException
    {
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("hum", null));
        device.setDesiredProperties(desiredProperties);
        System.out.println("Updating Device twin (remove hum)");
        twinClient.updateTwin(device);

        System.out.println("Getting the updated Device twin");
        device = twinClient.getTwin(device.getDeviceId());
        System.out.println(device);
    }

    private static void scheduleUpdateDesiredProperties(TwinClient twinClient, Twin device, JobClient jobClient) throws IOException, IotHubException, InterruptedException
    {
        // new set of desired properties
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("temp", new Random().nextInt(TEMPERATURE_RANGE)));
        desiredProperties.add(new Pair("hum", new Random().nextInt(HUMIDITY_RANGE)));
        device.setDesiredProperties(desiredProperties);
        // ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag
        device.setETag("*");

        // date when the update shall be executed
        Date updateDateInFuture = new Date(new Date().getTime() + ADD_10_SECONDS_IN_MILLISECONDS); // 10 seconds in the future.

        // query condition that defines the list of device to be updated
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        System.out.println("Schedule updating Device twin (new temp, hum) in 10 seconds");
        String jobId = UUID.randomUUID().toString();
        Job job = jobClient.scheduleUpdateTwin(jobId, queryCondition, device, updateDateInFuture, MAX_EXECUTION_TIME_IN_SECONDS);

        System.out.println("Wait for job completed...");
        while (job.getJobStatus() != JobStatus.completed)
        {
            Thread.sleep(GIVE_100_MILLISECONDS_TO_IOTHUB);
            job = jobClient.getJob(jobId);
        }
        System.out.println("job completed");

        System.out.println("Getting the updated Device twin");
        device = twinClient.getTwin(device.getDeviceId());
        System.out.println(device);
    }

    private static void cancelJob(TwinClient twinClient, Twin device, JobClient jobClient) throws IOException, IotHubException, InterruptedException
    {
        // new set of desired properties
        Set<Pair> desiredProperties = new HashSet<>();
        desiredProperties.add(new Pair("temp", new Random().nextInt(TEMPERATURE_RANGE)));
        desiredProperties.add(new Pair("hum", new Random().nextInt(HUMIDITY_RANGE)));
        device.setDesiredProperties(desiredProperties);
        // ScheduleUpdateTwin job type is a force update, which only accepts '*' as the Etag
        device.setETag("*");

        // date when the update shall be executed
        Date updateDateInFuture = new Date(new Date().getTime() + ADD_10_MINUTES_IN_MILLISECONDS); // 10 minutes in the future.

        // query condition that defines the list of device to be updated
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        System.out.println("Cancel updating Device twin (new temp, hum) in 10 minutes");
        String jobId = UUID.randomUUID().toString();
        Job job = jobClient.scheduleUpdateTwin(jobId, queryCondition, device, updateDateInFuture, MAX_EXECUTION_TIME_IN_SECONDS);

        Thread.sleep(WAIT_1_SECOND_TO_CANCEL_IN_MILLISECONDS);
        System.out.println("Cancel job after 1 second");
        jobClient.cancelJob(jobId);

        System.out.println("Wait for job cancelled...");
        job = jobClient.getJob(jobId);
        while (job.getJobStatus() != JobStatus.cancelled)
        {
            Thread.sleep(GIVE_100_MILLISECONDS_TO_IOTHUB);
            job = jobClient.getJob(jobId);
        }
        System.out.println("job cancelled");

        System.out.println("Getting the updated Device twin (no changes)");
        device = twinClient.getTwin(device.getDeviceId());
        System.out.println(device);
    }

    private static void queryTwin(QueryClient queryClient) throws IOException, IotHubException
    {
        System.out.println("Started Querying twin");

        SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, null, null);
        TwinQueryResponse twinQueryResponse = queryClient.queryTwins(sqlQuery.getQuery());

        while (twinQueryResponse.hasNext())
        {
            Twin queriedTwin = twinQueryResponse.next();
            System.out.println(queriedTwin);
        }
    }
}
