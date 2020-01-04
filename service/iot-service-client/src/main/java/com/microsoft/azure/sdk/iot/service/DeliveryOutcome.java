/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

/**
 * The possible delivery outcomes that a received AMQP message can be acknowledged with
 */
public enum DeliveryOutcome
{
    /**
     * Acknowledge the message as a failure, signal to service to send this message again later
     */
    Abandon,

    /**
     * Acknowledge the message as a failure, signal to service to not re-send this message
     */
    Reject,

    /**
     * Acknowledge the message as a success, service will not re-send this message
     */
    Complete
}
