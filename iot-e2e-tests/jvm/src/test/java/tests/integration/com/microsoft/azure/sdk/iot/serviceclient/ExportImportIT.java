/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.serviceclient;

import com.microsoft.azure.sdk.iot.deps.serializer.AuthenticationTypeParser;
import com.microsoft.azure.sdk.iot.deps.serializer.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import mockit.Deencapsulation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ExportImportIT
{
    private static String iotHubonnectionStringEnvVarName = "IOTHUB_CONNECTION_STRING";
    private static String storageAccountConnectionStringEnvVarName = "STORAGE_ACCOUNT_CONNECTION_STRING";
    private static String iotHubConnectionString = "";
    private static String storageAccountConnectionString = "";
    private static String deviceId = "java-crud-e2e-test";

    private static CloudBlobClient blobClient;
    private static CloudBlobContainer importContainer;
    private static CloudBlobContainer exportContainer;

    private static RegistryManager registryManager;

    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        Map<String, String> env = System.getenv();

        if (env.containsKey(iotHubonnectionStringEnvVarName))
        {
            iotHubConnectionString = env.get(iotHubonnectionStringEnvVarName);
        }
        else
        {
            throw new IllegalArgumentException("Environment variable is not set: " + iotHubonnectionStringEnvVarName);
        }

        if (env.containsKey(storageAccountConnectionStringEnvVarName))
        {
            storageAccountConnectionString = env.get(storageAccountConnectionStringEnvVarName);
        }
        else
        {
            throw new IllegalArgumentException("Environment variable is not set: " + storageAccountConnectionStringEnvVarName);
        }

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString);
        String uuid = UUID.randomUUID().toString();
        deviceId = deviceId.concat("-" + uuid);

        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageAccountConnectionString);
        blobClient = storageAccount.createCloudBlobClient();

        // Creating the export storage container and getting its URI
        String exportContainerName = "exportcontainersample-" + uuid;
        exportContainer = blobClient.getContainerReference(exportContainerName);
        exportContainer.createIfNotExists();

        // Creating the import storage container and getting its URI
        String importContainerName = "importcontainersample-" + uuid;
        importContainer = blobClient.getContainerReference(importContainerName);
        importContainer.createIfNotExists();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        //Deleting all devices created as a part of the bulk import-export test
        List<ExportImportDevice> exportedDevices = runExportJob();
        List<ExportImportDevice> devicesToBeDeleted = new ArrayList<>();

        for(ExportImportDevice device : exportedDevices)
        {
            if (device.getId().startsWith("java-bulk-test-"))
            {
                devicesToBeDeleted.add(device);
            }
        }

        runImportJob(devicesToBeDeleted, ImportMode.Delete);

        //Cleaning up the containers
        importContainer.deleteIfExists();
        exportContainer.deleteIfExists();
    }

    @Test
    public void export_import_e2e() throws Exception
    {
        //Creating the list of devices to be created, then deleted
        Integer numberOfDevices = 10;
        List<ExportImportDevice> devicesForImport = new ArrayList<>(numberOfDevices);
        for (int i = 0; i < numberOfDevices; i++)
        {
            String deviceId = "java-bulk-test-" + UUID.randomUUID().toString();
            Device device = Device.createFromId(deviceId, null, null);
            AuthenticationMechanism authentication = new AuthenticationMechanism(device.getSymmetricKey());

            ExportImportDevice deviceToAdd = new ExportImportDevice();
            deviceToAdd.setId(deviceId);
            deviceToAdd.setAuthentication(authentication);
            deviceToAdd.setStatus(DeviceStatus.Enabled);

            devicesForImport.add(deviceToAdd);
        }

        //importing devices - create mode
        runImportJob(devicesForImport, ImportMode.CreateOrUpdate);

        List<ExportImportDevice> exportedDevices = runExportJob();

        for (ExportImportDevice importedDevice : devicesForImport)
        {
            if (!exportedDevices.contains(importedDevice))
            {
                Assert.fail("Exported devices list does not contain device with id: " + importedDevice.getId());
            }
        }

        //importing devices - delete mode
        runImportJob(devicesForImport, ImportMode.Delete);

        exportedDevices = runExportJob();

        for (ExportImportDevice importedDevice : devicesForImport)
        {
            if (exportedDevices.contains(importedDevice))
            {
                Assert.fail("Device with id: " + importedDevice.getId() + " was not deleted by the import job");
            }
        }
    }

    private static List<ExportImportDevice> runExportJob() throws Exception
    {
        Boolean excludeKeys = false;
        String containerSasUri = getContainerSasUri(exportContainer);

        JobProperties exportJob = registryManager.exportDevices(containerSasUri, excludeKeys);

        JobProperties.JobStatus jobStatus;

        while (true)
        {
            exportJob = registryManager.getJob(exportJob.getJobId());
            jobStatus = exportJob.getStatus();
            if (jobStatus == JobProperties.JobStatus.COMPLETED || jobStatus == JobProperties.JobStatus.FAILED)
            {
                break;
            }
            Thread.sleep(100);
        }

        String exportedDevicesJson = "";
        for(ListBlobItem blobItem : exportContainer.listBlobs())
        {
            if (blobItem instanceof CloudBlockBlob)
            {
                CloudBlockBlob retrievedBlob = (CloudBlockBlob) blobItem;
                exportedDevicesJson = retrievedBlob.downloadText();
            }
        }

        List<ExportImportDevice> result = new ArrayList<>();

        Scanner scanner = new Scanner(exportedDevicesJson);
        while (scanner.hasNextLine())
        {
            String exportImportDeviceJson = scanner.nextLine();

            ExportImportDeviceParser parser = new ExportImportDeviceParser(exportImportDeviceJson);

            ExportImportDevice device = Deencapsulation.newInstance(ExportImportDevice.class, new Class[]{ExportImportDeviceParser.class}, parser);
            device.setImportMode(ImportMode.CreateOrUpdate);
            result.add(device);
        }

        if (jobStatus != JobProperties.JobStatus.COMPLETED)
        {
            Assert.fail("The export job was not completed successfully");
        }

        return result;
    }

    private static void runImportJob(List<ExportImportDevice> devices, ImportMode importMode) throws Exception
    {
        // Creating the json string to be submitted for import using the specified importMode
        StringBuilder devicesToAdd = new StringBuilder();
        for (int i = 0; i < devices.size(); i++)
        {
            devices.get(i).setImportMode(importMode);
            ExportImportDeviceParser parser = Deencapsulation.invoke(devices.get(i), "toExportImportDeviceParser");

            devicesToAdd.append(parser.toJson());

            if (i < devices.size() - 1)
            {
                devicesToAdd.append("\r\n");
            }
        }

        byte[] blobToImport = devicesToAdd.toString().getBytes(StandardCharsets.UTF_8);

        // Creating the Azure storage blob and uploading the serialized string of devices
        InputStream stream = new ByteArrayInputStream(blobToImport);
        String importBlobName = "devices.txt";
        CloudBlockBlob importBlob = importContainer.getBlockBlobReference(importBlobName);
        importBlob.deleteIfExists();
        importBlob.upload(stream, blobToImport.length);

        // Starting the import job
        JobProperties importJob = registryManager.importDevices(getContainerSasUri(importContainer), getContainerSasUri(importContainer));

        // Waiting for the import job to complete
        while (true)
        {
            importJob = registryManager.getJob(importJob.getJobId());
            if (importJob.getStatus() == JobProperties.JobStatus.COMPLETED
                    || importJob.getStatus() == JobProperties.JobStatus.FAILED)
            {
                break;
            }
            Thread.sleep(100);
        }

        // Checking the result of the import job
        if (importJob.getStatus() != JobProperties.JobStatus.COMPLETED)
        {
            Assert.fail("The import job was not completed successfully for " + importMode + " operation.");
        }
    }

    private static String getContainerSasUri(CloudBlobContainer container) throws InvalidKeyException, StorageException
    {
        //Set the expiry time and permissions for the container.
        //In this case no start time is specified, so the shared access signature becomes valid immediately.
        SharedAccessBlobPolicy sasConstraints = new SharedAccessBlobPolicy();
        Date expirationDate = Date.from(Instant.now().plus(Duration.ofDays(1)));
        sasConstraints.setSharedAccessExpiryTime(expirationDate);
        EnumSet<SharedAccessBlobPermissions> permissions = EnumSet.of(
                SharedAccessBlobPermissions.WRITE,
                SharedAccessBlobPermissions.LIST,
                SharedAccessBlobPermissions.READ,
                SharedAccessBlobPermissions.DELETE);
        sasConstraints.setPermissions(permissions);

        //Generate the shared access signature on the container, setting the constraints directly on the signature.
        String sasContainerToken = container.generateSharedAccessSignature(sasConstraints, null);

        //Return the URI string for the container, including the SAS token.
        return container.getUri() + "?" + sasContainerToken;
    }
}
