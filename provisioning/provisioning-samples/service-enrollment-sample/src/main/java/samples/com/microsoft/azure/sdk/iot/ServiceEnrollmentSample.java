// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.UUID;

/**
 * Create, get, query, and delete an individual enrollment on the Microsoft Azure IoT Hub Device Provisioning Service
 */
public class ServiceEnrollmentSample
{
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";

    private static final String REGISTRATION_ID = "[RegistrationId]";
    private static final String TPM_ENDORSEMENT_KEY = "[TPM Endorsement Key]";

    // Optional parameters
    private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";
    private static final String DEVICE_ID = "myJavaDevice";
    private static final ProvisioningStatus PROVISIONING_STATUS = ProvisioningStatus.ENABLED;

    public static void main(String[] args) throws ProvisioningServiceClientException
    {
        System.out.println("Starting sample...");

        // *********************************** Create a Provisioning Service Client ************************************
        ProvisioningServiceClient provisioningServiceClient =
                ProvisioningServiceClient.createFromConnectionString(PROVISIONING_CONNECTION_STRING);

        // ******************************** Create a new individual enrollment config **********************************
        System.out.println("\nCreate a new enrollment...");
        Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
        Enrollment enrollment =
                new Enrollment(
                        REGISTRATION_ID,
                        attestation);

        // The following parameters are optional. Remove it if you don't need.
        enrollment.setDeviceId(DEVICE_ID);
        enrollment.setIotHubHostName(IOTHUB_HOST_NAME);
        enrollment.setProvisioningStatus(PROVISIONING_STATUS);

        // ************************************ Create the individual enrollment *************************************
        System.out.println("\nAdd new enrollment...");
        Enrollment enrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(enrollment);
        System.out.println("\nEnrollment created with success...");
        System.out.println(enrollmentResult);

        // ************************************* Get info of individual enrollment *************************************
        System.out.println("\nGet the enrollment information...");
        Enrollment getResult = provisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
        System.out.println(getResult);

        // ************************************ Query info of individual enrollment ************************************
        System.out.println("\nCreate a query for enrollments...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next enrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }

        // *********************************** Delete info of individual enrollment ************************************
        System.out.println("\nDelete the enrollment...");
        provisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID);
    }
}
