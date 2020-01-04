/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

public interface FeedbackBatchMessageCallback
{
    /**
     * Called each time a file upload notification was received. The listener must return a delivery outcome so that
     * the amqp connection can acknowledge the message accordingly. The feedback batch cannot selectively acknowledge
     * each feedback message individually. They must all be completed, rejected or abandoned as a batch.
     *
     * @param feedbackBatch The received feedback batch that was received
     * @return Whether to Complete the message, Abandon it, or Reject it. See {@link DeliveryOutcome} for details
     */
    public DeliveryOutcome onFeedbackMessageReceived(FeedbackBatch feedbackBatch);
}
