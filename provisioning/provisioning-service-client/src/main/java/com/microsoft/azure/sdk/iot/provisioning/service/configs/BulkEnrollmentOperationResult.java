// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Representation of a single Device Provisioning Service bulk operation result with a JSON deserializer.
 *
 * <p> This result is returned as a result of the
 *     {@link ProvisioningServiceClient#runBulkEnrollmentOperation(BulkOperationMode, Collection)}.
 *
 * <p> The provisioning service provides general bulk result in the isSuccessful, and a individual error result
 *     for each enrolment in the bulk.
 *
 * <p>  The following JSON is an example of the result from a bulk operation.
 * <pre>
 * {@code
 * {
 *     "isSuccessful":true,
 *     "errors": [
 *         {
 *             "registrationId":"validRegistrationId1",
 *             "errorCode":200,
 *             "errorStatus":"Succeeded"
 *         },
 *         {
 *             "registrationId":"validRegistrationId2",
 *             "errorCode":200,
 *             "errorStatus":"Succeeded"
 *         }
 *     ]
 * }
 * }
 * </pre>
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public class BulkEnrollmentOperationResult
{
    // the is bulk operation success
    private static final String IS_SUCCESSFUL_TAG = "isSuccessful";
    @Expose
    @SerializedName(IS_SUCCESSFUL_TAG)
    private Boolean isSuccessful;

    // the list of errors
    private static final String ERRORS_TAG = "errors";
    @Expose
    @SerializedName(ERRORS_TAG)
    private BulkEnrollmentOperationError[] errors;

    /**
     * CONSTRUCTOR
     *
     * <p> This constructor creates an instance of the enrollment filling
     *     the class with the information provided in the JSON.
     *
     * @param json the {@code String} with the JSON received from the provisioning service.
     * @throws IllegalArgumentException If the provided JSON is null, empty, or invalid.
     */
    public BulkEnrollmentOperationResult(String json)
    {
        /* SRS_BULK_OPERATION_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
        if (Tools.isNullOrEmpty(json))
        {
            throw new IllegalArgumentException("JSON with result is null or empty");
        }

        /* SRS_BULK_OPERATION_RESULT_21_002: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
        /* SRS_BULK_OPERATION_RESULT_21_003: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        BulkEnrollmentOperationResult result = gson.fromJson(json, BulkEnrollmentOperationResult.class);

        /* SRS_BULK_OPERATION_RESULT_21_004: [The constructor shall throw IllegalArgumentException if the JSON do not contains isSuccessful.] */
        ParserUtility.validateObject(result.isSuccessful);

        /* SRS_BULK_OPERATION_RESULT_21_005: [The constructor shall throw IllegalArgumentException if the JSON contains invalid error.] */
        for (BulkEnrollmentOperationError error:result.errors)
        {
            error.validateError();
        }

        /* SRS_BULK_OPERATION_RESULT_21_006: [The constructor shall store the provided isSuccessful.] */
        this.isSuccessful = result.isSuccessful;
        /* SRS_BULK_OPERATION_RESULT_21_007: [The constructor shall store the provided errors.] */
        this.errors = result.errors;
    }

    /**
     * Getter for the Bulk Operation successful.
     *
     * @return The {@code Boolean} with the isSuccessful content. It cannot be {@code null}.
     */
    public Boolean getSuccessful()
    {
        /* SRS_BULK_OPERATION_RESULT_21_008: [The getSuccessful shall return the stored isSuccessful.] */
        return this.isSuccessful;
    }

    /**
     * Getter for the bulk of errors.
     *
     * @return The {@code BulkEnrollmentOperationError} with the errors content. It can be {@code null}.
     */
    public List<BulkEnrollmentOperationError> getErrors()
    {
        /* SRS_BULK_OPERATION_RESULT_21_009: [The getErrors shall return the stored errors as List of BulkEnrollmentOperationError.] */
        return Arrays.asList(this.errors);
    }

    /**
     * Creates a pretty print JSON with the content of this class and subclasses.
     *
     * @return The {@code String} with the pretty print JSON.
     */
    @Override
    public String toString()
    {
        /* SRS_BULK_OPERATION_RESULT_21_010: [The toString shall return a String with the information into this class in a pretty print JSON.] */
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /**
     * Empty constructor
     *
     * <p>
     *     Used only by the tools that will deserialize this class.
     * </p>
     */
    @SuppressWarnings("unused")
    protected BulkEnrollmentOperationResult()
    {
        /* SRS_BULK_OPERATION_RESULT_21_011: [The BulkEnrollmentOperationResult shall provide an empty constructor to make GSON happy.] */
    }
}
