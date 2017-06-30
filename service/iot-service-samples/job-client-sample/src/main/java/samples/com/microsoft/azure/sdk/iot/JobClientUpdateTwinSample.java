/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

<<<<<<< HEAD
package samples.com.microsoft.azure.sdk.iot.service.sdk;
=======
package samples.com.microsoft.azure.sdk.iot;
>>>>>>> master

import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/*
    Schedule an update Twin job on IotHub using the JobClient
 */
public class JobClientUpdateTwinSample
{
    private static final String iotHubConnectionString = "[IOT HUB Connection String]";

    public static void main(String[] args) throws Exception
    {
        final String jobId = "DHCMD" + UUID.randomUUID();
        final String deviceId = "JavaTest";
        final String queryCondition = "DeviceId IN ['" + deviceId + "']";

        DeviceTwinDevice updateTwin = new DeviceTwinDevice(deviceId);
        Set<Pair> tags = new HashSet<Pair>();
        tags.add(new Pair("HomeID", UUID.randomUUID()));
        updateTwin.setTags(tags);

        final Date startTimeUtc = new Date();
        final long maxExecutionTimeInSeconds = 100;

        System.out.println("Starting sample...");

        // *************************************** Create JobClient ***************************************
        System.out.println("Create JobClient from the connectionString...");
        JobClient jobClient = JobClient.createFromConnectionString(iotHubConnectionString);
        System.out.println("JobClient created with success");
        System.out.println();

        // *************************************** Schedule twin job ***************************************
        System.out.println("Schedule twin job " + jobId + " for device " + deviceId + "...");
        JobResult jobResult = jobClient.scheduleUpdateTwin(jobId, queryCondition, updateTwin, startTimeUtc , maxExecutionTimeInSeconds);
        if(jobResult == null)
        {
            throw new IOException("Schedule Twin Job returns null");
        }
        System.out.println("Schedule twin job response");
        System.out.println(jobResult.toString());
        System.out.println();

        // *************************************** Check completion ***************************************
        System.out.println("Monitoring jobClient for job completion...");
        jobResult = jobClient.getJob(jobId);
        System.out.println("First get response");
        System.out.println(jobResult.toString());
        while(jobResult.getJobStatus() != JobResult.JobStatus.completed)
        {
            Thread.sleep(100);
            jobResult = jobClient.getJob(jobId);
        }
        System.out.println("Job ends with status " + jobResult.getJobStatus());
        System.out.println("Last get response");
        System.out.println(jobResult.toString());
        System.out.println();

        System.out.println("Shutting down sample...");
    }

}
