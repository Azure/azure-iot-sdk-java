/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot.service.sdk;

import com.microsoft.azure.sdk.iot.service.devicetwin.DirectMethodClient;
import com.microsoft.azure.sdk.iot.service.devicetwin.Job;
import com.microsoft.azure.sdk.iot.service.devicetwin.MethodResult;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import com.microsoft.azure.sdk.iot.service.jobs.JobStatus;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Manages device Method operations on IotHub */
public class DeviceMethodSample
{
    public static final String iotHubConnectionString = "[IOT HUB Connection String]";
    public static final String deviceId = "[Device ID]";

    private static final long WAIT_1_SECOND_TO_CANCEL_IN_MILLISECONDS = 1000L;
    private static final long GIVE_100_MILLISECONDS_TO_IOTHUB = 100L;
    private static final long ADD_10_SECONDS_IN_MILLISECONDS = 10000L;
    private static final long ADD_10_MINUTES_IN_MILLISECONDS = 600000L;
    private static final long MAX_EXECUTION_TIME_IN_SECONDS = 100L;

    private static final String methodName = "[Function Name]";
    private static final Long responseTimeout = TimeUnit.SECONDS.toSeconds(200);
    private static final Long connectTimeout = TimeUnit.SECONDS.toSeconds(5);
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
        DirectMethodClient methodClient = new DirectMethodClient(iotHubConnectionString);

        try
        {
            // Manage complete Method
            // ================================ invoke method on device ===============================
            invokeMethod(methodClient);

            // ================================= schedule invoke method ===============================
            scheduleInvokeMethod(methodClient);

            // ================================== cancel job scheduled ================================
            cancelScheduleInvokeMethod(methodClient);
        }
        catch (IotHubException | IOException e)
        {
            System.out.println(e.getMessage());
        }

        System.out.println("Shutting down sample...");
    }

    private static void invokeMethod(DirectMethodClient methodClient) throws IotHubException, IOException
    {
        System.out.println("directly invoke method on the Device");
        MethodResult result = methodClient.invoke(deviceId, methodName, responseTimeout, connectTimeout, payload);
        if(result == null)
        {
            throw new IOException("Method invoke returns null");
        }
        System.out.println("Status=" + result.getStatus());
        System.out.println("Payload=" + result.getPayload());
    }

    private static void scheduleInvokeMethod(DirectMethodClient methodClient) throws IotHubException, IOException, InterruptedException
    {
        // query condition that defines the list of device to invoke
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        // date when the invoke shall be executed
        Date invokeDateInFuture = new Date(new Date().getTime() + ADD_10_SECONDS_IN_MILLISECONDS); // 10 seconds in the future.

        System.out.println("Schedule invoke method on the Device in 10 seconds");
        Job job = methodClient.scheduleDeviceMethod(queryCondition, methodName, responseTimeout, connectTimeout, payload, invokeDateInFuture, MAX_EXECUTION_TIME_IN_SECONDS);

        System.out.println("Wait for job completed...");
        JobResult jobResult = job.get();
        while (jobResult.getJobStatus() != JobStatus.completed)
        {
            Thread.sleep(GIVE_100_MILLISECONDS_TO_IOTHUB);
            jobResult = job.get();
        }
        System.out.println("job completed");
    }

    private static void cancelScheduleInvokeMethod(DirectMethodClient methodClient) throws IotHubException, IOException, InterruptedException
    {
        // query condition that defines the list of device to invoke
        String queryCondition = "DeviceId IN ['" + deviceId + "']";

        // date when the invoke shall be executed
        Date invokeDateInFuture = new Date(new Date().getTime() + ADD_10_MINUTES_IN_MILLISECONDS); // 10 minutes in the future.

        System.out.println("Schedule invoke method on the Device in 10 minutes");
        Job job = methodClient.scheduleDeviceMethod(queryCondition, methodName, responseTimeout, connectTimeout, payload, invokeDateInFuture, MAX_EXECUTION_TIME_IN_SECONDS);

        Thread.sleep(WAIT_1_SECOND_TO_CANCEL_IN_MILLISECONDS);
        System.out.println("Cancel job after 1 second");
        job.cancel();

        System.out.println("Wait for job cancelled...");
        JobResult jobResult = job.get();
        while (jobResult.getJobStatus() != JobStatus.cancelled)
        {
            Thread.sleep(GIVE_100_MILLISECONDS_TO_IOTHUB);
            jobResult = job.get();
        }
        System.out.println("job cancelled");
    }
}
