// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.auth;

/**
 * Enum for the type of authentication used.
 * SAS - shared access signature
 * selfSigned - self signed certificate
 * CERTIFICATE_AUTHORITY - certificate authority signed certificate
 */
public enum AuthenticationType
{
    SAS,
    selfSigned,
    CERTIFICATE_AUTHORITY
}