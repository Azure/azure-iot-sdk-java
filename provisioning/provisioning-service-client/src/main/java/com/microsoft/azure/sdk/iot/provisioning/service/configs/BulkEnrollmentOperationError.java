// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;

import java.util.Collection;

/**
 * Representation of a single Device Provisioning Service device registration operation error.
 *
 * <p> This error is returned as a result of the
 *     {@link ProvisioningServiceClient#runBulkEnrollmentOperation(BulkOperationMode, Collection)},
 *     in the {@link BulkEnrollmentOperationResult}.
 *
 * <p> The following JSON is an example of a single error operation from a Bulk operation
 * <pre>
 * {@code
 * {
 *      "registrationId":"validRegistrationId1",
 *      "errorCode":200,
 *      "errorStatus":"Succeeded"
 * }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class BulkEnrollmentOperationError
{
    // the registration identifier
    private static final String REGISTRATION_ID_TAG = "registrationId";
    @Expose
    @SerializedName(REGISTRATION_ID_TAG)
    private String registrationId;

    // the error code
    private static final String ERROR_CODE_TAG = "errorCode";
    @Expose
    @SerializedName(ERROR_CODE_TAG)
    private Integer errorCode;

    // the error status
    private static final String ERROR_STATUS_TAG = "errorStatus";
    @Expose
    @SerializedName(ERROR_STATUS_TAG)
    private String errorStatus;

    /**
     * Getter for the error registrationId.
     *
     * @return The {@code String} with the registrationId content. It cannot be {@code null}.
     */
    public String getRegistrationId()
    {
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_001: [The getRegistrationId shall return the stored registrationId.] */
        return this.registrationId;
    }

    /**
     * Getter for the error code.
     *
     * @return The {@code Integer} with the errorCode content. It cannot be {@code null}.
     */
    public Integer getErrorCode()
    {
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_002: [The getErrorCode shall return the stored errorCode.] */
        return this.errorCode;
    }

    /**
     * Getter for the error status.
     *
     * @return The {@code String} with the errorStatus content. It can be {@code null}.
     */
    public String getErrorStatus()
    {
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_003: [The getErrorStatus shall return the stored errorStatus.] */
        return this.errorStatus;
    }

    /**
     * Validate the parameters stored in the class as result of the deserialization.
     *
     * @throws IllegalArgumentException If one of the parameters in the class is not valid.
     */
    void validateError()
    {
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_004: [The validateError shall throw IllegalArgumentException if the registrationId is null, empty or not a valid id.] */
        ParserUtility.validateId(this.registrationId);
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_005: [The validateError shall throw IllegalArgumentException if the errorCode is null.] */
        ParserUtility.validateObject(this.errorCode);
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_006: [The validateError shall do nothing if all parameters in the class are correct.] */
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    BulkEnrollmentOperationError()
    {
        /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_007: [The BulkEnrollmentOperationResult shall provide an empty constructor to make GSON happy.] */
    }
}
