/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask;

import com.microsoft.azure.sdk.iot.dps.device.privateapi.exceptions.DPSClientException;

public interface DPSRestResponseCallback
{
    void run(byte[] responseData, Object context) throws DPSClientException;
}
