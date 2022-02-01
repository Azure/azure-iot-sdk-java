/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot.service.sdk;

import com.microsoft.azure.sdk.iot.service.jobs.DirectMethodsJobOptions;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.twin.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.twin.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.twin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Manages device Method operations on IotHub */
public class DeviceMethodSample
{
    public static final String iotHubConnectionString = "[IOT HUB Connection String]";
    public static final String deviceId = "[Device ID]";

    private static final long WAIT_1_SECOND_TO_CANCEL_IN_MILLISECONDS = 1000L;
    private static final long GIVE_100_MILLISECONDS_TO_IOTHUB = 100L;
    private static final long ADD_10_SECONDS_IN_MILLISECONDS = 10000L;
    private static final long ADD_10_MINUTES_IN_MILLISECONDS = 600000L;
    private static final int MAX_EXECUTION_TIME_IN_SECONDS = 100;

    private static final String methodName = "[Function Name]";
    private static final int responseTimeout = 200;
    private static final int connectTimeout = 5;
    private static final Map<String, Object> payload = new HashMap<String, Object>()
    {
        {
            put("arg1", "value1");
            put("arg2", 20);
        }
    };

    /**
     * Directly invoke method on remote device.
     *
     * @param args not used.
     * @throws Exception Throws Exception if sample fails
     */
    public static void main(String[] args) throws Exception
    {
        System.out.println("Starting sample...");
        System.out.println("Creating the Device Method");
        DirectMethodsClient methodClient = new DirectMethodsClient(iotHubConnectionString);
        JobClient jobClient = new JobClient(iotHubConnectionString);

        try
        {
            // Manage complete Method
            // ================================ invoke method on device ===============================
            invokeMethod(methodClient);

            // ================================= schedule invoke method ===============================
            scheduleInvokeMethod(methodClient, jobClient);

            // ================================== cancel job scheduled ================================
            cancelScheduleInvokeMethod(methodClient, jobClient);
        }
        catch (IotHubException | IOException e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println("Shutting down sample...");
    }

    private static void invokeMethod(DirectMethodsClient methodClient) throws IotHubException, IOException
    {
        System.out.println("directly invoke method on the Device");
        DirectMethodRequestOptions options =
            DirectMethodRequestOptions.builder()
                .payload(payload)
                .methodConnectTimeout(connectTimeout)
                .methodResponseTimeout(responseTimeout)
                .build();

        MethodResult result = methodClient.invoke(deviceId, methodName, options);
        if(result == null)
        {
            throw new IOException("Method invoke returns null");
        }
        System.out.println("Status=" + result.getStatus());
        System.out.println("Payload=" + result.getPayload());
    }

    private static void scheduleInvokeMethod(DirectMethodsClient methodClient, JobClient jobClient) throws IotHubException, IOException, InterruptedException
    {
        // query condition that defines the list of device to invoke
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        // date when the invoke shall be executed
        Date invokeDateInFuture = new Date(new Date().getTime() + ADD_10_SECONDS_IN_MILLISECONDS); // 10 seconds in the future.

        System.out.println("Schedule invoke method on the Device in 10 seconds");
        DirectMethodsJobOptions options =
            DirectMethodsJobOptions.builder()
                .payload(payload)
                .methodConnectTimeout(connectTimeout)
                .methodResponseTimeout(responseTimeout)
                .maxExecutionTimeInSeconds(MAX_EXECUTION_TIME_IN_SECONDS)
                .build();

        String jobId = UUID.randomUUID().toString();
        JobResult job = jobClient.scheduleDirectMethod(jobId, queryCondition, methodName, invokeDateInFuture, options);

        System.out.println("Wait for job completed...");
        while (job.getJobStatus() != JobStatus.completed)
        {
            Thread.sleep(GIVE_100_MILLISECONDS_TO_IOTHUB);
            job = jobClient.getJob(jobId);
        }
        System.out.println("job completed");
    }

    private static void cancelScheduleInvokeMethod(DirectMethodsClient methodClient, JobClient jobClient) throws IotHubException, IOException, InterruptedException
    {
        // query condition that defines the list of device to invoke
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        // date when the invoke shall be executed
        Date invokeDateInFuture = new Date(new Date().getTime() + ADD_10_MINUTES_IN_MILLISECONDS); // 10 minutes in the future.

        System.out.println("Schedule invoke method on the Device in 10 minutes");
        DirectMethodsJobOptions options =
            DirectMethodsJobOptions.builder()
                .payload(payload)
                .methodConnectTimeout(connectTimeout)
                .methodResponseTimeout(responseTimeout)
                .maxExecutionTimeInSeconds(MAX_EXECUTION_TIME_IN_SECONDS)
                .build();

        String jobId = UUID.randomUUID().toString();
        JobResult job = jobClient.scheduleDirectMethod(jobId, queryCondition, methodName, invokeDateInFuture, options);

        Thread.sleep(WAIT_1_SECOND_TO_CANCEL_IN_MILLISECONDS);
        System.out.println("Cancel job after 1 second");
        jobClient.cancelJob(jobId);

        System.out.println("Wait for job cancelled...");
        while (job.getJobStatus() != JobStatus.cancelled)
        {
            Thread.sleep(GIVE_100_MILLISECONDS_TO_IOTHUB);
            job = jobClient.getJob(jobId);
        }
        System.out.println("job cancelled");
    }
}
