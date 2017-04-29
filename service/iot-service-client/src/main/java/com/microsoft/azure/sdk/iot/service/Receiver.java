/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */

package com.microsoft.azure.sdk.iot.service;

import java.util.concurrent.CompletableFuture;

/**
* A receiver is a class abstracting the methods to receive messages,
* in order to use it for multiple endpoints, e.g. feedback endpoint,
* “alerts” (i.e. D2C).
*/
public abstract class Receiver
{
    protected abstract CompletableFuture<Void> openAsync();
    protected abstract CompletableFuture<Void> closeAsync();

    protected abstract CompletableFuture<?> receiveAsync();
    protected abstract CompletableFuture<?> receiveAsync(long timeoutSeconds);
}
