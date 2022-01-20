// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.provisioning.service.Tools;

import java.lang.reflect.Field;

/**
 * The Device Provisioning Service query result type
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollmentgroup">Device Enrollment Group</a>
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/registrationstatus">Registration Status</a>
 */
public enum QueryResultType
{
    /**
     * the provisioning service cannot parse the information in the body.
     * You shall cast the Objects in the items using {@code String} and
     * parser it depending on the query the you sent.
     */
    @SerializedName("unknown")
    UNKNOWN,

    /**
     * The query result in a list of enrollments. You shall cast the
     * Objects in the items using {@link IndividualEnrollment}.
     */
    @SerializedName("enrollment")
    ENROLLMENT,

    /**
     * The query result in a list of device registrations. You shall cast
     * the Objects in the items using {@link EnrollmentGroup}.
     */
    @SerializedName("enrollmentGroup")
    ENROLLMENT_GROUP,

    /**
     * The query result in a list of enrollments. You shall cast the
     * Objects in the items using {@link DeviceRegistrationState}.
     */
    @SerializedName("deviceRegistration")
    DEVICE_REGISTRATION;

    static QueryResultType fromString(String result)
    {
        if (Tools.isNullOrEmpty(result))
        {
            throw new IllegalArgumentException("type cannot be null or empty.");
        }

        Field[] fields = QueryResultType.class.getFields();
        for (Field field: fields)
        {
            if (field.getAnnotation(SerializedName.class).value().equalsIgnoreCase(result))
            {
                return QueryResultType.valueOf(field.getName());
            }
        }
        throw new IllegalArgumentException("type is invalid.");
    }
}
