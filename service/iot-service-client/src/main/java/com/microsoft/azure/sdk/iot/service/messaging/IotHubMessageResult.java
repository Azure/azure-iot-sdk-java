// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.service.messaging;

/**
 * A return value from a message callback that instructs an IoT hub to complete or abandon the message. These
 * states are only valid for AMQP messages.
 */
public enum IotHubMessageResult
{
    /**
     * Instructs IoT hub to complete the message.
     * <p>
     *      This will remove it from the IoT hub messaging queue and set the message to the Completed state.
     * </p>
     */
    COMPLETE,

    /**
     * Instructs IoT hub to abandon the message.
     * <p>
     *     This will put the message back into the IoT hub messaging queue to be processed again. The message will
     *     be enqueued back to the IoT hub messaging queue for the value specified for "Max Delivery Count" times.
     *     After the max is reached, the message will be set to to the dead lettered state and removed.
     * </p>
     */
    ABANDON
}
