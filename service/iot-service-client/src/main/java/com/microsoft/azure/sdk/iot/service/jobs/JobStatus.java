/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.jobs;

public enum JobStatus
{
    // List of possible IoTHub response status for a job
    unknown,
    enqueued,
    running,
    completed,
    failed,
    cancelled,
    scheduled,
    queued
}
