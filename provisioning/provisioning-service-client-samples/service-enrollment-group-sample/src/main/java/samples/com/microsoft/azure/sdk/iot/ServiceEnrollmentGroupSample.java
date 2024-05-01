// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.UUID;

/**
 * Create, get, query, and delete an enrollmentGroup on the Microsoft Azure IoT Hub Device Provisioning Service
 */
public class ServiceEnrollmentGroupSample
{
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";
    private static final String PUBLIC_KEY_CERTIFICATE_STRING =
            "-----BEGIN CERTIFICATE-----\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n" +
            "-----END CERTIFICATE-----\n";

    /*
     * The IoT Hub Host Name is an optional parameter, and it must fit one of the IoTHubs that you linked to your
     * provisioning service.
     */
    private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";

    public static void main(String[] args) throws ProvisioningServiceClientException
    {
        System.out.println("Starting sample...");

        /*
         * Create the device collection.
         */
        String enrollmentGroupId = "enrollmentgroupid-" + UUID.randomUUID();

        // *********************************** Create a Provisioning Service Client ************************************
        ProvisioningServiceClient provisioningServiceClient =
                new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // *************************************** Create a new enrollmentGroup ****************************************
        System.out.println("\nCreate a new enrollmentGroup...");
        Attestation attestation = X509Attestation.createFromRootCertificates(PUBLIC_KEY_CERTIFICATE_STRING);
        EnrollmentGroup enrollmentGroup =
                new EnrollmentGroup(
                        enrollmentGroupId,
                        attestation);
        enrollmentGroup.setIotHubHostName(IOTHUB_HOST_NAME);                // Optional parameter.
        enrollmentGroup.setProvisioningStatus(ProvisioningStatus.ENABLED);  // Optional parameter.
        System.out.println("\nAdd new enrollmentGroup...");
        EnrollmentGroup enrollmentGroupResult =  provisioningServiceClient.createOrUpdateEnrollmentGroup(enrollmentGroup);
        System.out.println("\nEnrollmentGroup created with success...");
        System.out.println(enrollmentGroupResult);

        // **************************************** Get info of enrollmentGroup ****************************************
        System.out.println("\nGet the enrollmentGroup information...");
        EnrollmentGroup getResult = provisioningServiceClient.getEnrollmentGroup(enrollmentGroupId);
        System.out.println(getResult);

        // *************************************** Query info of enrollmentGroup ***************************************
        System.out.println("\nCreate a query for the enrollmentGroups...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENT_GROUPS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createEnrollmentGroupQuery(querySpecification);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next enrollmentGroups...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }

        // ************************************** Delete info of enrollmentGroup ***************************************
        System.out.println("\nDelete the enrollmentGroup...");
        provisioningServiceClient.deleteEnrollmentGroup(enrollmentGroupId);
    }
}
