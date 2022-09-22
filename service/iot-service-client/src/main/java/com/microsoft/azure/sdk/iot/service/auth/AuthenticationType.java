// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.auth;

import com.google.gson.annotations.SerializedName;

/**
 * Enum for the type of authentication used.
 * SAS - shared access signature
 * SELF_SIGNED - self signed certificate
 * CERTIFICATE_AUTHORITY - certificate authority signed certificate
 */
public enum AuthenticationType
{
    @SerializedName("sas")
    SAS,
    @SerializedName("selfSigned")
    SELF_SIGNED,
    @SerializedName("certificateAuthority")
    CERTIFICATE_AUTHORITY
}