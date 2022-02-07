// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.jobs.scheduled;

import lombok.Builder;
import lombok.Getter;

import java.util.Date;

/**
 * The optional parameters to be used when scheduling direct methods with
 * {@link ScheduledJobsClient#scheduleDirectMethod(String, String, String, Date, DirectMethodsJobOptions)}.
 */
@Builder
public final class DirectMethodsJobOptions
{
    /**
     * The payload of the direct method request. May be null.
     */
    @Getter
    private final Object payload;

    /**
     * The timeout (in seconds) before the direct method request will fail if the device fails to respond to the request.
     * By default, this is set to 200 seconds (the maximum allowed value).
     */
    @Getter
    @Builder.Default
    private final int methodResponseTimeout = 200;

    /**
     * The timeout (in seconds) before the direct method request will fail if the request takes too long to reach the device.
     * By default, this is set to 200 seconds (the maximum allowed value).
     */
    @Getter
    @Builder.Default
    private final int methodConnectTimeout = 200;

    /**
     * The timeout for the direct method request job as a whole.
     * By default, this is set to 200 seconds.
     */
    @Getter
    @Builder.Default
    private final int maxExecutionTimeInSeconds = 200;
}
