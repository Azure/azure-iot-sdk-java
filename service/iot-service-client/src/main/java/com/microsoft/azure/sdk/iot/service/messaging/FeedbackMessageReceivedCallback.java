// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.service.messaging;

/**
 * The callback interface for handling cloud to device feedback messages received by {@link FeedbackReceiver} instances.
 */
public interface FeedbackMessageReceivedCallback
{
    /**
     * This callback is executed each time a cloud to device feedback message is received by the {@link FeedbackReceiver}.
     * Each feedback message batch must be completed or abandoned. See {@link IotHubMessageResult} for more details on what
     * completed and abandoned mean.
     * @param feedbackBatch The received cloud to device feedback message batch.
     * @return The way to acknowledge the received feedback message batch.
     */
    public IotHubMessageResult onFeedbackMessageReceived(FeedbackBatch feedbackBatch);
}
