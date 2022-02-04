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

        TokenCredential credential =
            new ClientSecretCredentialBuilder()
                    .tenantId(parsedArguments.getTenantId())
                    .clientId(parsedArguments.getClientId())
                    .clientSecret(parsedArguments.getClientSecret())
                    .build();

        ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClient(parsedArguments.getDPSHostName(), credential);

        runIndividualEnrollmentSample(provisioningServiceClient);
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
}
