// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.azure.sdk.iot.provisioning.service.configs.Serializable;
import org.junit.Test;
import tests.unit.com.microsoft.azure.sdk.iot.provisioning.service.Helpers;

/**
 * Unit tests for Device Provisioning Service Serializable
 * 100% methods, 100% lines covered
 */
public class SerializableTest
{
    final String JSON =
            "{" +
            "    \"prop1\":\"val1\"," +
            "    \"prop2\":\"val2\"," +
            "    \"prop3\":\"val3\"" +
            "}";

    static final class mockedChild extends Serializable
    {
        protected JsonElement toJsonElement()
        {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("prop1", "val1");
            jsonObject.addProperty("prop2", "val2");
            jsonObject.addProperty("prop3", "val3");

            return jsonObject;
        }
    }

    /* SRS_SERIALIZABLE_21_001: [The toJson shall return a String with the information in the child class in a JSON format.] */
    @Test
    public void toJsonSimpleEnrollment()
    {
        // arrange
        mockedChild child = new mockedChild();

        // act
        String result = child.toJson();

        // assert
        Helpers.assertJson(result, JSON);
    }

    /* SRS_SERIALIZABLE_21_002: [The toString shall return a String with the information in the child class in a pretty print JSON.] */
    @Test
    public void toStringSimpleEnrollmentWithTwin()
    {
        // arrange
        mockedChild child = new mockedChild();

        // act
        String result = child.toString();

        // assert
        Helpers.assertJson(result, JSON);
    }

}
