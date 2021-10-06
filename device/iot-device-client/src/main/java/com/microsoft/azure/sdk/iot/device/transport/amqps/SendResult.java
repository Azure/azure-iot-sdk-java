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
     * The message was sent successfully.
     */
    SUCCESS,

    /**
     * The message belonged to a different device session.
     */
    WRONG_DEVICE,

    /**
     * The message was a twin/method subscription message and the device session has already sent that subscription message.
     */
    DUPLICATE_SUBSCRIPTION_MESSAGE,

    /**
     * The message was a twin/method message and this device session's twin/method subscription was still in progress.
     */
    SUBSCRIPTION_IN_PROGRESS,

    /**
     * Default case.
     */
    UNKNOWN_FAILURE
}
