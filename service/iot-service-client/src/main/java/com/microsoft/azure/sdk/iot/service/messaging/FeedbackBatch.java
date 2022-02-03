/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Data structure for feedback messages received in Json array
 * Provide getters and setters for batch properties and messages
 */
public class FeedbackBatch
{
    @Getter
    @Setter
    private Instant enqueuedTimeUtc;

    @Getter
    @Setter
    private String userId;

    @Getter
    @Setter
    private String lockToken;

    @Getter
    @Setter
    private ArrayList<FeedbackRecord> records;
}
