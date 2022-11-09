// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import java.security.PrivateKey;

import lombok.Getter;
import lombok.Setter;

public class ClientCertificateIssuancePolicy
{
    // The CA name that can issue an operational certificate for a device on behalf of DPS.
    @Getter
    @Setter
    public PrivateKey CertificateAuthorityName;
}
