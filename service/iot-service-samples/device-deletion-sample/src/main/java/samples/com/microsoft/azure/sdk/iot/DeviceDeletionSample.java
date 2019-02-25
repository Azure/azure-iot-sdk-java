/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.EnrollmentGroup;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.IndividualEnrollment;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.QueryResult;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.QuerySpecification;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDeletionSample
{
    /**
     * A simple sample for deleting all devices from an iothub
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        if (args.length != 1 && args.length != 2)
        {
            System.out.format(
                    "Expected 1 or 2 arguments but received: %d.\n"
                            + "1.) Iot hub connection string to hub, this sample will delete all devices registered in this hub"
                            + "2.) (optional) DPS connection string, this sample will delete all enrollment groups and individual enrollments",
                    args.length);
            return;
        }

        String iotHubConnString = args[0];
        if (!(iotHubConnString == null || iotHubConnString.isEmpty() || iotHubConnString.equals(" ")))
        {
            cleanupIotHub(iotHubConnString);
        }

        if (args.length > 1 && args[1] != null && !args[1].isEmpty())
        {
            String dpsConnString = args[1];
            cleanupDPS(dpsConnString);
        }
    }

    private static void cleanupIotHub(String connString) throws IOException
    {
        RegistryManager registryManager = null;
        try
        {
            registryManager = RegistryManager.createFromConnectionString(connString);
        }
        catch (IOException e)
        {
            throw new IOException("Could not create registry manager from the provided connection string", e);
        }

        System.out.println("Querying ");
        DeviceTwin deviceTwin = null;
        try
        {
            deviceTwin = DeviceTwin.createFromConnectionString(connString);
        }
        catch (Exception e)
        {
            throw new IOException("Could not create device twin client from the provided connection string", e);
        }

        Query query = null;
        try
        {
            query = deviceTwin.queryTwin("SELECT * FROM Devices", 100);
        }
        catch (Exception e)
        {
            throw new IOException("Could not execute the query on your iot hub to retrieve the device list", e);
        }

        List<String> deviceIdsToRemove = new ArrayList<>();
        try
        {
            while (deviceTwin.hasNextDeviceTwin(query))
            {
                DeviceTwinDevice device = deviceTwin.getNextDeviceTwin(query);
                deviceIdsToRemove.add(device.getDeviceId());
            }
        }
        catch (Exception e)
        {
            throw new IOException("Could not collect the full list of device ids to delete", e);
        }

        int deletedDeviceCount = 0;
        for (String deviceIdToRemove : deviceIdsToRemove)
        {
            try
            {
                registryManager.removeDevice(deviceIdToRemove);
                deletedDeviceCount++;
            }
            catch (Exception e)
            {
                System.out.println("Could not remove device with id " +deviceIdToRemove);
                e.printStackTrace();

                System.out.println("Moving onto deleting the remaining devices anyways...");
            }
        }

        System.out.println("Deleted " + deletedDeviceCount + " out of the total " + deviceIdsToRemove.size() + " devices");

        registryManager.close();
    }

    private static void cleanupDPS(String connString) throws InterruptedException
    {
        ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(connString);
        QuerySpecification querySpecificationForAllEnrollments = new QuerySpecification("SELECT * FROM enrollments");

        deleteEnrollmentGroups(provisioningServiceClient, querySpecificationForAllEnrollments);
        deleteIndividualEnrollments(provisioningServiceClient, querySpecificationForAllEnrollments);
    }

    private static void deleteIndividualEnrollments(ProvisioningServiceClient provisioningServiceClient, QuerySpecification querySpecificationForAllEnrollments) throws InterruptedException
    {
        com.microsoft.azure.sdk.iot.provisioning.service.Query individualEnrollmentQuery = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecificationForAllEnrollments);
        while (individualEnrollmentQuery.hasNext())
        {
            try
            {
                QueryResult result = individualEnrollmentQuery.next();
                Object[] results = result.getItems();
                IndividualEnrollment[] individualEnrollments = (IndividualEnrollment[]) results;

                for (int i = 0; i < individualEnrollments.length; i++)
                {
                    try
                    {
                        System.out.println("Deleting individual enrollment with registration id: " + individualEnrollments[i].getRegistrationId());
                        provisioningServiceClient.deleteIndividualEnrollment(individualEnrollments[i]);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();

                        //likely a throttling exception, just wait a bit
                        Thread.sleep(2000);
                    }

                    Thread.sleep(100);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();

                //likely a throttling exception, just wait a bit
                Thread.sleep(4000);
            }
        }
    }

    private static void deleteEnrollmentGroups(ProvisioningServiceClient provisioningServiceClient, QuerySpecification querySpecificationForAllEnrollments) throws InterruptedException
    {
        com.microsoft.azure.sdk.iot.provisioning.service.Query enrollmentGroupQuery = provisioningServiceClient.createEnrollmentGroupQuery(querySpecificationForAllEnrollments, 1000);
        while (enrollmentGroupQuery.hasNext())
        {
            try
            {
                QueryResult result = enrollmentGroupQuery.next();
                Object[] results = result.getItems();
                EnrollmentGroup[] enrollmentGroups = (EnrollmentGroup[]) results;

                for (int i = 0; i < enrollmentGroups.length; i++)
                {
                    try
                    {
                        System.out.println("Deleting individual enrollment with group id: " + enrollmentGroups[i].getEnrollmentGroupId());
                        provisioningServiceClient.deleteEnrollmentGroup(enrollmentGroups[i]);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();

                        //likely a throttling exception, just wait a bit
                        Thread.sleep(400);
                    }
                }
            }
            catch (Exception e)
            {
                Thread.sleep(1000);

                //likely a throttling exception, just wait a bit
                e.printStackTrace();
            }
        }
    }
}
