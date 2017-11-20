// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.BulkOperationResult;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.DeviceRegistrationOperationError;
import mockit.Deencapsulation;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service bulk operation result deserializer
 * 100% methods, 100% lines covered
 */
public class BulkOperationResultTest
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
        new BulkOperationResult(null);

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyJson()
    {
        // arrange

        // act
        new BulkOperationResult("");

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_002: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorThrowsOnInvalidJson()
    {
        // arrange

        // act
        new BulkOperationResult("{\"isSuccessful\": \"true\",}");

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
        new BulkOperationResult(missingSuccessful);

        // assert
    }

    /* SRS_BULK_OPERATION_RESULT_21_005: [The constructor shall throw IllegalArgumentException if the JSON contains invalid error.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnErrorsWithFail(
            @Mocked final DeviceRegistrationOperationError mockedDeviceRegistrationOperationError)
    {
        // arrange
        new NonStrictExpectations()
        {
            {
                Deencapsulation.invoke(mockedDeviceRegistrationOperationError, "validateError");
                result = new IllegalArgumentException();
            }
        };

        // act
        new BulkOperationResult(VALID_JSON);

        // assert
        new Verifications()
        {
            {
                Deencapsulation.invoke(mockedDeviceRegistrationOperationError, "validateError");
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
        BulkOperationResult bulkOperationResult = new BulkOperationResult(VALID_JSON);

        // assert
        assertTrue(Deencapsulation.getField(bulkOperationResult, "isSuccessful"));
    }

    /* SRS_BULK_OPERATION_RESULT_21_003: [The constructor shall deserialize the provided JSON for the enrollment class and subclasses.] */
    /* SRS_BULK_OPERATION_RESULT_21_007: [The constructor shall store the provided errors.] */
    @Test
    public void constructorStoreErrors()
    {
        // arrange

        // act
        BulkOperationResult bulkOperationResult = new BulkOperationResult(VALID_JSON);

        // assert
        DeviceRegistrationOperationError[] errors = Deencapsulation.getField(bulkOperationResult, "errors");
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
        BulkOperationResult bulkOperationResult = new BulkOperationResult(VALID_JSON);

        // act
        Boolean successful = bulkOperationResult.getSuccessful();

        // assert
        assertTrue(successful);
    }

    /* SRS_BULK_OPERATION_RESULT_21_009: [The getErrors shall return the stored errors as List of DeviceRegistrationOperationError.] */
    @Test
    public void getErrorsReturnsErrors()
    {
        // arrange
        BulkOperationResult bulkOperationResult = new BulkOperationResult(VALID_JSON);

        // act
        List<DeviceRegistrationOperationError> errors = bulkOperationResult.getErrors();

        // assert
        assertNotNull(errors);
        assertEquals(2, errors.size());
        DeviceRegistrationOperationError deviceRegistrationOperationError = errors.get(0);
        assertEquals(VALID_REGISTRATION_ID_1, deviceRegistrationOperationError.getRegistrationId());
        assertEquals(VALID_ERROR_CODE_1, deviceRegistrationOperationError.getErrorCode());
        assertEquals(VALID_ERROR_STATUS_1, deviceRegistrationOperationError.getErrorStatus());
        deviceRegistrationOperationError = errors.get(1);
        assertEquals(VALID_REGISTRATION_ID_2, deviceRegistrationOperationError.getRegistrationId());
        assertEquals(VALID_ERROR_CODE_2, deviceRegistrationOperationError.getErrorCode());
        assertEquals(VALID_ERROR_STATUS_2, deviceRegistrationOperationError.getErrorStatus());
    }

    /* SRS_BULK_OPERATION_RESULT_21_010: [The toString shall return a String with the information into this class in a pretty print JSON.] */
    @Test
    public void toStringReturnsErrors()
    {
        // arrange
        BulkOperationResult bulkOperationResult = new BulkOperationResult(VALID_JSON);

        // act
        String result = bulkOperationResult.toString();

        // assert
        Helpers.assertJson(result, VALID_JSON);
    }

    /* SRS_BULK_OPERATION_RESULT_21_011: [The BulkOperationResult shall provide an empty constructor to make GSON happy.] */
    @Test
    public void emptyConstructorSucceeded()
    {
        // arrange

        // act
        BulkOperationResult bulkOperationResult =  Deencapsulation.newInstance(BulkOperationResult.class);

        // assert
        assertNotNull(bulkOperationResult);
    }
}
