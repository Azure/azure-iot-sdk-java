// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package samples.com.microsoft.azure.sdk.iot;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.Query;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;

import java.util.UUID;

public class ProvisioningRoleBasedAuthenticationSample
{
    public static void main (String[] args) throws ProvisioningServiceClientException
    {
        SamplesArguments parsedArguments = new SamplesArguments(args);

        // ****************** Create a TokenCredential from provided arguments **********************
        TokenCredential credential =
            new ClientSecretCredentialBuilder()
                    .tenantId(parsedArguments.getTenantId())
                    .clientId(parsedArguments.getClientId())
                    .clientSecret(parsedArguments.getClientSecret())
                    .build();

        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(parsedArguments.getDPSHostName(), credential);

        runIndividualEnrollmentSample(provisioningServiceClient);

        runEnrollmentGroupSample(provisioningServiceClient);
    }

    private static void runIndividualEnrollmentSample (ProvisioningServiceClient provisioningServiceClient) throws ProvisioningServiceClientException
    {
        // ******************************** Create a new individualEnrollment config **********************************
        String registrationId = "my-new-enrollment-" + UUID.randomUUID();
        Attestation attestation = new SymmetricKeyAttestation("", "");
        IndividualEnrollment individualEnrollment = new IndividualEnrollment(registrationId, attestation);

        // ************************************ Create the individualEnrollment *************************************
        System.out.println("\nAdd new individualEnrollment...");
        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(individualEnrollment);
        System.out.println("\nIndividualEnrollment created with success...");
        System.out.println(individualEnrollmentResult);

        // ************************************* Get info of individualEnrollment *************************************
        System.out.println("\nGet the individualEnrollment information...");
        IndividualEnrollment getResult = provisioningServiceClient.getIndividualEnrollment(registrationId);
        System.out.println(getResult);

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
        provisioningServiceClient.deleteIndividualEnrollment(registrationId);
    }

    private static void runEnrollmentGroupSample (ProvisioningServiceClient provisioningServiceClient) throws ProvisioningServiceClientException
    {
        /*
         * Create the device collection.
         */
        String enrollmentGroupId = "enrollmentgroupid-" + UUID.randomUUID();

        // *************************************** Create a new enrollmentGroup ****************************************
        System.out.println("\nCreate a new enrollmentGroup...");
        Attestation attestation = new SymmetricKeyAttestation("", "");
        EnrollmentGroup enrollmentGroup =
                new EnrollmentGroup(
                        enrollmentGroupId,
                        attestation);
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
