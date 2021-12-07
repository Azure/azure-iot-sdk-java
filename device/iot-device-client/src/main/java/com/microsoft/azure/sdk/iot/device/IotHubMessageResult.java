// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device;

/**
 * A return value from a message callback that instructs an IoT hub to complete, abandon, or reject the message. These
 * states are only valid for AMQP messages.
 * <p>
 *     See <a href="https://docs.microsoft.com/en-us/azure/iot-hub/iot-hub-devguide-messages-c2d#the-cloud-to-device-message-life-cycle"/>The cloud-to-device message life cycle</a>
 *     for more details on the states and what it means for message delivery and retry.
 * </p>
 */
public enum IotHubMessageResult
{
    /**
     * Instructs IoT hub to complete the message.
     * <p>
     *      This will remove it from the IoT hub messaging queue and set the message to the Completed state
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
    ABANDON,
    /**
     * Instructs IoT hub to reject the message.
     * <p>
     *      This will remove the message from the IoT hub messaging queue and sets the message to the dead lettered state.
     * </p>
     */
    REJECT
}
