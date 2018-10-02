// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import java.util.HashMap;
import java.util.Map;

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

/**
 * Create, get, query, and delete an individual enrollment on the Microsoft Azure IoT Hub Device Provisioning Service
 */
public class ServiceEnrollmentSample
{
    /*
     * Details of the Provisioning.
     */
    private static final String PROVISIONING_CONNECTION_STRING = "[Provisioning Connection String]";
    private static final String DPS_BASE_URL = "[DPS Service Base Url]";

    private static final String REGISTRATION_ID = "[RegistrationId]";
    private static final String TPM_ENDORSEMENT_KEY = "[TPM Endorsement Key]";

    // Optional parameters
    private static final String IOTHUB_HOST_NAME = "[Host name].azure-devices.net";
    private static final String DEVICE_ID = "myJavaDevice";
    private static final String PROVISIONING_STATUS = "enabled";
    private static final String TPM_ATTESTATION = "tpm";

    public static void main(String[] args) throws ProvisioningServiceErrorDetailsException, JsonProcessingException
    {
        System.out.println("Starting sample...");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        // *********************************** Create a Provisioning Service Client ************************************
        ServiceClientCredentials credentials = ProvisioningServiceClientExtension.createCredentialsFromConnectionString(PROVISIONING_CONNECTION_STRING);
        RestClient simpleRestClient = new RestClient.Builder(new OkHttpClient.Builder(), new Retrofit.Builder())
        	    .withBaseUrl(DPS_BASE_URL)
        	    .withCredentials(credentials)
        	    .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
        	  	.withSerializerAdapter(new JacksonAdapter())
        	  	.build();
        
		ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClientImpl(simpleRestClient);
        
        // ******************************** Create a new individualEnrollment config **********************************
		System.out.println("\nCreate a new individualEnrollment...");
        TpmAttestation attestation = new TpmAttestation().withEndorsementKey(TPM_ENDORSEMENT_KEY);
        AttestationMechanism attestationMechanism = new AttestationMechanism()
        		.withType(TPM_ATTESTATION)
        		.withTpm(attestation);
        Map<String, Object> desiredProperties = new HashMap<String, Object>()
        {
            {
                put("Brand", "Contoso");
                put("Model", "SSC4");
                put("Color", "White");
            }
        };
        IndividualEnrollment individualEnrollment = new IndividualEnrollment()
        		.withRegistrationId(REGISTRATION_ID)
        		.withAttestation(attestationMechanism);

        // The following parameters are optional. Remove it if you don't need.
        InitialTwin initialTwin = new InitialTwin().withProperties(
        		new InitialTwinProperties().withDesired(
        				new TwinCollection().withAdditionalProperties(
        						desiredProperties)));
        individualEnrollment
        	.withDeviceId(DEVICE_ID)
        	.withIotHubHostName(IOTHUB_HOST_NAME)
        	.withProvisioningStatus(PROVISIONING_STATUS)
        	.withInitialTwin(initialTwin);

        // ************************************ Create the individualEnrollment *************************************
        System.out.println("\nAdd new individualEnrollment...");
        IndividualEnrollment individualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(REGISTRATION_ID, individualEnrollment);
        System.out.println("\nIndividualEnrollment created with success...");
        System.out.println(ow.writeValueAsString(individualEnrollmentResult));

        // ************************************* Get info of individualEnrollment *************************************
        System.out.println("\nGet the individualEnrollment information...");
        IndividualEnrollment getIndividualEnrollmentResult = provisioningServiceClient.getIndividualEnrollment(REGISTRATION_ID);
        System.out.println(ow.writeValueAsString(getIndividualEnrollmentResult));
        
        // ********************************* Update the info of individualEnrollment ***********************************
        System.out.println("\nUpdate the individualEnrollment information...");
        desiredProperties.put("Color", "Glace white");
        initialTwin.withProperties(
        		new InitialTwinProperties().withDesired(
        				new TwinCollection().withAdditionalProperties(
        						desiredProperties)));
        getIndividualEnrollmentResult.withInitialTwin(initialTwin);
        IndividualEnrollment updateIndividualEnrollmentResult =  provisioningServiceClient.createOrUpdateIndividualEnrollment(REGISTRATION_ID, getIndividualEnrollmentResult, getIndividualEnrollmentResult.etag());
        System.out.println("\nIndividualEnrollment updated with success...");
        System.out.println(ow.writeValueAsString(updateIndividualEnrollmentResult));

        // ************************************ Query info of individualEnrollment ************************************
        /*System.out.println("\nCreate a query for enrollments...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next enrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }*/

        // *********************************** Delete info of individualEnrollment ************************************
        System.out.println("\nDelete the individualEnrollment...");
        provisioningServiceClient.deleteIndividualEnrollment(REGISTRATION_ID);
    }
}
