// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.helpers;

import com.microsoft.azure.sdk.iot.deps.auth.ProvisioningConnectionString;
import com.microsoft.azure.sdk.iot.deps.auth.ProvisioningConnectionStringBuilder;
import com.microsoft.azure.sdk.iot.deps.auth.SharedAccessSignatureCredentials;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ProvisioningServiceClientHelper
{
    public static ServiceClientCredentials createCredentialsFromConnectionString(String connectionString)
    {
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        return new SharedAccessSignatureCredentials(provisioningConnectionString);
    }
}
