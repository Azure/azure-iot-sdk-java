// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.*;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientServiceException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * IndividualEnrollment Manager
 *
 * <p> This is the inner class that implements the IndividualEnrollment APIs.
 * <p> For the public API, please see {@link ProvisioningServiceClient}.
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
class IndividualEnrollmentManager
{
    private final ContractApiHttp contractApiHttp;
    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_ENROLLMENTS = "enrollments";
    private static final String CONDITION_KEY = "If-Match";
    private static final String ATTESTATION_MECHANISM = "attestationmechanism";

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param contractApiHttp is the device registration client for one of the protocols
     * @throws IllegalArgumentException if the {@link ContractApiHttp} is {@code null}
     */
    private IndividualEnrollmentManager(ContractApiHttp contractApiHttp)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_001: [The constructor shall throw IllegalArgumentException if the provided ProvisioningConnectionString is null.] */
        if(contractApiHttp == null)
        {
            throw new IllegalArgumentException("ContractApiHttp cannot be null");
        }
        this.contractApiHttp = contractApiHttp;
    }

    /**
     * Create a new instance of the IndividualEnrollmentManager using the provided connection
     *    string and https as the transport protocol.
     *
     * @param contractApiHttp is the class that cares the Http communication.
     * @return The {@code IndividualEnrollmentManager} with the new instance of this class.
     * @throws IllegalArgumentException if the {@link ContractApiHttp} is {@code null}.
     */
    static IndividualEnrollmentManager createFromContractApiHttp(ContractApiHttp contractApiHttp)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_004: [The factory shall create a new instance of this.] */
        return new IndividualEnrollmentManager(contractApiHttp);
    }

    /**
     * Create or update a device enrollment record.
     *
     * @see ProvisioningServiceClient#createOrUpdateIndividualEnrollment(IndividualEnrollment)
     *
     * @param individualEnrollment is an {@link IndividualEnrollment} that describes the enrollment that will be created of updated. It cannot be {@code null}.
     * @return An {@link IndividualEnrollment} with the result of the creation or update request.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to create or update the enrollment.
     */
    IndividualEnrollment createOrUpdate(IndividualEnrollment individualEnrollment) throws ProvisioningServiceClientException
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_005: [The createOrUpdate shall throw IllegalArgumentException if the provided individualEnrollment is null.] */
        if(individualEnrollment == null)
        {
            throw new IllegalArgumentException("individualEnrollment cannot be null.");
        }
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_006: [The createOrUpdate shall send a Http request for the path `enrollments/[registrationId]`.] */
        String id = individualEnrollment.getRegistrationId();
        String enrollmentPath = IndividualEnrollmentManager.getEnrollmentPath(id);

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_007: [The createOrUpdate shall send a Http request with a body with the individualEnrollment content in JSON format.] */
        String enrollmentPayload = individualEnrollment.toJson();

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_045: [If the individualEnrollment contains eTag, the createOrUpdate shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(individualEnrollment.getEtag()))
        {
            headerParameters.put(CONDITION_KEY, individualEnrollment.getEtag());
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_008: [The createOrUpdate shall send a Http request with a Http verb `PUT`.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_009: [The createOrUpdate shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_010: [The createOrUpdate shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        HttpResponse httpResponse =
                    contractApiHttp.request(
                            HttpMethod.PUT,
                            enrollmentPath,
                            headerParameters,
                            enrollmentPayload);

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_042: [The createOrUpdate shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
        byte[] body = httpResponse.getBody();
        if(body == null)
        {
            throw new ProvisioningServiceClientServiceException("Http response for createOrUpdate cannot contains a null body");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_011: [The createOrUpdate shall return an IndividualEnrollment object created from the body of the response for the Http request .] */
        return new IndividualEnrollment(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * Rum a bulk individualEnrollment operation.
     *
     * @see ProvisioningServiceClient#runBulkEnrollmentOperation(BulkOperationMode, Collection)
     *
     * @param bulkOperationMode the {@link BulkOperationMode} that defines the single operation to do over the individualEnrollments. It cannot be {@code null}.
     * @param individualEnrollments the collection of {@link IndividualEnrollment} that contains the description of each individualEnrollment. It cannot be {@code null} or empty.
     * @return An {@link BulkEnrollmentOperationResult} with the result of the bulk operation request.
     * @throws IllegalArgumentException if the provided parameters are not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    BulkEnrollmentOperationResult bulkOperation(BulkOperationMode bulkOperationMode, Collection<IndividualEnrollment> individualEnrollments) throws ProvisioningServiceClientException
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_012: [The BulkEnrollmentOperation shall throw IllegalArgumentException if the provided bulkOperationMode is null.] */
        if(bulkOperationMode == null)
        {
            throw new IllegalArgumentException("bulkOperationMode cannot be null.");
        }
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_013: [The BulkEnrollmentOperation shall throw IllegalArgumentException if the provided individualEnrollments is null or empty.] */
        if((individualEnrollments == null) || individualEnrollments.isEmpty())
        {
            throw new IllegalArgumentException("individualEnrollments cannot be null or empty.");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_015: [The BulkEnrollmentOperation shall send a Http request with a body with the individualEnrollments content in JSON format.] */
        String bulkEnrollmentPayload = BulkEnrollmentOperation.toJson(bulkOperationMode, individualEnrollments);

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_016: [The BulkEnrollmentOperation shall send a Http request with a Http verb `POST`.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_017: [The BulkEnrollmentOperation shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_018: [The BulkEnrollmentOperation shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        HttpResponse httpResponse =
                contractApiHttp.request(
                        HttpMethod.POST,
                        PATH_ENROLLMENTS,
                        null,
                        bulkEnrollmentPayload);

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_043: [The BulkEnrollmentOperation shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
        byte[] body = httpResponse.getBody();
        if(body == null)
        {
            throw new ProvisioningServiceClientServiceException("Http response for BulkEnrollmentOperation cannot contains a null body");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_019: [The BulkEnrollmentOperation shall return a BulkEnrollmentOperationResult object created from the body of the response for the Http request .] */
        return new BulkEnrollmentOperationResult(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * Get individualEnrollment information.
     *
     * @see ProvisioningServiceClient#getIndividualEnrollment(String)
     *
     * @param registrationId the {@code String} that identifies the individualEnrollment. It cannot be {@code null} or empty.
     * @return An {@link IndividualEnrollment} with the enrollment information.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the get operation.
     */
    IndividualEnrollment get(String registrationId) throws ProvisioningServiceClientException
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_020: [The get shall throw IllegalArgumentException if the provided registrationId is null or empty.] */
        if(Tools.isNullOrEmpty(registrationId))
        {
            throw new IllegalArgumentException("registrationId cannot be null or empty.");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_021: [The get shall send a Http request for the path `enrollments/[registrationId]`.] */
        String enrollmentPath = IndividualEnrollmentManager.getEnrollmentPath(registrationId);

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_022: [The get shall send a Http request with a Http verb `GET`.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_023: [The get shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_024: [The get shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        HttpResponse httpResponse =
                    contractApiHttp.request(
                            HttpMethod.GET,
                            enrollmentPath,
                            null,
                            "");

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_044: [The get shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
        byte[] body = httpResponse.getBody();
        if(body == null)
        {
            throw new ProvisioningServiceClientServiceException("Http response for get cannot contains a null body");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_025: [The get shall return an IndividualEnrollment object created from the body of the response for the Http request .] */
        return new IndividualEnrollment(new String(body, StandardCharsets.UTF_8));
    }

    AttestationMechanism getAttestationMechanism(String registrationId) throws ProvisioningServiceClientException
    {
        if(Tools.isNullOrEmpty(registrationId))
        {
            throw new IllegalArgumentException("registrationId cannot be null or empty.");
        }

        String enrollmentAttestationMechanismPath = IndividualEnrollmentManager.getEnrollmentAttestationMechanismPath(registrationId);
        String payload = "{}";
        HttpResponse httpResponse = contractApiHttp.request(HttpMethod.POST, enrollmentAttestationMechanismPath, null, payload);

        byte[] body = httpResponse.getBody();
        if (body == null)
        {
            throw new ProvisioningServiceClientServiceException("Unexpected empty body received from service");
        }

        return new AttestationMechanism(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * Delete individualEnrollment.
     *
     * @see ProvisioningServiceClient#deleteIndividualEnrollment(IndividualEnrollment)
     *
     * @param individualEnrollment is an {@link IndividualEnrollment} that describes the enrollment that will be deleted. It cannot be {@code null}.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the delete operation.
     */
    void delete(IndividualEnrollment individualEnrollment) throws ProvisioningServiceClientException
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_026: [The delete shall throw IllegalArgumentException if the provided individualEnrollment is null.] */
        if(individualEnrollment == null)
        {
            throw new IllegalArgumentException("individualEnrollment cannot be null.");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_027: [The delete shall send a Http request for the path `enrollments/[registrationId]`.] */
        String enrollmentPath = IndividualEnrollmentManager.getEnrollmentPath(individualEnrollment.getRegistrationId());

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_028: [If the individualEnrollment contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(individualEnrollment.getEtag()))
        {
            headerParameters.put(CONDITION_KEY, individualEnrollment.getEtag());
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_029: [The delete shall send a Http request with a Http verb `DELETE`.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_030: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_031: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        contractApiHttp.request(
                HttpMethod.DELETE,
                enrollmentPath,
                headerParameters,
                "");
    }

    /**
     * Delete individualEnrollment.
     *
     * @see ProvisioningServiceClient#deleteIndividualEnrollment(String)
     * @see ProvisioningServiceClient#deleteIndividualEnrollment(String, String)
     *
     * @param registrationId is a {@link String} with the registrationId of the enrollment to delete. It cannot be {@code null} or empty.
     * @param eTag is a {@link String} with the eTag of the enrollment to delete. It can be {@code null} or empty (ignored).
     * @throws IllegalArgumentException if the provided registrationId is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the delete operation.
     */
    void delete(String registrationId, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_032: [The delete shall throw IllegalArgumentException if the provided registrationId is null or empty.] */
        if(Tools.isNullOrEmpty(registrationId))
        {
            throw new IllegalArgumentException("registrationId cannot be null.");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_033: [The delete shall send a Http request for the path `enrollments/[registrationId]`.] */
        String enrollmentPath = IndividualEnrollmentManager.getEnrollmentPath(registrationId);

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(eTag))
        {
            headerParameters.put(CONDITION_KEY, eTag);
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_035: [The delete shall send a Http request with a Http verb `DELETE`.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_036: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_037: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        contractApiHttp.request(
                HttpMethod.DELETE,
                enrollmentPath,
                headerParameters,
                "");
    }

    /**
     * Create a new individualEnrollment query.
     *
     * @see ProvisioningServiceClient#createIndividualEnrollmentQuery(QuerySpecification)
     * @see ProvisioningServiceClient#createIndividualEnrollmentQuery(QuerySpecification, int)
     *
     * @param querySpecification is a {@code String} with the SQL query specification. It cannot be {@code null}.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return A {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     */
    Query createQuery(QuerySpecification querySpecification, int pageSize)
    {
        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_038: [The createQuery shall throw IllegalArgumentException if the provided querySpecification is null.] */
        if(querySpecification == null)
        {
            throw new IllegalArgumentException("querySpecification cannot be null.");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_039: [The createQuery shall throw IllegalArgumentException if the provided pageSize is negative.] */
        if(pageSize < 0)
        {
            throw new IllegalArgumentException("pageSize cannot be negative.");
        }

        /* SRS_INDIVIDUAL_ENROLLMENT_MANAGER_21_041: [The createQuery shall create and return a new instance of the Query iterator.] */
        return new Query(contractApiHttp, PATH_ENROLLMENTS, querySpecification, pageSize);
    }

    private static String getEnrollmentPath(String registrationId)
    {
        return PATH_ENROLLMENTS + PATH_SEPARATOR + registrationId;
    }

    private static String getEnrollmentAttestationMechanismPath(String registrationId)
    {
        return PATH_ENROLLMENTS + PATH_SEPARATOR + registrationId + PATH_SEPARATOR + ATTESTATION_MECHANISM;
    }
}
