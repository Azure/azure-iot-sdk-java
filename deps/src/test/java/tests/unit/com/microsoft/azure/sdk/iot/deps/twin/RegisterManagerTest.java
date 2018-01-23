// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.deps.twin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.microsoft.azure.sdk.iot.deps.twin.RegisterManager;
import mockit.Deencapsulation;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for the TwinState
 * 100% methods, 100% lines covered
 */
public class RegisterManagerTest
{
    private final static String REGISTER_MANAGER_SAMPLE =
            "{" +
            "\"deviceId\":\"validDeviceId\"," +
            "\"generationId\":\"validGenerationId\"," +
            "\"version\":3," +
            "\"status\":\"enabled\"," +
            "\"statusReason\":\"validStatusReason\"," +
            "\"statusUpdatedTime\":\"2016-06-01T21:22:41+00:00\"," +
            "\"connectionState\":\"disconnected\"," +
            "\"connectionStateUpdatedTime\":\"2016-06-01T21:22:41+00:00\"," +
            "\"lastActivityTime\":\"xxx\"," +
            "\"etag\":\"xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx\"" +
            "}";


    /* Codes_SRS_REGISTER_MANAGER_21_001: [The setDeviceId shall throw IllegalArgumentException if the provided deviceId do not fits the criteria.] */
    @Test (expected = IllegalArgumentException.class)
    public void setDeviceIdThrowsOnNullDeviceId()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        RegisterManager result = gson.fromJson(REGISTER_MANAGER_SAMPLE, RegisterManager.class);

        // act - assert
        result.setDeviceId(null);
    }

    /* Codes_SRS_REGISTER_MANAGER_21_002: [The setDeviceId shall replace the `deviceId` by the provided one.] */
    @Test
    public void setDeviceIdSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        RegisterManager result = gson.fromJson(REGISTER_MANAGER_SAMPLE, RegisterManager.class);

        // act
        result.setDeviceId("newDeviceId");

        // assert
        assertEquals("newDeviceId", Deencapsulation.getField(result, "deviceId"));
    }

    /* Codes_SRS_REGISTER_MANAGER_21_003: [The setETag shall replace the `eTag` by the provided one.] */
    @Test
    public void setETagSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        RegisterManager result = gson.fromJson(REGISTER_MANAGER_SAMPLE, RegisterManager.class);

        // act
        result.setETag("yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyy");

        // assert
        assertEquals("yyyyyyyy-yyyy-yyyy-yyyy-yyyyyyyyyyy", Deencapsulation.getField(result, "eTag"));
    }

    /* Codes_SRS_REGISTER_MANAGER_21_004: [The getETag shall return the stored `eTag` content.] */
    /* Codes_SRS_REGISTER_MANAGER_21_005: [The getDeviceId shall return the stored `deviceId` content.] */
    /* Codes_SRS_REGISTER_MANAGER_21_006: [The getVersion shall return the stored `version` content.] */
    @Test
    public void registerManagerGettersSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        RegisterManager result = gson.fromJson(REGISTER_MANAGER_SAMPLE, RegisterManager.class);

        // act - assert
        assertEquals("validDeviceId", result.getDeviceId());
        assertEquals(3, (int)result.getVersion());
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", result.getETag());
    }

    /* Codes_SRS_REGISTER_MANAGER_21_007: [The RegisterManager shall provide an empty constructor to make GSON happy.] */
    @Test
    public void registerManagerDeserializerSucceed()
    {
        // arrange
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();
        RegisterManager result = gson.fromJson(REGISTER_MANAGER_SAMPLE, RegisterManager.class);

        // act - assert
        assertEquals("validDeviceId", Deencapsulation.getField(result, "deviceId"));
        assertEquals("validGenerationId", Deencapsulation.getField(result, "generationId"));
        assertEquals(3, Deencapsulation.getField(result, "version"));
        assertEquals("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", Deencapsulation.getField(result, "eTag"));
        assertEquals("ENABLED", Deencapsulation.getField(result, "status").toString());
        assertEquals("validStatusReason", Deencapsulation.getField(result, "statusReason"));
        assertEquals("2016-06-01T21:22:41+00:00", Deencapsulation.getField(result, "statusUpdatedTime"));
        assertEquals("DISCONNECTED", Deencapsulation.getField(result, "connectionState").toString());
        assertEquals("2016-06-01T21:22:41+00:00", Deencapsulation.getField(result, "connectionStateUpdatedTime"));
        assertEquals("xxx", Deencapsulation.getField(result, "lastActivityTime"));
    }
}
