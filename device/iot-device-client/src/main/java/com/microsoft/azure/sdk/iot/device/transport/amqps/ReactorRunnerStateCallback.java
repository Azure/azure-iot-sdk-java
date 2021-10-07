// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.sdk.iot.device.transport.amqps;

/**
 * Callback for all Reactor level events that our AMQP code should handle
 */
interface ReactorRunnerStateCallback
{
    /**
     * Executed when the proton-j reactor closed unexpectedly. For example, if the reactor encountered a runtime
     * exception and threw a handler exception at the ReactorRunner class.
     */
    void onReactorClosedUnexpectedly();
}
