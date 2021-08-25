// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import mockit.Deencapsulation;
import org.junit.Test;
import com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

import static org.junit.Assert.*;

/**
 * Unit tests for Device Provisioning Service query result deserializer
 * 100% methods, 100% lines covered
 */
public class QueryResultTest
{
    private static final String VALID_CONTINUATION_TOKEN = "{\"token\":\"+RID:Defghij6KLMNOPQ==#RS:1#TRC:2#FPC:AUAAAAAAAAAJQABAAAAAAAk=\",\"range\":{\"min\":\"0123456789abcd\",\"max\":\"FF\"}}";
    private static final String VALID_INT_JSON = "[1, 2, 3]";
    private static final String VALID_OBJECT_JSON = "[{\"a\":1}, {\"a\":2}, {\"a\":3}]";

    private static final String VALID_ENROLLMENT_1 =
            "    {\n" +
            "      \"registrationId\": \"registrationid-ae518a62-3480-4639-bce2-5b69a3bb35a3\",\n" +
            "      \"deviceId\": \"JavaDevice-c743c684-2190-4062-a5a8-efc416ad4dba\",\n" +
            "      \"attestation\": {\n" +
            "        \"type\": \"tpm\",\n" +
            "        \"tpm\": {\n" +
            "          \"endorsementKey\": \"randonendorsementkeyfortest==\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"iotHubHostName\": \"ContosoIotHub.azure-devices.net\",\n" +
            "      \"provisioningStatus\": \"enabled\",\n" +
            "      \"createdDateTimeUtc\": \"2017-09-19T15:45:53.3981876Z\",\n" +
            "      \"lastUpdatedDateTimeUtc\": \"2017-09-19T15:45:53.3981876Z\",\n" +
            "      \"capabilities\": {\n" + "        \"iotEdge\": false\n" + "      }" +
            "    }";
    private static final String VALID_ENROLLMENT_2 =
            "    {\n" +
            "      \"registrationId\": \"registrationid-6bdaeb7c-51fc-4a67-b24e-64e42d3aa698\",\n" +
            "      \"deviceId\": \"JavaDevice-eb17e87a-11aa-4794-944f-bbbf1fb960a0\",\n" +
            "      \"attestation\": {\n" +
            "        \"type\": \"tpm\",\n" +
            "        \"tpm\": {\n" +
            "          \"endorsementKey\": \"randonendorsementkeyfortest==\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"iotHubHostName\": \"ContosoIotHub.azure-devices.net\",\n" +
            "      \"provisioningStatus\": \"enabled\",\n" +
            "      \"createdDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\",\n" +
            "      \"lastUpdatedDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\"\n" +
            "    }";
    private static final String VALID_ENROLLMENT_JSON =
        "[\n" +
        VALID_ENROLLMENT_1 + ",\n" +
        VALID_ENROLLMENT_2 +
        "]";

    private static final String VALID_ENROLLMENT_GROUP_1 =
            "    {\n" +
            "      \"enrollmentGroupId\": \"enrollmentGroupId-ae518a62-3480-4639-bce2-5b69a3bb35a3\",\n" +
            "      \"attestation\": {\n" +
            "        \"type\": \"tpm\",\n" +
            "        \"tpm\": {\n" +
            "          \"endorsementKey\": \"randonendorsementkeyfortest==\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"iotHubHostName\": \"ContosoIotHub.azure-devices.net\",\n" +
            "      \"provisioningStatus\": \"enabled\",\n" +
            "      \"createdDateTimeUtc\": \"2017-09-19T15:45:53.3981876Z\",\n" +
            "      \"lastUpdatedDateTimeUtc\": \"2017-09-19T15:45:53.3981876Z\"\n" +
            "    }";
    private static final String VALID_ENROLLMENT_GROUP_2 =
            "    {\n" +
            "      \"enrollmentGroupId\": \"enrollmentGroupId-6bdaeb7c-51fc-4a67-b24e-64e42d3aa698\",\n" +
            "      \"attestation\": {\n" +
            "        \"type\": \"tpm\",\n" +
            "        \"tpm\": {\n" +
            "          \"endorsementKey\": \"randonendorsementkeyfortest==\"\n" +
            "        }\n" +
            "      },\n" +
            "      \"iotHubHostName\": \"ContosoIotHub.azure-devices.net\",\n" +
            "      \"provisioningStatus\": \"enabled\",\n" +
            "      \"createdDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\",\n" +
            "      \"lastUpdatedDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\"\n" +
            "    }";

    private static final String VALID_ENROLLMENT_GROUP_JSON =
            "[\n" +
            VALID_ENROLLMENT_GROUP_1 + ",\n" +
            VALID_ENROLLMENT_GROUP_2 +
            "]";

    private static final String VALID_REGISTRATION_STATUS_1 =
            "{\n" +
            "    \"registrationId\":\"registrationid-ae518a62-3480-4639-bce2-5b69a3bb35a3\",\n" +
            "    \"createdDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\",\n" +
            "    \"assignedHub\":\"ContosoIotHub.azure-devices.net\",\n" +
            "    \"deviceId\":\"JavaDevice-c743c684-2190-4062-a5a8-efc416ad4dba\",\n" +
            "    \"status\":\"assigned\",\n" +
            "    \"lastUpdatedDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\",\n" +
            "    \"errorCode\": 200,\n" +
            "    \"errorMessage\":\"Succeeded\",\n" +
            "    \"etag\": \"00000000-0000-0000-0000-00000000000\"\n" +
            "}";
    private static final String VALID_REGISTRATION_STATUS_2 =
            "{\n" +
            "    \"registrationId\":\"registrationid-6bdaeb7c-51fc-4a67-b24e-64e42d3aa698\",\n" +
            "    \"createdDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\",\n" +
            "    \"assignedHub\":\"ContosoIotHub.azure-devices.net\",\n" +
            "    \"deviceId\":\"JavaDevice-c743c684-2190-4062-a5a8-efc416ad4dba\",\n" +
            "    \"status\":\"assigned\",\n" +
            "    \"lastUpdatedDateTimeUtc\": \"2017-09-19T15:46:35.1533673Z\",\n" +
            "    \"errorCode\": 200,\n" +
            "    \"errorMessage\":\"Succeeded\",\n" +
            "    \"etag\": \"00000000-0000-0000-0000-00000000000\"\n" +
            "}";
    private static final String VALID_REGISTRATION_STATUS_JSON =
            "[\n" +
            VALID_REGISTRATION_STATUS_1 + ",\n" +
            VALID_REGISTRATION_STATUS_2 +
            "]";

    /* SRS_QUERY_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the provided type is null, empty, or not parsed to QueryResultType.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullType()
    {
        // arrange
        // act
        QueryResult queryResult = new QueryResult(null, VALID_INT_JSON, VALID_CONTINUATION_TOKEN);
        // assert
    }

    /* SRS_QUERY_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the provided type is null, empty, or not parsed to QueryResultType.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyType()
    {
        // arrange
        // act
        QueryResult queryResult = new QueryResult("", VALID_INT_JSON, VALID_CONTINUATION_TOKEN);
        // assert
    }

    /* SRS_QUERY_RESULT_21_001: [The constructor shall throw IllegalArgumentException if the provided type is null, empty, or not parsed to QueryResultType.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnInvalidType()
    {
        // arrange
        // act
        QueryResult queryResult = new QueryResult("InvalidType", VALID_INT_JSON, VALID_CONTINUATION_TOKEN);
        // assert
    }

    /* SRS_QUERY_RESULT_21_002: [The constructor shall throw IllegalArgumentException if the provided body is null or empty and the type is not `unknown`.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnNullBody()
    {
        // arrange
        // act
        QueryResult queryResult = new QueryResult("enrollment", null, VALID_CONTINUATION_TOKEN);
        // assert
    }

    /* SRS_QUERY_RESULT_21_002: [The constructor shall throw IllegalArgumentException if the provided body is null or empty and the type is not `unknown`.] */
    @Test (expected = IllegalArgumentException.class)
    public void constructorThrowsOnEmptyBody()
    {
        // arrange
        // act
        QueryResult queryResult = new QueryResult("enrollment", "", VALID_CONTINUATION_TOKEN);
        // assert
    }

    /* SRS_QUERY_RESULT_21_003: [The constructor shall throw JsonSyntaxException if the JSON is invalid.] */
    @Test (expected = JsonSyntaxException.class)
    public void constructorThrowsOnInvalidJson()
    {
        // arrange
        // act
        QueryResult queryResult = new QueryResult("enrollment", "[1, 2, ]", VALID_CONTINUATION_TOKEN);
        // assert
    }

    /* SRS_QUERY_RESULT_21_004: [If the type is `enrollment`, the constructor shall parse the body as IndividualEnrollment[].] */
    /* SRS_QUERY_RESULT_21_011: [The constructor shall store the provided parameters `type` and `continuationToken`.] */
    @Test
    public void constructorEnrollmentSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = new QueryResult("enrollment", VALID_ENROLLMENT_JSON, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.ENROLLMENT, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        IndividualEnrollment[] items = (IndividualEnrollment[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(2, items.length);
    }

    /* SRS_QUERY_RESULT_21_005: [If the type is `enrollmentGroup`, the constructor shall parse the body as EnrollmentGroup[].] */
    /* SRS_QUERY_RESULT_21_011: [The constructor shall store the provided parameters `type` and `continuationToken`.] */
    @Test
    public void constructorEnrollmentGroupSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = new QueryResult("enrollmentGroup", VALID_ENROLLMENT_GROUP_JSON, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.ENROLLMENT_GROUP, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        EnrollmentGroup[] items = (EnrollmentGroup[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(2, items.length);
    }

    /* SRS_QUERY_RESULT_21_006: [If the type is `deviceRegistration`, the constructor shall parse the body as DeviceRegistrationState[].] */
    /* SRS_QUERY_RESULT_21_011: [The constructor shall store the provided parameters `type` and `continuationToken`.] */
    @Test
    public void constructorDeviceRegistrationSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = new QueryResult("deviceRegistration", VALID_REGISTRATION_STATUS_JSON, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.DEVICE_REGISTRATION, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        DeviceRegistrationState[] items = (DeviceRegistrationState[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(2, items.length);
    }

    /* SRS_QUERY_RESULT_21_007: [If the type is `unknown`, and the body is null, the constructor shall set `items` as null.] */
    @Test
    public void constructorUnknownBodyNullSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = new QueryResult("unknown", null, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.UNKNOWN, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        assertNull(Deencapsulation.getField(queryResult, "items"));
    }

    /* SRS_QUERY_RESULT_21_008: [If the type is `unknown`, the constructor shall try to parse the body as JsonObject[].] */
    @Test
    public void constructorUnknownBodyObjectsSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = new QueryResult("unknown", VALID_OBJECT_JSON, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.UNKNOWN, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        JsonObject[] items = (JsonObject[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(3, items.length);
    }

    /* SRS_QUERY_RESULT_21_009: [If the type is `unknown`, and the constructor failed to parse the body as JsonObject[], it shall try to parse the body as JsonPrimitive[].] */
    @Test
    public void constructorUnknownBodyPrimitivesSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = new QueryResult("unknown", VALID_INT_JSON, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.UNKNOWN, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        JsonPrimitive[] items = (JsonPrimitive[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(3, items.length);
    }

    /* SRS_QUERY_RESULT_21_010: [If the type is `unknown`, and the constructor failed to parse the body as JsonObject[] and JsonPrimitive[], it shall return the body as a single string in the items.] */
    @Test
    public void constructorUnknownNonDeserializableBodySucceed()
    {
        // arrange
        final String body = "This is a non deserializable body";

        // act
        QueryResult queryResult = new QueryResult("unknown", body, VALID_CONTINUATION_TOKEN);

        // assert
        assertEquals(QueryResultType.UNKNOWN, Deencapsulation.getField(queryResult, "type"));
        assertEquals(VALID_CONTINUATION_TOKEN, Deencapsulation.getField(queryResult, "continuationToken"));
        String[] items = (String[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(1, items.length);
        assertEquals(body, items[0]);
    }

    /* SRS_QUERY_RESULT_21_011: [The constructor shall store the provided parameters `type` and `continuationToken`.] */
    @Test
    public void constructorNullContinuationTokenSucceed()
    {
        // arrange
        final String body = "This is a non deserializable body";

        // act
        QueryResult queryResult = new QueryResult("unknown", body, null);

        // assert
        assertEquals(QueryResultType.UNKNOWN, Deencapsulation.getField(queryResult, "type"));
        assertNull(Deencapsulation.getField(queryResult, "continuationToken"));
        String[] items = (String[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(1, items.length);
        assertEquals(body, items[0]);
    }

    /* SRS_QUERY_RESULT_21_011: [The constructor shall store the provided parameters `type` and `continuationToken`.] */
    @Test
    public void constructorEmptyContinuationTokenSucceed()
    {
        // arrange
        final String body = "This is a non deserializable body";

        // act
        QueryResult queryResult = new QueryResult("unknown", body, "");

        // assert
        assertEquals(QueryResultType.UNKNOWN, Deencapsulation.getField(queryResult, "type"));
        assertEquals("", Deencapsulation.getField(queryResult, "continuationToken"));
        String[] items = (String[]) Deencapsulation.getField(queryResult, "items");
        assertEquals(1, items.length);
        assertEquals(body, items[0]);
    }

    /* SRS_QUERY_RESULT_21_012: [The getType shall return the stored type.] */
    /* SRS_QUERY_RESULT_21_013: [The getContinuationToken shall return the stored continuationToken.] */
    /* SRS_QUERY_RESULT_21_014: [The getItems shall return the stored items.] */
    @Test
    public void GettersSucceed()
    {
        // arrange
        QueryResult queryResult = new QueryResult("enrollment", VALID_ENROLLMENT_JSON, VALID_CONTINUATION_TOKEN);

        // act - assert
        assertEquals(QueryResultType.ENROLLMENT, queryResult.getType());
        assertEquals(VALID_CONTINUATION_TOKEN, queryResult.getContinuationToken());
        IndividualEnrollment[] items = (IndividualEnrollment[]) queryResult.getItems();
        assertEquals(2, items.length);
    }

    /* SRS_QUERY_RESULT_21_015: [The toString shall return a String with the information in this class in a pretty print JSON.] */
    @Test
    public void toStringSucceed()
    {
        // arrange
        final String continuationToken = "{\"token\":\"+RID:Defghij6KLMNOPQ==#RS:1#TRC:2#FPC:AUAAAAAAAAAJQABAAAAAAAk=\",\"range\":{\"min\":\"0123456789abcd\",\"max\":\"FF\"}}";
        final String continuationTokenInJson = "{\\\"token\\\":\\\"+RID:Defghij6KLMNOPQ==#RS:1#TRC:2#FPC:AUAAAAAAAAAJQABAAAAAAAk=\\\",\\\"range\\\":{\\\"min\\\":\\\"0123456789abcd\\\",\\\"max\\\":\\\"FF\\\"}}";
        final String body = "[ " + VALID_ENROLLMENT_1 + " ]";
        final String expectedResult =
                "{\n" +
                "  \"type\":\"enrollment\",\n" +
                "  \"continuationToken\":\"" + continuationTokenInJson + "\",\n" +
                "  \"items\":[\n" +
                VALID_ENROLLMENT_1 + "\n" +
                "  ]\n" +
                "}";
        QueryResult queryResult = new QueryResult("enrollment", body, continuationToken);

        // act
        String result = queryResult.toString();

        // assert
        Helpers.assertJson(expectedResult, result);
    }

    /* SRS_QUERY_RESULT_21_016: [The EnrollmentGroup shall provide an empty constructor to make GSON happy.] */
    @Test
    public void deserializeResultSucceed()
    {
        // arrange

        // act
        QueryResult queryResult = Deencapsulation.newInstance(QueryResult.class);

        // assert
        assertNotNull(queryResult);
    }
}
