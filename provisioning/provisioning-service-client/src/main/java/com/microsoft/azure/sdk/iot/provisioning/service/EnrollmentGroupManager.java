// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.AttestationMechanism;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.EnrollmentGroup;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.QuerySpecification;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientServiceException;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ProvisioningServiceClientTransportException;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Enrollment Group Manager
 *
 * <p> This is the inner class that implements the Enrollment Group APIs.
 * <p> For the exposed API, please see {@link ProvisioningServiceClient}.
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 */
class EnrollmentGroupManager
{
    private final ContractApiHttp contractApiHttp;
    private static final String CONDITION_KEY = "If-Match";
    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_ENROLLMENT_GROUPS = "enrollmentGroups";
    private static final String ATTESTATION_MECHANISM = "attestationmechanism";

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param contractApiHttp is the device registration client for one of the protocols
     * @throws IllegalArgumentException if the {@link ContractApiHttp} is {@code null}
     */
    private EnrollmentGroupManager(ContractApiHttp contractApiHttp)
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_001: [The constructor shall throw IllegalArgumentException if the provided ContractApiHttp is null.] */
        if(contractApiHttp == null)
        {
            throw new IllegalArgumentException("ContractApiHttp cannot be null");
        }
        this.contractApiHttp = contractApiHttp;
    }

    /**
     * Create a new instance of the EnrollmentGroupManager using the provided connection
     *    string and https as the transport protocol.
     *
     * @param contractApiHttp is the class that cares the Http connection.
     * @return The {@code EnrollmentGroupManager} with the new instance of this class.
     * @throws IllegalArgumentException if the {@link ContractApiHttp} is {@code null}.
     */
    static EnrollmentGroupManager createFromContractApiHttp(ContractApiHttp contractApiHttp)
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_004: [The factory shall create a new instance of this.] */
        return new EnrollmentGroupManager(contractApiHttp);
    }

    /**
     * Create or update an enrollmentGroup record.
     *
     * @param enrollmentGroup is an {@link EnrollmentGroup} that describes the enrollmentGroup that will be created or
     *                        updated. It cannot be {@code null}.
     * @return a {@link EnrollmentGroup} with the result of the creation or update request.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to create or update the enrollmentGroup.
     */
    EnrollmentGroup createOrUpdate(EnrollmentGroup enrollmentGroup) throws ProvisioningServiceClientException
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_005: [The createOrUpdate shall throw IllegalArgumentException if the provided enrollmentGroup is null.] */
        if(enrollmentGroup == null)
        {
            throw new IllegalArgumentException("enrollmentGroup cannot be null.");
        }
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_006: [The createOrUpdate shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
        String id = enrollmentGroup.getEnrollmentGroupId();
        String enrollmentGroupPath = EnrollmentGroupManager.getEnrollmentGroupPath(id);

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_007: [The createOrUpdate shall send a Http request with a body with the enrollmentGroup content in JSON format.] */
        String enrollmentGroupPayload = enrollmentGroup.toJson();

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_045: [If the enrollmentGroup contains eTag, the createOrUpdate shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(enrollmentGroup.getEtag()))
        {
            headerParameters.put(CONDITION_KEY, enrollmentGroup.getEtag());
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_008: [The createOrUpdate shall send a Http request with a Http verb `PUT`.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_009: [The createOrUpdate shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_010: [The createOrUpdate shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        HttpResponse httpResponse =
                    contractApiHttp.request(
                            HttpMethod.PUT,
                            enrollmentGroupPath,
                            headerParameters,
                            enrollmentGroupPayload);

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_042: [The createOrUpdate shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
        byte[] body = httpResponse.getBody();
        if(body == null)
        {
            throw new ProvisioningServiceClientServiceException("Http response for createOrUpdate cannot contains a null body");
        }
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_011: [The createOrUpdate shall return an EnrollmentGroup object created from the body of the response for the Http request .] */
        return new EnrollmentGroup(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * Get enrollmentGroup information.
     *
     * @see ProvisioningServiceClient#getEnrollmentGroup(String)
     *
     * @param enrollmentGroupId the {@code String} that identifies the enrollmentGroup. It cannot be {@code null} or empty.
     * @return An {@link EnrollmentGroup} with the enrollmentGroup information.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the get operation.
     */
    EnrollmentGroup get(String enrollmentGroupId) throws ProvisioningServiceClientException
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_020: [The get shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
        if(Tools.isNullOrEmpty(enrollmentGroupId))
        {
            throw new IllegalArgumentException("enrollmentGroupId cannot be null or empty.");
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_021: [The get shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
        String enrollmentGroupPath = EnrollmentGroupManager.getEnrollmentGroupPath(enrollmentGroupId);

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_022: [The get shall send a Http request with a Http verb `GET`.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_023: [The get shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_024: [The get shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        HttpResponse httpResponse =
                    contractApiHttp.request(
                            HttpMethod.GET,
                            enrollmentGroupPath,
                            null,
                            "");

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_043: [The get shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
        byte[] body = httpResponse.getBody();
        if(body == null)
        {
            throw new ProvisioningServiceClientServiceException("Http response for get cannot contains a null body");
        }
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_025: [The get shall return an EnrollmentGroup object created from the body of the response for the Http request .] */
        return new EnrollmentGroup(new String(body, StandardCharsets.UTF_8));
    }

    AttestationMechanism getAttestationMechanism(String enrollmentGroupId) throws ProvisioningServiceClientException
    {
        if(Tools.isNullOrEmpty(enrollmentGroupId))
        {
            throw new IllegalArgumentException("enrollmentGroupId cannot be null or empty.");
        }

        String enrollmentAttestationMechanismPath = getEnrollmentGroupAttestationMechanismPath(enrollmentGroupId);

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
     * Delete enrollmentGroup.
     *
     * @see ProvisioningServiceClient#deleteEnrollmentGroup(EnrollmentGroup)
     *
     * @param enrollmentGroup is an {@link EnrollmentGroup} that describes the enrollmentGroup that will be deleted. It cannot be {@code null}.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the delete operation.
     */
    void delete(EnrollmentGroup enrollmentGroup) throws ProvisioningServiceClientException
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_026: [The delete shall throw IllegalArgumentException if the provided enrollmentGroup is null.] */
        if(enrollmentGroup == null)
        {
            throw new IllegalArgumentException("enrollmentGroup cannot be null.");
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_027: [The delete shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
        String enrollmentGroupPath = EnrollmentGroupManager.getEnrollmentGroupPath(enrollmentGroup.getEnrollmentGroupId());

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_028: [If the enrollmentGroup contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(enrollmentGroup.getEtag()))
        {
            headerParameters.put(CONDITION_KEY, enrollmentGroup.getEtag());
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_029: [The delete shall send a Http request with a Http verb `DELETE`.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_030: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_031: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        contractApiHttp.request(
                HttpMethod.DELETE,
                enrollmentGroupPath,
                headerParameters,
                "");
    }

    /**
     * Delete enrollmentGroup.
     *
     * @see ProvisioningServiceClient#deleteEnrollmentGroup(String)
     * @see ProvisioningServiceClient#deleteEnrollmentGroup(String, String)
     *
     * @param enrollmentGroupId is a {@code String} with the enrollmentGroupId of the enrollmentGroup to delete. It cannot be {@code null} or empty.
     * @param eTag is a {@code String} with the eTag of the enrollmentGroup to delete. It can be {@code null} or empty (ignored).
     * @throws IllegalArgumentException if the provided enrollmentGroupId is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the delete operation.
     */
    void delete(String enrollmentGroupId, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_032: [The delete shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
        if(Tools.isNullOrEmpty(enrollmentGroupId))
        {
            throw new IllegalArgumentException("enrollmentGroupId cannot be null.");
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_033: [The delete shall send a Http request for the path `enrollmentGroups/[enrollmentGroupId]`.] */
        String enrollmentGroupPath = EnrollmentGroupManager.getEnrollmentGroupPath(enrollmentGroupId);

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_034: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(eTag))
        {
            headerParameters.put(CONDITION_KEY, eTag);
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_035: [The delete shall send a Http request with a Http verb `DELETE`.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_036: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_037: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        contractApiHttp.request(
                HttpMethod.DELETE,
                enrollmentGroupPath,
                headerParameters,
                "");
    }

    /**
     * Create a new enrollmentGroup query.
     *
     * @see ProvisioningServiceClient#createEnrollmentGroupQuery(QuerySpecification)
     * @see ProvisioningServiceClient#createEnrollmentGroupQuery(QuerySpecification, int)
     *
     * @param querySpecification is a {@code String} with the SQL query specification. It cannot be {@code null}.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return A {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     */
    Query createQuery(QuerySpecification querySpecification, int pageSize)
    {
        /* SRS_ENROLLMENT_GROUP_MANAGER_21_038: [The createQuery shall throw IllegalArgumentException if the provided querySpecification is null.] */
        if(querySpecification == null)
        {
            throw new IllegalArgumentException("querySpecification cannot be null.");
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_039: [The createQuery shall throw IllegalArgumentException if the provided pageSize is negative.] */
        if(pageSize < 0)
        {
            throw new IllegalArgumentException("pageSize cannot be negative.");
        }

        /* SRS_ENROLLMENT_GROUP_MANAGER_21_041: [The createQuery shall create and return a new instance of the Query iterator.] */
        return new Query(contractApiHttp, PATH_ENROLLMENT_GROUPS, querySpecification, pageSize);
    }

    private static String getEnrollmentGroupPath(String enrollmentGroupId)
    {
        return PATH_ENROLLMENT_GROUPS + PATH_SEPARATOR + enrollmentGroupId;
    }

    private static String getEnrollmentGroupAttestationMechanismPath(String enrollmentGroupId)
    {
        return PATH_ENROLLMENT_GROUPS + PATH_SEPARATOR + enrollmentGroupId + PATH_SEPARATOR + ATTESTATION_MECHANISM;
    }
}
