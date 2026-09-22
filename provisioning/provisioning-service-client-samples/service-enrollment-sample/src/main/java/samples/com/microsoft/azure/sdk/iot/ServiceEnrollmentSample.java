// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.Collections;

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
                new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // ******************************** Create a new individualEnrollment config **********************************
        System.out.println("\nCreate a new individualEnrollment...");
        Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
        IndividualEnrollment individualEnrollment =
                new IndividualEnrollment(
                        REGISTRATION_ID,
                        attestation);

        // The following parameters are optional. Remove it if you don't need.
        individualEnrollment.setDeviceId(DEVICE_ID);
        individualEnrollment.setIotHubHostName(IOTHUB_HOST_NAME);
        individualEnrollment.setProvisioningStatus(PROVISIONING_STATUS);

        // ***** Optional 2026-11-02-preview fields *****
        // These fields are supported by the 2026-11-02-preview DPS service API version, which this SDK targets.
        // Remove them if you don't need them.
        individualEnrollment.setNamespaceName("[Namespace Name]");
        individualEnrollment.setCertificateAuthorityName("[Certificate Authority Name]");
        individualEnrollment.setCertificatePolicyName("[Certificate Policy Name]");
        individualEnrollment.setDeviceTypeRefs(Collections.singletonList("[Device Type Ref]"));

        // ************************************ Create the individualEnrollment *************************************
        System.out.println("\nAdd new individualEnrollment...");
        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
        System.out.println("\nIndividualEnrollment created with success...");
        System.out.println(individualEnrollmentResult);

        // ************************************* Get info of individualEnrollment *************************************
        System.out.println("\nGet the individualEnrollment information...");
        IndividualEnrollment getResult = provisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
        System.out.println(getResult);

        // ***** Inspect 2026-11-02-preview response fields *****
        System.out.println("\nNamespace name: " + getResult.getNamespaceName());
        System.out.println("Certificate authority name: " + getResult.getCertificateAuthorityName());
        System.out.println("Certificate policy name: " + getResult.getCertificatePolicyName());
        System.out.println("Device type refs: " + getResult.getDeviceTypeRefs());

        // The device registration state carries a response-only connectionProfile (2026-11-02-preview).
        // It is an extensible enum (for example "classic" or "mqttV5"); unknown values are preserved as-is.
        DeviceRegistrationState registrationState = getResult.getDeviceRegistrationState();
        if (registrationState != null)
        {
            System.out.println("Connection profile: " + registrationState.getConnectionProfile());
        }

        // ************************************ Query info of individualEnrollment ************************************
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

        // *********************************** Delete info of individualEnrollment ************************************
        System.out.println("\nDelete the individualEnrollment...");
        provisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID);
    }
}
