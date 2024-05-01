// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.HashMap;
import java.util.Map;

/**
 * Update the information in an individual enrollment on the Microsoft Azure IoT Hub Device Provisioning Service.
 *
 * <p> This sample will show how to update the information in a exited individualEnrollment. It will start creating
 *     a new individualEnrolment. This enrollment contains an initialTwin state with the following information.
 * <pre>
 * {@code
 * {
 *     "Brand":"Contoso",
 *     "Model":"SSC4",
 *     "Color":"White",
 * }
 * }
 * </pre>
 * <p> After that, the name of the color shall be updated to "Glace white".
 * <p> <b>Note:</b> If the device is already provisioned with the preview initialTwin state. Update the
 *     individualEnrollment will not change the Twin state in the device.
 */
public class ServiceUpdateEnrollmentSample
{
    /* The Device Provisioning Service connection string that you recover from the azure portal. */
    private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";

    /* Details of the initial individualEnrollment. */
    private static final String REGISTRATION_ID = "[RegistrationId]";
    private static final String TPM_ENDORSEMENT_KEY = "[TPM Endorsement Key]";

    public static void main(String[] args) throws ProvisioningServiceClientException
    {
        System.out.println("Starting sample...");

        // *********************************** Create a Provisioning Service Client ************************************
        ProvisioningServiceClient provisioningServiceClient =
                new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // ************************************ Create a new individualEnrollment **************************************
        System.out.println("\nCreate a new individualEnrollment...");
        Map<String, Object> desiredProperties = new HashMap<String, Object>()
        {
            {
                put("Brand", "Contoso");
                put("Model", "SSC4");
                put("Color", "White");
            }
        };
        Attestation attestation = new TpmAttestation(TPM_ENDORSEMENT_KEY);
        TwinState initialTwinState = new TwinState(
                null,
                new TwinCollection(desiredProperties)
                );
        IndividualEnrollment individualEnrollment =
                new IndividualEnrollment(
                        REGISTRATION_ID,
                        attestation);
        individualEnrollment.setInitialTwin(initialTwinState);
        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
        System.out.println("\nIndividualEnrollment created with success...");
        System.out.println(
                "Note that there is a difference between the content of the individualEnrollment that you sent and\n" +
                "  the individualEnrollmentResult that you received. The individualEnrollmentResult contains the eTag.");
        System.out.println(
                "\nindividualEnrollment:\n" + individualEnrollment);
        System.out.println(
                "\nindividualEnrollmentResult:\n" + individualEnrollmentResult);

        // ********************************* Update the info of individualEnrollment ***********************************
        /*
         * At this point, if you try to update your information in the provisioning service using the individualEnrollment
         * that you created, it will fail because of the "precondition". It will happen because the individualEnrollment
         * do not contains the eTag, and the provisioning service will not be able to check if the enrollment that you
         * are updating is the correct one.
         *
         * So, to update the information you must use the individualEnrollmentResult that the provisioning service returned
         * when you created the enrollment, another solution is get the latest enrollment from the provisioning service
         * using the provisioningServiceClient.getIndividualEnrollment(), the result of this operation is an IndividualEnrollment
         * object that contains the eTag.
         */
        System.out.println("\nUpdating the enrollment...");
        desiredProperties.put("Color", "Glace white");
        initialTwinState = new TwinState(
                null,
                new TwinCollection(desiredProperties)
        );
        individualEnrollmentResult.setInitialTwin(initialTwinState);
        individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollmentResult);
        System.out.println("\nIndividualEnrollment updated with success...");
        System.out.println(individualEnrollmentResult);

        // *********************************** Delete info of individualEnrollment *************************************
        System.out.println("\nDelete the individualEnrollment...");
        provisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID);
    }
}
