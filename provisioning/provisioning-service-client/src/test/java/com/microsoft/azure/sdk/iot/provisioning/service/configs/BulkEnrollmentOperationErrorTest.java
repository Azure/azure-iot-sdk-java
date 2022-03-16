// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.BulkEnrollmentOperationError;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service Device Registration Operation Error
 * 100% methods, 100% lines covered
 */
public class BulkEnrollmentOperationErrorTest
{
    private static final String VALID_REGISTRATION_ID_1 = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final Integer VALID_ERROR_CODE_1 = 201;
    private static final String VALID_ERROR_STATUS_1 = "this is a valid error status for enrollment 1";
    private static final String VALID_ERROR_JSON_1 =
            "      {\n" +
                    "        \"registrationId\": \"" + VALID_REGISTRATION_ID_1 + "\",\n" +
                    "        \"errorCode\": " + VALID_ERROR_CODE_1 + ",\n" +
                    "        \"errorStatus\": \"" + VALID_ERROR_STATUS_1 + "\"\n" +
                    "      }\n";

    BulkEnrollmentOperationError makeBulkEnrollmentOperationError(String json)
    {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        return gson.fromJson(json, BulkEnrollmentOperationError.class);
    }

    /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_001: [The getRegistrationId shall return the stored registrationId.] */
    @Test
    public void getRegistrationIdReturnsRegistrationId()
    {
        // arrange
        BulkEnrollmentOperationError bulkEnrollmentOperationError = makeBulkEnrollmentOperationError(VALID_ERROR_JSON_1);

        // act
        String result = bulkEnrollmentOperationError.getRegistrationId();

        // assert
        assertEquals(VALID_REGISTRATION_ID_1, result);
    }

    /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_002: [The getErrorCode shall return the stored errorCode.] */
    @Test
    public void getErrorCodeReturnsErrorCode()
    {
        // arrange
        BulkEnrollmentOperationError bulkEnrollmentOperationError = makeBulkEnrollmentOperationError(VALID_ERROR_JSON_1);

        // act
        Integer result = bulkEnrollmentOperationError.getErrorCode();

        // assert
        assertEquals(VALID_ERROR_CODE_1, result);
    }

    /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_003: [The getErrorStatus shall return the stored errorStatus.] */
    @Test
    public void getErrorStatusReturnsErrorStatus()
    {
        // arrange
        BulkEnrollmentOperationError bulkEnrollmentOperationError = makeBulkEnrollmentOperationError(VALID_ERROR_JSON_1);

        // act
        String result = bulkEnrollmentOperationError.getErrorStatus();

        // assert
        assertEquals(VALID_ERROR_STATUS_1, result);
    }

    /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_005: [The validateError shall throw IllegalArgumentException if the errorCode is null.] */
    @Test (expected = IllegalArgumentException.class)
    public void validateErrorThrowsOnNullErrorCode()
    {
        // arrange
        final String json =
                "      {\n" +
                "        \"registrationId\": \"" + VALID_REGISTRATION_ID_1 + "\",\n" +
                "        \"errorStatus\": \"" + VALID_ERROR_STATUS_1 + "\"\n" +
                "      }\n";
        BulkEnrollmentOperationError bulkEnrollmentOperationError = makeBulkEnrollmentOperationError(json);

        // act
        Deencapsulation.invoke(bulkEnrollmentOperationError, "validateError");

        // assert
    }

    /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_006: [The validateError shall do nothing if all parameters in the class are correct.] */
    @Test
    public void validateErrorSucceeded()
    {
        // arrange
        BulkEnrollmentOperationError bulkEnrollmentOperationError = makeBulkEnrollmentOperationError(VALID_ERROR_JSON_1);

        // act
        Deencapsulation.invoke(bulkEnrollmentOperationError, "validateError");

        // assert
    }

    /* SRS_DEVICE_REGISTRATION_OPERATION_ERROR_21_007: [The BulkEnrollmentOperationResult shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceeded()
    {
        // arrange

        // act
        BulkEnrollmentOperationError bulkEnrollmentOperationError =  Deencapsulation.newInstance(BulkEnrollmentOperationError.class);

        // assert
        assertNotNull(bulkEnrollmentOperationError);
    }
}
