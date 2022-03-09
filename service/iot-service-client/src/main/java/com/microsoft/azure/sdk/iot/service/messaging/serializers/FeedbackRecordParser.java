// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging.serializers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.microsoft.azure.sdk.iot.service.ParserUtility;
import com.microsoft.azure.sdk.iot.service.messaging.FeedbackStatusCode;
import lombok.Getter;

import java.util.Date;

import static com.microsoft.azure.sdk.iot.service.messaging.FeedbackStatusCode.unknown;

public class FeedbackRecordParser
{
    private static final String DEVICE_ID_TAG = "deviceId";
    @Expose
    @SerializedName(DEVICE_ID_TAG)
    @Getter
    private String deviceId = null;

    private static final String ORIGINAL_MESSAGE_ID_TAG = "originalMessageId";
    @Expose
    @SerializedName(ORIGINAL_MESSAGE_ID_TAG)
    @Getter
    private String originalMessageId = null;

    private static final String DESCRIPTION_TAG = "description";
    @Expose
    @SerializedName(DESCRIPTION_TAG)
    @Getter
    private String description = null;

    private static final String STATUS_CODE_TAG = "statusCode";
    @Expose
    @SerializedName(STATUS_CODE_TAG)
    @Getter
    private FeedbackStatusCode statusCode = unknown;

    private static final String DEVICE_GENERATION_ID_TAG = "deviceGenerationId";
    @Expose
    @SerializedName(DEVICE_GENERATION_ID_TAG)
    @Getter
    private String deviceGenerationId = null;

    private static final String ENQUEUED_TIME_UTC_TAG = "enqueuedTimeUtc";
    @Expose
    @SerializedName(ENQUEUED_TIME_UTC_TAG)
    private String enqueuedTimeUtc = null;

    /**
     * Empty constructor: Used only to keep GSON happy.
     */
    @SuppressWarnings("unused")
    FeedbackRecordParser()
    {
    }

    public Date getEnqueuedTimeUtcDate()
    {
        return ParserUtility.getDateTimeUtc(this.enqueuedTimeUtc);
    }
}
