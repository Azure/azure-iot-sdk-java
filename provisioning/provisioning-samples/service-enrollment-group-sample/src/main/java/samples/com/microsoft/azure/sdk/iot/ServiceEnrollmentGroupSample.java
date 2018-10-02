// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.SharedAccessKeyCredentials;
import com.microsoft.azure.sdk.iot.provisioning.service.implementation.ProvisioningServiceClientImpl;
import com.microsoft.azure.sdk.iot.provisioning.service.models.*;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.serializer.JacksonAdapter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
    private static final String DPS_BASE_URL = "[DPS Service Base Url]";
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
    
    private static final String PROVISIONING_STATUS_ENABLED = "enabled";
    private static final String X509_ATTESTATION = "x509";

    /*
     * The IoT Hub Host Name is an optional parameter, and it must fit one of the IoTHubs that you linked to your
     * provisioning service.
     */
    private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";

    public static void main(String[] args) throws ProvisioningServiceErrorDetailsException, JsonProcessingException
    {
        System.out.println("Starting sample...");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        /*
         * Create the device collection.
         */
        String enrollmentGroupId = "enrollmentgroupid-" + UUID.randomUUID();

        // *********************************** Create a Provisioning Service Client ************************************
        ServiceClientCredentials credentials = ProvisioningServiceClientExtension.createCredentialsFromConnectionString(PROVISIONING_CONNECTION_STRING);
        RestClient simpleRestClient = new RestClient.Builder(new OkHttpClient.Builder(), new Retrofit.Builder())
        	    .withBaseUrl(DPS_BASE_URL)
        	    .withCredentials(credentials)
        	    .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
        	  	.withSerializerAdapter(new JacksonAdapter())
        	  	.build();
        
		ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClientImpl(simpleRestClient);

        // *************************************** Create a new enrollmentGroup ****************************************
		System.out.println("\nCreate a new enrollmentGroup...");
        X509Attestation attestation = new X509Attestation().withSigningCertificates(
        		new X509Certificates().withPrimary(
        				new X509CertificateWithInfo().withCertificate(PUBLIC_KEY_CERTIFICATE_STRING)));
        AttestationMechanism attestationMechanism = new AttestationMechanism()
        		.withType(X509_ATTESTATION)
        		.withX509(attestation);
        EnrollmentGroup enrollmentGroup = new EnrollmentGroup()
        		.withEnrollmentGroupId(enrollmentGroupId)
        		.withAttestation(attestationMechanism);
        
        enrollmentGroup.withIotHubHostName(IOTHUB_HOST_NAME);                // Optional parameter.
        enrollmentGroup.withProvisioningStatus(PROVISIONING_STATUS_ENABLED);  // Optional parameter.
        System.out.println("\nAdd new enrollmentGroup...");
        EnrollmentGroup enrollmentGroupResult =  provisioningServiceClient.createOrUpdateEnrollmentGroup(enrollmentGroupId, enrollmentGroup);
        System.out.println("\nEnrollmentGroup created with success...");
        System.out.println(ow.writeValueAsString(enrollmentGroupResult));

        // **************************************** Get info of enrollmentGroup ****************************************
        System.out.println("\nGet the enrollmentGroup information...");
        EnrollmentGroup getResult = provisioningServiceClient.getEnrollmentGroup(enrollmentGroupId);
        System.out.println(ow.writeValueAsString(getResult));

        // *************************************** Query info of enrollmentGroup ***************************************
        /*System.out.println("\nCreate a query for the enrollmentGroups...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENT_GROUPS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createEnrollmentGroupQuery(querySpecification);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next enrollmentGroups...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }*/

        // ************************************** Delete info of enrollmentGroup ***************************************
        System.out.println("\nDelete the enrollmentGroup...");
        provisioningServiceClient.deleteEnrollmentGroup(enrollmentGroupId);
    }
}
