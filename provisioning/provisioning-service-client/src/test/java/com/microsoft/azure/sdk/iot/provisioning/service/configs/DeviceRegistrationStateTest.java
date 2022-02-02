// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonSyntaxException;
import com.microsoft.azure.sdk.iot.deps.serializer.ParserUtility;
import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service bulk operation result deserializer
 * 100% methods, 100% lines covered
 */
public class DeviceRegistrationStateTest
{
    private static final String VALID_REGISTRATION_ID = "8be9cd0e-8934-4991-9cbf-cc3b6c7ac647";
    private static final String VALID_DEVICE_ID = "19adff39-9cb4-4dcc-8f66-ac36ca2bdb15";
    private static final String VALID_ASSIGNED_HUB = "ContosoIoTHub.azure-devices.net";
    private static final Integer VALID_ERROR_CODE = 200;
    private static final String VALID_ERROR_MESSAGE = "Succeeded";
    private static final String VALID_STATUS = "assigned"; //EnrollmentStatus.ASSIGNED;
    private static final String VALID_SUBSTATUS = "initialAssignment"; //ProvisioningServiceClientSubstatus.initialAssignment
    private static final String VALID_ETAG = "\\\"00000000-0000-0000-0000-00000000000\\\"";
    private static final String VALID_PARSED_ETAG = "\"00000000-0000-0000-0000-00000000000\"";

    private static final Date VALID_DATE = new Date();
    private static final String VALID_DATE_AS_STRING = ParserUtility.dateTimeUtcToString(VALID_DATE);

    private static final String VALID_JSON =
            "{\n" +
            "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
            "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
            "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
            "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
            "    \"status\":\"" + VALID_STATUS + "\",\n" +
            "    \"substatus\":\"" + VALID_SUBSTATUS + "\",\n" +
            "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
            "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
            "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
            "    \"etag\": \"" + VALID_ETAG + "\"\n" +
            "}";


    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullJson()
    {
        // arrange
        // act
        new DeviceRegistrationState(null);
        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyJson()
    {
        // arrange
        // act
        new DeviceRegistrationState("");
        // assert
    }

    @Test (expected =  JsonSyntaxException.class)
    public void constructorThrowsOnInvalidJson()
    {
        // arrange
        // act
        new DeviceRegistrationState("{\"a\":\"b\",}");
        // assert
    }

    @Test (expected =  JsonSyntaxException.class)
    public void constructorThrowsOnInvalidErrorCode()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":\"InvalidErrorCode\",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test
    public void constructorParserJson()
    {
        // arrange

        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertNotNull(deviceRegistrationState);
    }

    @Test
    public void constructorStoreRegistrationId()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(VALID_REGISTRATION_ID, Deencapsulation.getField(deviceRegistrationState, "registrationId"));
    }

    @Test
    public void constructorStoreDeviceId()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(VALID_DEVICE_ID, Deencapsulation.getField(deviceRegistrationState, "deviceId"));
    }

    @Test
    public void constructorIgnoreNullCreatedDateTimeUtc()
    {
        // arrange
        final String json =
                "{\n" +
                "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                "    \"status\":\"" + VALID_STATUS + "\",\n" +
                "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(json);

        // assert
        assertNull(Deencapsulation.getField(deviceRegistrationState, "createdDateTimeUtcDate"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyCreatedDateTimeUtc()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"0000-00-00T00:00\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"createdDateTimeUtc\": \"\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidCreatedDateTimeUtc()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"0000-00-00T00:00\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"createdDateTimeUtc\": \"0000-00-00 00:00:00\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test
    public void constructorStoreCreatedDateTimeUtc()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(deviceRegistrationState, "createdDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    @Test
    public void constructorIgnoreNullLastUpdatedDateTimeUtc()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(json);

        // assert
        assertNull(Deencapsulation.getField(deviceRegistrationState, "lastUpdatedDateTimeUtcDate"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyLastUpdatedDateTimeUtc()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidLastUpdatedDateTimeUtc()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"0000-00-00 00:00:00\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test
    public void constructorStoreLastUpdatedDateTimeUtc()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(deviceRegistrationState, "lastUpdatedDateTimeUtcDate"), VALID_DATE_AS_STRING);
    }

    @Test
    public void constructorStoreAssignedHub()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "assignedHub"), VALID_ASSIGNED_HUB);
    }

    @Test
    public void constructorSucceedOnNullAssignedHub()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(json);

        // assert
        assertNull(Deencapsulation.getField(deviceRegistrationState, "assignedHub"));
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidStatus()
    {
        // arrange
        final String json =
                "{\n" +
                "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                "    \"status\":\"InvalidStatus\",\n" +
                "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidSubstatus()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"substatus\":INVALID_SUBSTATUS\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test
    public void constructorStoreStatus()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(EnrollmentStatus.ASSIGNED, Deencapsulation.getField(deviceRegistrationState, "status"));
    }

    @Test
    public void constructorSucceedOnNullErrorCode()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(json);

        // assert
        assertNull(Deencapsulation.getField(deviceRegistrationState, "errorCode"));
    }

    @Test
    public void constructorStoreErrorCode()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "errorCode"), VALID_ERROR_CODE);
    }

    @Test
    public void constructorSucceedOnNullErrorMessage()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"etag\": \"" + VALID_ETAG + "\"\n" +
                        "}";
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(json);

        // assert
        assertNull(Deencapsulation.getField(deviceRegistrationState, "errorMessage"));
    }

    @Test
    public void constructorStoreErrorMessage()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "errorMessage"), VALID_ERROR_MESSAGE);
    }

    @Test
    public void constructorSucceedOnNullEtag()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\"\n" +
                        "}";
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(json);

        // assert
        assertNull(Deencapsulation.getField(deviceRegistrationState, "etag"));
    }

    @Test
    public void constructorStoreEtag()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "etag"), VALID_PARSED_ETAG);
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyEtag()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidEtag()
    {
        // arrange
        final String json =
                "{\n" +
                        "    \"registrationId\":\"" + VALID_REGISTRATION_ID + "\",\n" +
                        "    \"createdDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"assignedHub\":\"" + VALID_ASSIGNED_HUB + "\",\n" +
                        "    \"deviceId\":\"" + VALID_DEVICE_ID + "\",\n" +
                        "    \"status\":\"" + VALID_STATUS + "\",\n" +
                        "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
                        "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
                        "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
                        "    \"etag\": \"\u1234InvalidEtag\"\n" +
                        "}";
        // act
        new DeviceRegistrationState(json);

        // assert
    }

    @Test
    public void gettersSucceed()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(VALID_REGISTRATION_ID, deviceRegistrationState.getRegistrationId());
        assertEquals(VALID_DEVICE_ID, deviceRegistrationState.getDeviceId());
        Helpers.assertDateWithError((Date)deviceRegistrationState.getCreatedDateTimeUtc(), VALID_DATE_AS_STRING);
        Helpers.assertDateWithError((Date)deviceRegistrationState.getLastUpdatedDateTimeUtc(), VALID_DATE_AS_STRING);
        assertEquals(VALID_ASSIGNED_HUB, deviceRegistrationState.getAssignedHub());
        assertEquals(EnrollmentStatus.ASSIGNED, deviceRegistrationState.getStatus());
        assertEquals(VALID_ERROR_CODE, deviceRegistrationState.getErrorCode());
        assertEquals(VALID_ERROR_MESSAGE, deviceRegistrationState.getErrorMessage());
        assertEquals(VALID_PARSED_ETAG, deviceRegistrationState.getEtag());
    }

    @Test
    public void emptyConstructorExists()
    {
        // arrange

        // act
        DeviceRegistrationState deviceRegistrationState = Deencapsulation.newInstance(DeviceRegistrationState.class);

        // assert
        assertNotNull(deviceRegistrationState);
    }
}
