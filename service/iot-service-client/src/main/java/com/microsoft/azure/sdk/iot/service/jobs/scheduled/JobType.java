/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

public enum JobType
{
    // List of possible jobs
    unknown,
    scheduleDeviceMethod,
    scheduleUpdateTwin
}
