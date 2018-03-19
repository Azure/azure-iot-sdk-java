/*
 *
 *  Copyright (c) Microsoft. All rights reserved.
 *  Licensed under the MIT license. See LICENSE file in the project root for full license information.
 *
 */

package com.microsoft.azure.sdk.iot.device.connection;

public interface ReconnectionPolicy
{
    /**
     * Waits an amount of time determined by the retry policy and returns if the caller should attempt to retry again
     *
     * @return true if the retry policy wants the caller to attempt to retry again
     */
    boolean waitAndRetry();
}
