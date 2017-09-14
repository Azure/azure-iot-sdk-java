/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask;

import static com.microsoft.azure.sdk.iot.dps.device.privateapi.dpstask.DPSRestState.DPS_REGISTRATION_UNKNOWN;

class DPSRestResponseData
{
    byte[] responseData;
    DPSRestState dpsRegistrationState;

    public DPSRestResponseData()
    {
        this.responseData = null;
        this.dpsRegistrationState = DPS_REGISTRATION_UNKNOWN;
    }
}
