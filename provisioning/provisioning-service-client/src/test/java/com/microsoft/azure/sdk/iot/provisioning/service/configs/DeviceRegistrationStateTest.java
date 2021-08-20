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
            "    \"lastUpdatedDateTimeUtc\": \"" + VALID_DATE_AS_STRING + "\",\n" +
            "    \"errorCode\":" + VALID_ERROR_CODE + ",\n" +
            "    \"errorMessage\":\"" + VALID_ERROR_MESSAGE + "\",\n" +
            "    \"etag\": \"" + VALID_ETAG + "\"\n" +
            "}";


    /* SRS_DEVICE_REGISTRATION_STATE_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullJson()
    {
        // arrange
        // act
        new DeviceRegistrationState(null);
        // assert
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_001: [The constructor shall throw IllegalArgumentException if the JSON is null or empty.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyJson()
    {
        // arrange
        // act
        new DeviceRegistrationState("");
        // assert
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_002: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected =  JsonSyntaxException.class)
    public void constructorThrowsOnInvalidJson()
    {
        // arrange
        // act
        new DeviceRegistrationState("{\"a\":\"b\",}");
        // assert
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_002: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_003: [The constructor shall deserialize the provided JSON for the DeviceRegistrationState class.] */
    @Test
    public void constructorParserJson()
    {
        // arrange

        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertNotNull(deviceRegistrationState);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_005: [The constructor shall store the provided registrationId.] */
    @Test
    public void constructorStoreRegistrationId()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(VALID_REGISTRATION_ID, Deencapsulation.getField(deviceRegistrationState, "registrationId"));
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_007: [The constructor shall store the provided deviceId.] */
    @Test
    public void constructorStoreDeviceId()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(VALID_DEVICE_ID, Deencapsulation.getField(deviceRegistrationState, "deviceId"));
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_008: [If the createdDateTimeUtc is provided, the constructor shall parse it as date and time UTC.] */
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
        assertNull(Deencapsulation.getField(deviceRegistrationState, "createdDateTimeUtc"));
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_031: [Te constructor shall throw IllegalArgumentException if the createdDateTimeUtc is empty or invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_031: [Te constructor shall throw IllegalArgumentException if the createdDateTimeUtc is empty or invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_008: [If the createdDateTimeUtc is provided, the constructor shall parse it as date and time UTC.] */
    @Test
    public void constructorStoreCreatedDateTimeUtc()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(deviceRegistrationState, "createdDateTimeUtc"), VALID_DATE_AS_STRING);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_009: [If the lastUpdatedDateTimeUtc is provided, the constructor shall parse it as date and time UTC.] */
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
        assertNull(Deencapsulation.getField(deviceRegistrationState, "lastUpdatedDateTimeUtc"));
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_032: [Te constructor shall throw IllegalArgumentException if the lastUpdatedDateTimeUtc is empty or invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_032: [Te constructor shall throw IllegalArgumentException if the lastUpdatedDateTimeUtc is empty or invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_009: [If the lastUpdatedDateTimeUtc is provided, the constructor shall parse it as date and time UTC.] */
    @Test
    public void constructorStoreLastUpdatedDateTimeUtc()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        Helpers.assertDateWithError((Date)Deencapsulation.getField(deviceRegistrationState, "lastUpdatedDateTimeUtc"), VALID_DATE_AS_STRING);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_010: [If the assignedHub is not null, the constructor shall judge and store it.] */
    @Test
    public void constructorStoreAssignedHub()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "assignedHub"), VALID_ASSIGNED_HUB);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_010: [If the assignedHub is not null, the constructor shall judge and store it.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_014: [The constructor shall throw IllegalArgumentException if the provided status is invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_015: [If the errorCode is not null, the constructor shall store the provided status.] */
    @Test
    public void constructorStoreStatus()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(EnrollmentStatus.ASSIGNED, Deencapsulation.getField(deviceRegistrationState, "status"));
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_016: [If the errorCode is not null, the constructor shall store it.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_016: [If the errorCode is not null, the constructor shall store it.] */
    @Test
    public void constructorStoreErrorCode()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "errorCode"), VALID_ERROR_CODE);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_017: [If the errorMessage is not null, the constructor shall store it.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_017: [If the errorMessage is not null, the constructor shall store it.] */
    @Test
    public void constructorStoreErrorMessage()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "errorMessage"), VALID_ERROR_MESSAGE);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_018: [If the etag is not null, the constructor shall judge and store it.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_018: [If the etag is not null, the constructor shall judge and store it.] */
    @Test
    public void constructorStoreEtag()
    {
        // arrange
        // act
        DeviceRegistrationState deviceRegistrationState = new DeviceRegistrationState(VALID_JSON);

        // assert
        assertEquals(Deencapsulation.getField(deviceRegistrationState, "etag"), VALID_PARSED_ETAG);
    }

    /* SRS_DEVICE_REGISTRATION_STATE_21_019: [The constructor shall throw IllegalArgumentException if an provided etag is empty or invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_019: [The constructor shall throw IllegalArgumentException if an provided etag is empty or invalid.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_020: [The getRegistrationId shall return a String with the stored registrationId.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_021: [The getDeviceId shall return a String with the stored deviceId.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_022: [The getCreatedDateTimeUtc shall return a Date with the stored createdDateTimeUtc.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_023: [The getLastUpdatedDateTimeUtc shall return a Date with the stored lastUpdatedDateTimeUtc.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_024: [The getAssignedHub shall return a String with the stored assignedHub.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_026: [The getStatus shall return an EnrollmentStatus with the stored status.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_027: [The getErrorCode shall return a Integer with the stored errorCode.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_028: [The getErrorMessage shall return a String with the stored errorMessage.] */
    /* SRS_DEVICE_REGISTRATION_STATE_21_029: [The getEtag shall return a String with the stored etag.] */
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

    /* SRS_DEVICE_REGISTRATION_STATE_21_030: [The DeviceRegistrationState shall provide an empty constructor to make GSON happy.] */
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
