/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.azure.sdk.iot.deps.serializer.ErrorCodeDescription;
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.FeedbackReceiver;
import com.microsoft.azure.sdk.iot.service.FeedbackRecord;
import com.microsoft.azure.sdk.iot.service.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.FileUploadNotificationReceiver;
import com.microsoft.azure.sdk.iot.service.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.Message;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.RegistryManagerOptions;
import com.microsoft.azure.sdk.iot.service.ServiceClient;
import com.microsoft.azure.sdk.iot.service.ServiceClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethod;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceMethodClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinClientOptions;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.SqlQuery;
import com.microsoft.azure.sdk.iot.service.digitaltwin.DigitalTwinClient;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import com.microsoft.azure.sdk.iot.service.jobs.JobClient;
import com.microsoft.azure.sdk.iot.service.jobs.JobClientOptions;
import com.microsoft.azure.sdk.iot.service.jobs.JobResult;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.UUID;

/**
 * This sample demonstrates how to use the constructors in the various service clients that take an instance of
 * {@link TokenCredential} in order to authenticate with role based access credentials.
 */
@Slf4j
public class RoleBasedAuthenticationSample
{
    private static final int FILE_UPLOAD_NOTIFICATION_LISTEN_SECONDS = 5 * 1000; // 5 seconds
    private static final int FEEDBACK_MESSAGE_LISTEN_SECONDS = 5 * 1000; // 5 seconds

    public static void main(String[] args)
    {
        SamplesArguments parsedArguments = new SamplesArguments(args);

        // Credentials can be built from types from the Azure Identity library like ClientSecretCredential.
        // The Azure Identity library also defines other implementations of the TokenCredential interface such as
        // DefaultAzureCredential, InteractiveBrowserCredential, and many others.
        TokenCredential credential =
            new ClientSecretCredentialBuilder()
                .tenantId(parsedArguments.getTenantId())
                .clientId(parsedArguments.getClientId())
                .clientSecret(parsedArguments.getClientSecret())
                .authorityHost("https://login.windows-ppe.net")
                .build();

        // "my-azure-iot-hub.azure-devices.net" for example
        String iotHubHostName = parsedArguments.getIotHubHostName();

        String newDeviceId = runRegistryManagerSample(iotHubHostName, credential);

        //runTwinClientSample(iotHubHostName, credential, newDeviceId);

        //runServiceClientSample(iotHubHostName, credential, newDeviceId);

        runJobClientSample(iotHubHostName, credential);

        //runDeviceMethodClientSample(iotHubHostName, credential, newDeviceId);
    }

    private static String runRegistryManagerSample(String iotHubHostName, TokenCredential credential)
    {
        // RegistryManager has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        RegistryManagerOptions options = RegistryManagerOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        RegistryManager registryManager = new RegistryManager(iotHubHostName, credential, options);

        String deviceId = "my-new-device-" + UUID.randomUUID().toString();
        Device newDevice = Device.createDevice(deviceId, AuthenticationType.SAS);

        try
        {
            log.info("Creating device {}", deviceId);
            registryManager.addDevice(newDevice);
        }
        catch (IOException | IotHubException e)
        {
            log.error("Failed to register new device", e);
            System.exit(-1);
        }

        return deviceId;
    }

    private static void runTwinClientSample(String iotHubHostName, TokenCredential credential, String deviceId)
    {
        // DeviceTwin has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        DeviceTwinClientOptions options = DeviceTwinClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        DeviceTwin twinClient = new DeviceTwin(iotHubHostName, credential, options);

        DeviceTwinDevice newDeviceTwin = new DeviceTwinDevice(deviceId);

        try
        {
            log.info("Getting twin for device {}", deviceId);
            twinClient.getTwin(newDeviceTwin);
        }
        catch (IotHubException | IOException e)
        {
            log.error("Failed to get the twin of the new device", e);
            System.exit(-1);
        }

        log.info("Successfully got the twin for the new device");
        log.info("Device Id: {}", newDeviceTwin.getDeviceId());
        log.info("ETag: {}", newDeviceTwin.getETag());
    }

    private static void runServiceClientSample(String iotHubHostName, TokenCredential credential, String deviceId)
    {
        // ServiceClient has some configurable options for setting a custom SSLContext, as well as for setting proxies.
        // For this sample, the default options will be used though.
        ServiceClientOptions options = ServiceClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        ServiceClient serviceClient =
            new ServiceClient(
                iotHubHostName,
                credential,
                IotHubServiceClientProtocol.AMQPS,
                options);

        String cloudToDeviceMessagePayload = "This is a message sent by an RBAC authenticated service client!";
        Message cloudToDeviceMessage = new Message(cloudToDeviceMessagePayload.getBytes());
        try
        {
            log.info("Sending cloud to device message to the new device");
            serviceClient.send(deviceId, cloudToDeviceMessage);
            log.info("Successfully sent cloud to device message to the new device");
        }
        catch (IOException | IotHubException e)
        {
            log.error("Failed to send a cloud to device message to the new device", e);
            System.exit(-1);
        }

        try
        {
            // FeedbackReceiver will use the same authentication mechanism that the ServiceClient itself uses,
            // so the below APIs are also RBAC authenticated.
            FeedbackReceiver feedbackReceiver = serviceClient.getFeedbackReceiver();

            log.info("Opening feedback receiver to listen for feedback messages");
            feedbackReceiver.open();
            FeedbackBatch feedbackBatch = feedbackReceiver.receive(FEEDBACK_MESSAGE_LISTEN_SECONDS);

            if (feedbackBatch != null)
            {
                for (FeedbackRecord feedbackRecord : feedbackBatch.getRecords())
                {
                    log.info("Feedback record received for device {} with status {}", feedbackRecord.getDeviceId(), feedbackRecord.getStatusCode());
                }
            }
            else
            {
                log.info("No feedback records were received");
            }

            feedbackReceiver.close();
        }
        catch (IOException | InterruptedException e)
        {
            log.error("Failed to listen for feedback messages", e);
            System.exit(-1);
        }

        try
        {
            // FileUploadNotificationReceiver will use the same authentication mechanism that the ServiceClient itself uses,
            // so the below APIs are also RBAC authenticated.
            FileUploadNotificationReceiver fileUploadNotificationReceiver = serviceClient.getFileUploadNotificationReceiver();

            log.info("Opening file upload notification receiver and listening for file upload notifications");
            fileUploadNotificationReceiver.open();
            FileUploadNotification fileUploadNotification = fileUploadNotificationReceiver.receive(FILE_UPLOAD_NOTIFICATION_LISTEN_SECONDS);

            if (fileUploadNotification != null)
            {
                log.info("File upload notification received for device {}\n", fileUploadNotification.getDeviceId());
            }
            else
            {
                log.info("No feedback records were received");
            }

            fileUploadNotificationReceiver.close();
        }
        catch (IOException | InterruptedException e)
        {
            log.error("Failed to listen for file upload notification messages", e);
            System.exit(-1);
        }
    }

    private static void runJobClientSample(String iotHubHostName, TokenCredential credential)
    {
        // JobClient has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        JobClientOptions options = JobClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        JobClient jobClient = new JobClient(iotHubHostName, credential, options);

        try
        {
            log.info("Querying all active jobs for your IoT Hub");

            try
            {
                DigitalTwinClient client = new DigitalTwinClient(iotHubHostName, credential);
                client.getDigitalTwin("asdf", DigitalTwinClient.class);

            }
            catch (Exception e)
            {
                log.error("faile d to joobb", e);
                System.exit(-1);
            }

            Query deviceJobQuery = jobClient.queryDeviceJob(SqlQuery.createSqlQuery("*", SqlQuery.FromType.JOBS, null, null).getQuery());
            int queriedJobCount = 0;
            while (jobClient.hasNextJob(deviceJobQuery))
            {
                queriedJobCount++;
                JobResult job = jobClient.getNextJob(deviceJobQuery);
                log.info("Job {} of type {} has status {}", job.getJobId(), job.getJobType(), job.getJobStatus());
            }

            if (queriedJobCount == 0)
            {
                log.info("No active jobs found for your IoT Hub");
            }
        }
        catch (IotHubException | IOException e)
        {
            log.error("Failed to query the jobs for your IoT Hub", e);
            System.exit(-1);
        }
    }

    private static void runDeviceMethodClientSample(String iotHubHostName, TokenCredential credential, String deviceId)
    {
        // JobClient has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        DeviceMethodClientOptions options = DeviceMethodClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        DeviceMethod deviceMethod = new DeviceMethod(iotHubHostName, credential, options);

        try
        {
            log.info("Invoking method on device if it is online");
            deviceMethod.invoke(
                deviceId,
                "someMethodName",
                5L,
                2L,
                "Some method invocation payload");
        }
        catch (IotHubException e)
        {
            if (e.getErrorCodeDescription() == ErrorCodeDescription.DeviceNotOnline)
            {
                log.info("Device was not online, so the method invocation failed.");
            }
            else
            {
                log.error("Failed to invoke a method on your device", e);
                System.exit(-1);
            }
        }
        catch (IOException e)
        {
            log.error("Failed to invoke a method on your device", e);
            System.exit(-1);
        }
    }
}
