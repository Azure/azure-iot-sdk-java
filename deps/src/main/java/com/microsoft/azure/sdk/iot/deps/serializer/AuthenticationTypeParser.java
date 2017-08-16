// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.deps.serializer;

import com.google.gson.annotations.SerializedName;

public enum AuthenticationTypeParser
{
    @SerializedName("sas")
    SAS,

    @SerializedName("selfSigned")
    SELF_SIGNED,

    @SerializedName("certificateAuthority")
    CERTIFICATE_AUTHORITY;
}
