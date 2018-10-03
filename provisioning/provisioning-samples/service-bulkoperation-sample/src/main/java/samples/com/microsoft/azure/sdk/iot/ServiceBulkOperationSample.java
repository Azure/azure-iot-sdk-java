// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.helpers.ProvisioningServiceClientHelper;
import com.microsoft.azure.sdk.iot.provisioning.service.implementation.ProvisioningServiceClientImpl;
import com.microsoft.azure.sdk.iot.provisioning.service.models.AttestationMechanism;
import com.microsoft.azure.sdk.iot.provisioning.service.models.BulkEnrollmentOperation;
import com.microsoft.azure.sdk.iot.provisioning.service.models.BulkEnrollmentOperationResult;
import com.microsoft.azure.sdk.iot.provisioning.service.models.IndividualEnrollment;
import com.microsoft.azure.sdk.iot.provisioning.service.models.ProvisioningServiceErrorDetailsException;
import com.microsoft.azure.sdk.iot.provisioning.service.models.QuerySpecification;
import com.microsoft.azure.sdk.iot.provisioning.service.models.TpmAttestation;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.rest.serializer.JacksonAdapter;

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
    private static final String DPS_BASE_URL = "[DPS Service Base Url]";
    private static final Map<String, String> DEVICE_MAP = new HashMap<String, String>()
    {
        {
            put("RegistrationId1","TPMEndorsementKey1");
            put("RegistrationId2","TPMEndorsementKey2");
        }
    };

    private static final String TPM_ATTESTATION = "tpm";
    private static final String BULK_OPERATION_CREATE = "create";
    private static final String BULK_OPERATION_UPDATE = "update";
    private static final String BULK_OPERATION_DELETE = "delete";
    private static final int QUERY_PAGE_SIZE = 3;

    public static void main(String[] args) throws ProvisioningServiceErrorDetailsException, JsonProcessingException
    {
        System.out.println("Starting sample...");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        // *********************************** Create a Provisioning Service Client ************************************
        ServiceClientCredentials credentials = ProvisioningServiceClientHelper.createCredentialsFromConnectionString(PROVISIONING_CONNECTION_STRING);
        
        RestClient simpleRestClient = new RestClient.Builder()
        	    .withBaseUrl(DPS_BASE_URL)
        	    .withCredentials(credentials)
        	    .withResponseBuilderFactory(new ServiceResponseBuilder.Factory())
        	  	.withSerializerAdapter(new JacksonAdapter())
        	  	.build();
        
		ProvisioningServiceClient provisioningServiceClient = new ProvisioningServiceClientImpl(simpleRestClient);

        // ******************************** Create a new bulk of individual enrollment *********************************
		System.out.println("\nCreate a new set of individualEnrollments...");
        List<IndividualEnrollment> individualEnrollments = new LinkedList<IndividualEnrollment>();
        for(Map.Entry<String, String> device:DEVICE_MAP.entrySet())
        {
            TpmAttestation attestation = new TpmAttestation().withEndorsementKey(device.getValue());
            AttestationMechanism attestationMechanism = new AttestationMechanism()
            		.withType(TPM_ATTESTATION)
            		.withTpm(attestation);
            String registrationId = device.getKey();
            System.out.println("  Add " + registrationId);
            IndividualEnrollment individualEnrollment = new IndividualEnrollment()
                    .withRegistrationId(registrationId)
                    .withAttestation(attestationMechanism);
            individualEnrollments.add(individualEnrollment);
        }
        
        BulkEnrollmentOperation bulkEnrollmentOperation = new BulkEnrollmentOperation()
        		.withEnrollments(individualEnrollments)
        		.withMode(BULK_OPERATION_CREATE);
        System.out.println("\nRun the bulk operation to create the individualEnrollments...");
        BulkEnrollmentOperationResult bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(bulkEnrollmentOperation);
        System.out.println("Result of the Create bulk enrollment...");
        System.out.println(bulkOperationResult.isSuccessful());

        // ************************************ Update the enrollments in bulk operation *************************************
        System.out.println("\nUpdating the enrollments in bulk...");
        List<IndividualEnrollment> updateEnrollments = new LinkedList<IndividualEnrollment>();
        for (IndividualEnrollment individualEnrollment : individualEnrollments)
        {
            String registrationId = individualEnrollment.registrationId();
            System.out.println("\nGet the individualEnrollment information for " + registrationId + ":");
            IndividualEnrollment getResult = provisioningServiceClient.getIndividualEnrollment(registrationId);
            System.out.println(ow.writeValueAsString(getResult));
            getResult.withDeviceId("java_device_id");
            updateEnrollments.add(getResult);
        }
        bulkEnrollmentOperation = new BulkEnrollmentOperation()
        		.withEnrollments(updateEnrollments)
        		.withMode(BULK_OPERATION_UPDATE);
        System.out.println("\nRun the bulk operation to update the individualEnrollments...");
        bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(bulkEnrollmentOperation);
        System.out.println("Result of the Update bulk enrollment...");
        System.out.println(bulkOperationResult.isSuccessful());

        // ************************************ Query info of individualEnrollments ***********************************
        System.out.println("\nCreate a query for enrollments...");
        QuerySpecification querySpecification =
                new QuerySpecification().withQuery("SELECT * FROM ENROLLMENTS");
        List<IndividualEnrollment> queryResult = provisioningServiceClient.queryIndividualEnrollments(querySpecification);
        
        for (IndividualEnrollment eachEnrollment : queryResult)
        {
        	System.out.println(ow.writeValueAsString(eachEnrollment));
        }

        // ********************************** Delete bulk of individualEnrollments ************************************
        System.out.println("\nDelete the set of individualEnrollments...");
        bulkEnrollmentOperation = new BulkEnrollmentOperation()
        		.withEnrollments(individualEnrollments)
        		.withMode(BULK_OPERATION_DELETE);
        bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(bulkEnrollmentOperation);
        System.out.println(bulkOperationResult.isSuccessful());
    }
}
