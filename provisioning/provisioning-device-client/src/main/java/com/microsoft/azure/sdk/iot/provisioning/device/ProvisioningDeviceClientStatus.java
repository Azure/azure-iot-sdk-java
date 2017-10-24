/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.provisioning.device;

public enum ProvisioningDeviceClientStatus
{
    DPS_DEVICE_STATUS_UNAUTHENTICATED,
    DPS_DEVICE_STATUS_READY_TO_AUTHENTICATE,
    DPS_DEVICE_STATUS_AUTHENTICATED,
    DPS_DEVICE_STATUS_ASSIGNING,
    DPS_DEVICE_STATUS_ASSIGNED,
    DPS_DEVICE_STATUS_ERROR
}
