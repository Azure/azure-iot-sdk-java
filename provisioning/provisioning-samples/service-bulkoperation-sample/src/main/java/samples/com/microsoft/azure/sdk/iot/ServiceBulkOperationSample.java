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
                new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // ******************************** Create a new bulk of individual enrollment *********************************
        System.out.println("\nCreate a new set of individualEnrollments...");
        List<IndividualEnrollment> individualEnrollments = new LinkedList<>();
        for(Map.Entry<String, String> device:DEVICE_MAP.entrySet())
        {
            Attestation attestation = new TpmAttestation(device.getValue());
            String registrationId = device.getKey();
            System.out.println("  Add " + registrationId);
            IndividualEnrollment individualEnrollment =
                    new IndividualEnrollment(
                            registrationId,
                            attestation);
            individualEnrollments.add(individualEnrollment);
        }

        System.out.println("\nRun the bulk operation to create the individualEnrollments...");
        BulkEnrollmentOperationResult bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(
                BulkOperationMode.CREATE, individualEnrollments);
        System.out.println("Result of the Create bulk enrollment...");
        System.out.println(bulkOperationResult);

        // ************************************ Get info of individualEnrollments *************************************
        for (IndividualEnrollment individualEnrollment : individualEnrollments)
        {
            String registrationId = individualEnrollment.getRegistrationId();
            System.out.println("\nGet the individualEnrollment information for " + registrationId + ":");
            IndividualEnrollment getResult = provisioningServiceClient.getIndividualEnrollment(registrationId);
            System.out.println(getResult);
        }

        // ************************************ Query info of individualEnrollments ***********************************
        System.out.println("\nCreate a query for individualEnrollments...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification, QUERY_PAGE_SIZE);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next individualEnrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }

        // ********************************** Delete bulk of individualEnrollments ************************************
        System.out.println("\nDelete the set of individualEnrollments...");
        bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(BulkOperationMode.DELETE, individualEnrollments);
        System.out.println(bulkOperationResult);
    }
}
