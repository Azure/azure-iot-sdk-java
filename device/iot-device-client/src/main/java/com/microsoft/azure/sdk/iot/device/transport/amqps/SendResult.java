/*
*  Copyright (c) Microsoft. All rights reserved.
*  Licensed under the MIT license. See LICENSE file in the project root for full license information.
*/

package com.microsoft.azure.sdk.iot.device.transport.amqps;

/**
 * All the different results of a send message operation that an AMQP device session can report
 */
enum SendResult
{
    /**
     * Default case.
     */
    UNKNOWN_FAILURE,

    /**
     * The message was sent successfully.
     */
    SUCCESS,

    /**
     * A multiplexed device sends messages only on it's session's links. This error occurs if the connection attempts
     * to send a message over a different device's session.
     */
    WRONG_DEVICE,

    /**
     * The message was a twin/method subscription message and the device session has already requested to be subscribed to that topic.
     */
    DUPLICATE_SUBSCRIPTION_MESSAGE,

    /**
     * The message was a twin/method message and this device session's twin/method subscription was still in progress.
     */
    SUBSCRIPTION_IN_PROGRESS,

    /**
     * The session attempted to send the message, but it didn't have the required links open. For instance, if a twin
     * message is sent when the twin sender link isn't open yet. This should never happen though, because telemetry links
     * are always opened along with the session, and our API design makes it so twin/method links are always opened
     * before any twin/method messages can be sent.
     */
    LINKS_NOT_OPEN
}
