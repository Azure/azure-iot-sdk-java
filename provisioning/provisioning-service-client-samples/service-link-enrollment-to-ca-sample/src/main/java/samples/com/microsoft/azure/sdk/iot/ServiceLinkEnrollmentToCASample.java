// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.*;

/**
 * Link Individual Enrollment in DPS to a Certificate Authority (CA). Intended to be used with the
 * Provisioning Certificate Issuance Sample.
 */
public class ServiceLinkEnrollmentToCASample
{
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "";

    private static final String REGISTRATION_ID = "";

    private static final String CA_NAME = "";

    public static void main(String[] args) throws ProvisioningServiceClientException
    {
        System.out.println("Starting sample...");

        ClientCertificateIssuancePolicy clientCertificateIssuancePolicy = new ClientCertificateIssuancePolicy();
        clientCertificateIssuancePolicy.setCertificateAuthorityName(CA_NAME);

        ProvisioningServiceClient provisioningServiceClient =
                new ProvisioningServiceClient(PROVISIONING_CONNECTION_STRING);

        // Get current enrollment from DPS and add a Certificate Issuance Policy
        IndividualEnrollment createdEnrollment = provisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
        createdEnrollment.setClientCertificateIssuancePolicy(clientCertificateIssuancePolicy);

        // Update enrollment
        IndividualEnrollment updatedEnrollment = provisioningServiceClient.createOrUpdateIndividualEnrollment(createdEnrollment);

        System.out.println("Enrollment updated, ready to run Provisioning Certificate Issuance Sample. Exiting current sample.");
    }
}
