/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package tests.integration.com.microsoft.azure.sdk.iot.iothub.serviceclient;

import com.microsoft.azure.sdk.iot.deps.serializer.ExportImportDeviceParser;
import com.microsoft.azure.sdk.iot.deps.serializer.StorageAuthenticationType;
import com.microsoft.azure.sdk.iot.deps.twin.TwinCollection;
import com.microsoft.azure.sdk.iot.service.*;
import com.microsoft.azure.sdk.iot.service.auth.AuthenticationMechanism;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubTooManyDevicesException;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import mockit.Deencapsulation;
import org.junit.*;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.IntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.TestConstants;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.Tools;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.ContinuousIntegrationTest;
import tests.integration.com.microsoft.azure.sdk.iot.helpers.annotations.IotHubTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

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

    private static CloudBlobContainer importContainer;
    private static CloudBlobContainer exportContainer;

    private static RegistryManager registryManager;

    @BeforeClass
    public static void setUp() throws URISyntaxException, InvalidKeyException, StorageException, IOException
    {
        iotHubConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.IOT_HUB_CONNECTION_STRING_ENV_VAR_NAME);
        isBasicTierHub = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_BASIC_TIER_HUB_ENV_VAR_NAME));
        storageAccountConnectionString = Tools.retrieveEnvironmentVariableValue(TestConstants.STORAGE_ACCOUNT_CONNECTION_STRING_ENV_VAR_NAME);
        isPullRequest = Boolean.parseBoolean(Tools.retrieveEnvironmentVariableValue(TestConstants.IS_PULL_REQUEST));

        registryManager = RegistryManager.createFromConnectionString(iotHubConnectionString, RegistryManagerOptions.builder().httpReadTimeout(HTTP_READ_TIMEOUT).build());
        String uuid = UUID.randomUUID().toString();
        deviceId = deviceId.concat("-" + uuid);

        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageAccountConnectionString);
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

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
        importContainer.deleteIfExists();
        exportContainer.deleteIfExists();

        if (registryManager != null)
        {
            registryManager.close();
            registryManager = null;
        }
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
            Device device = Device.createFromId(deviceId, null, null);
            AuthenticationMechanism authentication = new AuthenticationMechanism(device.getSymmetricKey());

            ExportImportDevice deviceToAdd = new ExportImportDevice();
            deviceToAdd.setId(deviceId);
            deviceToAdd.setAuthentication(authentication);
            deviceToAdd.setStatus(DeviceStatus.Enabled);
            TwinCollection tags = new TwinCollection(); tags.putFinal("test01", "firstvalue");
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
        JobProperties exportJob = null;
        while (!exportJobScheduled)
        {
            try
            {
                if (storageAuthenticationType.isPresent())
                {
                    JobProperties exportJobProperties = JobProperties.createForExportJob(containerSasUri, excludeKeys, storageAuthenticationType.get());
                    exportJob = registryManager.exportDevices(exportJobProperties);
                }
                else
                {
                    exportJob = registryManager.exportDevices(containerSasUri, excludeKeys);
                }
                exportJobScheduled = true;

            }
            catch (IotHubTooManyDevicesException e)
            {
                //test is being throttled, wait a while and try again
                Thread.sleep(10 * 1000);
            }
        }

        JobProperties.JobStatus jobStatus;

        long startTime = System.currentTimeMillis();
        while (true)
        {
            exportJob = registryManager.getJob(exportJob.getJobId());
            jobStatus = exportJob.getStatus();
            if (jobStatus == JobProperties.JobStatus.COMPLETED || jobStatus == JobProperties.JobStatus.FAILED)
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
        scanner.close();

        if (jobStatus != JobProperties.JobStatus.COMPLETED)
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
        CloudBlockBlob importBlob = importContainer.getBlockBlobReference(importBlobName);
        importBlob.deleteIfExists();
        importBlob.upload(stream, blobToImport.length);

        // Starting the import job
        boolean importJobScheduled = false;
        JobProperties importJob = null;
        while (!importJobScheduled)
        {
            try
            {
                if (storageAuthenticationType.isPresent())
                {
                    // For a given StorageAuthenticationType, create JobProperties and pass it
                    JobProperties importJobProperties = JobProperties.createForImportJob(getContainerSasUri(importContainer), getContainerSasUri(importContainer), storageAuthenticationType.get());
                    importJob = registryManager.importDevices(importJobProperties);
                }
                else
                {
                    importJob = registryManager.importDevices(getContainerSasUri(importContainer), getContainerSasUri(importContainer));
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
            importJob = registryManager.getJob(importJob.getJobId());
            if (importJob.getStatus() == JobProperties.JobStatus.COMPLETED
                    || importJob.getStatus() == JobProperties.JobStatus.FAILED)
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
