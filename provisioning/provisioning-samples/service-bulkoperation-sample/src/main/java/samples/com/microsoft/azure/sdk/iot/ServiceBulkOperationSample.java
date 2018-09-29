// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package samples.com.microsoft.azure.sdk.iot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;
import com.microsoft.azure.sdk.iot.provisioning.service.auth.SharedAccessKeyCredentials;
import com.microsoft.azure.sdk.iot.provisioning.service.implementation.ProvisioningServiceClientImpl;
import com.microsoft.azure.sdk.iot.provisioning.service.models.*;
import com.microsoft.rest.RestClient;
import com.microsoft.rest.ServiceResponseBuilder;
import com.microsoft.rest.serializer.JacksonAdapter;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
    private static final String BULK_OPERATION__UPDATE = "update";
    private static final String BULK_OPERATION_DELETE = "delete";
    private static final int QUERY_PAGE_SIZE = 3;

    public static void main(String[] args) throws ProvisioningServiceErrorDetailsException, JsonProcessingException
    {
        System.out.println("Starting sample...");

        // *********************************** Create a Provisioning Service Client ************************************
        SharedAccessKeyCredentials credentials = new SharedAccessKeyCredentials(PROVISIONING_CONNECTION_STRING);
        RestClient simpleRestClient = new RestClient.Builder(new OkHttpClient.Builder(), new Retrofit.Builder())
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
            getResult.withDeviceId("java_device_id");
            updateEnrollments.add(getResult);
        }
        bulkEnrollmentOperation = new BulkEnrollmentOperation()
        		.withEnrollments(updateEnrollments)
        		.withMode(BULK_OPERATION__UPDATE);
        System.out.println("\nRun the bulk operation to update the individualEnrollments...");
        bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(bulkEnrollmentOperation);
        System.out.println("Result of the Update bulk enrollment...");
        System.out.println(bulkOperationResult.isSuccessful());

        // ************************************ Query info of individualEnrollments ***********************************
        /*System.out.println("\nCreate a query for individualEnrollments...");
        QuerySpecification querySpecification =
                new QuerySpecificationBuilder("*", QuerySpecificationBuilder.FromType.ENROLLMENTS)
                        .createSqlQuery();
        Query query = provisioningServiceClient.createIndividualEnrollmentQuery(querySpecification, QUERY_PAGE_SIZE);

        while(query.hasNext())
        {
            System.out.println("\nQuery the next individualEnrollments...");
            QueryResult queryResult = query.next();
            System.out.println(queryResult);
        }*/

        // ********************************** Delete bulk of individualEnrollments ************************************
        System.out.println("\nDelete the set of individualEnrollments...");
        bulkEnrollmentOperation = new BulkEnrollmentOperation()
        		.withEnrollments(individualEnrollments)
        		.withMode(BULK_OPERATION_DELETE);
        bulkOperationResult =  provisioningServiceClient.runBulkEnrollmentOperation(bulkEnrollmentOperation);
        System.out.println(bulkOperationResult.isSuccessful());
    }
}
