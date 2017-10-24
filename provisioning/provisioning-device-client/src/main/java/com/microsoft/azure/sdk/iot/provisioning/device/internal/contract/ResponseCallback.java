/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.contract;

import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;

public interface ResponseCallback
{
    void run(byte[] responseData, Object context) throws ProvisioningDeviceClientException;
}
