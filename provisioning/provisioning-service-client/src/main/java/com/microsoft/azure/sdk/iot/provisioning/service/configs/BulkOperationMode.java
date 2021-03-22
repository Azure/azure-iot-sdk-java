// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import com.google.gson.annotations.SerializedName;

/**
 * The Device Provisioning Service bulk operation modes.
 *
 * @see <a href="https://docs.microsoft.com/en-us/rest/api/iot-dps/deviceenrollment">Device Enrollment</a>
 */
@SuppressWarnings("unused") // used by reflection during json serialization/deserialization
public enum BulkOperationMode
{
    @SerializedName("create")
    CREATE,

    @SerializedName("update")
    UPDATE,

    @SerializedName("updateIfMatchETag")
    UPDATE_IF_MATCH_ETAG,

    @SerializedName("delete")
    DELETE
}
