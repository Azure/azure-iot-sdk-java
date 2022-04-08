/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.microsoft.azure.sdk.iot.service.messaging.serializers.FeedbackRecordParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Data structure for feedback record received
 * Provide getters and setters for feedback record batch properties
 */
public class FeedbackRecord
{
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private Date enqueuedTimeUtc;

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

    protected FeedbackRecord(FeedbackRecordParser parser)
    {
        this.correlationId = "";
        this.description = parser.getDescription();
        this.deviceGenerationId = parser.getDeviceGenerationId();
        this.deviceId = parser.getDeviceId();
        this.enqueuedTimeUtc =  parser.getEnqueuedTimeUtcDate();
        this.originalMessageId = parser.getOriginalMessageId();

        if (parser.getStatusCode() == null)
        {
            this.statusCode = FeedbackStatusCode.unknown;
        }
        else
        {
            this.statusCode = parser.getStatusCode();
        }
    }
}
