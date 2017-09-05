/*
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.deps.transport.http;

/**
 * An HTTPS message. An HTTPS message is distinguished from a plain IoT Hub
 * message by its property names, which are prefixed with 'iothub-app-';
 * and by the explicit specification of a content-type.
 */
public interface HttpMessage
{
    /**
     * Gets the message body.
     * @return The message body.
     */
    byte[] getBody();

    /**
     * Gets the content type string.
     * @return The message content-type.
     */
    String getContentType();
}
