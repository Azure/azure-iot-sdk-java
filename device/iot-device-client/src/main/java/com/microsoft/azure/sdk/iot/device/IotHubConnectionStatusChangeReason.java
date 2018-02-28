/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device;

public enum IotHubConnectionStatusChangeReason
{
    EXPIRED_SAS_TOKEN,
    BAD_CREDENTIAL,
    RETRY_EXPIRED,
    NO_NETWORK,
    COMMUNICATION_ERROR,
    CONNECTION_OK
}
