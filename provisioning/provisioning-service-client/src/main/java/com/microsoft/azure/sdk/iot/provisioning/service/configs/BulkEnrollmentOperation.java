// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.provisioning.service.ProvisioningServiceClient;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Representation of a single Device Provisioning Service bulk operation with a JSON serializer.
 * 
 * <p> It is an internal class that creates a JSON for the bulk operations
 *     over the IndividualEnrollment. To use bulk operations, please use
 *     the external API {@link ProvisioningServiceClient#runBulkEnrollmentOperation(BulkOperationMode, Collection)}.
 *
 * <p> The following JSON is an example of the result of this serializer.
 * <pre>
 *{@code
 *{
 *    "mode":"update",
 *    "enrollments":
 *    [
 *        {
 *            "registrationId":"validRegistrationId-1",
 *            "deviceId":"ContosoDevice-1",
 *            "attestation":{
 *                "type":"tpm",
 *                "tpm":{
 *                    "endorsementKey":"validEndorsementKey"
 *                }
 *            },
 *            "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *            "provisioningStatus":"enabled"
 *        },
 *        {
 *            "registrationId":"validRegistrationId-2",
 *            "deviceId":"ContosoDevice-2",
 *            "attestation":{
 *                "type":"tpm",
 *               "tpm":{
 *                    "endorsementKey":"validEndorsementKey"
 *                }
 *            },
 *            "iotHubHostName":"ContosoIoTHub.azure-devices.net",
 *            "provisioningStatus":"enabled"
 *        }
 *    ]
 *}
 *}
 * </pre>
 * 
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
public final class BulkEnrollmentOperation
{
    // the is bulk operation mode
    private static final String BULK_OPERATION_MODE_TAG = "mode";

    // the collection of enrollments for bulk operation
    private static final String ENROLLMENTS_TAG = "enrollments";

    /**
     * Serializer
     *
     * <p> Creates a {@code String}, whose content represents the mode and the collection of
     *     individualEnrollments in a JSON format.
     *
     * @param mode the {@link BulkOperationMode} that defines the single operation to do over the individualEnrollments.
     * @param individualEnrollments the collection of {@link IndividualEnrollment} that contains the description of each individualEnrollment.
     * @return The {@code String} with the content of this class.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     */
    public static String toJson(BulkOperationMode mode, Collection<IndividualEnrollment> individualEnrollments)
    {
        /* SRS_BULK_OPERATION_21_001: [The toJson shall return a String with the mode and the collection of individualEnrollments using a JSON format.] */
        /* SRS_BULK_OPERATION_21_002: [The toJson shall throw IllegalArgumentException if the provided mode is null or the collection of individualEnrollments is null or empty.] */
        return BulkEnrollmentOperation.toJsonElement(mode, individualEnrollments).toString();
    }

    /**
     * Convert the class in a pretty print string.
     *
     * <p> Creates a {@code String}, whose content represents the mode and the collection of
     *     individualEnrollments in a pretty print JSON format.
     *
     * @param mode the {@link BulkOperationMode} that defines the single operation to do over the individualEnrollments.
     * @param individualEnrollments the collection of {@link IndividualEnrollment} that contains the description of each individualEnrollment.
     * @return The {@code String} with the content of this class.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     */
    public static String toString(BulkOperationMode mode, Collection<IndividualEnrollment> individualEnrollments)
    {
        /* SRS_BULK_OPERATION_21_003: [The toString shall return a String with the mode and the collection of individualEnrollments using a pretty print JSON format.] */
        /* SRS_BULK_OPERATION_21_004: [The toString shall throw IllegalArgumentException if the provided mode is null or the collection of individualEnrollments is null or empty.] */
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return gson.toJson(BulkEnrollmentOperation.toJsonElement(mode, individualEnrollments));
    }

    /**
     * Serializer
     *
     * <p> Creates a {@code JsonElement}, whose content represents the mode and the collection of
     *     individualEnrollments in a JSON format.
     *
     * <p> This is useful if the caller will integrate this JSON with JSON from other classes
     *     to generate a consolidated JSON.
     *
     * @param mode the {@link BulkOperationMode} that defines the single operation to do over the individualEnrollments.
     * @param individualEnrollments the collection of {@link IndividualEnrollment} that contains the description of each individualEnrollment.
     * @return The {@code JsonElement} with the content of this class.
     * @throws IllegalArgumentException if one of the parameters is invalid.
     */
    public static JsonElement toJsonElement(BulkOperationMode mode, Collection<IndividualEnrollment> individualEnrollments)
    {
        /* SRS_BULK_OPERATION_21_005: [The toJsonElement shall throw IllegalArgumentException if the provided mode is null or the collection of individualEnrollments is null or empty.] */
        if (mode == null)
        {
            throw new IllegalArgumentException("mode cannot be null");
        }
        if ((individualEnrollments == null) || individualEnrollments.isEmpty())
        {
            throw new IllegalArgumentException("individualEnrollments cannot be null or empty");
        }

        /* SRS_BULK_OPERATION_21_006: [The toJsonElement shall return a JsonElement with the mode and the collection of individualEnrollments using a JSON format.] */
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        JsonObject twinJson = new JsonObject();

        twinJson.add(BULK_OPERATION_MODE_TAG, gson.toJsonTree(mode));

        List<JsonElement> listOfJsonElements = new LinkedList<>();
        for (IndividualEnrollment individualEnrollment : individualEnrollments)
        {
            listOfJsonElements.add(individualEnrollment.toJsonElement());
        }

        JsonElement jsonElementOfEnrollmentList = gson.toJsonTree(listOfJsonElements);
        twinJson.add(ENROLLMENTS_TAG, jsonElementOfEnrollmentList);

        return twinJson;
    }
}
