// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service;

import com.microsoft.azure.sdk.iot.deps.transport.http.HttpMethod;
import com.microsoft.azure.sdk.iot.deps.transport.http.HttpResponse;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.DeviceRegistrationState;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.QuerySpecification;
import com.microsoft.azure.sdk.iot.provisioning.service.contract.ContractApiHttp;
import com.microsoft.azure.sdk.iot.provisioning.service.exceptions.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Registration Status Manager
 *
 * <p> This is the inner class that implements the Registration Status APIs.
 * <p> For the exposed API, please see {@link ProvisioningServiceClient}.
 *
 * @see <a href="https://docs.microsoft.com/en-us/azure/iot-dps/">Azure IoT Hub Device Provisioning Service</a>
 */
public class RegistrationStatusManager
{
    private final ContractApiHttp contractApiHttp;
    private static final String CONDITION_KEY = "If-Match";
    private static final String PATH_SEPARATOR = "/";
    private static final String PATH_REGISTRATIONS = "registrations";

    /**
     * PRIVATE CONSTRUCTOR
     *
     * @param contractApiHttp is the device registration client for one of the protocols
     * @throws IllegalArgumentException if the {@link ContractApiHttp} is {@code null}
     */
    private RegistrationStatusManager(ContractApiHttp contractApiHttp)
    {
        /* SRS_REGISTRATION_STATUS_MANAGER_21_001: [The constructor shall throw IllegalArgumentException if the provided ContractApiHttp is null.] */
        if(contractApiHttp == null)
        {
            throw new IllegalArgumentException("ContractApiHttp cannot be null");
        }
        this.contractApiHttp = contractApiHttp;
    }

    /**
     * Create a new instance of the RegistrationStatusManager using the provided connection
     *    string and https as the transport protocol.
     *
     * @param contractApiHttp is the class that cares the Http connection.
     * @return The {@code RegistrationStatusManager} with the new instance of this class.
     * @throws IllegalArgumentException if the {@link ContractApiHttp} is {@code null}.
     */
    public static RegistrationStatusManager createFromContractApiHttp(ContractApiHttp contractApiHttp)
    {
        /* SRS_REGISTRATION_STATUS_MANAGER_21_004: [The factory shall create a new instance of this.] */
        return new RegistrationStatusManager(contractApiHttp);
    }

    /**
     * Get device registration status information.
     *
     * @see ProvisioningServiceClient#getDeviceRegistrationState(String)
     *
     * @param id the {@code String} that identifies the registration status. It cannot be {@code null} or empty.
     * @return An {@link DeviceRegistrationState} with the registration status information.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    public DeviceRegistrationState get(String id) throws ProvisioningServiceClientException
    {
        /* SRS_REGISTRATION_STATUS_MANAGER_21_005: [The get shall throw IllegalArgumentException if the provided id is null or empty.] */
        if(Tools.isNullOrEmpty(id))
        {
            throw new IllegalArgumentException("Id cannot be null or empty.");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_006: [The get shall send a Http request for the path `registrations/[id]`.] */
        String enrollmentPath = RegistrationStatusManager.getDeviceRegistrationStatePath(id);

        /* SRS_REGISTRATION_STATUS_MANAGER_21_007: [The get shall send a Http request with a Http verb `GET`.] */
        /* SRS_REGISTRATION_STATUS_MANAGER_21_008: [The get shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_REGISTRATION_STATUS_MANAGER_21_009: [The get shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        HttpResponse httpResponse =
                contractApiHttp.request(
                        HttpMethod.GET,
                        enrollmentPath,
                        null,
                        "");

        /* SRS_REGISTRATION_STATUS_MANAGER_21_028: [The get shall throw ProvisioningServiceClientServiceException if the heepResponse contains a null body.] */
        byte[] body = httpResponse.getBody();
        if(body == null)
        {
            throw new ProvisioningServiceClientServiceException("Http response for get cannot contains a null body");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_010: [The get shall return a DeviceRegistrationState object created from the body of the response for the Http request .] */
        return new DeviceRegistrationState(new String(body, StandardCharsets.UTF_8));
    }

    /**
     * Delete registration status.
     *
     * @see ProvisioningServiceClient#deleteDeviceRegistrationStatus(DeviceRegistrationState)
     *
     * @param DeviceRegistrationState is a {@link DeviceRegistrationState} that describes the registration status
     *                                 that will be deleted. It cannot be {@code null}.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the delete operation.
     */
    @SuppressWarnings("deprecation")
    public void delete(DeviceRegistrationState DeviceRegistrationState) throws ProvisioningServiceClientException
    {
        /* SRS_REGISTRATION_STATUS_MANAGER_21_011: [The delete shall throw IllegalArgumentException if the provided DeviceRegistrationState is null.] */
        if(DeviceRegistrationState == null)
        {
            throw new IllegalArgumentException("DeviceRegistrationState cannot be null.");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_012: [The delete shall send a Http request for the path `registrations/[id]`.] */
        String enrollmentPath = RegistrationStatusManager.getDeviceRegistrationStatePath(DeviceRegistrationState.getRegistrationId());

        /* SRS_REGISTRATION_STATUS_MANAGER_21_013: [If the DeviceRegistrationState contains eTag, the delete shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(DeviceRegistrationState.getEtag()))
        {
            headerParameters.put(CONDITION_KEY, DeviceRegistrationState.getEtag());
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_014: [The delete shall send a Http request with a Http verb `DELETE`.] */
        /* SRS_REGISTRATION_STATUS_MANAGER_21_015: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_REGISTRATION_STATUS_MANAGER_21_016: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        contractApiHttp.request(
                HttpMethod.DELETE,
                enrollmentPath,
                headerParameters,
                "");
    }

    /**
     * Delete registration status.
     *
     * @see ProvisioningServiceClient#deleteDeviceRegistrationStatus(String)
     * @see ProvisioningServiceClient#deleteDeviceRegistrationStatus(String, String)
     *
     * @param id is a {@link String} with the identification of the registration status to delete. It cannot be {@code null} or empty.
     * @param eTag is a {@link String} with the eTag of the enrollment to delete. It can be {@code null} or empty (ignored).
     * @throws IllegalArgumentException if the provided id is not correct.
     * @throws ProvisioningServiceClientTransportException if the SDK failed to send the request to the Device Provisioning Service.
     * @throws ProvisioningServiceClientException if the Device Provisioning Service was not able to execute the bulk operation.
     */
    @SuppressWarnings("deprecation")
    public void delete(String id, String eTag) throws ProvisioningServiceClientException
    {
        /* SRS_REGISTRATION_STATUS_MANAGER_21_017: [The delete shall throw IllegalArgumentException if the provided id is null or empty.] */
        if(Tools.isNullOrEmpty(id))
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_018: [The delete shall send a Http request for the path `registrations/[id]`.] */
        String enrollmentPath = RegistrationStatusManager.getDeviceRegistrationStatePath(id);

        /* SRS_REGISTRATION_STATUS_MANAGER_21_019: [If the eTag is not null or empty, the delete shall send a Http request with `If-Match` the eTag in the header.] */
        Map<String, String> headerParameters = new HashMap<>();
        if(!Tools.isNullOrEmpty(eTag))
        {
            headerParameters.put(CONDITION_KEY, eTag);
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_020: [The delete shall send a Http request with a Http verb `DELETE`.] */
        /* SRS_REGISTRATION_STATUS_MANAGER_21_021: [The delete shall throw ProvisioningServiceClientTransportException if the request failed. Threw by the callee.] */
        /* SRS_REGISTRATION_STATUS_MANAGER_21_022: [The delete shall throw ProvisioningServiceClientException if the Device Provisioning Service could not successfully execute the request. Threw by the callee.] */
        contractApiHttp.request(
                HttpMethod.DELETE,
                enrollmentPath,
                headerParameters,
                "");
    }

    /**
     * Create a new registration status query for enrollmentGroup.
     *
     * @see ProvisioningServiceClient#createEnrollmentGroupRegistrationStatusQuery(QuerySpecification, String)
     * @see ProvisioningServiceClient#createEnrollmentGroupRegistrationStatusQuery(QuerySpecification, String, int)
     *
     * @param querySpecification is a {@code String} with the SQL query specification. It cannot be {@code null}.
     * @param enrollmentGroupId is a {@code String} with the enrollmentGroupId of the enrollmentGroup to delete. It cannot be {@code null} or empty.
     * @param pageSize the {@code int} with the maximum number of items per iteration. It can be 0 for default, but not negative.
     * @return A {@link Query} iterator.
     * @throws IllegalArgumentException if the provided parameter is not correct.
     */
    @SuppressWarnings("deprecation")
    public Query createEnrollmentGroupQuery(QuerySpecification querySpecification, String enrollmentGroupId, int pageSize)
    {
        /* SRS_REGISTRATION_STATUS_MANAGER_21_023: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided querySpecification is null.] */
        if(querySpecification == null)
        {
            throw new IllegalArgumentException("querySpecification cannot be null.");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_024: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided enrollmentGroupId is null or empty.] */
        if(Tools.isNullOrEmpty(enrollmentGroupId))
        {
            throw new IllegalArgumentException("enrollmentGroupId cannot be null or empty.");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_025: [The createEnrollmentGroupQuery shall throw IllegalArgumentException if the provided pageSize is negative.] */
        if(pageSize < 0)
        {
            throw new IllegalArgumentException("pageSize cannot be negative.");
        }

        /* SRS_REGISTRATION_STATUS_MANAGER_21_026: [The createEnrollmentGroupQuery shall create Query iterator with a Http path `registrations/[id]`.] */
        String targetPath = RegistrationStatusManager.getDeviceRegistrationStatePath(enrollmentGroupId);

        /* SRS_REGISTRATION_STATUS_MANAGER_21_027: [The createEnrollmentGroupQuery shall create and return a new instance of the Query iterator.] */
        return new Query(contractApiHttp, targetPath, querySpecification, pageSize);
    }

    private static String getDeviceRegistrationStatePath(String id)
    {
        return PATH_REGISTRATIONS + PATH_SEPARATOR + id;
    }
}
