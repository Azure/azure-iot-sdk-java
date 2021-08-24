/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Data structure for feedback record received
 * Provide getters and setters for feedback record batch properties
 */
public class FeedbackRecord
{
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Instant enqueuedTimeUtc;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String originalMessageId;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String correlationId;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private FeedbackStatusCode statusCode;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String description;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String deviceGenerationId;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String deviceId;
}
