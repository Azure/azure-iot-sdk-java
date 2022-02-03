/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service.transport.amqps;

import com.microsoft.azure.sdk.iot.service.messaging.IotHubMessageResult;

public interface AmqpFeedbackReceivedEvent
{
    IotHubMessageResult onFeedbackReceived(String feedbackJson);
}

