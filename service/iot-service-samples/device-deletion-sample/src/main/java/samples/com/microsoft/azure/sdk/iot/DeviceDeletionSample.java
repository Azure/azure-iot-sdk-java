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
import com.microsoft.azure.sdk.iot.service.Device;
import com.microsoft.azure.sdk.iot.service.RegistryManager;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwin;
import com.microsoft.azure.sdk.iot.service.devicetwin.DeviceTwinDevice;
import com.microsoft.azure.sdk.iot.service.devicetwin.Pair;
import com.microsoft.azure.sdk.iot.service.devicetwin.Query;
import com.microsoft.azure.sdk.iot.service.devicetwin.QueryType;
import com.microsoft.azure.sdk.iot.service.devicetwin.SqlQuery;
import com.microsoft.azure.sdk.iot.service.exceptions.IotHubNotFoundException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDeletionSample
{
    /**
     * A simple sample for deleting all devices from an iothub
     */
    public static void main(String[] args) throws InterruptedException
    {
        if (args.length != 1 && args.length != 2)
        {
            System.out.format(
                    "Expected 1 or 2 arguments but received: %d.\n"
                            + "1.) Iot hub connection string to hub, this sample will delete all devices registered in this hub\n"
                            + "2.) (optional) DPS connection string, this sample will delete all enrollment groups and individual enrollments",
                    args.length);
            return;
        }


        String iotHubConnString = args[0];
        Thread hubCleanupRunnable = null;
        if (!(iotHubConnString == null || iotHubConnString.isEmpty() || iotHubConnString.equals(" ")))
        {
            hubCleanupRunnable = new Thread(new HubCleanupRunnable(iotHubConnString));
            hubCleanupRunnable.start();
        }

        Thread dpsCleanupRunnable = null;
        if (args.length > 1 && args[1] != null && !args[1].isEmpty())
        {
            String dpsConnString = args[1];
            dpsCleanupRunnable = new Thread(new DPSCleanupRunnable(dpsConnString));
            dpsCleanupRunnable.start();
        }

        if (hubCleanupRunnable != null)
        {
            hubCleanupRunnable.join();
        }

        if (dpsCleanupRunnable != null)
        {
            dpsCleanupRunnable.join();
        }
    }

    public static class HubCleanupRunnable implements Runnable {

        public String iotConnString;

        public HubCleanupRunnable(String iotConnString)
        {
            this.iotConnString = iotConnString;
        }

        @Override
        public void run() {
            RegistryManager registryManager = null;
            DeviceTwin deviceTwin = null;
            try
            {
                registryManager = RegistryManager.createFromConnectionString(iotConnString);
                deviceTwin = DeviceTwin.createFromConnectionString(iotConnString);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println("Could not create registry manager from the provided connection string, exiting iot hub cleanup thread");
                return;
            }

            System.out.println("Querying ");

            List<String> deviceIdsToRemove = new ArrayList<>();
            while (true)
            {
                try
                {
                    SqlQuery sqlQuery = SqlQuery.createSqlQuery("*", SqlQuery.FromType.DEVICES, null, null);
                    final Query twinQuery = deviceTwin.queryTwin(sqlQuery.getQuery(), 100);

                    while (deviceTwin.hasNextDeviceTwin(twinQuery))
                    {
                        DeviceTwinDevice d = deviceTwin.getNextDeviceTwin(twinQuery);

                        if (!d.getDeviceId().toLowerCase().contains("longhaul"))
                        {
                            deviceIdsToRemove.add(d.getDeviceId());

                            if (deviceIdsToRemove.size() > 100)
                            {
                                System.out.println("Queried 100 devices, now attempting to delete them");
                                break;
                            }
                        }
                    }

                    for (String deviceIdToRemove : deviceIdsToRemove)
                    {
                        try
                        {
                            registryManager.removeDevice(deviceIdToRemove);
                            System.out.println("Removed device " + deviceIdToRemove);
                        }
                        catch (Exception e)
                        {
                            System.out.println("Could not remove device with id " +deviceIdToRemove);
                            e.printStackTrace();

                            if (e instanceof IotHubNotFoundException)
                            {
                                System.out.print("NotFound error hit, querying next set of devices...");
                                break;
                            }
                            else
                            {
                                System.out.println("Moving onto deleting the remaining devices anyways...");
                            }
                        }
                    }

                    deviceIdsToRemove.clear();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    System.out.println("Could not collect the full list of device ids to delete, exiting iot hub cleanup thread");
                    return;
                }
            }
        }
    }

    public static class DPSCleanupRunnable implements Runnable
    {
        public String dpsConnString;

        public DPSCleanupRunnable(String dpsConnString)
        {
            this.dpsConnString = dpsConnString;
        }

        @Override
        public void run() {
            ProvisioningServiceClient provisioningServiceClient = ProvisioningServiceClient.createFromConnectionString(this.dpsConnString);
            QuerySpecification querySpecificationForAllEnrollments = new QuerySpecification("SELECT * FROM enrollments");

            deleteEnrollmentGroups(provisioningServiceClient, querySpecificationForAllEnrollments);
            deleteIndividualEnrollments(provisioningServiceClient, querySpecificationForAllEnrollments);
        }
    }

    private static void deleteIndividualEnrollments(ProvisioningServiceClient provisioningServiceClient, QuerySpecification querySpecificationForAllEnrollments)
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
                try {
                    Thread.sleep(4000);
                }
                catch (InterruptedException ex) {
                    //ignore
                }
            }
        }
    }

    private static void deleteEnrollmentGroups(ProvisioningServiceClient provisioningServiceClient, QuerySpecification querySpecificationForAllEnrollments)
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
                        Thread.sleep(2000);
                    }
                }
            }
            catch (Exception e)
            {
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException ex) {
                    //Ignore
                }

                //likely a throttling exception, just wait a bit
                e.printStackTrace();
            }
        }
    }
}
