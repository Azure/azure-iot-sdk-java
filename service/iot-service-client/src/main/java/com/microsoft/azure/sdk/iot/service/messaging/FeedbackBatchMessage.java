/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.messaging;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;

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
                try (JsonReader jsonReader = Json.createReader(new StringReader(jsonString)))
                {
                    JsonArray jsonArray = jsonReader.readArray();
                    ArrayList<FeedbackRecord> records = new ArrayList<>();

                    for (JsonValue aJsonArray : jsonArray)
                    {
                        JsonObject jsonObject = (JsonObject) aJsonArray;

                        FeedbackRecord feedbackRecord = new FeedbackRecord();

                        feedbackRecord.setEnqueuedTimeUtc(Instant.parse(Tools.getValueFromJsonObject(jsonObject, "enqueuedTimeUtc")));

                        String originalMessageId = Tools.getValueFromJsonObject(jsonObject, "originalMessageId");
                        feedbackRecord.setOriginalMessageId(originalMessageId);
                        feedbackRecord.setCorrelationId("");

                        String description = Tools.getValueFromJsonObject(jsonObject, "description");
                        feedbackRecord.setDescription(description);
                        String statusCode = Tools.getValueFromJsonObject(jsonObject, "statusCode");
                        if (statusCode.equalsIgnoreCase("success"))
                        {
                            feedbackRecord.setStatusCode(FeedbackStatusCode.success);
                        }
                        else if (statusCode.equalsIgnoreCase("expired"))
                        {
                            feedbackRecord.setStatusCode(FeedbackStatusCode.expired);
                        }
                        else if (statusCode.equalsIgnoreCase("deliverycountexceeded"))
                        {
                            feedbackRecord.setStatusCode(FeedbackStatusCode.deliveryCountExceeded);
                        }
                        else if (statusCode.equalsIgnoreCase("rejected"))
                        {
                            feedbackRecord.setStatusCode(FeedbackStatusCode.rejected);
                        }
                        else
                        {
                            feedbackRecord.setStatusCode(FeedbackStatusCode.unknown);
                        }
                        feedbackRecord.setDeviceId(Tools.getValueFromJsonObject(jsonObject, "deviceId"));
                        feedbackRecord.setDeviceGenerationId(Tools.getValueFromJsonObject(jsonObject, "deviceGenerationId"));

                        records.add(feedbackRecord);
                    }

                    if (records.size() > 0)
                    {
                        returnFeedbackBatch.setEnqueuedTimeUtc(records.get(records.size() - 1).getEnqueuedTimeUtc());
                        returnFeedbackBatch.setUserId("");
                        returnFeedbackBatch.setLockToken("");
                        returnFeedbackBatch.setRecords(records);
                    }
                }
            }
        }
        return returnFeedbackBatch;
    }
}
