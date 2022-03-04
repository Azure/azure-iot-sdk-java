/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import com.google.gson.Gson;
import com.microsoft.azure.sdk.iot.service.messaging.serializers.FeedbackRecordParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Provide static function to parse Json string to FeedbackBatch object.
 */
public class FeedbackBatchMessage
{
    /**
     * Parse received Json and create FeedbackBatch object
     *
     * @param jsonString Json string to parse
     * @return The created FeedbackBatch
     */
    public static FeedbackBatch parse(String jsonString)
    {
        FeedbackBatch returnFeedbackBatch = new FeedbackBatch();

        if (!Tools.isNullOrEmpty(jsonString))
        {
            if (jsonString.startsWith("Data{"))
            {
                jsonString = jsonString.substring(5, jsonString.length() - 1);
            }

            if (!jsonString.equals(""))
            {
                Gson gson = new Gson();
                FeedbackRecordParser[] feedbackRecordParsers = gson.fromJson(jsonString, FeedbackRecordParser[].class);

                if (feedbackRecordParsers.length > 0)
                {
                    List<FeedbackRecord> feedbackRecords = new ArrayList<>();
                    for (int i = 0; i < feedbackRecordParsers.length; i++)
                    {
                        feedbackRecords.add(new FeedbackRecord(feedbackRecordParsers[i]));
                    }

                    returnFeedbackBatch.setRecords(feedbackRecords);
                    returnFeedbackBatch.setEnqueuedTimeUtc(feedbackRecords.get(feedbackRecords.size() - 1).getEnqueuedTimeUtc());
                    returnFeedbackBatch.setUserId("");
                    returnFeedbackBatch.setLockToken("");
                    returnFeedbackBatch.setRecords(feedbackRecords);
                }
            }
        }
        return returnFeedbackBatch;
    }
}
