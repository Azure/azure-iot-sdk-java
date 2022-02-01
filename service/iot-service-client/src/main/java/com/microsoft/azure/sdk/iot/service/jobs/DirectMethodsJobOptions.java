// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.jobs;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * The optional parameters to be used when scheduling direct methods with
 * {@link JobClient#scheduleDirectMethod(String, String, String, Date, DirectMethodsJobOptions)}.
 */
@Builder
public class DirectMethodsJobOptions
{
    /**
     * The payload of the direct method request. May be null.
     */
    @Getter
    private final Object payload;

    /**
     * The timeout (in seconds) before the direct method request will fail if the device fails to respond to the request.
     * By default, there is no timeout.
     */
    @Getter
    @Builder.Default
    private final int methodResponseTimeout = 0;

    /**
     * The timeout (in seconds) before the direct method request will fail if the request takes too long to reach the device.
     * By default, there is no timeout.
     */
    @Getter
    @Builder.Default
    private final int methodConnectTimeout = 0;

    /**
     * The timeout for the direct method request job as a whole.
     */
    @Getter
    private final int maxExecutionTimeInSeconds;
}
