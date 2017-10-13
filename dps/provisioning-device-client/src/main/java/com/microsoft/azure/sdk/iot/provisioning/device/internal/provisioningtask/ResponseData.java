/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device.internal.provisioningtask;

public class ResponseData
{
    byte[] responseData;
    ContractState dpsRegistrationState;

    ResponseData()
    {
        this.responseData = null;
        this.dpsRegistrationState = ContractState.DPS_REGISTRATION_UNKNOWN;
    }

    byte[] getResponseData()
    {
        return responseData;
    }

    void setResponseData(byte[] responseData)
    {
        this.responseData = responseData;
    }

    ContractState getDpsRegistrationState()
    {
        return dpsRegistrationState;
    }

    void setDpsRegistrationState(ContractState dpsRegistrationState)
    {
        this.dpsRegistrationState = dpsRegistrationState;
    }
}
