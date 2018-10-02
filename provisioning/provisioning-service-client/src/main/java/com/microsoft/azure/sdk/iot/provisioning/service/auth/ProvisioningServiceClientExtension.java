// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.provisioning.service.auth;

import com.microsoft.azure.sdk.iot.deps.util.Tools;
import com.microsoft.rest.credentials.ServiceClientCredentials;

public class ProvisioningServiceClientExtension
{
    public static ServiceClientCredentials createCredentialsFromConnectionString(String connectionString)
    {
        ProvisioningConnectionString provisioningConnectionString = ProvisioningConnectionStringBuilder.createConnectionString(connectionString);
        if (!Tools.isNullOrEmpty(provisioningConnectionString.sharedAccessSignature))
        {
            return new SharedAccessSignatureCredentials(provisioningConnectionString.sharedAccessSignature);
        }
        else
        {
            return new SharedAccessKeyCredentials(provisioningConnectionString);
        }
    }
}
