/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.service.jobs.registry.RegistryJobsClient;
import com.microsoft.azure.sdk.iot.service.jobs.scheduled.ScheduledJobsClient;
import com.microsoft.azure.sdk.iot.service.jobs.registry.ManagedIdentity;
import com.microsoft.azure.sdk.iot.service.jobs.registry.StorageAuthenticationType;
import com.microsoft.azure.sdk.iot.service.jobs.registry.JobProperties;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubException;
import java.io.IOException;

/* A sample to illustrate how to perform import and export jobs using managed identity to access the storage account.
    This sample will copy all the devices in the source hub to the destination hub.
    For this sample to succeed, the managed identity should be configured to access the storage account used for import and export.
    For more information on managed identities, see <see href="https://docs.microsoft.com/en-us/azure/active-directory/managed-identities-azure-resources/overview"/>
 */
public class DeviceManagerImportExportWithIdentitySample {
    private static final String sourceHubConnectionString  = System.getenv("SOURCE_IOTHUB_CONNECTION_STRING");
    private static final String destinationHubConnectionString  = System.getenv("DESTINATION_IOTHUB_CONNECTION_STRING");
    private static final String blobContainerUri  = System.getenv("BLOB_CONTAINER_URI");
    private static final String userDefinedManagedIdentityResourceId  = System.getenv("USER_DEFINED_MSI_RESOURCE_ID");

    public static void main(String[] args) throws IOException, IotHubException, InterruptedException
    {
        System.out.println("Exporting devices from source hub to " + blobContainerUri + "/devices.txt.");
        ExportDevices();
        System.out.println("Exporting devices completed.");

        System.out.println("Importing devices from " + blobContainerUri + "/devices.txt to destination hub.");
        ImportDevices();
        System.out.println("Importing devices completed.");
    }

    public static void ExportDevices() throws IOException, IotHubException, InterruptedException {
        RegistryJobsClient jobClient = new RegistryJobsClient(sourceHubConnectionString);

        // If StorageAuthenticationType is set to IdentityBased and userAssignedIdentity property is
        // not null, the jobs will use user defined managed identity. If the IoT hub is not
        // configured with the user defined managed identity specified in userAssignedIdentity,
        // the job will fail.
        // If StorageAuthenticationType is set to IdentityBased the userAssignedIdentity property is
        // null, the jobs will use system defined identity. If the IoT hub is not configured with the
        // user defined managed identity, the job will fail.
        // If StorageAuthenticationType is set to IdentityBased and neither user defined nor system defined
        // managed identities are configured on the hub, the job will fail.
        ManagedIdentity identity = new ManagedIdentity();
        identity.setUserAssignedIdentity(userDefinedManagedIdentityResourceId);

        JobProperties jobProperties = JobProperties.createForExportJob(
                blobContainerUri,
                false,
                StorageAuthenticationType.IDENTITY,
                identity);

        JobProperties exportJob = jobClient.exportDevices(jobProperties);

        while(true)
        {
            exportJob = jobClient.getJob(exportJob.getJobId());
            if (exportJob.getStatus() == JobProperties.JobStatus.COMPLETED)
            {
                break;
            }
            Thread.sleep(500);
        }
    }

    public static void ImportDevices() throws IOException, IotHubException, InterruptedException {
        RegistryJobsClient jobClient = new RegistryJobsClient(sourceHubConnectionString);

        // If StorageAuthenticationType is set to IdentityBased and userAssignedIdentity property is
        // not null, the jobs will use user defined managed identity. If the IoT hub is not
        // configured with the user defined managed identity specified in userAssignedIdentity,
        // the job will fail.
        // If StorageAuthenticationType is set to IdentityBased the userAssignedIdentity property is
        // null, the jobs will use system defined identity. If the IoT hub is not configured with the
        // user defined managed identity, the job will fail.
        // If StorageAuthenticationType is set to IdentityBased and neither user defined nor system defined
        // managed identities are configured on the hub, the job will fail.
        ManagedIdentity identity = new ManagedIdentity();
        identity.setUserAssignedIdentity(userDefinedManagedIdentityResourceId);

        JobProperties jobProperties = JobProperties.createForImportJob(
                blobContainerUri,
                blobContainerUri,
                StorageAuthenticationType.IDENTITY,
                identity);

        JobProperties exportJob = jobClient.importDevices(jobProperties);

        while(true)
        {
            exportJob = jobClient.getJob(exportJob.getJobId());
            if (exportJob.getStatus() == JobProperties.JobStatus.COMPLETED)
            {
                break;
            }
            Thread.sleep(500);
        }
    }
}
