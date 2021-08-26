// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonSyntaxException;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service bulk operation result deserializer
 * 100% methods, 100% lines covered
 */
public class BulkEnrollmentOperationResultTest
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

    private static final String VALID_REGISTRATION_ID_2 = "818B129D-20C4-4E91-8EEA-955776DB4340";
    private static final Integer VALID_ERROR_CODE_2 = 400;
    private static final String VALID_ERROR_STATUS_2 = "this is a valid error status for enrollment 2";
    private static final String VALID_ERROR_JSON_2 =
            "      {\n" +
            "        \"registrationId\": \"" + VALID_REGISTRATION_ID_2 + "\",\n" +
            "        \"errorCode\": " + VALID_ERROR_CODE_2 + ",\n" +
            "        \"errorStatus\": \"" + VALID_ERROR_STATUS_2 + "\"\n" +
            "      }\n";
    private static final String VALID_JSON =
            "{\n" +
            "  \"isSuccessful\":true,\n" +
            "  \"errors\": \n" +
            "    [\n" +
            VALID_ERROR_JSON_1 + ",\n" +
            VALID_ERROR_JSON_2 + "\n" +
            "    ]\n" +
            "}";

    /* SRS_BULK_OPERATION_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullJson()
    {
        // arrange

        // act
        new BulkEnrollmentOperationResult(null);

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyJson()
    {
        // arrange

        // act
        new BulkEnrollmentOperationResult("");

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_002: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorThrowsOnInvalidJson()
    {
        // arrange

        // act
        new BulkEnrollmentOperationResult("{\"isSuccessful\": \"true\",}");

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_004: [The constructor shall throw IllegalArgumentException if the JSON contains invalid error.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnMissingRegistrationId()
    {
        // arrange
        final String missingSuccessful =
                "{\n" +
                        "  \"errors\": \n" +
                        "    [\n" +
                        VALID_ERROR_JSON_1 + ",\n" +
                        VALID_ERROR_JSON_2 + "\n" +
                        "    ]\n" +
                        "}";

        // act
        new BulkEnrollmentOperationResult(missingSuccessful);

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_005: [The constructor shall throw IllegalArgumentException if the JSON contains invalid error.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnErrorsWithFail(
            @Mocked final BulkEnrollmentOperationError mockedBulkEnrollmentOperationError)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedBulkEnrollmentOperationError, "validateError");
                result = new IllegalArgumentException();
            }
        };

        // act
        new BulkEnrollmentOperationResult(VALID_JSON);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedBulkEnrollmentOperationError, "validateError");
                times = 1;
            }
        };
    }

    /* SRS_BULK_OPERATION_RESULT_21_003: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
    /* SRS_BULK_OPERATION_RESULT_21_006: [The constructor shall store the provided isSuccessful.] */
    @Test
    public void constructorStoreSuccessful()
    {
        // arrange

        // act
        BulkEnrollmentOperationResult bulkEnrollmentOperationResult = new BulkEnrollmentOperationResult(VALID_JSON);

        // assert
        assertTrue(Deencapsulation.getField(bulkEnrollmentOperationResult, "isSuccessful"));
    }

    /* SRS_BULK_OPERATION_RESULT_21_003: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
    /* SRS_BULK_OPERATION_RESULT_21_007: [The constructor shall store the provided errors.] */
    @Test
    public void constructorStoreErrors()
    {
        // arrange

        // act
        BulkEnrollmentOperationResult bulkEnrollmentOperationResult = new BulkEnrollmentOperationResult(VALID_JSON);

        // assert
        BulkEnrollmentOperationError[] errors = Deencapsulation.getField(bulkEnrollmentOperationResult, "errors");
        assertEquals(2, errors.length);
        assertEquals(VALID_REGISTRATION_ID_1, errors[0].getRegistrationId());
        assertEquals(VALID_ERROR_CODE_1, errors[0].getErrorCode());
        assertEquals(VALID_ERROR_STATUS_1, errors[0].getErrorStatus());
        assertEquals(VALID_REGISTRATION_ID_2, errors[1].getRegistrationId());
        assertEquals(VALID_ERROR_CODE_2, errors[1].getErrorCode());
        assertEquals(VALID_ERROR_STATUS_2, errors[1].getErrorStatus());
    }

    /* SRS_BULK_OPERATION_RESULT_21_008: [The getSuccessful shall return the stored isSuccessful.] */
    @Test
    public void getSuccessfulReturnsSuccessful()
    {
        // arrange
        BulkEnrollmentOperationResult bulkEnrollmentOperationResult = new BulkEnrollmentOperationResult(VALID_JSON);

        // act
        Boolean successful = bulkEnrollmentOperationResult.getSuccessful();

        // assert
        assertTrue(successful);
    }

    /* SRS_BULK_OPERATION_RESULT_21_009: [The getErrors shall return the stored errors as List of BulkEnrollmentOperationError.] */
    @Test
    public void getErrorsReturnsErrors()
    {
        // arrange
        BulkEnrollmentOperationResult bulkEnrollmentOperationResult = new BulkEnrollmentOperationResult(VALID_JSON);

        // act
        List<BulkEnrollmentOperationError> errors = bulkEnrollmentOperationResult.getErrors();

        // assert
        assertNotNull(errors);
        assertEquals(2, errors.size());
        BulkEnrollmentOperationError bulkEnrollmentOperationError = errors.get(0);
        assertEquals(VALID_REGISTRATION_ID_1, bulkEnrollmentOperationError.getRegistrationId());
        assertEquals(VALID_ERROR_CODE_1, bulkEnrollmentOperationError.getErrorCode());
        assertEquals(VALID_ERROR_STATUS_1, bulkEnrollmentOperationError.getErrorStatus());
        bulkEnrollmentOperationError = errors.get(1);
        assertEquals(VALID_REGISTRATION_ID_2, bulkEnrollmentOperationError.getRegistrationId());
        assertEquals(VALID_ERROR_CODE_2, bulkEnrollmentOperationError.getErrorCode());
        assertEquals(VALID_ERROR_STATUS_2, bulkEnrollmentOperationError.getErrorStatus());
    }

    /* SRS_BULK_OPERATION_RESULT_21_010: [The toString shall return a String with the information into this class in a pretty print JSON.] */
    @Test
    public void toStringReturnsErrors()
    {
        // arrange
        BulkEnrollmentOperationResult bulkEnrollmentOperationResult = new BulkEnrollmentOperationResult(VALID_JSON);

        // act
        String result = bulkEnrollmentOperationResult.toString();

        // assert
        Helpers.assertJson(result, VALID_JSON);
    }

    /* SRS_BULK_OPERATION_RESULT_21_011: [The BulkEnrollmentOperationResult shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceeded()
    {
        // arrange

        // act
        BulkEnrollmentOperationResult bulkEnrollmentOperationResult =  Deencapsulation.newInstance(BulkEnrollmentOperationResult.class);

        // assert
        assertNotNull(bulkEnrollmentOperationResult);
    }
}
