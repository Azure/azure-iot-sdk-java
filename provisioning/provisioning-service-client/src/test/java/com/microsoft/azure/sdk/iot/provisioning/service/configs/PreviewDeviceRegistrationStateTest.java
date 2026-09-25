// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Deterministic, JMockit-free unit tests for the {@code 2026-11-02-preview} DPS service
 * contract addition on {@link DeviceRegistrationState}: the response-only, extensible
 * {@code connectionProfile} field.
 *
 * <p>Semantics:
 * <ul>
 *   <li>Known values (e.g. {@code classic}, {@code mqttV5}) deserialize verbatim.</li>
 *   <li>Unknown / future values are preserved verbatim (extensible enum).</li>
 *   <li>Absent value stays {@code null}; it is never synthesized.</li>
 *   <li>Response-only: there is no public setter.</li>
 * </ul>
 */
public class PreviewDeviceRegistrationStateTest
{
    private static final String VALID_REGISTRATION_ID = "validRegistrationId";

    /* A known connectionProfile value must deserialize verbatim. */
    @Test
    public void constructorWithJson_knownConnectionProfile_isDeserialized()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"connectionProfile\": \"mqttV5\"\n" +
                "}";

        // act
        DeviceRegistrationState state = new DeviceRegistrationState(json);

        // assert
        assertEquals("mqttV5", state.getConnectionProfile());
    }

    /* An unknown / future connectionProfile value must be preserved verbatim (extensible). */
    @Test
    public void constructorWithJson_unknownConnectionProfile_isPreservedVerbatim()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\",\n" +
                "  \"connectionProfile\": \"someFutureProfile\"\n" +
                "}";

        // act
        DeviceRegistrationState state = new DeviceRegistrationState(json);

        // assert
        assertEquals("someFutureProfile", state.getConnectionProfile());
    }

    /* An absent connectionProfile must remain null (never synthesized). */
    @Test
    public void constructorWithJson_absentConnectionProfile_isNull()
    {
        // arrange
        final String json = "{\n" +
                "  \"registrationId\": \"" + VALID_REGISTRATION_ID + "\"\n" +
                "}";

        // act
        DeviceRegistrationState state = new DeviceRegistrationState(json);

        // assert
        assertNull(state.getConnectionProfile());
    }

    /* connectionProfile is response-only: no public setter may exist. */
    @Test
    public void connectionProfile_isResponseOnly_noPublicSetter()
    {
        for (java.lang.reflect.Method m : DeviceRegistrationState.class.getMethods())
        {
            assertNotEquals("setConnectionProfile", m.getName());
        }
    }
}
