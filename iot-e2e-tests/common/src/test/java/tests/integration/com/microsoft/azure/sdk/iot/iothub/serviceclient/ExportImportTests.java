/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlobInputStream;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryJob;
import com.microsoft.azure.sdk.iot.service.registry.serializers.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.service.registry.StorageAuthenticationType;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClient;
import com.microsoft.azure.sdk.iot.service.registry.RegistryClientOptions;
import com.microsoft.azure.sdk.iot.service.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.registry.Device;
import com.microsoft.azure.sdk.iot.service.registry.DeviceStatus;
import com.microsoft.azure.sdk.iot.service.registry.ExportImportDevice;
import com.microsoft.azure.sdk.iot.service.registry.ImportMode;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubTooManyDevicesException;
import mockit.Deencapsulation;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

import static junit.framework.TestCase.fail;

/**
 * Test class containing all tests to be run on JVM and android pertaining to Export and Import jobs.
 */
@IotHubTest
public class ExportImportTests extends IntegrationTest
{
    private static final long IMPORT_EXPORT_TEST_TIMEOUT_MILLISECONDS = 8 * 60 * 1000;
    private static final long IMPORT_JOB_TIMEOUT_MILLISECONDS = 6 * 60 * 1000;
    private static final long EXPORT_JOB_TIMEOUT_MILLISECONDS = 6 * 60 * 1000;

    protected static String iotHubConnectionString = "";
    public static boolean isBasicTierHub;
    protected static String storageAccountConnectionString = "";
    private static String deviceId = "java-crud-e2e-test";

    private static BlobContainerClient importContainer;
    private static BlobContainerClient exportContainer;

    private static RegistryClient registryClient;

    @BeforeClass
    public static void setUp() throws IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        storageAccountConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.STORAGE_ACCOUNT_CONNECTION_STRING_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryClient = new RegistryClient(iotHubConnectionString, RegistryClientOptions.builder().httpReadTimeoutSeconds(HTTP_READ_TIMEOUT).build());
        String uuid = UUID.randomUUID().toString();
        deviceId = deviceId.concat("-" + uuid);

        BlobServiceClient blobClient = new BlobServiceClientBuilder().connectionString(storageAccountConnectionString).buildClient();

        // Creating the export storage container and getting its URI
        String exportContainerName = "exportcontainersample-" + uuid;
        exportContainer = blobClient.getBlobContainerClient(exportContainerName);
        exportContainer.create();

        // Creating the import storage container and getting its URI
        String importContainerName = "importcontainersample-" + uuid;
        importContainer = blobClient.getBlobContainerClient(importContainerName);
        importContainer.create();
    }

    @AfterClass
    public static void tearDown() throws Exception
    {
        //Deleting all devices created as a part of the bulk import-export test
        List<ExportImportDevice> exportedDevices = runExportJob(Optional.empty());
        List<ExportImportDevice> devicesToBeDeleted = new ArrayList<>();

        for(ExportImportDevice device : exportedDevices)
        {
            if (device.getId().startsWith("java-bulk-test-"))
            {
                devicesToBeDeleted.add(device);
            }
        }

        runImportJob(devicesToBeDeleted, ImportMode.Delete, Optional.empty());

        //Cleaning up the containers
        importContainer.delete();
        exportContainer.delete();
    }

    @Test (timeout = IMPORT_EXPORT_TEST_TIMEOUT_MILLISECONDS)
    @ContinuousIntegrationTest
    @Ignore
    public void export_import_e2e() throws Exception
    {

        List<ExportImportDevice> devicesForImport = createListofDevices();

        //importing devices - create mode
        runImportJob(devicesForImport, ImportMode.CreateOrUpdate, Optional.empty());

        List<ExportImportDevice> exportedDevices = runExportJob(Optional.empty());

        for (ExportImportDevice importedDevice : devicesForImport)
        {
            if (!exportedDevices.contains(importedDevice))
            {
                Assert.fail("Exported devices list does not contain device with id: " + importedDevice.getId());
            }
        }

        //importing devices - delete mode
        runImportJob(devicesForImport, ImportMode.Delete, Optional.empty());

        exportedDevices = runExportJob(Optional.empty());

        for (ExportImportDevice importedDevice : devicesForImport)
        {
            if (exportedDevices.contains(importedDevice))
            {
                Assert.fail("Device with id: " + importedDevice.getId() + " was not deleted by the import job");
            }
        }
    }

    @Test (timeout = IMPORT_EXPORT_TEST_TIMEOUT_MILLISECONDS)
    @ContinuousIntegrationTest
    @Ignore
    public void export_import_key_based_storage_auth_e2e() throws Exception
    {
        export_import_storage_auth_e2e(StorageAuthenticationType.KEY);
    }

    @Test (timeout = IMPORT_EXPORT_TEST_TIMEOUT_MILLISECONDS)
    @ContinuousIntegrationTest
    @Ignore
    public void export_import_identity_based_storage_auth_e2e() throws Exception
    {
        export_import_storage_auth_e2e(StorageAuthenticationType.IDENTITY);
    }

    private static void export_import_storage_auth_e2e(StorageAuthenticationType storageAuthenticationType) throws Exception
    {

        List<ExportImportDevice> devicesForImport = createListofDevices();

        //importing devices - create mode
        runImportJob(devicesForImport, ImportMode.CreateOrUpdate, Optional.of(storageAuthenticationType));

        List<ExportImportDevice> exportedDevices = runExportJob(Optional.of(storageAuthenticationType));

        for (ExportImportDevice importedDevice : devicesForImport)
        {
            if (!exportedDevices.contains(importedDevice))
            {
                Assert.fail("Exported devices list does not contain device with id: " + importedDevice.getId());
            }
        }

        //importing devices - delete mode
        runImportJob(devicesForImport, ImportMode.Delete, Optional.of(storageAuthenticationType));

        exportedDevices = runExportJob(Optional.of(storageAuthenticationType));

        for (ExportImportDevice importedDevice : devicesForImport)
        {
            if (exportedDevices.contains(importedDevice))
            {
                Assert.fail("Device with id: " + importedDevice.getId() + " was not deleted by the import job");
            }
        }
    }

    private static List<ExportImportDevice> createListofDevices()
    {
        //Creating the list of devices to be created, then deleted
        Integer numberOfDevices = 10;
        List<ExportImportDevice> devicesForImport = new ArrayList<>(numberOfDevices);
        for (int i = 0; i < numberOfDevices; i++)
        {
            String deviceId = "java-bulk-test-" + UUID.randomUUID().toString();
            Device device = new Device(deviceId);
            AuthenticationMechanism authentication = new AuthenticationMechanism(device.getSymmetricKey());

            ExportImportDevice deviceToAdd = new ExportImportDevice();
            deviceToAdd.setId(deviceId);
            deviceToAdd.setAuthentication(authentication);
            deviceToAdd.setStatus(DeviceStatus.Enabled);
            TwinCollection tags = new TwinCollection(); tags.put("test01", "firstvalue");
            deviceToAdd.setTags(tags);

            devicesForImport.add(deviceToAdd);
        }

        return devicesForImport;
    }

    private static List<ExportImportDevice> runExportJob(Optional<StorageAuthenticationType> storageAuthenticationType) throws Exception
    {
        Boolean excludeKeys = false;
        String containerSasUri = getContainerSasUri(exportContainer);

        boolean exportJobScheduled = false;
        RegistryJob exportJob = null;
        while (!exportJobScheduled)
        {
            try
            {
                if (storageAuthenticationType.isPresent())
                {
                    RegistryJob exportJobProperties =
                        RegistryJob.createForExportJob(
                            containerSasUri,
                            excludeKeys,
                            storageAuthenticationType.get());

                    exportJob = registryClient.exportDevices(exportJobProperties);
                }
                else
                {
                    exportJob = registryClient.exportDevices(containerSasUri, excludeKeys);
                }
                exportJobScheduled = true;

            }
            catch (IotHubTooManyDevicesException e)
            {
                //test is being throttled, wait a while and try again
                Thread.sleep(10 * 1000);
            }
        }

        RegistryJob.JobStatus jobStatus;

        long startTime = System.currentTimeMillis();
        while (true)
        {
            exportJob = registryClient.getJob(exportJob.getJobId());
            jobStatus = exportJob.getStatus();
            if (jobStatus == RegistryJob.JobStatus.COMPLETED || jobStatus == RegistryJob.JobStatus.FAILED)
            {
                break;
            }

            if (System.currentTimeMillis() - startTime > EXPORT_JOB_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for the export job to complete");
            }

            Thread.sleep(100);
        }

        String exportedDevicesJson = "";
        for (BlobItem blobItem : exportContainer.listBlobs())
        {
            BlobInputStream stream = exportContainer.getBlobClient(blobItem.getName()).openInputStream();

            try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8.name())) {
                exportedDevicesJson = scanner.next();
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
        scanner.close();

        if (jobStatus != RegistryJob.JobStatus.COMPLETED)
        {
            Assert.fail("The export job was not completed successfully");
        }

        return result;
    }

    private static void runImportJob(List<ExportImportDevice> devices, ImportMode importMode, Optional<StorageAuthenticationType> storageAuthenticationType) throws Exception
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
        BlockBlobClient importBlob = importContainer.getBlobClient(importBlobName).getBlockBlobClient();
        importBlob.delete();
        importBlob.upload(stream, blobToImport.length);

        // Starting the import job
        boolean importJobScheduled = false;
        RegistryJob importJob = null;
        while (!importJobScheduled)
        {
            try
            {
                if (storageAuthenticationType.isPresent())
                {
                    // For a given StorageAuthenticationType, create RegistryJob and pass it
                    RegistryJob importJobProperties = RegistryJob.createForImportJob(getContainerSasUri(importContainer), getContainerSasUri(importContainer), storageAuthenticationType.get());
                    importJob = registryClient.importDevices(importJobProperties);
                }
                else
                {
                    importJob = registryClient.importDevices(getContainerSasUri(importContainer), getContainerSasUri(importContainer));
                }
                importJobScheduled = true;
            }
            catch (IotHubTooManyDevicesException e)
            {
                //test is being throttled, wait a while and try again
                Thread.sleep(10 * 1000);
            }
        }

        // Waiting for the import job to complete
        long startTime = System.currentTimeMillis();
        while (true)
        {
            importJob = registryClient.getJob(importJob.getJobId());
            if (importJob.getStatus() == RegistryJob.JobStatus.COMPLETED
                    || importJob.getStatus() == RegistryJob.JobStatus.FAILED)
            {
                break;
            }

            if (System.currentTimeMillis() - startTime > IMPORT_JOB_TIMEOUT_MILLISECONDS)
            {
                fail("Timed out waiting for the import job to complete");
            }

            Thread.sleep(100);
        }

        // Checking the result of the import job
        if (importJob.getStatus() != RegistryJob.JobStatus.COMPLETED)
        {
            Assert.fail("The import job was not completed successfully for " + importMode + " operation.");
        }
    }

    private static String getContainerSasUri(BlobContainerClient blobContainerClient)
    {
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        BlobContainerSasPermission permission =
            new BlobContainerSasPermission()
                .setReadPermission(true)
                .setAddPermission(true)
                .setCreatePermission(true)
                .setDeletePermission(true)
                .setListPermission(true);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission)
            .setStartTime(OffsetDateTime.now());

        return blobContainerClient.generateSas(values);
    }
}
