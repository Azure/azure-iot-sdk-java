// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.sdk.iot.device.twin;

/**
 * The callback to be executed each time a direct method is invoked on this client.
 */
public interface MethodCallback
{
    /**
     * The
     * @param methodName The name of the method being invoked.
     * @param methodPayload The payload of the method being invoked. May be null
     * @param context The context set when subscribing to direct methods. Will be null if no context was set when subscribing.
     * @return The direct method response to deliver to the process that invoked this method. May not be null.
     */
    DirectMethodResponse onMethodInvoked(String methodName, DirectMethodPayload methodPayload, Object context);
}
