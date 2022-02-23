/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.azure.sdk.iot.service.jobs.ScheduledJob;
import com.microsoft.azure.sdk.iot.service.messaging.AcknowledgementType;
import com.microsoft.azure.sdk.iot.service.messaging.ErrorContext;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackBatch;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotification;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.FileUploadNotificationProcessorClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClient;
import com.microsoft.azure.sdk.iot.service.messaging.MessageFeedbackProcessorClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClient;
import com.microsoft.azure.sdk.iot.service.query.JobQueryResponse;
import com.microsoft.azure.sdk.iot.service.query.QueryClient;
import com.microsoft.azure.sdk.iot.service.query.QueryClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.ErrorCodeDescription;
import com.microsoft.azure.sdk.iot.service.query.SqlQueryBuilder;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackRecord;
import com.microsoft.azure.sdk.iot.service.messaging.IotHubServiceClientProtocol;
import com.microsoft.azure.sdk.iot.service.messaging.Message;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.messaging.MessagingClientOptions;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationType;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodRequestOptions;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClient;
import com.microsoft.azure.sdk.iot.service.methods.DirectMethodsClientOptions;
import com.microsoft.azure.sdk.iot.service.twin.Twin;
import com.microsoft.azure.sdk.iot.service.twin.TwinClient;
import com.microsoft.azure.sdk.iot.service.twin.TwinClientOptions;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This sample demonstrates how to use the constructors in the various service clients that take an instance of
 * {@link TokenCredential} in order to authenticate with role based access credentials. For additional details
 * about the service side configurations required to use role based authentication on your IoT Hub, see
 * <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-dev-guide-azure-ad-rbac">this link</a>
 */
public class RoleBasedAuthenticationSample
{
    private static final int FILE_UPLOAD_NOTIFICATION_LISTEN_SECONDS = 5 * 1000; // 5 seconds
    private static final int FEEDBACK_MESSAGE_LISTEN_SECONDS = 5 * 1000; // 5 seconds

    public static void main(String[] args) throws InterruptedException
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
                .build();

        // "my-azure-iot-hub.azure-devices.net" for example
        String iotHubHostName = parsedArguments.getIotHubHostName();

        String newDeviceId = runRegistryManagerSample(iotHubHostName, credential);

        runTwinClientSample(iotHubHostName, credential, newDeviceId);

        runServiceClientSample(iotHubHostName, credential, newDeviceId);

        runJobClientSample(iotHubHostName, credential);

        runDirectMethodClientSample(iotHubHostName, credential, newDeviceId);
    }

    private static String runRegistryManagerSample(String iotHubHostName, TokenCredential credential)
    {
        // RegistryClient has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        RegistryClientOptions options = RegistryClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        RegistryClient registryClient = new RegistryClient(iotHubHostName, credential, options);

        String deviceId = "my-new-device-" + UUID.randomUUID().toString();
        Device newDevice = new Device(deviceId, AuthenticationType.SAS);

        try
        {
            System.out.println("Creating device " + deviceId);
            registryClient.addDevice(newDevice);
            System.out.println("Successfully created device " + deviceId);
        }
        catch (IOException | IotHubException e)
        {
            System.err.println("Failed to register new device");
            e.printStackTrace();
            System.exit(-1);
        }

        return deviceId;
    }

    private static void runTwinClientSample(String iotHubHostName, TokenCredential credential, String deviceId)
    {
        // TwinClient has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        TwinClientOptions options = TwinClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        TwinClient twinClient = new TwinClient(iotHubHostName, credential, options);

        Twin newDeviceTwin = null;
        try
        {
            System.out.println("Getting twin for device " + deviceId);
            newDeviceTwin = twinClient.get(deviceId);
        }
        catch (IotHubException | IOException e)
        {
            System.err.println("Failed to get twin for device " + deviceId);
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Successfully got the twin for the new device");
        System.out.println("Device Id: " + newDeviceTwin.getDeviceId());
        System.out.println("ETag: " + newDeviceTwin.getETag());
    }

    private static void runServiceClientSample(String iotHubHostName, TokenCredential credential, String deviceId) throws InterruptedException
    {
        // MessagingClient has some configurable options for setting a custom SSLContext, as well as for setting proxies.
        // For this sample, the default options will be used though.
        MessagingClientOptions options = MessagingClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        MessagingClient messagingClient =
            new MessagingClient(
                iotHubHostName,
                credential,
                IotHubServiceClientProtocol.AMQPS,
                options);

        String cloudToDeviceMessagePayload = "This is a message sent by an RBAC authenticated service client!";
        Message cloudToDeviceMessage = new Message(cloudToDeviceMessagePayload.getBytes(StandardCharsets.UTF_8));
        try
        {
            messagingClient.open();
            System.out.println("Sending cloud to device message to the new device");
            messagingClient.send(deviceId, cloudToDeviceMessage);
            System.out.println("Successfully sent cloud to device message to the new device");
            messagingClient.close();
        }
        catch (IOException | IotHubException e)
        {
            System.err.println("Failed to send a cloud to device message to the new device");
            e.printStackTrace();
            System.exit(-1);
        }

        try
        {
            Function<FeedbackBatch, AcknowledgementType> feedbackMessageProcessor = feedbackBatch ->
            {
                for (FeedbackRecord feedbackRecord : feedbackBatch.getRecords())
                {
                    System.out.println(String.format("Feedback record received for device %s with status %s", feedbackRecord.getDeviceId(), feedbackRecord.getStatusCode()));
                }

                return AcknowledgementType.COMPLETE;
            };

            Function<FileUploadNotification, AcknowledgementType> fileUploadNotificationProcessor = notification ->
            {
                System.out.println("File upload notification received for device " + notification.getDeviceId());
                return AcknowledgementType.COMPLETE;
            };

            Consumer<ErrorContext> errorProcessor = errorContext ->
            {
                if (errorContext.getIotHubException() != null)
                {
                    System.out.println("Encountered an IoT hub level error while receiving events " + errorContext.getIotHubException().getMessage());
                }
                else
                {
                    System.out.println("Encountered a network error while receiving events " + errorContext.getNetworkException().getMessage());
                }
            };

            FileUploadNotificationProcessorClientOptions fileUploadNotificationProcessorClientOptions =
                FileUploadNotificationProcessorClientOptions.builder()
                    .errorProcessor(errorProcessor)
                    .build();

            FileUploadNotificationProcessorClient fileUploadNotificationProcessorClient =
                new FileUploadNotificationProcessorClient(iotHubHostName, credential, IotHubServiceClientProtocol.AMQPS, fileUploadNotificationProcessor, fileUploadNotificationProcessorClientOptions);

            MessageFeedbackProcessorClientOptions messageFeedbackProcessorClientOptions =
                MessageFeedbackProcessorClientOptions.builder()
                    .errorProcessor(errorProcessor)
                    .build();

            MessageFeedbackProcessorClient messageFeedbackProcessorClient =
                new MessageFeedbackProcessorClient(iotHubHostName, credential, IotHubServiceClientProtocol.AMQPS, feedbackMessageProcessor, messageFeedbackProcessorClientOptions);

            // FeedbackReceiver will use the same authentication mechanism that the MessagingClient itself uses,
            // so the below APIs are also RBAC authenticated.
            System.out.println("Starting event processor to listen for feedback messages and file upload notifications");
            fileUploadNotificationProcessorClient.start();
            messageFeedbackProcessorClient.start();

            System.out.println("Sleeping 5 seconds while waiting for feedback records to be received");
            Thread.sleep(5000);

            fileUploadNotificationProcessorClient.stop();
            messageFeedbackProcessorClient.stop();
        }
        catch (IOException | IotHubException e)
        {
            System.err.println("Failed to listen for feedback messages");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void runJobClientSample(String iotHubHostName, TokenCredential credential)
    {
        // QueryClientOptions has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        QueryClientOptions queryClientOptions = QueryClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        QueryClient queryClient = new QueryClient(iotHubHostName, credential, queryClientOptions);

        try
        {
            System.out.println("Querying all active jobs for your IoT Hub");

            String jobsQueryString = SqlQueryBuilder.createSqlQuery("*", SqlQueryBuilder.FromType.JOBS, null, null);
            JobQueryResponse deviceJobQueryResponse = queryClient.queryJobs(jobsQueryString);
            int queriedJobCount = 0;
            while (deviceJobQueryResponse.hasNext())
            {
                queriedJobCount++;
                ScheduledJob job = deviceJobQueryResponse.next();
                System.out.println(String.format("ScheduledJob %s of type %s has status %s", job.getJobId(), job.getJobType(), job.getJobStatus()));
            }

            if (queriedJobCount == 0)
            {
                System.out.println("No active jobs found for your IoT Hub");
            }
        }
        catch (IotHubException | IOException e)
        {
            System.err.println("Failed to query the jobs for your IoT Hub");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    private static void runDirectMethodClientSample(String iotHubHostName, TokenCredential credential, String deviceId)
    {
        // ScheduledJobsClient has some configurable options for HTTP read and connect timeouts, as well as for setting proxies.
        // For this sample, the default options will be used though.
        DirectMethodsClientOptions options = DirectMethodsClientOptions.builder().build();

        // This constructor takes in your implementation of TokenCredential which allows you to use RBAC authentication
        // rather than symmetric key based authentication that comes with constructors that take connection strings.
        DirectMethodsClient directMethodsClient = new DirectMethodsClient(iotHubHostName, credential, options);

        try
        {
            System.out.println("Invoking method on device if it is online");
            DirectMethodRequestOptions directMethodRequestOptions =
                DirectMethodRequestOptions.builder()
                    .payload("Some method invocation payload")
                    .build();

            directMethodsClient.invoke(deviceId, "someMethodName", directMethodRequestOptions);
        }
        catch (IotHubException e)
        {
            if (e.getErrorCodeDescription() == ErrorCodeDescription.DeviceNotOnline)
            {
                System.out.println("Device was not online, so the method invocation failed.");
            }
            else
            {
                System.err.println("Failed to invoke a method on your device");
                e.printStackTrace();
                System.exit(-1);
            }
        }
        catch (IOException e)
        {
            System.err.println("Failed to invoke a method on your device");
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
