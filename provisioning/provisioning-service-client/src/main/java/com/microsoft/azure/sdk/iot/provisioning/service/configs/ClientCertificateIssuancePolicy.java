// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.configs;

import lombok.Getter;
import lombok.Setter;

/* Custom class for issuing client certificates. */
public class ClientCertificateIssuancePolicy
{
    /* The name of the CA that can receive signing requests from DPS and issue it with an operational certificate. */
    @Getter
    @Setter
    public String certificateAuthorityName;
}
