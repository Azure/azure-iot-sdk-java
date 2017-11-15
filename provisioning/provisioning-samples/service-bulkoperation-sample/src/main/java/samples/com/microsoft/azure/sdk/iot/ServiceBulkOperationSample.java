// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.*;

/**
 * Create, get, query, and delete a set of individual enrollments on the Microsoft Azure IoT Hub Device
 * Provisioning Service
 */
public class ServiceBulkOperationSample
{
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";
    private static final Map<String, String> DEVICE_MAP = new HashMap<String, String>()
    {
        {
            put("RegistrationId1","TPMEndorsementKey1");
            put("RegistrationId2","TPMEndorsementKey2");
        }
    };

    private static final int QUERY_PAGE_SIZE = 3;

    public static void main(String[] args) throws ProvisioningServiceClientException
    {
        System.out.println("Starting sample...");

        // *********************************** Create a Provisioning Service Client ************************************
        ProvisioningServiceClient provisioningServiceClient =
                ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // ******************************** Create a new bulk of individual enrollment *********************************
        System.out.println("\nCreate a new set of enrollments...");
        List<Enrollment> enrollments = new LinkedList<>();
        for(Map.Entry<String, String> device:DEVICE_MAP.entrySet())
        {
            Attestation attestation = new TpmAttestation(device.getValue());
            String registrationId = device.getKey();
            System.out.println("  Add " + registrationId);
            Enrollment enrollment =
                    new Enrollment(
                            registrationId,
                            attestation);
            enrollments.add(enrollment);
        }

        System.out.println("\nRun the bulk operation to create the enrollments...");
        BulkOperationResult bulkOperationResult =  provisioningServiceClient.runBulkOperation(
                BulkOperationMode.CREATE, enrollments);
        System.out.println("Result of the Create bulk enrollment...");
        System.out.println(bulkOperationResult);

        // ************************************ Get info of individual enrollments *************************************
        for (Enrollment enrollment: enrollments)
        {
            String registrationId = enrollment.getRegistrationId();
            System.out.println("\nGet the enrollment information for " + registrationId + ":");
            Enrollment getResult = provisioningServiceClient.getIndividualEnrollment(registrationId);
            System.out.println(getResult);
        }

        // ************************************ Query info of individual enrollments ***********************************
        System.out.println("\nCreate a query for enrollments...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification, QUERY_PAGE_SIZE);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next enrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }

        // ********************************** Delete bulk of individual enrollments ************************************
        System.out.println("\nDelete the set of enrollments...");
        bulkOperationResult =  provisioningServiceClient.runBulkOperation(BulkOperationMode.DELETE, enrollments);
        System.out.println(bulkOperationResult);
    }
}
